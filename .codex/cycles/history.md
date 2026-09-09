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

- Latest implementation and verification state: Slice 28B2, Persisted Theme
  Settings UI, is committed at `2c88b9c`; focused, broad, and installed
  evidence is retained at
  `.codex/test-artifacts/2026-09-09-slice-28b2-persisted-theme-settings-ui/`.
- Slice 28B1 is committed at `708172f` and merged by `82cf281`; the current
  Settings path now wires its production DataStore and exposes Oxygen, Paper,
  and Terminal selection with confirmed-write semantics.
- Current process state: Slice 29A remains the next specified candidate; no
  later slice is planned in this sync.

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

Status: committed
Mode: documentation-only
Slice: Post-20B Authority Sync
Commit: `0fb2ce6`

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

Status: committed
Mode: feature
Slice: Slice 21, Optional Device Location
Commit: `3ea5ae6`

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

Status: committed
Mode: documentation / provider contract
Slice: Slice 22, NWS Alert Provider Contract
Commit: `d0a7eb3`

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
- PR `#10` merged to `origin/main` as commit `d0a7eb3`; local `main` was
  fast-forwarded to the merge commit, and the slice branch was removed.

Artifacts:
- `.codex/test-artifacts/2026-09-05-slice-22-nws-alert-provider-contract/`.

Blockers:
- None.

Boundaries:
- No Kotlin, Compose, Gradle, resource, manifest, provider runtime,
  persistence, permission, UI, `DATA_SOURCES.md`, `PRIVACY.md`, specification,
  roadmap-status, active-provider claim, Android build/test, emulator, install,
  or screenshot behavior changed. NWS alerts remain roadmap-only.

### 2026-09-05-slice-23a-nws-alert-fixtures-parsing-mapping

Status: committed
Mode: feature
Slice: Slice 23A, NWS Alert Fixtures, Parsing, and Mapping
Commit: `17dab0c`

Result:
- Expanded the provider-neutral `WeatherAlert` model with urgency, certainty,
  lifecycle, replacement references, affected area, GeoJSON geometry,
  timestamps, optional alert metadata, and official-alert provenance retention.
- Added offline NWS active-alert DTO, parser, and mapper code under
  `core/.../provider/nws/` without transport, repository, cache, app, or UI
  wiring.
- Added committed-style fixture coverage for empty, one, many, missing optional,
  unknown enum, polygon geometry, null geometry, duplicate ID, update/cancel
  references, near-future effective, expired/superseded cached input,
  invalid timestamps, invalid geometry, malformed active envelope, malformed
  problem body, and unsupported-region problem body cases.

Evidence:
- Focused NWS parser/mapper tests passed:
  `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*NwsAlertParserTest' --tests '*NwsAlertMapperTest'`.
- Existing Open-Meteo/MET Norway provider parser/mapper regressions passed:
  `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*OpenMeteoForecastParserTest' --tests '*OpenMeteoForecastMapperTest' --tests '*MetNoForecastParserTest' --tests '*MetNoForecastMapperTest'`.
- Broad checks passed:
  `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
  `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
  `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-05-slice-23a-nws-alert-fixtures-parsing-mapping/`.

Blockers:
- None. The first focused NWS run failed because the malformed-problem parser
  test expected one envelope failure subtype for both problem fixtures; the
  test was corrected and the focused rerun passed. A later invalid-geometry
  coverage addition initially expected a less precise field path; the test was
  corrected to the mapper's exact polygon longitude path and rerun passed.

Boundaries:
- No NWS HTTP client, headers, request/error-result classification,
  repository composition, deduplication, cache filtering, persistence, Room,
  app UI, installed path, Gradle, dependency, live NWS request, emulator,
  connected test, alert presentation, or `AlertProvider` result-boundary
  behavior changed.

### 2026-09-05-slice-23b-nws-alert-transport-boundary

