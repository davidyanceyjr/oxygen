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

Status: committed at `64d4908`
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

Commit state: committed at `64d4908`; post-commit documentation
synchronization follows in the documentation-sync commit.

### 2026-09-14-gates-34b-35a-data-source-mvp-core-verification

Status: verified; Gate 34B and Gate 35A passed in one shared verification
session.

- Gate 34B installed Data Sources evidence passed 1/1. Active provider,
  attribution, privacy, and license claims matched production wiring; all five
  configured disclosure links opened through the injected URI boundary, with
  no forecast refetch and no permission request.
- Gate 35A connected evidence passed 9/9 on API-37 `oxygen_starter`: manual
  location, three MET Norway/Open-Meteo fallback and provenance cases, Room
  offline/stale restoration, units, saved-location selection/removal, and the
  truthful no-alert Home boundary. The ninth case was required by the gate's
  explicit official-alert acceptance clause and is the documented exception to
  the eight-case default.
- Focused app/core debug unit tests passed. Compile, debug/release assembly,
  and `git diff --check` passed. No production or authority-content correction
  was needed.
- Artifacts: `.codex/test-artifacts/2026-09-14-gates-34b-35a-data-source-mvp-core-verification/`.
  The wrapper's initial nested-artifact invocation was rejected before test
  execution; the corrected direct sibling artifact layout passed and is the
  retained evidence layout.

Limits: no live-provider/manual network journey, release-candidate decision,
TalkBack traversal, localization, automatic contrast audit, alert persistence,
background polling, or deferred Gate 30E work. README and specification claims
remain unchanged and accurate. Commit state: documentation/status updates are
uncommitted; no product files changed.

### 2026-09-14-gate-35b-mvp-presentation-accessibility-verification

Status: committed at `19347d9`
Mode: bounded installed presentation and accessibility evidence gate
Slice: Gate 35B, MVP Presentation and Accessibility Verification

Result:

- All eight planned focused connected cases passed on API-37
  `oxygen_starter`, each with 1 completed, 0 skipped, and 0 failed. The cases
  covered Standard compact/large-font Home reachability, RTL Home controls,
  deterministic alert-detail long content, Paper/High disabled-motion Home
  meaning, and Appearance compact/font-scale-2.0/RTL semantics.
- One installed session fetched real Chicago weather through manual selection
  and retained readable Standard Now, Hourly, Daily, and Details pages. It
  reached saved locations with current marking, Settings, Appearance, Units,
  Data Sources, Privacy, and About; selected Paper/High and restored
  Oxygen/Standard/Subtle; and captured compact, font-scale 1.3/2.0, RTL, and
  Effects Off with Android animation scales disabled.
- The installed Chicago alert lookup returned no active alert. This verifies
  truthful no-alert behavior only; it does not claim live alert-detail entry.

Evidence:

- Artifacts and verification ledger:
  `.codex/test-artifacts/2026-09-14-gate-35b-mvp-presentation-accessibility-verification/`.
- Broad checks passed: app/core debug unit tests, debug Kotlin compilation,
  debug/release assembly, and `git diff --check`.
- No production or test source changed. Active plan, roadmap, README,
  specification, and this history entry were synchronized to the verified
  evidence.

Limits: TalkBack service traversal, localization, automatic contrast, live
alert-detail entry, operational stale/error reproduction in this session,
alert persistence/background behavior, and release readiness remain
unverified. Gate 35C remains the next specified release-candidate decision.
Commit state: committed at `19347d9`; post-commit authority synchronization is
complete.

### 2026-09-14-gate-35a-connected-evidence-repair

Status: committed at `f84e7d4`
Mode: bounded runner-evidence repair with no product or test-source change
Slice: Gate 35A, MVP Core Behavior Verification

Result:

- Replaced the four unusable original records, whose Gradle logs reported
  `Tests 0/1 completed`, with one API-37 `oxygen_starter` session of exact
  filtered methods. Manual location, no-alert, offline Room-cache restoration,
  and units/Home reachability each completed `1/1`, with zero skipped and zero
  failed; the Gradle exit status was 0 for every invocation.
- The five other Gate 35A runner-backed records remain retained. Together, the
  nine selected core boundaries are verified. No production, test, manifest,
  provider, persistence, or workflow behavior changed.

Evidence and limits:

- Raw Gradle logs, fresh JUnit XML, `test-results.log`, `test-result.textproto`,
  device preflight, outcome files, and ledger are under
  `.codex/test-artifacts/2026-09-14-gate-35a-connected-evidence-repair/`.
- The emulator was stopped after the four-case session. Unit tests, Kotlin
  compilation, assembly, and manual/installed journeys were deliberately not
  rerun because this repair changed no build inputs; their prior retained
  evidence is not presented as fresh verification.
- Gate 35B remains planned: five original focused records have zero or missing
  completion output. Gate 35C and release readiness remain blocked on that
  separate repair.

Commit state: committed at `f84e7d4`; post-commit authority synchronization is
complete.

### 2026-09-14-gate-35b-connected-evidence-repair

Status: committed at `6ab7977`
Mode: bounded runner-evidence repair with no product or test-source change
Slice: Gate 35B, MVP Presentation and Accessibility Verification

Result:

- One fresh recovered API-37 x86_64 `oxygen_starter` session accepted all five
  previously unusable methods in order: `standardCompactHomeAtFontScale13KeepsLongContentAndAllPagesReachable`,
  `rtlStandardHomeCompactLongContentKeepsControlsReachableWithoutRefetch`,
  `officialAlertDetailLongContentRemainsReachableInRtlLargeFont`,
  `paperHighContrastDisabledMotionPreservesHomeMeaning`, and
  `appearanceCompactControlsRemainScrollReachable`.
