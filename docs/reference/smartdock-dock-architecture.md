# Smart Dock: arquitectura del dock y la barra de apps

Fuente: rama `dev`, paquete `cu.axel.smartdock`. Rutas relativas a `app/src/main/`. Medido leyendo el código; nada ejecutado.

## 1. Archivos que implementan el dock

Casi todo vive en un solo archivo: `java/cu/axel/smartdock/services/DockService.kt` (2587 líneas). No hay `dimens.xml`; los tamaños están fijos en los layouts y en el servicio.

### Layouts (`res/layout/`)
| Archivo | Responsabilidad |
|---|---|
| `dock.xml` | Raíz `HoverInterceptorLayout` > `RelativeLayout#dock_layout` (fondo `@drawable/round_rect`, padding 5/10dp). Contiene `RecyclerView#apps_lv` (apps fijadas + en ejecución, centrado), `LinearLayout#nav_panel` (izquierda: `apps_btn`, `back_btn`, `home_btn`, `recents_btn`, `assist_btn`, 40dp) y `LinearLayout#system_tray` (derecha: `pin_btn`, `status_area` con `notifications_btn`, `volume_btn`, `bluetooth_btn`, `wifi_btn`, `battery_btn`, y `TextClock#date_btn`). |
| `app_task_entry.xml` | Celda de una app en el dock: `icon_iv` 40dp, `task_count_badge` (nº de ventanas), `running_indicator` (barra 8x3dp, fondo `@drawable/running_indicator`, alpha 0 si no corre). |
| `dock_handle.xml` | Botón de 22dp (`dock_handle`) para el modo "handle" en vez de swipe. |
| `task_list.xml` | Menú flotante (ListView 190dp, fondo `round_rect`) para lista de ventanas y menú contextual de una app del dock. |
| `quick_settings_panel.xml` | Panel de notificaciones + ajustes rápidos (TabLayout `qs_tab_layout`, `volume_btn`, `volume_seekbar`, wifi, bluetooth, `wifi_ssid_tv`, etc.). Fondo `round_rect`. |
| `notification_entry.xml` | Fila de notificación en el panel. |
| `apps_menu.xml`, `app_entry.xml`, `app_entry_large.xml` | Menú de aplicaciones (lanzador) y sus celdas. |
| `power_menu.xml`, `toast.xml`, `pin_entry.xml` | Menú de energía, toast personalizado, entrada de apps fijadas. |

### Drawables (`res/drawable/`)
- `round_rect.xml`: rectángulo, radio 20dp, sólido `#FFFFFF` (se tiñe en código). Fondo del dock redondeado.
- `rect.xml`: rectángulo recto (fondo del dock cuando `round_dock` es false).
- `circle.xml`, `round_square.xml`: fondo de botones/íconos (`icon_shape`: `circle`, `round_rect`, `default`).
- `running_indicator.xml`: radio 5dp, `#CCBEBEBE`.
- `search_background.xml`: fondo de `status_area`.
- `dock_handle_bg_start.xml` / `dock_handle_bg_end.xml`: fondo del handle (radio 10dp en el lado exterior, `#000000`).
- `ic_*.xml` (home, back, recents, apps_menu, pin/unpin, wifi_on/off, bluetooth/_off, volume, `ic_expand_up_circle` para el botón de notificaciones, `ic_assistant`), `battery_*.xml` y `battery_charging_*.xml` (20/30/50/60/80/90/full/empty).
- `res/anim/slide_up.xml`, `slide_down.xml`: animación de mostrar/ocultar el dock.

### Valores y temas
- `res/values/colors.xml`: paleta Material3 (`md_theme_*`), `@color/action`. El color del dock NO sale de aquí.
- `res/values/themes.xml`: `AppTheme.Dock` (padre `Theme.Material3.Dark`) se aplica al inflar `dock.xml` y el panel; `AppTheme.Dock.Dialog`.
- `res/values/arrays.xml`: listas de las preferencias (métodos de activación, posiciones del handle, etc.).

