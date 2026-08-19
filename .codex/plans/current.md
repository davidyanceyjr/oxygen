# Active Cycle

Status: verified
Cycle ID: 2026-08-19-open-meteo-repository-path
Mode: feature
Goal: Return provider-neutral forecast data for an explicit selected `WeatherLocation` through the production repository path without using sample weather or exposing Open-Meteo implementation details.
Roadmap slice: Slice 5: Explicit-Location Open-Meteo Repository Path from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `docs/data-sources/OPEN_METEO_FORECAST.md`
- `docs/data-sources/PROVIDER_TEMPLATE.md`
Acceptance criteria:
- The repository accepts an explicit caller-provided `WeatherLocation` and uses that location's latitude, longitude, and IANA timezone to build the Open-Meteo forecast request.
- The repository composes the production Open-Meteo client, parser, and mapper to return provider-neutral `WeatherBundle` data on success.
- The repository exposes loading, success, and domain-level error results suitable for later Home UI state.
- No hidden default location, device-location fallback, or permission-dependent path is introduced.
- `SampleWeather.bundle`, Open-Meteo DTOs, and Open-Meteo-specific client errors do not cross the production repository/UI boundary.
- Open-Meteo attribution/provenance remains present in returned domain data.
Acceptance boundary: `:core` contains the explicit-location Open-Meteo-backed repository production path and focused repository-boundary tests. A caller can exercise the repository with a concrete `WeatherLocation` and observe loading, success, or domain-level error results. This slice does not wire the repository into Compose/Home, remove the scaffold sample screen from app startup, add persistence/cache, add geocoding, add live provider fallback, or implement manual location UI.
In scope:
- A domain-level repository result/state model that can represent loading, success with `WeatherBundle`, and forecast errors without exposing provider-specific DTOs or errors.
- An Open-Meteo forecast provider or repository implementation that converts an explicit `WeatherLocation` into `OpenMeteoForecastRequest`.
- Error translation from `OpenMeteoForecastClientError` into provider-neutral repository/domain errors such as offline/network unavailable, rate limited, provider unavailable, invalid response, and unexpected provider failure.
- A small injectable clock/fetched-time seam where needed so repository tests can assert deterministic provenance and bundle metadata.
- Focused tests for explicit coordinate/timezone propagation, success mapping through the production Open-Meteo parser/mapper path, loading-before-terminal result behavior, domain error translation, and absence of sample/provider-specific values at the repository boundary.
Out of scope:
- Compose/Home UI state wiring, app startup changes, first-run/manual location entry, geocoding/search, saved locations, Room/DataStore cache, stale data handling, retry/backoff policy, WorkManager, MET Norway fallback, alerts, unit preference presentation, live internet verification, emulator/manual verification, and data-source UI activation.
Focused test command:
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*Repository*'`
Real-path command or procedure:
- Exercise the production repository in focused unit tests with an explicit `WeatherLocation`, the production Open-Meteo client/parser/mapper path, and an injected test transport returning checked-in Open-Meteo fixture bodies or representative transport failures. No live provider or emulator exercise is expected for this repository-boundary slice.
Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
Current gate: ready
Current phase: ready
Last result: Slice 5 implemented as an explicit-location Open-Meteo repository path in `:core`; focused repository tests and broad Android verification commands passed. Not committed.
Blocker: none
Next phase: commit

## Implementation Plan

1. Confirm the existing `WeatherRepository`, `ForecastProvider`, domain models, Open-Meteo client, parser, mapper, and tests still match this plan before writing Slice 5 tests.
2. Add focused repository tests first:
   - repository emits or exposes loading before a terminal success/error result;
   - explicit `WeatherLocation` latitude, longitude, and `zoneId.id` are passed into the Open-Meteo request;
   - fixture-backed success flows through the production Open-Meteo client, parser, and mapper into a `WeatherBundle`;
   - returned data keeps the caller's `WeatherLocation`, Open-Meteo provenance, canonical units, and deterministic fetched time;
   - network/offline, rate limit, provider unavailable, invalid response, provider rejection, and unexpected HTTP failures translate to provider-neutral domain errors;
   - repository-facing result types do not expose `OpenMeteoForecastResponse`, Open-Meteo DTOs, or `OpenMeteoForecastClientError`;
   - no repository production path references `SampleWeather.bundle`.
3. Implement the smallest provider-neutral repository result/error surface needed for later UI state while preserving existing package ownership in `:core`.
4. Implement an Open-Meteo-backed repository or forecast provider composition under `:core` that reuses `OpenMeteoForecastClient` and `OpenMeteoForecastMapper`.
5. Keep `:app` Home/sample behavior unchanged in this slice; sample weather remains scaffold-only until the later UI activation slice.
6. Run the focused repository tests, then broad Android verification commands, review the diff for scope, and update phase evidence in this file.

## Phase Results

- planned: Selected Slice 5 from `.codex/plans/mvp-roadmap.md` after Slice 4 commit. Planned acceptance boundary is an explicit-location Open-Meteo-backed repository path returning provider-neutral loading, success, or error results without activating Home UI.
- covered: Added focused `OpenMeteoWeatherRepositoryTest` coverage for loading-before-terminal result behavior, explicit latitude/longitude/timezone propagation, fixture-backed success through the production Open-Meteo client/parser/mapper path, deterministic fetched time/provenance, provider-neutral error translation, and repository result types outside the Open-Meteo package.
- implemented: Added provider-neutral `ForecastError` and `WeatherRepositoryResult` in `:core`, changed `WeatherRepository.refresh` to return a loading/success/error `Sequence`, and added `OpenMeteoWeatherRepository` that maps an explicit `WeatherLocation` through `OpenMeteoForecastClient` plus `OpenMeteoForecastMapper`. `:app` Home/sample behavior was not wired or changed.
- verified: Red baseline failed before production implementation because repository result/error types and `OpenMeteoWeatherRepository` were missing; after implementation, `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*Repository*'` passed; `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed; `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed; `git diff --check` passed.
