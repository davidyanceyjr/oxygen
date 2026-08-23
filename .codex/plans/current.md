# Active Cycle

Status: verified
Cycle ID: 2026-08-23-met-norway-client-transport
Mode: feature
Goal: Implement Slice 13C by adding an isolated MET Norway Locationforecast compact production client whose request construction, required HTTP headers, cache revalidation metadata, parser-backed response handling, and deterministic error classification are verified with fake transport at the `:core` client boundary.
Roadmap slice: Slice 13C: MET Norway Client Transport and Error Classification from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 1, 4, 5, 6.2, 6.3, 16, 17, 39, 40, 41, 44, 46, and 48
- `.codex/plans/mvp-roadmap.md` Slice 13C and Forecast Provider Scope
- `docs/data-sources/MET_NORWAY_FORECAST.md` endpoint, required headers, request/rate limits, caching rules, fields used, time format, error responses, attribution, privacy implications, and failover behavior
- Existing `OpenMeteoForecastClient` and client tests as local style precedent, not as permission to copy Open-Meteo request/error semantics where MET Norway differs
- Existing `MetNoForecastParser`, `MetNoForecastResponse`, `MetNoForecastMapper`, and mapper/parser tests as provider-local boundaries
- `AGENTS.md`

Acceptance criteria:
- Add a provider-local `MetNoForecastClient` under `core.provider.metno` that accepts a bounded forecast request and returns a provider-local success/failure result without exposing MET Norway DTOs or client errors outside the provider package.
- Keep the base URL configurable and default it exactly to `https://api.met.no/weatherapi/locationforecast/2.0/compact`; `lat`, `lon`, and optional `altitude` are client-built query parameters, not part of the configured default URL.
- Reject configured base URLs that already contain query parameters or fragments. A configured base URL must identify only the compact endpoint path; the client is the only code allowed to add `lat`, `lon`, and optional `altitude`.
- Build exactly one compact forecast GET request for a single explicit point using `lat` and `lon` query parameters rounded to no more than four decimal places. Coordinate formatting must be deterministic and locale-independent: use decimal dot, no scientific notation, no unnecessary trailing zeroes, preserve valid boundary values, and cover representative rounding cases such as `41.123456` -> `41.1235`, `-87.00004` -> `-87`, `90.0` -> `90`, and `-180.0` -> `-180`.
- Because MET Norway Locationforecast compact does not support Open-Meteo-style field selection, the generated request URL must contain only `lat`, `lon`, and optional `altitude` query parameters. Do not add `current`, `hourly`, `daily`, `fields`, classic endpoint, or legacy format parameters.
- Reject invalid coordinates before transport with a provider-local invalid-request/client-configuration error when latitude or longitude is `NaN`, infinite, or outside valid WGS84 ranges: latitude `-90..90`, longitude `-180..180`. Transport must not be called for invalid coordinates.
- Include `altitude` only when the request/location has a trusted elevation; round altitude to a whole-meter value before sending.
- Send a non-generic, configurable `User-Agent` identity on every production request. The production client default identity is `OxygenWeather/0.1.0 https://github.com/oxygen-weather/oxygen`; callers may override it. Fail request construction deterministically before transport when the configured identity is blank, generic, browser-mimicking, or missing both the `OxygenWeather/` product token and a contact token.
- Send `Accept: application/json` on every production request.
- Support conditional revalidation by sending `If-Modified-Since` only when caller-supplied cached `Last-Modified` metadata exists.
- Keep request headers test-observable through the provider-local transport abstraction; do not require live internet for focused client tests.
- Successful HTTP 200 responses parse through the production `MetNoForecastParser` and return `MetNoForecastClientResult.Success(response, cacheHeaders)`, where `response` is `MetNoForecastResponse` and `cacheHeaders` carries raw provider-local `Expires`, `Last-Modified`, and `ETag` values when present.
- HTTP 304 returns a distinct provider-local `MetNoForecastClientResult.NotModified(cacheHeaders)` without attempting to parse a body; do not classify a valid not-modified response as a failure.
- I/O transport failures classify as network/offline.
- HTTP 429 or `X-ErrorClass: Ratelimitation` classify as rate limited.
- HTTP 500, 502, 503, or backend/internal provider error classes classify as provider unavailable.
- HTTP 403 or `X-ErrorClass: IllegalUserAgent` classify as illegal identification/client configuration failure.
- HTTP 400 parameter/format/validation classes, malformed HTTP 200 bodies, parser failures, invalid required envelope fields, invalid JSON, and unexpected body shape classify as invalid response/request.
- Unsupported/no-data/outside-area/outside-time-range provider error classes classify as unsupported or insufficient forecast data through a provider-local client error. In Slice 13C this classification is based on HTTP status and `X-ErrorClass` provider diagnostics only; parsed-body Home-path adequacy checks remain mapper/repository work.
- Known `X-ErrorClass` values classify through a conservative provider-local table in tests: `Ratelimitation` as rate limited, `IllegalUserAgent` as illegal identification/client configuration failure, backend/internal classes as provider unavailable, parameter/format/validation classes as invalid response/request, and no-data/outside-area/outside-time-range classes as unsupported or insufficient forecast data. Unknown `X-ErrorClass` values are not guessed; classify by HTTP status first, then unexpected status/failure when no known bucket applies.
- Unexpected HTTP statuses classify deterministically with status code retained for diagnostics inside provider-local errors.
- Provider-specific status text, HTML/plain-text bodies, and `X-ErrorClass` values remain provider-local diagnostics; no provider-specific copy crosses into provider-neutral `ForecastError`, app state, or UI in this slice.
- Preserve MET Norway response headers needed by later cache/repository work at the client result boundary where available: `Expires`, `Last-Modified`, and `ETag`. Look up response header names case-insensitively, including `X-ErrorClass`, while preserving raw header values as provider-local cache/diagnostic metadata on HTTP 200 success and HTTP 304 cache-not-modified results. Do not parse, normalize, or persist them yet.
- Do not call MET Norway when Open-Meteo succeeds, do not add repository fallback selection, and do not mark MET Norway active/current in disclosures.

