#!/bin/sh
set -e

. "$(dirname -- "$0")/android-env.sh"

mkdir -p "$XDG_RUNTIME_DIR"
chmod 700 "$XDG_RUNTIME_DIR"

AVD_NAME="${OXYGEN_AVD_NAME:-oxygen_starter}"
EMULATOR_WINDOW="${OXYGEN_EMULATOR_WINDOW:-${SPACE_GAME_EMULATOR_WINDOW:-0}}"
EMULATOR_NETWORK="${OXYGEN_EMULATOR_NETWORK:-1}"

if [ "$EMULATOR_NETWORK" = "1" ]; then
    (
        adb -e wait-for-device

        for _ in $(seq 1 90); do
            if [ "$(adb -e shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
                break
            fi
            sleep 2
        done

        adb -e shell cmd connectivity airplane-mode disable >/dev/null 2>&1 || true
        adb -e shell svc wifi enable >/dev/null 2>&1 || true
        adb -e shell svc data enable >/dev/null 2>&1 || true
    ) &
fi

if [ "$EMULATOR_WINDOW" = "1" ]; then
    emulator -avd "$AVD_NAME"
else
    emulator -avd "$AVD_NAME" \
        -no-window \
        -no-audio \
        -gpu swiftshader_indirect \
        -no-snapshot
fi