- Every method produced a fresh matching JUnit XML, `test-results.log`, and
  `test-result.textproto`: exactly 1 selected/completed, 0 skipped, 0 failed,
  and 0 errors. Every runner exited 0 without cleanup.
- The retained original cases 2, 7, and 8 plus these five repaired records
  restore all eight focused Gate 35B records. No production or test source
  changed.

Evidence and limits:

- Preflight, raw Gradle logs, fresh result paths and copies, device captures,
  outcomes, cleanup, and the verification ledger are under
  `.codex/test-artifacts/2026-09-14-gate-35b-connected-evidence-repair/`.
- Preconditions passed: `git diff --check`, `sh -n scripts/start-emulator.sh`,
  and `sh -n scripts/run-connected-method.sh`. The recovered emulator was
  stopped after case 5 and confirmed offline. No compile, unit, assembly,
  additional connected, installed/manual, screenshot, TalkBack, or release
  checks were run because this slice changed no build input and its five exact
  methods were the complete connected-test budget.
- Gate 35C remains specified and is not a release decision in this cycle.
  TalkBack service traversal, localization, automatic contrast, live
  alert-detail entry, operational stale/error reproduction in this session,
  alert persistence/background behavior, and release readiness remain
  unverified.

Commit state: committed at `6ab7977`; post-commit authority synchronization is
complete. Gate 35C is the next specified candidate; no release decision was
made.

### 2026-09-14-gate-35c-release-candidate-decision

Status: implemented; Gate 35C not verified/release-ready
Mode: bounded release-facing provider disclosure correction and release audit
Commit: `001b7d5`

Result:

- Removed internal release-candidate milestone wording from the installed Data
  Sources disclosure while retaining conditional GET/304 reuse and provider
  health/backoff as unfinished, and retained the actual roadmap-only limits.
- Added the planned JVM and connected assertions. The red JVM contract failed
  for the expected missing old-production wording; the corrected JVM class and
  the one planned connected disclosure case then passed. The connected result
  triplet records exactly 1 selected/completed, 0 skipped, 0 failures, and 0
  errors.
- Debug compilation, app/core unit tests, debug/release assembly, source
  audits, release APK/manifest inspection, and `git diff --check` passed. The
  release APK is correctly unsigned and was not installed.

Evidence and limits:

- Artifacts: `.codex/test-artifacts/2026-09-14-gate-35c-release-candidate-decision/`.
  Retained Gate 34B/35A/35B evidence was referenced and not rerun.
- The single installed journey installed the debug APK and cleared only app
  data, but Android showed a persistent “Process system isn’t responding”
  dialog immediately after launch. One “Wait” action did not recover the
  surface, so first-run entry, manual live provider weather, and installed
  disclosure/Privacy capture remain unavailable. The emulator was stopped and
  confirmed offline; no retry or second install was made.
- No hosted CI run exists for candidate SHA `a33a5a3` (or the post-change
  commit); the observed success run `34858709570` is for older SHA `a46cef9`.
  No push or PR was authorized or attempted. TalkBack, localization, signing,
  publication, and deferred features remain unverified/out of scope.

Commit state: committed at `001b7d5`; active plan, roadmap, and this history
entry were synchronized after commit. README, specification, provider, and
privacy documents were not changed because Gate 35C did not pass its installed
journey and qualifying-CI conditions.

### 2026-09-15-gate-35c-authorized-ci-attempt

Status: blocked before repository checks by hosted Android SDK setup
Mode: authorized candidate CI attempt; emulator-platform follow-up kept separate
Candidate: `fb4ab2319ce870ffec242b6b205187f12f3007cf`

Result:

- Protected `main` rejected the direct candidate push because changes must
  arrive through a pull request and the `Android checks` status is required.
  Candidate branch `candidate/gate-35c-ci-2026-09-14` and PR `#17` were then
  created for the same candidate inputs.
- Actions run `34917846117` started and failed in
  `android-actions/setup-android@v3` before Gradle. The action attempted to
  install the requested `tools` package and reported `Failed to find package
  'tools'`; compile, unit-test, assemble, and whitespace steps were skipped.

Evidence and limits:

- Run: `https://github.com/davidyanceyjr/oxygen/actions/runs/34917846117`.
- This is not qualifying hosted CI evidence and does not change the local
  product verification state. The same setup failure was not retried.
- The CI workflow repair/revalidation and the Android emulator “Process
  system isn’t responding” investigation remain separate follow-ups. No
  emulator was started or retried in this CI attempt.

### 2026-09-15-gate-35c-ci-workflow-repair

Status: committed; hosted revalidation passed
Mode: bounded CI configuration repair
Commit: `86db325`

Result:

- Updated `.github/workflows/android-ci.yml` from
  `android-actions/setup-android@v3` to `@v4` and set `packages:
  platform-tools`. The obsolete `tools` SDK package is no longer requested;
  no product, test, or emulator behavior changed.
- `git diff --check` passed. `actionlint` was not installed locally and was
  skipped. Hosted CI revalidation passed subsequently as recorded below.

Limits:

- The emulator-platform “Process system isn’t responding” investigation
  remains a separate follow-up and was not started or retried.

### 2026-09-15-gate-35c-ci-workflow-revalidation

Status: verified hosted CI; Gate 35C remains blocked by emulator platform
Mode: qualifying candidate CI revalidation
Candidate: `826faed89556b9d4cb0d2ec61762a70a2d2c26d5`

Result:

- Actions run `34918387599` passed in 1m29s after the workflow repair. SDK
  setup, Gradle setup, debug Kotlin compilation, app/core debug unit tests,
  debug APK assembly, and whitespace checks all completed successfully.
