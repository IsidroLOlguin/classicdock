# Retro — sprint-1 (E-1 Dock y barra de apps)

## Resultado
Las seis stories (S-1.1 a S-1.6) en `dev`, cada una en su rama `feature/*` mergeada con `--no-ff`. Evidencia en `docs/progress/`. `/code-review low` corrido por story: 6 hallazgos en total, 2 corregidos (requestLayout en S-1.2 y S-1.4), 4 descartados con motivo.

## Qué se cambió fuera de la story
- `deploy.sh` otorga además `GET_USAGE_STATS` (S-1.1): sin él el dock no lista apps abiertas sin Shizuku.
- `round_dock` pasa a `true` por defecto (S-1.2).
- El texto en inglés de `transparent` pasó a «Translucent» (S-1.3); los demás idiomas caen al inglés para «Fully transparent».

## Limitaciones declaradas
- Sin Shizuku ni app de sistema el id de tarea es -1 y los indicadores de app abierta (S-1.4) se ocultan, como pide el criterio. La captura de los dos estilos se tomó forzando alpha 1 de forma temporal; ese código no quedó en el repo.
- En `fully_transparent` los menús y el panel usan alpha 225 (translúcidos, no transparentes) a propósito, para que sigan legibles.
- El menú contextual del dock (S-1.2) no se capturó: usa `dockHeight` igual que el panel rápido, que sí quedó pegado al dock.

## Stories nacidas de la Review
Ninguna.

## Mejoras de proceso
- Tras `force-stop` Android quita el servicio de la lista de accesibilidad: hay que reponerlo con `settings put secure enabled_accessibility_services` (ver `docs/maintenance.md`).