### Código
- `services/DockService.kt`: `AccessibilityService` que crea y administra todo (dock, handle, menú de apps, panel, menú de energía, toasts, esquinas activas, notificaciones emergentes).
- `adapters/DockAppAdapter.kt`: adapter del RecyclerView del dock. Lee prefs `icon_pack`, `icon_padding`, `icon_shape`, `tint_indicators`. Pinta icono (con icon pack si existe), fondo teñido con el color dominante del icono, indicador (ancho 16dp si `app.packageName == AppUtils.currentApp`, 8dp si no), contador de ventanas.
- `models/DockApp.kt` (extiende `App`, lleva `tasks: ArrayList<AppTask>`), `models/AppTask.kt`, `models/App.kt`.
- `widgets/HoverInterceptorLayout.kt`: raíz del dock; intercepta hover para que mouse sobre el dock lo muestre/oculte.
- `utils/Utils.kt` (`makeWindowParams`, `dpToPx`, `getBatteryDrawable`), `utils/ColorUtils.kt` (temas), `utils/AppUtils.kt` (tareas, apps fijadas), `utils/IconPackUtils.kt`, `utils/OnSwipeListener.kt`, `utils/DeviceUtils.kt`.
- `receivers/BatteryStatsReceiver.kt`, `services/NotificationService.kt`, `components/NotificationLayout.kt`, `adapters/NotificationAdapter.kt`, `wrappers/WifiManagerWrapper.kt`, `BluetoothManagerWrapper.kt`, `ActivityManagerWrapper.kt`.
- Preferencias: `fragments/AppearancePreferences.kt`, `DockPreferences.kt`, `DockElementsPreferences.kt` (+ `Landscape`), `NotificationPreferences.kt`; XML en `res/xml/preferences_*.xml`; `dialogs/DockLayoutDialog.kt` (presets).

## 2. Flujo de datos

**Overlay.** `DockService.onServiceConnected` -> `createViews()` -> `createDock()`. El dock se infla con `ContextThemeWrapper(AppTheme_Dock)` y se añade con `windowManager.addView(dock, dockLayoutParams)`. `Utils.makeWindowParams(-1, dockHeight, ...)` produce `TYPE_APPLICATION_OVERLAY`, `FLAG_NOT_FOCUSABLE`, `PixelFormat.TRANSLUCENT`, ancho = ancho total de pantalla (`-1` se recorta con `coerceAtMost`), `gravity = BOTTOM | START`, sin márgenes. Se crean aparte: handle (22dp, `gravity BOTTOM | START/END`), esquinas activas, menú de apps (`createAppMenu`), `NotificationLayout` (popups, `y = 2dp + dockHeight`). Pantalla destino por `prefer_last_display` (`DeviceUtils.getDisplayContext`).

**Mostrar/ocultar.** `dock` (la ventana) siempre existe. En modo `swipe` la ventana mide `dock_activation_area` dp (10 por defecto) y `dock_layout` interno está `GONE`; hover/swipe llama `showDock()` (sube la ventana a `dock_height`, anima `slide_up`). `hideDock(delay)` hace lo inverso salvo si `isPinned`. En modo `handle` el dock se oculta y aparece `dock_handle`. `pin_btn`/`togglePin()` fija.

**Apps en ejecución.** `onAccessibilityEvent` reacciona a `TYPE_WINDOWS_CHANGED` (ventana añadida/removida) -> `updateRunningTasks()` (throttle 500 ms). Fuente de tareas, en orden: (1) app de sistema o permiso Shizuku: `AppUtils.getRunningTasks` (ActivityManager, filtra systemui/launcher/recents; guarda `AppUtils.currentApp` = primera tarea); (2) si no, `AppUtils.getRecentTasks` con `UsageStatsManager` (permiso `PACKAGE_USAGE_STATS`, sin ID de ventana, `tasks[0].id` puede ser -1 y entonces el indicador queda en alpha 0). Máximo `max_running_apps` / `max_running_apps_landscape`.

**Apps fijadas.** `loadPinnedApps()` -> `AppUtils.getPinnedApps(context, "dock_pinned.lst")` (archivo en storage de la app, no SharedPreferences). La lista del dock = fijadas primero, luego tareas; `AppUtils.containsTask` agrupa tareas de un mismo paquete en un `DockApp` (contador de ventanas). Favoritas del menú de apps: `pinned.lst`.

**Clic.** `onDockAppClicked`: 1 tarea -> la trae al frente; varias -> `task_list.xml` flotante con `windowManager.addView`; sin tareas -> lanza. Largo clic o botón secundario -> `showDockAppContextMenu`.

