#!/bin/sh
set -e

. "$(dirname -- "$0")/android-env.sh"

"$PROJECT_ROOT/gradlew" :app:assembleDebug
adb wait-for-device

for _ in $(seq 1 90); do
    if [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
        break
    fi
    sleep 2
done

adb install -r "$PROJECT_ROOT/app/build/outputs/apk/debug/app-debug.apk"
adb shell am start -n com.oxygen.weather/.MainActivity
