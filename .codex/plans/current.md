# Active Cycle

Status: planned
Cycle ID: 2026-08-23-met-norway-symbol-domain-mapping
Mode: feature
Goal: Implement Slice 13B by converting parsed MET Norway Locationforecast compact data into provider-neutral Oxygen weather domain models, with symbol mapping, timestamp handling, provenance, and null preservation verified at the `:core` mapper boundary.
Roadmap slice: Slice 13B: MET Norway Symbol and Domain Mapping from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 1, 4, 5, 6.2, 6.3, 12, 16, 17, 39, 40, 44, 46, and 48
- `.codex/plans/mvp-roadmap.md` Slice 13B and Forecast Provider Scope
- `docs/data-sources/MET_NORWAY_FORECAST.md` fields used, time format, unit format, weather-code mapping, failover behavior, and Oxygen semantics
- Existing `OpenMeteoForecastMapper` and mapper tests as local style precedent, not as permission to copy Open-Meteo-specific assumptions
- `AGENTS.md`

Acceptance criteria:
- Add a provider-local MET Norway forecast mapper under `core.provider.metno` that accepts `WeatherLocation`, `MetNoForecastResponse`, and deterministic `fetchedAt`.
- Map the first Home-path current, hourly, and daily values into provider-neutral `WeatherBundle`, `CurrentConditions`, `HourlyForecast`, and `DailyForecast` without exposing MET Norway DTOs or symbol strings beyond the provider package.
- Supported MET Norway `symbol_code` families map to `WeatherCondition`; unknown, unsupported, malformed, empty, or null symbol codes map to `UNKNOWN`.
- Normalize only documented `_day`, `_night`, and `_polartwilight` suffixes for symbol mapping. Preserve provider-specific symbol strings only inside tests/mapper internals.
- Use MET Norway UTC timestamps as `Instant` values and use `WeatherLocation.zoneId` only for local daily grouping. Do not use the phone timezone.
- Validate required mapped units from `properties.meta.units` before mapping; unexpected units fail deterministically through a provider-local mapper exception instead of being silently converted or guessed.
- Preserve null weather values as null/unknown; do not fabricate zeros or default weather values.
- Use canonical Oxygen domain units directly from the contracted MET Norway units: Celsius, hPa, percent, millimeters, degrees, meters per second, and unitless UV values where mapped.
- Provenance identifies provider ID `met-norway`, source name `MET Norway`, license `NLOD-2.0 OR CC-BY-4.0`, `issuedAt` from valid `properties.meta.updated_at`, caller-supplied `fetchedAt`, `MODEL_ESTIMATE` for current conditions, and `FORECAST` for hourly/daily rows.
- Fallback semantics remain non-active: no averaging, merging, smoothing, fallback selection, Open-Meteo calls, client transport, repository behavior, cache behavior, UI behavior, or data-source active disclosure changes.

Acceptance boundary: Slice 13B is complete when fixture-backed core mapper tests prove provider-neutral Home-path mapping, contracted symbol-family mapping, unknown/null symbol fallback, UTC timestamp conversion, location-timezone daily grouping, provenance, required-unit validation failure, null preservation, and no provider leakage outside `core.provider.metno`. Slice 13B is not verified by live MET Norway fetches, client transport, repository fallback, cache behavior, UI display, or active-provider disclosure changes.

Boundary decisions:
- Do not add MET Norway client transport, HTTP headers/User-Agent behavior, repository path, fallback selection, cache schema, Room/DataStore, WorkManager, Compose/UI state, Gradle dependencies, active provider disclosure, or app behavior.
- Do not update `DATA_SOURCES.md` to list MET Norway as active/current.
- Do not modify Open-Meteo production behavior except if a compile-only shared domain issue is discovered and explicitly recorded before editing.
- Do not broaden provider-neutral domain models unless the mapper cannot satisfy the existing 13B contract without a documented higher-authority conflict.
- Avoid AI slop: no TODO-only mapper branches, no fake live provider success, no empty abstractions for future slices, no unverified release/fallback claims, and no tests that merely prove symbols or constructors exist.

Focused evidence:
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*MetNo*Mapper*'`
- Static provider-boundary check: `rg -n "MetNo|metno|MET Norway|symbol_code" app core/src/main/kotlin/com/oxygen/weather/core/model core/src/main/kotlin/com/oxygen/weather/core/provider/WeatherProviders.kt README.md DATA_SOURCES.md PRIVACY.md`

Real-path command or procedure:
- None. This mapper slice is verified with offline fixtures only. Do not perform live MET Norway fetches as proof of implementation.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Current gate: planned
Current phase: contract
Last result: Selected Slice 13B after Slice 13A was verified and committed at `a0081f8`. No 13B implementation files changed yet.
Blocker: none.

## Implementation Plan

1. Add failing/covering `MetNoForecastMapperTest` cases for normal Home-path mapping, null preservation, UTC/local-date behavior, provenance, required-unit failure, contracted symbol families, unknown/null symbols, and no provider leakage.
2. Implement `MetNoForecastMapper` in `core.provider.metno` using existing provider-local DTOs and current Oxygen domain models.
3. Keep daily mapping conservative: derive daily rows from available MET Norway period data grouped by the selected location timezone, without sunrise/sunset fabrication because Locationforecast compact does not provide those fields.
4. Run focused mapper tests, static provider-boundary check, and broad verification commands.
5. Record only command-backed evidence in `.codex/plans/current.md` and `.codex/cycles/history.md` when the slice is actually verified.

## Phase Results

- planned: Selected Slice 13B as the next bounded implementation slice after committed Slice 13A.
