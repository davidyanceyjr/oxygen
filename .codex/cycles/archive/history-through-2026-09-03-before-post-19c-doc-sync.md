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
.codex/cycles/archive/history-through-2026-09-03-before-19a-tail-refresh.md
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

- Last committed implementation slice: Slice 19B, Saved Location Selection and
  Concurrency, committed at `0f649aa`.
- Current active slice: Slice 19C, Saved Location List and Selection UI, ready
  in the working tree and not yet committed.
- Current documentation drift under review: none. The Base Art Sheet filename,
  specification reference, and visible image title agree on v0.2.
- Current process correction: the live cycle history was re-trimmed on
  2026-09-03 after archiving the previous 511-line live file at
  `.codex/cycles/archive/history-through-2026-09-03-before-19a-tail-refresh.md`.

## Recent Cycles

### 2026-09-02-open-meteo-ready-forecast-recovery

Status: committed
Mode: fix
Slice: Slice 18J-R, Restore Installed Open-Meteo Ready Forecast Path
Commit: `15fc10e`

Result:
- Restored the installed manual Open-Meteo path needed by Slice 18J evidence:
  a real Madison geocoding selection reached a ready Home forecast.
- Mapped provider empty geocoding bodies without `results` to explicit empty
  results while preserving malformed present non-array `results` as invalid.
- Added focused Open-Meteo geocoding and forecast repository coverage.

Evidence:
- Focused checks passed: `. scripts/android-env.sh && ./gradlew
  :core:testDebugUnitTest --tests '*OpenMeteoGeocoding*'` and
  `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests
  '*OpenMeteo*'`.
- Installed-app real path used the emulator scripts, selected a real Madison
  Open-Meteo geocoding result, captured ready Now/Hourly/Daily/Details screens,
  then exercised Room cache restoration with emulator Wi-Fi/data disabled.
- Broad checks passed: compileDebugKotlin, app/core debug unit tests,
  assembleDebug, and `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-02-open-meteo-ready-forecast-recovery/`.

Boundaries:
- No saved-location list/switch/remove behavior, unit preference, installed-app
  MET Norway fallback, alert, air quality, radar, widget, release, or
  MVP-readiness behavior was added.

### 2026-09-02-standard-home-visual-convergence-resumed

Status: committed
Mode: feature
Slice: resumed Slice 18J, Standard Home Visual Convergence evidence and review
Commit: `7950a42`

Result:
- Captured installed Home Now, Hourly, Daily, and Details evidence after a real
  Madison manual Open-Meteo selection.
- Captured offline restored Room-cache Home with refresh-failed stale context
  after disabling emulator Wi-Fi/data and relaunching.
- Review found the Home evidence weather-first, scan-friendly, and consistent
  with the Slice 18H/18I behavior and accessibility baseline.

Evidence:
- Focused checks passed: HomeForecast unit tests, OpenMeteo core tests, and
  connected Home instrumentation with 25 tests on `oxygen_starter(AVD) - 17`.
- Broad checks passed: compileDebugKotlin, app/core debug unit tests,
  assembleDebug, and `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/`.

Boundaries:
- No production code changed during the resumed evidence pass beyond the
  verified 18J-R provider-path fix. Slice 19A was unblocked by this commit.

### 2026-09-03-art-sheet-version-doc-cleanup

Status: committed
Mode: documentation-only
Slice: Art-sheet version documentation cleanup
Commit: `67c1293`, merged by `d070380`

Result:
- Renamed the reviewable visual-language source artifact from v0.1 to v0.2 so
  the tracked filename matches the visible image title.
- Updated the specification and MVP roadmap to reference Base Art Sheet v0.2.
- Updated recent state to report no active art-sheet documentation drift.

Evidence:
- `git diff --check` passed.

Artifacts:
- `.codex/test-artifacts/2026-09-03-art-sheet-version-doc-cleanup/git-diff-check.log`.

Boundaries:
- No Kotlin, Compose, Gradle, manifest, provider, cache, selected-location,
  saved-location, unit preference, alert, appearance setting, release, or MVP
  behavior changed.
- Android compile, unit tests, and assemble were not run because this was a
  documentation/asset-governance cleanup only.

### 2026-09-03-saved-location-storage-model

Status: ready
Mode: feature
Slice: Slice 19A, Saved Location Storage Model
Commit: committed in this changeset

Result:
- Added a provider-neutral core `SavedLocationStorage` contract for saving,
  listing, and removing `WeatherLocation` records by `LocationId`.
- Added Room-backed saved-location storage in the existing Oxygen database with
  a separate `saved_locations` table and deterministic newest-last replacement
  ordering.
- Bumped the Room database from version 1 to version 2 with an explicit
  non-destructive migration that preserves forecast-cache tables.
