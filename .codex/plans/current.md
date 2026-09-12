# Test Runner Triage — Healthy Emulator and Bounded Instrumentation

**Status:** implemented; connected acceptance blocked at Standard-only boundary
**Cycle ID:** `2026-09-11-test-runner-recovery`
**Prerequisite evidence:** Slice 30B1A4 implementation `d95d268` is blocked at
the connected acceptance boundary; its prior logs are retained under
`.codex/test-artifacts/2026-09-11-slice-30b1a4-rtl-compact-layout-no-refetch/`.
**Mode:** bounded Android test-runner triage and recovery

## Immediate next action

Select a new bounded A4 investigation from the retained Standard-only timeout
diagnostics. The recovery and canary discriminator passed; do not rerun
Standard in this slice or run Simple here. The implemented entry points are
`--recover --artifact-dir <dir>` in `scripts/start-emulator.sh` and
`scripts/run-connected-method.sh` for a
single fully qualified test method, a required `--serial <recovery-serial>`
argument, 120-second Gradle invocation limit, and retained diagnostics. Do not
change app or Android-test Kotlin under this recovery slice.

## Selected behavior and acceptance boundary

The Android-test workflow rejects an ambiguous, stale, or non-responsive
emulator session, can explicitly start one isolated no-snapshot
`oxygen_starter` session, and runs one named instrumentation method with a
practical terminal timeout and retained diagnostics. That session must complete
a known previously passing Compose canary and then produce a terminal result
for the currently blocked 30B1A4 Standard method.

The primary acceptance boundary is the installed `AndroidJUnitRunner` path on
the repo-local API 37 `oxygen_starter` emulator. The Gradle/UTP invocation is
the production workflow; direct `adb shell am instrument` is a diagnostic
cross-check only. A terminal result means a pass or an ordinary asserted test
failure with runner output—not `Tests 0/1 completed`, silent cancellation,
emulator ANR, ADB loss, or an external timeout.

Prior evidence shows the exact 30B1A4 Standard filter hanging both through
Gradle and direct instrumentation. A fresh boot-complete session also
reproduced the stall, and a retained `lastanr` names Pixel Launcher rather than
Oxygen; neither fact establishes root cause. The runner declaration and class
filter were accepted. The canary is therefore a required discriminator: a
canary stall is runner/session evidence, while a passing canary followed by a
Standard-only stall is test/application-path evidence for a separately selected
A4 investigation. Do not change AndroidX Test, AGP, Kotlin, Compose, SDK, or
system-image versions in this slice.

## Contract and limits

- Preserve all application, provider, persistence, presentation, test fixture,
  test assertion, and Android manifest behavior. This is not a Home repair or a
  weakening/rewrite of 30B1A4 tests.
- Preserve the existing no-argument `scripts/start-emulator.sh` foreground
  workflow for a healthy reusable emulator. Its explicit recovery invocation
  must require an artifact directory below `.codex/test-artifacts/`, launch in
  the background with its PID and emulator output retained there, and return
  only after its bounded health preflight succeeds or fails.
- Recovery must refuse an ambiguous device set. It may stop an existing emulator
  only after confirming it is the configured `oxygen_starter` AVD; it must
  refuse multiple eligible emulators or any unverified AVD instead of choosing
  the first `adb devices` row. After launch, it must be the only online ADB
  device and its serial must be written as the sole line of
  `<artifact-dir>/serial.txt` for the method runner. Do not use unqualified
  `adb -e` in recovery or runner paths.
- Recovery starts the configured AVD with snapshots disabled and retains data.
  `--wipe-data` is valid only together with `--recover` and must be an explicit
  opt-in recorded in the artifact ledger. No recovery invocation in this slice
  uses it.
- Define emulator-ready beyond `sys.boot_completed`: assert ADB `device` state,
  boot completion, the `oxygen_starter` AVD/API 37/x86_64 identity,
  package-manager responsiveness, and at least 1 GiB free in `/data`; record
  density, font scale, focused window, `lastanr`, and a short bounded logcat
  excerpt. A historical Launcher ANR or normal Launcher focus is diagnostic
  context, not a health failure by itself.
