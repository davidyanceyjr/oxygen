# Active Cycle

Status: committed
Cycle ID: 2026-08-28-explicit-home-refresh-control
Mode: feature
Goal: Add an explicit Home refresh control for provider-backed fresh and stale dashboards.
Roadmap slice: Slice 17B: Explicit Home Refresh Control.
Branch or work context: local `oxygen` Android scaffold after Repository Engineering Gate completion.

Specification anchors:
- `AGENTS.md`
- `README.md`
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `.codex/plans/mvp-roadmap.md`
- `.codex/cycles/history.md`
- `app/src/main/`
- `app/src/test/`
- `app/src/androidTest/`
- `core/src/main/`
- `core/src/test/`

Prerequisites:
- Repository Engineering Gate is committed.
- Slice 17A Home dashboard presentation alignment is committed.
- Repository-level stale-success behavior exists for failed refresh with cached data.

Selected behavior:
- A user viewing a provider-backed Home success or stale-success dashboard can explicitly refresh the forecast for the exact selected location.
- Refresh state remains observable at the app state and UI boundary without relying on recomposition side effects.
- The existing retry behavior for no-cache errors remains distinct from refresh on a dashboard with data.

Acceptance criteria:
- Fresh Home success exposes a visible `Refresh` control.
- Stale Home success exposes a visible `Refresh` control while preserving stale/source/failure metadata.
- No-cache error states continue to expose `Retry`; dashboard success/stale-success states do not label the refresh action as retry.
- Production state-holder and UI wiring distinguish dashboard refresh from no-cache retry, for example with separate refresh and retry callbacks.
- Activating refresh invokes the forecast repository for the exact selected `WeatherLocation`; no default, sample, stale previous, or hidden location is substituted.
- Refresh is caused only by explicit user action or a controlled state-holder trigger, not by every recomposition.
- Refresh-in-progress state is visible without replacing useful dashboard data with an empty loading screen.
- Successful refresh replaces the displayed dashboard data with the newly served provider-neutral forecast, clears refresh-in-progress state, clears stale failure metadata, and does not leave stale dashboard values visible.
- Failed refresh with useful cached data clears refresh-in-progress state and retains the stale dashboard with source, stale age, failure, and refresh metadata.
- Failed refresh without cache clears refresh-in-progress state and remains a retryable provider-neutral no-cache error.
- Refresh and retry controls have adequate touch targets, meaningful text or accessibility labels, stable layout, and remain readable on narrow screens and large font settings.
- `SampleWeather.bundle`, provider DTOs, provider IDs, and provider-specific errors do not cross into production Home state or Composables.

Acceptance boundary: Slice 17B is complete when a user-visible `Refresh` control is implemented for fresh and stale Home dashboards, `Retry` remains reserved for no-cache errors, refresh action is verified through the app state and Compose/OxygenApp boundary for the exact selected `WeatherLocation`, refresh-in-progress retains useful dashboard content, terminal success/failure states clear in-progress state correctly, and static checks show no sample/provider DTO/provider-error leakage into production Home state or Composables. This slice does not add offline launch, installed-app durable cache wiring, saved-location persistence, background refresh, unit preferences, appearance persistence, alert lookup, air-quality lookup, radar, new provider behavior, or release readiness.

## Implementation Plan

