# Active Cycle

Status: verified
Cycle ID: 2026-08-23-met-norway-repository-path
Mode: feature
Goal: Implement Slice 13D by adding an explicit-location MET Norway repository path that converts the verified MET Norway client and mapper into provider-neutral `WeatherRepositoryResult` emissions without fallback selection, cache persistence, UI wiring, active-provider disclosure, or provider-specific leakage.
Roadmap slice: Slice 13D: Explicit-Location MET Norway Repository Path from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 1, 4, 5, 6.2, 6.3, 16, 17, 39, 40, 41, 44, 46, and 48
- `.codex/plans/mvp-roadmap.md` Slice 13D and Forecast Provider Scope
- `docs/data-sources/MET_NORWAY_FORECAST.md` fields used, caching rules, error responses, attribution, privacy implications, and failover behavior
- Existing `OpenMeteoWeatherRepository` and `OpenMeteoWeatherRepositoryTest` as local repository-boundary style precedent, not as permission to copy Open-Meteo provider semantics where MET Norway differs
- Existing `MetNoForecastClient`, `MetNoForecastMapper`, parser, DTO, mapper, and client tests as verified provider-local inputs
- `AGENTS.md`

Acceptance criteria:
- Add a MET Norway repository implementation under `core.provider.metno` that implements the existing provider-neutral `WeatherRepository` interface without changing that public interface or the existing `ForecastError` sealed class unless a hard implementation blocker is found and documented first.
- `refresh(location)` must use the exact explicit `WeatherLocation` supplied by the caller. It must not introduce a hidden default location, scaffold/sample location, fallback location, app-level selected-location state, or UI routing behavior.
- Emit `WeatherRepositoryResult.Loading` before every terminal result, matching the existing repository boundary used by Home state.
- Build the MET Norway client request from the selected location latitude and longitude, and include `location.elevationMeters` as optional altitude only when it exists. Do not fabricate elevation.
- Do not send `cachedLastModified` or `If-Modified-Since` from this repository slice because there is no cache source of truth yet. Cache revalidation belongs to a later persistence/stale-data slice.
- On `MetNoForecastClientResult.Success`, map through the production `MetNoForecastMapper` using the selected `WeatherLocation` and an injectable deterministic clock for `fetchedAt`, then emit `WeatherRepositoryResult.Success(weather)`.
- Preserve MET Norway provenance produced by the mapper: provider ID `met-norway`, source name `MET Norway`, license ID, issued time where available, model-estimate current conditions, and forecast hourly/daily rows.
- Translate all provider-local client failures into existing provider-neutral `ForecastError` values with provider ID `met-norway` where applicable. Provider-specific status codes, `X-ErrorClass`, raw response bodies, cache headers, symbols, DTOs, and parser/mapper exceptions must not cross the repository boundary.
- Treat mapper failures from invalid MET Norway data, unexpected units, invalid timestamps, empty/no usable timesteps, or malformed success data as provider-neutral invalid-response or provider-rejected/unsupported failure through existing `ForecastError` values. Do not add user-facing provider-specific copy.
- If `MetNoForecastClientResult.NotModified` is returned unexpectedly in this no-cache repository slice, return a deterministic provider-neutral failure rather than pretending cached data exists.
- Repository tests must prove loading-before-terminal behavior, explicit coordinate/elevation request construction, fixture-backed success through client plus mapper, deterministic fetched time/provenance, provider-neutral error translation, mapper-failure translation, unexpected 304/no-cache handling, and repository-boundary isolation.
- Keep `app` production wiring on the existing Open-Meteo repository. Do not make MET Norway active/current in product behavior or disclosures in this slice.

Acceptance boundary: Slice 13D is complete when core repository tests prove that an explicit `WeatherLocation` can be refreshed through the production MET Norway client/parser/mapper path into provider-neutral `WeatherRepositoryResult.Success`, client and mapper failures become provider-neutral `ForecastError` values, the request uses only the caller's location coordinates and optional elevation, no cache revalidation is attempted without cache state, unexpected 304 is handled as failure, and static checks show no MET Norway DTO/client/error/header/symbol leakage outside the MET Norway provider package. Slice 13D is not verified by fallback selection, UI display, active-provider disclosure, cache persistence, stale-data behavior, saved-location behavior, or live MET Norway fetches.

Boundary decisions:
- Do not modify `WeatherRepository`, `WeatherRepositoryResult`, or `ForecastError` unless implementation cannot satisfy 13D with the existing provider-neutral contract. If such a conflict appears, stop and document the exact conflict before changing public core surfaces.
- Do not add fallback orchestration, provider preference, provider health/backoff, retry policy, Room, DataStore, cache metadata persistence, WorkManager, stale UI, Home UI changes, Settings/About surfaces, or app wiring.
- Do not update `DATA_SOURCES.md`, `PRIVACY.md`, `README.md`, or `THIRD_PARTY_LICENSES.md` to list MET Norway as active/current. Repository implementation alone is not the verified active fallback path.
- Do not perform live MET Norway requests as acceptance evidence. Focused verification uses fake transports plus provider fixtures so request shape and result translation are deterministic.
- Do not add sample success, fake production repository data, TODO-only branches, constructor-only tests, broad abstractions for future fallback/cache work, or tests that only prove symbols exist.
- Do not import MET Norway weather icon assets or expose `symbol_code` beyond provider-local mapping tests.
- Do not change Open-Meteo behavior or app default provider wiring.

