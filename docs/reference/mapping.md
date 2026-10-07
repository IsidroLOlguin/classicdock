# Mapeo YoukiDEX a Smart Dock

Fuentes: `youkidex-dock-analysis.md`, `smartdock-dock-architecture.md`, y verificación directa en `app/src/main/`. Abreviaturas: `DS` = `java/cu/axel/smartdock/services/DockService.kt`, `CU` = `utils/ColorUtils.kt`, `DAA` = `adapters/DockAppAdapter.kt`, `res` = `app/src/main/res`.

Clasificación: **solo estilo** (XML, drawables, prefs existentes), **estilo + lógica menor** (prefs nuevas o unas pocas líneas), **función nueva** (componente o comportamiento que no existe).

## Tabla

| Elemento de YoukiDEX | Archivo(s) de Smart Dock | Clase | Dificultad | Nota |
|---|---|---|---|---|
| Dock pegado al borde | `res/drawable/rect.xml`; `DS.updateDockShape()` (pref `round_dock`=false) | Solo estilo | Baja | Ya es el comportamiento por defecto. |
| Dock flotante (ancho completo, margen 8dp, esquinas 20dp) | `DS.createDock()` (`makeWindowParams`, gravity, `y`), `res/drawable/round_rect.xml`, `DS.updateDockShape()` | Estilo + lógica menor | Media | Hoy `round_dock` solo cambia el fondo; faltan margen `y=8dp`, ancho `-2*8dp` y `x`. |
| Dock flotante que se encoge al contenido (estilo KDE) | `DS.createDock()`, `res/layout/dock.xml`, `DS.placeRunningApps()` | Función nueva | Alta | YoukiDEX tampoco lo hace; rompe el `RelativeLayout` con nav a la izquierda y tray a la derecha. |
| Presets de tamaño (`small` 40/34/42, `pc` 30/24/32) | `DS` lectura de `dock_height`, `DAA` (ícono 40dp fijo), `res/layout/app_task_entry.xml`, `DS` línea 1482 (`dpToPx(52)`) | Estilo + lógica menor | Media | Pref nueva `dock_size_preset`; el ícono y la celda hoy son fijos, hay que volverlos dinámicos. |
| Presets de layout (Phone/Tablet/Desktop) | `dialogs/DockLayoutDialog.kt`, `res/values/arrays.xml` | Estilo + lógica menor | Baja | Ya existe con 0/1/2; solo ajustar valores y, si se quiere, añadir prefs visuales al lote. |
| Agrupación de ventanas con badge numérico | `models/DockApp.kt`, `DAA`, `res/layout/app_task_entry.xml` (`task_count_badge`), `AppUtils.containsTask` | Solo estilo | Baja | Ya agrupa por paquete y muestra contador; retocar tamaño, fuente y posición del badge. |
| Agrupación con vista previa / menú de ventanas nuevo | `DS.onDockAppClicked`, `res/layout/task_list.xml` | Función nueva | Alta | YoukiDEX tampoco tiene miniaturas; no es necesario para el parecido. |
| Indicador de app abierta (barra 16x3 activa, punto 4x4 en segundo plano) | `res/drawable/running_indicator.xml`, `res/layout/app_task_entry.xml`, `DAA.onBindViewHolder` | Estilo + lógica menor | Baja | Hoy 16dp/8dp con radio 5dp y gris `#CCBEBEBE`; pasar el de fondo a punto 4x4 y el color a `#CCFFFFFF`. |
| Reloj / fecha | `res/layout/dock.xml` (`date_btn`), pref `enable_qs_date` | Solo estilo | Baja | Ya existe como `TextClock`; ajustar tamaño (13sp) y formato. |
| Batería | `res/layout/dock.xml` (`battery_btn`), `receivers/BatteryStatsReceiver.kt`, `res/drawable/battery_*.xml` | Solo estilo | Baja | Ya existe; YoukiDEX la muestra como texto 11sp (`show_battery_level`). |
| Wi-Fi / Bluetooth / volumen en la bandeja | `res/layout/dock.xml` (`status_area`), `res/drawable/ic_*.xml`, `res/drawable/search_background.xml` | Solo estilo | Baja | Ya existen; igualar tamaños (18/20dp) y píldora de 14dp. |
| Píldoras internas (`btn_bubble_rect`, 14dp) y `bubble_mode`/`bubble_alpha` | `res/drawable/search_background.xml`, `CU.applySecondaryColor`, `DS.applyTheme()` | Estilo + lógica menor | Media | Smart Dock tiñe con color secundario; no tiene `bubble_color` ni `bubble_alpha`. |
| Monitor de CPU / RAM | `res/layout/dock.xml` (TextView nuevo), `DS` (Handler 2 s, `/proc/stat`, `ActivityManager.MemoryInfo`), `res/xml/preferences_dock_elements*.xml`, `DockElementsPreferences.kt` | Función nueva | Media | No existe nada equivalente; ~60 líneas y una pref `show_resource_monitor`. |
| Bandeja de notificaciones | `DS.toggleQuickSettingsPanel`, `res/layout/quick_settings_panel.xml`, `services/NotificationService.kt`, `components/NotificationLayout.kt` | Solo estilo | Baja | Ya existe con contador en `notifications_btn`; reestilizar fondo, cabecera y radio. |
| Panel integrado con reproductor, brillo y mosaicos | `res/layout/quick_settings_panel.xml`, `DS` | Función nueva | Alta | YoukiDEX lo tiene en la captura 2; Smart Dock solo trae pestañas y slider de volumen. |
| Opacidad del dock | `CU.getMainColors`, prefs `theme_main_alpha`, `override_dock_background_alpha`, `dock_background_alpha` | Solo estilo | Baja | Ya existe, incluido alpha 225 en `transparent`. |
| Modo transparente | `CU.getMainColors` (`transparent`, alpha 225) | Estilo + lógica menor | Baja | Falta `fully_transparent` (alpha 0); un `when` más y una entrada en `arrays.xml`. |
| Esquinas (dock 20dp, píldoras 14dp, indicador 10dp, asa 10dp) | `res/drawable/round_rect.xml`, `running_indicator.xml`, `dock_handle_bg_*.xml`, `search_background.xml` | Solo estilo | Baja | Los radios ya coinciden casi todos (20dp, 10dp); solo el indicador es 5dp frente a 10dp. |
| Material You (oscuro) | `CU.getMainColors` caso `material_u` | Solo estilo | Baja | Ya existe, con factores 0.9/1.2 frente a 0.85/1.1 de YoukiDEX. |
| Material You claro | `CU.getMainColors`, `res/values/arrays.xml` | Estilo + lógica menor | Baja | Falta `material_u_light` (surface claro x0.96); ojo con el texto blanco fijo en `dock.xml`. |
| Animación mostrar/ocultar | `res/anim/slide_up.xml`, `slide_down.xml`, `DS.showDock/hideDock` | Estilo + lógica menor | Media | Cambiar a fade + escala 0.90 de 180 ms con `ViewPropertyAnimator`; hoy usa XML de slide. |
| Animación de pulsación (escala 0.82) | `res/anim/` (nuevo), `DAA` | Función nueva | Baja | Detalle fino; no cambia la forma del dock. |
| Altura del dock | pref `dock_height` (default 56), `DS.updateDockHeight` | Solo estilo | Baja | Ya es configurable sin código. |
| Tamaño de ícono (44dp en YoukiDEX, 40dp en Smart Dock) | `res/layout/app_task_entry.xml`, `icon_padding`, `icon_shape` | Solo estilo | Baja | Si cambia la celda hay que tocar el 52dp de `DS` (ver Riesgos). |

