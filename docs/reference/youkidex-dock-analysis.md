# Análisis del dock de YoukiDEX (referencia, GPL-3.0)

Código: `reference/youkidex/` (commit f804102). Todo bajo `app/src/main/`. Abreviaturas: `J` = `java/com/youki/dex`, `R` = `res`. `PerfectServer.kt` (6329 líneas) contiene la clase `DockService` (dock, menú de apps, panel rápido, menú de energía) y al final `NotificationService`.

## 1. Archivos que implementan el dock

| Ruta | Qué hace |
|---|---|
| `R/layout/dock.xml` | Layout único del dock. Raíz `HoverInterceptorLayout` (elevation 5dp) > `RelativeLayout#dock_layout` (fondo `round_rect`, padding start 5dp / end 10dp). Tres zonas: `nav_panel` (izq.), `center_group` (apps abiertas, `RecyclerView#apps_lv`) y `system_tray` (der.). |
| `R/layout/app_task_entry.xml` | Celda de una app en el dock: ícono 44dp, badge de conteo de ventanas (9sp, `circle`, alpha 0, elevation 4dp, translation 4dp/-2dp) e indicador de ejecución 16x3dp. |
| `R/layout/dock_handle.xml` | Asa colapsable: botón 22dp de ancho, `drawableStart=ic_expand_right`. Fondos `drawable/dock_handle_bg_start|end.xml` (negro `#000000`, radio 10dp en el lado exterior). |
| `R/layout/notification_panel.xml`, `quick_settings_panel.xml`, `audio_panel.xml`, `bluetooth_panel.xml`, `notification_entry.xml` | Paneles que salen del dock (bandeja de notificaciones, ajustes rápidos, volumen, Bluetooth). Fondo `round_rect`. |
| `R/layout/apps_menu.xml`, `app_entry.xml`, `app_entry_large.xml` | Menú de apps (el "lanzador" que abre el botón de la izquierda). |
| `R/drawable/round_rect.xml` | Fondo "flotante": rectángulo radio **20dp**, color `?attr/colorSurfaceVariant`. |
| `R/drawable/rect.xml` | Fondo "pegado": rectángulo sin esquinas, sólido blanco (se tiñe por código). |
| `R/drawable/btn_bubble_rect.xml` | "Píldora" interna de grupos de botones: radio **14dp**, negro (se tiñe por código). `btn_bubble.xml` (óvalo) y `btn_bubble_audio.xml` (radio 20dp) son variantes. |
| `R/drawable/running_indicator.xml` | Indicador de app abierta: rectángulo radio 10dp, `#CCFFFFFF` (alpha 0xCC = 80%). |
| `R/drawable/circle.xml` | Óvalo gris `#808080` (fondo del badge y forma de ícono "circle"). Hay también `round_square`, `win_square` como fondos de ícono. |
| `R/drawable/notification_ring.xml` | Óvalo con trazo 2dp `#FF4444`. |
| `R/drawable/round_rect_top.xml` | Radio 24dp solo arriba, `colorSurface` (hoja inferior). |
| `R/values/colors.xml`, `R/values/themes.xml`, `R/values-night/themes.xml` | Paleta Material 3 (seed `#2b69e2`, `md_theme_light_*` / `md_theme_dark_*`). |
| `R/anim/btn_press.xml`, `btn_release.xml` | Pulsación: escala 1.0 a 0.82 y alpha 1.0 a 0.60 en 55 ms (acelerado). |
| `R/anim/win_slide_up.xml`, `win_slide_down.xml` | Entrada estilo Windows: traslación 60% a 0 + escala 0.92 a 1.0, 280 ms, decelerado. |
| `R/xml/preferences_dock.xml`, `preferences_appearance.xml`, `preferences_notification.xml` | Pantallas de ajustes con todas las claves (listadas abajo). |
| `J/services/PerfectServer.kt` (`DockService`) | Servicio overlay que infla `dock.xml`, crea la ventana (`Utils.makeWindowParams`), forma, colores, animaciones, monitor de recursos, tareas abiertas, menús. |
| `J/adapters/DockAppAdapter.kt` | Adapter del `RecyclerView` del dock: ícono, indicador, badge, fondo de ícono. |
| `J/models/DockApp.kt` | Modelo: una app con lista de `tasks`; `icon` devuelve el de la tarea si hay 1, el de la app si hay varias. |
| `J/utils/DockPositionUtils.kt` | Posición TOP/BOTTOM (pref `dock_position`), gravity, animación de mostrar/ocultar. |
| `J/utils/ColorUtils.kt` | Temas, Material You, alphas (`getMainColors`, `getBubbleColor`, `applyMainColor`). |
| `J/utils/AppUtils.kt` (líneas ~833-865) | `DockSizeConfig` y presets de tamaño. |
| `J/dialogs/DockLayoutDialog.kt` | Diálogo de presets de layout (0/1/2). |
| `J/fragments/DockPreferences.kt`, `AppearancePreferences.kt` | Lógica de las pantallas de ajustes. |
| `J/widgets/HoverInterceptorLayout` | Raíz del dock; intercepta hover para mostrar/ocultar. |
| `NotificationService` (en `PerfectServer.kt` ~4617) | `NotificationListenerService` que alimenta la bandeja. |

