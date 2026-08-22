# Active Cycle

Status: planned
Cycle ID: 2026-08-22-provider-backed-home-success-dashboard
Mode: feature
Goal: Render the Home success dashboard from the selected location's provider-neutral `WeatherBundle` returned by `WeatherRepository`, replacing the Slice 10 terminal placeholder with real current, hourly, daily, metric, sun/update/source, and provenance UI where data exists.
Roadmap slice: Slice 11: Provider-Backed Home Success Dashboard from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 1, 3, 6, 12, 17, 31, 39, 40, and 44
- `.codex/plans/mvp-roadmap.md` Slice 11, with Slice 11A and later cache/fallback/units/accessibility gates treated as later boundaries
- `docs/data-sources/OPEN_METEO_FORECAST.md`
- `AGENTS.md`

Pre-implementation authority gate:
- Gate 3A is now recorded in `.codex/cycles/history.md` as a corrective documentation-only backfill. Slice 11 may start from this plan after confirming the Gate 3A commit is present in history.

Acceptance criteria:
- Manual selection still routes Home using the exact selected `WeatherLocation`; no hidden default, scaffold, or sample location is substituted.
- `WeatherRepositoryResult.Success` maps the actual returned `WeatherBundle` into provider-neutral Home success presentation state. The success UI renders only values derived from that bundle and the selected location.
- The Slice 10 terminal placeholder text is removed from the success path. Success must render a real dashboard, not a "coming later" message.
- Success renders MVP Home sections in the specified order where data exists: location header, alert area when present, current-condition hero, near-term precipitation summary when supported by existing domain data, hourly forecast, daily forecast, condition metric grid, sun/update/source information, and provenance footer.
- Missing/null values are displayed as unavailable/unknown or the affected optional field is omitted. Nulls must never be coerced to `0`, empty strings, fake times, fake alerts, fake air quality, or sample values.
- If `WeatherBundle.current` is null and hourly/daily lists are empty, success renders a provider-neutral returned-data-unavailable state with source/provenance where available, not an empty or fabricated dashboard.
- Current conditions from Open-Meteo remain visibly labeled as model estimates, not observations.
- Time presentation uses the `WeatherLocation.zoneId`, not the device timezone.
- Source/update/provenance UI uses `DataProvenance` and `WeatherBundle.fetchedAt`, including provider source name, data type, fetched/updated age or local timestamp, and license where available.
- Provider IDs may remain in core domain `DataProvenance` for traceability, but provider IDs, provider DTOs, provider client result types, provider error bodies, and provider implementation names must not become user-facing text or Compose inputs. UI receives provider-neutral labels derived from domain provenance such as source name, data type, fetched time, and license.
- Forecast request disclosure from Slice 10 remains visible somewhere on the active provider-backed success surface until a fuller Data Sources surface is implemented.
- Long selected place names, large font, narrow widths, and effects-disabled/decorative-scene-independent reading remain supported by stable layout dimensions and text wrapping.
- Loading, no-cache error, retry, first-run manual search, permission-denied behavior, stale geocoding isolation, duplicate forecast-load prevention, obsolete forecast-emission isolation, and no-sample production-boundary checks from earlier slices remain intact.
- Provider DTOs, Open-Meteo client result types, `SampleWeather`, and `SampleWeather.bundle` remain absent from Home presentation state and Composables. Provider construction may remain isolated at the composition/state-holder boundary as in Slice 10.

Acceptance boundary: Slice 11 is complete when focused app tests prove that a controlled `WeatherRepositoryResult.Success` for a selected provider-neutral location produces a Home success dashboard populated from the exact returned `WeatherBundle`, including current hero, hourly rows, daily rows, metric values, sun/update/source/provenance text, model-estimate labeling, null/unavailable handling, returned-data-unavailable handling, and location-timezone formatting, while preserving Slice 10 loading/error/retry behavior. Real-path evidence must exercise the default production path from live Open-Meteo geocoding result selection into a bounded Open-Meteo forecast success and save a log plus screenshot showing real provider-backed Home success. Slice 11 is not verified by retaining a bundle in state, reusing `SampleWeather`, rendering placeholder cards, running static checks alone, or asserting that Composables exist without checking visible state.

