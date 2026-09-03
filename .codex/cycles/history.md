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

- Last committed implementation slice: Slice 18J, Standard Home Visual
  Convergence evidence and review, with Slice 18J-R Open-Meteo ready forecast
  recovery included in the same commit.
- Current active slice: none. Next implementation candidate is Slice 19A, Saved
  Location Storage Model.
- Current documentation drift under review: the tracked art sheet path/spec say
  Base Art Sheet v0.1 while the visible image title says v0.2.
- Current process correction: cycle history is now tail-limited for normal reads
  and older detailed ledger content has been archived.

## Recent Cycles

### 2026-09-01-cycle-history-tail-read-contract

Status: ready
Mode: documentation-only
Slice: Cycle history token-size and archive contract
Commit: committed in this changeset

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
Commit: committed in this changeset

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

### 2026-09-02-mobile-one-handed-home-ergonomics-plan

Status: ready
Mode: documentation-only
Slice: Slice 18I planning and roadmap/spec sequencing
Commit: not committed in this turn

Result:
- Added Slice 18I, Mobile One-Handed Home Ergonomics, to
  `.codex/plans/mvp-roadmap.md` as a bounded follow-up to the committed Slice
  18H Standard Home baseline.
- Updated `docs/OXYGEN_FULL_SPECIFICATION.md` section 53 so the immediate next
  candidate is Slice 18I before returning to Slice 19 Saved Locations
  Persistence.
- Replaced the active `.codex/plans/current.md` cycle with a planned Slice 18I
  implementation contract covering bottom thumb-zone actions, Home footer
  clearance, stale-status visual weight, Details provenance ordering, About
  recovery placement, focused Compose tests, and installed-app screenshots.
- Preserved the boundary that operational provenance remains reachable from
  Home and full provider/privacy/license explanation remains in About.

Evidence:
- `git diff --check` passed.
- Android build/test/emulator commands were not run because this planning/status
  sync changed only Markdown files and no Kotlin, Compose, Gradle, manifest,
  resources, provider, persistence, or test behavior.

Artifacts:
- Planning check log:
  `.codex/test-artifacts/2026-09-02-mobile-ui-ergonomics-review/planning-git-diff-check.log`.
- Baseline UI review artifacts:
  `.codex/test-artifacts/2026-09-02-mobile-ui-ergonomics-review/`.

Boundaries:
- No production UI, provider, repository, Room/DataStore, forecast mapping,
  saved-location, unit-preference, alert, air-quality, radar, background
  refresh, persisted appearance, installed-app MET Norway fallback, release, or
  MVP-readiness behavior was changed.

### 2026-09-02-mobile-one-handed-home-ergonomics

Status: ready
Mode: feature
Slice: Slice 18I, Mobile One-Handed Home Ergonomics
Commit: not committed in this turn

Result:
- First-run and change-location screens now keep manual search/disclosure
  content scrollable above bottom-aligned Search, Use my location,
  Settings/About, and return Back actions.
- About overview/detail content now scrolls above a bottom Back action.
- Home Now keeps current weather visually before refresh/stale status, while
  keeping cached/refresh-failed context visible and reachable.
- Details presents metric groups and source/update information before stale
  status/provenance footer; operational provenance remains reachable from Home
  and full explanations remain in About.
- Scrollable Home pages have footer clearance; compact Daily retains its
  established fixed page composition.

Evidence:
- Baseline focused unit tests passed: `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest --tests '*HomeForecast*'`.
- Focused Home Compose instrumentation passed 24 tests on
  `oxygen_starter(AVD) - 17`: `. scripts/android-env.sh && ./gradlew
  :app:connectedDebugAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`.
- Installed-app real path used `scripts/list-avds.sh`, `scripts/start-emulator.sh`,
  `scripts/install-debug.sh`, clean app data, first-run capture, manual
  Open-Meteo geocoding search/select for Madison, Home Now/Hourly/Daily/Details,
  change-location Back/return, About overview, and About Privacy detail.
- Broad checks passed: `. scripts/android-env.sh && ./gradlew
  :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh &&
  ./gradlew :app:assembleDebug`; `git diff --check`.

Artifacts:
- Logs, screenshots, and hierarchies are under
  `.codex/test-artifacts/2026-09-02-mobile-one-handed-home-ergonomics/`.
- Installed evidence includes `installed-first-run-location.png`/`.xml`,
  `installed-location-results.png`/`.xml`, `installed-home-now.png`/`.xml`,
  `installed-home-hourly.png`/`.xml`, `installed-home-daily.png`/`.xml`,
  `installed-home-details.png`/`.xml`, `installed-change-location.png`/`.xml`,
  `installed-after-location-back.xml`, `installed-about-overview.png`/`.xml`,
  and `installed-about-privacy.png`/`.xml`.

Boundaries:
- No saved-location management, Room/DataStore schema behavior, provider
  behavior, forecast mapping, unit preferences, alerts, air quality, radar,
  widget, background refresh, persisted appearance, installed-app MET Norway
  fallback, release readiness, MVP readiness, or sample-weather production
  fallback was added.

### 2026-09-02-open-meteo-ready-forecast-recovery

Status: committed
Mode: fix
Slice: Slice 18J-R, Restore Installed Open-Meteo Ready Forecast Path
Commit: this commit

