# S-4.1 — Barra de controles bajo la bandeja

Cubre: FR-10 · Épica E-4 · Complejidad S · Rama `feature/controls-bar-corner` · Depende de S-3.1

## Criterios de aceptación
Los cuatro de `backlog.md` (S-4.1).

## Foco de revisión
- Posición derivada de insets del sistema (barra de estado + cutout), no de constantes de px; test o medición en horizontal y vertical.
- Display secundario: misma esquina, sin romper `Utils.makeWindowParams`.
- Sin regresión de S-3.1: filtro de display, Recents, aviso Shizuku, freeform oculto.