Boundary decisions:
- Keep domain models in `:core` unchanged unless a missing field blocks rendering a required existing-provider value. If model changes are required, prove them with mapper/repository tests and keep provider-neutral names.
- Reuse the existing `HomeScreen` visual direction only as a UI implementation starting point. Its production success path must receive presentation-ready state from `OxygenAppStateHolder`, not `SampleWeather.bundle`, and it must not keep the scaffold's source footer language.
- Introduce a Home success presentation model if needed so formatting, null handling, time-zone conversion, source labels, and privacy/provenance copy are testable outside Compose. Keep provider-specific DTO names and provider IDs out of that model.
- Keep US/custom unit settings out of scope. Until unit preferences exist, Slice 11 displays canonical metric units derived from domain models: degrees Celsius, millimeters, hectopascals, percent, meters or kilometers for visibility, and wind speed converted from meters per second only when the label names the displayed unit. Unit preferences and US/UK/custom conversion remain later slices.
- Do not implement cache, stale-cache UI, explicit pull-to-refresh, saved locations, MET Norway fallback, alerts provider integration, air-quality provider integration, detail screens, Data Sources/About navigation, unit preference UI, or release-candidate claims in this cycle.
- Alert and air-quality sections render only if the returned `WeatherBundle` already contains provider-neutral alerts or air-quality data. Do not invent active alerts or air-quality data for Open-Meteo success.
- UV is omitted in Slice 11 unless a provider-neutral domain field already exists by implementation time. Do not fabricate UV or add provider-specific UV shortcuts solely to fill the metric grid.
- Near-term precipitation summary renders only when at least one near-term hourly item has precipitation probability or precipitation amount. It may summarize maximum known probability and/or total known amount over the selected near-term window. If all near-term precipitation inputs are null, omit the section.

Minimum real dashboard for Slice 11:
- Location header: selected `WeatherLocation.displayName`, coordinates/timezone only as secondary metadata where useful.
- Current hero: current temperature, condition, apparent temperature when present, local updated time or age, and model-estimate label when current provenance type is `MODEL_ESTIMATE`.
- Hourly forecast: first available Home-window hourly rows with local time, condition, temperature, and precipitation probability when present.
- Daily forecast: available daily rows with local day/date, condition, precipitation probability when present, low/high, and sunrise/sunset where available.
- Metric grid: only fields already present in `CurrentConditions`: feels-like, humidity, wind speed/gust/direction, pressure, visibility, dew point, cloud cover, and precipitation amount.
- Sun card: sunrise and sunset from daily data when present.
- Source/provenance footer: source name, data type, fetched time or age, issued time when present, license when present, and forecast request disclosure.

In scope:
- Carry successful `WeatherBundle` data from `WeatherRepositoryResult.Success` into Home success presentation state.
- Replace the success placeholder in production Home routing with the provider-backed dashboard.
- Presentation formatting for current temperature/apparent temperature/condition, hourly time/temperature/precipitation probability, daily day/condition/precipitation/high/low/sunrise/sunset, current metric grid, near-term precipitation when real inputs exist, update/source/provenance footer, returned-data-unavailable success, and unavailable/null values.
- Focused app JVM tests for state-holder success mapping, presentation formatting, null preservation, timezone conversion, provenance/model-estimate labeling, no sample/default substitution, no provider leakage, and preservation of Slice 10 non-success behavior.
- Compose-boundary assertions or emulator screenshot evidence proving visible dashboard sections render provider-backed presentation state rather than scaffold/sample weather. Static checks are required guardrails but are not sufficient UI proof.
- Live production-path evidence using the default `OxygenAppStateHolder` path and Open-Meteo success for a manually selected geocoding result.

Out of scope:
- Gate 3A implementation unless selected separately before this slice starts.
- Slice 11A explicit refresh/pull-to-refresh behavior.
- Cache schema, stale-cache display, offline retained forecasts, Room/DataStore, saved locations, current-device-location lookup, Android OS permission launcher/manifest work, navigation framework adoption, MET Norway fallback, provider preference UI, units preferences, official alerts provider path, radar, air quality provider path, detail screens, widgets, notifications, full Data Sources/About surface, and release-candidate claims.