Acceptance boundary: Slice 13C is complete when fake-transport core client tests prove contracted URL/query/header construction from the exact no-query default compact URL, rejection of configured base URLs with preexisting query parameters or fragments, exact compact query shape with no extra provider-field or legacy parameters, deterministic locale-independent coordinate formatting and rounding, invalid coordinate rejection before transport, deterministic invalid User-Agent handling plus a valid production default User-Agent, optional altitude and conditional `If-Modified-Since`, parser-backed success with raw cache-header capture, case-insensitive response-header lookup with raw cache-header capture, distinct 304 cache-not-modified handling, network/offline classification, conservative provider error-class classification, rate limit detection, provider unavailable detection, illegal User-Agent detection, invalid-response detection for malformed success bodies and bad request classes, unsupported/no-data classification, unexpected-status classification, and no MET Norway client/DTO/error leakage outside `core.provider.metno`. Slice 13C is not verified by repository success, fallback selection, cache persistence, UI display, active-provider disclosure, or live MET Norway fetches.

Boundary decisions:
- Do not add `MetNoWeatherRepository`, fallback orchestration, provider-health/backoff state, Room/DataStore/cache schema, WorkManager, Compose/UI state, Settings/About disclosure, active Data Sources disclosure, or app wiring.
- Do not update `DATA_SOURCES.md` or `PRIVACY.md` to list MET Norway as active/current. A client class alone is not an active product provider path.
- Do not broaden provider-neutral `ForecastError` unless the client cannot satisfy 13C with provider-local errors. Repository-level translation belongs to Slice 13D or Slice 14.
- Do not add new dependencies unless the existing URL/HTTP APIs cannot satisfy required header and response-header behavior. Use a provider-local MET Norway transport request/response shape that carries `URL`, request headers, status code, response headers, and body so fake transports can observe headers and production `URLConnection` can capture cache/error headers.
- Do not make live MET Norway requests focused evidence. Live fetches may be exploratory only if explicitly recorded as non-acceptance evidence; they are not required for this slice.
- Do not add weather icon assets, UI icons, domain mapper changes, symbol mapping changes, or Open-Meteo behavior changes.
- Do not add parsed-body Home adequacy validation, repository translation, provider health/backoff, cache freshness policy, or fallback eligibility rules in 13C; those start at mapper/repository/fallback slices.
- Avoid AI slop: no TODO-only transport paths, no fake production success, no empty abstractions for future fallback/cache slices, no active-provider claims, and no tests that merely prove constructors or enum values exist.

Focused evidence to produce:
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*MetNoForecastClientTest'`
- Static provider-code boundary check: `rg -n "MetNoForecastClient|MetNoForecastClientResult|MetNoForecastClientError|MetNoForecastResponse|MetNoGeometry|MetNoMeta|MetNoTimeStep|MetNoInstant|MetNoPeriod|MetNoHttp|X-ErrorClass|IllegalUserAgent|Ratelimitation" app/src/main/kotlin core/src/main/kotlin/com/oxygen/weather/core/model core/src/main/kotlin/com/oxygen/weather/core/provider/WeatherProviders.kt`
- Static production leakage boundary check: `rg -n "MetNo(Forecast|Parse|Mapper|Http|Client|Geometry|Meta|TimeStep|Instant|Period|.*Error|.*Exception)|X-ErrorClass|IllegalUserAgent|Ratelimitation|symbolCode" app/src/main/kotlin core/src/main/kotlin -g '!core/src/main/kotlin/com/oxygen/weather/core/provider/metno/**'`
- Static active-disclosure boundary check: `rg -n "active.*MET Norway|MET Norway.*active|current.*MET Norway|MET Norway.*current" README.md DATA_SOURCES.md PRIVACY.md`

