# Active Cycle

Status: committed
Cycle ID: 2026-08-29-offline-launch-from-last-forecast
Mode: feature
Goal: Restore the selected location's last persisted forecast on installed-app launch and keep useful cached Home data visible when refresh fails.
Roadmap slice: Slice 18: Offline Launch From Last Forecast.
Branch or work context: local `oxygen` Android scaffold after committed Persistence Architecture Gate (`afda71a Add Room forecast persistence boundary`).

Specification anchors:
- `AGENTS.md`
- `README.md`
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `.codex/plans/mvp-roadmap.md`
- `.codex/cycles/history.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `core/build.gradle.kts`
- `scripts/android-env.sh`
- `app/src/main/`
- `app/src/test/`
- `app/src/androidTest/`
- `core/src/main/`
- `core/src/test/`
- `core/src/androidTest/`

Prerequisites:
- Repository Engineering Gate is committed.
- Slice 17B Explicit Home Refresh Control is committed.
- Slice 17C Home Presentation Accessibility Evidence Baseline is committed.
- Persistence Architecture Gate is committed and introduced `RoomForecastCacheStorage`, `RoomForecastCacheStorageFactory`, and the production Room-backed `ForecastCacheStorage` construction path.
- Repository-level stale retention exists through `CachedWeatherRepository`, but the installed app still does not wire durable forecast cache restoration on launch.

Selected behavior:
- Oxygen persists the last selected provider-neutral `WeatherLocation` snapshot through DataStore or an equivalent Android small-state persistence boundary. The snapshot includes the local `LocationId`, display name, latitude, longitude, IANA timezone, and optional elevation.
- On installed-app startup, Oxygen restores the last selected `WeatherLocation` snapshot, reads the matching Room-backed forecast cache, renders Home with cached current/hourly/daily forecast data when available, shows explicit stale age/source/update metadata, and starts a refresh for the same selected location.
- If startup or foreground refresh fails while useful same-location cached data exists, Home keeps the stale forecast visible with retry/refresh-failure metadata.
- If no selected location or no useful same-location forecast cache exists, Home does not fabricate a default forecast: no selected location routes to first-run manual selection, and selected-location/no-cache launch shows a provider-neutral retryable error.
- On successful refresh, the installed app replaces the persisted Room forecast data and renders the fresh provider result for the selected location.
- Installed-app Home state uses a lifecycle-aware boundary suitable for Room/DataStore collection, cancellation, process recreation, and repository refresh. If ViewModel, Flow, or new coroutine scopes are introduced, this cycle verifies the lifecycle behavior it claims.

Acceptance criteria:
- Production startup has an explicit cache-first boundary. Startup cache restoration must not depend on a provider refresh failing first. The app either introduces an app-layer coordinator that composes selected-location storage, `ForecastCacheStorage`, and `WeatherRepository`, or extends the repository boundary with an explicit startup/read-through API before implementation claims cache-first launch.
- A production Android small-state persistence boundary stores and restores the last selected provider-neutral `WeatherLocation` snapshot; DataStore is preferred because it is named in the specification, but an alternative must be explicitly justified before implementation.
- The persisted selected-location snapshot preserves exactly the fields needed to render Home context, scope Room cache lookup, and refresh forecasts without a geocoding network lookup: local `LocationId`, display name, latitude, longitude, IANA timezone, and optional elevation.
- The selected-location small state is not used as the canonical forecast database, does not duplicate normalized forecast payloads already owned by Room, and does not persist provider geocoding IDs, provider DTO fields, country/admin/postcode metadata, feature code, population, or saved-location list state.
- Manual selection persists the selected-location snapshot before claiming a durable Home handoff. If selected-location persistence fails, the app remains on the manual-selection boundary with a provider-neutral local-state error and does not route to Home as though offline launch is established.
- Production app wiring constructs the Room-backed forecast cache through the existing production factory or an equivalent production path, not a test-only or file-backed cache path.
- The installed app's default forecast repository path uses durable Room cache behavior for selected-location Home startup and refresh.
- Startup restores the selected `WeatherLocation` snapshot before reading Room forecast cache or attempting refresh. The restored location is the single selected-location identity used for the Home header, same-location Room cache lookup, no-cache error context, and provider refresh.
- Startup restores the matching persisted forecast cache for the restored local `LocationId` only; cached data for another location must not satisfy Home.
- Startup attempts a provider refresh after local restoration. Refresh success replaces persisted forecast data; refresh failure retains useful cached data with stale age and refresh-failure metadata.
- A useful cache for this slice is a provider-neutral `WeatherBundle` whose location id matches the restored selected `LocationId`, contains renderable current conditions, at least one hourly row, at least one daily row, source/provenance sufficient for disclosure, and timestamps sufficient for update/stale-age display. Missing, corrupt, wrong-location, or display-incomplete cache data is treated as no cache.
- Offline/network failure with useful cache renders Home cached current/hourly/daily data, source/provenance/update text, and explicit stale age.
- Offline/network failure without cache renders a retryable provider-neutral error and does not show sample/scaffold data or fabricated weather.
- First-run behavior remains intact when no selected location is stored.
- Manual search and selected-location handoff update the persisted selected `WeatherLocation` snapshot at the production app boundary.
- Local DataStore or Room failures map to provider-neutral error states and do not fabricate success.
- Lifecycle behavior is observable: startup restoration, cancellation/obsolete refresh isolation, and process/activity recreation behavior are covered at the Android state, Compose, or instrumentation boundary.
- Provider-neutral boundaries remain intact: Open-Meteo DTOs, MET Norway DTOs, provider IDs as user-facing location identity, provider-specific errors, Room entities, and DataStore implementation details do not cross into Composables.
- `SampleWeather.bundle` remains scaffold/preview-only and is not used by the production startup, cache, or Home success path.
- UI obligations travel with the behavior: cached and stale Home states remain readable at compact width and large font, expose meaningful semantics, and keep refresh/retry controls stable.
- Focused tests cover online launch with no cache, online launch with cache, offline launch with useful cache, offline launch without cache, failed foreground refresh with cache, failed foreground refresh without cache, selected `WeatherLocation` snapshot persistence/readback, same-location cache scoping, persisted selected-location handoff, lifecycle/obsolete-refresh isolation, local persistence failure mapping, and no sample/provider-detail leakage.
- Required completion tests cover selected-location snapshot write/read/default-empty behavior; corrupt/incomplete snapshot handling; manual selection persistence before Home route; selected-location write failure blocking durable Home route; startup with selected location plus matching cache rendering stale cached Home before or while refresh starts; startup with selected location plus wrong/no cache rendering retryable no-cache state; refresh success replacing persisted Room cache and UI; refresh failure with cache retaining stale UI; refresh failure without cache remaining retryable; obsolete refresh isolation; and `SampleWeather` absence from production startup/Home success paths. Supporting regression tests cover compact width, large font, sibling non-overlap, provider DTO/DataStore/Room leak checks, and existing first-run/search/retry behavior.
- Real-path evidence exercises the installed app or an explicitly labeled Android-boundary harness for restored cached Home rendering and no-cache retryable launch behavior. Offline evidence must be deterministic: either emulator network control with recorded commands/logs or an Android-boundary harness that uses real Room/DataStore storage and a fake weather repository returning `ForecastError.NetworkUnavailable`. A provider mock alone is not evidence of persistence unless the production persistence path is exercised.
- Broad Android verification passes.

Acceptance boundary:
- Slice 18 is complete when the production installed-app path persists the last selected provider-neutral `WeatherLocation` snapshot, restores that selected location and its matching Room-backed forecast cache on launch, renders cached Home data with stale/source/update metadata when network refresh fails, renders retryable no-cache errors for the restored selected location without fabrication, refreshes with the restored location and replaces persisted data on success, preserves first-run manual selection when no selected location exists, and verifies lifecycle-aware startup/refresh behavior at an Android-observable boundary. This slice proves offline launch only for the selected-location current/hourly/daily forecast path. It does not claim saved-location list switching, unit preferences, official alert lookup/cache behavior, air-quality lookup/cache behavior, radar, background refresh, installed-app MET Norway fallback activation, provider-specific fallback cache metadata, persisted presentation settings, release readiness, or MVP readiness.

Evidence plan:
- Save focused state/repository/DataStore/Room test logs under `.codex/test-artifacts/2026-08-29-offline-launch-from-last-forecast/`.
- Save Android-boundary or emulator logs, screenshots, semantics dumps, and any offline/network-control notes under the same artifact directory.
- Save broad verification logs under the same artifact directory.
- Append completed cycle evidence to `.codex/cycles/history.md` only when the slice is ready or committed.

## Implementation Plan

### Planning Decisions

- Startup/cache coordinator: introduce or identify one production app-layer boundary responsible for selected-location restore, Room cache read, refresh launch, refresh replacement, and no-cache error emission. The initial cached Home state must come from local storage, not from waiting for `CachedWeatherRepository.refresh()` to fail.
- Small-state persistence: use DataStore in `:app` for the last selected provider-neutral `WeatherLocation` snapshot: local `LocationId`, display name, latitude, longitude, IANA timezone, and optional elevation. Do not store forecast rows, provider geocoding IDs, provider DTO fields, country/admin/postcode metadata, feature code, population, or saved-location list state in this slice.
- Forecast persistence: use the Room-backed `ForecastCacheStorage` from the Persistence Architecture Gate as the installed-app durable forecast cache. Do not extend `FileForecastCacheStorage` for production offline launch.
- Startup model: keep no-selected-location startup routed to first-run manual selection. When a selected location is restored, Home should render cached same-location data first if available, then attempt a refresh for the same selected location.
- Lifecycle boundary: settle one concrete owner before behavior coding. Preferred implementation is an `OxygenAppViewModel` in `:app` that owns startup restore, manual selection persistence, refresh jobs, obsolete-result isolation, and lifecycle cancellation. If a smaller state-holder boundary is retained instead, the plan must record how activity recreation, process recreation inputs, and in-flight refresh isolation are observed without overstating lifecycle guarantees.
- Dependency gate: add only dependencies needed for this slice's real behavior. Expected candidates are `androidx.datastore:datastore-preferences` for selected-location small state, `androidx.lifecycle:lifecycle-viewmodel-compose` if Compose obtains a ViewModel, and coroutine dependencies only if the chosen lifecycle owner uses coroutine jobs. Do not add dependencies for future saved locations, unit preferences, alerts, background refresh, or persisted presentation settings.
- Persistence failure UX: selected-location write failure during manual candidate selection keeps the user at the manual-selection boundary with a provider-neutral local-state error and retry/select-again behavior. It does not route to Home or claim durable offline launch.
- Useful-cache rule: a cache hit is renderable only when the restored local `LocationId` matches and cached provider-neutral data can render current conditions, at least one hourly row, at least one daily row, source/provenance disclosure, and update/stale-age text without fabrication. All other local-cache read outcomes are no-cache or local-persistence errors.
- Offline scope: prove selected-location forecast restoration only. Saved-location lists, switching between multiple saved places, provider fallback metadata, alerts, air quality, and background work remain deferred.
- UI scope: reuse the existing Home dashboard/stale/error presentation where possible. Add UI changes only when needed to make launch restoration, stale age, refresh-failure metadata, or retryable no-cache behavior observable and accessible.

### Phase Plan

0. Discovery and baseline
   - Inspect the installed-app wiring around `OxygenApp`, app state holder construction, manual selection, Home refresh, current `WeatherRepository` construction, Room cache factory, and existing cache/stale tests.
   - Inspect whether DataStore, lifecycle ViewModel, and coroutine dependencies already exist; if not, identify exact minimal dependency placement and the behavior each dependency enables before editing Gradle files.
   - Decide and record the startup/cache coordinator boundary and lifecycle owner before implementation. Resolve whether `OxygenAppStateHolder` remains a pure reducer behind a ViewModel, is replaced by a ViewModel-owned state stream, or is retained with a narrower explicitly tested lifecycle claim.
   - Run baseline focused checks before behavior changes:
     - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*FirstRun*' --tests '*HomeHandoff*' --tests '*HomeForecast*' --tests '*OxygenApp*'`
     - `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*CachedWeatherRepositoryTest'`
     - `. scripts/android-env.sh && ./gradlew :core:connectedDebugAndroidTest`
   - Save logs under `.codex/test-artifacts/2026-08-29-offline-launch-from-last-forecast/`.