Status: committed
Mode: feature
Slice: Slice 23B, NWS Alert Transport, Classification, and Provider Boundary
Commit: `dcf707b`

Result:
- Added provider-neutral alert success metadata and classified failure results.
- Added configurable NWS HTTPS transport with required identity headers,
  point validation/query construction, problem-body classification,
  case-insensitive response headers, and injected clock use.
- Added the NWS provider adapter mapping parsed alerts into provider-neutral
  `WeatherAlert` values with deterministic provenance metadata.
- Added focused client, provider, and provider-contract tests covering empty
  and non-empty success collections, metadata propagation, invalid input,
  unsupported-region, ordinary 400, malformed problem, identification
  rejection, network, rate limit, provider-unavailable, malformed success,
  and unexpected-provider cases.

Evidence:
- Core compilation, Slice 23A parser/mapper regression, app compilation, full
  app/core unit tests, focused 23B client/provider/contract tests, debug
  assembly, and `git diff --check` passed.

Blockers:
- None.

Boundaries:
- No alert/forecast composition, deduplication, cache or persistence, app/UI,
  background refresh, emulator, connected test, or live NWS behavior changed.

### 2026-09-06-slice-23c-alert-repository-merge

Status: committed
Mode: feature
Slice: Slice 23C, Alert Repository Merge
Commit: `3c658a8`

Result:
- Corrected `AlertProvider` to the synchronous blocking result boundary.
- Added provider-neutral `AlertLookupStatus` and the outer
  `AlertMergingWeatherRepository`, including independent forecast success,
  failure, stale/fallback provenance, deterministic alert deduplication, and
  forecast-only cache composition.
- Kept installed-app alert presentation, Room alert persistence, retries, and
  background work out of scope.

Evidence:
- Baseline focused 23A/23B/cache tests passed before changes.
- Focused merge, provider-contract, NWS transport/provider, and cache tests
  passed.
- Madison live `NwsAlertProvider` check passed within the 60-second cap; the
  disposable test was removed afterward.
- App compile, full app/core unit tests, debug assembly, and `git diff --check`
  passed.

Artifacts:
- `.codex/test-artifacts/2026-09-06-slice-23c-alert-repository-merge/`.

Blockers:
- None.

Boundaries:
- No app factory wiring, UI, Room schema, alert cache, forecast provider
  semantics, or connected/emulator test was added.

### 2026-09-06-slice-24a-alert-summary-banner-ui

Status: committed
Mode: feature
Slice: Slice 24A, Alert Summary/Banner UI
Commit: `cf9ddaf`
Authority sync: `cc2af8b`

Result:
- Installed the production composition
  `Open-Meteo -> FallbackWeatherRepository -> CachedWeatherRepository ->
  AlertMergingWeatherRepository(NwsAlertProvider)`.
- Added provider-neutral successful alert metadata and process-local
  per-provider/per-point 30-second alert request gating, including failed-call
  backoff, positive `Retry-After` extension, exact-boundary retry, and retained
  in-memory successful results on skips.
- Carried `WeatherBundle` plus `AlertLookupStatus` through the app canonical
  forecast session so unit remaps preserve the alert source-check time and
  cached restores remain `NotRequested`.
- Rendered the effects-independent Home Now official-alert summary with
  selected-zone expiry/source-check time, total count, NOAA/NWS attribution,
  validated HTTPS source link/fallback, and direct complete `WeatherAlert`
  values retained for later detail work.
- Reconciled README, data-source, privacy, disclosure, and full-specification
  status while retaining detail, persistence/cache, background, notification,
  and non-NWS-region boundaries.

Evidence:
- Core focused merge/provider/cache tests passed, including final gate
  hardening.
- App focused factory/mapper/state/disclosure tests passed.
- `HomeDashboardUiTest` passed all 37 connected tests; the dedicated alert test
  passed again against the final APK/test APK and produced the exact 360x640,
  density-1, font-scale-1.3, effects-off PNG and semantics artifacts.