- Preserved DataStore as the current selected-location source of truth and kept
  saved-location storage unwired from Home/MainActivity.

Evidence:
- Baseline checks passed: `:app:testDebugUnitTest --tests
  '*HomeForecastStateHolderTest'` and `:core:testDebugUnitTest --tests
  '*CachedWeatherRepositoryTest'`.
- Focused connected checks passed on `oxygen_starter(AVD) - 17`: 16 core Room
  tests including saved-location storage, forecast-cache separation, and v1-to-v2
  migration; 5 app offline/selected-location persistence tests including saved
  removal independence.
- Broad checks passed: `. scripts/android-env.sh && ./gradlew
  :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh &&
  ./gradlew :app:assembleDebug`; `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-03-saved-location-storage-model/`.

Boundaries:
- No saved-location UI, Home location switching, selection replacement policy,
  provider behavior, forecast mapping, unit preferences, alerts, air quality,
  radar, appearance settings, installed-app MET Norway fallback, release, or
  MVP-readiness behavior was added.

### 2026-09-03-saved-location-selection-state

Status: ready
Mode: feature
Slice: Slice 19B, Saved Location Selection and Concurrency
Commit: committed in this changeset

Result:
- Added production app-state saved-location loading and saved-location
  selection by provider-neutral `LocationId`.
- Preserved DataStore `SelectedLocationStorage` as the selected-location source
  of truth by writing it before cache restore or provider refresh starts.
- Reused the existing Home lifecycle for loading, matching-cache restore,
  provider refresh, stale-after-refresh-failure, retry, and obsolete work
  isolation.
- Wired `RoomSavedLocationStorageFactory` into `MainActivity` alongside the
  Room forecast cache and cached Open-Meteo repository.

Evidence:
- Focused unit check passed: `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'`.
- Connected checks passed on `oxygen_starter`: `:core:connectedDebugAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.core.provider.cache.room.RoomSavedLocationStorageInstrumentedTest`
  and `:app:connectedDebugAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.OfflineLaunchPersistenceInstrumentedTest`.
- Installed debug launch passed through `scripts/install-debug.sh`.
- Broad checks passed: `. scripts/android-env.sh && ./gradlew
  :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh &&
  ./gradlew :app:assembleDebug`; `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-03-saved-location-selection-state/`.

Boundaries:
- No saved-location picker/list/edit/delete/reorder UI, save button,
  current-selection badge, Room schema change, DataStore format change,
  forecast-cache format change, provider request change, unit preference,
  alert, air quality, radar, appearance setting, installed-app MET Norway
  fallback, release, or MVP-readiness behavior was added.

### 2026-09-03-saved-location-list-select-ui

Status: ready
Mode: feature
Slice: Slice 19C, Saved Location List and Selection UI
Commit: committed in this changeset

Result:
- Home's `Location` action now opens the existing location-entry surface with
  saved locations loaded from production `SavedLocationStorage`.
- The location-entry screen renders saved rows with city, region/country,
  coordinate/time-zone detail, a current-location marker, and visible select
  controls.
- Saved selection is wired through the Slice 19B app-state path by local
  `LocationId`; Room/DataStore saved-list work runs off the Compose main thread.
- Manual search remains usable when saved storage is empty, unavailable, or
  failing. README/About status now separates saved list/select from save/remove
  UI, which remains unimplemented.

Evidence:
- Baseline unit check passed: `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'`.
- Initial baseline connected check failed while the emulator was still booting;
  the retried connected HomeDashboard class later passed.
- Focused checks passed: app unit tests for Home state, About disclosure, and
  app contract; `HomeDashboardUiTest` with 29 connected tests; and
  `OfflineLaunchPersistenceInstrumentedTest` with 7 connected tests including
  the production Room-backed 19C path.
- Raw installed-app exercise passed by `adb install`, `am instrument` seeding of
  production Room/DataStore state, Home launch, Location tap, UI-tree inspection,
  and default/compact/large-font screenshot capture.
- Broad checks passed: `. scripts/android-env.sh && ./gradlew
  :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh &&
  ./gradlew :app:assembleDebug`; `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-03-saved-location-list-select-ui/`.
- Installed screenshots include `installed-saved-locations-default-final.png`,
  `installed-saved-locations-compact-360dp-scrolled.png`, and
  `installed-saved-locations-compact-360dp-large-font-deeper-scroll.png`.

Boundaries:
- No search-result save UI, saved-location remove UI, remove confirmation,
  reorder/grouping, automatic multi-location refresh, background refresh, unit
  preference, alert, air quality, radar/map, widget, notification, provider
  request, Room schema, DataStore format, forecast-cache format, release, or
  MVP-readiness behavior was added.
