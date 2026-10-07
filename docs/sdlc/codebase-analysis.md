# Análisis de codebase — Smart Dock (base de ClassicDock)

El análisis vive en `docs/reference/` y no se duplica aquí:
- [smartdock-dock-architecture.md](../reference/smartdock-dock-architecture.md): archivos, flujo de datos, qué se reestiliza sin tocar lógica.
- [youkidex-dock-analysis.md](../reference/youkidex-dock-analysis.md): referencia visual.
- [mapping.md](../reference/mapping.md): elemento por elemento, con clasificación, riesgos y alcance mínimo.

Stack: Android, Kotlin/Java, Views XML, `DockService.kt` (AccessibilityService, overlay). Build base verificado: `./gradlew assembleDebug` OK (JDK 21, compileSdk 36).
