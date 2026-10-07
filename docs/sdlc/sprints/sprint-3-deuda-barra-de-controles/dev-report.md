# Dev report — S-3.1 Deuda de la barra de controles

Rama `feature/window-controls-debt`. Dispositivo: SM-X910 (adb `R52X603XGQT`, Android 16, Shizuku corriendo). El orquestador dijo que ese serial era un teléfono; `getprop ro.product.model` devuelve `SM-X910` (`gts9uwifi`), así que sí es la tablet y se verificó en ella.

## Qué cambió
- `components/WindowControlsBar.kt`
  - `ForegroundTask` gana `displayId` y `activity`. `targetTask(tasks, displayId, launcher, own)` recibe la lista de tareas en ejecución, toma la primera de **su** display y aplica los filtros previos (freeform, launcher, ClassicDock, systemui) más Recents.
  - `displayId` de `RunningTaskInfo` se lee por reflexión del campo público (`getField("displayId")`). Fallo seguro: si no se puede leer vale `INVALID_DISPLAY_ID` (-1), nunca coincide y la barra se oculta. El display de la barra sale de `context.display.displayId` (con `runCatching`, default 0).
  - `update()` ya no llama a `getRunningTasks` en el hilo principal: un `Executors.newSingleThreadExecutor` resuelve la tarea y el resultado se aplica con `view.post`. Eventos en ráfaga se fusionan con un `AtomicBoolean` (no se encolan N consultas). `destroy()` hace `shutdownNow`.
  - Cerrar: `WindowControlsPolicy.close(available, task, remove)` devuelve `CLOSED / NO_TARGET / UNAVAILABLE`. Sin Shizuku (wrapper nulo o `!isAlive()`) o con `removeTask` lanzando: `UNAVAILABLE` -> `Toast` + aviso dentro de la barra (3 s). id <= 0 o sin tarea: `NO_TARGET`, no llama (como antes).
  - El clic de cerrar también resuelve y quita la tarea en el hilo secundario (misma razón que update).
- `res/layout/window_controls.xml`: botones envueltos en `window_buttons` + `TextView window_notice`.
- Strings `close_needs_shizuku` (values, values-es).
- `docs/maintenance.md`: trampa de Toast suprimido. `docs/progress/s-3.1-{fullscreen,recents,aviso-shizuku}.crop.png`.
- `WindowControlsPolicyTest.kt`: 19 pruebas (eran 8).

## Causa raíz medida de Recents
Recents en esta tablet es la tarea `type=recents` (activityType 3, id 17) con actividad `com.sec.android.app.launcher/com.android.quickstep.RecentsActivity` (`dumpsys activity activities` y `recents`, `mRecentsComponent` igual). Su **paquete es el del launcher** (`com.sec.android.app.launcher`), no systemui. Con el launcher por defecto de Samsung ya quedaba excluido por el filtro de launcher: medido con el build anterior, en Recents la ventana de la barra tenía `mViewVisibility=0x8`. El hueco real es con otro launcher por defecto: Samsung sigue alojando Recents en TouchWizHome, y entonces el paquete ya no coincide con `getCurrentLauncher` y la barra saldría. Por eso se filtra por la clase de actividad `com.android.quickstep.RecentsActivity` (la misma que ya excluye `AppUtils.getRunningTasks`). No pude reproducir ese caso en la tablet (no se cambió el launcher por defecto); lo cubre el test `recentsNoEsObjetivoAunqueSuPaqueteNoSeaElLauncher`.

## Verificación en tablet
- Reloj a pantalla completa: barra visible (`0x0`, 162x49 px) -> `displayId` se lee bien y coincide con 0. Home y Recents: `0x8`. Cerrar con Shizuku: la tarea del reloj desaparece de `am stack list`.
- Shizuku caído (`kill` del pid de `shizuku_server`) y tap en cerrar: la tarea no se cierra, sale el aviso en la barra («Sin Shizuku no se puede cerrar la app», visible ~2.4 s después del tap) y luego la barra se oculta.
- Hallazgo: **el Toast no se ve en esta tablet.** `Toast.show` corre pero el sistema lo suprime (`NotificationService: Suppressing toast from package dev.isidro.classicdock by user request`); las notificaciones de la app están bloqueadas y su interruptor está inhabilitado (la app no declara `POST_NOTIFICATIONS`). Un Toast solo habría dejado el silencio que la story prohíbe, así que se añadió el aviso dentro de la barra además del Toast.
- Dos bugs propios hallados al medir y corregidos: el aviso desaparecía en <0.3 s porque el `update()` que dispara el propio tap ocultaba la barra; se bloquea la visibilidad mientras `warning` está activo (también en el `view.post` del resultado asíncrono).
- No verificado en dispositivo: filtro por display con un segundo display real (la tablet no tiene) y ausencia de bloqueo del hilo principal (por construcción, sin StrictMode/trace).