1. Discover the current Home state holder, Home presentation state, refresh/retry plumbing, repository interfaces, and existing Home state/Compose tests.
2. Add or update focused failing tests for explicit refresh from fresh success and stale success, exact selected-location repository calls, refresh-in-progress dashboard retention, successful replacement, failed-refresh stale retention, and no-cache error preservation.
3. Add or update Compose/UI-boundary tests for visible refresh control, accessibility label/text, stable section layout, and compact-width/large-font readability.
4. Add or update an `OxygenApp`-level click-through test with a controlled `WeatherRepository` proving fresh and stale dashboard `Refresh` taps request the exact selected `WeatherLocation`, while recomposition alone does not create extra refresh calls.
5. Implement the smallest production-path changes needed in Home state and UI to expose and handle explicit refresh separately from no-cache retry.
6. Run focused app tests for Home forecast/state, Home presentation UI, and `OxygenApp` refresh click-through behavior.
7. Run static checks for sample/provider DTO leakage across production Home boundaries:
   - `rg -n "SampleWeather|core\\.provider\\.(openmeteo|metno).*Dtos|providerId|open-meteo|metno-provider" app/src/main/kotlin/com/oxygen/weather/app`
   - `rg -n "OpenMeteo.*Dto|MetNo.*Dto|ForecastError\\.(RateLimited|ProviderUnavailable|InvalidResponse|ProviderRejectedRequest|UnexpectedProviderFailure)" app/src/main/kotlin/com/oxygen/weather/app`
   - Expected result: no production Home/UI leakage, allowing deliberate provider-neutral disclosure source names only when derived from provenance.
8. Run broad verification:
   - `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
   - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
   - `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
   - `git diff --check`
9. Exercise the Android UI boundary with controlled repository data and save logs/screenshots under `.codex/test-artifacts/2026-08-28-explicit-home-refresh-control/`. Installed-app/emulator evidence may prove visible control behavior and interaction plumbing, but stale durable-cache behavior is not claimed unless installed-app cache wiring is added in this slice.
10. Review the diff for scope, update this plan with evidence, and append cycle evidence to `.codex/cycles/history.md` only when the slice is ready or committed.

## Phase Results

- planned: Selected Slice 17B after committed Repository Engineering Gate and committed Home dashboard presentation alignment.
- covered: Added focused state-holder tests for dashboard refresh labels, exact selected-location refresh calls, refresh-in-progress dashboard retention, successful refresh replacement/stale-metadata clearing, and refresh failure without cache falling back to retryable no-cache error. Added Compose/OxygenApp tests for visible dashboard `Refresh`, stale dashboard `Refresh`, no dashboard `Retry`, compact large-font bounds, and fresh/stale click-through calls using the exact selected `WeatherLocation`.
- implemented: `OxygenAppStateHolder` now exposes `onHomeForecastRefresh()` separately from `onHomeForecastRetry()`, only refreshes from ready Home dashboards, and uses the current dashboard `WeatherLocation`. `OxygenApp` passes separate retry and refresh callbacks. `HomeLoadingScreen` renders a full-width 48dp-minimum `Refresh` control for fresh and stale dashboard states, keeps `Retry` reserved for no-cache errors, and disables duplicate taps while refresh is in progress while preserving the visible dashboard.
- verified: Pre-fix red state-holder log saved at `.codex/test-artifacts/2026-08-28-explicit-home-refresh-control/pre-fix-red-home-refresh-state.log`.
- verified: Focused state-holder tests passed: `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'`; log saved at `.codex/test-artifacts/2026-08-28-explicit-home-refresh-control/focused-home-refresh-state.log`.
- verified: Focused Compose/OxygenApp boundary tests passed on `oxygen_starter(AVD) - 17`: `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`; 7 tests executed; log saved at `.codex/test-artifacts/2026-08-28-explicit-home-refresh-control/focused-home-dashboard-ui.log`.
- verified: Broad checks passed: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh && ./gradlew :app:assembleDebug`; `git diff --check`. Logs saved in `.codex/test-artifacts/2026-08-28-explicit-home-refresh-control/`.
- verified: The broad planned `rg` patterns produced pre-existing non-Home-boundary matches in the sample scaffold package and provider-neutral `ForecastError` mapping; raw logs saved as `static-home-provider-leak-1.log` and `static-home-provider-leak-2.log`. Focused Home production-boundary checks returned no matches; logs saved as `static-production-home-boundary-leak-1.log` and `static-production-home-boundary-leak-2.log`.
- verified: This slice did not add offline launch, installed-app durable cache wiring, saved-location persistence, background refresh, unit preferences, appearance persistence, alert lookup, air-quality lookup, radar, new provider behavior, release readiness, or active installed-app MET Norway fallback.
- committed: Explicit Home Refresh Control evidence and implementation were committed in this history entry.