- Add only the smallest repository-local runner entry point needed to run one
  fully qualified `Class#method` filter through
  `:app:connectedDebugAndroidTest`, apply a finite timeout, and retain command
  output plus post-timeout diagnostics in a caller-specified artifact directory.
  Do not add a general CI framework, orchestration service, managed device,
  dependency, or test-suite abstraction.
- On timeout or failed health preflight, return non-zero and preserve evidence;
  never report success based on process start, APK install, Gradle compilation,
  or an interrupted UTP result.
- Validate the method argument as a fully qualified `Class#method` selector and
  require `--serial <recovery-serial>` plus an explicit artifact-directory
  argument below `.codex/test-artifacts/`. It must verify that the supplied
  serial matches the recovery record and remains the only online ADB device.
  Create the directory before running Gradle, capture stdout/stderr and exit
  status, and write preflight, timeout, and post-run diagnostics there. Use
  POSIX shell syntax and fail with an actionable error if the host `timeout`
  utility required for the bound is unavailable.
- The 120-second bound begins when the filtered Gradle invocation begins and
  includes its incremental build, installation, and instrumentation work.
  Timeout handling must terminate the foreground Gradle process, allow a short
  cleanup grace period, then capture bounded ADB, package, process, window,
  ANR, and logcat diagnostics. It must not silently leave an active test run.
- A normal pass is valid only when the invocation reports exactly one completed
  test with zero skipped and zero failures and a result file produced or updated
  by that run.
  A normal assertion failure is a terminal red result only when runner output
  and a result file identify the same named method. Any missing, stale,
  interrupted, or zero-test result is infrastructure failure even if Gradle
  exits successfully.
- The recovery process is separately authorized to retry only the 30B1A4
  Standard method once after the canary passes. Do not run the Simple method in
  this slice. If Standard produces a normal assertion failure, preserve that red
  result and return to a separately selected Home/A4 correction plan.

## Intended files

- `scripts/start-emulator.sh` — explicit fresh/recovery launch path and
  observable session-health preflight, exact-device selection, and retained
  recovery-process output; retain normal healthy-session reuse.
- `scripts/run-connected-method.sh` — bounded one-method Gradle invocation and
  timeout/result diagnostics against the exact recovery serial.
- `README.md` — document the supported recovery invocation while preserving the
  normal two-terminal run instructions.
- `.codex/test-artifacts/2026-09-11-test-runner-recovery/` — ignored command
  logs, timeout diagnostics, environment records, UTP XML/textproto if
  produced, and the concise verification ledger.
- `.codex/plans/current.md` — active-slice state and actual results.

No app, core, Android-test Kotlin, Gradle version catalog, build file,
manifest, SDK package, AVD definition, or product/provider documentation
authority is planned for change.

## Focused evidence and real-path exercise

**Budget:** one recovery-created ADB-ready `oxygen_starter` session; exactly
two named Gradle-connected methods, one invocation each. Do not repeat direct
`adb shell am instrument`: the retained prior direct run already established
that the selector reaches the runner. Use a 120-second bound for each Gradle
invocation after preflight; stop after the first infrastructure failure or
after a Standard-only stall following a passing canary.

1. Capture the prior A4 blocker reference and recovery preflight values in the
   new verification ledger. Start the isolated session with:

   ```sh
   scripts/start-emulator.sh --recover \
     --artifact-dir .codex/test-artifacts/2026-09-11-test-runner-recovery/emulator
   ```

   Do not wipe AVD data. If device selection is ambiguous or preflight fails,
   preserve the diagnostics and stop without running a test.