## Tests
`./gradlew assembleDebug testDebugUnitTest` exit 0, 19 pruebas, 0 fallos. Mutación: sin el filtro de display fallan 4; quitando además Recents, 5.

## Ajuste post-QA (ciclo 2)
Decisión de Isidro: con Shizuku caído la barra sigue visible si hay tarea objetivo; Minimizar funciona y Cerrar avisa.
- `getRunningTasks` sin Shizuku solo devuelve las tareas propias (Android 5+), así que no resuelve la tarea ajena. Comportamiento mínimo: `WindowControlsBar` recibe `foregroundPackage: (displayId) -> String?`; `DockService` lo implementa con `windowsOnAllDisplays[display]` (el servicio ya tiene `flagRetrieveInteractiveWindows`): ventana `TYPE_APPLICATION` activa de ese display y su `root.packageName`. `WindowControlsPolicy.fallbackTask` lo vuelve un `ForegroundTask` (id -1, modo pantalla completa asumido) y pasa por los mismos filtros (launcher, ClassicDock, systemui). Con Shizuku vivo nada cambia.
- Límites sin Shizuku: no se conoce el modo de ventana (una ventana freeform muestra la barra) ni la actividad (Recents con un launcher distinto al de Samsung no se excluye). Cerrar siempre cae en `UNAVAILABLE` (id -1): aviso en la barra + Toast.
- `update()` ya no oculta la barra cuando `activityManager()` es nulo.
- QA 2: `warning` y `destroyed` ahora `@Volatile`. QA 3: `executor.execute` va envuelto (`runInBackground`) y captura `RejectedExecutionException` tras `shutdownNow`.
- Tests: 22 (3 nuevos: fallback con app, fallback con launcher/systemui/propia/nulo, id -1 no cierra y avisa). `./gradlew assembleDebug testDebugUnitTest --offline` exit 0.
- Tablet SM-X910 (R52X603XGQT), build nuevo, `kill` de `shizuku_server` (pid 30809): Brave a pantalla completa -> ventana de la barra `mViewVisibility=0x0`; Home -> `0x8`; Minimizar (tap) -> launcher al frente; Cerrar (tap) -> aviso «Sin Shizuku no se puede cerrar la app» en la barra y Brave sigue como tarea resumida; `Suppressing toast` confirma que el Toast sigue suprimido. Captura: `docs/progress/s-3.1-sin-shizuku-aviso.crop.png`. Shizuku se volvió a arrancar (pid 5828) y Brave se cerró con `force-stop`.
- Sin verificar: segundo display real, ni el caso de que la ventana activa de otro display sea la que cuenta.

## Decidí NO hacer
- No tocar `DockService.kt`: la API pública de la barra (`update`/`destroy`, constructor) no cambió.
- No reutilizar `AppUtils.getRunningTasks`: carga íconos/labels y no expone display (decisión de S-2.1 vigente).
- No declarar `POST_NOTIFICATIONS` ni cambiar permisos del dispositivo: fuera de alcance; el aviso en la barra evita depender de ello. Los Toast existentes de ClassicDock (p. ej. `start_message`) sufren lo mismo; no los toqué.
- No medir el caso "otro launcher" cambiando el launcher por defecto de la tablet.
- `displayId` ilegible oculta la barra (en vez de actuar sin filtro): preferí perder la barra en un ROM raro que cerrar la tarea de otro display.

## Estado del dispositivo al terminar
Shizuku corriendo, servicio de ClassicDock activo con el build de esta rama instalado. Se quitaron las capturas temporales de `/sdcard`.

## Ronda de code-review
Commits `d818095` (*.800.png) y `fe77b70` (código). `./gradlew assembleDebug testDebugUnitTest --offline` exit 0, 22 pruebas (`WindowControlsPolicyTest`), 0 fallos.

