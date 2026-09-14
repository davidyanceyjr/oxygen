# Cycle History

## Reading Contract

Normal discovery must not read the full archived cycle ledger.

Read AGENTS.md, .codex/plans/current.md, and this file first. For cycle
history context, use this live file plus at most the most recent three cycle
entries unless a specific implementation detail, regression, artifact, commit,
or authority conflict requires older evidence.

Older detailed history is retained in:

.codex/cycles/archive/history-through-2026-09-01-before-tail-limited-history.md
.codex/cycles/archive/history-through-2026-09-01-before-three-entry-trim-20260901-203633.md
.codex/cycles/archive/history-through-2026-09-03-before-19a-tail-refresh.md
.codex/cycles/archive/history-through-2026-09-03-before-post-19c-doc-sync.md
.codex/cycles/archive/history-through-2026-09-03-before-slice-32-planning.md
.codex/cycles/archive/history-through-2026-09-04-before-plan-gap-fixes.md
.codex/cycles/archive/history-through-2026-09-04-before-pre-19d-authority-drift-cleanup.md
.codex/cycles/archive/history-through-2026-09-04-before-post-20a-doc-sync.md
.codex/cycles/archive/history-through-2026-09-05-before-slice-21-ledger-update.md
.codex/cycles/archive/history-through-2026-09-05-before-slice-22-commit-status-update.md
.codex/cycles/archive/history-through-2026-09-10-before-repo-audit-cleanup.md

When adding a new history entry, append it to this file as a self-contained
section with status, changed behavior or documents, focused evidence, broad
evidence, artifacts, blockers, and commit state. Keep each entry concise enough
that the last one to three entries remain usable within roughly 1,000 tokens.

Before replacing or compressing this live file, archive the previous live file
under .codex/cycles/archive/. Do not create a full duplicate archive before
ordinary append-only writes; Git history plus the archive file preserve previous
ledger states.

## Recent State Summary

- Latest implementation and verification state: Slice 30B1A3A2, Standard Home
  RTL Daily Chronology, is committed at 9390601; evidence is retained under
  .codex/test-artifacts/2026-09-10-slice-30b1a3a2-rtl-standard-daily-chronology/.
- Slice 30A1 is committed at da7b886; Slice 30A2 is committed at 1a8e14f;
  30A3A1 evidence is complete; and 30A3B2 document sync is committed at
  fb51f7b.
- Slice 30B1A1, RTL Semantic Page Navigation Contract, is committed at
  63ed25a; Slice 30B1A2 is committed at 20b6ddc; Slice 30B1A3A1 is committed
  at 74675e2; and Slice 30B1A3A2 is committed at 9390601. Slice 30B1A3A3 is
  the next planned slice in .codex/plans/current.md. No later Gate 30 boundary
  is claimed complete.

## Recent Cycles


### 2026-09-09-slice-30a2-home-compact-large-font-resilience

Status: committed
Mode: bounded Home layout/accessibility implementation
Slice: Slice 30A2, Home Compact and Large-Font Resilience
Commit: 1a8e14f

Result:

- Added three focused connected cases covering Standard compact 1.3, Simple
  compact 1.3 with no-refetch unit/layout changes, and Standard Details 2.0.
- Preserved mapper-owned descriptions, long-content reachability, touch
  targets, bounds, non-overlap, and canonical repository request counts.

Evidence:

- The exact three-case connected filter passed on oxygen_starter / emulator-5554;
  the installed production Chicago path exercised Standard, Simple, and Details.
- Broad compile, app/core unit tests, assemble, and git diff --check passed.

Artifacts:

- .codex/test-artifacts/2026-09-09-slice-30a2-home-compact-large-font-resilience/

Limits:

- RTL, reduced motion, appearance matrix, alerts, localization, and release
  verification remained outside the slice.

### 2026-09-10-slice-30a3b2-home-accessibility-evidence-document-sync

Status: committed
Mode: documentation-only evidence and authority sync
Slice: Slice 30A3B2, Home Accessibility Evidence Document Sync
Commit: fb51f7b

Result:

- Reconciled README, specification, roadmap, active plan, and cycle evidence
  for the completed Home speech/layout boundary.
- Confirmed the named three-case connected command passed with 3 completed,
  0 skipped, and 0 failed tests.
- Retained installed compact Home hierarchies and screenshots under the
  30A3A1 artifact directory and preserved limits around TalkBack, RTL, reduced
  motion, localization, and release readiness.

Evidence:

- Reviewed the retained focused command, installed evidence, and broad
  compile/unit/assemble results; git diff --check passed.
- Android and emulator checks were not rerun because this was documentation-only.

Next candidate: Slice 30B1, Home RTL Navigation and Chronology, selected in
the active plan.

### 2026-09-10-repository-audit-cleanup