- The prior setup failure was resolved without changing product behavior,
  provider behavior, persistence, permissions, or emulator state.

Evidence and limits:

- Run: `https://github.com/davidyanceyjr/oxygen/actions/runs/34918387599`.
- This satisfies the hosted-CI condition for the updated candidate inputs, but
  Gate 35C is not release-ready because the clean-state installed journey is
  still blocked by Android’s “Process system isn’t responding” dialog.
- The emulator-platform follow-up remains separate and was not started in this
  cycle.

### 2026-09-15-gate-35c-emulator-platform-follow-up

Status: verified locally; final release-candidate decision pending
Mode: bounded recovered emulator and installed production journey
Slice: Gate 35C-P1, Recovered Emulator Clean-State Journey

Result:

- Created the missing repo-local artifact parent after the launcher rejected
  its first pre-execution invocation; no emulator or app state changed in that
  invocation.
- One wiped API-37 x86_64 `oxygen_starter` session reached `recovery_ready`
  with ADB/device and boot complete, a responsive package manager, and 9.47 GB
  free `/data`. The debug APK built and installed once.
- On clean first run, coarse location remained `granted=false` and no system
  ANR dialog or hidden default location appeared. Manual Chicago search returned
  the real Open-Meteo result `41.8500, -87.6501 | America/Chicago`.
- Selecting Chicago reached live Home Now weather, then Hourly and Daily pages,
  with source/update/provenance text. Data Sources displayed the corrected
  active-provider disclosure, and the journey returned to Home with Chicago
  weather retained.
- Final diagnostics showed no app `data_app_anr` record. The emulator was
  stopped once and `adb devices` confirmed no online device.

Evidence and verification:

- Screenshots, UI hierarchies, package/permission state, logcat, ANR/dropbox
  captures, emulator recovery records, and the command ledger are under
  `.codex/test-artifacts/2026-09-14-gate-35c-emulator-platform-follow-up/`.
- The build-input comparison from qualifying candidate
  `826faed89556b9d4cb0d2ec61762a70a2d2c26d5` to `HEAD` was empty. `git diff
  --check` passed. Existing local compile/unit/assembly and hosted CI evidence
  remain applicable because no product or build input changed.

Limits and next action:

- Live alert-detail entry, operational stale/error reproduction in this
  session, TalkBack service traversal, localization, automatic contrast, alert
  persistence/background behavior, signing, publication, and deferred
  provider/cache work remain unverified. No release or signed artifact was
  made.
- The next bounded action is the Gate 35C final release-candidate decision and
  documentation sync; it must preserve these limits and must not claim
  `released` without separate release evidence.

Commit state: uncommitted documentation/status synchronization; no product
source changed.

### 2026-09-15-gate-35c-release-candidate-decision-sync

Status: verified locally; release-candidate status not granted
Mode: documentation-only release-candidate evidence decision and status sync
Slice: Gate 35C, Release Candidate Decision and Documentation Sync

Result:

- Reviewed the retained Gate 34B, Gate 35A, Gate 35B, Gate 35C-P1, and
  qualifying hosted-CI evidence against the Gate 35C acceptance boundary.
- The clean-state installed journey passed in one wiped API-37
  `oxygen_starter` session: manual Chicago selection without a location grant,
  live Open-Meteo current/hourly/daily weather, source/update/provenance,
  corrected Data Sources disclosure, and return to Home. Hosted CI run
  `34918387599` passed for candidate `826faed89556b9d4cb0d2ec61762a70a2d2c26d5`.
- Decision: the Gate 35C evidence boundary is verified locally. The repository
  is not declared a release candidate, release-ready, MVP-complete, signed, or
  published.
- Reconciled `.codex/plans/current.md`, `.codex/plans/mvp-roadmap.md`,
  `README.md`, `docs/OXYGEN_FULL_SPECIFICATION.md`, and this history entry to
  preserve the decision and named evidence.

Evidence and limits:

- Installed evidence, screenshots, hierarchies, diagnostics, and the command
  ledger are under
  `.codex/test-artifacts/2026-09-14-gate-35c-emulator-platform-follow-up/`.
- No automated, hosted-CI, or installed-journey rerun was needed: this slice
  changed only documentation, and the retained build-input comparison is
  empty. `git diff --check` passed after the reconciliation.
- Live alert-detail entry, operational stale/error reproduction in this
  session, TalkBack service traversal, localization, automatic contrast,
  alert persistence/background behavior, conditional GET/304 handling,
  provider health/backoff, signing, and publication remain unverified or
  unimplemented as recorded by the authorities.

Commit state: uncommitted documentation-only synchronization; no product
source changed and no commit was requested.

### 2026-09-15-gate-30e-host-audio-preflight-blocker

Status: blocked before emulator startup; Gate 30E remains unverified
Mode: bounded installed TalkBack gate preflight
Slice: Gate 30E, Installed TalkBack and Accessibility Closure

Result:

- The default `xdpyinfo` check failed because `DISPLAY` was unset, while the
  required explicit `DISPLAY=:0` check passed and opened the logged-in X.Org
  display.
- `timeout 5 pactl info` failed with no `/run/user/1000/pulse` directory and
  `Connection refused`. The plan's host-audio prerequisite therefore did not
  pass, so emulator startup and the TalkBack journey were stopped.
- ADB was checked and showed no online device. No emulator was started, APK was
  built or installed, TalkBack state was changed, or product behavior was
  exercised.

Evidence and limits:

- The command/result ledger is under
  `.codex/test-artifacts/2026-09-15-gate-30e-installed-talkback-accessibility-closure/verification-ledger.md`.
