# Active Cycle

Status: planned
Cycle ID: 2026-08-22-home-loading-error-retry
Mode: feature
Goal: Route a manually selected provider-neutral `WeatherLocation` into Home forecast loading through `WeatherRepository`, expose provider-neutral no-cache error and retry states for that same location, and prevent the selected-location weather retrieval path from hanging.
Roadmap slice: Slice 10: Manual Selection Routes to Home Loading, Error, and Retry from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 1, 3, 10, 12, 14, 19, 31, 37, 39, 40, and 50
- `.codex/plans/mvp-roadmap.md` Slice 10, with Slice 11 treated as a later boundary
- `docs/data-sources/OPEN_METEO_FORECAST.md`
- `docs/data-sources/OPEN_METEO_GEOCODING.md`
- `AGENTS.md`

Acceptance criteria:
- With no selected location, production app state still shows or routes to first-run manual selection, not sample weather.
- Selecting a first-run manual-search result routes Home using the exact selected `WeatherLocation`; `OxygenAppPresentationState.selectedLocation` and every Home forecast state retain that same value.
- Home forecast loading is caused by `WeatherRepository` for the selected location, not `SampleWeather.bundle`, a hidden default location, a scaffold location, or provider DTOs in app UI state.
- The app state holder consumes repository `Loading`, terminal `Error`, and terminal `Success` emissions without hanging. Success may be retained as provider-neutral state for Slice 11, but the full success dashboard is out of scope here.
- Terminal success must be visibly non-loading at the Home boundary. It must not show a spinner, must not render forecast values, and must not claim the Slice 11 dashboard is implemented.
- Loading and no-cache error states are visible, accessible, provider-neutral, and tied to the selected location. Error copy must not expose provider DTO names, provider error bodies, or implementation details.
- Because Slice 10 activates the forecast request path, Home loading, no-cache error, retry, and terminal non-loading success surfaces must disclose that forecast requests use Open-Meteo and send the selected location's coordinates/timezone to retrieve weather data. This is not a full Data Sources/About surface and does not claim Slice 11 source/provenance footer completion.
- Because forecast cache is out of scope for Slice 10, every `WeatherRepositoryResult.Failure` from the Home forecast path is treated as a no-cache failure and maps to a retryable Home error state.
- Retry is visible from Home error, invokes `WeatherRepository` again for the same selected `WeatherLocation`, and does not route back to first-run or substitute another location.
- Slow or never-completing repository work remains observable as loading without launching duplicate requests on recomposition; changing selected location or retrying supersedes obsolete work. Call this cancellation only if the implementation actually cancels an in-flight worker/future.
- Long selected place names remain readable in loading and error states without depending on decorative weather scene effects, gradients, transparency, or animation.
- Manual search, empty/failure states, permission-denied behavior, and stale geocoding emissions from earlier slices remain intact.
- Provider DTOs, provider IDs, `SampleWeather`, and `SampleWeather.bundle` remain absent from the production app UI boundary.

Acceptance boundary: Slice 10 is complete when focused app tests prove that a selected provider-neutral manual-search candidate starts exactly one Home forecast load through `WeatherRepository` for the selected `WeatherLocation`, shows a typed loading state, maps no-cache repository errors into visible accessible provider-neutral Home error state, retries with the same `WeatherLocation`, and never substitutes sample/default/scaffold data. The state holder must also handle repository success as a terminal provider-neutral state so the production path cannot hang, but full Home success dashboard rendering, stale-cache UI, source/provenance footer composition, unit conversion UI, saved-location persistence, explicit pull-to-refresh, and fallback provider behavior remain later slices. Slice 10 is not verified by adding state classes alone; verification must prove selected-location repository execution, visible loading/error/retry, retry re-execution, stale-result isolation, terminal non-loading success behavior, and provider privacy disclosure on the active forecast request path. Real-path evidence must exercise the production default path from live Open-Meteo geocoding result selection into a bounded Open-Meteo forecast repository outcome and save a log proving the final app state is not indefinitely loading.

