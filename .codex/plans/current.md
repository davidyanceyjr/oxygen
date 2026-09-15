# Gate 35B — Connected Evidence Repair

**Status:** verified; ready for commit
**Cycle ID:** `2026-09-14-gate-35b-connected-evidence-repair`
**Prerequisite:** Gate 35A's core acceptance repair is committed at `f84e7d4`;
Gate 35C remains specified and blocked on this repair.

## Selected behavior and acceptance boundary

Repair the five unusable Gate 35B connected-test records without changing
production behavior, test behavior, or the accepted three-case evidence.
The original eight-case Gate 35B set has runner-backed `1/1 completed`, zero
skipped, zero failed records only for cases 2, 7, and 8. Source and runner
inputs are unchanged between `19347d9`, `f84e7d4`, and the selected baseline,
so those three records remain the retained evidence and must not be rerun.

The acceptance boundary is one freshly recovered API-37 x86_64
`oxygen_starter` session. Each of these exact existing methods must produce a
fresh matching JUnit XML, `test-results.log`, and `test-result.textproto`, all
showing exactly one selected test, zero skipped, and zero failures/errors:

1. `HomeDashboardUiTest#standardCompactHomeAtFontScale13KeepsLongContentAndAllPagesReachable`
2. `HomeDashboardUiTest#rtlStandardHomeCompactLongContentKeepsControlsReachableWithoutRefetch`
3. `HomeDashboardUiTest#officialAlertDetailLongContentRemainsReachableInRtlLargeFont`
4. `HomeDashboardUiTest#paperHighContrastDisabledMotionPreservesHomeMeaning`
5. `AppearanceSemanticsUiTest#appearanceCompactControlsRemainScrollReachable`

Together with the retained cases 2, 7, and 8, this restores the eight focused
Gate 35B records. It does not add an installed/manual journey, rerun broad
checks, make a release decision, or widen any previously verified claim.

## Contract and implementation scope

The selected observable contracts already live in:

- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/settings/AppearanceSemanticsUiTest.kt`
- `scripts/run-connected-method.sh`
- `scripts/start-emulator.sh`

The five methods respectively exercise: compact 360x640/font-scale-1.3
Standard Home reachability and semantics; compact RTL Standard Home control
order/no-refetch behavior; long official-alert detail at RTL/font-scale-2.0
with Effects Off and High contrast; Paper/High Home meaning with Android
motion disabled; and compact Appearance error/status/retry/control
reachability. They use deterministic production-path fixtures, not
`SampleWeather.bundle` or a live provider result.

No production, test-source, runner, fixture, manifest, provider, persistence,
permission, navigation, presentation, or accessibility change is planned.
Do not weaken assertions, alter fixture semantics, or change code to obtain a
passing record. If a selected method exposes an assertion failure, timeout,
zero-test result, or invalid evidence triplet, retain the result and stop this
cycle. Select a separate, bounded product or harness repair only after the
failure is diagnosed; do not use a rerun in this cycle as a repair attempt.

## Result

All five selected methods were accepted on one recovered API-37 x86_64
`oxygen_starter` session (`emulator-5554`). Each produced a fresh matching XML,
`test-results.log`, and `test-result.textproto` with exactly one selected test,
zero skipped, zero failures, and zero errors; each runner exited 0 without
cleanup. The emulator was stopped and confirmed offline. Evidence and the
verification ledger are under
`.codex/test-artifacts/2026-09-14-gate-35b-connected-evidence-repair/`.

The retained cases 2, 7, and 8 plus these five repaired cases restore the
eight focused Gate 35B records. No production or test source changed. No
compile, unit, assembly, additional connected, installed/manual, or release
checks were run, as required by this evidence-repair slice.

## Evidence plan

Create `.codex/test-artifacts/2026-09-14-gate-35b-connected-evidence-repair/`
and a concise `verification-ledger.md` before execution. Record the baseline
HEAD, dirty-worktree summary, command, serial, runner exit status, `outcome`,
fresh artifact paths, completed/skipped/failed counts, and stop/cleanup result
for every invocation. Test artifacts remain untracked.

1. Before starting the session, confirm the only intended worktree change is
   this plan, run `git diff --check`, and run `sh -n` on the two emulator/test
   scripts. These are execution preconditions, not behavior verification.
2. Run `scripts/start-emulator.sh --recover` once with the cycle's
   `emulator/` artifact directory. The exact-method runner requires the
   recovery-generated `serial.txt`; therefore do not substitute an existing
   ADB session. Accept the session only when its recorded preflight confirms
   the sole online `oxygen_starter`, API 37, x86_64, responsive package
   manager, and required free data space.
3. Run the five methods above in the listed order with
   `scripts/run-connected-method.sh --serial "$serial"`, placing each result
   in `01-standard-compact-font13/` through
   `05-appearance-compact-controls/`. Do not begin a later method until the
   previous case's evidence is accepted.
4. For each case, the runner is the acceptance authority: it creates a fresh
   marker before execution and requires a matching one-test/zero-failure XML,
   instrumentation log, and AGP textproto before its 120-second deadline.
   Inspect and record `outcome.txt`, `gradle.exit-status`,
   `fresh-result-*-paths.txt`, copied raw results, device post-run capture,
   and `gradle.log`. An `outcome=pass` or
   `outcome=pass-after-runner-cleanup` with all three matching fresh paths is
   accepted; Gradle's raw exit status is recorded but never substitutes for
   the three result records.
5. On the first non-accepted case, retain its complete directory, write the
   exact stop reason to the ledger, stop the recovery emulator with
   `adb -s "$serial" emu kill`, confirm it is offline, and end the cycle.
   Do not execute remaining cases, mutate source, install an APK, or retry.
6. After five accepted cases, stop that same emulator and confirm no device
   remains. Run `git diff --check` once. Do not run compile, unit, assemble,
   full connected classes, the three retained methods, or installed/manual
   journeys: this slice changes no build input and the five exact tests are
   its complete connected-test budget.

Representative invocation (repeat only with the next named method and
directory after acceptance):

```sh
cycle_dir=.codex/test-artifacts/2026-09-14-gate-35b-connected-evidence-repair
scripts/start-emulator.sh --recover --artifact-dir "$cycle_dir/emulator"
serial=$(tr -d '\r\n' < "$cycle_dir/emulator/serial.txt")
scripts/run-connected-method.sh --serial "$serial" \
  com.oxygen.weather.app.ui.home.HomeDashboardUiTest#standardCompactHomeAtFontScale13KeepsLongContentAndAllPagesReachable \
  "$cycle_dir/01-standard-compact-font13"
```

## Required documentation and commit closure

Only after all five records are accepted, make these factual tracking updates:

- Append one self-contained Gate 35B evidence-repair entry to
  `.codex/cycles/history.md`, with the five exact results, retained cases,
  artifact path, preconditions/broad check actually run, limits, and commit
  state. This is append-only; do not rewrite or archive history for it.
- Update `.codex/plans/mvp-roadmap.md` to mark Gate 35B verified (then
  committed after commit), cite both the original and repair artifact roots,
  and leave Gate 35C as the next specified decision rather than claiming a
  release.
- Update `README.md` and `docs/OXYGEN_FULL_SPECIFICATION.md` to replace only
  the now-stale five-record repair warning with the precise repaired evidence.
  Preserve the early-app/non-release wording and all existing limits:
  TalkBack service traversal, localization, automatic contrast, live
  alert-detail entry, operational stale/error reproduction in this session,
  alert persistence/background behavior, and release readiness remain
  unverified.
- Update this plan to the evidence actually obtained. After the descriptive
  documentation/evidence commit, perform the required post-commit authority
  reconciliation and make Gate 35C the next candidate only if every document
  still matches the commit. Do not create or imply a Gate 35C result here.

If any selected record is not accepted, do not change README, specification,
roadmap, or historical Gate 35B status to claim success. Update only the
active plan and append history with the factual blocker if the cycle is ready
for handoff.

## Out of scope

- Product, test, harness, fixture, provider, persistence, manifest, or
  preference changes.
- Additional connected cases, test-suite sweeps, compilation, unit tests,
  assembly, APK installation, screenshots, or manual app exercise.
- TalkBack service traversal, localization, automatic contrast, live-alert
  entry, alert persistence/background behavior, and release readiness.
- Gate 35C or any release-candidate/MVP-complete decision.
