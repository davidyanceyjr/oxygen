# Cycle History

## Reading Contract

Normal discovery reads this live file's summary and at most its most recent
three entries. Older detailed history is retained under
`.codex/cycles/archive/`, including
`history-through-2026-09-12-before-30b1a4-a4-stall-handoff.md`.

When adding a new entry, append a concise self-contained section with status,
result, evidence, artifacts, limits, and commit state. Archive the current live
file before replacing or compressing it.

## Recent State Summary

- Gate 30D3 is committed after the Appearance semantics and layout slices.
- Gate 30E's visible emulator startup was recovered with `DISPLAY=:0`;
  installed TalkBack was confirmed, but host audio prevented the speech audit.
- User decision on 2026-09-14: Gate 30E and its remaining audio prerequisite
  are deferred, optional, and non-blocking for the first release. TalkBack
  service traversal remains unverified. Slice 33A is the next candidate to plan.
- Earlier summary and entries are preserved in
  `archive/2026-09-14-before-talkback-first-release-deferral.md`.

## Recent Cycles

### 2026-09-11-slice-30b1a4-rtl-compact-layout-no-refetch

Status: implemented; connected acceptance incomplete
Mode: bounded Home RTL compact-layout and request-count test implementation
Slice: Slice 30B1A4, RTL Compact Layout and No-Refetch Evidence
Commit: `d95d268`

Result:

- Added the planned Compose-local RTL Standard and Simple cases for compact
  bounds, touch targets, overlap, long-content reachability, and stable
  repository request lists. No production correction was needed.

Evidence and limits:

- Compile, app/core unit tests, debug assembly, and `git diff --check` passed.
- Standard connected acceptance stalled with no result; Simple was not run.
  Artifacts: `.codex/test-artifacts/2026-09-11-slice-30b1a4-rtl-compact-layout-no-refetch/`.

### 2026-09-12-test-runner-recovery

Status: committed; Standard acceptance remains blocked
Mode: bounded Android test-runner triage and recovery
Slice: Test Runner Triage — Healthy Emulator and Bounded Instrumentation
Commit: `1972972`

Result:

- Added an exact-device recovery session and bounded one-method connected-test
  runner. The recovery canary completed with 1 passed test; Standard still
  timed out with `Tests 0/1 completed` and no fresh XML.

Artifacts: `.codex/test-artifacts/2026-09-11-test-runner-recovery/`

### 2026-09-12-slice-30b1a4-a4-standard-rtl-stall

Status: blocked; uncommitted diagnostic handoff
Mode: bounded Android/Compose test-path diagnosis
Slice: Slice 30B1A4-A4, Standard RTL Compact Stall Investigation

Result:

- Added non-production Android-log checkpoints to the Standard case. The
  initial bounded run reached Daily after Now and Hourly; the post-hypothesis
  run isolated the first non-returning operation to the Daily spoken-weather
  node's `performScrollTo()` after the Daily title rendered and the page was
  idle.