## Riesgos

- **Offsets que dependen de `dockHeight`.** Panel rápido, `NotificationLayout`, menús contextuales y popups se anclan con `y = 2dp + dockHeight` (`DS` líneas 464, 975, 1088, 1658; también 948). Un dock flotante con margen de 8dp exige sumar ese margen en todos, o los paneles quedan solapados o con hueco. Además `hideDock`/`showDock` redimensionan la ventana a `dock_activation_area`, y con `y > 0` la zona de activación del borde queda separada del borde real.
- **Celda de 52dp fija.** `DS` línea 1482 calcula el ancho del `RecyclerView` como `dpToPx(52) * apps.size`. Cambiar ícono (40 a 44dp) o `icon_padding` sin tocar ese valor recorta o ensancha la fila; los presets de tamaño deben derivarlo de un solo valor.
- **Color teñido en código.** `round_rect.xml` y `rect.xml` son blancos y `ColorUtils.applyMainColor` los tiñe con `SRC_ATOP`; editar el color en el drawable no cambia nada visible. Lo mismo para `search_background` y el secundario. Para Material You claro hay que revisar los textos y las `ic_*` en blanco fijo (`dock.xml`).
- **Texto blanco fijo.** `date_btn` y `battery_btn` tienen `textColor` blanco; con un fondo claro (`material_u_light`) pierden contraste.
- **Fondo y esquinas con alpha.** `override_dock_background_alpha` se aplica al fondo del dock; las píldoras y el panel usan alphas distintos (`getMainColors[3]`), así que con transparente puede quedar un dock casi invisible sobre píldoras opacas.
- **Layout de ancho completo.** `RelativeLayout` con `nav_panel` y `system_tray` pegados a los lados asume que la ventana ocupa la pantalla; un dock que se encoge al contenido obliga a reescribir `placeRunningApps()`.
- **Throttle y `tasks[0].id == -1`.** Con `UsageStatsManager` (sin Shizuku) el indicador queda en alpha 0; cualquier rediseño del indicador debe seguir tolerando ese caso.
- **Licencia.** YoukiDEX es GPL-3.0; usar solo valores y comportamientos como referencia, sin copiar fuentes (Smart Dock ya es GPL, pero la procedencia debe quedar clara).
- **Sin DeX clásico de referencia.** `docs/reference/dex-classic/` no existe; el mapeo parte de YoukiDEX, no de las capturas definitivas de Isidro.

