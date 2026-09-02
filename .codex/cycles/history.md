# Cycle History

## Reading Contract

Normal discovery must not read the full archived cycle ledger.

Read `AGENTS.md`, `.codex/plans/current.md`, and this file first. For cycle
history context, use this live file plus at most the most recent three cycle
entries unless a specific implementation detail, regression, artifact, commit,
or authority conflict requires older evidence.

Older detailed history is retained in:

```text
.codex/cycles/archive/history-through-2026-09-01-before-tail-limited-history.md
```

When adding a new history entry, append it to this file as a self-contained
section with status, changed behavior or documents, focused evidence, broad
evidence, artifacts, blockers, and commit state. Keep each entry concise enough
that the last one to three entries remain usable within roughly 1,000 tokens.

Before replacing or compressing this live file, archive the previous live file
under `.codex/cycles/archive/`. Do not create a full duplicate archive before
ordinary append-only writes; Git history plus the archive file preserve previous
ledger states.

## Recent State Summary

- Last committed implementation slice: Slice 18H, Standard Home Accessibility
  and Visual Verification Gate, commit `4f5f383`.
- Active planned implementation slice: none. Next implementation candidate is
  Slice 19, Saved Locations Persistence.
- Current documentation drift under review: the tracked art sheet path/spec say
  Base Art Sheet v0.1 while the visible image title says v0.2.
- Current process correction: cycle history is now tail-limited for normal reads
  and older detailed ledger content has been archived.

## Recent Cycles

### 2026-09-01-cycle-history-tail-read-contract

Status: ready
Mode: documentation-only
Slice: Cycle history token-size and archive contract
Commit: not committed in this turn

Result:
- Archived the previous full cycle ledger at
  `.codex/cycles/archive/history-through-2026-09-01-before-tail-limited-history.md`.
- Replaced the live `.codex/cycles/history.md` with a recent-history contract,
  current summary, and self-contained recent entries.
- Updated `AGENTS.md` so normal discovery reads only the live recent history and
  at most the most recent three cycle entries unless older evidence is
  specifically required.
- Clarified that full archive copies are required before replacing or
  compressing live history, not before ordinary append-only history writes.

Evidence:
- `git diff --check` passed.
- Android build/test/emulator commands were not run because this
  documentation-only process change touched only Markdown files and no Kotlin,
  Compose, Gradle, manifest, resources, provider, persistence, or test behavior.

### 2026-09-01-home-design-system-art-sheet-consolidation

Status: committed
Mode: feature
Slice: Slice 18G, Oxygen Home Design-System Consolidation
Commit: `fae63b3`

Result:
- Added static app-local Home design roles for spacing, card shape/padding,
  glass surfaces, outline accents, weather-mark colors, and Home typography.
- Replaced the generic blob-like `WeatherConditionMark` with provider-neutral
  gold-line marks for all current `WeatherCondition` values.
- Applied the Home roles across the installed Home Now, Hourly, Daily, Details,
  stale/status, metric/list, source, and shared glass-panel surfaces without
  changing provider, repository, Room, DataStore, weather mapping, Gradle,
  app identity, saved-location, unit-preference, or alert behavior.

Evidence:
- Baseline focused HomeForecast unit tests passed before production edits.
- Initial baseline connected Home instrumentation failed with `No connected
  devices`; after starting `oxygen_starter`, focused Home instrumentation
  passed 16 tests, including the new rendered-pixel gold-line weather-mark test.
- Final focused checks passed: `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest --tests '*HomeForecast*'`; `. scripts/android-env.sh
  && ./gradlew :app:connectedDebugAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`.
- Broad checks passed: `. scripts/android-env.sh && ./gradlew
  :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh &&
  ./gradlew :app:assembleDebug`; `git diff --check`.

Artifacts:
- Logs and screenshots are under
  `.codex/test-artifacts/2026-09-01-home-design-system-art-sheet-consolidation/`.
- Valid installed Home evidence: `installed-final-home-now.png`/`.xml`,
  `installed-final-home-hourly.png`/`.xml`,
  `installed-final-home-daily.png`/`.xml`,
  `installed-final-home-details.png`/`.xml`, and
  `installed-final-cached-stale-now.png`/`.xml`.
- Final focused logs: `focused-homeforecast-unit.log`,
  `focused-home-compose-instrumentation-final.log`.
- Broad logs: `broad-compile-debug-kotlin.log`,
  `broad-debug-unit-tests.log`, `broad-assemble-debug.log`,
  `git-diff-check.log`.

Boundaries:
- The art-sheet naming drift remains documented only: the tracked path/spec say
  Base Art Sheet v0.1 while the rendered image title says v0.2.
- Atmospheric scene imagery, production bitmap scenes, downloaded fonts, full
  icon packs, persisted presentation settings, full theme engine, installed-app
  MET Norway fallback, saved-location switching, unit preferences, release
  readiness, and MVP readiness were not added or claimed.
- A final installed retryable no-cache screenshot was not captured; the attempt
  failed after the targeted instrumentation run left the debug activity
  unavailable. No-cache behavior remains covered by passing Home instrumentation,
  state-holder, and offline persistence instrumentation boundaries.

### 2026-09-01-project-status-sync-after-home-design-system-consolidation

Status: ready
Mode: documentation-only
Slice: Project status sync after Slice 18G
Commit: not committed in this turn

Result:
- Compared README, specification section 53, roadmap Slice 18G/18H status,
  roadmap next-candidate guidance, active-cycle state, cycle history, `git log`,
  and worktree status after committed Slice 18G `fae63b3`.
