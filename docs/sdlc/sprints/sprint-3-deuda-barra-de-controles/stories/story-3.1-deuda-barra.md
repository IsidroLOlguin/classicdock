# S-3.1 — Saldar deuda de la barra de controles

Cubre: FR-09 · Épica E-3 · Complejidad M · Rama `feature/window-controls-debt` · Depende de S-2.1

## Criterios de aceptación
Los cuatro de `backlog.md` (S-3.1).

## Foco de revisión
- `targetTask` con tarea de otro display: test en `WindowControlsPolicyTest` que falla sin el filtro.
- Recents: paquete real medido en la tablet y cubierto por test de `targetTask`.
- `update()` no consulta `getRunningTasks` en el hilo principal (resultado se aplica con `view.post`/handler).
- Shizuku caído: `runOnTask` + aviso; test de la rama sin `activityManager`.
- Sin regresión: id -1/0 sigue sin llamar, freeform sigue oculto.
