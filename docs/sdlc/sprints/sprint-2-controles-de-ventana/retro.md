# Retro — sprint-2 (E-2 Controles de ventana)

## Resultado
S-2.0 (Shizuku operativo) y S-2.1 (barra de controles) en `dev`, mergeadas con `--no-ff`. FR-09 cubierto con minimizar y cerrar. Evidencia en `docs/progress/s-2.0-*` y `s-2.1-*` (versiones `.800` y `.crop`).

## Qué se cambió fuera de la story
- Maximizar se quitó: Android 16 no expone `setTaskWindowingMode` y `resizeTask` no saca la tarea de freeform (medido). FR-09 en `prd.md` y el backlog quedaron con dos botones.
- `AppUtils.getCurrentLauncher` pasó a público.
- `/code-review high`: 10 hallazgos, 5 corregidos (fuga de vistas al destruir, reflexión sin guarda, cerrar con tarea obsoleta, barra GONE tras recrear vistas, capturas completas fuera de git); el resto es deuda.

## Limitaciones declaradas
- Sin Shizuku la barra no aparece (no se conoce la tarea en primer plano).
- Deuda: filtro por display, excluir Recents, `update()` fuera del hilo principal, aviso si Shizuku cae.
- RootTask fantasma 46 en la tablet por un experimento; se limpia al reiniciarla.

## Stories nacidas de la Review
Maximizar real (reflexión sobre `WindowContainerTransaction`, riesgo medio): no creada, solo si Isidro la pide.

## Mejoras de proceso
- `claude -p "/code-review high dev...rama"` desde el repo corre síncrono; volcarlo a archivo, `tail` corta la lista.