- Final `:app:compileDebugKotlin`, all app/core debug unit tests,
  `:app:assembleDebug`, and `git diff --check` passed.

Artifacts:
- `.codex/test-artifacts/2026-09-06-slice-24a-alert-summary-banner-ui/`.

Blockers:
- None. The fresh installed target had no selected location, so installed
  launch captures are first-run evidence; deterministic connected Home fixture
  coverage is the alert acceptance evidence.

Boundaries:
- Existing Now-page overflow scrolling remains an explicit compatibility
  exception used at large font scale for readable content; normal-scale Home
  navigation composition was not changed.
- No alert detail navigation, alert persistence/cache, background polling,
  notifications, provider routing changes, Room alert entities, or live NWS
  request was added.

### 2026-09-06-slice-24b-official-alert-detail-navigation

Status: committed
Mode: feature
Slice: Slice 24B, Official Alert Detail Navigation
Commit: `ceb6253`

Result:
- Added the Home Now official-alert detail route for the active forecast
  session, including first-alert opening, multi-alert selection, in-surface and
  system back handling, and return-to-Home behavior.
- Added the dedicated alert detail Compose screen, mapper output for per-alert
  presentation, and state-holder route preservation through refresh and unit
  remap flows.
- Added focused mapper, state-holder, and connected Home UI tests for the new
  alert detail behavior.
- Updated the repository-facing docs so installed alert detail navigation is
  described as implemented while alert persistence/cache and background alert
  polling remain future work.

Evidence:
- `git diff --check` passed in this session after the implementation and doc
  sync edits.
- The user said the relevant Android tests had already passed and no rerun was
  requested in this session.

Blockers:
- None.

Boundaries:
- `scripts/start-emulator.sh` was left untouched as an unrelated worktree
  change.
- Alert persistence/cache, background polling, notifications, and other future
  alert work remain out of scope.

### 2026-09-06-pre-25a-authority-sync

Status: verified
Mode: documentation-only
Commit state: uncommitted

Result:
- Reconciled roadmap statuses and sequencing through committed Slice 24B and
  selected Slice 25A as the one planned next implementation slice.
- Corrected the live history summary and README contradictions about active MET
  Norway fallback and installed official-alert presentation.
- Updated the specification next-task section and removed the active plan's
  assumption that an emulator was already connected.

Evidence:
- `git diff --check` passed.
- Android compile, unit, connected, and assemble commands were not run because
  this changeset is Markdown-only and changes no production behavior.

Artifacts:
- None.

Blockers:
- None.

Boundaries:
- Gate 25 remains specified and incomplete. Slice 25A remains planned, not
  covered, implemented, verified, or committed.
- No Kotlin, Compose, Gradle, manifest, provider, persistence, permission,
  installed-app, or release behavior changed.

### 2026-09-06-slice-25a-settings-information-architecture

Status: committed
Mode: feature
Commit state: committed
Commit: `2484e90`

Result:
- Replaced the mixed Settings / About root with a coherent Settings root and
  distinct Appearance, Units, Locations, Data Sources, Privacy, Open Source
  Licenses, and About destinations.
- Preserved existing unit and disclosure behavior, routed Locations through the
  real location-entry surface, and preserved exact return routing through
  Settings to Home or first-run state.
- Added a truthful read-only Appearance summary of the effective theme, Standard
  layout, and runtime effects; no appearance preference behavior was added.

Evidence:
- Focused app state-holder tests passed.
- `HomeDashboardUiTest` passed all 39 connected tests, including compact
  360x640/font-scale-1.3 coverage, in-surface Back, and Android Back.
- Final app compile, app/core debug unit tests, debug assemble, and
  `git diff --check` passed.

Artifacts:
- `.codex/test-artifacts/2026-09-06-slice-25a-settings-information-architecture/`.

Blockers:
- None.

