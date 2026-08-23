# Active Cycle

Status: verified
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
- Pin the official MET weathericons `weather/` filename stems current at implementation time as a test-only resource, with source/date evidence saved in the cycle artifacts. Production mapping must use normalized family rules, not a product-facing provider symbol registry.
- Use MET Norway UTC timestamps as `Instant` values and use `WeatherLocation.zoneId` only for local daily grouping. Do not use the phone timezone.
- Daily rows are grouped by `WeatherLocation.zoneId`. For each local date, `highC` is the max non-null `air_temperature_max` falling back to max instant `air_temperature`; `lowC` is the min non-null `air_temperature_min` falling back to min instant `air_temperature`; precipitation probability is the max non-null period probability; condition is the first non-null mapped condition from `next_12_hours`, then `next_6_hours`, then `next_1_hours`; sunrise and sunset remain null.
- Validate required mapped units from `properties.meta.units` before mapping; unexpected units fail deterministically through a provider-local mapper exception instead of being silently converted or guessed.
- Invalid timestep timestamps, invalid `properties.meta.updated_at`, and empty/no-usable timeseries fail deterministically through provider-local mapper exceptions.
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
- Do not broaden `WeatherBundle`, `HourlyForecast`, or `DailyForecast` in 13B. MET Norway parsed fields without current provider-neutral slots, including cloud layers, fog fraction, UV max, thunder probability, and precipitation min/max, remain provider-local/unmapped until a later domain/UI slice explicitly adds observable behavior.
- Add only provider-local mapper exceptions needed by the production mapper, limited to unexpected units, invalid timestamps, and no usable timesteps. Do not add provider-neutral error models in this slice.
- Avoid AI slop: no TODO-only mapper branches, no fake live provider success, no empty abstractions for future slices, no unverified release/fallback claims, and no tests that merely prove symbols or constructors exist.

Focused evidence:
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*MetNoForecastMapperTest'`
- Static provider-code boundary check: `rg -n "MetNo|metno|symbol_code|symbolCode" app core/src/main/kotlin/com/oxygen/weather/core/model core/src/main/kotlin/com/oxygen/weather/core/provider/WeatherProviders.kt`
- Static active-disclosure boundary check: `rg -n "active.*MET Norway|MET Norway.*active|current.*MET Norway|MET Norway.*current" README.md DATA_SOURCES.md PRIVACY.md`

Real-path command or procedure:
- None. This mapper slice is verified with offline fixtures only. Do not perform live MET Norway fetches as proof of implementation.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Current gate: verified
Current phase: ready
Last result: Slice 13B mapper implementation verified. `MetNoForecastMapper` maps parsed MET Norway compact fixtures to provider-neutral current/hourly/daily Oxygen domain models with symbol-family mapping, UTC timestamp parsing, selected-location timezone daily grouping, provider-local mapper exceptions, null preservation, and MET Norway provenance. No live MET Norway fetch, client transport, repository fallback, cache, UI, or active-provider disclosure behavior was added.
Blocker: none.

## Implementation Plan

1. Pin the current official MET weathericons `weather/` symbol stems into a test-only resource and save source/date evidence under `.codex/test-artifacts/2026-08-23-met-norway-symbol-domain-mapping/`.
2. Add failing/covering `MetNoForecastMapperTest` cases for normal Home-path current/hourly/daily mapping, deterministic daily aggregation, null preservation, UTC/local-date behavior, provenance, required-unit failure, invalid timestamps, empty/no-usable timeseries, all pinned official symbol stems, unknown/malformed/null symbols, and no provider leakage.
3. Implement `MetNoForecastMapper` and provider-local mapper exceptions in `core.provider.metno` using existing provider-local DTOs and current Oxygen domain models.
4. Keep daily mapping conservative according to the acceptance policy: selected-location timezone grouping, max/min temperature aggregation, max precipitation probability, first mapped condition from `next_12_hours` then `next_6_hours` then `next_1_hours`, and null sunrise/sunset.
5. Run focused mapper tests, static provider-code and active-disclosure boundary checks, and broad verification commands.
6. Record only command-backed evidence in `.codex/plans/current.md` and `.codex/cycles/history.md` when the slice is actually verified.

## Phase Results

- planned: Selected Slice 13B as the next bounded implementation slice after committed Slice 13A.
- contract review: Tightened the Slice 13B plan to define deterministic daily aggregation, test-only official symbol coverage, provider-local mapper failure modes, unchanged provider-neutral domain boundaries, and observable static leakage/disclosure checks before implementation.
- red-or-baseline: `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*MetNoForecastMapperTest'` failed before production mapper code existed with unresolved `MetNoForecastMapper`/`MetNoMapperException` references.
- implemented: Added `core.provider.metno.MetNoForecastMapper` and provider-local `MetNoMapperException` variants for unexpected units, invalid timestamps, and no usable timesteps. Added fixture-backed mapper tests and pinned 83 official MET weathericons `weather/svg` filename stems as a test-only resource from `https://api.github.com/repos/metno/weathericons/contents/weather/svg`; source snapshot and UTC fetch time saved under `.codex/test-artifacts/2026-08-23-met-norway-symbol-domain-mapping/`.
- focused-green: `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*MetNoForecastMapperTest'` passed; log saved at `.codex/test-artifacts/2026-08-23-met-norway-symbol-domain-mapping/focused-metno-mapper-tests.log`.
- static-boundary: `rg -n "MetNo|metno|symbol_code|symbolCode" app core/src/main/kotlin/com/oxygen/weather/core/model core/src/main/kotlin/com/oxygen/weather/core/provider/WeatherProviders.kt` returned no matches; `rg -n "active.*MET Norway|MET Norway.*active|current.*MET Norway|MET Norway.*current" README.md DATA_SOURCES.md PRIVACY.md` returned no matches. Logs saved under `.codex/test-artifacts/2026-08-23-met-norway-symbol-domain-mapping/`.
- broad-checks: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`, `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`, `. scripts/android-env.sh && ./gradlew :app:assembleDebug`, and `git diff --check` passed. Logs saved under `.codex/test-artifacts/2026-08-23-met-norway-symbol-domain-mapping/`.
