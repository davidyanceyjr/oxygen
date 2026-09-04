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
.codex/cycles/archive/history-through-2026-09-03-before-post-19c-doc-sync.md
.codex/cycles/archive/history-through-2026-09-03-before-slice-32-planning.md
.codex/cycles/archive/history-through-2026-09-04-before-plan-gap-fixes.md
.codex/cycles/archive/history-through-2026-09-04-before-pre-19d-authority-drift-cleanup.md
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

- Last committed implementation slice: Slice 19D, Save Search Result UI,
  committed at `8599640`.
- Last committed documentation cleanup: Post-19D Authority Sync, committed at
  `0fb2ce6`.
- Current planned implementation slice: Slice 19E, Remove Saved Location UI,
  currently uncommitted.
- Current documentation drift under review: About disclosure copy still uses the
  coarse phrase `saved-location save/remove UI`; the 19E review must split or
  update that wording after removal behavior is verified. Specification,
  roadmap, and README are aligned to Slice 19E as the next candidate.
- Current process correction: the live cycle history was compressed on
  2026-09-04 after archiving the previous live file at
  `.codex/cycles/archive/history-through-2026-09-04-before-pre-19d-authority-drift-cleanup.md`.

## Recent Cycles

### 2026-09-03-fallback-cache-provenance

Status: committed
Mode: feature
Slice: Slice 31B, Fallback Cache and Provenance
Commit: `4028044`

Result:
- Added cache-only forecast metadata for provider cache headers and provider
  response metadata without adding raw headers to `WeatherBundle` or Home UI.
- MET Norway repository success now carries Expires, Last-Modified, ETag,
  fetch time, response coordinates/elevation, provider updated time, and
  provider ID for cache storage.
- Room forecast cache storage persists that metadata with a v2-to-v3 migration,
  clears stale provider cache metadata on non-metadata replacement, and still
  restores cached MET Norway forecasts with MET Norway provenance.

Evidence:
- Baseline checks passed: `InstalledForecastRepositoryFactoryTest`,
  `HomeForecastStateHolderTest`, and `CachedWeatherRepositoryTest`.
- Focused checks passed: core `CachedWeatherRepositoryTest` plus
  `MetNoWeatherRepositoryTest`, app `AboutDisclosureStateHolderTest`, connected
  `RoomForecastCacheStorageInstrumentedTest`, and connected
  `RoomSavedLocationStorageInstrumentedTest`.
- Broad checks passed: compileDebugKotlin, app/core debug unit tests,
  assembleDebug, and `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/`.

Blockers:
- Initial connected Room attempt found no connected devices. The repo-local
  `oxygen_starter` emulator was started. A parallel connected rerun then
  crashed instrumentation/uninstall; sequential reruns passed.

Boundaries:
- No conditional GET request, 304 not-modified handling, provider health or
  backoff state, provider preference UI, saved-location save/remove UI, unit
  preference, alert, air quality, radar, release-candidate status, or
  MVP-readiness behavior was added.

### 2026-09-03-fallback-real-path-verification

Status: committed
Mode: feature
Slice: Slice 32, Fallback Real-Path Verification
Commit: `9b9d706`

Result:
- Added deterministic connected installed-boundary tests for fallback-served
  Room restore and later Open-Meteo replacement using
  `RoomForecastCacheStorageFactory.create(...)`,
  `DataStoreSelectedLocationStorage`,
  `InstalledForecastRepositoryFactory.create(...)`, and
  `OxygenAppStateHolder`.
- Verified fallback-served Home state keeps MET Norway source, combined
  NLOD/CC-BY license, issued/fetched/model-estimate provenance, no sample data,
  empty alert state, and no rendered alert section.
- Verified a later Open-Meteo success replaces cached MET Norway data through
  the normal selected-location refresh/cache path.
- Reconciled `docs/data-sources/MET_NORWAY_FORECAST.md` with active
  installed-app fallback status while leaving conditional GET/304, provider
  health/backoff, and release-candidate fallback behavior unclaimed.