1. Contract tests for selected-location small state
   - Add focused tests for default no-selected-location state, persisted selected `WeatherLocation` snapshot write on manual candidate selection, persisted selected-location read on app startup, invalid/corrupt/incomplete stored snapshot handling, and local persistence failure mapping.
   - Ensure persisted snapshot tests prove exact local `LocationId`, display name, coordinates, timezone, and optional elevation read back, and that provider IDs are not used as the user-facing selected `LocationId`.
   - Add tests proving manual candidate selection persists the selected-location snapshot before routing Home and that selected-location persistence failure leaves the user at the manual-selection boundary with a provider-neutral local-state error.
   - Ensure online launch with selected location and no cache attempts refresh using the restored coordinates/timezone, not a default location.
   - Ensure offline/no-cache launch shows retryable error for the restored selected location without fabricated weather.
   - Capture an intentional red log when practical before implementation.

2. Installed-app Room cache wiring
   - Wire the production app forecast path to construct `RoomForecastCacheStorage` through Android `Context`.
   - Wrap the active forecast repository in `CachedWeatherRepository` for installed-app selected-location Home refresh.
   - Keep provider fallback activation unchanged unless already active before this slice; do not introduce MET Norway installed-app fallback behavior here.
   - Add tests that prove the installed-app path no longer uses the file-backed cache or scaffold data for Home startup.

