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

- Last committed implementation slice: Slice 20C, Unit Conversion Presentation
  Boundary, committed at `1b52718`.
- Last committed implementation gate: Gate 20-0, Presentation Semantics and
  Localization Safety, committed at `587b0ad`.
- Last committed documentation sync: Post-20-0 Authority Sync, committed at
  `a0bca26`. The next implementation candidate is Slice 21: optional device
  location through the installed path.
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

### 2026-09-04-gate-19f-saved-locations-doc-sync

Status: committed
Mode: documentation-only
Slice: Gate 19F, Saved Locations Documentation Sync
Commit: `8386484`

Result:
- Replaced the completed Slice 19E active plan with a bounded Gate 19F
  documentation-sync plan and results.
- Updated specification section 53 so it reflects saved-location behavior
  committed through Slice 19E at `00cb88a`, identifies Gate 19F as the current
  documentation sync, and names Slice 20A as the next implementation candidate
  after the sync is committed.
- Updated the MVP roadmap so Slice 19E is committed at `00cb88a`, Gate 19F is
  committed at `8386484`, and next-candidate guidance advances to Slice 20A.
- Updated this live history summary to remove stale claims that 19E is
  uncommitted or next.

Evidence:
- `git diff --check` passed.

Artifacts:
- `.codex/test-artifacts/2026-09-04-gate-19f-saved-locations-doc-sync/git-diff-check.log`.

Blockers:
- None.

Boundaries:
- No Kotlin, Compose, Gradle, manifest, provider request, Room schema,
  DataStore format, forecast-cache format, UI behavior, saved-location
  behavior, provider behavior, unit preference, alert, air quality, radar,
  release, or MVP behavior changed.
- Android compile, unit, connected, assemble, emulator, and install commands
  were not run because this was a Markdown-only status sync.

### 2026-09-04-slice-20a-unit-preference-contract

Status: committed
Mode: feature
Slice: Slice 20A, Unit Preference Contract
Commit: committed in this changeset

Result:
- Added provider-neutral unit preference contract types in `:core` for
  temperature, wind speed, pressure, precipitation, and visibility.
- Added deterministic resolution for Metric, US, UK, and Custom preferences;
  Custom carries explicit choices for every category.
- Documented Metric, US, UK, and Custom preset behavior in specification
  section 38.

Evidence:
- Baseline provider canonical check passed:
  `:core:testDebugUnitTest --tests '*OpenMeteoForecastClientTest' --tests
  '*OpenMeteoForecastMapperTest' --tests '*MetNoForecastMapperTest'`.
- Red focused check failed before implementation on unresolved unit preference
  symbols; rerun after implementation passed with
  `:core:testDebugUnitTest --tests '*UnitPreferenceTest'`.
- Broad checks passed: `:app:compileDebugKotlin`,
  `:app:testDebugUnitTest :core:testDebugUnitTest`, `:app:assembleDebug`, and
  `git diff --check`.
- Static review found unit preference symbols only under `core.model`, with no
  provider, cache, Room, DataStore, app UI, Home formatting, or provider request
  adoption.

Artifacts:
- `.codex/test-artifacts/2026-09-04-slice-20a-unit-preference-contract/`.

Blockers:
- None.

Boundaries:
- No unit conversion math, persisted unit preference storage, Settings/Home UI,
  Home presentation formatting, provider request units, Room schema, DataStore
  format, forecast-cache format, saved-location storage, installed-app runtime
  behavior, alert, air quality, radar, release, or MVP-readiness behavior
  changed.
- Emulator, install, connected Android tests, and screenshot capture were not
  run because this pure provider-neutral contract slice intentionally does not
  change installed UI or runtime behavior.

### 2026-09-04-post-20a-authority-sync

Status: verified
Mode: documentation-only
Slice: Post-20A Authority Sync
Commit: committed in this changeset

Result:
- Updated the roadmap and specification to record committed Slice 20A and set
  Gate 20-0, Presentation Semantics and Localization Safety, as next.
- Replaced the active plan with a bounded Gate 20-0 plan and corrected the
  recent-state summary.

