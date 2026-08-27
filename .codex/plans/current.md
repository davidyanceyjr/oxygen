# Active Cycle

Status: committed
Cycle ID: 2026-08-27-failed-refresh-retains-cached-forecast
Mode: feature
Goal: Implement Slice 17 by making a foreground refresh failure retain and emit a useful cached forecast for the selected `LocationId`, then showing Home as usable but stale with explicit refresh-failed metadata and retry. Do not claim offline launch, saved-location persistence, or broad offline-first behavior.
Roadmap slice: Slice 17: Failed Refresh Retains Cached Forecast.
Branch or work context: local `oxygen` Android scaffold on top of committed Slice 16 `3bee3b1`.
Specification anchors:
- `AGENTS.md`
- `README.md`
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 16, 17, 39, 40, 41, 48, and 50
- `.codex/plans/mvp-roadmap.md` Slice 17, Forecast Provider Scope, MVP Acceptance Boundary, Release Gate, and Cache/Offline/Stale gates
- `docs/data-sources/OPEN_METEO_FORECAST.md`
- `docs/data-sources/MET_NORWAY_FORECAST.md`
- `DATA_SOURCES.md` and `PRIVACY.md`
- Existing `WeatherRepository`, `WeatherRepositoryResult`, `ForecastError`, `WeatherBundle`, `WeatherLocation`, `DataProvenance`, `CachedWeatherRepository`, `ForecastCacheStorage`, `FileForecastCacheStorage`, and `HomeForecastPresentationState` behavior

Acceptance criteria:
- A foreground refresh that fails after a useful cached forecast exists for the same stable `WeatherLocation.id` / `LocationId` must emit a provider-neutral cached-forecast success state instead of replacing Home with `NoCacheError`.
- Cached-retained results must preserve the cached `WeatherBundle` current/hourly/daily forecast values, location identity/display/coordinates/elevation/timezone, bundle `fetchedAt`, and provenance exactly as persisted by Slice 16. Do not merge failed-provider metadata into the cached weather values or fabricate missing fields.
- Retained-cache success must carry explicit stale/refresh-failed metadata on `WeatherRepositoryResult.Success` through a provider-neutral freshness/status model. Home app state and Composables must consume that structured metadata rather than inferring stale status from text. The metadata must include stale age based on an injectable clock, the failure reason mapped through the existing provider-neutral `ForecastError`, and source/update/provenance status from the cached bundle.
- Home UI/state must remain usable and visibly stale after refresh failure with cache: forecast dashboard remains visible, stale age is exposed as text, source/update status remains visible, refresh-failed metadata is visible, and retry remains available for the same selected `WeatherLocation`.
- Refresh failure without cache must preserve the existing retryable no-cache error behavior.
- Failed refresh for one `LocationId` must not read, emit, or display another location's cached forecast.
- Upstream loading behavior must not cause an empty or error replacement when the current Home state already has useful data for the same selected location.
- Cache read failure during failed-refresh retention must emit `WeatherRepositoryResult.Failure(ForecastError.LocalCacheFailure)` when `ForecastCacheStorage.readBundle(location.id)` throws. A null read is the existing no-cache signal in `ForecastCacheStorage` and must preserve the retryable no-cache provider failure. Do not fabricate stale success from a missing or unreadable cache, and do not report thrown local cache/storage failure as provider unavailable, invalid provider response, or unexpected provider failure.
- Provider DTOs, raw JSON, HTTP headers, provider-specific error bodies, WMO weather codes, MET Norway symbol codes, provider client result/error classes, and provider cache-header details must not cross into cache consumers, app state, or Composables.
- Slice 17 may add provider-neutral result/state fields needed for stale-cache presentation. Keep the change narrow; do not introduce generic sync policies, background work, cache expiration enforcement, provider health/backoff, settings storage, saved-location storage, migrations, or a broad offline repository abstraction.
- App default forecast wiring may be revised only as far as needed to exercise installed-app foreground stale-cache behavior for a selected in-memory location. If Android context/database lifecycle or durable app cache wiring is added, evidence must prove it through an installed-app foreground refresh path. If that is too broad, keep installed-app wiring unchanged and report Slice 17 as verified at the repository plus app-state boundary only.
- MET Norway live fallback, active installed-app fallback wiring, offline launch from cache, process-restart cache recovery, saved-location persistence, unit preferences, alert cache, air-quality cache, radar cache, background refresh, dependency license generation, and release behavior remain out of scope.
- Disclosure documents and in-app About text may be updated only after verified behavior exists. They must distinguish foreground failed-refresh stale-cache retention from unimplemented offline launch, saved-location persistence, and broad offline cache behavior.