Evidence:
- Baseline checks passed: app `InstalledForecastRepositoryFactoryTest`, app
  `HomeForecastStateHolderTest`, core `FallbackWeatherRepositoryTest`, and
  `scripts/list-avds.sh`.
- Focused checks passed: app installed factory, app Home forecast state, core
  fallback repository, connected `InstalledFallbackRepositoryInstrumentedTest`,
  and connected `RoomForecastCacheStorageInstrumentedTest`.
- Real-path exercise passed: `scripts/start-emulator.sh` and
  `scripts/install-debug.sh` on `oxygen_starter`.
- Broad checks passed: compileDebugKotlin, app/core debug unit tests,
  assembleDebug, and `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/`.

Blockers:
- Initial focused connected run failed because no device was connected. After
  starting `oxygen_starter`, the first rerun exposed a test expectation mismatch
  for MET Norway's combined license string; the corrected rerun passed.

Boundaries:
- No production Kotlin behavior, provider semantics, Home copy, UI layout,
  Room schema, DataStore format, selected-location behavior, saved-location
  behavior, official alert provider behavior, conditional GET/304 handling,
  provider health/backoff, unit preference, appearance setting, release, or
  MVP-readiness behavior changed.
- Live manual Open-Meteo geocoding selection was not run; deterministic
  connected installed-boundary coverage exercised the default Open-Meteo
  success/replacement path without relying on provider/network availability.

### 2026-09-04-pre-19d-authority-drift-cleanup

Status: committed
Mode: documentation-only
Slice: Pre-19D authority drift cleanup
Commit: `2c779cc`

Result:
- Updated specification section 53 so it no longer points future work at
  already committed Slice 19A and now identifies Slice 19D as the next
  implementation candidate.
- Updated the MVP roadmap tail so Slice 32 is committed at `9b9d706`, the next
  candidate remains Slice 19D, and startup guidance no longer asks for a Slice
  32 plan.
- Archived the pre-compression live cycle history and kept this live file to
  the reading contract plus three recent cycle entries.

Evidence:
- `git diff --check` passed.

Artifacts:
- `.codex/test-artifacts/2026-09-04-pre-19d-authority-drift-cleanup/git-diff-check.log`.

Boundaries:
- No Kotlin, Compose, Gradle, manifest, provider request, Room schema,
  DataStore format, forecast-cache format, UI behavior, saved-location
  behavior, provider behavior, unit preference, alert, air quality, radar,
  release, or MVP behavior changed.
- Android compile, unit, connected, and assemble commands were not run because
  this was a Markdown-only authority cleanup.

### 2026-09-04-slice-19d-save-search-result-ui

Status: committed
Mode: feature
Slice: Slice 19D, Save Search Result UI
Commit: `8599640`

Result:
- Added a search-result save event to `OxygenAppStateHolder` that saves the
  provider-neutral `WeatherLocation` through `SavedLocationStorage`, refreshes
  saved rows, and reports local saved-location failures without selecting the
  location or starting a forecast.
- Added separate per-result `Save` and `Use now` controls to the location-entry
  Compose surface with stable tags such as `location-entry-result-save-0` and
  `location-entry-result-use-now-0`.
- Hid search-result save controls when saved-location storage is unavailable.
- Updated README status so search-result save UI is no longer listed as not
  implemented.

Evidence:
- Baseline focused unit passed:
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*FirstRunLocationStateHolderTest' --tests '*HomeForecastStateHolderTest'`.
- Red focused unit failed on missing `onManualLocationCandidateSaved`, then the
  same focused unit command passed after implementation.
- Connected Room/app-state evidence passed on rerun:
  `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.OfflineLaunchPersistenceInstrumentedTest`.
- Connected Compose evidence passed:
  `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`.
- Installed-app exercise passed on `oxygen_starter`: searched live Open-Meteo
  geocoding for Chicago, saw Save and Use now controls, saved Chicago into the
  visible saved-location list without leaving location entry, then Use now
  opened Home with Open-Meteo forecast data.
