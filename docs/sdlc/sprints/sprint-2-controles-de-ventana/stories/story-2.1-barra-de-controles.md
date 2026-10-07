# S-2.1 — Barra de controles de ventana

- Épica: E-2 · Cubre: FR-09 · Complejidad: M · Rama: `feature/window-controls` (desde `dev`)
- Dev: sdlc-dev · QA: sdlc-qa (captura en la tablet SM-X910, adb `R52X603XGQT`)

## Historia
Como Isidro quiero minimizar y cerrar visibles sobre la app en primer plano, también a pantalla completa.

## Criterios de aceptación
1. Given una app a pantalla completa When miro la parte superior Then veo una barra fina de ClassicDock con minimizar y cerrar.
2. Given una ventana flotante (`windowingMode == FREEFORM`) When la miro Then la barra no se muestra.
3. Given los dos botones When los toco Then minimizan (Home) o cierran (`removeTask`) la tarea en primer plano.
4. Given que la tarea en primer plano es launcher, systemui o ClassicDock Then la barra no se muestra y no actúa sobre ellos.
5. Given Shizuku sin correr (wrapper nulo) Then la barra no se muestra (sin Shizuku no se conoce la tarea en primer plano); con id <= 0 o tarea nula, cerrar no hace nada y no hay crash.
6. La barra no tapa contenido relevante de la app: superposición fina, altura medida en captura.

## Diseño (restricciones)
- Vista y clase propias, p.ej. `components/WindowControlsBar.kt` + `res/layout/window_controls.xml`. DockService solo la crea, la actualiza con la tarea en primer plano y la destruye: añadir lo mínimo (<~40 líneas) a `DockService.kt` (ya 2,592 líneas).
- Reutilizar `Utils.makeWindowParams` (overlay `TYPE_ACCESSIBILITY_OVERLAY`), `ActivityManagerWrapper.removeTask`, `AppUtils.getRunningTasks`, `AppTask.windowingMode`, constantes `WINDOWING_MODE_*`. Cambio de modo a fullscreen: añadir a `ActivityManagerWrapper` un método sobre `IActivityManager` (p.ej. `setTaskWindowingMode`/`resizeTask` según exista en el stub; verificar contra la API real, no memoria) .
- Estilo coherente con el dock flotante (tema `fully_transparent`, íconos 44dp del dock se reducen para la barra; ver `docs/maintenance.md`).
- Código sin comentarios salvo lógica no obvia; español de México en strings.

## Foco de revisión
- Tarea objetivo mal identificada (launcher, systemui, ClassicDock): test unit del filtro de tarea objetivo.
- Barra visible sobre flotantes: test unit de la regla de visibilidad por `windowingMode`.
- Cerrar/maximizar con id -1: no hacer nada, sin crash (test unit).
- Contenido tapado: medir en captura.
- `DockService.kt` crece lo mínimo.

## Verificación en tablet (QA)
`scripts/deploy.sh`; tras `am force-stop dev.isidro.classicdock` reponer `enabled_accessibility_services` (leer la lista actual, añadir el servicio), **esperar 3 s** y reiniciar el servicio para que tome Shizuku (ver `docs/maintenance.md`). Shizuku debe estar corriendo. Capturas en `docs/progress/s-2.1-*.png` (y `.800.png`).

## Reporte Dev
Rama `feature/window-controls`. Barra: `components/WindowControlsBar.kt` (política pura `WindowControlsPolicy` + vista), `res/layout/window_controls.xml`, `drawable/window_controls_bg.xml`, `drawable/ic_minimize.xml`, strings `minimize`/`maximize` (values y values-es). `DockService.kt` +12 líneas (campo, creación, `update()` en eventos WINDOWS_CHANGED/WINDOW_STATE_CHANGED, destrucción). `ActivityManagerWrapper.setTaskFullscreen`; `AppUtils.getCurrentLauncher` pasó de private a público; `app/build.gradle` + `testImplementation junit:junit:4.13.2`.

Diseño: barra de 132x28dp (medido en `dumpsys window`: frame [1364,0][1596,49] px, 49 px de 1848 = 2.7 %), arriba al centro, `TYPE_ACCESSIBILITY_OVERLAY` vía `Utils.makeWindowParams(..., true)`. Objetivo = primera tarea en ejecución de `getTasks(5)` por Shizuku (no se salta a la siguiente: si el launcher va al frente la barra se oculta). Sin Shizuku (wrapper nulo) no hay objetivo y la barra no sale.

Tests: `./gradlew testDebugUnitTest` exit 0, 7 pruebas (filtro de objetivo: launcher/systemui/ClassicDock/null; visibilidad: FREEFORM oculta, fullscreen y modo 6 muestran; id -1, 0 y tarea nula no ejecutan; id real sí). `./gradlew assembleDebug` exit 0.