Acceptance boundary: Slice 17 is complete when focused repository/cache tests fail before implementation and pass after implementation for failed refresh with same-location cache, no-cache failure, wrong-location isolation, cache-read failure behavior, repository-owned stale metadata, retry availability inputs, and preservation of cached values/provenance; focused app state or Compose tests prove Home remains on the cached dashboard with visible stale age/source/update/failure/retry after refresh failure; focused app state tests prove an in-progress same-location refresh does not replace an already useful `ForecastReady` dashboard with a loading-only screen; a real-path foreground JVM or Android state-holder exercise through the cache-retention repository path records success-cache-then-failure-retains-cache for one explicit `WeatherLocation`; static leak and cache/offline claim checks pass; broad Android verification passes. Slice 17 does not prove offline app relaunch, selected-location persistence, saved-location switch/remove behavior, cache expiration policy enforcement, background work, alert/air-quality/radar cache, unit preferences, active installed-app MET Norway fallback, installed-app cache behavior unless app cache wiring is added and exercised, or release readiness.

Boundary decisions:
- Prefer extending `CachedWeatherRepository` rather than adding a parallel cache wrapper, because Slice 16 already established that boundary around provider-neutral `WeatherBundle`.
- Add provider-neutral repository-owned freshness/status metadata to `WeatherRepositoryResult.Success`, for example a default fresh status and a stale-after-failed-refresh status carrying stale age and refresh failure. The selected shape must make stale status observable without requiring app state or Composables to infer it from text.
- Stale age is presentation metadata derived from cached bundle timestamps and an injectable clock. Do not use wall-clock calls directly in tests.
- Retain cached data only for refresh failures after the upstream terminal result is a failure. Do not skip successful provider refreshes merely because cache exists.
- Treat transient/provider-side `ForecastError.NetworkUnavailable`, `RateLimited`, `ProviderUnavailable`, `InvalidResponse`, and `UnexpectedProviderFailure` as eligible for stale-cache retention when a same-location cached bundle exists. Treat `ForecastError.ProviderRejectedRequest` as an immediate failure for this slice unless a later, explicit contract justifies showing stale data after likely request/configuration rejection. Treat `ForecastError.LocalCacheFailure` as a local cache/storage failure; if cache read throws, emit `Failure(LocalCacheFailure)`. If cache read returns null, preserve the original retryable no-cache failure because the storage contract uses null for missing cache.
- Keep alerts and air quality out of retained forecast behavior unless already present inside the cached `WeatherBundle`; no new lookup/cache behavior is added for them.
- Use artifact directory `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/` for all new logs. Do not modify or replace Slice 16 evidence.

Focused evidence to produce:
- A pre-fix red focused repository log after adding stale-cache retention tests and before implementing production behavior. At least one compiled behavioral assertion must fail because failed refresh with cache currently emits `Failure` instead of cached stale success.
- Focused core tests proving provider success writes/reads cache as before and a later same-location provider failure emits the cached bundle with stale/refresh-failed metadata rather than `Failure`.
- Focused core tests proving failed refresh without cache emits the existing retryable no-cache failure path.
- Focused core tests proving failed refresh for a different `LocationId` does not emit another location's cached forecast.
- Focused core tests proving stale age and refresh-failed metadata are deterministic with an injected clock and preserve the original cached source/update/provenance fields.
- Focused core tests proving thrown cache read failure during retention maps to provider-neutral local cache/storage failure, while a null same-location read preserves the existing retryable no-cache failure.
- Focused app state or Compose tests proving Home remains on a forecast dashboard after failed refresh with cache, exposes stale age text, source/update status, refresh-failed message, and retry for the same selected location.
- Focused app state tests proving Home already showing `ForecastReady` for the selected location keeps the dashboard visible during a same-location refresh `Loading` emission and exposes refresh-in-progress status without losing the previous useful forecast.
- Focused app state or Compose tests proving no-cache failure still renders the existing error state and retry.
- Static no-provider-detail-leak check: after implementation, run this against `app/src/main/kotlin` and every production cache/storage path actually touched under `core/src/main/kotlin`: `rg -n "OpenMeteoForecastResponse|OpenMeteoCurrent|OpenMeteoHourly|OpenMeteoDaily|OpenMeteoForecastClientResult|OpenMeteoForecastClientError|OpenMeteoGeocodingDto|OpenMeteoGeocodingResult|MetNoForecastResponse|MetNoGeometry|MetNoMeta|MetNoTimeStep|MetNoInstant|MetNoPeriod|MetNoForecastClientResult|MetNoForecastClientError|X-ErrorClass|symbolCode|weather_code" app/src/main/kotlin <actual-core-cache-storage-paths>`. Record the exact command in Phase Results before reporting verified.
- Static cache/offline-claim check: `rg -n "offline|stale|failed refresh|failed-refresh|saved location|saved-location|cache|cached|fallback" README.md DATA_SOURCES.md PRIVACY.md app/src/main/kotlin core/src/main/kotlin`
- Static plan-audit check: `rg -n "offline|stale|failed refresh|failed-refresh|saved location|saved-location|cache|cached|fallback" .codex/plans/current.md`; allowed matches are the planned Slice 17 scope, explicit exclusions, fallback-provider references, and this static-check definition.
- Save focused test and static-check logs under `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/` and record project-local paths in Phase Results before reporting the slice ready.