Focused review command or procedure:
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*' --tests '*HomeSuccess*' --tests '*HomeHandoff*' --tests '*FirstRun*' --tests '*OxygenApp*'`
- Add or update focused tests so they fail before implementation because Slice 10 success still renders only terminal placeholder text.
- Presentation tests must assert metric units, no fabricated zeroes, near-term precipitation omission or summary from real inputs, returned-data-unavailable handling, and timezone formatting with a non-device timezone fixture.
- UI evidence must include Compose assertions or emulator screenshots for visible current hero, hourly, daily, metrics, sun/source/provenance, and unavailable/null handling. Static source checks alone cannot verify Slice 11 UI.
- Static no-provider-leak check: `rg "OpenMeteo.*Dto|OpenMeteoForecastResponse|OpenMeteoGeocodingResponse|OpenMeteoForecastClientResult|OpenMeteoGeocodingClientResult|providerId" app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt app/src/main/kotlin/com/oxygen/weather/app/ui`
- Static no-Open-Meteo-ui-import check: `rg "openmeteo|OpenMeteo" app/src/main/kotlin/com/oxygen/weather/app/ui`
- Static no-sample production-path check: `rg "SampleWeather|SampleWeather\\.bundle" app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt app/src/main/kotlin/com/oxygen/weather/app/ui`
- Static no-placeholder-success check: `rg "coming in a later slice|Dashboard display is coming|placeholder|TODO" app/src/main/kotlin/com/oxygen/weather/app app/src/test/kotlin/com/oxygen/weather/app`

Real-path command or procedure:
- Execute a live Open-Meteo geocoding query through the default `OxygenAppStateHolder` production path, wait with a bounded timeout until `ManualLocationSearchState.Results`, select one candidate, wait for terminal `ForecastReady`, and record the selected candidate, repository result sequence, rendered Home success presentation fields, provenance/source fields, null/unavailable fields, timezone used for hourly/daily/sun formatting, and final non-loading state to `.codex/test-artifacts/2026-08-22-provider-backed-home-success-dashboard/live-provider-backed-home-success.log`.
- Install and launch the debug app on the repo-local emulator, perform the manual selection path, and capture at least one production Home success screenshot plus one narrow or large-font/effects-independent screenshot when emulator automation allows it. Save evidence under `.codex/test-artifacts/2026-08-22-provider-backed-home-success-dashboard/`. If emulator UI automation blocks screenshots, record the exact blocker and do not claim visual verification.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Current gate: committed
Current phase: planned
Last result: Slice 11 plan created and tightened after review. Implementation has not started. Gate 3A was committed as a corrective documentation-only backfill so Slice 11 is no longer blocked by missing repository-level license, privacy, and provider-disclosure documents.
Blocker: none

## Implementation Plan

1. Baseline current Slice 10 behavior with focused app tests and static checks, confirming success currently stops at the terminal placeholder.
2. Add focused failing tests for Home success presentation state generated from a controlled provider-neutral `WeatherBundle`, including location identity, section ordering signals, minimum dashboard values, null/unavailable handling, returned-data-unavailable handling, metric units, near-term precipitation behavior, timezone formatting, model-estimate labeling, and provenance/source text.
3. Add focused failing tests that prove success rendering does not import provider DTO/client-result types, does not expose provider IDs as UI text or Compose inputs, does not use `SampleWeather`, and does not preserve placeholder success copy.
4. Add focused regression tests for loading/error/retry, duplicate-load prevention, obsolete emission isolation, first-run manual search, and selected-location handoff.
5. Implement the minimal success presentation mapping in `OxygenAppStateHolder` or adjacent app-layer presentation code, carrying the actual `WeatherBundle` or a provider-neutral formatted projection to Home success.
6. Implement production Compose success dashboard rendering from presentation state in MVP Home order, with stable dimensions, readable wrapping, no scaffold source footer copy, and no dependency on decorative scene effects.
7. Run focused tests and static checks. Save logs under `.codex/test-artifacts/2026-08-22-provider-backed-home-success-dashboard/`.
8. Run the live Open-Meteo geocoding-to-forecast success exercise and save the log. Capture emulator screenshots for Home success and a constrained accessibility-oriented view where feasible.
9. Run broad Android checks and `git diff --check`.
10. Review the diff for SLOP risks: no fake success, no sample data, no fabricated zeroes, no hidden defaults, no provider DTO leakage, no placeholder dashboard, no cache/fallback/units claims, no unverified release status. Update phase results and append cycle evidence to `.codex/cycles/history.md` only when ready.

## Phase Results

- planned: Selected Slice 11 from `.codex/plans/mvp-roadmap.md`. Planning identified Gate 3A as an unresolved authority gate that must be completed or explicitly amended before implementation begins. Review tightened the plan to resolve provider-provenance wording, define the minimum real dashboard from existing domain models, require real UI evidence, name temporary metric unit display, and block fake near-term precipitation, UV, empty dashboard, placeholder success, and static-check-only verification.
- committed-gate: Gate 3A corrective documentation-only backfill added root `LICENSE`, refreshed root notice, third-party license, data-source, and privacy disclosures, and recorded active Open-Meteo forecast/geocoding provider disclosure separately from specified roadmap providers.
