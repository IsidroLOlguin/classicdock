# Sprint 4 — E-4 Retiro de la barra propia (redefinido 2026-10-07)

Original: mover la barra fuera del notch. Cambió: la barra nativa de Samsung no se puede fijar y Isidro prefiere usarla; S-4.1 ahora retira `WindowControlsBar`. Dev anterior (8ee2889) descartado.

## Stories
| Story | Cubre | Complejidad | Depende de | Dev | Aislamiento |
|---|---|---|---|---|---|
| S-4.1 | FR-09, FR-10 | S | S-3.1 | sdlc-dev | worktree `feature/remove-controls-bar` |

## Paralelización
Ninguna: una story, toca `WindowControlsBar.kt` (gravity y offset en `init`, línea ~102) y su layout.

## Presupuesto
| Story | Despachos | Tokens estimados |
|---|---|---|
| S-4.1 | 1 Dev + 1 QA (x1.7 ciclos) | ~255k workers + ~180k coordinación |

## Riesgos
- Altura real de la barra de estado/bandeja en la tablet: medir con `adb` y captura en horizontal y vertical antes de fijar el offset (no hardcodear px).
- El notch cambia de borde según rotación (en vertical está en el borde derecho); el offset debe salir de los insets del sistema, no de constantes.
- Cualquier cambio visual se verifica en la tablet SM-X910 (`R52X603XGQT`) con captura en `docs/progress/`.

## Después de E-4
Spike de factibilidad (resizeTask por Shizuku, teclas por accesibilidad en Android 16) y recién entonces se definen E-5 (organizar ventanas) y E-6 (teclado). Acordado en brainstorming 2026-10-07.