Focused evidence to produce:
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*MetNo*Repository*'`
- Static provider-code boundary check: `rg -n "MetNoWeatherRepository|MetNoForecastClient|MetNoForecastClientResult|MetNoForecastClientError|MetNoForecastResponse|MetNoGeometry|MetNoMeta|MetNoTimeStep|MetNoInstant|MetNoPeriod|MetNoHttp|X-ErrorClass|IllegalUserAgent|Ratelimitation|symbolCode" app/src/main/kotlin core/src/main/kotlin/com/oxygen/weather/core/model core/src/main/kotlin/com/oxygen/weather/core/provider/WeatherProviders.kt`
- Static production leakage boundary check: `rg -n "MetNo(Forecast|Parse|Mapper|Http|Client|Geometry|Meta|TimeStep|Instant|Period|.*Error|.*Exception)|X-ErrorClass|IllegalUserAgent|Ratelimitation|symbolCode" app/src/main/kotlin core/src/main/kotlin -g '!core/src/main/kotlin/com/oxygen/weather/core/provider/metno/**'`
- Static active-disclosure boundary check: `rg -n "active.*MET Norway|MET Norway.*active|current.*MET Norway|MET Norway.*current" README.md DATA_SOURCES.md PRIVACY.md`
- Save focused test and static-check logs under `.codex/test-artifacts/2026-08-23-met-norway-repository-path/` and record the project-local paths in Phase Results before reporting the slice ready.

Real-path command or procedure:
- None required. This repository-boundary slice is verified with fake MET Norway transports and parser-backed fixtures only. Do not describe final evidence as live MET Norway product behavior.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
- Save broad verification logs under `.codex/test-artifacts/2026-08-23-met-norway-repository-path/` and record the project-local paths in Phase Results before reporting the slice ready.

Current gate: verified
Current phase: ready
Last result: Slice 13D implementation is verified with fake-transport core repository tests, static provider-boundary checks, and broad Android build/test commands. Evidence logs are saved under `.codex/test-artifacts/2026-08-23-met-norway-repository-path/`.
Blocker: none.

## Implementation Plan

1. Add failing/covering `MetNoWeatherRepositoryTest` cases using fake MET Norway transports for loading-before-terminal behavior, explicit latitude/longitude/elevation request construction, absence of `If-Modified-Since` in this no-cache slice, fixture-backed success through production client/parser/mapper, deterministic fetched time/provenance, client failure translation, mapper failure translation from malformed/invalid provider data, and unexpected 304/no-cache behavior.
2. Implement the smallest `MetNoWeatherRepository` that mirrors the existing `OpenMeteoWeatherRepository` boundary: constructor-inject `MetNoForecastClient` and `clock`, implement `WeatherRepository.refresh(location)`, yield loading, build `MetNoForecastRequest` from the explicit location, map success with `MetNoForecastMapper`, and translate terminal failures.
3. Translate provider-local errors conservatively into existing `ForecastError` values: network to `NetworkUnavailable`; rate limited to `RateLimited("met-norway")`; provider unavailable to `ProviderUnavailable("met-norway")`; illegal identification and invalid request/response classes to provider-neutral rejected/invalid failures as appropriate; unsupported or insufficient data and unexpected HTTP failures to deterministic existing provider-neutral failures without leaking MET Norway diagnostics.
4. Catch provider-local mapper exceptions inside the repository and translate them to provider-neutral invalid-response or provider-rejected/unsupported failure. Do not allow provider-local exception types to escape the `WeatherRepository` sequence.
5. Run the focused repository test, static provider-code/leakage/active-disclosure boundary checks, and broad verification commands.
6. Record only command-backed evidence in `.codex/plans/current.md` and append `.codex/cycles/history.md` when the slice is actually verified or committed.

## Known Non-Blocking Drift

- `README.md` still contains scaffold-era wording that says no network weather provider has been wired yet and the screen displays `SampleWeather.bundle`. Do not fix it in Slice 13D because this cycle intentionally avoids unrelated disclosure/documentation changes; handle it in a later documentation or disclosure-alignment gate.

## Phase Results

- planned: Selected Slice 13D as the next bounded implementation slice after verified Slice 13C.
- covered: Added `MetNoWeatherRepositoryTest` fake-transport coverage for loading-before-terminal emissions, explicit coordinate and optional elevation request construction, no `If-Modified-Since` without cache state, fixture-backed success through the production MET Norway client/parser/mapper path, deterministic fetched time and provenance, provider-neutral client error translation, mapper failure translation, unexpected 304/no-cache handling, and repository-boundary isolation.
- implemented: Added `MetNoWeatherRepository` under `core.provider.metno`; it implements the existing `WeatherRepository` interface, builds `MetNoForecastRequest` from only the caller's `WeatherLocation`, maps success through `MetNoForecastMapper` with an injectable clock, catches mapper exceptions, translates provider-local client results to existing `ForecastError` values, and does not add fallback selection, cache persistence, app wiring, UI disclosure, or public provider-neutral contract changes.
- verified: `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*MetNo*Repository*'` passed; static provider-code, production-leakage, and active-disclosure boundary checks passed; `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed; `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed; `git diff --check` passed. Logs saved in `.codex/test-artifacts/2026-08-23-met-norway-repository-path/`.