Real-path command or procedure:
- None required. This client transport slice is verified with fake transports and parser-backed fixtures only. Do not perform live MET Norway fetches as proof of implementation, and do not describe final evidence as live MET Norway behavior verified.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Current gate: verified
Current phase: ready
Last result: Slice 13C implementation is verified with fake-transport core client tests, static provider-boundary checks, and broad Android build/test commands. Evidence logs are saved under `.codex/test-artifacts/2026-08-23-met-norway-client-transport/`.
Blocker: none.

## Implementation Plan

1. Add failing/covering `MetNoForecastClientTest` cases using fake transports for URL/query construction from the exact no-query default compact URL, rejection of configured base URLs with existing query parameters or fragments, no extra query parameters beyond `lat`, `lon`, and optional `altitude`, deterministic coordinate formatting/rounding including representative decimal and boundary cases, invalid coordinate rejection for `NaN`, infinite, and out-of-range latitude/longitude with transport-not-called behavior, required headers, valid production default User-Agent, invalid User-Agent, optional altitude, conditional `If-Modified-Since`, parser-backed HTTP 200 success with `Success(response, cacheHeaders)`, case-insensitive response-header lookup with raw cache-header capture on HTTP 200 and HTTP 304, distinct HTTP 304 `NotModified(cacheHeaders)` without body parsing, I/O failure, rate-limit status/header class, provider-unavailable status/header class, illegal User-Agent status/header class, invalid bad-request class, malformed success body, unsupported/no-data provider error class, unknown `X-ErrorClass` fallback behavior, and unexpected HTTP status.
2. Define the smallest provider-local request/result/error/header/transport types needed for the client tests. Keep them in `core.provider.metno`; do not add provider-neutral error models or repository translation. The provider-local transport should use a request object with `URL` plus headers and a response object with status code, headers, and body.
3. Implement `MetNoForecastClient` with configurable no-query/no-fragment base URL, default and overrideable user-agent identity, deterministic request validation, locale-independent query encoding/rounding, required headers, optional revalidation headers, URLConnection production transport, response-header capture, and parser-backed success. Reject blank/whitespace user agents; `Java`, `Dalvik`, `Android`, `okhttp`, browser-mimicking prefixes such as `Mozilla/`, `Chrome/`, `Safari/`, and `curl/`; and identities that do not contain `OxygenWeather/` plus a contact token such as `http://`, `https://`, or `mailto:`.
4. Keep MET Norway diagnostics provider-local: retain status code and `X-ErrorClass` where useful in client errors, classify known error classes through a conservative table, do not guess unknown error classes beyond HTTP status classification, and do not expose provider text/HTML/plain-body copy to app/domain/UI boundaries.
5. Run the focused client test, static provider-code and active-disclosure boundary checks, and broad verification commands.
6. Record only command-backed evidence in `.codex/plans/current.md` and append `.codex/cycles/history.md` when the slice is actually verified or committed.

## Phase Results

- planned: Selected Slice 13C as the next bounded implementation slice after verified Slice 13B.
- contract review: Tightened the Slice 13C plan to require an explicit `Success(response, cacheHeaders)` result shape, reject pre-query/pre-fragment configured base URLs, define deterministic coordinate formatting, name the valid production default User-Agent, and broaden static leakage checks to include MET Norway DTO/result terms.
- covered: Added `MetNoForecastClientTest` fake-transport coverage for compact URL/query/header construction, base URL rejection, coordinate formatting and invalid-coordinate rejection before transport, optional altitude, conditional revalidation, valid/default and invalid User-Agent behavior, parser-backed success, case-insensitive cache and error-header lookup, 304 not-modified, network failure, known `X-ErrorClass` classifications, malformed success bodies, unsupported/no-data classes, unknown error-class fallback, and unexpected statuses.
- implemented: Added provider-local `MetNoForecastClient`, `MetNoForecastRequest`, `MetNoForecastClientResult`, `MetNoForecastClientError`, cache-header metadata, `MetNoHttpRequest`/`MetNoHttpResponse`, fake-observable `MetNoHttpTransport`, and `UrlConnectionMetNoHttpTransport` under `core.provider.metno`; no repository fallback, UI wiring, cache persistence, active-provider disclosure, or live MET Norway fetch was added.
- verified: `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*MetNoForecastClientTest'` passed; static provider-code, production-leakage, and active-disclosure boundary checks passed; `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed; `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed; `git diff --check` passed. Logs saved in `.codex/test-artifacts/2026-08-23-met-norway-client-transport/`.