Boundary decisions:
- Keep the app-level state holder pattern for this slice; do not add a navigation framework or Android ViewModel unless existing code makes it necessary.
- Add a typed Home forecast state with at least `Loading`, `Error`, and terminal `ForecastReady` or equivalent provider-neutral success carrier. `ForecastReady` must be externally distinguishable from loading, must not show a spinner, and must not render current/hourly/daily/metric forecast values before Slice 11. Prefer keeping any `WeatherBundle` retention inside state-holder/internal state for later Slice 11 handoff; the Slice 10 UI should receive only provider-neutral location/status/disclosure copy. Do not adapt `SampleWeather` or render a fake success dashboard.
- Repository execution must be controlled by state-holder events, not Compose recomposition. A selected location should synchronously publish Home loading, then start one forecast job on an injected forecast executor/worker; retry should synchronously publish replacement Home loading and start one replacement job for the same location.
- Obsolete forecast emissions must be ignored if they do not match the active selected location or active request generation. The expected implementation shape is a monotonically increasing Home forecast request generation/id; each repository emission applies only when both the generation and selected `WeatherLocation` still match. Do not introduce cache, persistence, or broad concurrency abstractions solely for this slice.
- Error state copy should be presentation-ready and provider-neutral, derived from repository/domain errors such as offline, rate-limited, provider unavailable, and invalid response.
- If a live forecast succeeds during real-path evidence, record that terminal state in the log and leave dashboard rendering to Slice 11. Do not keep the UI in loading after success.
- Display minimal Open-Meteo forecast privacy/request disclosure in Slice 10 because the forecast provider is active. Keep full success-source/provenance footer composition for Slice 11 unless this slice displays actual forecast values.

In scope:
- Wiring selected-location Home state to the existing `WeatherRepository` production path.
- Provider-neutral Home loading, no-cache error, and retry presentation states and Compose surfaces.
- Terminal provider-neutral success carrier at the app state boundary to prevent indefinite loading; full success dashboard UI remains out of scope.
- Minimal active-forecast-provider privacy disclosure on Home loading, error/retry, and terminal non-loading success surfaces.
- Focused app JVM tests using controlled repositories for exact selected-location repository calls, loading, no-cache error, retry, visible non-loading success terminal handling, duplicate-load prevention, obsolete-emission isolation, no default/sample substitution, and first-run preservation.
- Static production-boundary checks for provider DTO/provider result/provider ID/sample weather leakage. Provider repository construction may remain in the app composition root or state-holder constructor defaults for this slice, but provider DTOs, provider result types, provider IDs, and provider-specific error copy must not enter presentation state or Composables. UI package checks should reject any Open-Meteo imports; app composition/state-holder checks may allow only provider repository construction imports while still rejecting DTO/client-result/provider-ID leakage.
- Live production-path exercise that starts with a live geocoding selection and reaches a bounded forecast repository terminal outcome or intentionally bounded loading observation with a recorded timeout classified as a blocker, not success.
- Screenshot or Compose/UI evidence for visible Home loading and Home error/retry states tied to the selected location. Controlled repository UI evidence is acceptable for deterministic loading/error/retry screenshots or assertions; live Open-Meteo evidence remains the required production-path log.

Out of scope:
- Full provider-backed Home success dashboard, hourly/daily/metric rendering, source/provenance footer composition, stale-cache UI, Room/DataStore, saved locations, current-device-location lookup, Android OS permission launcher/manifest work, navigation framework adoption, debounce/cancel behavior for geocoding search, explicit pull-to-refresh, offline cache, MET Norway fallback, Data Sources/About surfaces, units preferences, alerts, radar, air quality, and release-candidate claims.