Boundaries:
- Gate 25 disclosure auditing remains separate and incomplete.
- Effects Off is covered by the deterministic connected fixture; the installed
  default summary reports its actual Subtle runtime effects and no preference
  selector was introduced.

### 2026-09-06-gate-25-disclosure-baseline-check

Status: committed
Mode: feature and documentation sync
Gate: Gate 25, Disclosure Baseline Check
Implementation commit: `23a9d49`

Result:
- Added active-provider attribution/license/privacy disclosure text and five
  centralized HTTPS attribution links for Open-Meteo forecast/timezone and
  geocoding, GeoNames, MET Norway, and NOAA/NWS.
- Added labelled minimum-48-dp Settings link actions through LocalUriHandler;
  corrected the NWS condition to forecast success and identified Oxygen's
  `GPL-3.0-or-later` source license.
- Corrected new MET Norway provenance to `NLOD-2.0 AND CC-BY-4.0` and
  normalized only that exact legacy `OR` value at Home presentation.
- Reconciled the specification, root DATA_SOURCES disclosure, and active
  provider contracts with the installed behavior and direct Settings hierarchy.

Evidence:
- Focused app/core unit tests passed for disclosure content, Home provenance,
  MET Norway mapper/repository/client, and installed factory behavior.
- The planned two-case connected run passed on `oxygen_starter`, covering the
  disclosure journey, all five links, compact scrolling/semantics, Back,
  unchanged repository call count, no permission request, and the installed
  MET Norway fallback provenance regression.
- One-emulator baseline/final manual route passed at 360x640, density 160,
  font scale 1.3, including Chrome external-link handoff and return. Release
  dependency/manifest/source audits, compile, full unit suites, assemble, and
  `git diff --check` passed.

Artifacts:
- `.codex/test-artifacts/2026-09-06-gate-25-disclosure-baseline-check/`.

Blockers:
- None. Live provider requests, the complete Slice 33 dependency/privacy audit,
  and release-candidate verification remain out of scope.

Boundaries:
- No provider request, selection, fallback eligibility, persistence schema,
  permission, forecast value, alert lookup, location, unit, appearance, or
  navigation behavior changed.

Authority sync:
- Specification section 44 and the obsolete section 53 Units path were
  reconciled before implementation; section 53 and the roadmap now identify
  Slice 26 as the next candidate. This history entry and the active plan were
  synchronized after implementation commit.

### 2026-09-07-slice-26-effects-preference

Status: committed and verified at the focused Android/state boundary
Mode: feature and documentation sync
Slice: Slice 26, Persisted Effects Off/Subtle Baseline
Commit: `c7b578a`
Commit state: committed; unrelated `.codex/review/findings.md` edit retained

Result:
- Added a versioned application-context Preferences DataStore for Off/Subtle
  effects, with missing/unsupported records defaulting to Subtle and local
  read/write failures exposed to the Appearance surface.
- Added guarded pending/confirmed effects state that survives forecast,
  location, unit, and alert presentation reconstruction without new provider
  requests. Added Settings Off/Subtle controls with selected semantics,
  retryable read/write feedback, and no Full alias.
- Added the Android `ValueAnimator.areAnimatorsEnabled()` adapter, lifecycle
  resume sampling, conservative effective Off policy, and immediate pager
  navigation when animators are disabled.
- Updated privacy/about/specification/README status to describe only this
  verified baseline; Full effects and other persisted appearance settings
  remain unfinished.

Evidence:
- Focused app unit tests passed for storage codec/state defaults, read failure
  recovery, failed-write retry, and zero forecast request delta.
- The final seven-case connected filter passed for real DataStore readback and
  Activity recreation, selector/request preservation, first-run write-failure
  retry, injected read-error recovery, actual Android animator override, and
  the planned Settings/Units regressions.
- Installed production checks passed for compact readable Appearance controls,
  Off force-stop persistence, Subtle restoration, animator-scale override,
  restoration at scale 1, and device-setting cleanup.
