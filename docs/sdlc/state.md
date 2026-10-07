# Estado del proyecto — ClassicDock

## Setup
- Modo de persistencia: repo-externo (fork propio `IsidroLOlguin/classicdock`; push solo a `origin`, nunca a `upstream`)
- Contexto: brownfield (fork de Smart Dock; análisis en `docs/reference/`)
- Fecha de inicio: 2026-10-06
- Rama de integración: dev
- Motor de ejecución: task
- Run ID:

## Etapa actual
- Loop: interno
- Artefacto/Sprint en curso: sprint-4 (E-4) retirar barra propia
- Story en curso y fase: S-4.1 implementada y con QA ok en rama feature/remove-controls-bar; falta verificar en tablet y gate de merge

## Gates cruzados
| Fecha | Artefacto/Story | Notas |
|---|---|---|
| 2026-10-06 | codebase-analysis.md | Aprobado por la ruta del punto de control 1; análisis en docs/reference/ |
| 2026-10-06 | Alcance v1 | «Mínimo DeX clásico» elegido por Isidro, 6 elementos |
| 2026-10-06 | Plan de historias | Aprobado (punto de control 2), 8 historias |
| 2026-10-07 | S-1.1 a S-1.6 | En dev, verificadas en tablet (docs/progress), retro en sprints/sprint-1-dock-y-barra-de-apps |
| 2026-10-07 | S-2.0 | Shizuku v13.6.0 en tablet, ids de tarea reales medidos; docs/progress/s-2.0-shizuku.png |
| 2026-10-07 | Plan sprint-2 (E-2) | Aprobado: Shizuku (S-2.0) y barra propia (S-2.1), oculta en flotantes |
| 2026-10-07 | S-2.1 | Barra min+cerrar (maximizar imposible en Android 16), QA y code-review ok, en dev |
| 2026-10-07 | Plan sprint-3 (E-3) | Aprobado: S-3.1 única story, salda deuda de S-2.1 |
| 2026-10-07 | S-3.1 | Deuda de S-2.1 saldada, QA y code-review ok, medido en tablet, en dev; retro sprint-3 |
| 2026-10-07 | Plan sprint-4 (E-4) | Aprobado: S-4.1 única story, barra a la esquina superior derecha |
| 2026-10-07 | E-4 redefinida | Barra nativa de Samsung no se fija; Isidro quita la barra propia, FR-09/FR-10 Won't |
| 2026-10-07 | S-0.1, S-0.2 | Rebrand y deploy.sh en dev (push hecho); tablet SM-X910 verificada |

## Épicas
| Épica | Estado | Sprint |
|---|---|---|
| E-0 Identidad del fork | DONE | sprint-0 |
| E-1 Dock y barra de apps | DONE | sprint-1 |
| E-2 Controles de ventana | DONE | sprint-2 |
| E-3 Deuda de la barra de controles | DONE | sprint-3 |
| E-4 Retiro de la barra propia | pendiente | sprint-4 |

## Bloqueos y pendientes
- Sin verificar de S-3.1: segundo display, rama FAILED, costo de accesibilidad (retro sprint-3).
- Mantenimiento y trampas en docs/maintenance.md.

## Decisiones rápidas (que no ameritan ADR)
- 2026-10-06: ceremonia reducida por instrucción de Isidro: PRD, backlog y plan en un solo paso, con un gate (punto de control 2).
- 2026-10-06: rama principal renombrada `main` a `master` en el fork; `dev` creada.
