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
command -v setsid >/dev/null 2>&1 || { echo "setsid utility is required" >&2; exit 1; }
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
result_root="$PROJECT_ROOT/app/build/outputs/androidTest-results"
class_name=${method%%#*}
method_name=${method##*#}
class_simple=${class_name##*.}
package_name=${class_name%.$class_simple}
result_deadline_seconds=120
gradle_grace_seconds=5
gradle_pid=
gradle_status=missing
result_xml=
result_log=
result_timestamp=
cleanup_signal=none
cleanup_outcome=not-needed
gradle_command=${OXYGEN_GRADLE_COMMAND:-$PROJECT_ROOT/gradlew}

find_accepted_result() {
    result_xml=
    result_log=
    result_textproto=
    find "$result_root" -type f -newer "$marker" -name '*.xml' -print >"$artifact/fresh-result-xml-paths.txt" 2>/dev/null || true
    while IFS= read -r candidate; do
        grep -q 'tests="1"' "$candidate" &&
            grep -q 'failures="0"' "$candidate" &&
            grep -q 'errors="0"' "$candidate" &&
            grep -q 'skipped="0"' "$candidate" &&
            [ "$(grep -c '<testcase ' "$candidate")" -eq 1 ] &&
            grep -Eq "<testcase[^>]*name=\"${method_name}\"[^>]*classname=\"${class_name}\"" "$candidate" || continue
        result_xml=$candidate
        break
    done <"$artifact/fresh-result-xml-paths.txt"
    find "$result_root" -type f -newer "$marker" -name 'test-results.log' -print >"$artifact/fresh-result-log-paths.txt" 2>/dev/null || true
    while IFS= read -r candidate; do
        grep -q "INSTRUMENTATION_STATUS: class=$class_name" "$candidate" &&
            grep -q "INSTRUMENTATION_STATUS: test=$method_name" "$candidate" &&
            grep -q 'OK (1 test)' "$candidate" || continue
        result_log=$candidate
        break
    done <"$artifact/fresh-result-log-paths.txt"
    find "$result_root" -type f -newer "$marker" -name 'test-result.textproto' -print >"$artifact/fresh-result-textproto-paths.txt" 2>/dev/null || true
    while IFS= read -r candidate; do
        grep -q 'test_status: PASSED' "$candidate" &&
            grep -q 'scheduled_test_case_count: 1' "$candidate" &&
            grep -q "test_class: \"$class_simple\"" "$candidate" &&
            grep -q "test_package: \"$package_name\"" "$candidate" &&
            grep -q "test_method: \"$method_name\"" "$candidate" || continue
        result_textproto=$candidate
        break
    done <"$artifact/fresh-result-textproto-paths.txt"
    [ -n "$result_xml" ] && [ -n "$result_log" ] && [ -n "$result_textproto" ]
}

capture_artifacts() {
    find "$result_root" -type f -newer "$marker" -exec cp --parents '{}' "$artifact/" \; 2>/dev/null || true
    adb -s "$serial" shell 'date -Is; echo [packages]; pm path com.oxygen.weather; pm path com.oxygen.weather.test; echo [processes]; ps -A -o PID,PPID,STAT,NAME,ARGS | grep -E "oxygen|instrument|test" | grep -v grep || true; echo [window]; dumpsys window windows | grep -m 3 -E "mCurrentFocus|mFocusedApp" || true; echo [anr]; dumpsys dropbox --print data_app_anr | tail -80 || true' >"$artifact/post-run-device.txt" 2>&1 || true
    adb -s "$serial" shell logcat -d -t 400 2>/dev/null | tail -400 >"$artifact/post-run-logcat.txt" || true
}

cleanup_owned_gradle_group() {
    cleanup_signal=TERM
    if kill -TERM -- "-$gradle_pid" 2>/dev/null; then
        cleanup_outcome=term-sent
    else
        cleanup_outcome=term-not-sent
        return
    fi
    cleanup_deadline=$(( $(date +%s) + gradle_grace_seconds ))
    while kill -0 "$gradle_pid" 2>/dev/null && [ "$(date +%s)" -lt "$cleanup_deadline" ]; do sleep 1; done
    if kill -0 "$gradle_pid" 2>/dev/null; then
        cleanup_signal=KILL
        if kill -KILL -- "-$gradle_pid" 2>/dev/null; then cleanup_outcome=kill-sent-after-term-grace; else cleanup_outcome=kill-not-sent-after-term-grace; fi
    else
        cleanup_outcome=terminated-after-term
    fi
}

setsid env ANDROID_SERIAL="$serial" "$gradle_command" :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=$method" --console=plain >"$artifact/gradle.log" 2>&1 &
gradle_pid=$!
started_at=$(date +%s)
accepted=0
while kill -0 "$gradle_pid" 2>/dev/null; do
    if find_accepted_result; then
        accepted=1
        result_timestamp=$(date -Is)
        printf '%s\n' "$result_timestamp" >"$artifact/result-accepted-at.txt"
        grace_deadline=$(( $(date +%s) + gradle_grace_seconds ))
        while kill -0 "$gradle_pid" 2>/dev/null && [ "$(date +%s)" -lt "$grace_deadline" ]; do sleep 1; done
        if kill -0 "$gradle_pid" 2>/dev/null; then cleanup_owned_gradle_group; fi
        break
    fi
    [ $(( $(date +%s) - started_at )) -lt "$result_deadline_seconds" ] || break
    sleep 1
done
if [ "$accepted" -eq 0 ] && find_accepted_result; then
    accepted=1
    result_timestamp=$(date -Is)
    printf '%s\n' "$result_timestamp" >"$artifact/result-accepted-at.txt"
fi
if kill -0 "$gradle_pid" 2>/dev/null && [ "$accepted" -eq 0 ]; then
    cleanup_signal=INT
    if kill -INT -- "-$gradle_pid" 2>/dev/null; then cleanup_outcome=deadline-interrupt-sent; else cleanup_outcome=deadline-interrupt-not-sent; fi
    sleep 1
    if kill -0 "$gradle_pid" 2>/dev/null; then cleanup_owned_gradle_group; fi
fi
set +e
wait "$gradle_pid"
gradle_status=$?
set -e
printf '%s\n' "$gradle_status" >"$artifact/gradle.exit-status"
capture_artifacts
{
    echo "method=$method"
    echo "gradle_status=$gradle_status"
    echo "result_xml=${result_xml:-missing}"
    echo "result_log=${result_log:-missing}"
    echo "result_textproto=${result_textproto:-missing}"
    echo "result_timestamp=${result_timestamp:-missing}"
    echo "cleanup_signal=$cleanup_signal"
    echo "cleanup_outcome=$cleanup_outcome"
} >"$artifact/outcome.txt"
if [ "$accepted" -eq 1 ]; then
    if [ "$cleanup_signal" = none ]; then echo outcome=pass >>"$artifact/outcome.txt"; else echo outcome=pass-after-runner-cleanup >>"$artifact/outcome.txt"; fi
    exit 0
fi
if [ "$cleanup_outcome" = deadline-interrupt-sent ] || [ "$cleanup_outcome" = deadline-interrupt-not-sent ]; then echo outcome=timeout-or-interrupted >>"$artifact/outcome.txt"; else echo outcome=infrastructure-or-assertion-failure >>"$artifact/outcome.txt"; fi
exit 1