Status: committed
Mode: repository cleanup and documentation synchronization
Scope: Findings from the 2026-09-10 Repository Messiness Audit
Commit: 6cab109

Result:

- Corrected the active cycle summary and retained complete prior history in
  dated archives; the live history now keeps the reading contract, summary,
  and three recent entries.
- Archived completed roadmap material while retaining the current queue and
  remaining candidate contracts in the live roadmap.
- Corrected stale production-path, wrapper, and current specification wording;
  renamed the completed license decision record.
- Removed dead scaffold presentation state, moved sample data to the debug
  preview source set, and documented the retained file-backed cache boundary.
- Moved reusable Home and Settings presentation strings into Android resources
  without changing their English output, semantics, or test tags.
- Preserved SPACE_GAME_EMULATOR_WINDOW as an undocumented compatibility fallback
  while making OXYGEN_EMULATOR_WINDOW the supported variable.

Evidence:

- Focused compile and unit checks passed:
  . scripts/android-env.sh && ./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :core:testDebugUnitTest
- Broad debug and release assembly passed:
  . scripts/android-env.sh && ./gradlew :app:assembleDebug :app:assembleRelease
- git diff --check passed after the cleanup.

Limits:

- No connected tests, emulator journey, provider network checks, or dependency
  audit tooling were rerun; this slice changed no provider, domain, cache
  schema, DataStore format, manifest, or user-facing weather behavior.

### 2026-09-10-slice-30b1a1-rtl-semantic-page-navigation

Status: committed
Mode: bounded Home RTL semantic page navigation test slice
Slice: Slice 30B1A1, RTL Semantic Page Navigation Contract
Commit: 63ed25a

Result:

- Added two Compose-local RTL connected cases covering exact semantic action
  progression for Standard Now -> Hourly -> Daily -> Details and Simple Now
  -> Forecast, including first/intermediate/final action sets, page positions,
  titles, handled actions, and destinations.
- Added an explicit layout-direction parameter to the existing test content
  helper, defaulting to LTR, and tightened the custom-action assertion to the
  complete ordered action list. No production correction was needed.

Evidence:

- The exact focused command passed twice on one `oxygen_starter` / `emulator-5554`
  session: 2 completed, 0 skipped, 0 failed each run. The device was API 37,
  420dpi, font scale 1.0, initially LTR (`ldltr`); device direction was not
  changed.
- Broad compile, app/core unit tests, debug assembly, and `git diff --check`
  passed.

Artifacts:

- `.codex/test-artifacts/2026-09-10-slice-30b1a1-rtl-semantic-page-navigation/`
  contains the ledger and focused/broad command logs.

Limits:

- No installed/manual RTL journey, device-level RTL, screenshots or UI
  hierarchies, visual mirroring, swipe-direction acceptance, chronology,
  compact/refetch, provider, or release evidence was collected; those remain
  in 30B1A2–30B1B1 or later scope.

### 2026-09-10-slice-30b1a2-rtl-directional-affordances-gesture

Status: committed
Mode: bounded Home RTL control-mirroring and pager-gesture test slice
Slice: Slice 30B1A2, RTL Directional Affordances and Gesture Behavior
Commit: 20b6ddc

Result:

- Added exactly two Compose-local RTL connected cases covering mirrored
  Standard `Details, Daily, Hourly, Now` and Simple `Forecast, Now` selector
  placement, 48dp targets, selected state, complete semantic action lists,
  settled forward/reverse swipes, and first/final page boundaries.
- No production correction was needed; provider, repository, cache, forecast
  meaning, persistence, and custom-action labels were unchanged.

Evidence:

- The exact focused command passed on `oxygen_starter` / `emulator-5554`:
  exactly 2 completed, 0 skipped, 0 failed. The device was API 37, 420dpi,
  font scale 1.0; RTL remained Compose-local and device direction was not
  changed.
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed;
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
  passed; `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed;
  and `git diff --check` passed. No checks were rerun.

Artifacts:

- `.codex/test-artifacts/2026-09-10-slice-30b1a2-rtl-directional-affordances-gesture/`
  contains the focused instrumentation result files and verification ledger.

Limits:

- No APK reinstall, installed/manual RTL journey, screenshots, UI hierarchies,
  device-level RTL, chronology/spoken-meaning, compact/refetch, provider, or
  TalkBack service evidence was collected; those remain in 30B1A3–30B1B1 or
  later scope.

### 2026-09-10-slice-30b1a3a3-simple-rtl-forecast-chronology

Status: committed
Mode: bounded Simple Home RTL rendered-chronology test slice
Slice: Slice 30B1A3A3, Simple Home RTL Forecast Chronology
Commit: 91974b2

Result:

- Added `rtlSimpleHomeForecastChoicesPreserveChronologicalRenderedOrder` to
  the production-Compose Home test boundary. It selects Simple Forecast
  Hourly and Daily choices, compares rendered unmerged semantics order under
  LTR and Compose-local RTL, and asserts exact six-entry chronology, tags,
  first/last visible payloads, and mapper-owned descriptions.
- No production correction was needed; `HomeLoadingScreen.kt`, provider,
  mapper, repository, cache, persistence, navigation, gestures, and resources
  were unchanged.

Evidence:

- The adjacent RTL Daily baseline passed with 1 completed, 0 skipped, and 0
  failed. The final focused command passed with exactly 1 completed, 0 skipped,
  and 0 failed on `oxygen_starter` / `emulator-5554`, API 37, 420 dpi, font
  scale 1.0. RTL was Compose-local and device-wide direction was unchanged.
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed;
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
  passed; `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed;
  and `git diff --check` passed.

