# Active Cycle

Status: committed
Cycle ID: 2026-08-20-manual-search-results-selection
Mode: feature
Goal: Wire first-run manual search to the production Open-Meteo geocoding repository, render selectable provider-neutral location results, and keep selection observable without routing Home yet.
Roadmap slice: Slice 9A: Manual Search Results Selection from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 10, 12, 14, 19, and 50
- `.codex/plans/mvp-roadmap.md` Slice 9A, with Slice 9B treated as a later boundary
- `docs/data-sources/OPEN_METEO_GEOCODING.md`
- `AGENTS.md`

Acceptance criteria:
- Submitting a non-empty manual query from the production `OxygenApp` state holder calls the production `GeocodingRepository` path; default production construction uses `OpenMeteoGeocodingRepository`.
- Search presentation state exposes loading, success results, empty, network/offline, rate-limited, provider-unavailable, invalid-response, rejected-request, and unexpected-failure states without provider DTOs or provider IDs crossing into app UI state or Composables.
- Results render enough provider-neutral disambiguation for long or similar names: display name, administrative areas/country, coordinates, and timezone.
- Selecting a result produces the exact provider-neutral `WeatherLocation` with stable local `LocationId`, records it in app state, and does not route Home, fetch forecasts, persist saved locations, or use sample weather.
- Empty and failure states retain the submitted query and expose a retry action where applicable. Retry reuses the retained query and calls the repository again.
- Manual search never emits a location-permission command; `Use my location` remains the only path that emits `RequestLocationPermission`.
- The active search surface includes minimal Open-Meteo/GeoNames attribution and privacy copy before live geocoding is used.
- Default `MainActivity -> OxygenApp()` still starts on first-run manual selection with no hidden default, scaffold, sample, or selected location.

Acceptance boundary: Slice 9A is complete when focused app tests prove the production app state holder consumes a `GeocodingRepository`, maps repository loading/success/empty/failure states to provider-neutral app presentation state, renders retryable visible failures without exposing provider DTOs or provider IDs, selects a provider-neutral `WeatherLocation` exactly from a candidate, and does not route Home or use sample weather. Real-path evidence must exercise a live Open-Meteo geocoding query from the same default production repository path used by `OxygenApp`. This slice does not implement selected-location handoff to Home, Home loading, forecast fetching, saved-location persistence, Android OS location permission dialogs, device-location lookup, debounce/cancel behavior, or offline cache.

Boundary decisions:
- Use constructor injection for `GeocodingRepository` so production defaults to `OpenMeteoGeocodingRepository` and focused tests use deterministic fakes.
- Keep app-layer candidate/presentation types provider-neutral. They may hold `WeatherLocation`; they must not import Open-Meteo DTOs or expose provider IDs.
- Keep asynchronous live search out of the main thread. The state holder publishes state changes to `OxygenApp`; tests may use a direct executor.
- Selection is observable app state only in Slice 9A. Routing Home starts in Slice 9B.
- Add only minimal disclosure copy needed for active geocoding; broader Data Sources UI remains out of scope.

In scope:
- App state holder search states, repository execution, retry, result selection, and selected-location recording.
- First-run Compose UI for loading, result list, empty and failure states, retry, selection, result disambiguation, and geocoding disclosure.
- Focused app JVM tests at the state-holder boundary and static contract checks for no provider DTO/provider ID exposure in app UI state/Composables.
- Live Open-Meteo real-path command or small JVM exercise through the default production repository path.

Out of scope:
- Navigation library, ViewModel, DataStore, Room, persistence, Home routing/loading, weather forecast fetch, selected-location handoff to Home, Android permission launcher/manifest changes, current-device-location lookup, debounce/cancel/autocomplete, result caching, and broad in-app data-source settings screens.

Focused review command or procedure:
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*ManualLocation*' --tests '*OxygenApp*'`
- Static no-provider-leak check: `rg "OpenMeteoGeocodingDto|OpenMeteoGeocodingResult|providerId" app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt app/src/main/kotlin/com/oxygen/weather/app/ui/firstrun/FirstRunLocationEntryScreen.kt`
- Static no-sample production-path check: `rg "SampleWeather|SampleWeather\\.bundle" app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt app/src/main/kotlin/com/oxygen/weather/MainActivity.kt`