Result:
- Restored the installed manual Open-Meteo path needed by Slice 18J evidence:
  a real Madison geocoding selection now reaches a ready Home forecast.
- Updated `OpenMeteoGeocodingParser` so a provider empty-search body without a
  `results` array maps to explicit empty results instead of invalid response.
- Preserved malformed present non-array `results` as invalid response.
- Added focused coverage for the current empty geocoding body shape and a
  representative Madison Open-Meteo forecast repository mapping through
  current, hourly, daily, and provenance data.
- No Home visual redesign, saved-location list/switch/remove, unit preference,
  MET Norway installed fallback, alert, air quality, radar, widget, release, or
  MVP-readiness behavior was added.

Evidence:
- Baseline focused Open-Meteo tests passed before production edits.
- Focused checks passed: `. scripts/android-env.sh && ./gradlew
  :core:testDebugUnitTest --tests '*OpenMeteoGeocoding*'`;
  `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests
  '*OpenMeteo*'`.
- Installed-app real-path evidence used `scripts/list-avds.sh`,
  `scripts/start-emulator.sh`, `scripts/install-debug.sh`, manual
  comma-separated Madison search, real Open-Meteo geocoding result selection,
  and captured ready Home Now, Hourly, Daily, and Details screens.
- Room cache behavior was exercised by disabling emulator Wi-Fi/data,
  relaunching the app, and capturing the same Madison forecast restored from
  cache with refresh-failed stale status.
- Broad checks passed: `. scripts/android-env.sh && ./gradlew
  :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh &&
  ./gradlew :app:assembleDebug`; `git diff --check`.

Artifacts:
- Logs, live diagnostic bodies, screenshots, and hierarchies are under
  `.codex/test-artifacts/2026-09-02-open-meteo-ready-forecast-recovery/`.
- Installed evidence includes `installed-home-after-selection.png`/`.xml`,
  `installed-home-hourly.png`/`.xml`, `installed-home-daily.png`/`.xml`,
  `installed-home-details.png`/`.xml`, and
  `installed-home-offline-restored.png`/`.xml`.

Boundaries:
- The successful ready forecast path used the real Open-Meteo provider path and
  did not use `SampleWeather.bundle`, mocked provider success, or fabricated
  fallback data.
- Resume Slice 18J installed visual evidence/review next. Slice 19A remains
  blocked until Slice 18J is committed.

### 2026-09-02-standard-home-visual-convergence-resumed

Status: committed
Mode: feature
Slice: resumed Slice 18J, Standard Home Visual Convergence evidence and review
Commit: this commit

Result:
- Resumed Slice 18J after verified Slice 18J-R removed the installed
  invalid-response blocker.
- Installed current debug app on `oxygen_starter`, cleared app data, used the
  real first-run manual Open-Meteo search path with query `Madison`, selected
  `Madison, Wisconsin, United States`, and captured ready Home Now, Hourly,
  Daily, and Details evidence.
- Disabled emulator Wi-Fi/data, relaunched the app, and captured the same
  Madison forecast restored from Room cache with explicit refresh-failed stale
  context; network services were restored afterward.
- Review found the 18J Home evidence materially more weather-first than the
  committed Slice 18I reference: the scene foundation is visible across pages,
  Now is dominated by the condition mark/current temperature, Hourly/Daily are
  scan-friendly forecast surfaces, Details groups semantic metrics with
  source/update context, and bottom navigation/actions remain subdued and
  reachable.
- No new Home production-code changes were made during this resumed evidence
  pass. No saved-location list/select/remove behavior, unit preference, device
  location expansion, MET Norway installed fallback, alert, air quality, radar,
  widget, background refresh, release, MVP-readiness, sample-weather fallback,
  or additional provider behavior beyond the verified 18J-R fix was added.

Evidence:
- Focused checks passed: `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest --tests '*HomeForecast*'`; `. scripts/android-env.sh
  && ./gradlew :core:testDebugUnitTest --tests '*OpenMeteo*'`; connected Home
  instrumentation with
  `-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`
  finished 25 tests on `oxygen_starter(AVD) - 17`.
- Broad checks passed: `. scripts/android-env.sh && ./gradlew
  :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh &&
  ./gradlew :app:assembleDebug`; `git diff --check`.

Artifacts:
- Logs, screenshots, and hierarchies are under
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/`.
- Resumed installed evidence includes `installed-resumed-first-run.png`/`.xml`,
  `installed-resumed-search-results.png`/`.xml`,
  `installed-resumed-home-now.png`/`.xml`,
  `installed-resumed-home-hourly.png`/`.xml`,
  `installed-resumed-home-daily.png`/`.xml`,
  `installed-resumed-home-details.png`/`.xml`, and
  `installed-resumed-home-offline-restored.png`/`.xml`.
- Resumed logs include `install-debug-resumed.log`,
  `focused-homeforecast-resumed.log`,
  `focused-openmeteo-resumed.log`,
  `focused-home-compose-instrumentation-resumed.log`,
  `broad-compile-debug-kotlin-resumed.log`,
  `broad-debug-unit-tests-resumed.log`,
  `broad-assemble-debug-resumed.log`, and
  `git-diff-check-resumed.log`.

Boundaries:
- Slice 19A is unblocked after this commit.