Real-path command or procedure:
- Run a live JVM or Android state-holder exercise through a cache-retention repository path for one explicit `WeatherLocation`: first refresh succeeds through a controlled or live provider-backed cache write, second refresh fails with a provider-neutral network/offline failure, `CachedWeatherRepository` emits `Success` with stale-after-failed-refresh metadata, and emitted/presented state remains cached stale success with source/provenance, stale age, refresh-failed metadata, and retry. This is foreground refresh evidence only.
- No process restart, airplane-mode relaunch, saved-location recovery, MET Norway live fallback, background refresh, or emulator offline-launch evidence is required or accepted for this slice.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
- Save broad verification logs under `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/` and record project-local paths in Phase Results before reporting the slice ready.

Current gate: verified
Current phase: ready
Last result: Slice 17 is implemented and verified at the repository plus app-state/UI boundary. `CachedWeatherRepository` now emits same-location cached forecasts as stale `WeatherRepositoryResult.Success` after eligible foreground refresh failures, with provider-neutral freshness metadata and deterministic stale age. Home keeps an existing dashboard visible during same-location loading and displays stale refresh-failed metadata with retry when the repository emits stale success. App default durable cache wiring, installed-app offline cache behavior, offline launch, saved-location persistence, unit preferences, alert/air-quality/radar cache, background work, release behavior, and active installed-app MET Norway fallback remain out of scope and unimplemented.
Blocker: none.

## Implementation Plan

1. Inspect current `CachedWeatherRepository`, `ForecastCacheStorage`, `WeatherRepositoryResult`, `ForecastError`, `OxygenAppStateHolder`, and Home UI presentation tests to choose the smallest provider-neutral stale metadata shape.
2. Add focused failing core tests for same-location failed-refresh retention, no-cache failure, wrong-location isolation, stale age metadata, refresh-failed metadata, cache read failure, and cached-value/provenance preservation.
3. Save the pre-fix red focused-test log under `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/`.
4. Implement repository-owned provider-neutral freshness/status metadata on `WeatherRepositoryResult.Success` and the cache behavior in the existing cache boundary, keeping provider details out of the public result and UI state.
5. Add focused failing app state or Compose tests for Home stale-cache presentation after refresh failure, same-location loading retention while already `ForecastReady`, and unchanged no-cache error presentation.
6. Implement Home presentation fields and UI text for stale age, refresh-in-progress status, refresh-failed status, source/update status, and retry while keeping the existing forecast dashboard visible.
7. Run focused core/app tests and save logs.
8. Run the real-path foreground JVM or Android state-holder refresh exercise and save evidence.
9. Run static provider-detail leak and cache/offline-claim checks; update README, DATA_SOURCES, PRIVACY, and in-app disclosure only if verified production behavior requires wording changes. If app default cache wiring is not added and exercised, do not update disclosures to imply installed-app cache behavior.
10. Run broad Android verification commands and `git diff --check`, saving logs in the Slice 17 artifact directory.
11. Review the diff for slop: remove unused abstractions, placeholder text, dead flags, TODOs, fabricated cache metadata, and unverified offline/saved-location claims.
12. Append `.codex/cycles/history.md` only when Slice 17 is actually verified or committed.

## Known Starting Conditions

