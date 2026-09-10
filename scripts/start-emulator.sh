#!/bin/sh
set -e

. "$(dirname -- "$0")/android-env.sh"

mkdir -p "$XDG_RUNTIME_DIR"
chmod 700 "$XDG_RUNTIME_DIR"

AVD_NAME="${OXYGEN_AVD_NAME:-oxygen_starter}"
EMULATOR_WINDOW="${OXYGEN_EMULATOR_WINDOW:-${SPACE_GAME_EMULATOR_WINDOW:-0}}"
# Keep the old variable as a temporary compatibility fallback for existing
# local scripts; OXYGEN_EMULATOR_WINDOW is the supported name.
EMULATOR_NETWORK="${OXYGEN_EMULATOR_NETWORK:-1}"
EMULATOR_SERIAL=$(
    adb devices | awk '
        /^emulator-[0-9]+[[:space:]]/ {
            print $1
            exit
        }
    '
)

adb_for_emulator() {
    if [ -n "$EMULATOR_SERIAL" ]; then
        adb -s "$EMULATOR_SERIAL" "$@"
    else
        adb -e "$@"
    fi
}

if [ -n "$EMULATOR_SERIAL" ]; then
    echo "Using existing emulator $EMULATOR_SERIAL" >&2
    adb_for_emulator wait-for-device

    for _ in $(seq 1 90); do
        if [ "$(adb_for_emulator shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
            break
        fi
        sleep 2
    done

    if [ "$EMULATOR_NETWORK" = "1" ]; then
        adb_for_emulator shell cmd connectivity airplane-mode disable >/dev/null 2>&1 || true
        adb_for_emulator shell svc wifi enable >/dev/null 2>&1 || true
        adb_for_emulator shell svc data enable >/dev/null 2>&1 || true
    fi

    exit 0
fi

if [ "$EMULATOR_NETWORK" = "1" ]; then
    (
        adb_for_emulator wait-for-device

        for _ in $(seq 1 90); do
            if [ "$(adb_for_emulator shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
                break
            fi
            sleep 2
        done

        adb_for_emulator shell cmd connectivity airplane-mode disable >/dev/null 2>&1 || true
        adb_for_emulator shell svc wifi enable >/dev/null 2>&1 || true
        adb_for_emulator shell svc data enable >/dev/null 2>&1 || true
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
