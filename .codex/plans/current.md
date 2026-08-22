# Active Cycle

Status: committed
Cycle ID: 2026-08-22-selected-location-home-handoff
Mode: feature
Goal: Route a selected first-run manual-search location to an observable Home loading surface for exactly that provider-neutral `WeatherLocation`, without fetching forecasts or substituting sample/default data.
Roadmap slice: Slice 9B: Selected Location Handoff To Home from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 1, 3, 10, 14, 19, 31, and 50
- `.codex/plans/mvp-roadmap.md` Slice 9B, with Slice 10 treated as a later boundary
- `docs/data-sources/OPEN_METEO_GEOCODING.md`
- `AGENTS.md`

Acceptance criteria:
- Selecting a result from first-run manual search routes the production app state to Home using the exact selected provider-neutral `WeatherLocation`.
- `OxygenAppPresentationState.selectedLocation` and the Home screen state carry the same `WeatherLocation`; no hidden default, scaffold, sample, or newly fabricated location is substituted.
- The Home route is a loading/handoff surface only. It does not call `WeatherRepository`, fetch forecasts, persist saved locations, render forecast success, render `SampleWeather.bundle`, or claim data is available.
- The handoff is observable at the app state boundary: tests can prove the state moves from first-run results to Home loading and that Home loading is tied to the selected location.
- Home loading is a typed provider-neutral app screen state, not a marker route. It must retain the exact selected `WeatherLocation` and may expose derived title/subtitle/status copy for rendering.
- The Compose production path renders visible Home loading copy for the selected location, including enough place disambiguation to keep long or similar place names readable.
- Long selected place names wrap without depending on decorative weather scene effects, gradients, transparency, or animation.
- Late manual-search repository emissions after selection do not replace Home loading, change the selected location, or route back to first-run.
- Manual search, retry, empty/failure states, and optional location-permission behavior from Slice 9A remain intact.
- Default `MainActivity -> OxygenApp()` still starts at first-run manual location entry when no selected location exists.
- Provider DTOs, provider IDs, `SampleWeather`, and `SampleWeather.bundle` remain absent from the production app UI boundary.

Acceptance boundary: Slice 9B is complete when focused app tests prove that selecting a provider-neutral manual-search result routes to a typed Home loading screen state for the exact selected `WeatherLocation`, `OxygenAppPresentationState.selectedLocation` and the Home loading state retain the same `WeatherLocation`, late manual-search emissions cannot undo the Home handoff, no sample/default weather path is used, and long selected place names remain present in loading presentation state. Real-path evidence must exercise the production default `OxygenAppStateHolder` path from a live Open-Meteo geocoding result selection into Home loading. UI readability evidence must include either a narrow-screen emulator/manual screenshot of the production Home loading surface or a Compose UI test if test infrastructure is deliberately added. This slice does not implement forecast repository execution from Home, Home error/retry states, Home success dashboard rendering, saved-location persistence, device-location lookup, Android OS permission launcher integration, navigation library adoption, debounce/cancel behavior, result caching, or offline forecast cache.

Boundary decisions:
- Keep the app-level route model lightweight. Add a provider-neutral Home loading presentation state if needed, instead of introducing a navigation framework or ViewModel in this slice.
- Selection should be the only first-run manual-search action that routes Home. Search submission, retry, empty results, and search failures must remain on first-run.
- Home loading state should be modeled as typed app state, for example `OxygenAppScreen.Home(loading = HomeLoadingPresentationState(...))`, rather than a route marker plus unrelated fields.
- Home loading state may include presentation-ready location text derived from `WeatherLocation`; it must still retain the exact `WeatherLocation` for later Slice 10 forecast loading.
- Home loading should use a small production loading surface, such as `HomeLoadingScreen`, instead of adapting the existing forecast-success `HomeScreen` with nullable, fake, scaffold, or sample forecast data.
- Guard manual-search result application so stale repository emissions are ignored once selection has routed Home, and so results from an older submitted query cannot overwrite a newer first-run search state.
- Keep the existing sample Home scaffold isolated under `app/scaffold`; production `OxygenApp` must not import or pass `SampleWeather`.

Plan corrections from review:
- Static sample-weather checks must target the production `OxygenApp` state/rendering boundary and must not fail on intentionally isolated `app/sample` or `app/scaffold` files.
- Real-path live geocoding evidence must use a bounded wait for repository-backed results before selection, then save a log that records the selected candidate and resulting Home loading state.
- UI readability evidence for this slice will be a narrow-screen emulator/manual screenshot of the production Home loading surface; Compose UI test infrastructure is out of scope unless deliberately added in a later cycle.
- Remove Slice 9A "next slice" production copy during implementation; after selection, user-visible copy belongs to Home loading and must not describe implementation staging.

In scope:
- App state holder route transition from manual-search result selection to Home loading.
- Provider-neutral Home loading presentation state bound to the selected `WeatherLocation`.
- Production Compose rendering for Home loading, including readable selected-location text.
- Focused app JVM tests for exact selected-location handoff, no substitution, no sample usage, no permission command, stale search emission isolation, long-name presentation-state preservation, and preservation of first-run non-selection states.
- Static production-boundary checks for provider DTO/provider ID/sample weather leakage.
- Live geocoding real-path exercise that selects a returned candidate and records the resulting Home loading state.
- Narrow-screen Home loading readability evidence from a production path screenshot, unless a focused Compose UI test is intentionally added instead.

Out of scope:
- `WeatherRepository` calls from Home, forecast loading through Open-Meteo, Home error/retry, Home success dashboard, saved-location persistence, current-device-location lookup, Android permission launcher/manifest work, navigation framework adoption, Room/DataStore, result caching, offline cache, and broad Data Sources UI.