- Both runs timed out at the runner's 120-second bound with `Tests 0/1
  completed` and no fresh XML. No app ANR was recorded. A disabled-motion
  harness hypothesis was reverted because it did not change the outcome.

Evidence:

- The retained preflight, both runner outputs, Android checkpoints, diagnostics,
  cleanup records, and ledger are under
  `.codex/test-artifacts/2026-09-12-slice-30b1a4-a4-standard-rtl-stall/`.
- `git diff --check` passed. Compile, unit, and assembly checks were not run
  after this terminal connected-test blocker.

Limits and next action:

- No Simple method, installed RTL journey, provider/persistence work, or
  production Home change was performed. Select a new bounded investigation for
  the RTL Daily `performScrollTo()` test interaction before retrying Standard.

### 2026-09-12-slice-30b1a4-a5-rtl-daily-scroll

Status: implemented; connected acceptance failed normally; uncommitted
Mode: bounded Android/Compose test-harness correction
Slice: Slice 30B1A4-A5, RTL Daily Scroll Interaction Draft

Result:

- Replaced the blocked Daily child `performScrollTo()` with one direct
  `swipeUp()` on the visible `home-page-daily` scroll container. No production
  Home change was made.
- The bounded runner reached the named test and returned a normal assertion
  failure: the exact spoken Daily weather node was not displayed after the
  gesture. This converts the prior stall into a reproducible red rendered
  boundary, but does not satisfy the slice acceptance.

Evidence and limits:

- Artifacts are under
  `.codex/test-artifacts/2026-09-12-slice-30b1a4-a5-rtl-daily-scroll/`.
- Final result was `Tests 1/1 completed, 0 skipped, 1 failed`; no fresh passing
  result exists. `git diff --check` passed.
- Compile, unit tests, assembly, Simple method, installed RTL journey, and
  production correction were not run or performed after the red result.

Commit state: uncommitted diagnostic/test interaction change; next action is a
separately selected bounded A4/Home correction investigation from this red
Daily visibility evidence.

### 2026-09-12-slice-30b1a4-rtl-compact-layout-completion

Status: implemented; connected acceptance failed normally; uncommitted
Mode: bounded Android-test correction and RTL compact-layout verification
Slice: Slice 30B1A4, RTL Compact Layout and No-Refetch Evidence Completion

Result:

- Corrected the Standard Daily contract to verify entry 0 before one bounded
  container swipe and entry 4's exact speech, bounds, and spacing afterward;
  removed all temporary checkpoint logging.
- The Daily boundary passed, but the later Details helper failed because
  `home-section-status` was absent. The fresh XML records 1 completed, 0
  skipped, and 1 failed test. No production defect is established.

Evidence and limits:

- Artifacts: `.codex/test-artifacts/2026-09-12-slice-30b1a4-rtl-compact-layout-completion/`.
- The wrapper reached its 120-second bound after instrumentation wrote the
  ordinary failure. Per the plan, Simple, broad checks, retries, commit, and
  document-sync completion were not performed. A4 remains unverified.

### 2026-09-12-slice-30b1a4-a6-details-status-fixture-alignment

Status: committed; Standard boundary verified; Simple pending
Mode: bounded Android-test fixture correction and one-method connected verification
Slice: Slice 30B1A4-A6, Standard Details Status Fixture Alignment
Commit: `8b5647b`

Result:

- Retained the Daily container-scroll correction and made the Standard RTL
  fixture explicitly stale, aligning its Details status-card expectation with
  the production freshness contract. No production code changed.
- The sole Standard connected method completed with 1 test, 0 skipped, and 0
  failed on the recovered `oxygen_starter` emulator; the recovery emulator was
  stopped afterward.

Evidence and limits:

- `git diff --check`, `:app:compileDebugKotlin`, app/core debug unit tests,
  and `:app:assembleDebug` passed. Artifacts are under
  `.codex/test-artifacts/2026-09-12-slice-30b1a4-a6-details-status-fixture-alignment/`.
- The Simple RTL method, installed RTL journey, TalkBack, and Gate 30B1B1 were
  not run or completed. The next selected slice is the one-method Simple A4
  completion boundary.

### 2026-09-12-slice-30b1a4-a7-simple-rtl-compact-completion

Status: verified; tracking handoff pending Gate 30B1B1 documentation sync
Mode: bounded one-method Android connected verification
Slice: Slice 30B1A4-A7, Simple RTL Compact Completion

Result:

- `HomeDashboardUiTest#rtlSimpleHomeCompactLayoutAndForecastChoicesDoNotRefetch`
  completed once on the recovered `oxygen_starter` emulator with 1 completed,
  0 skipped, and 0 failed. The runner's Gradle status was 0, and the recovery
  emulator was stopped afterward.
- No production or test source changed. Together with Standard evidence at
  `8b5647b`, this verifies both focused 30B1A4 compact/no-refetch boundaries.

Evidence and limits:

- Recovery preflight, runner result, copied Android test result, and ledger
  are under
  `.codex/test-artifacts/2026-09-12-slice-30b1a4-a7-simple-rtl-compact-completion/`.
  An initial recovery invocation stopped before creating an emulator because
  the new cycle's parent artifact directory was absent; after that directory
  was created, the one planned emulator session and one method ran.
- `git diff --check` passed. A6's compile, app/core unit-test, and debug
  assembly checks are reused because source and execution environment were
  unchanged. No Standard rerun, full class, installed RTL journey, screenshots,
  hierarchy capture, TalkBack, or Gate 30B1B1 work was run.

### 2026-09-12-gate-30b1b1-rtl-installed-evidence-doc-sync

Status: committed
Mode: bounded installed RTL evidence and documentation-sync gate
Slice: Gate 30B1B1, RTL Installed Evidence and Documentation Sync

Result:

- Reused the focused 30B1A4 evidence: Standard at `8b5647b` and the retained
  Simple one-method result with 1 completed, 0 skipped, and 0 failed. No
  connected-test, compile, unit-test, or assembly rerun was needed because no
  source or relevant execution-environment change occurred.
- Installed the existing debug APK on `oxygen_starter` (API 37), used its real
  manual Open-Meteo selected-location path for Chicago, and then exercised the
  installed Standard Home surface while the activity configuration was
  `ldrtl`. Now, Hourly, Daily, and Details retained their titles/positions and
  semantic order; the visible selector was `Details`, `Daily`, `Hourly`,
  `Now` from left to right.
