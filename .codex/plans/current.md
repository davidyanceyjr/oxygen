# Active Cycle

Status: committed
Cycle ID: 2026-08-19-open-meteo-geocoding-client-repository
Mode: feature
Goal: Search locations through an isolated Open-Meteo geocoding client and expose provider-neutral repository states for manual location search without leaking provider DTOs or provider IDs.
Roadmap slice: Slice 8: Geocoding Search Client and Repository Boundary from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `docs/data-sources/OPEN_METEO_GEOCODING.md`
- `docs/data-sources/PROVIDER_TEMPLATE.md`
- `.codex/plans/mvp-roadmap.md`
Acceptance criteria:
- Open-Meteo geocoding base URL and search query construction are isolated in `:core` provider code and remain configurable outside UI code.
- Search requests use contracted geocoding parameters: trimmed non-blank `name=<query>`, bounded `count`, `format=json`, optional `language`, and optional `countryCode` when explicitly supplied by the caller.
- Geocoding request validation is local and observable: blank/whitespace-only queries do not call transport and become a provider-neutral invalid-query failure; count accepts `1..20` with default `10`; out-of-range count is clamped into `1..20`; blank optional filters are omitted; `countryCode`, when supplied, must be exactly two ASCII letters and is normalized uppercase before transport.
- The production client parses HTTP 200 responses through the existing Open-Meteo geocoding parser and classifies I/O network/offline failure, HTTP 429 rate limit, HTTP 5xx provider unavailable, provider error bodies, malformed/invalid success bodies, and unexpected HTTP status without leaking parser exceptions.
- The provider-neutral geocoding repository boundary emits `Loading` before terminal states and exposes `Success`, `Empty`, and `Failure` states suitable for later UI search.
- Repository failures distinguish invalid query, network/offline, provider unavailable, rate-limit, invalid response, provider rejected request, and unexpected provider failure.
- Empty provider results are represented as an explicit terminal empty state, not as a failure and not as fabricated sample data.
- Search ordering remains deterministic for identical provider responses and follows the provider response order after production parser/mapper validation.
- Repository success returns only provider-neutral `GeocodingLocationCandidate` values with embedded `WeatherLocation`; provider DTOs, provider IDs, and Open-Meteo-specific errors do not cross the repository boundary.
Acceptance boundary: Slice 8 is complete when focused `:core` tests exercise the production Open-Meteo geocoding client and repository using fake HTTP transports and existing fixtures, proving query construction, success/empty/error states, deterministic ordering, and boundary isolation without live internet. This slice does not add Compose search UI, debounce/cancel behavior in Android state, first-run navigation, selected-location handoff, saved-location persistence, live provider calls, active Data Sources disclosure, or forecast loading from search results.
Boundary decisions:
- This active implementation slice is committed locally. `Status: committed`, `Current gate: ready`, and `Current phase: committed` mean Slice 8 is covered, implemented, verified, and recorded in local version-control history without live-provider or UI evidence.
- Add a new provider-neutral `GeocodingRepository` and geocoding search result/error types for this slice. The older scaffold `GeocodingProvider.search(query): List<WeatherLocation>` remains untouched and must not be used as Slice 8 evidence; replacing or adapting that scaffold interface is deferred until a later UI/state integration slice needs it.
- The Open-Meteo geocoding repository exposes provider-neutral candidates, including embedded `WeatherLocation`; it does not expose provider DTOs, provider IDs, provider ranking internals, or Open-Meteo-specific client errors.
- Fake HTTP transport is allowed only as a deterministic boundary for unit tests that exercise production client/parser/mapper/repository code. It is not live-provider verification and must not be reported as a live geocoding call.
In scope:
- Add `OpenMeteoGeocodingClient`, request/result/error types, and reuse the existing Open-Meteo HTTP transport shape where practical.
- Add provider-neutral geocoding search result/error/repository types in `:core`.
- Add an Open-Meteo geocoding repository that maps client success through the existing geocoding mapper.
- Add focused unit tests for query construction, count clamping/defaulting, blank-query short circuit, optional filter omission/normalization, successful fixture parsing, empty results, network/offline, rate-limit, provider unavailable, provider error body, invalid response, unexpected status, loading-before-terminal behavior, deterministic ordering, and provider-neutral boundary isolation.
- Keep existing parser/mapper behavior and fixtures as the source of valid and invalid provider body semantics.
Out of scope:
- UI search field, result list Composables, debouncing, cancellation, first-run routing, location permission behavior, saved-location cache, selected-location handoff to Home, live Open-Meteo geocoding calls, active provider disclosure in app surfaces, forecast repository changes, and emulator/manual verification.
Focused review command or procedure:
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*Geocoding*Client*' --tests '*Geocoding*Repository*'`
Real-path command or procedure:
- Production-path repository tests in `:core` with fake HTTP transport exercising the real Open-Meteo geocoding client, parser, mapper, and repository. No live geocoding provider call is part of this slice, and fake-transport evidence must not be described as live/manual verification.
Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
Current gate: ready
Current phase: committed
Last result: Slice 8 Open-Meteo geocoding client and provider-neutral repository boundary are implemented in `:core`. Focused fake-transport tests and broad Android checks passed; no live provider call, UI, first-run flow, persistence, or active-provider disclosure was added.
Blocker: none
Next phase: plan Slice 9

