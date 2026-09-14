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

- Slice 30B1A3B1, RTL/LTR Spoken-Meaning Equivalence, is committed at
  `26b32b8`.
- Slice 30B1A4's two RTL compact/no-refetch boundaries are verified: the
  Standard fixture alignment is committed at `8b5647b`, and the one-method
  Simple result passed with 1 completed, 0 skipped, and 0 failed.
- Gate 30B1B1 is committed as the installed device-wide RTL Home evidence and
  documentation closure. Slice 30B2 remains the next specified candidate.

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
