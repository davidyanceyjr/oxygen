# Active Cycle

Status: verified
Cycle ID: 2026-08-28-home-presentation-accessibility-evidence-baseline
Mode: feature
Goal: Establish Home presentation accessibility and layout evidence before offline launch and persistence work rely on the same UI.
Roadmap slice: Slice 17C: Home Presentation Accessibility Evidence Baseline.
Branch or work context: local `oxygen` Android scaffold after committed Slice 17B Explicit Home Refresh Control.

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
- Slice 17B explicit Home refresh control is committed.
- Home success, stale-success, loading, no-cache error, refresh, source/provenance, and disclosure states already exist as production app or presentation paths.

Selected behavior:
- Home presentation states have observable Compose or Android-boundary evidence for layout, semantics, and accessibility-oriented conditions.
- Important weather information remains understandable through text or semantics, not color or decorative effects alone.
- Compact phone width and large font settings do not cause Home text, controls, rows, cards, weather marks, source/provenance, stale/failure, retry, or refresh states to overlap or escape their containers.

Acceptance criteria:
- Home success, stale-success, loading, no-cache error, source/provenance, stale/refresh-failed, refresh-control, refresh-in-progress, and retry states are exercised at a Compose or Android UI boundary.
- Important weather semantics, including current condition, temperature, apparent temperature, high/low range, stale age, refresh failure, source, fetched/issued timestamps, and provider disclosure, have meaningful visible text or accessibility semantics.
- Compose UI tests verify rendered vertical order for dashboard sections, and a saved semantics-tree dump or merged semantics traversal log records the accessible order for representative fresh and stale dashboards, including location/header actions, stale or refresh-in-progress status, alerts when present, current conditions, hourly forecast, daily forecast, metrics, sun/update/source details, and provenance footer.
- Long location names, provider names, timestamps, stale text, retry and refresh controls, current condition text, high/low range, hourly item text, daily row text, metric labels/values, source/provenance text, and weather marks have positive rendered size, remain within compact phone bounds at large font settings, and do not overlap the next checked sibling.
- The Home dashboard remains understandable without relying on `WeatherConditionMark`, color, animation, gradients, or atmospheric effects; every weather meaning represented visually is also present as visible text or content description. This slice does not claim a production effects-off setting.
- Evidence is saved under `.codex/test-artifacts/2026-08-28-home-presentation-accessibility-evidence-baseline/` as a focused UI test log, compact large-font UI test log, fresh dashboard semantics dump, stale dashboard semantics dump, refresh-in-progress semantics dump or screenshot, no-cache error semantics dump or screenshot, and static Home-boundary leakage logs.
- `SampleWeather.bundle`, provider DTOs, provider IDs, and provider-specific errors do not cross into production Home state or Composables while adding or adjusting evidence.

Boundary note: Production app boundary evidence covers Home loading, no-cache retry, refresh click-through, and selected-location repository calls. Controlled Compose presentation evidence covers fresh success, stale success, refresh-in-progress, provider disclosure, section order, semantics, and compact large-font layout.

Acceptance boundary: Slice 17C is complete when Home presentation accessibility and layout evidence covers success, stale-success, loading, no-cache error, retry, refresh, refresh-in-progress, source/provenance, and disclosure states at a Compose or Android UI boundary; important weather semantics have readable text or meaningful semantics; compact-width/large-font rendered bounds, checked sibling overlap, rendered section order, and saved accessible-order evidence are verified; required evidence artifacts are saved under the active cycle directory; and focused static checks show no sample/provider DTO/provider-error leakage into production Home state, app wiring, or Home Composables. This slice does not add new forecast behavior, offline launch, installed-app durable cache wiring, saved-location persistence, unit preferences, appearance persistence, alert lookup, air-quality lookup, radar, background refresh, new provider behavior, release readiness, active installed-app MET Norway fallback, or a production effects-off setting.

## Implementation Plan

