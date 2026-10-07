# Spike: ventanas flotantes (Shizuku) y teclado (accesibilidad), Android 16

Fecha 2026-10-07. Tablet SM-X910, Android 16, One UI 8.0.5, pantalla 2960x1848 en modo DeX (barra de estado arriba, taskbar Samsung abajo). Base: dev 1ee1c3d. Evidencias en /tmp/spike (resize-matrix.txt, two-windows.txt, keys-raw-evidence.txt, snap-left.png, two-snap.png).

## A) Mover/redimensionar ventanas freeform: FACTIBLE

Shizuku arrancado por adb (pid 18697) según maintenance.md. ClassicDock lo recibió: apareció el diálogo «¿Permitir acceder a Shizuku?», se concedió «todo el tiempo», y tras `force-stop` + reponer accesibilidad `getRunningTasks` devolvió ids reales (`TASKS [93:...popupcalculator, 16:..., 22:..., 21:..., 32:...]`).

Caminos medidos, todos cambian los bounds de una tarea freeform (Calculadora, task 93):
- `adb shell am task resize <id> l t r b` (uid 2000, equivale a Shizuku): OK.
- `adb shell cmd activity task resize ...`: OK (mismo comando).
- Código real de la app, `ActivityManagerWrapper.resizeTask` (IActivityManager.resizeTask vía ShizukuBinderWrapper, AIDL propio): OK. Request 1480,53,2960,1848 sobre [0,53][1480,1848] dio [1480,53][2960,1848]. Sin excepción en logcat.
- `IActivityTaskManager.resizeTask` por reflexión + Shizuku (servicio `activity_task`): OK, [0,53][1000,900] pedido y obtenido.
- Dos ventanas lado a lado, ClassicDock task 94 por la ruta Shizuku de la app a [0,53][1480,1848] y Calculadora a [1480,53][2960,1848]: OK, confirmado con captura (two-snap.png muestra las dos mitades; la captura se tomó antes de reordenar la segunda, los bounds finales salen de `am stack list`).

Matriz medida (pedido -> resultado):
| pedido | resultado |
|---|---|
| 0 0 1480 1848 | [0,53][1480,1901] (top se empuja a 53 conservando alto: el borde inferior se sale 53 px) |
| 0 53 1480 1848 | [0,53][1480,1848] OK |
| 1480 53 2960 1848 | [1480,53][2960,1848] OK |
| 0 53 1480 950 / 1480 950 2960 1848 | exactos (mitades superior/inferior) |
| 0 53 2960 1848 | exacto (maximizado visual) |
| 1480 924 2960 1848 | exacto |
| 100 100 200 200 | [100,100][632,632] (tamaño mínimo 532 px) |
| 2500 100 3500 700 | aceptado, queda fuera de pantalla (sin clamp a derecha) |
| left negativo | `am task resize` rechaza («bad left arg»); por Binder no se probó |
| tarea fullscreen (Claude, task 21) | sin efecto: resize solo actúa en freeform |

Reglas para snap: top mínimo = 53 (alto de la barra de estado en este modo; con `top<53` la ventana se desplaza abajo y se corta). Usar top=53 y bottom=altoPantalla; izquierda/derecha = mitades en x=1480; cuartos con y=950 (53..950 / 950..1848 o el punto medio calculado). Hay que restar también la taskbar de Samsung si se quiere no taparla: a la captura se ve a abajo; no se midió su altura (bottom 1848 se solapa con ella). No cambia el modo de ventana: una tarea fullscreen no se vuelve flotante con resize (el código del repo ya relanza con `--windowingMode 5` para eso; `am start --windowingMode 5` funcionó).

Hallazgo de código: `AppUtils.resizeTask` (usado por los atajos Alt+flechas/F3) usa `DeviceUtils.runAsRoot("am task resize ...")`, que sin root no hace nada. El camino bueno es `ActivityManagerWrapper.resizeTask` (Shizuku), que está medido funcionando; el AIDL propio con orden de métodos 1,2,3 no explota en Android 16 (resizeTask corrió sin error y movió la ventana).