HACER
- 1 `close()` recibe `remove: (Int) -> Boolean`; `false` -> nuevo `CloseResult.FAILED` (aviso `close_failed`, values y values-es, mismo aviso en barra + Toast). Test `removeTaskQueDevuelveFalseNoCuentaComoCerrada`.
- 2 `foregroundPackage` ya no corre en el executor: `update()` y `closeForeground()` lo resuelven en el hilo que llama (principal: evento de accesibilidad, clic, `postDelayed`) y lo guardan en `@Volatile fallbackPackage`; el executor solo lee el String. Se guarda en cada llamada (no solo en la que gana el `queued`) para que una ráfaga no deje un paquete viejo. No se reciclan nodos (el repo no lo hace en ningún lado; en API 33+ es no-op, minSdk es 30, así que en 30-32 el `root` sigue sin reciclarse, igual que antes).
- 4 `runOnTask` y sus 5 pruebas borrados (sin uso en producción); lo que cubrían ahora lo cubre `close`. También se borró `INVALID_DISPLAY_ID` y su prueba (sin uso: ver 3).
- 5 `Log.w` de la excepción de `removeTask` (en la barra, el policy sigue puro). `SecurityException` y `RemoteException` (incluye `DeadObjectException`) -> `UNAVAILABLE` («Shizuku no disponible»); cualquier otra excepción -> `FAILED`. Pruebas incluidas.
- 7 `getRunningTasks(20)` (constante `MAX_TASKS`).
- 9 `git rm --cached` de `s-3.1-{fullscreen,recents}.crop.800.png` (los únicos `.800.png` que añade esta rama respecto a `dev`) y `*.800.png` en `.gitignore`. Los 13 `.800.png` de S-1.x/S-2.x ya vienen de `dev`; no los toqué (el story 2.1 los cita como evidencia); siguen trackeados aunque el patrón los ignore.

JUZGAR
- 3 Hecho. `getRunningTasks` que lanza o `displayId` por reflexión ausente ahora caen al fallback de accesibilidad con `Log.w`, en vez de ocultar la barra: el usuario conserva Minimizar y Cerrar avisa. Sustituye la decisión previa «displayId ilegible oculta la barra». Coste: una consulta de accesibilidad por cada `update()` aunque Shizuku vaya bien (sin medir; las ráfagas son eventos del propio servicio). Si pesa, resolverla solo cuando `am == null`, a costa de volver al caso 3 sin fallback.
- 6 Eran reales los tres. `update()` ya no sale temprano con aviso: si el resultado es «sin objetivo» (cambio a launcher/Recents) cierra el aviso (`endNotice`, cancela el timer) y oculta la barra; `warn()` ignora el resultado tardío si la barra no está visible o ya hay `destroyed`; `destroy()` cancela `hideNotice` y `hideNotice`/el `view.post` salen si `destroyed`. Medido sin Shizuku: Cerrar -> aviso (barra 384 px de ancho); Home 0.6 s después -> `mViewVisibility=0x8` (antes el aviso aguantaba 3 s sobre el launcher). Con warning activo y la app aún al frente, la barra se mantiene (el update del propio tap ya no la oculta; visto en la tablet).
- 8 No cambié el color. La barra es una píldora de fondo fijo `#D9292929` con iconos fijos `#FFFFFFFF`; un color de tema (claro en tema oscuro, oscuro en tema claro) dejaría texto oscuro sobre fondo oscuro. Sí añadí `maxLines=1`, `ellipsize=end` y `maxWidth=320dp`. Medido en la tablet: texto en español 384 px de barra total, en inglés (`cmd locale set-app-locales ... en-US`, ya restaurado) 492 px = 281 dp, ambos completos y sin recorte; maxWidth solo protege pantallas angostas.

Medido en tablet SM-X910 (build nuevo, servicio reiniciado): con Shizuku, Brave a pantalla completa -> barra `0x0`; Cerrar -> tarea fuera de `am stack list` y barra `0x8` en <0.7 s (2 de 2 corridas; una corrida justo tras reiniciar el servicio la vio `0x0` a los 2 s y `0x8` poco después, no se reprodujo). Sin Shizuku: Cerrar -> aviso, Home -> oculta.
No verificado: rama `FAILED` en dispositivo (no hay forma de forzar `removeTask` a `false`), cada ruta de 3 con un ROM sin `displayId`, y el coste de la consulta de accesibilidad.
Estado final: Shizuku corriendo (pid 10220, reiniciado con `libshizuku.so`), servicio de ClassicDock activo, tablet en Home, locale de la app restaurado.
