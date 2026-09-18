# Cycle History

## Reading Contract

Normal discovery reads this live file's summary and at most its most recent
three entries. Older detailed history is retained under `.codex/cycles/archive/`.
The complete live ledger before the CD-00 failure closure is archived at
`archive/history-through-2026-09-17-before-cd-00-failure.md`.

When adding a new entry, append a concise self-contained section with status,
result, evidence, artifacts, limits, and commit state. Archive the current live
file before replacing or compressing it.

## Recent State Summary

- UI-04 Standard Now and UI-05 Standard Hourly are implemented and committed
  in `1d0547f` and `a119ac2`; their original ledgers and installed evidence are
  retained under `.codex/test-artifacts/`.
- The former UI roadmap was retired after its visual-overhaul outcome failed.
  The user then selected concept F, Celestial Dial, and a new active roadmap was
  specified. No Celestial Dial production slice has started.
- The inherited two-implementation-slice checkpoint remains due. CD-00 stopped
  when its first fresh post-UI-05 Compose case completed 1/1 with one compact
  Standard Now location/current overlap failure. Its other four cases were not
  run, the count remains 2 of 2, and CD-00R is the selected bounded repair.
- TalkBack service traversal remains unverified and is not an early-cycle
  roadmap or release gate under the 2026-09-17 project decision.

## Recent Cycles

### 2026-09-17-ui-roadmap-retired

Status: documentation decision; committed in `b064bd8`
Mode: planning reset after failed visual-overhaul outcome

Result:

- Retired the former UI roadmap and removed UI-06 selection after the user
  rejected the visual-overhaul result. The visual overhaul remained
  unimplemented; no production UI or behavior changed.
- Recorded a visibly distinct Standard Now hero as the next product direction,
  pending a newly selected design and roadmap.

Evidence and limits:

- Reviewed the affected documentation diff and `git diff --check` passed.
- No Android build, unit, connected, emulator, installed-app, provider, or
  production checks ran because the change only removed planning claims.

### 2026-09-17-now-celestial-dial-roadmap

Status: specified; committed with this change
Mode: design selection and implementation-roadmap definition

Result:

- The user selected concept F, Celestial Dial, from nine Now-page concepts.
- Added the concept board and an active roadmap whose production slices each
  have an installed visual boundary and an estimate below the user-directed
  60% context ceiling. Checkpoints do not count as visual-overhaul progress.
- Preserved the inherited checkpoint due after UI-04/UI-05. CD-00 was selected
  before any Celestial Dial production work; CD-01 remained `specified`.

Evidence and limits:

- Reviewed the current Standard Now Compose path, procedural scene, weather
  marks, theme roles, full/UI specifications, and existing connected coverage.
- This was documentation/design work only. It did not build, install, test, or
  change production behavior, and it did not implement the visual overhaul.

### 2026-09-17-cd-00-pre-overhaul-checkpoint

Status: covered; not verified; stopped on normal connected-test failure;
committed with this change
Mode: test-only inherited evidence checkpoint

Result:

- Audited commits `1d0547f` and `a119ac2`, both prior verification ledgers,
  accepted runner bundles, installed 360x640 PNG/XML evidence, and recorded
  broad results. Both commits are ancestors of the current source and their
  stated feature limits remain accurate.
- Recovered one healthy API-37 x86_64 `oxygen_starter` emulator session. The
  first required method,
  `standardNowCompactHierarchyUsesFixedHeroAndNonOverflowingSupport`, completed
  1/1 with zero skips and one failure: `home-section-location should not
  overlap home-section-current` at `HomeDashboardUiTest.kt:4447`.
- Preserved the failure XML, instrumentation output, textproto, wrapper result,
  device diagnostics, and logcat, stopped the emulator, and did not run the
  other four methods. CD-00 is not verified and the implementation count was
  not reset.

Evidence and limits:

- Ledger and artifacts:
  `.codex/test-artifacts/2026-09-17-cd-00-pre-overhaul-checkpoint/`.