Focused review command or procedure:
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*FirstRun*' --tests '*HomeHandoff*' --tests '*HomeForecast*' --tests '*OxygenApp*'`
- Static no-provider-leak check: `rg "OpenMeteo.*Dto|OpenMeteoForecastResponse|OpenMeteoGeocodingResponse|OpenMeteoForecastClientResult|OpenMeteoGeocodingClientResult|providerId" app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt app/src/main/kotlin/com/oxygen/weather/app/ui`
- Static no-Open-Meteo-ui-import check: `rg "openmeteo|OpenMeteo" app/src/main/kotlin/com/oxygen/weather/app/ui`
- Static no-sample production-path check: `rg "SampleWeather|SampleWeather\\.bundle" app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt app/src/main/kotlin/com/oxygen/weather/app/ui`

Real-path command or procedure:
- Execute a live Open-Meteo geocoding query through the default `OxygenAppStateHolder` production path, wait with a bounded timeout until `ManualLocationSearchState.Results`, select one candidate, verify the exact selected `WeatherLocation` starts Home forecast loading through the default `WeatherRepository`, then wait with a bounded timeout for terminal success/error. Save selected candidate, repository result sequence, final app state, retry behavior when error is reproducible, and timeout diagnostics to `.codex/test-artifacts/2026-08-22-home-loading-error-retry/live-selected-location-forecast.log`.
- Capture a production emulator screenshot for Home loading after selecting a live geocoding result. For Home error/retry, use controlled JVM/state-holder evidence unless an existing non-fake production path produces an error; capture an emulator screenshot only when it can be produced without adding fake provider switches, debug-only UI, or placeholder production behavior. Save evidence under `.codex/test-artifacts/2026-08-22-home-loading-error-retry/`.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Current gate: committed
Current phase: committed
Last result: Slice 10 implementation is committed in `current HEAD`. Selected manual locations now start the default `WeatherRepository` Home refresh path, publish typed Home loading/error/success states, map no-cache failures to retryable provider-neutral Home errors, retry the same selected `WeatherLocation`, ignore obsolete forecast emissions by request generation, and disclose Open-Meteo forecast request privacy on Home loading/error/terminal success surfaces. The live default `OxygenAppStateHolder` path selected `Madison, Wisconsin, United States` from Open-Meteo geocoding and reached terminal `ForecastReady` with `terminal is loading=false`; log saved at `.codex/test-artifacts/2026-08-22-home-loading-error-retry/live-selected-location-forecast.log`. Emulator install/launch succeeded, and first-run screenshots were saved, but Home screenshot automation was blocked by repeated Pixel Launcher ANR dialogs and unreliable emulator text input; Home loading/error/retry visibility is covered by controlled app JVM state-holder and Compose-boundary source evidence, with live production-path terminal behavior covered by the saved log.
Blocker: none

## Project Notes

- 2026-08-22: As of commit `59ead79` (`Implement selected location home handoff`), the application hangs when trying to retrieve weather data for a selected location. The location itself is correctly identified from the search field; the hang occurs after that, during the location-weather retrieval path.

## Implementation Plan