## Alcance mínimo recomendado

1. **Dock flotante con margen y esquinas 20dp** (estilo + lógica menor). Es el cambio visual más grande; el dock clásico de DeX es una barra flotante y no pegada al borde. Hay que corregir de una vez los offsets de `dockHeight`.
2. **Opacidad y modo transparente** (`fully_transparent` + ajuste del alpha 225). Casi todo el aspecto de DeX viene de la barra translúcida oscura; cuesta un `when` y una entrada de lista.
3. **Indicadores de app abierta: barra larga en la activa, punto en las demás**, en blanco `#CCFFFFFF`. Es el detalle más reconocible de la zona central y se resuelve en `DAA` y un drawable.
4. **Bandeja a la derecha reestilizada**: píldora de 14dp con wifi/volumen/bluetooth/batería y reloj fuera de la píldora. Ya existen todos los elementos; solo estilo, y da la lectura clásica de la barra del sistema.
5. **Tamaños de ícono y de celda unificados** (altura 48-56dp, ícono 44dp, celda derivada de un solo valor en lugar del 52dp fijo). Evita regresiones del punto 1 y deja un preset de tamaño reutilizable.
6. **Animación de mostrar/ocultar fade + escala 0.90 a 180 ms.** Barata y da la sensación de ventana flotante del escritorio.

Se deja fuera a propósito: monitor CPU/RAM, panel con reproductor y mosaicos, Material You claro, miniaturas de ventanas. Aportan función, no aspecto, y cada uno es una función nueva.