## Implementation Plan

1. Discover whether the existing `OpenMeteoHttpTransport` should be reused directly or generalized without changing forecast behavior.
2. Add failing focused client tests for configurable base URL, encoded search query parameters, default count, count clamping to `1..20`, blank-query transport short circuit, optional language omission, optional country-code omission/uppercase normalization/rejection, production parser success, empty result success, I/O network failure, 429, 5xx, provider error body, malformed success body, and unexpected HTTP status.
3. Add failing focused repository tests for loading-before-terminal, invalid-query failure, success candidate mapping, explicit empty state, client-error translation to provider-neutral geocoding errors, deterministic provider-order preservation, and provider-neutral boundary isolation.
4. Implement the smallest provider-neutral geocoding repository result/error surface needed by Slice 8.
5. Implement `OpenMeteoGeocodingClient` using the existing parser and the same transport boundary style as the forecast client.
6. Implement `OpenMeteoGeocodingRepository` by mapping successful client responses through `OpenMeteoGeocodingMapper` and translating client errors to provider-neutral errors.
7. Run focused geocoding client/repository tests, then broad Android checks and `git diff --check`.
8. Review the diff for scope, update phase evidence in this file, and append completed evidence to `.codex/cycles/history.md` only after the cycle is ready or committed.

## Phase Results

- planned: Selected Slice 8 from `.codex/plans/mvp-roadmap.md`. Planned scope is limited to fixture-backed/fake-transport Open-Meteo geocoding client and provider-neutral repository boundary behavior in `:core`; no UI, first-run flow, persistence, live provider calls, or active provider disclosure is included.
- covered: Added focused geocoding client and repository tests for configurable base URL/query construction, trimmed query, default/clamped count, optional filter omission/normalization/rejection, blank-query transport short circuit, parser-backed success and empty bodies, I/O network failure, 429, 5xx, provider error body, malformed success body, unexpected status, loading-before-terminal behavior, provider-neutral error translation, explicit empty state, deterministic provider order, and boundary isolation. Initial focused run failed at `:core:compileDebugUnitTestKotlin` because Slice 8 production types did not exist yet.
- implemented: Added provider-neutral `GeocodingRepository`, `GeocodingRepositoryResult`, and `GeocodingError`; added `OpenMeteoGeocodingClient` with local request validation, configurable base URL, fakeable HTTP transport reuse, parser-backed HTTP classification, and no transport call for invalid local requests; added `OpenMeteoGeocodingRepository` mapping client success through `OpenMeteoGeocodingMapper` into provider-neutral candidates and translating Open-Meteo client/mapper failures to provider-neutral errors.
- verified: `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*Geocoding*Client*' --tests '*Geocoding*Repository*'` passed. `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed. `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed with `:app:testDebugUnitTest` `NO-SOURCE` and `:core:testDebugUnitTest` executed. `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed. `git diff --check` passed.
- committed: `8748b03` (`Add Open-Meteo geocoding repository boundary`) created locally.
