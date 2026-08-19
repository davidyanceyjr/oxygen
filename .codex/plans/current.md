# Active Cycle

Status: committed
Cycle ID: 2026-08-19-open-meteo-domain-mapping
Mode: feature
Goal: Map parsed Open-Meteo forecast data into provider-neutral Oxygen forecast domain models without live internet.
Roadmap slice: Slice 3: Open-Meteo Weather-Code and Domain Mapping from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `docs/data-sources/OPEN_METEO_FORECAST.md`
- `docs/data-sources/PROVIDER_TEMPLATE.md`
Acceptance criteria:
- Supported Open-Meteo weather codes map to provider-neutral `WeatherCondition`; unknown, unsupported, malformed, or null codes map to `UNKNOWN`.
- Mapper produces provider-neutral current, hourly, and daily forecast data with canonical units, `Instant` timestamps derived using the provider/location timezone, and missing nullable values preserved as null rather than fabricated.
- Provenance identifies Open-Meteo, fetched time, source/license fields, and correct `DataType`.
- Open-Meteo current conditions remain `DataType.MODEL_ESTIMATE`; hourly and daily forecasts remain `DataType.FORECAST`.
- Open-Meteo DTOs and provider-specific parse errors remain isolated from UI/domain consumers; no Composable or repository path consumes provider DTOs in this slice.
Acceptance boundary: `:core` contains Open-Meteo-specific mapping production code and fixture-backed mapper tests. The mapper accepts already-parsed Open-Meteo DTOs and returns provider-neutral domain data only; it does not fetch from the network or wire repository/UI success paths.
In scope:
- Weather-code mapping for the Open-Meteo WMO codes contracted in `docs/data-sources/OPEN_METEO_FORECAST.md`.
- Provider-specific Open-Meteo forecast mapper under `core`.
- Minimal provider-neutral model changes required to represent missing forecast values and per-row provenance truthfully.
- Focused unit tests using checked-in Open-Meteo fixtures parsed through the production parser before mapping.
Out of scope:
- Open-Meteo HTTP client, URL construction, retry, caching, rate-limit handling, or live internet calls.
- Repository, Room persistence, selected-location flow, Home UI state, Compose provider-backed success, geocoding, alerts, and release/provider activation.
- Unit preference presentation; mapper emits canonical metric/domain units only.
Focused test command:
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*OpenMeteo*Mapper*'`
Real-path command or procedure:
- Run the focused mapper tests against checked-in fixture resources through the production parser and mapper path; no live provider or emulator exercise is expected for this offline mapping slice.
Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
Current gate: ready
Current phase: committed
Last result: Open-Meteo parser-to-domain mapping implemented in `:core`, focused mapper tests passed, required broad verification passed, and the work was committed locally.
Blocker: none
Next phase: Slice 4 planning

## Phase Results

- discover: Read required repository authorities, confirmed no nested Oxygen `AGENTS.md` applies, reviewed roadmap Slice 3, Open-Meteo provider contract, existing DTO/parser code, current domain models, and sample Home consumers.
- contract: Selected Slice 3 only; implementation must remain an offline parser-to-domain mapper slice.
- red-or-baseline: Added `OpenMeteoForecastMapperTest`; initial focused command failed at compile because `OpenMeteoForecastMapper` did not exist yet.
- implemented: Added `OpenMeteoForecastMapper`, including contracted weather-code mapping, Open-Meteo provenance, local-time-to-`Instant` conversion using response timezone, km/h-to-m/s wind conversion, current model-estimate provenance, and forecast provenance for hourly/daily rows.
- implemented: Updated provider-neutral models so missing temperature/wind values and hourly/daily row provenance can be represented truthfully; updated scaffold sample/UI consumers to display unavailable values as unavailable instead of zero.
- covered: `OpenMeteoForecastMapperTest` verifies normal fixture mapping, null preservation, timezone-sensitive timestamp mapping, full contracted WMO weather-code mapping, unknown-code fallback, canonical wind conversion, and provenance fields.
- focused-green: `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*OpenMeteo*Mapper*'` passed.
- real-path-exercise: The focused mapper tests exercised checked-in fixture resources through the production parser and mapper path; no live provider or emulator exercise applies to this offline mapping slice.
- broad-checks: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed with `:core:testDebugUnitTest` executed and `:app:testDebugUnitTest` `NO-SOURCE`; `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed; `git diff --check` passed.
- review: Confirmed Open-Meteo production mapping remains isolated under `core/src/main/kotlin/com/oxygen/weather/core/provider/openmeteo/`; no repository, HTTP client, live provider, or Compose provider-backed success path was introduced.
- committed: current HEAD (`Map Open-Meteo forecast domain data`).