3. Startup restoration flow
   - Implement startup behavior through the selected cache-first coordinator: restore the persisted selected `WeatherLocation` snapshot, read same-location forecast cache directly from the production Room-backed `ForecastCacheStorage`, and emit Home cached/stale state before or while refresh is attempted.
   - Preserve first-run manual selection when no selected location exists.
   - Ensure cache miss for a restored selected location produces a retryable no-cache state instead of sample/default weather.
   - Ensure wrong-location, corrupt, or display-incomplete cache rows are ignored or mapped to provider-neutral local-persistence errors according to the useful-cache rule.

4. Refresh replacement and stale retention
   - Verify refresh success writes replacement forecast data to Room and renders fresh Home state.
   - Verify network/provider refresh failure with cache retains stale cached Home data with explicit stale age and refresh-failure metadata.
   - Verify refresh failure without cache remains a retryable provider-neutral error.
   - Verify obsolete refresh results cannot replace a newer selected-location state.

5. Lifecycle and process recreation evidence
   - If `OxygenAppViewModel` is introduced, wire `OxygenApp`/`MainActivity` through the ViewModel and test activity recreation using persisted selected-location and forecast cache inputs. Verify ViewModel-owned jobs isolate obsolete refresh results and cancel on clear where observable.
   - If a ViewModel is not introduced, record the narrower lifecycle claim before implementation and add Android-boundary coverage for the exact state holder behavior being claimed.
   - Add focused lifecycle tests or Android-boundary harness coverage for app/activity recreation after selected-location and forecast cache persistence.
   - Exercise cancellation or obsolete refresh isolation when the restored selected location changes or a new manual selection supersedes startup refresh.
   - If ViewModel/Flow/coroutine lifecycle changes are introduced, test the specific lifecycle behavior claimed.