- Restored the original `en-US` device locale, absent global RTL setting, false
  RTL property, and `ldltr` configuration. Android required one no-data-wipe
  restart to apply the restored direction; the emulator was then stopped.

Evidence and limits:

- Screenshots and matching UI hierarchies for all four Standard pages, install
  and launch output, direction records, emulator logs, and the verification
  ledger are under
  `.codex/test-artifacts/2026-09-12-gate-30b1b1-rtl-installed-evidence-doc-sync/`.
- The initial direct RTL-setting attempt retained `ldltr`; its launch/location
  captures are diagnostic only and are excluded from the RTL acceptance claim.
  `ar-SA` plus the Android RTL flag produced the verified `ldrtl` Home state.
- `git diff --check` passed. TalkBack traversal, Simple installed RTL,
  font-scale-2.0, reduced motion, theme/contrast invariance, alerts,
  localization, and release checks remain outside this gate.

Commit state: committed by this documentation-sync entry; no production or test
source changed.

### 2026-09-12-slice-30b2-reduced-motion-appearance-invariance

Status: planned; focused matrix incomplete; uncommitted
Mode: bounded reduced-motion and theme/contrast connected verification
Slice: Slice 30B2, Home Reduced-Motion and Appearance Invariance

Result:

- Added the two planned Paper/High and Terminal/High injected-disabled-motion
  contracts and strengthened the Android-policy case with saved-choice,
  preference-write, and repository-request assertions.
- Cases 1, 2, 3, and 5 each passed with 1 completed, 0 skipped, and 0 failed.
- Cases 4 and 6 each failed initially and on one reserved rerun at the alert
  assertion. Diagnosis identified test-fixture setup: the production-path
  fixture lacked an explicit severe alert plus `AlertLookupStatus.Available`;
  no production defect was established. The source fixture is corrected but
  has not been rerun.

Evidence and limits:

- Android-test compilation and `git diff --check` passed before the matrix.
  All per-method runner outputs, XML/log copies, device diagnostics, and the
  verification ledger are under
  `.codex/test-artifacts/2026-09-12-slice-30b2-reduced-motion-appearance-invariance/`.
- The recovery wrapper hung after ADB boot during optional diagnostics; the
  healthy `emulator-5554` session remained in use and its serial was recorded.
- The connected budget is exhausted for this cycle. Broad compile/unit/
  assembly checks, installed real-path exercise, commit, and post-commit
  authority sync were not run.

Commit state: uncommitted test contracts and fixture correction; next action is
a fresh bounded rerun of cases 4 and 6.

### 2026-09-12-slice-30b2-fixture-correction-retry

Status: planned; focused matrix incomplete; uncommitted
Mode: bounded two-method connected verification after fixture correction
Slice: Slice 30B2, Home Reduced-Motion and Appearance Invariance

Result:

- Created fresh evidence under
  `.codex/test-artifacts/2026-09-12-slice-30b2-reduced-motion-appearance-invariance-fixture-correction/`.
- Recovered one API-37 `oxygen_starter` emulator. Manual validation recorded
  only `emulator-5554` online with ADB state `device` and SDK 37.
- Case 4, `paperHighContrastDisabledMotionPreservesHomeMeaning`, passed once:
  1 completed, 0 skipped, 0 failed; runner Gradle status 0.
- Case 6, `terminalHighContrastDisabledMotionPreservesHomeMeaning`, received
  one bounded attempt. The runner ended with status 137 at its 120-second
  timeout after the log reached `Tests 0/1 completed`; its Gradle log later
  contained `BUILD SUCCESSFUL`, but no accepted completed-test result exists.
- The emulator was stopped after the timeout. No rerun or production change
  was made.

Evidence and limits:

- Case 6 remains red/timeout for acceptance purposes. Installed production Home
  exercise, exact settings restoration exercise, broad checks, review, commit,
  and post-commit authority sync were not performed.
- The active plan records the timeout and selects a future case-6 investigation
  before any retry. The connected budget for this bounded attempt is closed.

### 2026-09-12-slice-30b2-runner-finalization-repair

Status: implemented; connected reruns prohibited

Result:

- Corrected `run-connected-method.sh` to require fresh matching XML, test log,
  and AGP textproto terminal evidence, then accept the result before allowing a
  five-second Gradle grace period and cleaning up only its own process group.
  Accepted results return `pass-after-runner-cleanup` when cleanup was needed.
- Retained case 4 and case 6 evidence remains authoritative: both methods passed
  at the Android instrumentation boundary. Case 6's prior 137 was a false
  wrapper timeout after completion, not a product or fixture failure.

Evidence and limits:

- Fresh ledger: `.codex/test-artifacts/2026-09-12-slice-30b2-runner-finalization-repair/`.
- `sh -n scripts/run-connected-method.sh` and `git diff --check` passed.
- No connected rerun, installed Home journey, broad checks, or commit was done;
  those remain the next Slice 30B2 action.