- No speech, focus traversal, alert-detail, Appearance, screenshot, hierarchy,
  or real-path accessibility evidence was collected. Gate 30E is not verified.
- No Kotlin, Compose, provider, persistence, manifest, or production behavior
  changed. The next action is to restore host PulseAudio and select a fresh
  bounded attempt.

Commit state: uncommitted blocker documentation; no commit was requested.

### 2026-09-15-gate-30e-partial-installed-run-after-audio-recovery

Status: implemented; acceptance incomplete; Gate 30E remains unverified
Mode: bounded installed TalkBack run after host-runtime recovery
Slice: Gate 30E, Installed TalkBack and Accessibility Closure

Result:

- `/run/user/1000/pulse/native` and the session D-Bus socket became visible in
  Codex. `pactl info` passed against PulseAudio 17.0. The visible API-37
  `oxygen_starter` emulator booted on `emulator-5554`, the debug APK built and
  installed once, and Oxygen launched through its production path.
- TalkBack was enabled through Android Accessibility Settings and diagnostics
  showed the Google TalkBack service enabled and bound. The run reached real
  Chicago Home Now data with source/update/provenance, then focused activation
  verified Now -> Hourly -> Daily -> Details and reverse Details -> Daily.
- Appearance was entered, Paper was selected with `Theme saved`, Oxygen was
  restored, and Back returned through Settings to Home. A PulseAudio monitor
  capture was non-silent (`mean_volume -34.6 dB`, `max_volume -15.1 dB`).

Evidence and limits:

- Detailed screenshots, UI hierarchies, accessibility/TTS diagnostics, audio
  capture statistics, restoration state, and the command ledger are under
  `.codex/test-artifacts/2026-09-15-gate-30e-installed-talkback-accessibility-closure/`.
- Chicago exposed no active official alert summary, so live alert-detail
  traversal was unavailable. The audio capture demonstrates sink output but is
  not a human-confirmed speech transcript. Gate 30E therefore remains
  unverified; no product defect was established.
- TalkBack was restored to disabled, the app was force-stopped, the emulator
  was stopped, and ADB showed no online device. No Kotlin, Compose, provider,
  persistence, manifest, or production behavior changed. Compile/unit/connected
  suites were not rerun because this was an installed evidence gate with no
  source changes.

Commit state: uncommitted partial-gate documentation; no commit was requested.

### 2026-09-15-gate-30e-closure-resolution

Status: blocked during bounded installed attempt; Gate 30E remains unverified
Mode: bounded installed TalkBack, live-alert, and production-path evidence
attempt
Slice: Gate 30E, Installed TalkBack and Accessibility Closure

Result:

- NWS validation found one current `status=actual` Severe Flash Flood Warning
  for searchable Shallowater, Texas at `33.68897,-101.99823`, fetched at
  19:33 CDT. The alert was issued by NWS Lubbock TX, affected Lubbock County,
  and expired at 21:00 CDT.
- `DISPLAY=:0 xdpyinfo` and `pactl info` passed. One visible API-37
  `oxygen_starter` session booted, the current debug APK installed once, and
  TalkBack bound with touch exploration enabled.
- Oxygen's normal search selected Shallowater, but its production forecast path
  remained in the truthful offline/no-cache state. The emulator reported
  validated cellular and Wi-Fi networks and a successful ping; the cause of
  the app/provider failure was not established. No alert summary or detail was
  reached.
- TalkBack focus and audio artifacts were captured, but Android logged `TTS is
  not ready`; no human-confirmed speech transcript was obtained. The gate's
  acceptance boundary therefore failed without a product correction.

Evidence:

- Alert validation, selected-location screens, production state, TalkBack
  diagnostics/audio, restoration state, and the command outputs are under
  `.codex/test-artifacts/2026-09-15-gate-30e-closure-resolution/`.
- Restoration returned Chicago as the selected location, removed the temporary
  Shallowater saved location, disabled TalkBack/touch exploration, preserved
  permissions/display settings, stopped the emulator, and ended with no online
  ADB device.
- No Kotlin, Compose, provider, persistence, manifest, or production behavior
  changed. No compile, unit, connected-test, or assembly rerun was needed after
  the unchanged APK installation.

Limits and next action:

- Gate 30E remains unverified. Live alert summary/detail traversal, human
  speech transcript, and TalkBack Appearance traversal were not completed in
  this attempt. Do not claim release readiness or accessibility closure.
- Select a separately named bounded investigation for the installed
  Open-Meteo failure and TTS initialization before repeating this gate.

Commit state: uncommitted documentation/evidence handoff; no commit was requested.

### 2026-09-15-slice-30e-p1-installed-open-meteo-recovery-attempt

Status: planned; blocked at the installed acceptance boundary
Mode: bounded production Open-Meteo recovery investigation
Slice: 30E-P1, Installed Open-Meteo Forecast Recovery

Result:

- Reflection against the compiled request/client contract generated the exact
  Shallowater request. One host `curl --get --data-urlencode` control returned
  HTTP 200 in 0.461988 seconds with a 9,002-byte JSON body, ruling out a
  currently malformed or provider-rejected query at the host boundary.
- One API-37 `oxygen_starter` session reached the recovery preflight. The
  unchanged debug APK was installed once and Oxygen launched without an app
  crash or ANR. Its production path refreshed the retained Chicago location
  through Open-Meteo with current, 48 hourly, and 10 daily rows.
- The first installed UI capture was blocked by a System UI not-responding
  modal. One non-destructive Wait action was followed immediately by a Pixel
  Launcher not-responding modal. Android recorded a 5,000 ms launcher focus
  timeout plus launcher/System UI startup/service timeouts, so the bounded
  attempt stopped without another launch, restart, retry, or Shallowater
  selection.

