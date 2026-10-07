# Mantenimiento de ClassicDock

## Compilar e instalar
`scripts/deploy.sh` compila el APK debug, lo instala por adb y otorga `WRITE_SECURE_SETTINGS`, `SYSTEM_ALERT_WINDOW`, `ACCESS_RESTRICTED_SETTINGS` y `GET_USAGE_STATS`. Requiere un solo dispositivo en adb.

## Activar el servicio (una vez, o tras `force-stop`)
La instalación con `-r` conserva el servicio activo. `am force-stop` lo quita de la lista; se repone con:
```
adb shell settings put secure enabled_accessibility_services "<los que ya había>:dev.isidro.classicdock/cu.axel.smartdock.services.DockService"
```
Leer primero la lista actual con `settings get secure enabled_accessibility_services`.

## Shizuku (ids de tarea reales, requerido por S-2.1)
Instalado en la tablet desde el APK de RikkaApps (v13.6.0). No sobrevive a un reinicio de la tablet; arrancarlo con la tablet por USB:
```
P=$(adb shell pm path moe.shizuku.privileged.api | sed 's/package://; s/base.apk//')
adb shell "${P}lib/arm64/libshizuku.so"
```
Primera vez: ClassicDock > Administrar permisos > Opcional > Shizuku > Conceder > «Permitir todo el tiempo». Después hay que reiniciar el servicio (`am force-stop` y reponer accesibilidad): los wrappers se crean solo al arrancar. Con Shizuku, `getRunningTasks` devuelve ids reales (medido: 46, 48, 47...) y no -1.

## Trampas medidas
- Sin `SYSTEM_ALERT_WINDOW` el servicio truena con `BadTokenException type 2038`.
- En One UI 8 «Permitir ajustes restringidos» no aparece en el menú: se da por `appops`.
- Las preferencias se leen al arrancar el servicio; cambiar el XML de `shared_prefs` exige `force-stop` y reactivar.
- Con dos perfiles (trabajo, usuario 150) `adb shell pm list packages` falla: usar `--user 0`.
- En la tablet las notificaciones de ClassicDock están bloqueadas y el interruptor no se puede activar (la app no declara `POST_NOTIFICATIONS`): Samsung suprime sus `Toast` (`NotificationService: Suppressing toast`). Los avisos que deban verse van dentro de la barra, no solo en `Toast`.
- Los indicadores de app abierta solo salen con Shizuku o app de sistema.
- Barra de controles sin Shizuku: sigue visible (la tarea se deduce de la ventana de aplicación activa de accesibilidad), Minimizar funciona y Cerrar muestra «Sin Shizuku no se puede cerrar la app». Sin Shizuku no distingue ventanas freeform ni Recents de otro launcher.

## Cambios propios sobre Smart Dock
Íconos de 44dp (`dimens.xml`), dock flotante (`dock_float_margin`, `Utils.dockHeightPx`), tema `fully_transparent`, indicadores 16x3dp / 4x4dp, píldora de bandeja `tray_pill.xml`, animaciones `dock_show` / `dock_hide`. El namespace del código sigue siendo `cu.axel.smartdock`.

## Actualizar desde upstream
`git fetch upstream` y mergear a una rama aparte; nunca se hace push a `upstream`. Conflictos probables: `DockService.kt`, `dock.xml`, `DockAppAdapter.kt`.