Real-path command or procedure:
- Execute a live Open-Meteo geocoding query through the default `OpenMeteoGeocodingRepository` path and record loading plus a terminal result for a bounded query. Save logs under `.codex/test-artifacts/2026-08-20-manual-search-results-selection/`.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Current gate: ready
Current phase: committed
Last result: Slice 9A manual search results selection is covered, implemented, verified, and ready for local version-control history in `:app`. Default production `OxygenApp` state now uses a live `OpenMeteoGeocodingRepository` path for manual search, renders provider-neutral loading/success/empty/failure states with retry, disambiguates results with admin/country/coordinates/timezone, records selected provider-neutral `WeatherLocation` values without routing Home, and keeps sample weather plus Open-Meteo DTO/provider IDs out of the production app UI boundary.
Blocker: none

## Implementation Plan

1. Add focused app tests for repository-backed loading, success, result disambiguation, selection without Home routing, empty/failure visibility, retry, no permission command on search, and static no-provider/no-sample leakage.
2. Replace the Slice 9 "search not connected" state with provider-neutral manual search presentation state and selectable candidate presentation values.
3. Inject `GeocodingRepository` into `OxygenAppStateHolder`, defaulting to `OpenMeteoGeocodingRepository`, and execute searches off the main thread while publishing state changes.
4. Update `FirstRunLocationEntryScreen` to render loading, results, empty, failure/retry, selection acknowledgment, and Open-Meteo/GeoNames attribution/privacy copy.
5. Run focused tests, static checks, a live Open-Meteo real-path query, broad Android checks, and `git diff --check`.
6. Review scope and evidence, then update phase results and append cycle evidence to `.codex/cycles/history.md` when ready.

## Phase Results

- planned: Selected Slice 9A from `.codex/plans/mvp-roadmap.md`. Planned scope is limited to live repository-backed first-run manual search, provider-neutral result rendering and selection, retryable visible search states, and evidence that provider DTOs/provider IDs/sample weather do not cross into app presentation or production routes. Slice 9B Home handoff, forecast loading, persistence, device-location lookup, permission launcher integration, debounce/cancel, and result caching are out of scope.
- covered: Added focused app JVM tests for repository-backed manual search success, retained trimmed query, visible empty state, retryable network failure and retained-query retry, visible provider-neutral mappings for every current `GeocodingError`, exact candidate selection to `WeatherLocation` without Home routing, permission command isolation, first-run geocoding disclosure copy, and static absence of Open-Meteo DTO/provider-ID leakage from `OxygenApp`, `OxygenAppStateHolder`, and `FirstRunLocationEntryScreen`.
- implemented: Injected `GeocodingRepository` into `OxygenAppStateHolder` with `OpenMeteoGeocodingRepository` as the default production dependency; added provider-neutral manual search state and candidate presentation models; execute search work off the main thread and publish state back to `OxygenApp`; render loading/results/empty/failure/retry states, selectable result rows, selected-location acknowledgement, Open-Meteo/GeoNames attribution, and typed-search privacy copy in `FirstRunLocationEntryScreen`.
- verified: Focused command `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*ManualLocation*' --tests '*OxygenApp*'` passed; log saved at `.codex/test-artifacts/2026-08-20-manual-search-results-selection/focused-app-tests.log`. Static no-provider-leak and no-sample production-path checks returned no matches; logs saved at `.codex/test-artifacts/2026-08-20-manual-search-results-selection/static-no-provider-leak.log` and `.codex/test-artifacts/2026-08-20-manual-search-results-selection/static-no-sample-production-path.log`. Live Open-Meteo repository exercise passed through temporary `OpenMeteoGeocodingRepository` JVM test and returned `Loading` then `Success` for `Madison`; logs saved at `.codex/test-artifacts/2026-08-20-manual-search-results-selection/live-open-meteo-geocoding.log` and `.codex/test-artifacts/2026-08-20-manual-search-results-selection/live-open-meteo-geocoding.xml`. Broad checks passed with logs saved in the same artifact directory: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh && ./gradlew :app:assembleDebug`; `git diff --check`.
- committed: `current HEAD` after the Slice 9A commit is created.