Evidence:
- `git diff --check` passed.
- Stale next-candidate search found no remaining 20A-next references in the
  active roadmap, specification, history, or plan.

Artifacts:
- Archived the pre-sync live history at
  `.codex/cycles/archive/history-through-2026-09-04-before-post-20a-doc-sync.md`.

Boundaries:
- No Kotlin, Compose, Gradle, manifest, provider, persistence, cache, or
  installed-app behavior changed.

### 2026-09-04-gate-20-0-presentation-semantics-localization-safety

Status: committed
Mode: gate
Slice: Gate 20-0, Presentation Semantics and Localization Safety
Commit: `587b0ad`

Result:
- Kept existing Home formatted output while adding explicit nullable canonical
  values to current, hourly, daily, and metric presentation models. Metric
  variants name their canonical units, including Celsius, percent, meters per
  second, degrees, hPa, meters, and millimeters.
- Preserved `HomeMetricIdentity` for grouping and `WeatherCondition` for
  condition/icon semantics; no Home composable parses formatted weather text.

Evidence:
- Focused `HomeForecastPresentationMapperTest` and
  `HomeForecastStateHolderTest` passed; the mapper test covers semantic values,
  formatted text stability, condition identity, and null preservation.
- Connected `HomeDashboardUiTest` passed all 33 tests on `oxygen_starter`,
  including changed-label Details grouping and rendered semantics.
- Installed debug app launched on `oxygen_starter`; broad compile, app/core
  unit tests, assemble, and `git diff --check` all passed.

Artifacts:
- `.codex/test-artifacts/2026-09-04-gate-20-0-presentation-semantics-localization-safety/`.

Blockers:
- None.

Boundaries:
- No conversion formulas, persisted preferences, Settings controls, provider,
  repository, Room/DataStore/cache, location, alert, air-quality, radar/map,
  appearance, widget, notification, release, or MVP behavior changed.

### 2026-09-04-post-20-0-authority-sync

Status: committed
Mode: documentation-only
Slice: Post-20-0 Authority Sync
Commit: `a0bca26`

Result:
- Updated the specification and roadmap to record committed Gate 20-0 evidence
  at `587b0ad`.
- Advanced the next implementation candidate to Slice 20B: Unit Conversion
  Presentation Boundary.
- Replaced the active plan with this bounded Markdown-only authority sync.

Evidence:
- Stale next-candidate search returned no matches in the active specification,
  roadmap, current plan, or live recent history.
- `git diff --check` passed.

Artifacts:
- `.codex/test-artifacts/2026-09-04-post-20-0-authority-sync/git-diff-check.log`.

Blockers:
- None.

Boundaries:
- No Kotlin, Compose, Gradle, manifest, provider request, Room schema,
  DataStore format, forecast-cache format, UI behavior, saved-location
  behavior, provider behavior, unit conversion, persisted unit preference,
  alert, air quality, radar, release, or MVP behavior changed.
- Android compile, unit, connected, assemble, emulator, install, and screenshot
  commands were not run because this was a Markdown-only authority sync.

### 2026-09-04-slice-20b-unit-conversion-presentation-boundary

Status: committed
Mode: feature
Slice: Slice 20B, Unit Conversion Presentation Boundary
Commit: committed in this changeset

Result:
- Added selected-unit conversion at the Home presentation mapper boundary.
- Preserved the installed compatibility default of Fahrenheit, km/h, hPa, mm,
  and km, including whole-meter sub-kilometer visibility.
- Centralized deterministic `HALF_UP` rounding for temperature, wind, pressure,
  precipitation, and visibility display text.
- Kept canonical `WeatherBundle` values, Gate 20-0 semantic fields, condition
  identities, metric identities, source, provenance, and section ordering
  unchanged.
- Added focused mapper tests for Metric, US, UK, Custom, null preservation,
  direction-only wind, aggregate precipitation, rounding boundaries, default
  compatibility output, and canonical immutability.

Evidence:
- Baseline focused app/state and core provider/unit tests passed before edits.
- Focused mapper/state tests passed after edits.
- Connected `HomeDashboardUiTest` passed all 33 tests on `oxygen_starter`.
- `scripts/install-debug.sh` installed and launched the debug app successfully.
- Broad `:app:compileDebugKotlin`, full app/core unit tests,
  `:app:assembleDebug`, and `git diff --check` passed.