### 2026-09-12-gate-30b3-home-environment-evidence-doc-sync

Status: committed
Mode: bounded Home evidence/documentation sync
Slice: Gate 30B3, Home Environment Evidence and Documentation Sync
Commits: Slice 30B2 `728f4c2`; this documentation sync

Result:

- Reconciled retained RTL compact/install evidence with six retained 30B2
  focused Android results. The 30B2 matrix covers every implemented
  theme/contrast pair at 360x640 dp and font scale 1.3; every selected method
  has one completed test, zero skipped, and zero failed instrumentation output.
  Case 6's prior 137 was a false wrapper timeout after matching passing XML,
  test log, and textproto evidence; the repaired runner's isolated fixture
  accepted the same terminal-evidence shape and returned
  `pass-after-runner-cleanup`.
- Installed the debug APK once on API-37 `oxygen_starter`, chose Chicago through
  the production manual Open-Meteo path, set all Android animation scales to
  zero, and force-stopped/relaunched. Appearance was effectively Off while the
  saved Subtle choice remained selected; Standard Now, Hourly, Daily, and
  Details retained page identity, weather, source/update/provenance, and their
  visible navigation. Original scales and saved appearance were restored before
  stopping the emulator.

Evidence and limits:

- Artifacts are under
  `.codex/test-artifacts/2026-09-12-slice-30b2-reduced-motion-appearance-invariance/`,
  `.codex/test-artifacts/2026-09-12-slice-30b2-runner-finalization-repair/`,
  and the retained Gate 30B1B1 directory.
- `sh -n scripts/run-connected-method.sh`, the isolated runner fixture,
  Android-test compilation, `:app:compileDebugKotlin`, app/core debug unit
  tests, `:app:assembleDebug`, and `git diff --check` passed. No connected case
  was rerun after runner repair.
- TalkBack service traversal, Simple installed RTL, localization, live-alert
  success, alert accessibility, and release checks remain outside this gate.

### 2026-09-12-slice-30c1-official-alert-summary-accessibility

Status: committed
Mode: bounded official-alert summary accessibility acceptance coverage
Slice: Slice 30C1, Official-Alert Summary Accessibility
Commit: `4ffc507`

Result:

- Added four named connected acceptance cases in
  `HomeDashboardUiTest`: required summary fields plus meaningful action
  semantics and 48dp targets, truthful `NoAlerts` rendering, Home-to-detail-
  to-Home request/state retention, and long-text RTL/high-contrast overflow.
  No production correction was needed; the existing mapper, state-holder, and
  Home route satisfied the demonstrated boundary.
- The first summary case attempt failed only because the test asserted a
  below-viewport severity line without scrolling to it. The one permitted
  focused test correction scrolled each required field/action; the rerun passed.

Evidence:

- Focused unit command passed:
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests 'com.oxygen.weather.app.HomeForecastPresentationMapperTest' --tests 'com.oxygen.weather.app.HomeForecastStateHolderTest'`.
- `sh -n scripts/run-connected-method.sh` and
  `:app:compileDebugAndroidTestKotlin` passed. The four connected methods each
  completed with 1 test, 0 skipped, and 0 failed on the sole `emulator-5554`
  API-37 `oxygen_starter` session; the summary method passed on its reserved
  rerun after the test-only correction.
- Broad checks passed:
  `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :core:testDebugUnitTest`,
  `. scripts/android-env.sh && ./gradlew :app:assembleDebug`, and
  `git diff --check`.
- Evidence is retained under
  `.codex/test-artifacts/2026-09-12-slice-30c1-official-alert-summary-accessibility/`,
  including connected result/log/XML artifacts, the baseline method result,
  installed screenshots/UI hierarchies, and the verification ledger.

Installed result and limits:

- The debug APK was installed once after focused green. Manual Chicago,
  Illinois selection completed through the production Open-Meteo path. The
  live result was a truthful no-alert Home with weather/source/update content;
  no alert data was seeded or fabricated, so live-alert summary/detail-entry
  evidence is unavailable.
- Alert-detail reading layout, TalkBack service traversal, localization,
  background alert behavior, and release readiness remain outside this slice.

Commit state: committed; the post-commit README, specification section 53,
roadmap, active plan, and this history entry are synchronized to the evidence.

### 2026-09-13-slice-30c2-official-alert-detail-accessibility

Status: committed
Mode: bounded official-alert detail accessibility acceptance coverage
Slice: Slice 30C2, Official-Alert Detail Accessibility
Commit: `70304b8`

Result:

- Strengthened the existing rendered detail fixture with logical route order,
  selector/source/Back target-size checks, and preserved verbatim text/action
  behavior. Added one `OxygenApp` RTL/font-scale-2.0, High-contrast, Effects-Off
  long-content case covering event, severity, issuer, area, description,
  instructions, source action, selection, return, and no-refetch.
- No production code or provider behavior changed.

Evidence:

- Focused mapper/state-holder unit methods passed; Android-test compilation
  passed. The three named connected methods each passed with 1 completed,
  0 skipped, and 0 failed on one API-37 `oxygen_starter` session. An initial
  ambiguous-text/fixture/helper test failure was corrected in test code and
  rerun after the relevant source changes.
- Broad `:app:compileDebugKotlin`, app/core debug unit tests, `:app:assembleDebug`,
  and `git diff --check` passed. Artifacts are under
  `.codex/test-artifacts/2026-09-13-slice-30c2-official-alert-detail-accessibility/`.

Installed result and limits:

- Manual Chicago search and selection completed through the production
  Open-Meteo path after one debug APK install. The resulting Home had weather
  data but no active-alert summary/detail action; no alert data was seeded.
- Alert transport/cache/background work, notifications, TalkBack, localization,
  and release checks remain outside this slice.

Commit state: committed; the post-commit README, specification, roadmap, and
active-plan synchronization is pending in the following documentation commit.

### 2026-09-13-gate-30c3-alert-accessibility-evidence-doc-sync

Status: committed
Mode: bounded alert accessibility evidence reconciliation and documentation sync
Slice: Gate 30C3, Alert Accessibility Evidence and Documentation Sync

Result:

- Audited Slice 30C1 at `4ffc507` and Slice 30C2 at `70304b8` against their
  retained result ledgers and production alert path. The seven accepted named
  deterministic connected cases are:
  `officialAlertSummaryExposesRequiredFieldsAndActionSemanticsAtCompactFont`,
  `noAlertStatusDoesNotRenderSummaryCardCountOrActions`,
  `oxygenAppAlertDetailRoundTripPreservesHomeAndDoesNotRefresh`,
  `officialAlertSummaryLongTextRemainsScrollableInRtlHighContrast`,
  `officialAlertDetailFlowSelectsSecondAlertPreservesVerbatimTextAndReturnsHome`,
  `highContrastAlertDetailKeepsHazardMeaningAndSelectionNonColorCues`, and
  `officialAlertDetailLongContentRemainsReachableInRtlLargeFont`.
- Retained result artifacts show one completed, zero skipped, zero failed for
  each accepted method. Superseded failed/ambiguous attempts remain recorded
  but were not counted as acceptance evidence.

Evidence and limits:

- Summary artifacts: `.codex/test-artifacts/2026-09-12-slice-30c1-official-alert-summary-accessibility/`.
  Detail artifacts: `.codex/test-artifacts/2026-09-13-slice-30c2-official-alert-detail-accessibility/`.
  Together they cover the exercised deterministic Compose/Android boundary:
  alert meaning, non-color semantics, validated actions, 48dp targets,
  selection and Back state, no-refetch behavior, long-content reachability,
  LTR/RTL, large-font, Effects Off, and High contrast.
- Each retained installed manual Chicago attempt used the production selected
  location path and ended in a truthful no-alert Home. No live-alert summary or
  detail-entry journey is claimed.
- TalkBack service traversal, localization, alert persistence/cache, background
  polling, notifications, additional alert regions, and release readiness
  remain unverified.

Checks:

- Read-only commit/source/artifact inspection and production-path review passed.
- `git diff --check` passed, and `git status --short` confirmed only the five
  intended Markdown files plus the protected archive. Android compilation,
  unit tests, assembly, emulator startup, connected tests, and installed
  retries were intentionally skipped because this gate changed Markdown only
  and the retained evidence/environment were unchanged.

Commit state: committed by this documentation-sync entry; the unrelated
untracked archive `.codex/cycles/archive/2026-09-12-before-slice-30b2-runner-finalization-repair.md`
was preserved unchanged. Slice 30D1 remains the next specified candidate.

### 2026-09-13-slice-30d1-appearance-control-semantics

Status: committed at `78ecb84`
Mode: bounded Settings / Appearance presentation and accessibility semantics
Slice: Slice 30D1, Appearance Control Semantics

Result:

- `SettingsScreen` now exposes readable headings for Theme, Contrast, Layout,
  and Effects; visible labels and RadioButton roles for all supported choices;
  selected/disabled/action semantics; confirmed selection during pending writes;
  named Effects status nodes; and named 48dp Retry/Back controls. Effects no
  longer hides its action behind a wrapper semantics node.
- Added the real-`OxygenApp` cross-group semantics case and aligned the four
  existing preference connected cases to the named D1 boundary. Existing
  storage/state models, callbacks, preference independence, and forecast
  request behavior were preserved.

Evidence:

- Retained under `.codex/test-artifacts/2026-09-13-slice-30d1-appearance-control-semantics/`:
  pre-change semantics dump, five accepted connected results, the Layout
  viewport-repair attempts, and installed screenshots/UI hierarchies.
- `appearanceGroupsAndChoicesExposeMeaningfulSemantics`,
  `themePreferenceSemanticsRetainConfirmedChoiceThroughPendingAndRetry`,
  `layoutPreferenceSemanticsRetainConfirmedChoiceThroughPendingAndRetry`,
  `effectsPreferenceSemanticsRetainConfirmedChoiceThroughPendingAndRetry`,
  and `contrastPreferenceSemanticsRetainConfirmedChoiceThroughPendingAndRetry`
  each completed with one test, zero skipped, and zero failed on API-37
  `oxygen_starter` / `emulator-5554`. The Layout method passed after its
  test-only scroll-to-status correction.
- Focused preference JVM tests, Android-test compilation, broad app/core
  compile and unit tests, debug assembly, and `git diff --check` passed.
  The installed journey selected Chicago through Open-Meteo without location
  permission, saved Paper, and returned to Home with the same source/update/
  provenance presentation. Numeric request-count proof remains fixture-backed.

Limits:

- TalkBack service traversal, large-font/RTL resilience, localization,
  storage-failure behavior on the installed path, and release checks remain
  unverified or out of scope. Slice 30D2 is next.

Commit state: committed; post-commit README, specification, roadmap, active
plan, and this history entry are synchronized to the retained evidence. The
untracked archive `.codex/cycles/archive/2026-09-12-before-slice-30b2-runner-finalization-repair.md`
was preserved unchanged.

### 2026-09-13-slice-30d2-appearance-layout-environment-resilience

Status: committed at `2955aa5`; post-commit documentation sync completed.

Result:

- `SettingsScreen` now gives the existing Appearance scroll content bottom
  separation from the fixed Back action, and exposes a stable disabled-motion
  status node. No preference, state, provider, navigation, or weather behavior
  changed.
- `AppearanceSemanticsUiTest` adds the six bounded cases
  `appearanceCompactControlsRemainScrollReachable`,
  `appearanceFontScale13ControlsRemainReadableAndReachable`,
  `appearanceFontScale20ControlsRemainReadableAndReachable`,
  `appearanceRtlPreservesLogicalLabelControlOrder`,
  `appearanceReducedMotionOffPreservesEffectsMeaning`, and
  `appearanceThemeContrastPairsPreserveMeaning`.

Evidence:

- All six named cases passed once on API-37 `oxygen_starter` /
  `emulator-5554`, with zero skipped or failed. Focused state-holder JVM tests,
  app/core unit tests, debug compilation, debug assembly,
  `scripts/install-debug.sh`, and `git diff --check` passed.
- Installed evidence is retained under
  `.codex/test-artifacts/2026-09-13-slice-30d2-appearance-layout-environment-resilience/`
  for compact LTR, font scale 2.0, RTL, disabled-animation Effects Off, and a
  Terminal/High pair. The temporary compact display override and environment
  settings were restored; the first emulator recovery attempt is recorded in
  the verification ledger because its artifact parent did not yet exist.

Limits:

- Installed evidence does not prove numeric request counts or injected storage
  failures; those remain fixture-backed. TalkBack service traversal,
  localization, automatic contrast, and release checks remain unverified or
  out of scope. The unrelated untracked archive
  `.codex/cycles/archive/2026-09-12-before-slice-30b2-runner-finalization-repair.md`
  was preserved unchanged.

### 2026-09-14-gate-30d3-appearance-accessibility-evidence-doc-sync

Status: committed
Mode: bounded Appearance accessibility evidence reconciliation and documentation sync
Slice: Gate 30D3, Appearance Accessibility Evidence and Documentation Sync

Result:

- Audited Slice 30D1 at `78ecb84` and Slice 30D2 at `2955aa5` against their
  retained artifacts, verification ledgers, and changed-file boundaries.
- Confirmed five accepted D1 connected cases and six accepted D2 connected
  cases on API-37 `oxygen_starter`, plus installed LTR, font-scale-2.0, RTL,
  disabled-animation Effects Off, and Terminal/High evidence.
- Synchronized README, specification, roadmap, active plan, and this history
  entry. The next specified candidate is Gate 30E.

Evidence and limits:

- D1 artifacts: `.codex/test-artifacts/2026-09-13-slice-30d1-appearance-control-semantics/`.
  D2 artifacts: `.codex/test-artifacts/2026-09-13-slice-30d2-appearance-layout-environment-resilience/`.
- TalkBack service traversal, localization, automatic contrast, injected
  storage failures, numeric installed request counts, and release checks remain
  unverified. Android checks and emulator work were intentionally skipped
  because this gate changed Markdown only and relied on unchanged evidence.

Checks: `git diff --check` and `git status --short` passed before commit.

Commit state: committed; the unrelated untracked archive
`.codex/cycles/archive/2026-09-12-before-slice-30b2-runner-finalization-repair.md`
was preserved unchanged.


### 2026-09-14-gate-30e-emulator-blocker-recovery

Status: diagnostic handoff complete, uncommitted; Gate 30E planned and unverified.

- Resolved the visible startup failure with explicit `DISPLAY=:0` after a
  successful `xdpyinfo` probe. The host abstract X11 socket is reachable despite
  the absent filesystem X0 socket. No project code or launcher repair needed.
- One visible `oxygen_starter` session booted on `emulator-5554`, API 37/x86_64;
  bounded ADB/boot/package-manager checks passed. Runtime discovery confirmed
  installed TalkBack 17.0.0.889642762 and Google TTS.
- Remaining blocker: PulseAudio connection refused and emulator audio-driver
  initialization failure in this execution session. No TalkBack enablement,
  speech, installed traversal, or gate-closure claim. The report supplies a
  manual resumption procedure in the logged-in desktop's audio environment.
- Read-only before/after settings comparison passed; emulator stopped cleanly
  and ADB lists no devices. No Kotlin, tests, dependencies, scripts, APK install,
  or automated reruns; unchanged checks cannot establish audible speech.
- Report and exact logs:
  `.codex/test-artifacts/2026-09-14-gate-30e-emulator-blocker-recovery/blocker-report.log`.
  The command budget/results are in that directory's `verification-ledger.md`.
  Original failed-attempt artifacts and unrelated untracked archive preserved.
- Active plan updated with the narrower audio blocker. No commit or successful
  gate authority sync; broader accessibility claims remain unchanged.


### 2026-09-14-gate-30e-first-release-deferral

Status: specified — Gate 30E deferred; documentation decision uncommitted.

- User explicitly made the remaining emulator/TalkBack audit non-blocking and
  not imperative for the first version. The host audio prerequisite and manual
  service traversal are optional follow-up; no successful audit is claimed.
- Updated specification section 37 and current status, README, roadmap, active
  plan, and recent history. Slice 33A is the next specified candidate to plan.
- Existing accessibility obligations and all other release checks remain in
  effect. Original diagnostic logs/manual handoff are retained under
  `.codex/test-artifacts/2026-09-14-gate-30e-emulator-blocker-recovery/`.
- Documentation-only review: no production/test changes, Android checks, or
  emulator session. The prior live ledger was archived before summary edits;
  unrelated user changes and the earlier untracked archive were preserved.


### 2026-09-14-slice-33a-dependency-manifest-privacy-audit

Status: implemented; installed acceptance blocked; uncommitted diagnostic handoff
Mode: bounded Android dependency and manifest privacy audit
Slice: Slice 33A, Dependency and Manifest Privacy Audit

Result:

- Added `android:usesCleartextTraffic="false"` to the production application
  manifest and added four installed-package privacy/component checks.
- The cleartext check passed on API 37. Three checks failed against actual
  merged/package behavior: generated
  `com.oxygen.weather.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, exported
  `androidx.activity.ComponentActivity`, and exported
  `androidx.profileinstaller.ProfileInstallReceiver` protected by `DUMP`.
