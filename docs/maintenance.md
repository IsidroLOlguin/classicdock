# Mantenimiento de ClassicDock

## Compilar e instalar
`scripts/deploy.sh` compila el APK debug, lo instala por adb y otorga `WRITE_SECURE_SETTINGS`, `SYSTEM_ALERT_WINDOW`, `ACCESS_RESTRICTED_SETTINGS` y `GET_USAGE_STATS`. Requiere un solo dispositivo en adb.

## Activar el servicio (una vez, o tras `force-stop`)
La instalación con `-r` conserva el servicio activo. `am force-stop` lo quita de la lista; se repone con:
```
adb shell settings put secure enabled_accessibility_services "<los que ya había>:dev.isidro.classicdock/cu.axel.smartdock.services.DockService"
```
Leer primero la lista actual con `settings get secure enabled_accessibility_services`.

## Trampas medidas
- Sin `SYSTEM_ALERT_WINDOW` el servicio truena con `BadTokenException type 2038`.
- En One UI 8 «Permitir ajustes restringidos» no aparece en el menú: se da por `appops`.
- Las preferencias se leen al arrancar el servicio; cambiar el XML de `shared_prefs` exige `force-stop` y reactivar.
- Con dos perfiles (trabajo, usuario 150) `adb shell pm list packages` falla: usar `--user 0`.
- Los indicadores de app abierta solo salen con Shizuku o app de sistema.

## Cambios propios sobre Smart Dock
Íconos de 44dp (`dimens.xml`), dock flotante (`dock_float_margin`, `Utils.dockHeightPx`), tema `fully_transparent`, indicadores 16x3dp / 4x4dp, píldora de bandeja `tray_pill.xml`, animaciones `dock_show` / `dock_hide`. El namespace del código sigue siendo `cu.axel.smartdock`.

## Actualizar desde upstream
`git fetch upstream` y mergear a una rama aparte; nunca se hace push a `upstream`. Conflictos probables: `DockService.kt`, `dock.xml`, `DockAppAdapter.kt`.
