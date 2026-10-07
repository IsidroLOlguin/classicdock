# Sprint 3 — E-3 Deuda de la barra de controles

## Stories
| Story | Cubre | Complejidad | Depende de | Dev | Aislamiento |
|---|---|---|---|---|---|
| S-3.1 | FR-09 | M | S-2.1 | sdlc-dev | worktree `feature/window-controls-debt` |

## Paralelización
Ninguna: las cuatro deudas tocan `WindowControlsBar.kt` y su llamada en `DockService.kt` (~520, ~2096); un solo Dev.

## Riesgos
- Recents en One UI 8 puede no ser `com.android.systemui` ni el launcher: medir el paquete real en la tablet (SM-X910) antes de filtrar.
- `RunningTaskInfo.displayId` es API oculta: leerla por reflexión como `getWindowingMode`, con fallo seguro.
- Cualquier cambio de comportamiento se verifica en la tablet con captura en `docs/progress/`.