1. Discover existing Home UI, state-holder, presentation-state, Compose test, Android test, semantics, and weather mark coverage relevant to accessibility and compact layout.
2. Establish a baseline by running the focused Home Compose/Android UI tests that currently cover dashboard sections, stale success, loading, no-cache error, refresh, and compact large-font bounds; save logs under the active cycle artifact directory.
3. Add or update focused UI-boundary tests for missing 17C evidence, prioritizing visible text/content descriptions, rendered section order, saved semantics order, refresh-in-progress visibility, retry/refresh touch targets, compact large-font child-node bounds, and checked sibling overlap.
4. Add Android-boundary artifact capture for the required evidence set: focused UI test log, compact large-font UI test log, fresh dashboard semantics dump, stale dashboard semantics dump, refresh-in-progress semantics dump or screenshot, no-cache error semantics dump or screenshot, and static Home-boundary leakage logs.
5. Implement the smallest production UI or presentation adjustments required only where the new evidence exposes a real accessibility, semantics, order, or layout defect.
6. Run focused checks for the Home UI and state paths touched by the evidence work.
7. Run static checks for sample/provider DTO leakage across production Home boundaries:
   - `rg -n "SampleWeather|core\\.provider\\.(openmeteo|metno).*Dtos|providerId|open-meteo|metno-provider" app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt app/src/main/kotlin/com/oxygen/weather/app/Home*.kt app/src/main/kotlin/com/oxygen/weather/app/ui/home`
   - `rg -n "OpenMeteo.*Dto|MetNo.*Dto|ForecastError\\.(RateLimited|ProviderUnavailable|InvalidResponse|ProviderRejectedRequest|UnexpectedProviderFailure)" app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt app/src/main/kotlin/com/oxygen/weather/app/Home*.kt app/src/main/kotlin/com/oxygen/weather/app/ui/home`
   - Expected result: no matches in production Home state, app wiring, or Home Composables, allowing deliberate provider-neutral source names only when derived from provenance and existing provider-neutral error mapping where already accepted.
8. Run broad verification:
   - `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
   - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
   - `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
   - `git diff --check`
9. Review the diff for scope, update this plan with evidence, and append cycle evidence to `.codex/cycles/history.md` only when the slice is ready or committed.

## Phase Results

- planned: Selected Slice 17C after committed Repository Engineering Gate, Slice 17A Home dashboard presentation alignment, and Slice 17B explicit Home refresh control.
- covered: Added Android UI-boundary assertions in `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt` for weather mark content description, visible current/temperature/apparent/high-low/update/source/issued/fetched semantics, stale age and refresh-failure copy, refresh-in-progress status with disabled refresh control, saved semantics dumps, compact `360dp` / `fontScale = 1.3f` rendered bounds, and checked sibling non-overlap.
- implemented: No production code changed. Slice 17C adds evidence coverage only; Home production behavior remains unchanged.
- verified: Focused Home UI boundary test passed on `oxygen_starter(AVD) - 17`: `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`; log saved at `.codex/test-artifacts/2026-08-28-home-presentation-accessibility-evidence-baseline/focused-home-ui-test.log`.
- verified: Dedicated compact large-font UI boundary test passed on `oxygen_starter(AVD) - 17`: `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest#compactLargeFontDashboardSectionsHaveReadableRenderedBounds`; log saved at `.codex/test-artifacts/2026-08-28-home-presentation-accessibility-evidence-baseline/compact-large-font-ui-test.log`.
- verified: Manual instrumentation pass passed 8 Home UI tests and enabled pulling app-private semantics artifacts before uninstall; log saved at `.codex/test-artifacts/2026-08-28-home-presentation-accessibility-evidence-baseline/manual-instrumentation-home-ui-test.log`.
- verified: Semantics dumps saved at `.codex/test-artifacts/2026-08-28-home-presentation-accessibility-evidence-baseline/fresh-dashboard-semantics.txt`, `.codex/test-artifacts/2026-08-28-home-presentation-accessibility-evidence-baseline/stale-dashboard-semantics.txt`, `.codex/test-artifacts/2026-08-28-home-presentation-accessibility-evidence-baseline/refresh-in-progress-semantics.txt`, `.codex/test-artifacts/2026-08-28-home-presentation-accessibility-evidence-baseline/loading-semantics.txt`, and `.codex/test-artifacts/2026-08-28-home-presentation-accessibility-evidence-baseline/no-cache-error-semantics.txt`.
- verified: Focused Home state-holder test passed: `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*'`; log saved at `.codex/test-artifacts/2026-08-28-home-presentation-accessibility-evidence-baseline/focused-home-state-test.log`.
- verified: Static Home-boundary leakage checks returned no matches; logs saved at `.codex/test-artifacts/2026-08-28-home-presentation-accessibility-evidence-baseline/static-home-boundary-leakage-1.log` and `.codex/test-artifacts/2026-08-28-home-presentation-accessibility-evidence-baseline/static-home-boundary-leakage-2.log`.
- verified: Broad checks passed: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`, `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`, `. scripts/android-env.sh && ./gradlew :app:assembleDebug`, and `git diff --check`; logs saved under `.codex/test-artifacts/2026-08-28-home-presentation-accessibility-evidence-baseline/`.
- verified: No offline launch, installed-app durable cache wiring, saved-location persistence, unit preferences, appearance persistence, alert lookup, air-quality/radar behavior, background refresh, new provider behavior, release readiness, active installed-app MET Norway fallback, or production effects-off setting was added or claimed.
