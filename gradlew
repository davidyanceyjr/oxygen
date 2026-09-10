#!/bin/sh
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -f "$WRAPPER_JAR" ]; then
    if [ -n "${JAVA_HOME:-}" ]; then JAVACMD="$JAVA_HOME/bin/java"; else JAVACMD="java"; fi
    exec "$JAVACMD" ${DEFAULT_JVM_OPTS:-} ${JAVA_OPTS:-} ${GRADLE_OPTS:-} \
        -Dorg.gradle.appname=gradlew \
        -classpath "$WRAPPER_JAR" \
        org.gradle.wrapper.GradleWrapperMain "$@"
fi

# Standalone fallback for this repository. The normal wrapper JAR is preferred;
# this fallback bootstraps the same Gradle version when it is unavailable.
GRADLE_VERSION=9.7.0
CACHE_ROOT="${GRADLE_USER_HOME:-$HOME/.gradle}/oxygen-bootstrap"
DIST_DIR="$CACHE_ROOT/gradle-$GRADLE_VERSION"
ZIP="$CACHE_ROOT/gradle-$GRADLE_VERSION-bin.zip"
URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$DIST_DIR/bin/gradle" ]; then
    mkdir -p "$CACHE_ROOT"
    if [ ! -f "$ZIP" ]; then
        if command -v curl >/dev/null 2>&1; then
            curl -fL "$URL" -o "$ZIP"
        elif command -v wget >/dev/null 2>&1; then
            wget -O "$ZIP" "$URL"
        else
            echo "Oxygen bootstrap requires curl or wget when gradle-wrapper.jar is absent." >&2
            exit 1
        fi
    fi
    command -v unzip >/dev/null 2>&1 || { echo "Oxygen bootstrap requires unzip." >&2; exit 1; }
    rm -rf "$DIST_DIR"
    unzip -q "$ZIP" -d "$CACHE_ROOT"
fi

exec "$DIST_DIR/bin/gradle" "$@"