No hay Compose: todo es XML + Views + Material Components.

## 2. Cómo se resuelve cada aspecto visual

### Flotante centrado (KDE) vs pegado al borde (Windows)
- Pref `round_dock` (default false). Se aplica en `DockService.recomputeDockShape()` (~3374) y en el arranque (~669-697).
- **Pegado** (`R.drawable.rect`): ancho `MATCH_PARENT` (-1), `gravity = BOTTOM|START` (o `TOP|START`), `y = 0`, sin esquinas.
- **Flotante** (`R.drawable.round_rect`, radio 20dp): ancho = `usableDisplayWidth - 2*8dp`, `gravity = BOTTOM|CENTER_HORIZONTAL`, `y = 8dp` (variable `dockMargin = dpToPx(8)`), `x = leftInset`. Es una barra flotante a todo el ancho con 8dp de margen, no un dock que se encoge al contenido; solo el grupo central está centrado (ver abajo).
- Posición vertical: pref `dock_position` = `bottom`|`top` (`DockPositionUtils.dockGravity`). Izq./der. se eliminaron por inestabilidad.
- Altura: pref `dock_height`, default **56dp** en `DockService` (el slider de preferencias declara 48). Presets de tamaño `dock_size_preset` (`AppUtils.getDockSizeConfig`): `small` = altura 40 / ícono 34 / grid 42 dp; `pc` = 30 / 24 / 32 dp usando densidad del sistema; resto = `dock_height` / ícono 50 / grid 52.
- Pref `always_floating`: no es del dock, fuerza lanzar apps en ventana flotante (modo `standard`).

### Presets de dock
No se llaman Minimal/Default/Gesture en la UI: el array `layouts` (`R/values/arrays.xml`) es **Phone / Tablet / Desktop**, y los comentarios de `DockLayoutDialog.kt` los nombran 0 = minimal, 1 = default, 2 = "swipe"/gesture. Valores aplicados (guardados en `dock_layout`):

| Clave | 0 Minimal (Phone) | 1 Default (Tablet) | 2 Gesture (Desktop) |
|---|---|---|---|
| botones nav (back/home/recents) | off | on | on |
| wifi / volumen / fecha / notif. en bandeja | off | on | on |
| `max_running_apps` (vertical) | 4 | 10 | 15 |
| `max_running_apps_landscape` | 8 | 10 | 15 |
| `dock_activation_area` | 25 | 25 | 5 |
| `app_menu_fullscreen` | true | true | false |
| `enable_qs_pin` | true | true | false |
| `activation_method` | handle | handle | handle (el modo swipe se eliminó) |
Todos fijan `launch_mode=standard` (ventana libre).

