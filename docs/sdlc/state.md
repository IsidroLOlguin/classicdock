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
- Artefacto/Sprint en curso: sprint-1 (E-1), plan aprobado 2026-10-06
- Story en curso y fase: S-1.1 por arrancar (feature/icon-size)

## Gates cruzados
| Fecha | Artefacto/Story | Notas |
|---|---|---|
| 2026-10-06 | codebase-analysis.md | Aprobado por la ruta del punto de control 1; análisis en docs/reference/ |
| 2026-10-06 | Alcance v1 | «Mínimo DeX clásico» elegido por Isidro, 6 elementos |
| 2026-10-06 | Plan de historias | Aprobado (punto de control 2), 8 historias |
| 2026-10-07 | S-0.1, S-0.2 | Rebrand y deploy.sh en dev (push hecho); tablet SM-X910 verificada |

## Épicas
| Épica | Estado | Sprint |
|---|---|---|
| E-0 Identidad del fork | DONE | sprint-0 |
| E-1 Dock y barra de apps | en curso | sprint-1 |

## Bloqueos y pendientes
- deploy.sh debe agregar `appops set dev.isidro.classicdock SYSTEM_ALERT_WINDOW allow` y `ACCESS_RESTRICTED_SETTINGS allow`; sin overlay el servicio crashea (type 2038).
- Activar accesibilidad por adb: `settings put secure enabled_accessibility_services <existentes>:dev.isidro.classicdock/cu.axel.smartdock.services.DockService`.
- Tablet SM-X910 por USB (serie R52X603XGQT); servicio ya activo. No se verificó aún la captura del dock.
- Ruta de trabajo: ~/Documents/Proyectos/classicdock/classicdock (reference/youkidex al lado).
- Isidro puede aportar capturas del DeX clásico en `docs/reference/dex-classic/` (no bloquea).

## Decisiones rápidas (que no ameritan ADR)
- 2026-10-06: ceremonia reducida por instrucción de Isidro: PRD, backlog y plan en un solo paso, con un gate (punto de control 2).
- 2026-10-06: rama principal renombrada `main` a `master` en el fork; `dev` creada.