- Broad checks passed: compileDebugKotlin, app/core debug unit tests,
  assembleDebug, and `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/`.

Blockers:
- Initial connected Room/app-state run failed because the new test used
  persistent DataStore selected-location state and started on Home. The test was
  corrected to isolate selected-location storage while still using production
  `RoomSavedLocationStorageFactory.create(...)` for the save path.
- The installed-app exercise initially hit an emulator Pixel Launcher ANR
  dialog. Dismissing the system dialog allowed the Oxygen installed path to be
  exercised.

Boundaries:
- No saved-location removal UI, saved-location reordering/favorites, provider
  changes, forecast cache/Room schema changes, DataStore format changes, unit
  preferences, alerts, air quality, radar/maps, appearance settings, widgets,
  background refresh, notifications, release, or MVP-readiness behavior was
  added.

### 2026-09-04-post-19d-authority-sync

Status: committed
Mode: documentation-only
Slice: Post-19D Authority Sync
Commit: `0fb2ce6`

Result:
- Updated specification section 53 so it no longer identifies already committed
  Slice 19D as the next implementation candidate and now points to Slice 19E.
- Updated the MVP roadmap so Slice 19D is committed at `8599640`, the latest
  completed local implementation slice is Slice 19D, and the next-candidate
  startup guidance selects Slice 19E.
- Updated this live history summary to reflect the post-19D authority sync.

Evidence:
- `git diff --check` passed.

Artifacts:
- `.codex/test-artifacts/2026-09-04-post-19d-authority-sync/git-diff-check.log`.

Boundaries:
- No Kotlin, Compose, Gradle, manifest, provider request, Room schema,
  DataStore format, forecast-cache format, UI behavior, saved-location
  behavior, provider behavior, unit preference, alert, air quality, radar,
  release, or MVP behavior changed.
- Android compile, unit, connected, and assemble commands were not run because
  this was a Markdown-only authority sync.

### 2026-09-04-slice-19e-remove-saved-location-ui

Status: committed
Mode: feature
Slice: Slice 19E, Remove Saved Location UI
Commit: committed in this changeset

Result:
- Added explicit saved-location removal handling to the installed
  location-entry path.
- Saved rows now expose separate select/remove controls; removal requires an
  inline confirmation/cancel step before calling production
  `SavedLocationStorage.removeLocation(...)`.
- Confirmed removal refreshes only saved-location presentation state. Removing
  the current saved location leaves selected-location storage, Room forecast
  cache data, provider refresh requests, and the visible Home forecast
  unchanged.
- Updated README and About disclosure copy so saved-location removal UI is no
  longer listed as unimplemented.

Evidence:
- Baseline focused app-state unit check passed before production edits.
- Focused checks passed: `:app:testDebugUnitTest --tests
  '*FirstRunLocationStateHolderTest' --tests '*HomeForecastStateHolderTest'`,
  connected `OfflineLaunchPersistenceInstrumentedTest`, and connected
  `HomeDashboardUiTest`.
- Real-path exercise: `scripts/list-avds.sh`, `scripts/start-emulator.sh`, and
  `scripts/install-debug.sh` passed on `oxygen_starter`; installed launch was
  captured after dismissing an emulator System UI ANR dialog.
- Broad checks passed: `:app:compileDebugKotlin`,
  `:app:testDebugUnitTest :core:testDebugUnitTest`, `:app:assembleDebug`, and
  `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-04-slice-19e-remove-saved-location-ui/`.

Blockers:
- Full manual live-geocoding creation/removal was not claimed. Deterministic
  connected installed-boundary tests seeded production Room/DataStore/cache
  state and exercised the app-state removal path.

Boundaries:
- No Room schema, DataStore format, forecast-cache format, provider request,
  forecast repository, fallback-selection, unit preference, alert, air quality,
  radar/map, appearance setting, release, or MVP-readiness behavior changed.
