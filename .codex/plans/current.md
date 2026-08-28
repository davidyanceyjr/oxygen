# Active Cycle

Status: planned
Cycle ID: 2026-08-28-home-dashboard-presentation-alignment
Mode: feature
Goal: Implement Slice 17A by aligning the provider-backed Home success and stale-success dashboard with the specified Home hierarchy, stable readable Compose structure, and UI-boundary evidence before offline launch work depends on this surface.
Roadmap slice: Slice 17A: Home Dashboard Presentation Alignment.
Branch or work context: local `oxygen` Android scaffold on top of committed Slice 17 review repair.
Specification anchors:
- `AGENTS.md`
- `README.md`
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 20, 24, 25, 26, 31, 35, 37, 39, 40, 44, and 48
- `.codex/plans/mvp-roadmap.md` Slice 17A, UI Rule, MVP Acceptance Boundary, Cache/Offline/Stale gates, and Release Gate
- Existing `HomeForecastPresentationState`, `HomeSuccessPresentation`, `HomeSuccessSection`, `HomeForecastFreshness`, `OxygenAppStateHolder`, `HomeLoadingScreen`, `HomeScreen`, `WeatherConditionMark`, and `WeatherScene`

Acceptance criteria:
- Provider-backed Home success and stale-success render as a vertically scrolling weather dashboard in the specified order: location header, active alert area when present, current-condition hero, near-term precipitation, hourly forecast, daily forecast, metric grid, sun/update/source information, and provenance/disclosure footer.
- Rendered values must continue to come from provider-neutral `WeatherRepositoryResult.Success`, `WeatherBundle`, and presentation state. No `SampleWeather.bundle`, provider DTOs, provider client result classes, WMO/weather-code literals, MET Norway symbols, raw JSON, provider-specific error bodies, or fabricated fallback values may cross into the production Home UI path.
- The current-condition hero must show temperature, condition, feels-like, high/low when available from daily data, update/source/provenance status, and a provider-neutral weather identity element such as `WeatherConditionMark` or the existing procedural weather scene. The text must remain meaningful if decorative effects, gradients, transparency, and animation are visually unavailable.
- Fresh, stale-after-failed-refresh, refresh-in-progress, no-cache error, and retry states must remain observable and provider-neutral after presentation alignment. A stale dashboard must keep forecast content visible while showing stale age, source/update status, refresh-failed metadata, and retry.
- Hourly rows, daily rows, metric cells, source/provenance text, stale/refresh-failed text, and retry/about controls must use stable dimensions or responsive constraints that avoid layout shift and text overlap on compact phone width and large font settings.
- Existing loading, no-cache error, retry, provider disclosure, in-app About reachability, and Home state-holder behavior must not regress.
- This slice may extract or rename Home Composables only where it supports the verified Home dashboard behavior. It must not introduce generic design-system ceremony, dormant settings, placeholder screens, broad refactors, or new modules.
- This slice does not add offline launch, installed-app durable cache wiring, saved-location persistence, unit preferences, appearance/effects persistence, alert lookup, air-quality lookup, radar/maps, background work, dependency license generation, release behavior, or new forecast/geocoding/provider behavior.

Acceptance boundary: Slice 17A is complete when focused tests prove the production Home presentation state contains the required dashboard hierarchy and semantic text for fresh and stale success without sample/provider leakage; focused rendered-UI evidence proves the production Home Composables expose the required fresh success, stale-success, alert-present success, loading, no-cache error, retry, source/provenance, disclosure, and About-entry states; compact-width/large-font Android or Compose evidence shows no obvious text overlap or unreadable controls for long location/provider names; static leakage and claim checks pass; broad Android verification passes. Slice 17A does not prove offline app relaunch, selected-location persistence, saved-location switch/remove behavior, unit conversion preferences, effects/theme persistence, official alert network lookup, air-quality/radar behavior, active installed-app MET Norway fallback, or release readiness.