Evidence and limits:

- Request manifest, host response/hash/headers, pre/final app state, screenshots,
  hierarchies, platform ANR/logcat/window diagnostics, emulator recovery output,
  and the verification ledger are under
  `.codex/test-artifacts/2026-09-15-slice-30e-p1-installed-open-meteo-recovery/`.
- The exact installed Shallowater request was not issued, so the prior
  `NetworkUnavailable` result was neither reproduced nor closed. No Oxygen
  defect, red test, production change, or verified recovery is claimed.
- Selected-location and theme records remained byte-identical; Chicago stayed
  the only saved/cache location; coarse location and accessibility stayed
  disabled; emulator settings were unchanged; shutdown ended with no online
  ADB device. Gradle checks were skipped because production/test inputs did not
  change and the failure was the bounded platform timeout.

Next action: recover the emulator launcher/System UI input path, then resume
30E-P1 in one fresh bounded session. Do not select 30E-P2 yet.

Commit state: uncommitted blocked-attempt documentation; no commit was made.

### 2026-09-15-slice-30e-p1-installed-open-meteo-recovery

Status: verified at the installed acceptance boundary; uncommitted
Mode: resumed bounded production Open-Meteo recovery verification
Slice: 30E-P1, Installed Open-Meteo Forecast Recovery

Result:

- After the user recovered the API-37 emulator to its main screen, preflight
  found a responsive launcher and no ANR since boot. The unchanged debug APK,
  SHA-256
  `9c27f1c390b15e57f3fec9951a5e3fa82f001ba2d9c46120d0c65a7552c4757a`,
  was installed exactly once in the fresh bounded session.
- Normal Oxygen search returned Shallowater, Texas at
  `33.68897,-101.99823`, `America/Chicago`. One `Use now` activation reached
  ready Home through Open-Meteo with real current conditions and truthful
  source/update/provenance. Hourly exposed six visible entries and Daily six
  visible dates from the same loaded forecast.
- The production Room cache keyed the result to Shallowater and contained
  Open-Meteo current data plus 48 hourly and 10 daily rows. No sample data,
  fixture, hidden route, another location's cache, or MET Norway fallback
  satisfied the boundary. Android continued to report no ANR.
- The compiled exact-request host control had already returned HTTP 200 in
  0.461988 seconds with a 9,002-byte body. Because the exact normal installed
  path now also passed without a source/test change, the prior
  `NetworkUnavailable` did not reproduce and is classified as a
  non-reproducible external runtime/transient. No Oxygen defect, red test, or
  production repair is justified; this slice is verified, not `implemented`.

Evidence and restoration:

- Now, Hourly, Daily, hierarchy, logcat, request/cache identity, pre/final
  state, and the complete command ledger are under
  `.codex/test-artifacts/2026-09-15-slice-30e-p1-installed-open-meteo-recovery/`.
- Chicago was restored as the selected and sole saved location; no Shallowater
  saved row was created. Selected-location and theme files were byte-identical
  to preflight. Coarse location and accessibility remained disabled;
  network/display settings were preserved; the emulator was stopped; and ADB
  ended with no online device.
- No Kotlin, Compose, provider, persistence, manifest, APK, or test input
  changed. Compile, unit, connected, and assembly checks were therefore not
  rerun. `README.md`, `docs/OXYGEN_FULL_SPECIFICATION.md`, and
  `docs/data-sources/OPEN_METEO_FORECAST.md` required no change because the
  recovered result matches their existing contracts.

Next action: select Slice 30E-P2 for separately bounded TalkBack/TTS runtime
readiness. Do not treat either repair slice as Gate 30E closure.

Commit state: uncommitted verified-evidence/documentation handoff; no commit
was requested.

### 2026-09-16-slice-30e-p2-talkback-tts-runtime-readiness

Status: planned; platform speech retrospectively human-confirmed; Oxygen
boundary pending
Mode: bounded installed TalkBack/TTS runtime-readiness attempt
Slice: 30E-P2, TalkBack/TTS Runtime Readiness

Result:

- Host display/PulseAudio preflight passed. One visible API-37
  `oxygen_starter` session reached `emulator-5554`, boot completion, and
  package-manager readiness. Device/package/settings/accessibility baseline
  state was captured before changes; the attempted Oxygen app-data snapshots
  were zero-byte files, so selected-location, saved-location, and Appearance
  values were not independently retained in this session.
- TalkBack was enabled through Android Accessibility Settings and was enabled,
  bound, and touch-exploring. Google TTS initialized after startup
  `TTS is not ready` messages; later synthesis and accessibility audio-focus
  events were recorded.
- The normal Android Text-to-speech Settings `Play` control generated Google
  TTS synthesis, but the PulseAudio monitor captured 2,115,156 bytes over
  11.99 seconds at `mean_volume=-91.0 dB` and `max_volume=-67.4 dB`. The user
  later directly confirmed hearing the audio and synthesized voice, so the
  platform speech requirement is satisfied retrospectively; the monitor is
  retained as a non-authoritative diagnostic.
- Oxygen was not installed or exercised after that failure. No Kotlin,
  Compose, provider, persistence, manifest, dependency, test, or APK input
  changed; no Oxygen defect is established.

Evidence and restoration:

- Preflight, Settings screenshots/hierarchies, TalkBack/TTS diagnostics,
  synthesis/audio logcat, raw audio and analysis, APK identity, final state,
  shutdown output, and the ledger are under
  `.codex/test-artifacts/2026-09-15-slice-30e-p2-talkback-tts-runtime-readiness/`.
