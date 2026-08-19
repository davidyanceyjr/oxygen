# Active Cycle

Status: committed
Cycle ID: 2026-08-19-open-meteo-fixtures-dto-parsing
Mode: feature
Goal: Parse representative Open-Meteo forecast fixtures into provider-specific DTOs without live internet.
Roadmap slice: Slice 2: Open-Meteo Fixtures and DTO Parsing from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `docs/data-sources/OPEN_METEO_FORECAST.md`
- `docs/data-sources/PROVIDER_TEMPLATE.md`
Acceptance criteria:
- Provider fixtures live under `core/src/test/resources/providers/openmeteo/`.
- Fixture set covers normal Home forecast response, missing optional values, malformed envelope, invalid weather code, provider error body, and timezone-sensitive response.
- Production Open-Meteo DTO/parser code parses only the first Home-path current, hourly, and daily fields contracted in `docs/data-sources/OPEN_METEO_FORECAST.md`.
- Required envelope validation fails deterministically with provider-local parse errors.
- Nullable weather values remain null and are never fabricated as zero.
- Open-Meteo DTOs and provider parse errors remain isolated from UI/domain consumers; no Composable, repository, or `WeatherBundle` path consumes provider DTOs in this slice.
Acceptance boundary: `:core` contains Open-Meteo-specific forecast DTO/parser production code and fixture-backed parser tests. The parser returns provider-specific parsed data only; it does not map to Oxygen domain models or fetch from the network.
In scope:
- Minimal JSON parsing dependency/plugin wiring needed by `:core`.
- Provider-specific DTO/parser package under `core`.
- Checked-in Open-Meteo forecast and error fixtures under `core/src/test/resources/providers/openmeteo/`.
- Focused unit tests for successful fixture parsing, required envelope validation, null preservation, malformed/error response behavior, invalid weather-code value preservation, and timezone metadata parsing.
Out of scope:
- Weather-code to `WeatherCondition` mapping.
- Domain `WeatherBundle` mapping.
- Open-Meteo HTTP client, URL construction, retry, caching, or rate-limit handling beyond parsing the documented error body shape.
- Repository, Room persistence, selected-location flow, Home UI state, Compose changes, geocoding, alerts, and release/provider activation.
- Live internet calls; parser tests must run offline.
Focused test command:
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*OpenMeteo*'`
Real-path command or procedure:
- Run the focused parser tests against checked-in fixture resources through the production parser path; no live provider or emulator exercise is expected for this offline parsing slice.
Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
Current gate: ready
Current phase: committed
Last result: Open-Meteo fixture-backed DTO parsing implemented in `:core`, focused parser tests passed, required broad verification passed, and the work was committed locally.
Blocker: none
Next phase: Slice 3 planning

## Phase Results

- discover: Read required repository authorities, confirmed no nested Oxygen `AGENTS.md` applies, reviewed the existing `:core` domain/provider layout, and confirmed the scaffold currently has no JSON parsing dependency.
- contract: Selected Slice 2 only; implementation must remain an offline fixture and DTO parsing slice.
- red-or-baseline: Added focused parser tests and Open-Meteo fixtures under `core/src/test/resources/providers/openmeteo/` for normal Home forecast response, missing optional values, malformed envelope, invalid weather code, provider error body, and timezone-sensitive response.
- implemented: Added `kotlinx.serialization.json` as the minimal `:core` JSON parsing dependency, provider-specific Open-Meteo DTOs, and `OpenMeteoForecastParser`.
- covered: `OpenMeteoForecastParserTest` verifies successful current/hourly/daily parsing, deterministic missing-envelope failure, provider error body reporting, null preservation, invalid weather-code preservation, and timezone metadata preservation.
- focused-green: `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*OpenMeteo*'` passed.
- real-path-exercise: The focused parser tests exercised checked-in fixture resources through the production parser path; no live provider or emulator exercise applies to this offline parsing slice.
- broad-checks: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed with `:core:testDebugUnitTest` executed and `:app:testDebugUnitTest` `NO-SOURCE`; `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed; `git diff --check` passed.
- review: Confirmed Open-Meteo production code is isolated under `core/src/main/kotlin/com/oxygen/weather/core/provider/openmeteo/`; no app UI, repository, `WeatherBundle`, HTTP client, or domain mapper path consumes provider DTOs in this slice.
- committed: current HEAD (`Parse Open-Meteo forecast fixtures`).