Medido en la tablet (SM-X910, Android 16, Shizuku corriendo; sin capturas, eso es de QA):
- Home: ventana de la barra `mViewVisibility=0x8` (oculta). Reloj a pantalla completa: `0x0` (visible). Calculadora `--windowingMode 5`: `0x8` (oculta).
- Minimizar: reloj -> topResumed pasa a LauncherActivity. Cerrar: la tarea 47 desaparece de `am stack list`. Maximizar: sin crash, pero el sistema registra `ActivityTaskManager: resizeTask not allowed on task=#47` (no-op).
- AC5 con Shizuku caído no se probó en tablet (no se detuvo Shizuku); cubierto por unit (`runOnTask` con id -1/0/nulo) y `activityManager()?` nulo.

### Desviaciones
0. **Decisión de Isidro (2026-10-07): se quita el botón maximizar.** Android 16 no permite cambiar el modo de ventana de una tarea por esta vía (medido, ver punto 1), así que el botón era un no-op. Se eliminaron del repo el botón del layout, su listener, `ActivityManagerWrapper.setTaskFullscreen` y las strings `maximize` (values y values-es). La barra queda con minimizar y cerrar (92x28dp en lugar de 132x28dp; las medidas de arriba son de la versión con tres botones). AC1 y AC3 pasan a "minimizar y cerrar". Los puntos 1 y 3 siguientes se conservan como motivo histórico.
1. **Maximizar no cambia el modo de ventana (AC3 parcial).** `setTaskWindowingMode` no existe en `IActivityManager` ni `IActivityTaskManager` de AOSP android16-release (grep del AIDL descargado; el stub del repo solo declara getTasks/resizeTask/removeTask, y en runtime se usa la clase real del framework). `resizeTask` solo actúa sobre tareas freeform/multiventana (`am help`) y no cambia su modo: medido, calculadora freeform + `am task resize 48 0 0 2960 1848` queda `mode=freeform`; sobre una tarea fullscreen el sistema responde "resizeTask not allowed". Además AC2 oculta la barra en freeform, así que el botón solo se ve sobre tareas que ya son fullscreen: su efecto real es nulo. Opciones para Isidro: quitar el botón, o historia aparte con `WindowContainerTransaction.setWindowingMode` (API de organizer por reflexión, riesgo medio). Se dejó el botón con `setTaskFullscreen` (resizeTask con `Rect()` vacío, como la acción "fullscreen" del menú contextual).
2. Sin Shizuku la barra no aparece (el plan hablaba de degradar a solo minimizar): sin él no se conoce la tarea en primer plano ni su modo.
3. `getCurrentLauncher` se hizo público en vez de duplicar la resolución del launcher.

### Experimento que ensució la tablet (no es repo)
Probando `am stack move-task 48 46 true` (moveTaskToRootTask) para pasar la calculadora a fullscreen, system_server lanzó `ClassCastException` y luego un NPE en `force-stop`. Quedó una RootTask fantasma id=46 con taskId=48 (calculadora, sin actividad) en `am stack list`; `am stack remove 46` no la quita. Es inocua para la barra (`isRunning` falso la descarta) y se limpia al reiniciar la tablet. No usar moveTaskToRootTask: no es una vía viable.

### No hecho
- Sin ajuste de posición/ancho por preferencia, sin animación de la barra, sin aviso cuando Shizuku no corre (no pedidos).
- No se tocó la lógica existente del menú contextual ni `updateRunningTasks`.

## QA (2026-10-07, ciclo 1)
Pasa: AC1-AC4 y AC6 verificados en la tablet (capturas `docs/progress/s-2.1-*.png`; barra 162x49 px, 2.7 % del alto); AC5 por unit. Pendiente conocido: el filtro de systemui solo lo cubre el unit test; `update()` y la vista no tienen test.

## Correcciones del code-review (2026-10-07)
- `DockService.removeAllViews`: `windowControls.destroy()` va en su propio try, después del bloque compartido (si lanza no filtra el resto de vistas).
- `WindowControlsBar`: `getMethod("getWindowingMode")` envuelto en `runCatching` (nulo si falla; `update()` oculta la barra vía el catch). No se reutilizó `AppUtils.getRunningTasks`: filtra distinto (baseActivity, systemui/Recents) y carga íconos/labels; duplicaría trabajo caro en cada evento.
- Cerrar re-resuelve la tarea en primer plano al hacer clic y aplica `targetTask` antes de `removeTask`; ya no usa el objetivo cacheado. `update()` y el clic comparten `resolveTarget()`; su catch ahora hace `Log.w`.
- `createViews` llama `update()` tras construir la barra.
- Tests: 8 pruebas (nuevo: launcher al frente al clic no ejecuta nada), `assembleDebug testDebugUnitTest` exit 0. Tablet: reloj fullscreen -> `mViewVisibility=0x0` (frame 162x49 px); tap en cerrar -> tarea 56 desaparece de `am stack list`, topResumed = launcher, barra `0x8`.
- Capturas: se quitaron del git (y del disco) los originales de tamaño completo `s-2.1-{fullscreen,launcher,freeform}.png`; quedan `.800.png` y `.crop.png`.
- Deuda anotada, no hecha: filtro por display, exclusión de Recents, `update()` fuera del hilo principal, aviso por Shizuku caído.
