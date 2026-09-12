#!/bin/sh
set -eu

. "$(dirname -- "$0")/android-env.sh"
mkdir -p "$XDG_RUNTIME_DIR"
chmod 700 "$XDG_RUNTIME_DIR"
AVD_NAME="${OXYGEN_AVD_NAME:-oxygen_starter}"
EMULATOR_WINDOW="${OXYGEN_EMULATOR_WINDOW:-${SPACE_GAME_EMULATOR_WINDOW:-0}}"
EMULATOR_NETWORK="${OXYGEN_EMULATOR_NETWORK:-1}"

usage() { echo "Usage: $0 [--recover --artifact-dir DIR [--wipe-data]]" >&2; exit 2; }
RECOVER=0
WIPE_DATA=0
ARTIFACT_DIR=
while [ "$#" -gt 0 ]; do
    case "$1" in
        --recover) RECOVER=1 ;;
        --artifact-dir) [ "$#" -ge 2 ] || usage; ARTIFACT_DIR=$2; shift ;;
        --wipe-data) WIPE_DATA=1 ;;
        *) usage ;;
    esac
    shift
done
[ "$RECOVER" -eq 1 ] || { [ "$WIPE_DATA" -eq 0 ] && [ -z "$ARTIFACT_DIR" ] || usage; }
[ "$RECOVER" -eq 0 ] || {
    [ -n "$ARTIFACT_DIR" ] || usage
    ARTIFACT_DIR=$(CDPATH= cd -- "$(dirname -- "$ARTIFACT_DIR")" && pwd -P)/$(basename -- "$ARTIFACT_DIR")
    case "$ARTIFACT_DIR" in "$PROJECT_ROOT/.codex/test-artifacts"/*) ;; *) echo "--artifact-dir must be below .codex/test-artifacts" >&2; exit 2 ;; esac
    mkdir -p "$ARTIFACT_DIR"
    ARTIFACT_DIR=$(CDPATH= cd -- "$ARTIFACT_DIR" && pwd -P)
    command -v timeout >/dev/null 2>&1 || { echo "host timeout utility is required" >&2; exit 1; }
    log() { printf '%s\n' "$*" | tee -a "$ARTIFACT_DIR/recovery.log" >&2; }
    : >"$ARTIFACT_DIR/recovery.log"
    log "recovery_started=$(date -Is) avd=$AVD_NAME wipe_data=$WIPE_DATA"
    adb start-server >/dev/null
    rows=$(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }')
    count=$(printf '%s\n' "$rows" | awk 'NF { n++ } END { print n + 0 }')
    if [ "$count" -gt 0 ]; then
        [ "$count" -eq 1 ] || { log "ERROR: multiple online ADB devices"; adb devices -l >>"$ARTIFACT_DIR/recovery.log"; exit 1; }
        old_serial=$(printf '%s\n' "$rows" | sed -n '1p')
        case "$old_serial" in emulator-*) old_avd=$(adb -s "$old_serial" emu avd name 2>/dev/null | tr -d '\r' | sed -n '1p') ;; *) log "ERROR: unverified non-emulator device $old_serial"; exit 1 ;; esac
        [ "$old_avd" = "$AVD_NAME" ] || { log "ERROR: device $old_serial is AVD '$old_avd', expected '$AVD_NAME'"; exit 1; }
        adb -s "$old_serial" emu kill >/dev/null 2>&1 || true
        for _ in $(seq 1 30); do adb devices | awk -v s="$old_serial" '$1 == s { found=1 } END { exit found ? 0 : 1 }' || break; sleep 1; done
    fi
    EMULATOR_LOG="$ARTIFACT_DIR/emulator.log"
    if [ "$WIPE_DATA" -eq 1 ]; then
        nohup setsid emulator -avd "$AVD_NAME" -no-snapshot -wipe-data -no-window -no-audio -gpu swiftshader_indirect >"$EMULATOR_LOG" 2>&1 </dev/null &
    else
        nohup setsid emulator -avd "$AVD_NAME" -no-snapshot -no-window -no-audio -gpu swiftshader_indirect >"$EMULATOR_LOG" 2>&1 </dev/null &
    fi
    echo "$!" >"$ARTIFACT_DIR/emulator.pid"
    serial=
    for _ in $(seq 1 180); do
        candidates=$(adb devices | awk '$1 ~ /^emulator-[0-9]+$/ && $2 == "device" { print $1 }')
        count=$(printf '%s\n' "$candidates" | awk 'NF { n++ } END { print n + 0 }')
        if [ "$count" -eq 1 ]; then
            candidate=$(printf '%s\n' "$candidates" | sed -n '1p')
            [ "$(adb -s "$candidate" emu avd name 2>/dev/null | tr -d '\r' | sed -n '1p' || true)" = "$AVD_NAME" ] && { serial=$candidate; break; }
        elif [ "$count" -gt 1 ]; then log "ERROR: multiple eligible emulator devices during recovery"; exit 1; fi
        sleep 1
    done
    [ -n "$serial" ] || { log "ERROR: recovery emulator did not become eligible"; exit 1; }
    ready=0
    for _ in $(seq 1 120); do
        state=$(adb -s "$serial" get-state 2>/dev/null | tr -d '\r' || true)
        boot=$(adb -s "$serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)
        if [ "$state" = device ] && [ "$boot" = 1 ]; then ready=1; break; fi
        sleep 2
    done
    [ "$ready" -eq 1 ] || { log "ERROR: ADB/boot preflight timed out"; exit 1; }
    avd=$(adb -s "$serial" emu avd name | tr -d '\r' | sed -n '1p')
    sdk=$(adb -s "$serial" shell getprop ro.build.version.sdk | tr -d '\r')
    abi=$(adb -s "$serial" shell getprop ro.product.cpu.abi | tr -d '\r')
    pm_ok=0; timeout 10 adb -s "$serial" shell pm path android >"$ARTIFACT_DIR/pm-path.txt" 2>&1 && pm_ok=1 || true
    free_bytes=$(adb -s "$serial" shell df -k /data 2>/dev/null | awk 'NR > 1 { print $4 * 1024; exit }' | tr -d '\r')
    density=$(adb -s "$serial" shell wm density 2>/dev/null | tr -d '\r')
    font_scale=$(adb -s "$serial" shell settings get system font_scale 2>/dev/null | tr -d '\r')
    focused=$(adb -s "$serial" shell dumpsys window windows 2>/dev/null | grep -m 1 -E 'mCurrentFocus|mFocusedApp' || true)
    adb -s "$serial" shell dumpsys dropbox --print data_app_anr 2>/dev/null | tail -80 >"$ARTIFACT_DIR/lastanr.txt" || true
    adb -s "$serial" shell logcat -d -t 300 2>/dev/null | tail -300 >"$ARTIFACT_DIR/logcat.txt" || true
    { echo "serial=$serial"; echo "avd=$avd"; echo "sdk=$sdk"; echo "abi=$abi"; echo "adb_state=$state"; echo "boot_completed=$boot"; echo "package_manager_responsive=$pm_ok"; echo "free_data_bytes=$free_bytes"; echo "density=$density"; echo "font_scale=$font_scale"; echo "focused_window=$focused"; } | tee "$ARTIFACT_DIR/preflight.txt" >>"$ARTIFACT_DIR/recovery.log"
    [ "$avd" = "$AVD_NAME" ] && [ "$sdk" = 37 ] && [ "$abi" = x86_64 ] && [ "$pm_ok" -eq 1 ] && [ "${free_bytes:-0}" -ge 1073741824 ] || { log "ERROR: emulator health preflight failed"; exit 1; }
    printf '%s\n' "$serial" >"$ARTIFACT_DIR/serial.txt"
    log "recovery_ready=$(date -Is)"
    exit 0
}


# Keep the old variable as a temporary compatibility fallback for existing
# local scripts; OXYGEN_EMULATOR_WINDOW is the supported name.
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