**Notificaciones.** Dos servicios: `DockService` (accesibilidad; también toasts personalizados con `custom_toasts`) y `NotificationService` (`NotificationListenerService`). `DockService.bindNotificationService()` se enlaza por AIDL (`INotificationServiceBridge`) y registra `INotificationCallback`; en `onNotificationPosted/Removed` actualiza el contador (`updateNotificationCount`: si hay >0, `notifications_btn` usa `circle` con el número, si no `ic_expand_up_circle`), el panel (`updateNotificationArea`, filtra `ignored_notifications_panel`) y los popups (`NotificationLayout`).

**Estado de red/BT/batería.** `ConnectivityManager.NetworkCallback` -> `updateWiFiStatus()` cambia `wifi_btn` entre `ic_wifi_on/off`. `updateBluetoothStatus()` entre `ic_bluetooth/_off`. `BatteryStatsReceiver` (ACTION_BATTERY_CHANGED) pone el drawable `Utils.getBatteryDrawable(level, charging)` como compound drawable de `battery_btn`; el texto `NN%` solo si `show_battery_level`.

### Claves de SharedPreferences relevantes (default `PreferenceManager.getDefaultSharedPreferences`)
- Geometría/comportamiento: `dock_height` (56), `dock_activation_area` (10), `activation_method` (`swipe`|handle), `handle_position` (`start`|`end`), `handle_opacity` (0.5), `center_running_apps` (true), `max_running_apps`, `max_running_apps_landscape`, `lock_landscape`, `prefer_last_display`, `auto_pin`, `dock_layout` (int: 0 teléfono, 1 tablet, 2 escritorio; -1 sin elegir).
- Aspecto: `theme` (`dark`|`black`|`transparent`|`material_u`|`custom`), `theme_main_color`, `theme_main_alpha`, `override_dock_background_alpha`, `dock_background_alpha`, `round_dock`, `icon_shape`, `icon_padding` (5), `icon_pack`, `tint_indicators`, `single_line_labels`, `menu_icon_uri`.
- Elementos (sufijo `_landscape` en horizontal): `enable_nav_apps|back|home|recents|assist`, `enable_qs_notif|bluetooth|battery|wifi|vol|date|pin`, `show_battery_level`.
- Notificaciones: `show_notifications`, `notification_display_time`, `show_ongoing`, `silence_current`, `ignored_notifications_popups`, `enable_notif_panel`, `ignored_notifications_panel`, `custom_toasts`.
- Otros: `app_clock` (app del reloj al tocar la fecha), `hot_corners_delay`, `enable_corner_top_right|bottom_right`.
- `onSharedPreferenceChanged` (DockService ~L1361) aplica en vivo: `theme*` -> `applyTheme`, `round_dock` -> `updateDockShape`, `dock_height` -> `updateDockHeight`, `icon_*`/`tint_indicators` -> `updateRunningTasks(true)`, `enable_nav_*`, `enable_qs_*`, etc.

## 3. Qué se reestiliza sin tocar lógica

Solo XML/valores/drawables:
- Radios: `round_rect.xml` (20dp), `rect.xml`, `running_indicator.xml` (5dp), `dock_handle_bg_*.xml`.
- Tamaños de botones/iconos/paddings de `dock.xml` y `app_task_entry.xml` (40dp, 22dp, 8dp, márgenes). Ojo: `updateRunningTasks` fija `tasksGv.layoutParams.width = dpToPx(52) * apps.size` (asume celda de 52dp); si cambias el tamaño de celda en `app_task_entry.xml`, ajusta ese 52.
- Altura del dock: pref `dock_height` (slider/lista), sin código.
- Iconos del tray y batería: reemplazar `ic_*.xml` y `battery_*.xml`.
- Forma y relleno de iconos: prefs `icon_shape`, `icon_padding`, `tint_indicators`.
- Fondo/opacidad: prefs `theme`, `theme_main_color`, `dock_background_alpha`. Ojo: el XML de fondo es blanco y se tiñe en código con `ColorUtils.applyMainColor` (`setColorFilter SRC_ATOP`), así que cambiar el color en el drawable no tiene efecto visible.
- Animaciones: `res/anim/slide_up.xml`, `slide_down.xml`.
- Color de texto de fecha/batería: `android:textColor` en `dock.xml` (blanco fijo).