Artifacts:
- `.codex/test-artifacts/2026-09-04-slice-20b-unit-conversion-presentation-boundary/`.

Blockers:
- None for this slice. The first connected attempt occurred before emulator
  boot completed; the rerun passed after boot.

Boundaries:
- No persisted preference selection, Settings/Home unit controls, provider
  request units, provider mappings, repositories, Room/DataStore/cache schema,
  location, alerts, air quality, radar/map, appearance, release, or MVP
  behavior changed. Slice 20C must exercise persisted alternate-unit choice
  through the installed path.

### 2026-09-05-post-20b-authority-sync

Status: ready
Mode: documentation-only
Slice: Post-20B Authority Sync
Commit: not committed

Result:
- Updated README to distinguish the implemented provider-neutral unit
  preference foundation and Home presentation conversion boundary from
  unimplemented persisted unit preference selection UI.
- Updated specification section 53 to record Slice 20B as committed and make
  Slice 20C, persisted alternate-unit reachability through the installed path,
  the next implementation candidate.
- Replaced the active plan with this bounded Markdown-only authority sync.

Evidence:
- Stale authority search identified outdated Slice 20B next-candidate and
  broad unit-preference-not-implemented claims before edits.
- `git diff --check` passed.

Artifacts:
- `.codex/test-artifacts/2026-09-05-post-20b-authority-sync/git-diff-check.log`.

Blockers:
- None.

Boundaries:
- No Kotlin, Compose, Gradle, manifest, provider request, Room schema,
  DataStore format, forecast-cache format, UI behavior, saved-location
  behavior, provider behavior, unit conversion, persisted unit preference
  selection, alert, air quality, radar, release, or MVP behavior changed.
- Android compile, unit, connected, assemble, emulator, install, and screenshot
  commands were not run because this was a Markdown-only authority sync.

### 2026-09-05-slice-20c-persisted-unit-selection

Status: committed
Mode: feature
Slice: Slice 20C, Persisted Alternate-Unit Reachability
Commit: `1b52718`

Result:
- Added a dedicated versioned Preferences DataStore for preset and custom
  provider-neutral unit preferences, with safe non-mutating invalid-record
  fallback and a no-op default storage.
- Added installed Settings / About / Units selection for Oxygen default, Metric,
  US, and UK; successful writes remap the visible selected-location Home through
  the existing mapper without refresh or cache mutation.
- Applied the loaded selection to live, restored-cache, refreshed, and retained
  stale Home presentations while preserving canonical weather and provenance.

Evidence:
- Focused app unit tests passed for codec, state-holder, About, and mapper
  boundaries.
- OfflineLaunchPersistenceInstrumentedTest passed all 10 tests, including a
  fresh DataStore readback and state-holder startup readback.
- HomeDashboardUiTest passed all 34 tests, including the installed Compose
  Units journey; a direct single-test rerun also passed and produced the
  screenshot artifact.
- Broad compile, full app/core unit tests, assemble, and diff checks passed.

Artifacts:
- `.codex/test-artifacts/2026-09-05-slice-20c-persisted-unit-selection/`
- Includes installed Units and changed-Home PNGs plus semantics/test logs.

Blockers:
- None. An initial HomeDashboard run was interrupted by an emulator ADB
  disconnect after four tests; the restarted complete run passed.

Boundaries:
- No provider request units or mappings, core domain models, forecast/cache/
  selected-location/saved-location schemas, location permission, alerts, air
  quality, radar/maps, appearance settings beyond selected units, release, or
  MVP behavior changed. Custom-unit editing remains out of scope.

### 2026-09-05-slice-21-optional-device-location

Status: verified
Mode: feature
Slice: Slice 21, Optional Device Location
Commit: not committed

Result:
- Added an app-local coarse-location acquisition path with a cancellable
  attempt boundary, a one-shot device-point source, and a selected-location
  write before the existing Home handoff.
- Added provider-neutral coordinate-to-timezone resolution in `:core` through
  the verified Open-Meteo `timezone=auto` metadata request.