- TalkBack, enabled/bound services, and touch exploration returned to disabled
  baseline values. TTS/settings, locale, volumes, animation scales,
  permissions, and Oxygen app data were unchanged. The emulator stopped and
  final `adb devices -l` showed no online device.
- `git diff --check` passed and `git status --short` recorded only the active
  plan, roadmap, and history documentation changes. Gradle, unit, connected,
  and assembly checks were skipped because no source, test, resource, manifest,
  dependency, or APK input changed.

Next action: run one fresh bounded session, repeat the independent Android TTS
control, then install and exercise Oxygen's Now-to-Hourly boundary. Gate 30E
remains deferred and unverified.

Commit state: uncommitted blocked-attempt documentation; no commit was made.

### 2026-09-16-slice-30e-p2-human-audio-correction

Status: platform-control result corrected; P2 remains unverified
Mode: user-supplied human observation correction
Slice: 30E-P2, TalkBack/TTS Runtime Readiness

The user reported hearing the Android Text-to-speech Settings `Play` control
and hearing a synthesized voice during the prior bounded session. This
supersedes the earlier missing-human-confirmation classification for the
independent platform control. The low-level PulseAudio monitor remained nearly
silent and is not used as the human oracle. Oxygen was not installed or
exercised in that session, so P2 still requires one fresh session containing
the platform control and the Oxygen Now-summary/Now-to-Hourly traversal.

No production or test source changed. The prior session's restoration and
shutdown evidence remain valid; the retained zero-byte Oxygen app-data
snapshots remain an evidence limitation for that attempt.

### 2026-09-16-slice-30e-p2-retry-2-oxygen-boundary

Status: planned; platform speech and Oxygen semantic/action boundary passed;
human Oxygen speech observation pending
Mode: fresh bounded installed TalkBack/TTS retry
Slice: 30E-P2, TalkBack/TTS Runtime Readiness

Result:

- One fresh visible API-37 `oxygen_starter` session passed host/device
  preflight. TalkBack was enabled through Android Accessibility Settings,
  bound, and touch-exploring. Google TTS initialized after startup messages.
- The Android Text-to-speech Settings `Play` control was repeated, and the
  user confirmed hearing the audio and synthesized voice. This satisfies the
  independent platform speech boundary; the low-level monitor remains only
  corroborating evidence.
- The unchanged debug APK was installed exactly once. Oxygen launched through
  the production path to real Chicago Home data: Cloudy, 70°F, feels like 74°F,
  Open-Meteo provenance, and six visible hourly entries after navigation.
- TalkBack focus reached the combined Now summary and named Hourly action.
  Activating the focused action visibly reached `Hourly, Page 2 of 4`. Evidence
  is under
  `.codex/test-artifacts/2026-09-15-slice-30e-p2-talkback-tts-runtime-readiness/retry-2/`.
- Preference contents were byte-identical before and after the run. TalkBack,
  touch exploration, and bound/enabled services were restored; Oxygen was
  force-stopped; the emulator stopped; and ADB ended with no online device.
- The user has not separately recorded the words heard for the Oxygen Now
  summary and Hourly action. No transcript is fabricated, so P2 remains
  unverified although its production semantic/action boundary passed. No
  product or test source changed.

Next action: confirm from the human observation that Now included the visible
current-condition meaning and Hourly named its destination, then close P2's
evidence state without implying Gate 30E closure.

Commit state: uncommitted blocked/pending-observation documentation; no commit
was made.

### 2026-09-16-slice-30e-p2-human-observation-detail

Status: planned; human Oxygen semantic observation partial
Mode: user-supplied non-verbatim speech observation
Slice: 30E-P2, TalkBack/TTS Runtime Readiness

The user reported that every TalkBack announcement began with the application
name, followed by speech dependent on the focused card/page. This records the
announcement structure and confirms audible app speech, but does not establish
from memory that the Now announcement included the visible current-condition
meaning or that the Hourly announcement named its destination. P2 remains
unverified; no transcript or semantic match is fabricated.

Next action: obtain those two semantic confirmations, then close the P2
evidence state without implying Gate 30E closure.

### 2026-09-16-slice-30e-p2-final-test-correction

Status: planned; final semantic attempt invalid for the required data-focus boundary
Mode: user-supplied correction to installed TalkBack test interpretation
Slice: 30E-P2, TalkBack/TTS Runtime Readiness

The user answered NO to both required blockers: Now did not announce the
current-condition meaning, and Hourly did not announce the named destination.
The user also clarified that each TalkBack activation announced the page-level
application name `Oxygen` followed by the right-aligned page identity, and page
changes announced `Now` or `Hourly`. The weather-data area was not focused while
TalkBack remained active, so the prior run did not accurately exercise the
semantic boundary.

This is a test-procedure correction, not yet an Oxygen defect finding. The
production semantics may have been supplied correctly but never reached, or
the data nodes may be missing/incorrect; a corrected traversal must keep
TalkBack active, visibly establish focus on the Now data node and Hourly
destination/data node, and capture the human result before any repair slice is
opened. Retry-3 artifacts are under
`.codex/test-artifacts/2026-09-15-slice-30e-p2-talkback-tts-runtime-readiness/retry-3-final-confirmation/`.

No production or test source changed. TalkBack and device state were restored,
the emulator stopped, ADB ended with no online device, and `git diff --check`
passed. Gate 30E remains deferred and unverified.

Next action: plan the corrected data-focus traversal only; do not claim P2
verified or diagnose an Oxygen semantics defect from this run.

### 2026-09-16-slice-30e-p2-evidence-documentation-commit

Status: committed in `21e188c`; P2 and Gate 30E remain unverified
Mode: post-commit authority synchronization
Slice: 30E-P2, TalkBack/TTS Runtime Readiness