Focused review command or procedure:
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*FirstRun*' --tests '*HomeHandoff*' --tests '*OxygenApp*'`
- Static no-provider-leak check: `rg "OpenMeteoGeocodingDto|OpenMeteoGeocodingResult|providerId" app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt app/src/main/kotlin/com/oxygen/weather/app/ui`
- Static no-sample production-path check: `rg "SampleWeather|SampleWeather\\.bundle" app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt app/src/main/kotlin/com/oxygen/weather/app/ui`

Real-path command or procedure:
- Execute a live Open-Meteo geocoding query through the default `OxygenAppStateHolder` production path, wait with a bounded timeout until `ManualLocationSearchState.Results` is reached, select one returned candidate, assert Home loading contains the exact selected `WeatherLocation`, and save logs to `.codex/test-artifacts/2026-08-22-selected-location-home-handoff/live-geocoding-home-handoff.log`.
- Capture a narrow-screen production Home loading screenshot after selecting a long or disambiguated location. Save the screenshot under `.codex/test-artifacts/2026-08-22-selected-location-home-handoff/` and record the project-local path in phase results and cycle history.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Current gate: ready
Current phase: committed
Last result: Slice 9B is implemented, verified, and committed in `current HEAD`: selecting a first-run manual-search candidate routes to typed Home loading for the exact provider-neutral `WeatherLocation`, stale/superseded search emissions are ignored, production Home loading renders selected-location copy without forecast success, and broad Android checks passed. Narrow-screen manual evidence was attempted, but resizing after selection recreated the activity and narrow interaction was not reliable enough for a valid screenshot; default-size emulator screenshot evidence was captured instead.
Blocker: none

## Implementation Plan

1. Replace the Slice 9A selection acknowledgement test that asserts "without routing home" with 9B tests asserting route transition to typed Home loading for the exact selected `WeatherLocation`.
2. Add focused app tests for no hidden/sample/default substitution, no permission command, stale manual-search emission isolation after Home handoff, long-name loading presentation-state preservation, and preservation of first-run search failure/retry behavior before selection.
3. Replace the Slice 9A selection acknowledgement with a route transition that sets a typed `OxygenAppScreen.Home` loading state carrying the selected `WeatherLocation`.
4. Guard manual-search result application so late emissions after selection and results for superseded submitted queries cannot replace the current route/state.
5. Add a small production Home loading Compose surface in `OxygenApp`/app UI so the selected location is visible and readable while no forecast success is claimed.
6. Keep `SampleWeather` confined to the scaffold path and keep provider DTOs/provider IDs out of app UI state and Composables.
7. Run focused tests, scoped static checks, a bounded live Open-Meteo geocoding-to-selection real-path exercise, narrow-screen Home loading screenshot evidence, broad Android checks, and `git diff --check`.
8. Review scope and evidence, then update phase results and append cycle evidence to `.codex/cycles/history.md` when ready.

## Phase Results

- planned: Selected Slice 9B from `.codex/plans/mvp-roadmap.md`. Planned scope is limited to routing a manually selected provider-neutral `WeatherLocation` into an observable typed Home loading surface, proving exact-location handoff, no sample/default substitution, stale search emission isolation, and honest long-name readability evidence. Forecast repository execution, Home error/retry, Home success, persistence, current-device-location lookup, Android permission launcher integration, navigation framework adoption, debounce/cancel, and offline cache are out of scope.
- covered: Focused app JVM tests cover default first-run state, repository-backed manual search preservation, empty/failure/retry behavior, exact selected `WeatherLocation` handoff into typed `OxygenAppScreen.Home`, no permission command on selection, late in-flight search emission isolation after Home handoff, superseded query isolation, and long selected place-name preservation in Home loading presentation state.
- implemented: `OxygenAppStateHolder` now routes selected manual-search candidates to `OxygenAppScreen.Home(loading = HomeLoadingPresentationState)` while retaining the exact selected `WeatherLocation` in both `OxygenAppPresentationState.selectedLocation` and Home loading state. `OxygenApp` renders a dedicated `HomeLoadingScreen` with selected-location title, coordinates/timezone, loading status, and explicit not-yet-connected forecast copy; it does not invoke `WeatherRepository` or render forecast success.
- verified: `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*FirstRun*' --tests '*HomeHandoff*' --tests '*OxygenApp*'` passed. Static no-provider-leak and no-sample production-boundary `rg` checks returned no matches. Temporary live Open-Meteo geocoding exercise through default `OxygenAppStateHolder` selected `Madison, Wisconsin, United States` and reached Home loading with the same `WeatherLocation`; log saved at `.codex/test-artifacts/2026-08-22-selected-location-home-handoff/live-geocoding-home-handoff.log`. Emulator production-path Home loading screenshot saved at `.codex/test-artifacts/2026-08-22-selected-location-home-handoff/home-loading-default.png`.
- verified: Broad checks passed: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh && ./gradlew :app:assembleDebug`; `git diff --check`.
- verified caveat: Narrow-screen screenshot evidence was attempted on the repo-local emulator. Resizing after selection recreated the activity because persistence is out of scope; keeping the emulator narrow from launch reached Home loading via keyboard traversal, but a subsequent scroll gesture hit the launcher home gesture and invalidated that screenshot. The retained screenshot evidence is therefore default emulator size, while long-name/wrapping behavior is covered at the presentation-state boundary.
- committed: `current HEAD` (`Implement selected location home handoff`).
