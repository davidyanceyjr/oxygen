#!/bin/sh
set -eu
. "$(dirname -- "$0")/android-env.sh"
usage() { echo "Usage: $0 --serial SERIAL Class#method ARTIFACT_DIR" >&2; exit 2; }
serial=
method=
artifact=
while [ "$#" -gt 0 ]; do
    case "$1" in
        --serial) [ "$#" -ge 2 ] || usage; serial=$2; shift ;;
        -*) usage ;;
        *)
            if [ -z "$method" ]; then method=$1
            elif [ -z "$artifact" ]; then artifact=$1
            else usage
            fi
            ;;
    esac
    shift
done
[ -n "$serial" ] && [ -n "$method" ] && [ -n "$artifact" ] || usage
printf '%s\n' "$method" | grep -Eq '^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*)*#[A-Za-z_][A-Za-z0-9_]*$' || { echo "method must be a fully qualified Class#method selector" >&2; exit 2; }
artifact=$(CDPATH= cd -- "$(dirname -- "$artifact")" && pwd -P)/$(basename -- "$artifact")
case "$artifact" in "$PROJECT_ROOT/.codex/test-artifacts"/*) ;; *) echo "artifact directory must be below .codex/test-artifacts" >&2; exit 2 ;; esac
mkdir -p "$artifact"
artifact=$(CDPATH= cd -- "$artifact" && pwd -P)
command -v timeout >/dev/null 2>&1 || { echo "host timeout utility is required" >&2; exit 1; }
recovery_dir="$(dirname -- "$artifact")/emulator"
record="$recovery_dir/serial.txt"
[ -f "$record" ] || { echo "missing recovery serial record; run start-emulator.sh --recover first" >&2; exit 1; }
[ "$serial" = "$(tr -d '\r\n' < "$record")" ] || { echo "serial does not match recovery record" >&2; exit 1; }
devices=$(adb devices | awk '$2 == "device" { print $1 }')
[ "$(printf '%s\n' "$devices" | awk 'NF { n++ } END { print n + 0 }')" -eq 1 ] && [ "$devices" = "$serial" ] || { echo "recovery serial is not the only online ADB device" >&2; adb devices -l >&2; exit 1; }
adb -s "$serial" get-state >"$artifact/preflight.txt" 2>&1
adb -s "$serial" shell pm path android >>"$artifact/preflight.txt" 2>&1
marker="$artifact/result-marker"
: >"$marker"
set +e
ANDROID_SERIAL="$serial" timeout --signal=INT --kill-after=10s 120s "$PROJECT_ROOT/gradlew" :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=$method" --console=plain >"$artifact/gradle.log" 2>&1
status=$?
set -e
echo "$status" >"$artifact/gradle.exit-status"
find "$PROJECT_ROOT/app/build/outputs/androidTest-results" -type f -newer "$marker" -exec cp --parents '{}' "$artifact/" \; 2>/dev/null || true
adb -s "$serial" shell 'date -Is; echo [packages]; pm path com.oxygen.weather; pm path com.oxygen.weather.test; echo [processes]; ps -A -o PID,PPID,STAT,NAME,ARGS | grep -E "oxygen|instrument|test" | grep -v grep || true; echo [window]; dumpsys window windows | grep -m 3 -E "mCurrentFocus|mFocusedApp" || true; echo [anr]; dumpsys dropbox --print data_app_anr | tail -80 || true' >"$artifact/post-run-device.txt" 2>&1 || true
adb -s "$serial" shell logcat -d -t 400 2>/dev/null | tail -400 >"$artifact/post-run-logcat.txt" || true
xml=$(find "$PROJECT_ROOT/app/build/outputs/androidTest-results" -type f -newer "$marker" -name '*.xml' -print | head -1)
result_log=$(find "$PROJECT_ROOT/app/build/outputs/androidTest-results" -type f -newer "$marker" -name 'test-results.log' -print | head -1)
pass=0
if [ -n "$xml" ] && [ -n "$result_log" ]; then
    grep -q 'tests="1"' "$xml" && grep -q 'failures="0"' "$xml" && grep -q 'errors="0"' "$xml" && grep -q 'skipped="0"' "$xml" && grep -q "classname=\"${method%%#*}\"" "$xml" && grep -q "test=${method##*#}" "$result_log" && [ "$status" -eq 0 ] && pass=1
fi
{ echo "method=$method"; echo "gradle_status=$status"; echo "result_xml=${xml:-missing}"; echo "result_log=${result_log:-missing}"; } >"$artifact/outcome.txt"
if [ "$pass" -eq 1 ]; then echo outcome=pass >>"$artifact/outcome.txt"; exit 0; fi
if [ "$status" -eq 124 ] || [ "$status" -eq 130 ] || [ "$status" -eq 137 ]; then echo outcome=timeout-or-interrupted >>"$artifact/outcome.txt"; exit 1; fi
echo outcome=infrastructure-or-assertion-failure >>"$artifact/outcome.txt"
exit 1
