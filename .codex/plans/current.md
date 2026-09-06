# Slice 23C — Alert Repository Merge

**Status:** committed
**Planning basis:** local `main` `3c658a8` (Slice 23C), reviewed 2026-09-06
**Cycle ID:** `2026-09-06-slice-23c-alert-repository-merge`

## Decision and acceptance boundary

Slices 22 (`d0a7eb3`), 23A (`17dab0c`), and 23B (`dcf707b`) are committed
prerequisites. Slice 23C combined a completed forecast result with an
independent NWS alert lookup in `:core`; it does not activate installed-app
alert UI, persist alerts, change Room, or alter forecast fallback/cache
semantics.

The current `AlertProvider` is `suspend`, but `WeatherRepository.refresh` is a
synchronous `Sequence` and the NWS client already performs blocking
`HttpURLConnection` I/O. Before implementing the decorator, this slice will
correct the provider contract to a synchronous blocking operation:

```kotlin
interface AlertProvider {
    val id: String
    fun getActiveAlerts(location: GeoPoint): AlertProviderResult
}
```

This is deliberately not a coroutine migration: do not add coroutines, use
`runBlocking`, or introduce a continuation bridge. Update the obsolete
`suspend` declaration in the specification at the same time, and adjust 23B
tests to call the direct method. The provider implementation must retain its
existing classified result boundary and bounded transport timeouts. This makes
the callable production boundary explicit before composition begins.

Acceptance is a forecast `Success` whose `WeatherBundle.alerts` and explicit
alert lookup status faithfully represent the independent lookup. `Loading` and
forecast `Failure` pass through unchanged and do not trigger alert work.

## Production design

Reuse the existing 23B types; do not create `AlertRepositoryResult`, a second
alert-error taxonomy, or another NWS adapter. `NwsAlertProvider` is already the
provider-neutral adapter and returns `AlertProviderResult.Success(alerts,
metadata)` or `Failure(AlertProviderError)`.

Add `AlertLookupStatus` in `WeatherProviders.kt`:

```kotlin
sealed interface AlertLookupStatus {
    data object NotRequested : AlertLookupStatus
    data object NoAlerts : AlertLookupStatus
    data object Available : AlertLookupStatus
    data object UnsupportedRegion : AlertLookupStatus
    data class Failed(val error: AlertProviderError) : AlertLookupStatus
}
```

Extend `WeatherRepositoryResult.Success` source-compatibly with
`alertStatus: AlertLookupStatus = AlertLookupStatus.NotRequested`. Retaining
the 23B `AlertProviderError` in `Failed` keeps an alert failure distinct from a
forecast error; an empty list never represents a failed request.

Add `AlertMergingWeatherRepository` as the outer decorator:

```text
Open-Meteo/MET Norway -> FallbackWeatherRepository -> CachedWeatherRepository
    -> AlertMergingWeatherRepository
```

For each upstream terminal `Success`, call
`alertProvider.getActiveAlerts(location.point)` exactly once, replace rather
than append `weather.alerts`, and yield the copied success. Map a successful
nonempty list to `Available`, a successful empty list to `NoAlerts`,
`Failure(UnsupportedRegion)` to `UnsupportedRegion`, and every other failure
to `Failed(error)` with an empty alert list. Do not perform a lookup for an
upstream loading or terminal forecast failure.

The decorator preserves every field present on the upstream success: forecast
weather other than its alert list, forecast provenance/fetched time,
`freshness`, and the received `cacheMetadata`. This is intentionally narrower
than claiming cache metadata survives `CachedWeatherRepository`: that existing
decorator can discard it after a write/readback, and fixing that is outside
23C. A fresh alert response must never reset a stale forecast's age or
refresh-failure context. Forecast source identity, including a MET Norway
fallback success, must not affect whether NWS is called.

The forecast cache remains forecast-only. Alert merging occurs after it, and no
alert-bearing bundle reaches `ForecastCacheStorage.replaceBundle`; no schema or
cache-format change is allowed.

