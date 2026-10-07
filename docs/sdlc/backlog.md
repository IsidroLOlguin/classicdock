# Backlog — ClassicDock

## Épicas
| ID | Épica | Objetivo | Cubre | Orden sugerido |
|---|---|---|---|---|
| E-0 | Identidad del fork | Instalable junto al original, con créditos y despliegue de un comando | FR-01, FR-08 | 1 |
| E-1 | Dock y barra de apps | Aspecto DeX clásico | FR-02..FR-07 | 2 |
| E-2 | Controles de ventana | Minimizar y cerrar siempre visibles | FR-09 | 3 |
| E-3 | Deuda de la barra de controles | Barra correcta por display, sin Recents, sin trabajo en el hilo principal y con aviso si Shizuku cae | FR-09 | 4 |

## Stories
### S-0.1 — Rebrand (rama `feature/rebrand`)
- Como Isidro quiero instalar ClassicDock junto a Smart Dock para probarlo sin perder el original
- Cubre: FR-01
- Complejidad: S
- Depende de: n/a
- Criterios de aceptación:
  - Given el build When se instala Then el paquete es `dev.isidro.classicdock` y el nombre visible es «ClassicDock»
  - Given el repo When se abre Then README y NOTICE.md traen créditos a Smart Dock y YoukiDEX y la licencia GPL-3.0

### S-0.2 — Script de despliegue (rama `feature/deploy-script`)
- Como Isidro quiero un solo comando para compilar, instalar y dar permisos
- Cubre: FR-08
- Complejidad: S
- Depende de: S-0.1
- Criterios de aceptación:
  - Given la tablet conectada When corro `scripts/deploy.sh` Then compila, instala y otorga `WRITE_SECURE_SETTINGS` sin pasos manuales

### S-1.1 — Íconos de 44dp y celda unificada (rama `feature/icon-size`)
- Como Isidro quiero íconos más grandes y parejos sin que la fila se recorte
- Cubre: FR-02
- Complejidad: S
- Depende de: S-0.2
- Criterios de aceptación:
  - Given el dock con 10 apps abiertas When se muestra Then los íconos miden 44dp y la fila no se recorta ni deja huecos
  - Given el código When se busca el 52dp fijo Then ya no existe; el ancho sale de un solo valor

### S-1.2 — Dock flotante (rama `feature/floating-dock`)
- Como Isidro quiero el dock flotante con margen y esquinas redondeadas
- Cubre: FR-03
- Complejidad: M
- Depende de: S-1.1
- Criterios de aceptación:
  - Given `round_dock` activo When se muestra Then el dock flota con 8dp de margen y esquinas de 20dp
  - Given el dock flotante When abro panel rápido, notificaciones, menú contextual y popups Then aparecen pegados sobre el dock, sin solaparse ni dejar hueco
  - Given el dock oculto When lo activo Then la zona de activación sigue funcionando

### S-1.3 — Transparencia total (rama `feature/transparent-mode`)
- Como Isidro quiero un tema totalmente transparente además del translúcido
- Cubre: FR-04
- Complejidad: S
- Depende de: S-1.2
- Criterios de aceptación:
  - Given el selector de tema When elijo `fully_transparent` Then el fondo del dock queda con alpha 0 y los íconos siguen legibles
  - Given tema transparente When veo píldoras y panel Then no hay bloques opacos desentonando

### S-1.4 — Indicadores de app abierta (rama `feature/running-indicators`)
- Como Isidro quiero distinguir la app activa de las abiertas en segundo plano
- Cubre: FR-05
- Complejidad: S
- Depende de: S-1.1
- Criterios de aceptación:
  - Given una app activa When miro el dock Then veo barra 16x3dp `#CCFFFFFF`; las demás abiertas muestran punto 4x4dp
  - Given sin Shizuku (UsageStats) When el id de tarea es -1 Then no hay fallo y el indicador se oculta

### S-1.5 — Bandeja en píldora con reloj aparte (rama `feature/tray-pill`)
- Como Isidro quiero la bandeja de estado agrupada como en el DeX clásico
- Cubre: FR-06
- Complejidad: M
- Depende de: S-1.2
- Criterios de aceptación:
  - Given el dock When veo la derecha Then wifi, volumen, bluetooth y batería van en una píldora de 14dp y el reloj queda fuera
  - Given un fondo claro u oscuro When veo el texto Then es legible

### S-1.6 — Animación mostrar/ocultar (rama `feature/dock-animation`)
- Como Isidro quiero que el dock aparezca con fade y escala suave
- Cubre: FR-07
- Complejidad: S
- Depende de: S-1.2
- Criterios de aceptación:
  - Given el dock oculto When lo activo Then aparece con fade y escala desde 0.90 en 180 ms, y al ocultarse hace lo inverso

### S-2.0 — Shizuku operativo (rama `feature/shizuku-setup`)
- Como Isidro quiero ids de tarea reales para que la barra pueda cerrar
- Cubre: FR-09
- Complejidad: S
- Depende de: S-1.2
- Criterios de aceptación:
  - Given Shizuku instalado y arrancado por adb When abro ClassicDock Then `AppTask.id` es > 0 en las apps abiertas
  - Given el procedimiento When se documenta Then `docs/maintenance.md` explica cómo reiniciar Shizuku

### S-2.1 — Barra de controles de ventana (rama `feature/window-controls`)
- Como Isidro quiero minimizar y cerrar visibles en cualquier ventana, también a pantalla completa
- Cubre: FR-09
- Complejidad: M
- Depende de: S-2.0
- Criterios de aceptación:
  - Given una app a pantalla completa When miro la parte superior Then veo una barra fina de ClassicDock con minimizar y cerrar
  - Given una ventana flotante When la miro Then no hay botones duplicados con los de Samsung
  - Given los dos botones When los toco Then minimizan o cierran la tarea en primer plano
- Preguntas abiertas: cómo cerrar la tarea sin Shizuku; qué hacer en flotantes (ocultar la barra o integrarse); ver `DockService.kt` ~638 (acciones de tarea) y `AppUtils.makeLaunchBounds`.

### S-3.1 — Saldar deuda de la barra de controles (rama `feature/window-controls-debt`)
- Como Isidro quiero que la barra de controles actúe sobre la tarea correcta y avise cuando no puede cerrar
- Cubre: FR-09
- Complejidad: M
- Depende de: S-2.1
- Criterios de aceptación:
  - Given la barra en el display principal y una app abierta en el secundario When toco cerrar Then solo se cierra la tarea del display de la barra
  - Given la pantalla de Recents abierta When miro la parte superior Then la barra no aparece
  - Given cambios rápidos de app When `update()` corre Then la consulta de tareas no bloquea el hilo principal
  - Given Shizuku caído When toco cerrar Then veo un aviso y la barra no hace nada en silencio

## Historial de cambios
| Fecha | Cambio | Motivo | Sprint origen |
|---|---|---|---|
| 2026-10-06 | Backlog inicial | Alcance v1 aprobado | n/a |
| 2026-10-07 | E-2 y S-2.1 | Isidro pidió botones de ventana siempre visibles; elegida barra propia (DeX descartado: no activable por adb) | n/a |