Artifacts:

- `.codex/test-artifacts/2026-09-10-slice-30b1a3a3-simple-rtl-forecast-chronology/`
  contains focused logs, successful result XML/textproto, emulator metadata,
  both retained rendered semantics files, and the verification ledger.
- The first post-edit attempt failed on detached test semantics nodes; LTR
  labels were snapshotted before branch replacement and the focused case then
  passed. An artifact-capture rerun was required because the connected runner
  uninstalls the app after completion; the corrected rerun pulled both files
  before teardown and is not counted as a second acceptance result.

Limits:

- No device-wide or installed/manual RTL journey, screenshots, UI hierarchies,
  TalkBack service traversal, spoken-meaning equivalence, compact/refetch,
  provider/network/cache, or release evidence was collected. The next active
  candidate is Slice 30B1A3B1.

### 2026-09-10-slice-30b1a3a1-rtl-standard-hourly-chronology

Status: committed
Mode: bounded Home RTL rendered-chronology test slice
Slice: Slice 30B1A3A1, Standard Home RTL Hourly Chronology
Commit: 74675e2

Result:

- Added `rtlStandardHomeHourlyPreservesChronologicalRenderedOrder` to the
  production-Compose Home test boundary. It compares LTR and Compose-local RTL
  semantics traversal, requiring exactly the six rendered Hourly tags in
  `6 AM` through `11 AM` order plus visible and spoken first/last payloads.
- No production correction was needed; provider, mapper, repository, cache,
  persistence, navigation, gesture, and localized strings were unchanged.

Evidence:

- The exact focused command passed with 1 completed, 0 skipped, and 0 failed on
  `oxygen_starter` / `emulator-5554`, API 37, 420dpi, font scale 1.0. RTL was
  Compose-local and device direction was unchanged.
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed;
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
  passed; `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed;
  and `git diff --check` passed.

Artifacts:

- `.codex/test-artifacts/2026-09-10-slice-30b1a3a1-rtl-standard-hourly-chronology/`
  contains the command logs, result XML/textproto, retained rendered
  semantics, environment record, and verification ledger. The semantics
  artifact required one capture rerun because the connected task uninstalls
  the app after completion.

Limits:

- No device-wide or installed/manual RTL journey, screenshots, TalkBack
  service traversal, Standard Daily/Simple chronology, spoken-meaning
  equivalence, compact/refetch, provider, or release evidence was collected.
  Slice 30B1A3A2 is the next planned boundary.

### 2026-09-10-slice-30b1a3a2-rtl-standard-daily-chronology

Status: committed
Mode: bounded Home RTL rendered-chronology test slice
Slice: Slice 30B1A3A2, Standard Home RTL Daily Chronology
Commit: 9390601

Result:

- Added `rtlStandardHomeDailyPreservesChronologicalRenderedOrder` to the
  production-Compose Home test boundary. It compares LTR and Compose-local RTL
  semantics traversal, requiring exactly six rendered Daily tags in `Sat, Aug
  22` through `Thu, Aug 27` order plus first/last row meaning.
- No production correction was needed; provider, mapper, repository, cache,
  persistence, navigation, gestures, and localized strings were unchanged.

Evidence:

- The final focused command passed with 1 completed, 0 skipped, and 0 failed on
  `oxygen_starter` / `emulator-5554`, API 37, 420dpi, font scale 1.0. RTL was
  Compose-local and device direction was unchanged.
- Debug compile, app/core unit tests, debug assembly, and `git diff --check`
  passed.

Artifacts:

- `.codex/test-artifacts/2026-09-10-slice-30b1a3a2-rtl-standard-daily-chronology/`
  contains focused logs, result XML, retained rendered semantics, emulator
  metadata, and the verification ledger. Artifact capture required one rerun
  because the connected runner uninstalls the app after completion.

Limits:

- No device-wide or installed/manual RTL journey, screenshots, TalkBack
  service traversal, Simple chronology, spoken-meaning equivalence,
  compact/refetch, provider, or release evidence was collected. Slice
  30B1A3A3 is the next planned boundary.