### Agrupación de ventanas bajo un ícono
`DockService` (~3270-3283): por cada tarea con el mismo `packageName`, `DockApp.addTask()`; si no existe, `DockApp(task)`. El adapter (`DockAppAdapter` ~127-129) muestra el **badge numérico** `tasks.size` si hay más de 1 ventana (9sp, blanco sobre círculo gris, esquina sup. der. del ícono). Sin menú de miniaturas; el clic sobre varias ventanas se resuelve en la lógica de `DockService` (tiling por ventana: `tiled-left/right/top/bottom`, `AppUtils.resizeTask`).

### Indicadores de apps abiertas
`DockAppAdapter` líneas ~94-125; pref `show_running_indicators` (default true), `tint_indicators` (tiñe con el color dominante del ícono).
- App activa en primer plano: barra **16x3dp**, radio 10dp, `#CCFFFFFF`.
- App en segundo plano: punto **4x4dp**.
- "Realmente en ejecución" se decide cruzando con la lista de procesos vivos (Shizuku opcional, `shizuku_active_app_detection`).
- Fondo de ícono opcional: `circle` / `round_square` / `win_square` (padding 6dp en estilo "win", si no `icon_padding` default 5dp).

### Reloj, batería, Wi-Fi, Bluetooth, volumen, notificaciones (`dock.xml`, `system_tray`)
Un `status_area` (píldora `btn_bubble_rect` 14dp, padding 8/4dp, margen 5dp) con, en orden: pin (28dp), notificaciones (22dp, contador 10sp), volumen (20dp), Bluetooth (20dp, oculto por defecto), Wi-Fi (18dp), batería (texto 11sp, oculto por defecto), monitor de recursos. A la derecha, `TextClock#date_btn` 13sp blanco, sin acción al tocar. Todo se controla por prefs `enable_qs_*` (defaults: wifi, vol, date, pin = on; notif, bluetooth, battery = off).

### CPU y RAM
Pref `show_resource_monitor` (default false). TextView `resource_monitor_tv` 11sp monospace, texto inicial "0% 0M". Se refresca cada **2000 ms** (`postDelayed(this, 2000)`, primer tick 1200 ms): CPU leyendo `/proc/stat`, RAM con `ActivityManager.MemoryInfo` (formato `"%.1fG"` si >=1024 MB, si no `"NNNM"`).

### Bandeja de notificaciones
`NotificationService` (`NotificationListenerService`) + `R/layout/notification_panel.xml` (área con fondo `round_rect`, padding 4/8dp, cabecera 40dp con ícono 16dp y título 12sp, letterSpacing 0.05). Se abre con clic al botón de notificaciones o con pulsación larga en `status_area` (`toggleNotificationPanel`). Se ancla sobre el dock: `y = dockHeight + dockFloatMargin(8dp si round_dock) + margins`. Panel en captura 2 integra reproductor, sliders de volumen/brillo y mosaicos Wi-Fi / Bluetooth / Cafe Mode / No molestar.

### Opacidad
- Tema `transparent`: color `#050505` con alpha **225** (88%). `fully_transparent`: alpha **0**. `custom`: pref `theme_main_alpha` (0-255, default 255). Secundario: si alpha < 255, `alpha - 60%` (p. ej. 225 pasa a 90).
- Override independiente: `override_dock_background_alpha` + `dock_background_alpha` (default 255), aplicado a `dockLayout.background.alpha`.
- Burbujas/píldoras: `bubble_mode` (`material_u` | `material_u_light` | custom `bubble_color` default `#808080`), `bubble_alpha` default 255. Se aplica con `setColorFilter(SRC_ATOP)` + `alpha` a `actionBtnsGroup`, `navBtnsGroup`, `statusArea` (`applyBubbleColors`, ~4007).
- Asa: `handle_opacity` default 0.5.

### Esquinas
Dock flotante 20dp; píldoras internas 14dp; asa 10dp (solo lado exterior); panel de notificaciones 20dp; hoja inferior 24dp arriba; indicador 10dp; dock pegado 0dp.