2. Run the existing, previously passing Compose canary through the new bounded
   workflow:

   ```sh
   RECOVERY_SERIAL=$(tr -d '\r\n' < .codex/test-artifacts/2026-09-11-test-runner-recovery/emulator/serial.txt)
   scripts/run-connected-method.sh --serial "$RECOVERY_SERIAL" \
     com.oxygen.weather.app.ui.home.HomeDashboardUiTest#rtlStandardHomeDailyPreservesChronologicalRenderedOrder \
     .codex/test-artifacts/2026-09-11-test-runner-recovery/canary
   ```

   It must finish with `1 completed, 0 skipped, 0 failed` and a retained,
   run-current result. If it does not, retain diagnostics and stop: runner or
   session health remains unproven.
3. Only after the canary passes, run the previously stalled 30B1A4 Standard
   method once through the same bounded workflow:

   ```sh
   scripts/run-connected-method.sh --serial "$RECOVERY_SERIAL" \
     com.oxygen.weather.app.ui.home.HomeDashboardUiTest#rtlStandardHomeCompactLongContentKeepsControlsReachableWithoutRefetch \
     .codex/test-artifacts/2026-09-11-test-runner-recovery/30b1a4-standard
   ```

   A pass is runner-recovery evidence and permits resuming A4 in a new active
   plan. A regular assertion failure also proves runner recovery but does not
   authorize a Home fix here. A Standard-only stall after a passing canary is
   test/application-path evidence and requires a new bounded A4 investigation;
   a stall, timeout, ANR, ADB loss, missing result, or UTP interruption before
   the canary passes leaves this plan blocked.
4. If shell changes were needed, run narrow shell checks and `git diff --check`.
   Do not rerun Android compile, unit, or assembly checks unless the runner
   implementation uses Gradle/build inputs that require them; the connected
   methods build the APKs as needed.

## Implementation sequence

1. Record the existing blocker and inspect the current ADB/emulator state into
   the cycle artifact directory without altering app or test sources.
2. Extend `start-emulator.sh` with the explicit recovery interface, verified
   AVD/serial selection, background PID/log retention, no-snapshot launch, and
   bounded preflight. Do not alter no-argument behavior or erase data.
3. Add the one-method runner. It must require and validate the recovery serial,
   set the matching ADB target for the Gradle invocation, enforce the defined
   bound, copy result files updated by the run, preserve UTP/Gradle output, and
   distinguish pass, ordinary assertion failure, timeout, and missing-result
   infrastructure failure.
4. Run `sh -n` on both scripts and `git diff --check`. Exercise invalid-argument
   and ambiguous-device rejection paths without launching Gradle; record their
   exit status and diagnostics. Do not treat these shell checks as acceptance.
5. Use one recovery-created emulator session for the canary and, only if it
   passes, the blocked Standard method. Stop at the classification boundary and
   retain evidence.
6. Review the result at the acceptance boundary. If Standard has a normal
   assertion failure or a Standard-only stall after a passing canary, create a
   new bounded A4 investigation. If it passes, create the A4 closure plan for
   only the remaining Simple method and documentation sync.

## Completion and handoff

Only after both focused methods have terminal results and the canary passes:

- record recovery mode, destructive/non-destructive choice, emulator conditions,
  timeout value, command paths, outcome, and artifact locations;
- if Standard passed, select a new A4 closure plan that runs only the remaining
  Simple method and completes A4 documentation work;
- if Standard failed normally, select a new bounded A4/Home correction plan
  from its retained red evidence;
- if Standard alone still stalls, select a new bounded A4 investigation from
  its retained test-path diagnostics; do not claim emulator recovery fixed it;
- commit the script repair with a descriptive subject and body stating the
  observable behavior, commands actually run, evidence, and limits; then
  complete required post-commit authoritative documentation sync, including
  the README recovery invocation.

## Out of scope

Changing AndroidX Test/UTP, AGP, Gradle, Kotlin, Compose, Android SDK/system
image, emulator hardware profile, managed-device adoption, CI integration,
full-suite retries, app/product fixes, 30B1A4 Simple execution, screenshots,
device-wide RTL, TalkBack, provider/network work, persistence, privacy,
release readiness, and Gate 30B1B1 are out of scope. Existing A4 implementation
evidence remains incomplete and must not be upgraded by this plan alone.