- Slice 16 committed `CachedWeatherRepository`, `ForecastCacheStorage`, and `FileForecastCacheStorage`.
- `CachedWeatherRepository` currently emits upstream failures directly; it does not read cached data after a failed refresh.
- `WeatherRepositoryResult.Success` currently carries only `WeatherBundle`; no stale or refresh-failed metadata exists at the repository boundary.
- `OxygenAppStateHolder` currently maps every `WeatherRepositoryResult.Failure` to `HomeForecastPresentationState.NoCacheError`.
- `HomeForecastPresentationState.ForecastReady` currently represents a fresh success dashboard and has no stale/refresh-failed metadata.
- App default forecast wiring currently constructs `OpenMeteoWeatherRepository()` and does not wire durable app cache behavior.
- README, DATA_SOURCES, PRIVACY, and in-app About content currently say repository-level cache persistence exists but installed-app cache wiring, stale offline UI, offline forecast cache behavior, and saved-location persistence are not implemented.

## Phase Results

- planned: Selected Slice 17 as the next roadmap slice and bounded it to foreground failed-refresh stale-cache retention for the selected forecast path. The plan explicitly excludes offline app relaunch, saved-location persistence, unit preferences, alert/air-quality/radar caches, background work, release behavior, and active installed-app MET Norway fallback.
- covered: Pre-fix red focused cache log saved at `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/pre-fix-red-cache-tests.log`; tests compiled and failed because failed refresh with same-location cache still emitted failure instead of cached stale success, and retention-path cache read behavior was absent.
- implemented: Added provider-neutral `ForecastFreshness` on `WeatherRepositoryResult.Success`, extended `CachedWeatherRepository` with an injectable `Clock`, eligible failed-refresh retention from `ForecastCacheStorage.readBundle(location.id)`, stale age metadata, refresh failure metadata, and local cache failure mapping for thrown read failures. Null cache reads preserve the existing retryable no-cache provider failure because `ForecastCacheStorage` uses null as the missing-cache signal.
- implemented: Added Home `ForecastReady` freshness/refresh-in-progress/retry presentation, kept same-location refresh loading from replacing an existing useful dashboard, and added Compose text/buttons for cached stale refresh-failed status while preserving source/update/provenance dashboard content.
- verified: Focused cache tests passed with `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*CachedWeatherRepositoryTest'`; log saved at `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/focused-cache-tests.log`. Tests cover provider success write/readback, success-then-failed-refresh same-location retention, no-cache failure preservation, wrong-location isolation, deterministic stale age, refresh failure metadata, cached value/provenance preservation, thrown cache read failure mapping, and rejected-request no-retention behavior.
- verified: Focused Home state tests passed with `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'`; log saved at `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/focused-home-state-tests.log`. Tests cover stale success dashboard presentation with source/update/failure/retry metadata, same-location loading retention while already ready, and unchanged no-cache retryable errors.
- verified: Foreground JVM cache-retention exercise passed with `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests 'com.oxygen.weather.core.provider.cache.CachedWeatherRepositoryTest.successThenLaterFailedRefreshRetainsCachedForecastForSameLocation'`; log saved at `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/real-path-cache-retention-exercise.log`. The exercise writes cache on first success for explicit `manual-chicago`, then a second refresh fails with `ForecastError.NetworkUnavailable` and emits cached stale success with Open-Meteo-like provenance and 45-minute stale age.
- verified: Static provider-detail leak check returned no matches; log saved at `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/static-provider-detail-leak-check.log`. Exact command: `rg -n "OpenMeteoForecastResponse|OpenMeteoCurrent|OpenMeteoHourly|OpenMeteoDaily|OpenMeteoForecastClientResult|OpenMeteoForecastClientError|OpenMeteoGeocodingDto|OpenMeteoGeocodingResult|MetNoForecastResponse|MetNoGeometry|MetNoMeta|MetNoTimeStep|MetNoInstant|MetNoPeriod|MetNoForecastClientResult|MetNoForecastClientError|X-ErrorClass|symbolCode|weather_code" app/src/main/kotlin core/src/main/kotlin/com/oxygen/weather/core/provider/cache`.
- verified: Static cache/offline claim and plan-audit checks were reviewed for scoped language; logs saved at `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/static-cache-offline-claim-check.log` and `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/static-plan-audit-check.log`. README, DATA_SOURCES, PRIVACY, and in-app About now distinguish repository/app-state foreground stale retention from unimplemented installed-app durable cache wiring and offline launch/cache behavior.
- verified: Broad checks passed: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`, `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`, `. scripts/android-env.sh && ./gradlew :app:assembleDebug`, and `git diff --check`. Logs saved at `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/broad-compile-debug-kotlin.log`, `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/broad-unit-tests.log`, `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/broad-assemble-debug.log`, and `.codex/test-artifacts/2026-08-27-failed-refresh-retains-cached-forecast/git-diff-check.log`.
- committed: Slice 17 is being committed as one focused changeset after diff review.