### Colores Material You
`ColorUtils.getMainColors(prefs, ctx)`, pref `theme` (default `material_u`; valores `dark`, `black`, `transparent`, `material_u`, `material_u_light`, `custom`):
- `dark`: `#212121`, secundario x1.35. `black`: `#060606`, sec. x2.2. `transparent`: ver arriba.
- `material_u`: `getThemeColors(forceDark=true)[1]` (surface oscuro por `DynamicColors`) con `manipulateColor(x0.85)`; secundario x1.1. Fallback sin dynamic color: colores del fondo de pantalla.
- `material_u_light`: surface claro x0.96; secundario x0.88.
- `custom`: `theme_main_color` default `#212121`, secundario x1.2.
- `getBubbleColor`: surface oscuro x0.80 (fallback `rgb(30,30,30)`).
- Caché de contexto dinámico (`dynamicColorCache`) para evitar reinflar; `applyTheme()` (~4044) reaplica al cambiar prefs.

### Animaciones del dock
- Mostrar/ocultar (`DockPositionUtils.animateDockVisibility`): 180 ms; entra desde alpha 0, escala 0.90, desplazado un pequeño `distancePx` en la dirección del borde; `DecelerateInterpolator(2f)`; sale con `AccelerateInterpolator(2f)`.
- Botón apps: pulsación a escala 0.82 / alpha 0.65 y vuelve a 1 (`DockService` ~486).
- Menú de apps: alpha 0 a 1, escala desde 0.92, translationY; sale a 0.92. Menú de energía 180 ms.
- Toast: fade in 150 ms, fade out 600 ms.

### Layout interno (resumen de dimensiones)
- Izquierda `nav_panel`: botón apps 42dp (padding 6dp), píldora de acciones (usuario 35dp, fondo 40dp, cast 35dp), píldora de navegación (back/home/recents/asistente, 35dp c/u, padding 9dp).
- Centro: `apps_btn_center` 32dp (opcional, pref `center_apps_btn`, estilo Windows 11) + `RecyclerView` con `fadingEdgeLength` 6dp. `center_running_apps` (default true) usa `CENTER_IN_PARENT`; si no, entre `nav_panel` y `system_tray`.

## 3. Capturas de YoukiDEX
Copiadas a `classicdock/docs/reference/youkidex/` (`1.jpg`, `2.jpg`, `3.jpg`, 800x450).

- **`youkidex/1.jpg`**: escritorio oscuro con ilustración. Barra inferior **flotante** (esquinas redondeadas, margen visible a los costados y abajo), casi negra, a ancho completo. Izquierda: avatar de usuario + ícono galería (píldora), luego atrás y home. Centro: botón de cuadrícula (menú de apps) y 5 íconos redondos (Brave, Archivos, Google, Ajustes, TikTok) con puntitos de ejecución debajo; el último tiene barra larga (activo). Derecha: píldora con pin, contador, volumen, Bluetooth, Wi-Fi, batería "1%", y reloj "6:13 PM" fuera de la píldora.
- **`youkidex/2.jpg`**: igual, con el **menú de apps** abierto encima del centro (panel oscuro redondeado, cuadrícula de 5 columnas, etiquetas pequeñas) y el **panel de notificaciones / ajustes rápidos** a la derecha (reproductor, aviso de overlay, sliders de volumen y brillo, 4 mosaicos: Wi-Fi activo en azul, Bluetooth, Cafe Mode, No molestar). Ambos anclados justo sobre el dock.
- **`youkidex/3.jpg`**: **menú de usuario/energía** a la izquierda (avatar, Settings, Lock, Close DEX, Restart DEX, Power off en rojo, Restart) y panel de notificaciones a la derecha. Dock con el mismo estilo; ahora 5 íconos abiertos.

Lectura visual: el dock es una barra completa translúcida/oscura, no un dock KDE que se ajuste al contenido; el efecto "centrado" viene solo de que los íconos abiertos están al centro. Los íconos van sin fondo, con indicador inferior fino.

## 4. Comparación con DeX clásico
Pendiente: Isidro aportará capturas del DeX clásico (`classicdock/docs/reference/dex-classic/` no existe).