1. Discover the current app/core repository interfaces and reproduce or baseline the noted post-`59ead79` selected-location weather retrieval hang with a bounded test or live-path log before changing behavior.
2. Add focused app tests that encode Slice 10 behavior: selected location starts one forecast repository load, loading is visible and tied to that location, minimal Open-Meteo forecast request privacy disclosure is present, no selected location stays on first-run, no sample/default location is used, no provider DTO/provider ID reaches app UI state, and repository success does not leave Home stuck loading.
3. Add focused app tests for provider-neutral no-cache error mapping and retry: offline/network, rate-limited, provider unavailable, invalid response where existing repository errors support them; retry must call the repository again with the same selected `WeatherLocation`.
4. Add focused app tests for duplicate-load and obsolete-emission isolation so recomposition/state observation does not launch repeated forecast requests and late emissions from an old location/request cannot replace the active Home state.
5. Add focused app tests proving repository success maps to a visible non-loading terminal Home state that retains the selected `WeatherLocation`, does not render forecast values, and does not use sample/scaffold data.
6. Implement the minimal typed Home forecast state and state-holder executor/request generation handling needed to synchronously publish loading, call `WeatherRepository` off the caller thread, map loading/error/success terminal results, retain the exact selected `WeatherLocation`, and supersede stale work. Do not claim cancellation unless an in-flight worker/future is actually canceled.
7. Implement production Compose loading, no-cache error/retry, and minimal non-loading success terminal surfaces with accessible text and touch targets. Keep them provider-neutral and avoid full forecast dashboard rendering.
8. Run focused tests, scoped static checks, the bounded live geocoding-to-forecast real-path exercise, visual/UI evidence, broad Android checks, and `git diff --check`.
9. Review the diff for SLOP risks: no fake production success, no hidden loading-after-success path, no dead TODO path, no broad refactor, no cache/persistence placeholders, no new provider contract gaps, no completion claims without command evidence. Update phase results and append cycle evidence to `.codex/cycles/history.md` only when ready.

## Phase Results

- planned: Selected Slice 10 from `.codex/plans/mvp-roadmap.md`. Planned scope is limited to selected-location Home forecast loading through `WeatherRepository`, provider-neutral no-cache error/retry, minimal active forecast-provider privacy disclosure, duplicate-load prevention, obsolete-emission isolation, and a terminal success carrier that prevents indefinite loading without rendering the full success dashboard. Slice 11 success dashboard, stale-cache UI, persistence, saved locations, explicit pull-to-refresh, current-device-location lookup, provider fallback, units, alerts, and Data Sources surfaces are out of scope.
- covered: Added focused app JVM coverage in `HomeForecastStateHolderTest` plus updated first-run/contract tests. Coverage proves no selected location makes no weather request, selected manual candidate starts exactly one forecast refresh for the exact selected `WeatherLocation`, loading/error/success Home states retain the selected location, no-cache forecast errors map to provider-neutral retryable messages, retry reuses the same selected location, obsolete retry emissions cannot replace the active Home state, terminal success is non-loading and does not render forecast values, and static app/UI boundaries reject provider DTO/result/provider-id/sample leakage.
- implemented: Wired `OxygenAppStateHolder` to the default `OpenMeteoWeatherRepository` through injected `WeatherRepository` and forecast executor dependencies. Selection and retry synchronously publish typed Home loading and execute repository refresh off the caller thread. Repository `Loading`, `Failure`, and `Success` emissions map to provider-neutral Home presentation states. Request generation and selected-location checks ignore obsolete emissions. `HomeLoadingScreen` now renders loading, retryable no-cache error, and terminal non-loading success surfaces with minimal Open-Meteo forecast request disclosure and without Slice 11 dashboard values.
- verified: Focused command `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*FirstRun*' --tests '*HomeHandoff*' --tests '*HomeForecast*' --tests '*OxygenApp*'` passed; log saved at `.codex/test-artifacts/2026-08-22-home-loading-error-retry/focused-app-tests.log`. Static no-provider-leak, no-Open-Meteo-UI-import, and no-sample production-path checks returned no matches; logs saved under `.codex/test-artifacts/2026-08-22-home-loading-error-retry/`. Live default state-holder exercise selected `Madison, Wisconsin, United States`, entered Home loading for that location, and reached terminal `ForecastReady`; log saved at `.codex/test-artifacts/2026-08-22-home-loading-error-retry/live-selected-location-forecast.log`. Broad checks passed: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`, `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`, `. scripts/android-env.sh && ./gradlew :app:assembleDebug`, and `git diff --check`; logs saved in the same artifact directory. Emulator install/launch succeeded and first-run screenshots were saved, but Home screenshot automation was blocked by repeated Pixel Launcher ANR dialogs and unreliable emulator text input, so no Home screenshot is claimed.
