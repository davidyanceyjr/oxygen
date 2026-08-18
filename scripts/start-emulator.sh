#!/bin/sh
set -e

. "$(dirname -- "$0")/android-env.sh"

mkdir -p "$XDG_RUNTIME_DIR"
chmod 700 "$XDG_RUNTIME_DIR"

AVD_NAME="${OXYGEN_AVD_NAME:-oxygen_starter}"
EMULATOR_WINDOW="${OXYGEN_EMULATOR_WINDOW:-${SPACE_GAME_EMULATOR_WINDOW:-0}}"

if [ "$EMULATOR_WINDOW" = "1" ]; then
    emulator -avd "$AVD_NAME"
else
    emulator -avd "$AVD_NAME" \
        -no-window \
        -no-audio \
        -gpu swiftshader_indirect \
        -no-snapshot
fi