6. Compose and accessibility boundary
   - Add or update Compose tests for cached stale Home launch, no-cache retryable launch, refresh-in-progress cached state, refresh success replacement, compact width, large font, stable refresh/retry controls, semantics labels, and sibling non-overlap.
   - Keep provider DTOs, Room entities, DataStore keys, and provider-specific errors out of Composables and presentation state.

7. Real-path exercise
   - Use the repo-local emulator to install and launch the debug app.
   - Exercise at least one installed-app or Android-boundary flow that persists a selected location and forecast through real DataStore/Room production storage, relaunches/restores Home from cache, and records stale/source/update metadata.
   - Exercise a no-cache selected-location launch or equivalent Android-boundary harness that uses real selected-location storage with no matching Room cache and shows retryable no-cache behavior.
   - Make offline behavior deterministic by recording emulator network-control commands/logs or by using an Android-boundary harness with real Room/DataStore storage and a fake weather repository that returns `ForecastError.NetworkUnavailable`.
   - Save screenshots, semantics dumps, and logs under `.codex/test-artifacts/2026-08-29-offline-launch-from-last-forecast/`.

8. Static boundary checks
   - Check that `SampleWeather` remains preview/scaffold-only and is absent from production startup/cache/Home success paths.
   - Check that DataStore and Room implementation details do not leak into Composables.
   - Check that Open-Meteo/MET Norway DTOs and provider-specific errors do not leak into app UI, DataStore small state, or Home presentation.
   - Check that `FileForecastCacheStorage` is not used for installed-app offline launch.

