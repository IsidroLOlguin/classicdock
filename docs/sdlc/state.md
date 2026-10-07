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
- Artefacto/Sprint en curso: sprint-1 (E-1) cerrado, falta decidir merge a master
- Story en curso y fase: S-1.1 a S-1.6 DONE en dev

## Gates cruzados
| Fecha | Artefacto/Story | Notas |
|---|---|---|
| 2026-10-06 | codebase-analysis.md | Aprobado por la ruta del punto de control 1; análisis en docs/reference/ |
| 2026-10-06 | Alcance v1 | «Mínimo DeX clásico» elegido por Isidro, 6 elementos |
| 2026-10-06 | Plan de historias | Aprobado (punto de control 2), 8 historias |
| 2026-10-07 | S-1.1 a S-1.6 | En dev, verificadas en tablet (docs/progress), retro en sprints/sprint-1-dock-y-barra-de-apps |
| 2026-10-07 | S-0.1, S-0.2 | Rebrand y deploy.sh en dev (push hecho); tablet SM-X910 verificada |

## Épicas
| Épica | Estado | Sprint |
|---|---|---|
| E-0 Identidad del fork | DONE | sprint-0 |
| E-1 Dock y barra de apps | DONE | sprint-1 |

## Bloqueos y pendientes
- Merge dev a master y push a origin: esperan confirmación de Isidro.
- Mantenimiento y trampas en docs/maintenance.md.

## Decisiones rápidas (que no ameritan ADR)
- 2026-10-06: ceremonia reducida por instrucción de Isidro: PRD, backlog y plan en un solo paso, con un gate (punto de control 2).
- 2026-10-06: rama principal renombrada `main` a `master` en el fork; `dev` creada.
