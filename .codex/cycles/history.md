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
  specified. CD-01 through CD-05 are now verified production slices.
- CD-00 and CD-03 closed the inherited checkpoint and CD-06 closed the second
  two-slice checkpoint. CD-07 is verified and the active implementation count
  is 1 of 2. The combined CD-08 draft exceeded the context ceiling and was
  decomposed; CD-08A is now the selected bounded production slice, with CD-09
  due after CD-08A verification.
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

### 2026-09-18-cd-03-checkpoint-a

Status: verified; committed in `ec49c5f`
Mode: evidence-recovery and documentation checkpoint over CD-01/CD-02

Result:

- Reconciled the installed same-route CD-01 and CD-02 baseline/final PNG/XML
  pairs. Visual inspection confirms the CD-01 centered dial and CD-02 upper
  High/Low satellites while retaining current semantics, source/update context,
  footer, and the documented scope limits.
- Recovered the missing CD-01 focused runner bundles in one API-37 x86_64
  `oxygen_starter` session. The compact hierarchy and alert-outcome cases each
  completed 1/1 with matching XML, instrumentation log, textproto, wrapper
  status 0, acceptance marker, and zero failures/errors/skips.
- Confirmed CD-02's retained focused bundles and both slices' retained broad
  compile, unit, assembly, and diff results. Commits `3cf07b9`, `3500f1f`, and
  `54c53a6` remain in the current ancestry; no app or core source changed.

Evidence and limits:

- Recovery ledger and accepted CD-01 bundles:
  `.codex/test-artifacts/2026-09-18-cd-03a-cd-01-evidence-recovery/`.
- CD-01 and CD-02 installed evidence and retained CD-02 verification ledger
  remain under their existing artifact directories.
- README and the product/UI specifications required no edits because their
  CD-01/CD-02 status and limits were already accurate. Provider, repository,
  cache, persistence, mapper, navigation, theme, Simple layout, responsive,
  RTL, atmosphere, TalkBack, and release behavior remain outside this
  checkpoint.
- The emulator was stopped after the two recovery cases. Broad Android checks
  were not rerun because source and test inputs did not change.

State closure:

- The implementation-slice count reset from 2 of 2 to 0 of 2. CD-04 and CD-05
  remain `specified`; no visual-overhaul progress beyond CD-01/CD-02 is claimed.
- The active plan and roadmap now record CD-03 as verified and committed in
  `ec49c5f`.

### 2026-09-18-cd-04-precipitation-wind-satellites

Status: verified; committed in `17c7a60`
Mode: bounded Standard Now typed presentation and lower constellation slice

Result:

- Added typed near-term forecast precipitation and current-wind satellite
  presentation values. Hourly precipitation is aggregated canonically before
  resolved-unit conversion; current-condition precipitation is not used as a
  forecast satellite. Wind retains raw speed, gust, and direction components,
  and the renderer consumes mapper-owned compact and semantic values.
- Added a fixed 220dp by 64dp lower constellation beneath the unchanged 150dp
  dial. Available satellites use stable tags, symmetric end placement, sole
  satellite centering, truthful omission, and one ordered precipitation-then-
  wind accessibility description. The existing current summary remains
  unchanged and child satellite decoration is hidden from accessibility.
- Added exactly three focused mapper tests and exactly two focused connected
  cases for the planned acceptance boundary.

Evidence and limits:

- Same-route cache-backed Chicago baseline/final PNG/XML, focused unit log,
  connected bundles, broad checks, and the complete ledger are retained under
  `.codex/test-artifacts/2026-09-18-cd-04-precipitation-wind-satellites/`.
- The initial connected attempt failed deterministically because
  `clearAndSetSemantics` hid required child tags; the bounded semantics change
  was applied and the accepted rerun passed. Both accepted connected methods
  completed 1/1 with zero failures, errors, and skips on API-37
  `oxygen_starter`.
- The final installed capture was relaunched with networking disabled from the
  selected Room-backed cache and visibly shows both lower satellites. The
  connected runner removed app data after its run, so Chicago was restored
  through the real saved-location path before the final offline capture.
- Passing broad checks: `:app:compileDebugKotlin`,
  `:app:testDebugUnitTest :core:testDebugUnitTest`, `:app:assembleDebug`, and
  `git diff --check`. Provider/repository/cache tests were inapplicable because
  those production boundaries did not change. Responsive variants, other
  themes, localization, TalkBack traversal, and release verification remain
  out of scope or unverified.

Commit state:

- The verified implementation is committed in `17c7a60`; this documentation
  entry is synchronized by the follow-up documentation commit. CD-05 remains
  `specified` and no next production slice is selected.

### 2026-09-18-cd-05-celestial-halo-now-atmosphere

Status: verified; implementation committed in `e8a24a1`; documentation closure
committed with this entry
Mode: bounded Standard Now visual implementation slice

Result:

- Replaced the generic full-screen scene branch with explicit static profiles
  for every `WeatherCondition`, including a distinct `RAIN_SHOWERS` profile and
  neutral `UNKNOWN` fallback. The renderer uses no animation clock, random
  output, downloaded imagery, or intensity/day-night inference.
- Scoped the scene to the active Standard Now page and clipped it away from
  the shared header/footer and all other Standard/Simple pages. Added a
  distinct gold/cyan concentric halo inside the unchanged 150dp dial for
  effective Subtle effects. Both decorative hosts are hidden from accessibility
  and expose no spoken content or actions.
- Preserved current values, exact merged spoken description, satellite and
  fixed-section bounds, page behavior, and Effects-Off completeness. Added the
  condition profile unit matrix, one Now-local connected case, the retained
  scene accessibility assertion, and the disabled-motion halo-off assertion.

Evidence and limits:

- Same-route Room-cache Chicago baseline/final Off and Subtle PNG/XML pairs,
  focused unit log, accepted connected bundles, broad checks, emulator
  preflight, and the complete verification ledger are retained under
  `.codex/test-artifacts/2026-09-18-cd-05-celestial-halo-now-atmosphere/`.
- `WeatherSceneTest` passed; both named API-37 `oxygen_starter` connected
  methods passed 1/1 with zero failures, errors, and skips. App/core unit
  suites, Kotlin compilation, debug assembly/install, and `git diff --check`
  passed. The first recovery setup, baseline timing, and missing dial-flag
  compile issues were corrected with changed retries and remain recorded in
  the ledger.
- Provider/repository/cache checks were inapplicable because no data boundary
  changed. Full effects, other themes/high contrast, responsive/RTL,
  localization, TalkBack service traversal, alert redesign, and release
  verification remain out of scope or unverified.

State closure:

- The implementation count is now 2 of 2 since CD-03. CD-06 is selected as the
  required test/documentation-only checkpoint; no further production slice is
  selected. README, roadmap, and active plan are synchronized with the
  implementation commit.

### 2026-09-18-cd-06-checkpoint-b

Status: verified; test/documentation checkpoint closed
Mode: CD-04/CD-05 evidence reconciliation and implementation-count reset

Result:

- Reconciled the retained CD-04 and CD-05 same-route installed baseline/final
  PNG/XML pairs, focused unit results, accepted connected bundles, broad
  checks, failed-attempt dispositions, and stated limits.
- Confirmed implementation commits `17c7a60` and `e8a24a1` are in the current
  ancestry and that both slices' production, test, and documentation claims
  remain consistent with the retained evidence.
- Reset the implementation-slice count from 2 of 2 to 0 of 2. CD-07 is now
  selected as the next bounded production slice; this checkpoint adds no visual
  progress.

Evidence and limits:

- CD-04 artifacts are retained under
  `.codex/test-artifacts/2026-09-18-cd-04-precipitation-wind-satellites/`;
  CD-05 artifacts are retained under
  `.codex/test-artifacts/2026-09-18-cd-05-celestial-halo-now-atmosphere/`.
- No Android, emulator, connected, provider, repository, or installed-app
  command was rerun because the accepted evidence and source inputs did not
  change. Documentation consistency was checked with `git diff --check`.

Commit state:

- This checkpoint closure and CD-07 selection are committed together as a
  documentation-only change; no production behavior changed.

### 2026-09-18-cd-07-plan-draft

Status: planned; active first implementation draft; not committed
Mode: bounded planning and authority-consistency documentation

Result:

- Refined `.codex/plans/current.md` as the active CD-07 plan for the
  precipitation and provenance glass panel.
- Kept CD-07 as the only planned slice after the CD-06 reset, with a 35%
  session estimate, a 360x640dp installed Standard Now acceptance boundary,
  typed mapper ownership, and explicit out-of-scope limits.
- Added the mapper production boundary to the intended implementation files
  and staged the baseline, red/baseline coverage, Compose implementation,
  installed exercise, and broad-check sequence.
- Corrected the active roadmap header so it agrees with its body: CD-00
  through CD-06 are verified and CD-07 is the active planned production
  slice.

Evidence and limits:

- Reviewed the active roadmap, current Standard Now mapper/screen path, focused
  mapper coverage, connected Home coverage, UI workflow, and repository
  authorities.
- `git diff --check` passed. No Android build, unit test, connected test,
  emulator, install, or screenshot command ran because this cycle changes
  planning documentation only.
- No production behavior, test behavior, provider/repository behavior, or
  artifact payload changed.

Commit state: the plan and roadmap correction are present in the worktree and
are not committed in this turn.

### 2026-09-18-cd-07-precipitation-provenance-glass-panel

Status: verified; implementation committed in `74e4b33`; documentation closure
in this follow-up commit
Mode: bounded Standard Now typed presentation and compact GlassPanel slice

Result:

- Promoted the existing six-row, canonical `BigDecimal` precipitation aggregate
  to `HomeNearTermPrecipitationPresentation`, owning millimetres, maximum
  probability, compact typed probability/amount text, the compatibility
  summary, and the existing spoken text. The lower precipitation satellite
  derives from that same presentation value.
- Replaced only the normal-font Standard Now precipitation section with one
  opaque existing `GlassPanel`. Its ordered semantics are title, available
  probability, and available amount; it has no action. Simple and large-font
  paths retain their existing summary projection.
- Added mapper assertions for canonical aggregation, metric/US formatting,
  reported zero, probability-only data, absent data, satellite projection,
  summary compatibility, and spoken meaning. Added the two planned Compose
  methods for available context/geometry and zero/probability-only/absent
  omission behavior.

Evidence and artifacts:

- Real selected-location route: Chicago was searched, saved, and selected
  through the installed Open-Meteo path. Baseline is retained at
  `.codex/test-artifacts/2026-09-18-cd-07-precipitation-provenance-glass-panel/baseline/`.
  Final Standard Now panel is retained at `.../final/home-final.png` and its
  hierarchy at `.../final/home-final-hierarchy.xml`. Actual Details provenance
  is retained at `.../final/details-provenance.png` and its hierarchy.
- Both final connected methods passed 1/1 with zero skips, failures, and
  errors on API-37 `oxygen_starter`. Accepted runner XML, instrumentation log,
  textproto, and device/logcat evidence are retained under
  `.../connected-available-final-panel/` and
  `.../connected-omission-final-panel/`.
- Focused mapper tests, Android-test compilation, app/core unit suites, app
  Kotlin compilation, debug assembly, and `git diff --check` passed. The
  verification ledger is
  `.codex/test-artifacts/2026-09-18-cd-07-precipitation-provenance-glass-panel/verification-ledger.md`.

Failed-attempt disposition and limits:

- The first installed visual attempt exposed a collapsed panel; the panel was
  redesigned as a single compact row while retaining typed semantics and the
  fixed geometry. The first available connected runner then timed out without
  XML and no app ANR; a changed rerun exposed a test assertion against a
  non-merged parent text node, which was corrected. The final changed-source
  rerun passed.
- No provider, repository, cache, persistence, navigation, core-model,
  alert, stale/error, responsive/RTL, localization, TalkBack service, or
  release behavior changed or was claimed. No additional product slice is
  selected; CD-08 remains specified.

Commit state: implementation and tests are committed in `74e4b33`; README,
roadmap, active-plan, history, and ledger closure is committed in this
follow-up documentation commit.

### 2026-09-19-cd-08ar2-standard-now-layout-repair

Status: verified; implementation and documentation committed in `a6db648`,
merged to `main` in `68f1fba`
Mode: bounded Standard Now layout repair after the CD-08A harness investigation

Result:

- Repaired the normal-font Standard Now composition so the local supporting
  alert viewport has usable height at 360x640 while preserving the fixed
  150dp dial, 220x64dp lower constellation, typed alert copy, semantics order,
  action absence, and active-alert action branch.
- Kept the five-state regression at explicit `fontScale = 1f`, added elapsed
  phase markers and bounds diagnostics, and retained the large-font non-active
  and large-font RTL active-alert preservation checks.
- The repair first used three failed materially changed attempts: zero-height
  viewport, diagnosed 20dp viewport, and a 20dp-to-48dp repair that exposed
  checked-time reachability. A separate repair slice then passed after the
  supporting viewport reached 72dp and the compact non-active panel used
  tighter vertical padding.

Evidence:

- `standardNowAlertLookupOutcomesAreTruthfulAndActionFree` passed with accepted
  XML, instrumentation log, textproto, exact five-state text/time checks,
  local scrolling, non-overlap, and fixed geometry under
  `.codex/test-artifacts/2026-09-19-cd-08ar2-standard-now-layout-repair/`.
- `compactLargeFontDashboardSectionsHaveReadableRenderedBounds` and
  `standardNowLargeFontRtlOverflowKeepsHeroNavigationAndAlertActionsReachable`
  each passed with accepted runner bundles in the same root.
- `simpleNowContractRemainsScrollableAndUnchanged` also passed after the
  final guard was narrowed to Standard Now; this fourth named case was the
  justified shared-container regression check.
- Private deterministic no-alert PNG and semantics were pulled while the test
  app was installed under `five-state-private-artifacts/`. The installed
  selected Chicago search/save/select route produced a live `Flood Watch`; its
  PNG/XML and actual state are retained under `installed/` and are not claimed
  as no-alert evidence.
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`,
  app and Android-test Kotlin compilation, `:app:assembleDebug`, debug install,
  and `git diff --check` passed. Provider/cache checks, CD-08B, CD-09
  reconciliation, TalkBack service traversal, localization, and release checks
  remain out of scope.

Commit state: production/test changes and synchronized status files are
committed in `a6db648` and merged to `main` in `68f1fba`. CD-09 is selected as
the next test/documentation checkpoint; CD-08B remains specified after it.