- README now records the installed Home art-sheet-aligned weather marks,
  surface roles, typography roles, and app-local design roles.
- Roadmap/spec next-candidate guidance now points to Slice 18H: Standard Home
  Accessibility and Visual Verification Gate.
- Live cycle history now records Slice 18G as committed at `fae63b3`.

Evidence:
- `git diff --check` passed.
- Android build/test/emulator commands were not run because this
  documentation-only sync changed only Markdown files and no Kotlin, Compose,
  Gradle, manifest, resources, provider, persistence, or test behavior.

### 2026-09-01-standard-home-accessibility-visual-verification-gate

Status: committed
Mode: feature
Slice: Slice 18H, Standard Home Accessibility and Visual Verification Gate
Commit: `4f5f383`

Result:
- Added named previous/next custom accessibility actions to the existing
  Standard Home pager container and kept page tabs/swipes as the visible
  navigation model.
- Added an app-local, non-persisted `OxygenAppearance` input to
  `HomeLoadingScreen` so `EffectsLevel.OFF` renders opaque Home surfaces while
  preserving the same production Home composable path and weather semantics.
- Raised Standard Home page tabs to a 48dp minimum height.
- Added focused Home instrumentation coverage for named pager actions,
  child-control page isolation, 48dp control bounds, and effects-disabled
  stale/source/weather meaning.
- Preserved provider, repository, Room, DataStore, forecast mapping,
  selected-location, manual-search, stale-cache, retry/refresh, disclosure,
  Gradle, manifest, saved-location, unit-preference, fallback, alert, air
  quality, radar, release, and MVP-readiness behavior.

Evidence:
- Baseline focused state tests passed:
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*'`.
- Focused Home instrumentation passed 19 tests on `oxygen_starter(AVD) - 17`:
  `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`.
- Installed-app real-path evidence used `scripts/list-avds.sh`,
  `scripts/start-emulator.sh`, and `scripts/install-debug.sh`, then selected a
  real Open-Meteo geocoding result and captured Now, Hourly, Daily, and Details.
- Installed stale refresh-failed cached Home was reached by disabling device
  Wi-Fi/data with `adb shell svc wifi disable` and `adb shell svc data disable`,
  tapping Refresh, then restoring services.
- Broad checks passed: `. scripts/android-env.sh && ./gradlew
  :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh &&
  ./gradlew :app:assembleDebug`; `git diff --check`.

Artifacts:
- Logs and screenshots are under
  `.codex/test-artifacts/2026-09-01-standard-home-accessibility-visual-verification-gate/`.
- Installed screenshots/hierarchies: `installed-final-home-now.png`/`.xml`,
  `installed-final-home-hourly.png`/`.xml`,
  `installed-final-home-daily.png`/`.xml`,
  `installed-final-home-details.png`/`.xml`, and
  `installed-operational-refresh-failed-attempt.png`/`.xml`.
- Focused/broad logs: `baseline-homeforecast-unit.log`,
  `focused-home-compose-instrumentation.log`,
  `broad-compile-debug-kotlin.log`, `broad-debug-unit-tests.log`,
  `broad-assemble-debug.log`, and `git-diff-check.log`.

Boundaries:
- Android shell denied direct airplane-mode broadcasts during operational setup;
  Wi-Fi/data service toggles were sufficient to capture stale refresh-failed
  cached Home, and network services were restored afterward.
- No subjective visual inconsistency was found in the installed Now, Hourly,
  Daily, and Details screenshots during review.

### 2026-09-01-project-status-sync-after-home-accessibility-gate

Status: ready
Mode: documentation-only
Slice: Project status sync after Slice 18H
Commit: not committed in this turn

Result:
- Reconciled active-cycle and recent-history status with committed Slice 18H at
  `4f5f383`.
- Updated roadmap and specification next-candidate guidance from Slice 18H to
  Slice 19: Saved Locations Persistence.
- Preserved the boundary that saved-location behavior is not implemented or
  planned until a new active Slice 19 cycle is selected.

Evidence:
- `git log --oneline -5` showed `4f5f383 Complete Slice 18H Home accessibility
  gate`.
- `git diff --check` passed.
- Android build/test/emulator commands were not run because this
  documentation-only sync changed only Markdown files and no Kotlin, Compose,
  Gradle, manifest, resources, provider, persistence, or test behavior.

### 2026-09-02-location-card-return-path

Status: committed
Mode: fix
Slice: Location card return path from Home
Commit: this commit

Result:
- Opening Location from Home now preserves the current Home screen as a return
  target and shows a visible Back action on the Location card.
- Tapping Back from that Location card restores the previous Home without
  starting a new search or forecast request.
- Selecting a new manual search result from the same Location card still
  replaces the selected Home location through the existing manual-selection
  path.

Evidence:
- Focused state tests passed: `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest' --tests
  '*FirstRunLocationStateHolderTest'`.
- Focused Home Compose instrumentation passed 20 tests on
  `oxygen_starter(AVD) - 17`: `. scripts/android-env.sh && ./gradlew
  :app:connectedDebugAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`.
- Broad checks passed: `. scripts/android-env.sh && ./gradlew
  :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh &&
  ./gradlew :app:assembleDebug`; `git diff --check`.

Artifacts:
- Logs are under
  `.codex/test-artifacts/2026-09-02-location-card-return-path/`.

Boundaries:
- This did not add saved-location management, device-location lookup, provider
  behavior, Room/DataStore schema behavior, unit preferences, alerts, air
  quality, radar, release readiness, or MVP readiness.
- The active saved-location persistence plan in `.codex/plans/current.md`
  predated this fix and was left untouched.