Boundary decisions:
- Treat this as a UI behavior slice, not a new data or provider slice. Production changes should stay in `app/src/main/kotlin/com/oxygen/weather/app/ui/home`, `HomeForecastPresentationMapper`, and narrowly related Home presentation state only if the UI requires additional provider-neutral fields.
- Prefer extracting `HomeDashboardScreen`/section Composables from the current loading-era `HomeLoadingScreen` file over changing repository behavior.
- Keep `SampleWeather` limited to scaffold/preview code. If previews or scaffold use sample data, keep that path clearly outside production `OxygenApp`.
- Use state-holder/JVM tests for presentation contract coverage already supported by current dependencies. Add Compose UI test dependencies only if they are necessary for a meaningful UI-boundary assertion and the diff remains focused.
- For visual evidence, prefer installed-app emulator screenshots/hierarchy dumps of controlled production states if available. If emulator automation is blocked, record the exact blocker and use rendered Compose UI assertions or captured rendered Compose artifacts only for the states they can honestly prove. Plain state-holder, mapper, or log-only evidence does not count as UI-boundary proof.
- Do not update README, DATA_SOURCES, PRIVACY, or in-app disclosure unless verified behavior changes require wording. If wording changes are made, they must not imply offline launch, installed-app durable cache behavior, saved-location persistence, or release readiness.

Focused evidence to produce:
- Focused pre-change baseline or red log for rendered Home UI tests showing the current gap in dashboard alignment, stable hierarchy, stale-success rendering, alert-present rendering, or hero high/low rendering. The log must come from a compiled behavioral assertion against production Home Composables, not a source-text existence test or mapper/state-only assertion.
- Focused app tests proving `HomeSuccessPresentation.sectionOrder` and mapped fields support the specified dashboard order for a provider-backed fresh success with current/hourly/daily/metrics/sun/source/provenance data.
- Focused app tests proving stale-after-failed-refresh keeps the same dashboard content visible and exposes stale age, refresh-failed message, source/update/provenance, and retry.
- Focused app tests proving the current-condition hero derives high/low text from daily data when available, renders that text in the hero, and omits it without fabricated fallback values when daily high/low data is unavailable.
- Focused app tests proving missing current/hourly/daily values remain unavailable/omitted instead of fabricated, and long location/provider names remain present in presentation state.
- Rendered UI-boundary evidence for fresh success, stale success, alert-present success, loading, no-cache error, retry, provider disclosure, and About entry. Save screenshots, hierarchy dumps, Compose UI assertion logs, or equivalent Android/rendered-Compose artifacts under `.codex/test-artifacts/2026-08-28-home-dashboard-presentation-alignment/`. Plain Compose logs, state-holder logs, mapper logs, or JVM logs may support focused behavior coverage but do not prove rendered UI behavior.
- Compact-width/large-font evidence for at least one fresh success and one stale-success dashboard with long location/provider names. Record exact device/emulator dimensions and font-scale settings used. Minimum target shape: compact phone width such as `360x800`, large font scale such as `1.3` or higher, long selected-location display name, long provider/source or license text, and fresh plus stale rendered screenshots or hierarchy dumps.
- Static production sample-data check: `rg -n "SampleWeather|SampleWeather\\.bundle" app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt app/src/main/kotlin/com/oxygen/weather/app/ui app/src/main/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapper.kt app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt`.
- Static provider-detail leak check: `rg -n "OpenMeteoForecastResponse|OpenMeteoCurrent|OpenMeteoHourly|OpenMeteoDaily|OpenMeteoForecastClientResult|OpenMeteoForecastClientError|OpenMeteoGeocodingDto|OpenMeteoGeocodingResult|MetNoForecastResponse|MetNoGeometry|MetNoMeta|MetNoTimeStep|MetNoInstant|MetNoPeriod|MetNoForecastClientResult|MetNoForecastClientError|X-ErrorClass|symbolCode|weather_code" app/src/main/kotlin/com/oxygen/weather/app app/src/main/kotlin/com/oxygen/weather/app/ui`.
- Static cache/offline/claim check: `rg -n "offline|stale|failed refresh|failed-refresh|saved location|saved-location|cache|cached|fallback|release-ready|MVP-complete" README.md DATA_SOURCES.md PRIVACY.md app/src/main/kotlin .codex/plans/current.md`.