The bounded P2 evidence, active plan, and roadmap updates are recorded in
`21e188c`. The commit preserves the human-confirmed independent Android TTS
control, the real Oxygen Now-to-Hourly production traversal, and the corrected
finding that the final attempt did not establish TalkBack focus on the weather
data nodes. No production or test source changed. The next action remains one
corrected data-focus traversal; this commit does not close P2, Gate 30E,
accessibility closure, release readiness, or MVP completion.

Post-commit checks: `git diff --check` passed before commit; no Gradle or
connected checks were rerun because source and test inputs were unchanged.

### 2026-09-16-slice-30e-p2-home-talkback-data-focus-semantics

Status: implemented; focused boundary passed; installed speech observation
pending
Mode: bounded Compose semantics repair and installed production recheck
Slice: 30E-P2, Home TalkBack Data-Focus Semantics

Result:

- Added the standalone full-width `home-current-summary` reading target and
  removed the merged weather description from the decorative current mark.
  Pager and Hourly semantics remain unchanged.
- The focused connected case completed 1/1 with 0 skipped and 0 failed on API
  37. It verifies the summary description/bounds, absence of a merged mark
  node and summary actions, the Hourly action, and the first Hourly card.
- The installed production path loaded real Chicago/Open-Meteo data. Native
  hierarchy evidence exposed the corrected summary description at non-empty
  bounds. Human TalkBack speech was not exercised in this session, so P2 is
  not verified and Gate 30E remains deferred/unverified.

Evidence and limits:

- Artifacts: `.codex/test-artifacts/2026-09-16-slice-30e-p2-home-talkback-data-focus-semantics/`.
- Semantics artifact retrieval was attempted after the test, but teardown had
  removed the package (`run-as: unknown package`).
- Passed `:app:compileDebugKotlin :app:compileDebugAndroidTestKotlin`,
  `:app:testDebugUnitTest :core:testDebugUnitTest`, `:app:assembleDebug`, and
  `git diff --check`. The app was cleared, the emulator stopped, and final
  ADB state was captured.

Commit state: committed in `b63b0fe`; the post-commit plan, roadmap, and live
history synchronization remains represented by the final entry below.

### 2026-09-16-slice-30e-p2-home-talkback-data-focus-semantics-installed-blocker

Status: implemented; focused summary and Hourly-card boundaries verified;
installed TalkBack action traversal deferred for remote-control logistics
Mode: bounded visible API-37 TalkBack production journey
Slice: 30E-P2, Home TalkBack Data-Focus Semantics

Result:

- One candidate APK install used SHA-256
  `1a41d5bff0fa2a738d67321d50c5dbe2f5267a7fb41030a931b28b1efa7344cd`.
  Oxygen loaded real Chicago/Open-Meteo Home data, and the native hierarchy plus
  green TalkBack focus outline exposed the new full-width Now summary.
- The user heard the independent Android TTS sample. For Oxygen, the user
  reported hearing `72 degrees` followed by the daily high and low and judged
  it intended; no additional speech words were inferred.
- A horizontal TalkBack gesture was intercepted by the pager and changed to
  Hourly directly, so it did not verify the named `Show next page: Hourly`
  custom action. The deterministic focused connected case had already verified
  that named action and the first Hourly-card semantics; the Lead Project
  Engineer concurred that the remaining installed traversal gap is logistical
  remote control of the emulator, not a product failure. The direct installed
  Hourly screen remains excluded from TalkBack acceptance evidence.

Evidence and limits:

- Artifacts and ledger: `.codex/test-artifacts/2026-09-16-slice-30e-p2-home-talkback-data-focus-semantics/installed-talkback-journey/`.
- The platform control produced a 2,113,496-byte capture and the user heard
  it. Real Home, native hierarchies, focus screenshots, logcat, package state,
  and restoration records are retained.
- A pre-existing Pixel Launcher ANR dialog was dismissed by force-stopping the
  launcher before Settings setup. The first cleanup attempt rejected an empty
  accessibility-service argument; a cleanup-only emulator boot then deleted
  the service setting and verified `accessibility_enabled=0` and
  `touch_exploration_enabled=0` before shutdown. The acceptance journey was not
  retried.
- Focused connected, compile, unit-test, assembly, and diff checks were not
  rerun because source and test inputs were unchanged and prior passing results
  remain valid.

Commit state: committed in `b63b0fe`; the focused Hourly-card test is verified,
while the installed TalkBack action traversal and Gate 30E remain unverified.

### 2026-09-16-ui-specification-review-resolution

Status: specified; documentation ready; committed in `aafefe6`
Mode: documentation-only UI contract and authority alignment

Result:

- Finalized Oxygen UI Specification Version 0.3 from the Draft 0.2 audit
  resolutions and retained the Base Art Sheet v0.2 as direction rather than a
  runtime asset or implementation claim.
- Aligned the product and provider authorities to the target rolling 72-hour
  hourly and ten-day daily horizons. Open-Meteo is specified to request those
  horizons in the first later implementation slice; MET Norway retains actual
  sparse/partial timestamps without padding or interpolation.
- Resolved Standard Home window, navigation, Back, card-boundary, appearance,
  missing-data, accessibility, and evidence rules. Remaining type, token,
  palette, responsive, component, and screenshot-matrix choices are delegated
  to the bounded slices that first need them.

Evidence and limits:

- Read repository authorities, the UI workflow, active roadmap sections, and
  recent cycle state; inspected Base Art Sheet v0.2 and current provider and
  presentation limits; rechecked official Open-Meteo and MET Norway horizon
  documentation.
- `git diff --check`, the untracked-new-file whitespace check, and the focused
  terminology/authority search passed.