- No production or test source changed. UI-05 cases, unit suites, assembly,
  provider/repository tests, live traffic, a new installed journey, screenshots,
  and TalkBack traversal were not run after the terminal first-case failure.
- README now records the compact no-overlap boundary as pending repair. The
  full and UI specifications were reviewed unchanged because their intended
  contracts remain accurate.

Next action: CD-00R, a bounded investigation and repair of the Standard Now
compact location/current overlap. Return to the complete five-case CD-00
checkpoint after that repair; do not select CD-01 beforehand.

### 2026-09-17-cd-00r-standard-now-compact-overlap

Status: verified; test-boundary repair accepted; committed in `bb2a4cc`
Mode: bounded deterministic Compose harness repair

Result:

- The initial retained CD-00 failure was reproduced once as a normal assertion
  failure. A diagnostic rerun passed and recorded valid production semantic
  bounds: location `Rect.fromLTRB(18, 92, 342, 140)`, current
  `Rect.fromLTRB(18, 150, 342, 297)`, and precipitation
  `Rect.fromLTRB(18, 307, 342, 379)`.
- The deterministic compact case now waits for Compose idle before reading
  semantics bounds. The final named method completed 1/1 with zero failures,
  errors, and skips; fresh XML, instrumentation log, textproto, and wrapper
  agree.

Evidence and limits:

- Repair artifacts, including the retained failed run, diagnostic bounds log,
  and accepted result bundles:
  `.codex/test-artifacts/2026-09-17-cd-00r-standard-now-compact-overlap/`.
- Focused broad checks passed in
  `.codex/test-artifacts/2026-09-17-cd-00r-standard-now-compact-overlap/broad-checks.log`:
  Android-test compilation, app/core unit tests, debug assembly, and
  `git diff --check`.
- No production layout, weather semantics, provider, repository, cache,
  persistence, navigation, theme, or Simple layout changed. The complete
  five-case CD-00 checkpoint is still pending and CD-01 remains unspecified.

Next action: rerun all five named CD-00 cases on the repaired test boundary.

### 2026-09-17-cd-00-pre-overhaul-checkpoint-rerun

Status: verified; committed
Mode: inherited two-production-slice checkpoint rerun

Result:

- On one recovered API-37 x86_64 `oxygen_starter` session, all five named
  UI-04 Home Compose cases passed individually after CD-00R:
  compact hierarchy, alert outcomes, operational missing/empty states,
  large-font RTL overflow, and unchanged Simple Now.
- Each fresh runner bundle agrees: XML `tests="1" failures="0" errors="0"
  skipped="0"`, instrumentation log `OK (1 test)`, textproto
  `test_status: PASSED`, and wrapper status 0. The emulator was stopped after
  the run.
- Post-checkpoint app/core unit tests, debug assembly, and `git diff --check`
  passed. The implementation-slice count reset from 2 of 2 to 0 of 2.

Evidence and limits:

- Per-case bundles and emulator diagnostics:
  `.codex/test-artifacts/2026-09-17-cd-00-pre-overhaul-checkpoint-rerun/connected/`.
- Broad-check log:
  `.codex/test-artifacts/2026-09-17-cd-00-pre-overhaul-checkpoint-rerun/broad-checks.log`.
- No Celestial Dial production slice started; provider, repository, cache,
  persistence, navigation, theme, weather semantics, and Simple layout remain
  unchanged. CD-01 remains `specified` and is not selected by this checkpoint.

### 2026-09-17-cd-01-central-dial

Status: verified; committed
Mode: bounded Standard Now visual implementation slice

Result:

- Replaced the normal-font Standard Now rectangular current hero with one
  centered code-native circular dial. The dial visibly contains the condition
  mark, current temperature, condition text, and apparent temperature; actual
  high/low remains in the temporary line below it for CD-02.
- Added a truthful unavailable circular state for missing current data without
  rendering a fake temperature. Preserved the existing compact-font path,
  current spoken summary, selected location, precipitation, alerts, footer,
  and other page behavior.
