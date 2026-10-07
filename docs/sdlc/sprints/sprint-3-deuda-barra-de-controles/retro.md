# Retro — sprint-3 (E-3 Deuda de la barra de controles)

## Resultado
S-3.1 en `dev` (merge `--no-ff`). Las 4 deudas de S-2.1 saldadas: filtro por display, Recents excluido (`RecentsActivity`), consulta de tareas fuera del hilo principal, aviso cuando cerrar no se puede. Tests unitarios 8 → 22, exit 0. Evidencia en `docs/progress/s-3.1-*`.

## Qué se cambió fuera de la story
- Decisión de Isidro: con Shizuku caído la barra sigue visible (Minimizar funciona, Cerrar avisa). Sin Shizuku el paquete en primer plano sale de accesibilidad.
- El aviso va dentro de la barra además del Toast: Samsung suprime los Toast de ClassicDock (notificaciones bloqueadas, la app no declara `POST_NOTIFICATIONS`).
- `/code-review high`: 9 hallazgos, todos atendidos (resultado de `removeTask` ignorado, accesibilidad en hilo de fondo, `getRunningTasks(5)` por display, código muerto, estado del aviso, `.800.png` fuera de git).

## Limitaciones declaradas
- Sin verificar en dispositivo: rama `FAILED` de cerrar, segundo display real, costo de la consulta de accesibilidad por `update()`.
- Sin Shizuku: una ventana freeform muestra la barra y Recents con launcher distinto al de Samsung no se excluye.
- 13 `.800.png` de S-1.x/S-2.x siguen trackeados en `dev`.

## Stories nacidas de la Review
Ninguna.

## Mejoras de proceso
- Una ronda de QA y una de code-review bastaron; el code-review destapó fallos reales que el QA no vio (resultado ignorado, hilo de accesibilidad).
- Volcar `claude -p "/code-review ..."` a archivo antes de leerlo.