- No Gradle, unit, connected, emulator, install, or screenshot check ran
  because no production or test source changed. The installed app still asks
  Open-Meteo for 48 hours and presents 12 hourly entries; no new UI behavior is
  implemented or verified.

Commit state: committed in `aafefe6`; next work is a separate
roadmap-sequencing cycle, starting with the bounded forecast-horizon data
contract.

### 2026-09-16-ui-specification-slice-rule-clarifications

Status: documentation reviewed and committed; UI behavior specified
Mode: documentation-only specification and operating-rule clarification

Result:

- Clarified independently observable slice outcomes, production-path
  prerequisites, protection against scaffolding-only splits, and the roughly
  40% context estimate including evidence, broad checks, and closure.
- Required evidence proportional to the changed boundary, with necessary
  verification included in the same slice. Defined the checkpoint after two
  production-changing slices, counting repairs/refactors and resetting after
  the test/documentation cycle completes.
- Separated behavior status from commit identity and required post-commit
  consistency review without recursive hash-only documentation commits.
- Added the UI specification to the authority order and left sequencing to
  the roadmap. No roadmap is active during UI specification finalization;
  `ui-roadmap.md` is intended but not created, and the MVP roadmap is inactive.
- Retained the pending UI rule: page names stay visible; numeric page count
  and position remain available through accessibility semantics only.

Evidence and limits:

- Reviewed the complete documentation diff and focused `rg` terminology
  results across `AGENTS.md`, the UI specification, and the active plan;
  `git diff --check` passed. Final whitespace check is repeated after this
  completion entry because the documentation inputs changed.
- No production/test files changed. No Gradle build, unit test, connected
  test, emulator, install, or screenshot acceptance ran for these Markdown
  changes. No new runtime behavior is claimed; the generated Now concepts
  are design references, not installed-app verification artifacts.

Commit state: committed in the current documentation commit; roadmap creation
and implementation are separate subsequent work.

### 2026-09-16-ui-roadmap-first-draft

Status: specified; first roadmap draft reviewed and revised; no implementation
slice selected
Mode: documentation-only roadmap review and sequencing

Result:

- Created and audited `.codex/plans/ui-roadmap.md` from UI Specification v0.3,
  the full product specification, and the installed-app/documentation baseline.
- Revised the title and scope to the Home UI v0.3 program rather than implying
  complete 1.0 coverage, and recorded higher-authority custom-unit, missing-
  metric, layout, and later-feature gaps without silently closing them.
- Removed the duplicate four-page composition slice, split Hourly and Daily
  window state machines, split navigation/Back, marks, scenes, Full effects,
  Settings/disclosures, alert detail, and location entry at observable
  boundaries, and assigned page state/accessibility acceptance to each owner.
- Added recurring two-production-slice checkpoints and a minimal orthogonal
  evidence policy requiring functional assertions plus installed rendering
  without repeated cross-product journeys.
- Kept every candidate `specified`; only the active plan may select UI-01 as
  `planned`.

Evidence and limits:

- Reviewed the UI specification, full product specification, UI workflow,
  README, provider template, inactive MVP roadmap, active plan, recent cycle
  history, and current app/core production boundaries.
- `git diff --check`, explicit trailing-whitespace review for the untracked
  roadmap, and focused terminology, dependency, checkpoint-cadence, and status
  review passed.
- No Android build, unit test, connected test, emulator, install, or screenshot
  checks ran because this is documentation-only roadmap work. No UI behavior is
  implemented or verified by this draft.

Commit state: committed in this documentation cycle; the next action is
acceptance of the reviewed roadmap, then separate selection of UI-01 in the
active plan.

### 2026-09-16-ui-01-forecast-horizon-transport

Status: verified; committed with this change
Mode: bounded provider, repository, cache, and installed Android acceptance
Slice: UI-01 — Forecast-horizon transport and preservation

Result:

- Open-Meteo's configurable default request now asks for 72 hourly hours and
  ten daily days. Open-Meteo mapping stably orders valid hourly and daily rows
  after indexed mapping while retaining duplicates, null optional values,
  unequal optional arrays, sparse entries, and valid empty timelines.
- Fallback and cached repository tests prove long nullable hourly/daily lists
  pass through unchanged. The one planned connected case passed 1/1 through
  the installed Open-Meteo client/parser/repository, fallback wrapper, cache
  wrapper, Room storage, and no-alert merge; all 72 hourly instants and ten
  daily dates matched at the terminal and direct Room boundaries.

Evidence and artifacts:

- 49 focused core JVM tests passed; Android test compilation passed; app/core
  unit tests, app Kotlin compilation, debug assembly, and `git diff --check`
  passed. The required old-48-hour query red result is retained.
- Evidence and command output are under
  `.codex/test-artifacts/2026-09-16-ui-01-forecast-horizon-transport/`, with
  the connected result in `android-factory/` and the verification ledger at
  `verification-ledger.md`.
- The recovered API-37 `oxygen_starter` session was stopped after the single
  connected attempt. The recovery host wrapper ended before its preflight
  record was written; read-only preflight was completed in that same emulator
  session and no session restart or connected retry occurred.

Limits and documentation:

- Hourly/Daily window selection, partial-horizon presentation, controls,
  visual work, new providers, Room schema/migrations, and release remain
  outside this slice. Screenshots and live-provider traffic were inapplicable.
- `README.md`, the Open-Meteo provider contract, the UI roadmap, and the active
  plan now state the verified behavior. The full product specification, UI
  specification, and MET Norway provider contract were reviewed unchanged.

Commit state: committed in this change; UI-02 remains `specified` and is not
selected until a new active plan is created.
