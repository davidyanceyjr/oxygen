#!/bin/sh

if [ -n "${BASH_SOURCE:-}" ]; then
    SCRIPT_PATH=${BASH_SOURCE}
else
    SCRIPT_PATH=$0
fi

PROJECT_ROOT=$(CDPATH= cd -- "$(dirname -- "$SCRIPT_PATH")/.." && pwd -P)

export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-26-openjdk}"
export ANDROID_HOME="$PROJECT_ROOT/.android-sdk"
export ANDROID_SDK_ROOT="$PROJECT_ROOT/.android-sdk"
export ANDROID_USER_HOME="$PROJECT_ROOT/.android"
export ANDROID_AVD_HOME="$ANDROID_USER_HOME/avd"

if [ -z "${XDG_RUNTIME_DIR:-}" ] || [ ! -w "$XDG_RUNTIME_DIR" ]; then
    export XDG_RUNTIME_DIR="$PROJECT_ROOT/.android-runtime"
else
    export XDG_RUNTIME_DIR
fi

export GRADLE_USER_HOME="$PROJECT_ROOT/.gradle"
case " ${GRADLE_OPTS:-} " in
    *" --enable-native-access=ALL-UNNAMED "*) ;;
    *) export GRADLE_OPTS="${GRADLE_OPTS:+$GRADLE_OPTS }--enable-native-access=ALL-UNNAMED" ;;
esac
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
