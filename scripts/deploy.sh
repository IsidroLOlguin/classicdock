#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

APP_ID=dev.isidro.classicdock
APK=app/build/outputs/apk/debug/app-debug.apk

./gradlew assembleDebug --console=plain -q
adb get-state >/dev/null 2>&1 || { echo "No hay dispositivo en adb. Empareja la tablet (adb pair / adb connect)." >&2; exit 1; }
adb install -r "$APK"
adb shell pm grant "$APP_ID" android.permission.WRITE_SECURE_SETTINGS

echo "Listo. A mano en la tablet: 'Permitir ajustes restringidos' y activar el servicio de accesibilidad de ClassicDock."