- Added focused connected assertions for the 150dp centered dial geometry and
  unavailable-dial state.

Evidence:

- Baseline and final same-route installed captures/XML:
  `.codex/test-artifacts/2026-09-17-cd-01-central-dial/`; final clean route is
  `home-final-clean-2.png` with `home-final-clean-2.xml`.
- Focused API-37 `oxygen_starter` cases
  `standardNowCompactHierarchyUsesFixedHeroAndNonOverflowingSupport` and
  `standardNowAlertLookupOutcomesAreTruthfulAndActionFree` each completed 1/1
  with zero failures, errors, and skips. The single emulator session was
  stopped after evidence capture.
- `:app:testDebugUnitTest :core:testDebugUnitTest :app:assembleDebug`,
  `:app:compileDebugKotlin`, and `git diff --check` passed. One first focused
  attempt exposed insufficient lower-content space; a concrete geometry
  adjustment fixed it and the accepted rerun passed.

Limits and commit state:

- No provider/repository/cache/persistence, navigation/footer, Simple layout,
  Hourly/Daily/Details, new weather value, orbit satellite, atmosphere, or
  theme redesign changed. Compact-font responsive variants remain CD-11 scope.
- Committed in the current change; the implementation-slice count is now 1 of
  2 since CD-00. CD-02 remains specified and is not selected by this slice.

### 2026-09-18-cd-02-high-low-satellites

Status: verified; committed in `3500f1f`
Mode: bounded normal-font Standard Now visual implementation slice

Result:

- Replaced CD-01's temporary Today high/low line with an upper High/Low
  constellation of two static 52dp circular satellites above the unchanged
  centered 150dp dial. Existing formatted presentation values are rendered
  verbatim; missing high/low values omit only their named satellite and both
  missing omits the constellation.
- Preserved the merged current spoken description, condition/current/apparent
  semantics, selected location, precipitation, source/update context, footer,
  compact path, unavailable-current path, and all provider/repository inputs.
- Added two focused connected methods covering available values plus dynamic
  high-missing, low-missing, and both-missing fixtures. The sparse fixture was
  constrained so the mapper's intentional next-daily fallback could not supply
  stale hero values in the both-missing case.

Evidence:

- Installed same-route baseline/final PNG and XML:
  `.codex/test-artifacts/2026-09-17-cd-02-high-low-satellites/baseline/` and
  `final/`. The final real Chicago route visibly shows both satellites and
  retains actual high/low, spoken summary, source/update, and footer context.
- Focused API-37 `oxygen_starter` cases
  `standardNowShowsHighLowSatellitesWithoutChangingCurrentSummary` and
  `standardNowSatelliteStatesOmitOnlyMissingDailyValues` each completed 1/1
  with zero failures, errors, and skips. Fresh runner bundles and diagnostics
  are under `focused-available/` and `focused-sparse-final/`; the failed
  fixture and diagnostic attempts remain under `focused-sparse/` and
  `focused-sparse-diagnostic/`.
- `broad-checks.log` records passing `:app:compileDebugKotlin`,
  `:app:testDebugUnitTest :core:testDebugUnitTest`, `:app:assembleDebug`, and
  `git diff --check`. Full ledger:
  `.codex/test-artifacts/2026-09-17-cd-02-high-low-satellites/verification-ledger.md`.

Limits and next action:

- Provider/repository exercise was inapplicable because production data,
  mapper logic, cache, persistence, units, and navigation did not change.
  Compact/large-font responsive work, RTL/theme translation, precipitation or
  wind satellites, atmosphere, and other Celestial Dial surfaces remain out of
  scope. TalkBack service traversal remains unverified under the project
  decision.
- The implementation count is now 2 of 2 since CD-00. CD-03 is selected as
  the required test/documentation checkpoint and will reset the count before
  another production slice is selected.

Commit state:

- This verified slice is included in the current documentation and production
  change; the commit identity is recorded separately by Git.