Alternativas no necesarias (no se exploraron `cmd window`/`wm`/shell transitions): `cmd window help` no tiene comandos de bounds de tareas.

## B) Teclas en el AccessibilityService: FACTIBLE (con una pregunta abierta)

Método: `adb shell input keycombination` NO llega al servicio (0 eventos, la inyección de `input` no pasa por el filtro; probados 16 combos). `sendevent` a /dev/input/event14 (HL Keyboard real): permiso denegado. Sí funcionó crear un teclado HID virtual con `/dev/uhid` (el shell está en el grupo uhid) y mandar reportes de teclado: llegan a `onKeyEvent` como dispositivo externo (dev=72/73, src=0x101) igual que uno físico. La tablet tiene conectado un teclado real («HL Keyboard», EXTERNAL), no se pulsó físicamente.

Build de prueba (worktree temporal, retirado) con log en `onKeyEvent` (`flagRequestFilterKeyEvents` ya está en accessibility_service.xml; dumpsys accessibility: `requestFilterKeyEvents=true`, `KeyboardInterceptor` activo en display 0). Recibidos (ACTION_DOWN de todas las teclas con su metaState correcto), 25 de 25 combos:
Alt+Tab, Meta (sola), Alt+F4, Ctrl+Alt+T, Ctrl+Alt+Supr, Meta+E, Meta+D, Meta+Tab, Meta+Izq, Alt+Izq, Alt+L, Alt+Shift+Izq, Ctrl+Esc, Alt+Esc, Ctrl+T, Alt+Espacio, Meta+Espacio, Ctrl+Alt+Izq, Ctrl+Alt+A, Alt+D, F5, Meta+L, Alt+F3, Alt+F9. (Alt+F2 llegó sin Alt por un fallo de mi script de reportes, no de la plataforma.) La Meta sola llega como META_LEFT down/up. Todo el log en keys-raw-evidence.txt. También llegan con la pantalla bloqueada.

NO probado: si el servicio devolviendo `true` (consumir) evita que Samsung ejecute su propio atajo (Alt+Tab -> Recientes, Meta -> menú, Alt+F4 -> cerrar ventana), o si se ejecutan los dos. Al intentarlo me di cuenta de que el atajo existente Alt+L de DockService bloqueó la tablet con credencial (no se la sé) y no se pudieron hacer más pruebas visuales. Hay que repetirlo con la tablet desbloqueada: cambiar el build de prueba para devolver true en Alt+Tab, Meta, Alt+F4 y ver si Samsung actúa. El diseño del filtro (se ejecuta antes del despacho a ventanas) sugiere que lo consumido en `interceptKeyBeforeDispatching` sí se puede bloquear, pero lo que Samsung resuelva en `interceptKeyBeforeQueueing` no; sin medir.

## Recomendación

E-5 (organizar ventanas): entra. Usar `ActivityManagerWrapper.resizeTask` (Shizuku): snap izquierda/derecha/arriba/abajo/cuartos y maximizar visual con top=53 (usar el inset real, no 0). Corregir `AppUtils.resizeTask` para que use el wrapper en vez de `runAsRoot`. Solo actúa sobre tareas freeform; para tareas fullscreen, relanzar con windowingMode 5 (ya existe). Medir antes la altura de la taskbar de Samsung para bottom.
E-6 (teclado): entra con alcance acotado: los atajos propios (Alt+L, Alt+flechas, etc.) recibidos están confirmados. Antes de prometer reemplazar Alt+Tab/Meta/Alt+F4 de Samsung, agregar una historia corta de medición con tablet desbloqueada. Cuidado: Alt+L (bloquear) está activo por defecto y bloqueó la tablet en la prueba; considerar apagarlo por defecto.

## Estado final
Worktree /tmp/spike-keys eliminado. APK de dev (1ee1c3d, sin parches) reinstalado con scripts/deploy.sh; servicio de accesibilidad activo (`enabled_accessibility_services` = ClassicDock DockService, `Bound services` presente). Shizuku sigue corriendo. Tablet bloqueada con la pantalla de credencial: requiere que Isidro la desbloquee.
