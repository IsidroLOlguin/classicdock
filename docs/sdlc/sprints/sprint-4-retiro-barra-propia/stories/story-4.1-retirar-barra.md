# S-4.1 — Retirar la barra propia de controles

Cubre: FR-09, FR-10 · Épica E-4 · Complejidad S · Rama `feature/remove-controls-bar` · Depende de S-3.1

## Criterios de aceptación
Los tres de `backlog.md` (S-4.1).

## Foco de revisión
- Sin referencias colgantes a `WindowControlsBar`/`WindowControlsPolicy` en `DockService` y recursos (layout, strings).
- `./gradlew testDebugUnitTest assembleDebug` verde tras borrar tests de la barra.
- Dock y bandeja intactos; en la tablet no queda barra propia (captura recortada en docs/progress/).