- The findings are dependency/manifest repair work and remain in scope for a
  separately named follow-up; no finding was silently treated as documentation.

Evidence and limits:

- Artifacts: `.codex/test-artifacts/2026-09-14-slice-33a-dependency-manifest-privacy-audit/`.
- Dependency resolution, merged manifest processing, debug assembly/install,
  app/core unit tests, and `git diff --check` passed. The one planned API-37
  emulator session was stopped cleanly.
- Connected result: 4 tests completed, 1 passed, 3 failed. `:app:lintDebug`
  failed on the pre-existing API-30 `LocationManager.getCurrentLocation` min
  SDK finding plus 13 warnings. No repair slice, backup-policy change, or
  release claim was made.

Changed files: `app/src/main/AndroidManifest.xml` and
`app/src/androidTest/kotlin/com/oxygen/weather/PrivacyManifestInstrumentedTest.kt`.

Commit state: uncommitted; current plan records the repair handoff. Existing
uncommitted documentation and archive changes were preserved.

### 2026-09-14-slice-33b-dependency-manifest-exposure-repair

Status: committed; installed acceptance verified
Mode: bounded Android dependency-owned manifest exposure repair
Slice: Slice 33B, Dependency-Owned Manifest Exposure Repair
Commit: `02f668f`

Result:

- Removed AndroidX Core's generated dynamic-receiver permission and Compose
  `ui-test-manifest`'s exported `androidx.activity.ComponentActivity` from the
  production merge with narrow manifest-merger directives.
- Retained ProfileInstaller startup while forcing
  `androidx.profileinstaller.ProfileInstallReceiver` non-exported.
- Restored strict installed assertions for the exact three requested
  permissions and launcher-only exported activity policy.

Evidence:

- Artifacts are retained under
  `.codex/test-artifacts/2026-09-14-slice-33b-dependency-manifest-exposure-repair/`.
- Manifest ownership/dependency processing and Android-test compilation passed.
- API-37 installed privacy instrumentation passed 4/4 cases. Selected-cache
  and manual-location production instrumentation each passed 1/1 case.
- App/core unit tests, debug assembly, and `git diff --check` passed. Fresh APK
  installation succeeded, and package output confirmed exact permissions and
  component exposure.

Limits:

- A direct headless Home launch reached the emulator's system “Process system
  isn't responding” dialog before app UI inspection; it was recorded once and
  not repeated. This slice makes no visual-launch claim.
- Backup policy, TalkBack, provider disclosure, release readiness, and the
  pre-existing API-30 location lint finding remain unverified/out of scope.

Commit state: committed; post-commit plan, roadmap, README, specification, and
history synchronization completed.

