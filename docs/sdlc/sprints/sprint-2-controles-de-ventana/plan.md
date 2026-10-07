# Plan — sprint-2 (E-2 Controles de ventana)

## Decisiones de Isidro (2026-10-07)
- Barra propia de ClassicDock con minimizar, maximizar y cerrar (DeX descartado: no se activa por adb).
- Se instala Shizuku en la tablet: cerrar = `removeTask`, maximizar = cambio de modo de ventana con el id de tarea real.
- En ventanas flotantes la barra se oculta (Samsung ya trae sus botones).

## Stories
| Story | Cubre | Complejidad | Depende de | Agente Dev | Aislamiento |
|---|---|---|---|---|---|
| S-2.0 Shizuku en la tablet y verificación del id de tarea | FR-09 | S | S-1.2 | coordinador (no delegado: instala apps y toca permisos en el dispositivo) | actual |
| S-2.1 Barra de controles de ventana | FR-09 | M | S-2.0 | sdlc-dev | rama `feature/window-controls` |

Sin paralelismo: S-2.1 depende de que Shizuku dé ids de tarea reales (hoy `taskId = -1`).

## S-2.0 — Shizuku operativo (rama `feature/shizuku-setup`)
- Criterios:
  - Given Shizuku instalado y arrancado por adb When abro ClassicDock Then `AppTask.id` es > 0 en las apps abiertas y los indicadores de S-1.4 aparecen.
  - Given el procedimiento When se documenta Then `docs/maintenance.md` trae cómo reiniciar Shizuku tras reiniciar la tablet.
- Foco de revisión: ids de tarea reales (`AppUtils.getRunningTasks`); `scripts/deploy.sh` no debe fallar si Shizuku no corre.

## S-2.1 — Foco de revisión
- Tarea en primer plano mal identificada (launcher, systemui, ClassicDock mismo): la barra no debe actuar sobre ellos. Prueba: unit sobre el filtro de tarea objetivo.
- Barra visible sobre flotantes: debe ocultarse por `windowingMode == FREEFORM`.
- Cerrar sin id válido (-1): no hacer nada, sin crash.
- La barra no debe tapar el contenido de la app: reservar altura o superponer fina; medir en captura.
- `TYPE_ACCESSIBILITY_OVERLAY` ya usado en `Utils.kt:144`; reutilizar `Utils.makeWindowParams`.

## Riesgos
- Shizuku muere al reiniciar la tablet: la barra degrada a solo minimizar (Home) y avisa.
- `DockService.kt` ya tiene ~2,000 líneas: la barra va en vista y clase propias, no más código dentro del servicio.

## Presupuesto (`/ai-sdlc:budget`)
| Story | Despachos | Tokens estimados |
|---|---|---|
| S-2.0 | 0 (coordinador) | ~60k |
| S-2.1 | Dev 1, revisor 0, QA 1 | ~350k |
| Coordinación (0.7×) | | ~250k |
