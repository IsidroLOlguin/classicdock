# S-2.1 — Barra de controles de ventana

- Épica: E-2 · Cubre: FR-09 · Complejidad: M · Rama: `feature/window-controls` (desde `dev`)
- Dev: sdlc-dev · QA: sdlc-qa (captura en la tablet SM-X910, adb `R52X603XGQT`)

## Historia
Como Isidro quiero minimizar, maximizar y cerrar visibles sobre la app en primer plano, también a pantalla completa.

## Criterios de aceptación
1. Given una app a pantalla completa When miro la parte superior Then veo una barra fina de ClassicDock con minimizar, maximizar y cerrar.
2. Given una ventana flotante (`windowingMode == FREEFORM`) When la miro Then la barra no se muestra.
3. Given los tres botones When los toco Then minimizan (Home), maximizan (la tarea pasa a fullscreen con su id real) o cierran (`removeTask`) la tarea en primer plano.
4. Given que la tarea en primer plano es launcher, systemui o ClassicDock Then la barra no se muestra y no actúa sobre ellos.
5. Given Shizuku sin correr (id = -1 o wrapper nulo) Then cerrar y maximizar no hacen nada y no hay crash; minimizar (Home) sigue funcionando.
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