### 2026-09-14-slice-33b-provider-disclosure-local-privacy-audit

Status: verified; ready for commit
Mode: bounded provider disclosure and local data privacy audit
Slice: Slice 33B, Provider Disclosure and Local Data Privacy Audit

Result:

- Existing production wiring and provider contracts agree with the active
  disclosures for Open-Meteo forecast/timezone, Open-Meteo/GeoNames geocoding,
  MET Norway fallback forecast, and foreground NOAA/NWS alerts. No provider or
  disclosure-content change was needed.
- The installed Settings journey reached Data Sources, Privacy, and Open
  Source Licenses, verified required text and links, opened all five configured
  disclosure URLs through the injected URI boundary, preserved forecast
  request count, and made no permission request: 1 completed, 0 skipped,
  0 failed.
- Repaired a regression from the previous manifest slice by scoping Compose's
  test helper `ComponentActivity` to the debug app manifest as non-exported.
  It is absent from the release APK; `MainActivity` remains the only exported
  activity.

Evidence:

- Focused `AboutDisclosureStateHolderTest` passed.
- Connected `HomeDashboardUiTest#settingsDisclosuresShowActiveProviderLicenseAndPrivacyBaseline`
  passed once on API-37 `oxygen_starter`.
- `:app:compileDebugKotlin`, app/core debug unit tests, `:app:assembleDebug`,
  `:app:assembleRelease`, and `git diff --check` passed.
- Artifacts: `.codex/test-artifacts/2026-09-14-slice-33b-provider-disclosure-local-privacy-audit/`.

Limits:

- No live provider call or release-candidate verification was added. TalkBack
  service traversal, localization, new providers, alert persistence,
  conditional requests, backup-policy changes, and deferred Gate 30E work
  remain unverified/out of scope.

Commit state: committed in `da3a9a3`; post-commit plan, roadmap, and history
synchronization completed.

### 2026-09-14-gate-34a-settings-about-release-check

Status: verified; ready for commit
Mode: bounded installed Settings/About release check
Slice: Gate 34A — Settings and About Release Check

Result:

- Existing production Settings/About behavior matched the contract. No
  production, provider, persistence, permission, manifest, README, or
  specification change was needed.
- Added connected boundary coverage for the nine supported Appearance choices
  and absence of the unfinished Full effects choice. The fixture now supplies
  in-memory supported preference stores so the managed persisted-choice surface
  is exercised.
- Settings root/location-back and disclosure/provider-license/privacy journeys
  passed 1/1 each on API-37 `oxygen_starter`, retaining forecast semantics,
  no-refetch behavior, disclosure links, and no permission request.

Evidence:

- Focused `AboutDisclosureStateHolderTest` passed.
- Broad compile, app/core debug unit tests, debug/release assembly, and
  `git diff --check` passed.
- Artifacts: `.codex/test-artifacts/2026-09-14-gate-34a-settings-about-release-check/`;
  accepted connected results are under `settings-root-accepted/` and
  `settings-disclosures/`, with emulator evidence under `emulator/`.
- Initial connected failures were limited to test-fixture/query issues and
  remain retained under the corresponding `settings-root*` artifact folders.

Limits: no live provider call, release-candidate verification, TalkBack service
traversal, localization, automatic contrast audit, or deferred Gate 30E work.

Commit state: uncommitted; verified changes are ready for commit.