Real-path command or procedure:
- Install and launch the debug app on the repo-local emulator, then exercise a controlled production Home path far enough to capture Home dashboard evidence. Preferred path: use the existing manual search/Open-Meteo flow for a real location and capture the provider-backed fresh dashboard. For stale-success and alert-present dashboard evidence, use a controlled state-holder or debug/test harness only if it drives production Home Composables from provider-neutral `ForecastReady` state. Any harness must be test-only or debug-only, excluded from production release behavior, and labeled as controlled stale or alert UI evidence. Do not fabricate provider success in the installed production path and call it a live provider result.
- Save screenshots, hierarchy dumps, and command logs under `.codex/test-artifacts/2026-08-28-home-dashboard-presentation-alignment/`.
- If emulator startup, input, or screenshot capture fails, save the command log and name the exact blocker. Do not replace missing Android-boundary evidence with a release or offline claim.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
- Save broad verification logs under `.codex/test-artifacts/2026-08-28-home-dashboard-presentation-alignment/` and record project-local paths in Phase Results before reporting the slice ready.

Current gate: planned
Current phase: ready-to-start
Last result: Slice 17 and its review repair are committed. The app has provider-neutral Home success/stale state and repository-level failed-refresh cache retention, but the next implementation should align and verify the actual Home dashboard presentation before building offline launch behavior on top of it.
Blocker: none.

## Implementation Plan

1. Inspect current Home production Composables, presentation mapper, state-holder tests, and any existing screenshot/manual verification scripts.
2. Add focused Home presentation tests that encode the Slice 17A dashboard order, fresh/stale semantics, no-fabrication rules, long text preservation, and retry/source/provenance visibility.
3. Save a red or baseline focused-test log under `.codex/test-artifacts/2026-08-28-home-dashboard-presentation-alignment/` before production UI edits.
4. Refactor the current Home UI from the loading-era structure into explicit dashboard sections with stable, responsive dimensions and weather identity, keeping behavior provider-neutral.
5. Update presentation mapping only for provider-neutral fields required by the dashboard, such as current hero high/low or section labels; do not add placeholder settings or provider-specific data.
6. Run focused app tests and save logs.
7. Produce UI-boundary evidence for fresh success, stale success, loading, no-cache error/retry, disclosure, and compact-width/large-font long-text cases. Use emulator screenshots/hierarchy dumps where feasible; record blockers exactly if not.
8. Run static sample-data, provider-detail, and cache/offline/claim checks and save logs.
9. Run broad Android verification commands and `git diff --check`, saving logs.
10. Review the diff for SLOP: remove unused abstractions, dead previews in production paths, unexercised controls, placeholder TODOs, fabricated data, and unverified offline/saved-location/release claims.
11. Append `.codex/cycles/history.md` only when Slice 17A is actually verified or committed.

## Known Starting Conditions

- `HomeForecastPresentationState.ForecastReady` already carries provider-neutral dashboard content, freshness, refresh-in-progress text, retry availability, forecast disclosure, and privacy note.
- `HomeForecastPresentationMapper` already maps current, hourly, daily, metrics, sun, alerts, source/provenance, precipitation summary, and section order from `WeatherBundle`.
- `HomeLoadingScreen` currently renders loading, error, and ready states, but the ready dashboard is still structurally simple and lives in a loading-era file.
- `HomeScreen` currently delegates directly to `HomeLoadingScreen` and does not use its theme-selection parameters.
- App unit tests cover state-holder and presentation mapping behavior, but there is not yet explicit UI-boundary evidence for Slice 17A dashboard alignment, compact-width/large-font readability, or stale-success dashboard rendering.
- App default durable cache wiring, installed-app offline cache behavior, offline launch, saved-location persistence, unit preferences, alert lookup, air-quality/radar behavior, background work, release behavior, and active installed-app MET Norway fallback remain unimplemented.

## Phase Results

- planned: Selected Slice 17A as the next roadmap slice because Slice 17 is committed and offline launch should not build on an under-verified Home dashboard surface. The selected behavior is Home dashboard presentation alignment and evidence only.
