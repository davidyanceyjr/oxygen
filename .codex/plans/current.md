# Slice 23B — NWS Alert Transport, Classification, and Provider Boundary

**Artifact:** implementation plan
**Roadmap slice:** 23B
**Prepared against:** local `main` `17dab0c` (committed Slice 23A)
**Prepared:** 2026-09-05
**Status:** ready, not committed
**Cycle ID:** `2026-09-05-slice-23b-nws-alert-transport-boundary`

## Goal and acceptance boundary

Add the production NWS alert transport boundary in `:core`. A request for a
validated point is built from provider-local configuration, sent with the
required identity, parsed through the Slice 23A parser/mapper, and returned
through an evolved provider-neutral `AlertProvider` contract. This slice does
not compose alerts with forecasts, filter/deduplicate cached alerts, persist
responses, poll in the background, or add UI.

Before implementation, perform the bounded documentation authority-sync
required by the review: reconcile the roadmap's next-candidate/status labels
and the live history's Slice 23A status with `17dab0c`, then record the sync in
the appropriate documentation/history entry. Do not claim this plan ready while
that higher-authority drift remains.

## Provider-neutral contract decisions

Owner: `core/src/main/kotlin/com/oxygen/weather/core/provider/WeatherProviders.kt`.
Replace `AlertProvider.getActiveAlerts(location): List<WeatherAlert>` with
`suspend fun getActiveAlerts(location: GeoPoint): AlertProviderResult`.
`AlertProviderResult` is a sealed provider-neutral result:

| Result | Meaning |
| --- | --- |
| `Success(alerts, metadata)` | HTTP 2xx active collection, including an empty list |
| `Failure(InvalidPoint)` | local point is non-finite or outside WGS84 bounds; no request |
| `Failure(InvalidRequest)` | ordinary HTTP 400, malformed request configuration, or other rejected request not matching the supported region discriminator |
| `Failure(UnsupportedRegion)` | only HTTP 400 with `application/problem+json` and exact contract `type`, `title`, and `detail` for unsupported NWS region |
| `Failure(IdentificationRejected)` | remote response rejects/misuses the required identity, including the contract-defined identification response |
| `Failure(Network)` | timeout, connectivity, or offline transport failure |
| `Failure(RateLimited)` | HTTP 429, retaining `Retry-After` when present |
| `Failure(ProviderUnavailable)` | HTTP 5xx or provider-unavailable response |
| `Failure(InvalidResponse)` | malformed declared problem envelope, malformed successful alert body, wrong content type, or otherwise unusable response |
| `Failure(UnexpectedProvider)` | any provider outcome not safely classified above |

No public result exposes `NwsAlertCollection` or another NWS DTO. The client
may use a named NWS-local intermediate, but a concrete NWS `AlertProvider`
adapter maps it to `List<WeatherAlert>` before returning. Mapping receives the
injected fetch time and provenance remains the Slice 23A model contract.

`AlertSuccessMetadata` is provider-neutral and typed:
`requestPoint: GeoPoint`, `providerId: String`, `fetchedAt: Instant`,
`cacheControl: String?`, `expires: String?`, `etag: String?`, and
`lastModified: String?`. Header names are captured case-insensitively. The
transport receives an injected `Clock`/time source and never calls
`Instant.now()` directly. No persistence or conditional requests are added.

The NWS base URL, query/identity values, and user-agent remain provider-local
and configurable. The local URI decision is: an absolute HTTPS base URI with
no existing query or fragment; coordinates use locale-safe decimal formatting
with latitude and longitude serialized separately in the provider's documented
query shape. Tests lock this decision without generalizing it into `:core`.

## Required implementation and tests

Use the existing Slice 23A classes `NwsAlertParserTest` and
`NwsAlertMapperTest`; do not rename them. Add transport/client and provider
boundary tests under `core/src/test/kotlin/com/oxygen/weather/core/provider/nws/`
and provider-contract tests under `core/src/test/kotlin/com/oxygen/weather/core/provider/`.
Use fixture-backed responses; no fabricated success or live network is needed
for focused tests.

The focused matrix must cover:

