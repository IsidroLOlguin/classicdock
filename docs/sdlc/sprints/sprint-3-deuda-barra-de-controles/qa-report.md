# QA S-3.1 — ciclo 1
Suite: `./gradlew testDebugUnitTest --offline` exit 0 (19 pruebas). Mutación en copia /tmp/qa-s31 (sin filtro de display y sin Recents): 5 fallan.
| AC | Estado | Evidencia |
|---|---|---|
| 1 cerrar solo tarea del display de la barra | PASS | WindowControlsBar.kt:36 filtro por displayId; tests tareaDeOtroDisplay*/objetivoEsLaPrimera*; mutación falla |
| 2 Recents sin barra | PASS | WindowControlsBar.kt:37 filtro por RecentsActivity; test recentsNoEsObjetivo*; mutación falla |
| 3 update() no bloquea main | PASS (por lectura) | :150-160 getRunningTasks en executor, view.post; sin test ni trace |
| 4 Shizuku caído -> aviso | CONCERNS | :119-127 y close(); ver hallazgo 1 |
Hallazgos:
1. CONCERNS WindowControlsBar.kt:150-154: con Shizuku caído, update() oculta la barra (am==null). El aviso solo sale si Shizuku muere entre el último update y el tap (caso que midió el Dev); en general la barra desaparece, y con ella Minimizar (antes visible). Decidir si es intencional.
2. CONCERNS `warning`/`destroyed` no son @Volatile y se leen desde el executor (:~150, :133); riesgo bajo.
3. CONCERNS closeForeground tras destroy(): guard `destroyed` cubre casi todo; carrera mínima con executor.execute tras shutdownNow (RejectedExecutionException) si el clic llega justo entre ambos.
4. Sin test de hilo/visibilidad del aviso: solo la política está cubierta; id<=0 y freeform ocultos sí tienen test.
No verificado: segundo display real, tablet (no la toqué).