- Updated the installed app disclosures, manifest, and first-run surface to
  keep manual search usable while making device location optional and
  action-triggered.
- Verified the installed path on the emulator: coarse permission grants,
  approximate device point acquisition, metadata-only timezone resolution,
  selected-location persistence, and restored Home with the approximate
  label.

Evidence:
- Focused unit tests passed:
  `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*OpenMeteoTimeZoneResolverTest*'`
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*FirstRunLocationStateHolderTest*' --tests '*DeviceLocationSourceTest*'`
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`.
- Connected persistence test passed:
  `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.OfflineLaunchPersistenceInstrumentedTest`.
- Broad checks passed:
  `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
  `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
  `git diff --check`.
- Installed screenshots saved under
  `.codex/test-artifacts/2026-09-05-slice-21-device-location/`:
  `entry-baseline.png`, `permission-or-progress.png`, `after-permission-grant.png`,
  `after-test-provider-start.png`, and `large-font-restored.png`.

Artifacts:
- `.codex/test-artifacts/2026-09-05-slice-21-device-location/`.

Blockers:
- Emulator `geo fix` alone did not deliver a timely fix to the app's bounded
  request window; the successful real-path proof used the emulator's test GPS
  provider via `adb shell cmd location providers add-test-provider gps ...`
  and `set-test-provider-location gps --location 43.0731,-89.4012`.

Boundaries:
- No fine/background location, Play Services dependency, provider/cache
  schema change, saved-location schema change, or continuous tracking behavior
  was added.

### 2026-09-05-slice-22-nws-alert-provider-contract

Status: ready
Mode: documentation / provider contract
Slice: Slice 22, NWS Alert Provider Contract
Commit: not committed

Result:
- Added `docs/data-sources/NWS_ALERTS.md` as the NOAA/National Weather Service
  active-alert provider contract for selected-point official alerts.
- Contracted exact endpoint, headers/User-Agent, source-reviewed rate/cache
  behavior, required alert fields, timestamps, severity/urgency/certainty,
  identity/lifecycle, geometry/affected-area fallback, errors, attribution,
  license/privacy, unsupported-region behavior, fixtures, and alert/forecast
  independence.
- Recorded that existing `WeatherAlert`/`AlertProvider` names must be evolved
  rather than duplicated: domain expansion belongs to Slice 23A, result/error
  boundary evolution to Slice 23B, and independent forecast/alert composition to
  Slice 23C.

Evidence:
- Live NWS evidence passed on 2026-09-05 with
  `User-Agent: OxygenWeather/0.1 (https://github.com/davidyanceyjr/oxygen/issues)`
  and `Accept: application/geo+json`: Madison point returned HTTP 200
  GeoJSON `FeatureCollection` with zero features and cache headers; London
  point returned HTTP 400 problem JSON matching the contracted conservative
  unsupported-region discriminator.
- OpenAPI fetch passed and was inspected for `/alerts/active`, `AlertPoint`,
  response media types, alert properties/enums/references, and generic problem
  schema.
- Source review covered NWS API service docs, OpenAPI, Alerts Web Service, NWS
  Geolocation Guide, NWS CAP/OASIS CAP sources, disclaimer, privacy policy, and
  PNS26-62.
- Broad Markdown checks passed: `git diff --check`, `git diff --stat`, and the
  bounded diff review over current plan, NWS contract, spec, data/privacy docs,
  roadmap, and cycle history. `git diff --no-index -- /dev/null
  docs/data-sources/NWS_ALERTS.md` was used to review the new untracked
  contract file; its exit code 1 is expected for new-file content.

Artifacts:
- `.codex/test-artifacts/2026-09-05-slice-22-nws-alert-provider-contract/`.

Blockers:
- None.

Boundaries:
- No Kotlin, Compose, Gradle, resource, manifest, provider runtime,
  persistence, permission, UI, `DATA_SOURCES.md`, `PRIVACY.md`, specification,
  roadmap-status, active-provider claim, Android build/test, emulator, install,
  or screenshot behavior changed. NWS alerts remain roadmap-only.