- Broad checks passed: compileDebugKotlin, app/core debug unit tests, assemble,
  and `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-06-slice-26-effects-preference/`.

Blockers:
- One bounded live manual-search attempt did not reach a real forecast because
  emulator text-entry/tap interaction left the query unchanged. No installed
  Home forecast screenshot or real alert/stale session is claimed; deterministic
  connected evidence covers the changed presentation boundary. TalkBack was
  not run.

Boundaries:
- No Full effects, richer procedural scene behavior, persisted theme/layout/
  icon settings, provider behavior, location permission behavior, or release
  readiness was added.

### 2026-09-07-slice-27a-simple-layout-definition

Status: committed
Mode: feature and documentation sync
Slice: Slice 27A, Simple Layout Definition
Commit: `660e376`
Commit state: implementation committed; post-commit authority sync performed
afterward; unrelated `.codex/review/findings.md` edit retained

Result:
- Added a session-only Simple/Standard layout control to the installed
  Settings / Appearance surface. Standard remains the launch/restart default;
  no layout persistence, migration, or restart restoration was added.
- Simple Home renders `Now -> Forecast`; Forecast exposes Hourly and Daily
  choices. Layout replacement resets to the first page, and Simple forecast
  choice resets to Hourly.
- Existing ready forecast, alert, stale/cache failure, source/update/
  provenance, privacy disclosure, and callback behavior remain reachable where
  supplied. Layout and Hourly/Daily changes do not refetch provider data.
- Standard Home keeps `Now -> Hourly -> Daily -> Details` and its page
  navigation/accessibility behavior.

Evidence:
- Baseline installed capture retained the truthful first-run/no-ready state;
  no installed ready Standard or Simple live-provider journey is claimed.
- Red connected attempt observed missing Simple Settings controls and Simple
  page semantics after test scaffolding correction.
- Final focused connected filter passed six cases on `oxygen_starter`:
  Simple production reachability, layout replacement/reset/request-count
  preservation, sparse/operational honesty, compact large-font effects-off
  readability, and two Standard page/navigation regressions.
- Artifact rerun passed one connected case after reinstalling the debug and
  androidTest APKs so app-private screenshot/semantics files could be pulled.
- Broad checks passed: compileDebugKotlin, app/core debug unit tests,
  assembleDebug, and `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/`.

Blockers/skips:
- TalkBack service-level traversal was not run because TalkBack was installed
  but disabled on the emulator (`accessibility_enabled=0`,
  `enabled_accessibility_services=null`). Gate 30 traversal remains
  unverified; this slice verified semantics-tree order, labels, selected
  state, custom actions, and target sizes.

Boundaries:
- No persisted layout selection/restoration, Detailed or Meteorologist layout,
  theme/icon settings, Full effects, provider/repository/cache/location/unit
  behavior, new weather values, release readiness, or MVP-complete claim was
  added.

### 2026-09-08-slice-27b-persisted-layout-selection

Status: committed for 27B1/27B2; 27B3 installed force-stop verification remains planned
Mode: feature
Slice: Slice 27B1/27B2, Layout Preference Storage and Settings Transaction UI
Commit: `b68ca19`
Commit state: implementation committed; post-commit authority sync performed
afterward; unrelated `.codex/review/findings.md` edit retained

Result:
- Added versioned DataStore-backed Simple/Standard layout preference storage
  with `oxygen_layout_preferences`, `layout_preference_version`, and
  `layout_preference_value`.
- Unsupported or malformed layout records resolve to `NoSupportedChoice`
  without aliasing Detailed/Meteorologist or rewriting storage.
- Wired managed layout state through `MainActivity`, `OxygenAppStateHolder`,
  `OxygenApp`, and Settings / Appearance with loading, saved, pending, failed,
  and retry states.
- Preserved forecast, alert, selected-location, unit, effects, source,
  provenance, and ready dashboard presentation across layout read/write
  transitions. Layout selection does not refetch provider data.