- valid empty and non-empty 2xx collections, with mapped `WeatherAlert`s and
  deterministic `fetchedAt`;
- exact metadata propagation for request point, provider ID, all four headers,
  and injected clock value;
- local non-finite/out-of-range coordinates as `InvalidPoint` with no request;
- valid unsupported-region problem (`UnsupportedRegion`);
- ordinary HTTP 400 (`InvalidRequest`);
- malformed declared problem envelope (`InvalidResponse`);
- remote identification rejection (`IdentificationRejected`);
- timeout/offline (`Network`), 429 plus `Retry-After` (`RateLimited`), 5xx
  (`ProviderUnavailable`), malformed 2xx body (`InvalidResponse`), and an
  unclassifiable provider response (`UnexpectedProvider`);
- case-insensitive response-header capture and URI/query/fragment/locale-safe
  coordinate decisions;
- public success/error values containing no NWS DTOs and mapper provenance
  retaining the deterministic fetched time.

Preserve the Slice 23A parser/mapper regression set, including
`parsesOneAlertFixtureAndRetainsProviderFields`,
`mapsFullAlertFixtureToProviderNeutralDomain`, and the existing invalid-input
tests. Keep forecast, app, Room, persistence, composition, UI, and background
refresh out of this slice.

Expected diff is limited to the provider-neutral alert result/metadata types,
NWS client/adapter/configuration, focused tests/fixtures, and the required
authority-sync Markdown/history changes. No dependency churn or raw response
body exposure.

## Verification ledger selected before work

**Budget:** 40 minutes / 14k tokens; one run of each passing command. Re-run
only after a relevant production/test/environment change, recording why.
Artifacts: `.codex/test-artifacts/2026-09-05-slice-23b-nws-alert-transport-boundary/`.

| Command | Evidence |
| --- | --- |
| `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests 'com.oxygen.weather.core.provider.nws.NwsAlertParserTest' --tests 'com.oxygen.weather.core.provider.nws.NwsAlertMapperTest'` | Slice 23A parser/mapper regression |
| `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests 'com.oxygen.weather.core.provider.nws.NwsAlertClientTest' --tests 'com.oxygen.weather.core.provider.nws.NwsAlertProviderTest' --tests 'com.oxygen.weather.core.provider.AlertProviderContractTest'` | transport classification, metadata, and public boundary |
| `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` | app compilation |
| `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` | unit regression |
| `. scripts/android-env.sh && ./gradlew :app:assembleDebug` | debug assembly |
| `git diff --check` | whitespace integrity |

If a bounded live NWS re-review is run, cap it at 60 seconds; record timeout or
failure as a blocker and never substitute it with fixture success. No emulator,
installation, connected test, app UI, persistence, or forecast composition is
selected for this transport-only slice.

## Execution evidence

Changed production files:

- `core/src/main/kotlin/com/oxygen/weather/core/provider/WeatherProviders.kt`
- `core/src/main/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertClient.kt`
- `core/src/main/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertProvider.kt`

Changed test files:

- `core/src/test/kotlin/com/oxygen/weather/core/provider/AlertProviderContractTest.kt`
- `core/src/test/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertClientTest.kt`
- `core/src/test/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertProviderTest.kt`
- `core/src/test/resources/providers/nws/alerts_problem_ordinary.json`

Evidence:

- `. scripts/android-env.sh && ./gradlew :core:compileDebugKotlin` passed.
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests 'com.oxygen.weather.core.provider.nws.NwsAlertParserTest' --tests 'com.oxygen.weather.core.provider.nws.NwsAlertMapperTest'` passed.
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests 'com.oxygen.weather.core.provider.nws.NwsAlertClientTest' --tests 'com.oxygen.weather.core.provider.nws.NwsAlertProviderTest' --tests 'com.oxygen.weather.core.provider.AlertProviderContractTest'` passed.
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed.
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed.
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed.
- `git diff --check` passed.

No emulator, connected test, live NWS request, persistence, UI, forecast
composition, or background-refresh command was run; these are outside this
transport-only acceptance boundary. The transport classification and provider
boundary are now covered by focused automated tests, and the slice is ready to
be recorded as committed once the git commit is created.