Requiere cambio de código (comportamiento):
- **Dock flotante centrado con márgenes**: `dockLayoutParams` usa ancho de pantalla, `gravity BOTTOM|START`, sin `x/y`. Hay que cambiar `createDock()` (ancho `WRAP_CONTENT` o fijo, `gravity BOTTOM|CENTER_HORIZONTAL`, `y` = margen), y propagar el offset a todo lo que se ancla con `dockHeight` (panel rápido L1650, `NotificationLayout.y`, menús contextuales, menú de apps, `showDock`/`hideDock` que redimensionan la ventana a `dock_activation_area`). Además `dock_layout` con `nav_panel` a la izquierda y `system_tray` a la derecha asume ancho completo (`RelativeLayout` con `alignParentStart/End`).
- **Agrupar ventanas**: ya existe agrupación por paquete (`DockApp.tasks`, badge). Cambiar a "una app = un ícono con vista previa" o menú de ventanas distinto exige tocar `onDockAppClicked` y `task_list.xml`.
- **Indicador de apps abiertas con otro diseño** (punto, subrayado largo, glow): el aspecto base es `running_indicator.xml` (sí reestilizable); cambiar la lógica (p. ej. indicar solo foco vs. todas) está en `DockAppAdapter.onBindViewHolder` (ancho 16/8dp, alpha 0/1 con `tasks[0].id != -1`).
- **Presets**: hoy `DockLayoutDialog` solo escribe un lote de prefs (`dock_layout`, `max_running_apps`, `activation_method`, `dock_activation_area`, visibilidad). Para presets visuales (radio, margen, tamaño) habría que añadir prefs nuevas y leerlas en `updateDockShape`/`createDock`; el `onSharedPreferenceChanged` actual no conoce claves nuevas.
- Reordenar el layout (tray al centro, etc.): `placeRunningApps()` manipula reglas del `RelativeLayout` por código.

## 4. Reloj, fecha, batería, wifi, bluetooth, volumen, notificaciones

Todo está dentro de `dock.xml > system_tray`:
- **Reloj/fecha**: `TextClock#date_btn` (`dateTv` en el servicio). Formato del sistema (sin `format12Hour/24Hour` explícito), color blanco fijo. Clic abre `app_clock` (default `com.android.deskclock`); largo clic abre `Settings.ACTION_DATE_SETTINGS`. Visible por `enable_qs_date`.
- **Batería**: `TextView#battery_btn` con `drawableStart` dinámico; `BatteryStatsReceiver` actualiza icono y `updateBatteryBtn()` el texto `%`. Visible por `enable_qs_battery`.
- **Wifi**: `ImageView#wifi_btn` 20dp; `updateWiFiStatus()` (solo ON/OFF; el SSID solo aparece en el panel).
- **Bluetooth**: `ImageView#bluetooth_btn`; `updateBluetoothStatus()`.
- **Volumen**: `ImageView#volume_btn` en el dock es solo un icono; el control real es `volume_seekbar` del `quick_settings_panel` (stream `STREAM_MUSIC`), abierto al tocar `status_area` (`toggleQuickSettingsPanel(1)`).
- **Bandeja/notificaciones**: `TextView#notifications_btn` (22dp, círculo con contador); clic abre el panel (pestaña 0) si `enable_notif_panel` y `NotificationService` está corriendo, si no pide permiso (`NotificationPermissionDialog`) o usa `GLOBAL_ACTION_QUICK_SETTINGS`. Panel: ventana overlay 400x305dp, `gravity BOTTOM|END`, `y = 2dp + dockHeight`, `x = 2dp`, fondo `round_rect` teñido con `applyMainColor`.
- Colores de botones/área: `ColorUtils.applySecondaryColor` sobre `back_btn`, `home_btn`, `recents_btn`, `assist_btn`, `pin_btn`, `status_area` (lo hace `applyTheme()`). `getMainColors` devuelve `[0]` color principal, `[1]` alpha, `[2]` secundario (principal x1.2-2.2), `[3]` alpha secundario (alpha - 60%), `[4]` separadores.

## 5. Permisos y registro

`AndroidManifest.xml`: `DockService` con `BIND_ACCESSIBILITY_SERVICE` y config `res/xml/accessibility_service.xml` (eventos `typeNotificationStateChanged|typeWindowsChanged|typeWindowStateChanged`, `flagRetrieveInteractiveWindows`, `canRequestFilterKeyEvents`); `NotificationService` con `BIND_NOTIFICATION_LISTENER_SERVICE`; permisos `SYSTEM_ALERT_WINDOW`, `GET_TASKS`, `PACKAGE_USAGE_STATS`, `QUERY_ALL_PACKAGES`, `WRITE_SECURE_SETTINGS`.