- Corrected compact Appearance layout controls so selected/disabled semantics
  and 48dp targets are observable at 360x640/font-scale-1.3.

Evidence:
- The required pre-implementation red phase was missed because the candidate
  implementation and tests already existed in the worktree; the ledger records
  this rather than claiming a fabricated red result.
- Focused unit filter passed for `LayoutPreferenceStorageTest`,
  `LayoutPreferenceStateHolderTest`, and `OxygenAppContractTest`.
- Targeted connected filter passed three cases on `oxygen_starter`: production
  DataStore readback through recreated app state, Settings commit/no-refetch,
  and read/write failure retry with compact target checks.
- Broad checks passed: `:app:compileDebugKotlin`, app/core debug unit tests,
  `:app:assembleDebug`, and `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-08-slice-27b-persisted-layout-selection/`.

Blockers/skips:
- A separate ADB-driven installed-app force-stop/relaunch journey was not run
  before this commit. Slice 27B3 remains planned for that evidence.

Boundaries:
- No Detailed or Meteorologist layout, theme/icon settings, Full effects,
  provider/repository/cache/location/unit behavior, release readiness, or
  MVP-complete claim was added.
### 2026-09-08-slice-27b3-installed-layout-restoration

Status: verified
Mode: installed verification and documentation sync
Slice: Slice 27B3, Installed Layout Restoration Verification

Result:
- Established a ready Chicago Home through the production first-run search and
  forecast path on one pinned `oxygen_starter` emulator at 360x640 and font
  scale 1.3, with Effects Off selected through Settings / Appearance.
- Simple restored as `Now, Page 1 of 2` after explicit Activity recreation and
  as the first ready page after force-stop/relaunch; no earlier sampled ready
  Standard page appeared before restored Simple.
- Standard restored as `Now, Page 1 of 4` after Activity recreation and as the
  first ready page after force-stop/relaunch. Hourly, Daily, and Details were
  usable; both restored Settings states showed the saved layout and Effects
  Off selected.

Evidence:
- `.codex/test-artifacts/2026-09-08-slice-27b3-installed-layout-restoration/`.
- Focused `LayoutPreferenceDataStoreInstrumentedTest` connected check passed
  1/1, 0 skipped, 0 failed on `oxygen_starter`.
- `:app:assembleDebug` and `git diff --check` passed.

Boundaries:
- No production code, provider behavior, new layout type, theme/icon/effects
  implementation, release-readiness, or MVP claim was added.
- The connected-test harness removed the app during cleanup; the already-built
  APK was reinstalled and the production first-run path was repeated only to
  leave the emulator on Standard Home. The acceptance captures precede that
  cleanup and remain the source of restoration evidence.

### 2026-09-08-slice-28a1-paper-theme-rendering-baseline

Status: committed
Mode: bounded visual implementation
Slice: Slice 28A1, Paper Theme Rendering Baseline
Commit: `06c987b`

Result:
- Made Paper a deliberate warm, opaque Home translation with serif display and
  heading typography, restrained outlines, readable supporting-content roles,
  and Paper warning emphasis for official-alert severity.
- Added direct Home theme injection and a named compact OxygenApp fixture for
  Paper rendering, semantic invariance, and no-refetch checks.
- Preserved Oxygen/Terminal theme paths, layout/page semantics, weather values,
  source/provenance, callbacks, and Effects-Off scene absence. Added 48dp
  loading/error Home actions where the compact boundary exposed a gap.
- Corrected README's stale claim that layout selection/restoration remained
  unimplemented; theme persistence remains out of scope.

Evidence:
- Final six-case connected filter passed on `oxygen_starter` / `emulator-5554`:
  Paper baseline, Standard, Simple, operational states, Oxygen Effects Off,
  and Terminal mark smoke. The final Standard oracle rerun also passed after
  the actual Paper warning-surface pair was finalized.