9. Broad verification
   - `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
   - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
   - `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
   - `git diff --check`

10. Review and ready state
   - Review the diff for scope, lifecycle behavior, provider-neutral boundary preservation, persistence failure handling, UI accessibility, and claim discipline.
   - Update Phase Results with commands actually run and artifact paths.
   - Append completed cycle evidence to `.codex/cycles/history.md` only when the slice is ready or committed.

## Phase Results

- planned: Selected Slice 18 after committed Persistence Architecture Gate.
- specified: Roadmap Slice 18 requires selected-location small-state persistence, last selected location and forecast load from local storage, stale cached Home rendering on network failure, retryable no-cache launch, startup refresh/replacement behavior, and lifecycle-aware installed-app Home state.
- implemented: Added DataStore-backed selected-location snapshot persistence in `:app` for local `LocationId`, display name, latitude, longitude, IANA timezone, and optional elevation. Manual candidate selection writes this snapshot before routing Home; write failure keeps the app at manual selection with a provider-neutral local-state error.
- implemented: Wired installed-app startup in `MainActivity` to construct `DataStoreSelectedLocationStorage`, production `RoomForecastCacheStorageFactory`, and `CachedWeatherRepository(OpenMeteoWeatherRepository, RoomForecastCacheStorage)`. MET Norway installed-app fallback remains inactive.
- implemented: `OxygenAppStateHolder` now restores selected-location state on its forecast executor, reads same-location Room cache before refresh, renders useful cached current/hourly/daily Home data with cache age/source/update metadata, starts refresh for the restored location, maps no-cache/offline launch to a retryable Home error, and preserves obsolete refresh isolation.
- covered: Focused JVM state-holder and static contract tests cover manual selected-location persistence before Home route, selected-location write/read failure mapping, startup selected-location restore with matching useful cache, no-cache offline launch, wrong-location cache isolation, production DataStore/Room cache wiring, and no sample/file-cache production path.
- covered: Android-boundary instrumentation `OfflineLaunchPersistenceInstrumentedTest` uses real production DataStore selected-location storage and Room forecast cache storage with deterministic `ForecastError.NetworkUnavailable` to cover selected-location snapshot write/read, cached offline launch with stale Home, and selected-location/no-cache retryable launch.
- verified: Baseline app focused tests passed; log saved at `.codex/test-artifacts/2026-08-29-offline-launch-from-last-forecast/baseline-app-focused.log`.
- verified: Baseline core cache tests passed; log saved at `.codex/test-artifacts/2026-08-29-offline-launch-from-last-forecast/baseline-core-cache.log`.
- verified: Baseline core Room instrumentation passed on `oxygen_starter(AVD) - 17`; log saved at `.codex/test-artifacts/2026-08-29-offline-launch-from-last-forecast/baseline-core-room-instrumentation.log`.
- verified: Focused app unit tests passed from final source state; log saved at `.codex/test-artifacts/2026-08-29-offline-launch-from-last-forecast/focused-app-final.log`.
- verified: Focused app offline-launch instrumentation passed on `oxygen_starter(AVD) - 17`; log saved at `.codex/test-artifacts/2026-08-29-offline-launch-from-last-forecast/focused-app-offline-launch-instrumentation-r3.log`.
- verified: Existing Home Compose instrumentation passed on `oxygen_starter(AVD) - 17`; log saved at `.codex/test-artifacts/2026-08-29-offline-launch-from-last-forecast/home-compose-instrumentation.log`.
- verified: Static production-boundary checks for `SampleWeather`, provider DTO/details, Room/DataStore UI leakage, and `FileForecastCacheStorage` usage returned no production matches; logs saved under `.codex/test-artifacts/2026-08-29-offline-launch-from-last-forecast/static-*.log`.
- verified: Required broad checks passed from final source state: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`, `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`, `. scripts/android-env.sh && ./gradlew :app:assembleDebug`, and `git diff --check`. Final logs saved as `broad-compile-debug-kotlin-final.log`, `broad-debug-unit-tests-final.log`, `broad-assemble-debug-final.log`, and `git-diff-check-final.log`.
- not claimed: Saved-location list switching, unit preferences, official alerts, air quality, radar, background refresh, installed-app MET Norway fallback activation, provider-specific fallback cache metadata, persisted presentation settings, release readiness, and MVP readiness remain outside this slice.
