# Active Cycle

Status: committed
Cycle ID: 2026-09-03-fallback-cache-provenance
Mode: feature
Slice: Slice 31B, Fallback Cache and Provenance

Goal: Preserve truthful provider identity, source/license/update timestamps,
and MET Norway cache metadata across fallback success, Room cache persistence,
offline restore, later Open-Meteo replacement, and foreground refresh failure.

Basis:
- Slice 31A installed-app fallback wiring is committed at `4cdecdd`.
- The installed app now composes Open-Meteo as default, MET Norway as fallback,
  and the existing Room-backed cache wrapper for the selected-location Home path.
- `.codex/plans/mvp-roadmap.md` selects Slice 31B as the next candidate after
  Slice 31A.
- `docs/data-sources/MET_NORWAY_FORECAST.md` requires MET Norway fallback
  success to keep MET Norway attribution, license, source/update, stale/cache
  metadata, and provider ID.

## Contract

Selected behavior:
- A MET Norway fallback success stores and restores as a MET Norway forecast,
  with provider-neutral provenance still identifying MET Norway.
- MET Norway provider-specific cache metadata is persisted with the cached
  forecast where needed for truthful restore and later revalidation behavior.
- Cached MET Norway restore must not be relabeled as Open-Meteo, generic cached
  data, or unavailable source.
- A later successful Open-Meteo refresh replaces a cached MET Norway forecast
  only through the normal verified cache replacement transaction.
- A failed foreground refresh while a MET Norway fallback forecast is visible
  retains the visible forecast as stale with truthful MET Norway source,
  license, update, and refresh-failure context.
- UI, app state, and cache consumers continue to receive provider-neutral
  domain/provenance/freshness state. Raw MET Norway headers, endpoint details,
  symbol codes, and provider-specific errors stay in provider/cache code.

Acceptance boundary:
- Extend the current forecast cache/storage path only as much as required to
  preserve provider identity/provenance and MET Norway cache metadata
  truthfully.
- Keep existing selected-location, saved-location, manual search, retry, and
  fallback eligibility behavior unchanged.
- Add focused tests that prove MET Norway fallback cache write/read,
  Open-Meteo replacement after cached MET Norway data, stale failed-refresh
  provenance retention, and provider-neutral UI/app-state exposure.
- Add Room/instrumented coverage if schema or persisted metadata changes.
- Save logs under
  `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/`.

Out of scope:
- Provider preference UI, saved-location save/remove UI, background refresh,
  unit preferences, official alerts, air quality, radar/maps, widgets,
  notifications, release-candidate status, MVP-readiness claims, or a broad
  provider-selection framework.

## Design

- Prefer extending existing cache DTO/entity mapping over adding a parallel
  fallback cache.
- Keep MET Norway HTTP cache directives provider-specific at the storage or
  provider boundary; expose only normalized cache/freshness/provenance state to
  Home.
- If a Room schema change is required, add an explicit migration and connected
  migration coverage.
- Preserve the existing `CachedWeatherRepository` wrapper shape unless the
  current interface cannot carry required metadata without provider leakage.

## Workflow

Baseline:
- `git status --short`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*InstalledForecastRepositoryFactoryTest'`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'`
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*CachedWeatherRepositoryTest'`

Build and focused evidence:
- Inspect current Room forecast-cache schema and domain cache model.
- Add focused failing coverage for MET Norway provenance/cache restoration and
  stale failed-refresh retention before production edits.
- Implement the smallest cache/storage/domain changes required by those tests.
- Run affected app/core unit tests.

Real-path exercise:
- If Room schema or metadata persistence changes, run connected Room/cache
  coverage and an installed/state-holder restore path that observes MET Norway
  source/provenance after cache restore.

Broad checks:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Artifacts target:
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/`

## Phase Results

- specified: Slice 31B is defined by the roadmap as fallback cache and
  provenance after committed installed-app fallback wiring.
- planned: Bounded to cache/provenance truthfulness for MET Norway fallback,
  replacement by later Open-Meteo success, stale failed-refresh retention, and
  provider-neutral exposure.
- covered: Added focused core repository tests for cache metadata write-through,
  MET Norway cache metadata on repository success, stale MET Norway cache
  retention, and Open-Meteo replacement after cached MET Norway data. Added
  connected Room tests for MET Norway cache-header metadata persistence and
  clearing on non-metadata replacement. Updated disclosure tests for truthful
  implemented/unimplemented cache status.
- implemented: `WeatherRepositoryResult.Success` can carry cache-only
  `ForecastCacheMetadata`; `CachedWeatherRepository` writes that metadata only
  when the storage boundary supports it. `MetNoWeatherRepository` maps MET
  Norway response cache headers, response coordinates/elevation, provider
  updated time, provider ID, and fetch time into cache metadata. Room forecast
  cache storage now persists that metadata with a v2-to-v3 migration.
- verified: Baseline checks passed for installed forecast factory, Home
  forecast state, and cached weather repository. Focused core unit checks,
  About disclosure checks, connected Room forecast-cache checks, and connected
  saved-location migration checks passed. Broad compile, app/core unit tests,
  assemble, and `git diff --check` passed.
- committed: committed in this changeset.

Artifacts:
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/baseline-git-status.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/baseline-installed-forecast-factory.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/baseline-home-forecast-state.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/baseline-cached-weather-repository.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/red-cache-metadata-storage.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/red-metno-cache-metadata.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/focused-cache-metadata-unit.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/focused-core-cache-metno-unit.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/focused-room-forecast-cache-rerun-2.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/focused-room-saved-location-migration-rerun.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/focused-about-disclosure.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/broad-compile-debug-kotlin.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/broad-unit-tests.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/broad-assemble-debug-rerun.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/broad-git-diff-check-rerun.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/broad-compile-debug-kotlin-doc-sync.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/broad-unit-tests-doc-sync.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/broad-assemble-debug-doc-sync.log`
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/broad-git-diff-check-doc-sync.log`

Notes:
- Initial connected Room attempts failed before useful assertions when no
  device was connected, then again when two connected Gradle invocations ran in
  parallel and the instrumentation process crashed. Sequential reruns passed on
  `oxygen_starter(AVD) - 17`.
- Conditional GET requests, 304 not-modified handling, provider health/backoff,
  release-candidate fallback verification, saved-location save/remove UI, unit
  preferences, alerts, air quality, and radar remain out of scope and
  unimplemented.