- Named opaque Paper normal/supporting/warning role pairs passed WCAG 4.5:1;
  bitmap mark sampling passed with 791 measured pixels and sampled coordinates
  recorded in `paper-mark-sampling.txt`.
- Broad checks passed: `:app:compileDebugKotlin`, app/core debug unit tests,
  `:app:assembleDebug`, and `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-08-slice-28a1-paper-theme-rendering-baseline/`.

Boundaries:
- No theme storage, Settings selection, MainActivity theme reachability,
  provider/cache/location/unit/alert semantics, new assets/dependencies,
  layout/effects persistence, or release/MVP claim was added.

### 2026-09-09-slice-28a2-terminal-theme-rendering-baseline

Status: committed
Mode: bounded visual implementation
Slice: Slice 28A2, Terminal Theme Rendering Baseline
Commit: `80dd961`

Result:
- Implemented Terminal as an explicit non-persisted dark translation with
  opaque surfaces, readable warning/supporting roles, monospace typography,
  green/cyan weather marks, and compact corners.
- Replaced the Terminal gold-mark smoke with four connected acceptance cases
  covering Standard semantic invariance, Simple forecast choices/no-refetch,
  operational states, and representative weather marks.

Evidence:
- Focused connected suite passed 6/6 on `oxygen_starter` / `emulator-5554`.
- Broad checks passed: `:app:compileDebugKotlin`, app/core debug unit tests,
  `:app:assembleDebug`, and `git diff --check`.
- Logs retained under `.codex/test-artifacts/2026-09-08-slice-28a2-terminal-theme-rendering-baseline/`.

Blockers/skips:
- Pre-change Terminal baseline was not captured before the initial production
  edit; this gap is recorded rather than backfilled.
- No repository CI workflow exists; local Gradle checks are the CI-equivalent.

Boundaries:
- No theme persistence, Settings selection, MainActivity reachability, provider,
  layout, effects, accessibility semantics, release, or MVP behavior changed.

### 2026-09-09-slice-28b2-persisted-theme-settings-ui

Status: committed
Mode: bounded settings integration and installed restoration verification
Slice: Slice 28B2, Persisted Theme Settings UI
Commit: `2c88b9c`

Result:
- Wired the existing `DataStoreThemePreferenceStorage` into `MainActivity` and
  passed confirmed theme state and retry/selection events through `OxygenApp`.
- Added managed Appearance controls for Oxygen, Paper, and Terminal with
  confirmed-only selected semantics, 48dp vertical targets, disabled pending/
  failure states, truthful save/load/error copy, and retained-target retry.
- Updated the production-DataStore state-holder recreation test to select Paper
  through the Appearance control.

Evidence:
- Focused JVM theme storage/state tests passed before and after implementation.
- Connected `ThemePreferenceUiTest` passed 2 cases and
  `ThemePreferenceDataStoreInstrumentedTest` passed 1 case on
  `oxygen_starter` / `emulator-5554`.
- Installed 1080x2400, font-scale-1.3 evidence with Effects Off rendered and
  selected Paper and Terminal in Appearance. Paper survived relaunch before a
  new selection; Terminal survived rotation and force-stop/relaunch in
  Settings. Rotation settings were restored.
- Broad `:app:compileDebugKotlin`, app/core debug unit tests,
  `:app:assembleDebug`, and `git diff --check` passed.

Artifacts:
- `.codex/test-artifacts/2026-09-09-slice-28b2-persisted-theme-settings-ui/`.

Blockers/skips:
- The installed Activity returned to first-run location entry after rotation
  and relaunch, so no post-relaunch Home forecast screenshot is claimed. No
  location, provider result, sample data, or app-private theme data was seeded.
- A separate pre-production red connected run was not recorded; the new UI
  test was compiled with the production wiring in the same edit.

Boundaries:
- No DataStore contract/state-machine redesign, provider/cache/location/unit/
  alert behavior, new theme, effects/layout behavior, or release claim changed.
