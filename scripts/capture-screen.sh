#!/bin/sh
set -e

. "$(dirname -- "$0")/android-env.sh"

OUTPUT_PATH=${1:-oxygen-screen.png}
OUTPUT_DIR=$(dirname -- "$OUTPUT_PATH")

if [ "$OUTPUT_DIR" != "." ]; then
    mkdir -p "$OUTPUT_DIR"
fi

adb wait-for-device

if ! adb exec-out screencap -p > "$OUTPUT_PATH"; then
    rm -f "$OUTPUT_PATH"
    echo "Failed to capture screen to $OUTPUT_PATH" >&2
    exit 1
fi

echo "$OUTPUT_PATH"