## Deterministic alert policy

NWS alert IDs are opaque. At merge time, deduplicate only exact IDs. Preserve
the source position of an ID's first occurrence, but replace that position's
value with its last occurrence in the response; unique IDs retain source order.
Thus `[a1, b, a2, c, b2]` becomes `[a2, b2, c]`. This is deterministic without
inventing lifecycle, severity, headline, event, or reference inference. Mapper
fixtures may retain duplicates; the repository owns the final composition
policy.

## Focused tests

Add `AlertMergingWeatherRepositoryTest` and update the affected 23B provider
tests for the synchronous contract. Use fixed forecast repositories and a
recording `AlertProvider`; no test may use a custom suspend/continuation helper.
The focused set must prove:

1. loading and terminal forecast failure are forwarded byte-for-byte and make
   zero alert calls;
2. a forecast success calls NWS once with the selected `location.point`,
   replaces preexisting alerts, and reports `Available`;
3. empty provider success is `NoAlerts`, while
   `Failure(UnsupportedRegion)` is `UnsupportedRegion`;
4. every non-unsupported `AlertProviderError`, including rate limit, produces
   forecast success with `Failed(error)` and no alerts;
5. duplicate IDs use the first-position/last-value rule and retain deterministic
   ordering;
6. stale cached and fallback-provenance successes each call the provider once
   while retaining forecast freshness, fetched time, provider provenance, and
   any cache metadata delivered to the merge decorator; and
7. a recording forecast cache in the required composition proves it receives no
   alert-bearing bundle.

Keep the 23A parser/mapper and 23B client/provider regressions. Do not add UI,
app-factory wiring, Room, alert cache, retry, background work, a DI framework,
or a generic orchestration layer.

## Verification

Before changes, run the focused 23A/23B and forecast-cache baseline. After
focused green, run:

```sh
. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*AlertMergingWeatherRepositoryTest' --tests '*AlertProviderContractTest' --tests '*NwsAlertProviderTest' --tests '*NwsAlertClientTest' --tests '*CachedWeatherRepositoryTest'
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

For one non-UI production-path exercise, create a disposable, untracked core
JUnit live-check that constructs the production `NwsAlertProvider` with its
default client and invokes it for Madison's valid point. Run only that check
with `timeout 60s`, save stdout/stderr to
`.codex/test-artifacts/2026-09-06-slice-23c-alert-repository-merge/nws-live-check.log`,
then remove the check. It must assert only a classified `AlertProviderResult`,
not a particular alert count. A timeout or provider failure is recorded once as
a blocker; deterministic fixtures cover failure independence and active-alert
content. This exercise has no app wiring, persistence, or UI surface.

Record all selected-command results and the disposable-check removal in the
artifact ledger. The slice is ready only when the synchronous contract,
independent merge behavior, duplicate policy, forecast-only cache boundary,
focused tests, production live check (or recorded bounded blocker), and broad
checks have evidence. Update the plan, live history, README, and specification
to factual post-commit state after the implementation commit.

## Execution evidence

Changed production files:

- `core/src/main/kotlin/com/oxygen/weather/core/provider/WeatherProviders.kt`
- `core/src/main/kotlin/com/oxygen/weather/core/provider/AlertMergingWeatherRepository.kt`
- `core/src/main/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertProvider.kt`

Changed test files:

- `core/src/test/kotlin/com/oxygen/weather/core/provider/AlertMergingWeatherRepositoryTest.kt`
- `core/src/test/kotlin/com/oxygen/weather/core/provider/AlertProviderContractTest.kt`
- `core/src/test/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertProviderTest.kt`

Evidence is recorded in `.codex/test-artifacts/2026-09-06-slice-23c-alert-repository-merge/ledger.md`.
The disposable live-check passed for Madison's point and was removed after
the run. The implementation is committed in `3c658a8`.
