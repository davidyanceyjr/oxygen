# Active Cycle

Status: committed
Cycle ID: 2026-08-19-open-meteo-client-transport
Mode: feature
Goal: Fetch Open-Meteo forecast data through an isolated production client and classify transport/provider/parse failures without activating repository or UI success paths.
Roadmap slice: Slice 4: Open-Meteo Client Transport and Error Classification from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `docs/data-sources/OPEN_METEO_FORECAST.md`
- `docs/data-sources/PROVIDER_TEMPLATE.md`
Acceptance criteria:
- Open-Meteo forecast base URL and query construction are isolated/configurable under `:core`; no provider URL or query literals are introduced in UI code.
- The client requests only fields required by the first provider-backed Home path from the Open-Meteo provider contract.
- Successful HTTP responses parse through the production `OpenMeteoForecastParser` and return `OpenMeteoForecastResponse`.
- The client classifies network/offline failure, provider unavailable, HTTP/rate-limit where detectable, Open-Meteo provider error bodies, and invalid response bodies into typed client errors.
- Provider DTOs and provider-specific client errors remain isolated from repository and UI consumers in this slice.
Acceptance boundary: `:core` contains Open-Meteo-specific production client transport code, configurable request construction, a small injectable HTTP transport abstraction for tests, and focused client tests. The client returns parsed Open-Meteo DTOs or typed Open-Meteo client failures only; it does not map to `WeatherBundle`, implement `ForecastProvider`, wire `WeatherRepository`, cache data, or change Compose/Home behavior.
In scope:
- `OpenMeteoForecastRequest` or equivalent provider-specific request configuration using explicit latitude, longitude, and IANA timezone inputs.
- Open-Meteo forecast URL/query construction for the contracted current, hourly, daily, time, unit, and forecast-window parameters.
- Minimal production transport implementation suitable for Android/JVM, kept injectable so unit tests can exercise success and failure paths without live internet.
- Typed Open-Meteo client failure model covering network/offline, provider unavailable/5xx, rate limit/429, provider bad request/error body, invalid response/parse failure, and unexpected HTTP failure where useful.
- Focused tests that assert exact query parameters, successful response parsing through fixture JSON, and each required error classification.
Out of scope:
- Repository activation, `ForecastProvider` implementation, `WeatherRepository` results, Home UI state, Compose rendering of provider-backed success, sample weather removal, persistence/cache, retry policy, backoff, live internet verification, geocoding, alerts, unit preference presentation, and MET Norway fallback.
Focused test command:
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*OpenMeteo*Client*'`
Real-path command or procedure:
- Run focused client tests with an injected test transport that returns checked-in Open-Meteo fixture bodies and throws representative I/O failures; no live provider or emulator exercise is expected for this isolated transport slice.
Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
Current gate: ready
Current phase: committed
Last result: Slice 4 implemented as an isolated Open-Meteo forecast client in `:core`; focused client tests and broad Android verification commands passed; committed locally as `current HEAD`.
Blocker: none
Next phase: red-or-baseline

## Implementation Plan

1. Discover existing Open-Meteo parser/mapper behavior, provider contract fields, and Gradle dependencies; confirm no nested `AGENTS.md` changes authority.
2. Add focused client tests first:
   - URL construction uses configurable base URL and explicit location/timezone.
   - Current/hourly/daily field parameters exactly match the first Home-path contract.
   - Canonical metric unit/time parameters and forecast window parameters are present.
   - HTTP 200 fixture response is parsed through `OpenMeteoForecastParser`.
   - I/O failure is classified as network/offline.
   - HTTP 429 is classified as rate-limited.
   - HTTP 5xx is classified as provider unavailable.
   - HTTP 400 provider error JSON is classified as provider bad request/error body without leaking raw parser exceptions.
   - Malformed or structurally invalid success bodies are classified as invalid response.
3. Implement a provider-local client package in `core/src/main/kotlin/com/oxygen/weather/core/provider/openmeteo/`:
   - request/configuration data type;
   - URL/query builder;
   - `OpenMeteoHttpTransport` abstraction and a small JVM/Android production implementation;
   - `OpenMeteoForecastClient` that composes transport plus parser;
   - sealed typed client result/error model.
4. Keep all new provider-specific types under the Open-Meteo package and avoid changing `WeatherRepository`, `ForecastProvider`, app UI, or sample data.
5. Run focused client tests, then broad Android verification commands, then review the diff for scope and update cycle evidence.

## Phase Results

- planned: Selected Slice 4 from `.codex/plans/mvp-roadmap.md` after Slice 3 commit. Planned acceptance boundary is an isolated `:core` Open-Meteo client returning parsed DTOs or typed client failures only.
- covered: Added focused `OpenMeteoForecastClientTest` coverage for configurable base URL/query construction, contracted current/hourly/daily field parameters, canonical unit/time/window parameters, fixture-backed HTTP 200 parsing through `OpenMeteoForecastParser`, I/O/network failure, HTTP 429 rate limit, HTTP 5xx provider unavailable, HTTP 400 provider error body, malformed success body, and unexpected HTTP status classification.
- implemented: Added provider-local `OpenMeteoForecastRequest`, `OpenMeteoForecastClient`, typed client result/error model, injectable `OpenMeteoHttpTransport`, and `UrlConnectionOpenMeteoHttpTransport` under `core/src/main/kotlin/com/oxygen/weather/core/provider/openmeteo/`. No repository, `ForecastProvider`, UI, cache, or sample-weather activation path was changed.
- verified: `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*OpenMeteo*Client*'` passed; `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed; `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed; `git diff --check` passed.
- committed: `current HEAD` (`Add Open-Meteo client transport`).
