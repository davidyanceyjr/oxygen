# Active Cycle

Status: committed
Cycle ID: 2026-09-03-fallback-real-path-verification
Mode: feature
Slice: Slice 32, Fallback Real-Path Verification

Goal: Verify the already-implemented Open-Meteo plus MET Norway fallback path
at the installed Android boundary, including cache restoration and later
Open-Meteo replacement, without adding new product behavior or widening the
provider architecture.

Basis:
- Slice 31A installed-app fallback wiring is committed at `4cdecdd`.
- Slice 31B fallback cache and provenance is committed at `4028044`.
- `.codex/plans/mvp-roadmap.md` selects Slice 32 as the next candidate after
  Slice 31B.
- The README reports installed-app MET Norway fallback, Room cache restoration,
  stale refresh failure context, and MET Norway cache-header/provenance
  persistence as implemented, while conditional GET/304, official alerts, and
  release-candidate fallback verification remain unimplemented.
- Current drift closed in this cycle: `.codex/plans/mvp-roadmap.md` required
  real-path fallback verification before MET Norway was described as active in
  Data Sources. Connected Slice 32 evidence now verifies the installed-boundary
  fallback/cache/replacement path, and `docs/data-sources/MET_NORWAY_FORECAST.md`
  has been reconciled with the active installed-app fallback status while
  leaving conditional GET/304, provider health/backoff, and release-candidate
  fallback verification unclaimed.

## Contract

Selected behavior:
- The installed app can still reach a normal Open-Meteo Home success by default
  for a manually selected location.
- A controlled fallback-eligible Open-Meteo terminal failure drives the
  installed factory through MET Norway fallback success.
- The fallback-served Home state exposes provider-neutral data with truthful
  MET Norway source, license, issued/update, fetched, model-estimate, and
  freshness/provenance text.
- A Room-cached fallback-served forecast restores offline as MET Norway data
  and remains visibly distinct from Open-Meteo and sample data.
- A later successful Open-Meteo refresh replaces a previously cached MET Norway
  forecast through the normal selected-location refresh/cache path.
- Fallback and cache replacement do not create, mutate, fake, couple, or claim
  official alert provider behavior.

Acceptance boundary:
- Deterministic connected/instrumented tests that run the same installed
  factory, Room forecast cache, DataStore selected-location storage, and Home
  presentation path used by `MainActivity` are the required proof.
- Slice 32 connected tests must use `RoomForecastCacheStorageFactory.create(...)`,
  `DataStoreSelectedLocationStorage`, `InstalledForecastRepositoryFactory.create(...)`,
  and `OxygenAppStateHolder` so the exercised path matches `MainActivity` wiring
  as closely as controllable tests allow.
- Add only test seams or production wiring needed to make the installed
  boundary controllable; do not change provider semantics, Home copy, cache
  schema, selected-location behavior, saved-location behavior, or UI layout
  unless a failing Slice 32 test proves a real defect.
- Prefer existing controllability first: `InstalledForecastRepositoryFactory.create(...)`
  already accepts injected default and fallback repositories, so new production
  seams are out of scope unless connected tests expose a real gap.
- Use provider fixtures/controlled transports for fallback failure/success
  scenarios. Use the real Open-Meteo path only for the default-success
  installed exercise if network is available and record that as environmental
  evidence, not as required or sole automated proof.
- Save logs and any screenshots/UI-tree dumps under
  `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/`.

Out of scope:
- Conditional GET requests, 304 not-modified handling, provider health/backoff,
  provider preference UI, alert provider implementation, saved-location
  save/remove UI, unit preferences, appearance/effects settings, background
  refresh, widgets, notifications, release-candidate status, MVP-readiness
  claims, or broad provider-selection abstractions.

## Design

- Start by extending `InstalledFallbackRepositoryInstrumentedTest` or adding a
  neighboring connected test class that composes `OxygenAppStateHolder` and
  `OxygenApp` with `InstalledForecastRepositoryFactory.create(...)`, production
  Room cache storage, and controlled Open-Meteo/MET Norway repositories injected
  through the factory's existing parameters.
- Keep controlled failures at the `WeatherRepository` boundary unless HTTP
  request assertions are required; keep MET Norway User-Agent/provenance checks
  where the existing client transport seam already supports them.
- Exercise Room persistence by writing fallback success through the factory,
  constructing a fresh state holder with an offline failing upstream, and
  asserting the restored Home state still reports MET Norway.
- Exercise later replacement by starting from cached MET Norway data, refreshing
  with a controlled Open-Meteo success, and asserting Home/source/cache readback
  switch to Open-Meteo for the same selected location.
- For official-alert independence, assert fallback success, offline fallback
  restore, and later Open-Meteo replacement all keep `dashboard.alerts.isEmpty()`,
  render no Home alert section, and preserve Room's forecast-only rejection for
  bundles that contain alerts.

## Workflow

Baseline:
- `git status --short`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*InstalledForecastRepositoryFactoryTest'`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'`
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*FallbackWeatherRepositoryTest'`
- `scripts/list-avds.sh`

Build and focused evidence:
- Add failing connected coverage for fallback-served Room restore and later
  Open-Meteo replacement through the installed factory path.
- Add focused assertions to existing installed fallback coverage for source,
  update/provenance, no sample data, empty Home alert state, no rendered alert
  section, and Room forecast-only alert rejection if they are not already
  observable.
- Implement only the smallest production/test-seam changes required by those
  tests.
- Run affected app unit tests and the focused connected Slice 32 test class.

Real-path exercise:
- Run `scripts/start-emulator.sh` and `scripts/install-debug.sh`.
- If network is available, use the installed app to select a real Open-Meteo
  geocoding result and capture Home evidence showing default Open-Meteo
  success.
- If live Open-Meteo is unavailable, record the exact network/provider blocker
  and continue with deterministic installed-boundary proof.
- Run the controlled connected fallback scenario to capture deterministic
  fallback success, offline fallback restore, and Open-Meteo replacement
  evidence. Prefer UI-tree dumps plus screenshots only where they add
  externally observable value.

Broad checks:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Artifacts target:
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/`

Planned artifact target:
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/planning-git-diff-check.log`

## Phase Results

- specified: Slice 32 is defined by the roadmap as installed Android boundary
  verification for Open-Meteo default success, controlled fallback success,
  fallback provenance, offline fallback restore, later Open-Meteo replacement,
  and proof that fallback/cache replacement does not create or mutate alert
  state. It also carries the active Data Sources wording gate for MET Norway:
  verify the installed-boundary fallback behavior and reconcile
  `docs/data-sources/MET_NORWAY_FORECAST.md`, or downgrade the active-provider
  wording before ready.
- planned: Bounded to deterministic installed-boundary verification and minimal
  test seams for already-implemented fallback/cache behavior.
- covered: Extended `InstalledFallbackRepositoryInstrumentedTest` with
  deterministic connected coverage that uses
  `RoomForecastCacheStorageFactory.create(...)`,
  `DataStoreSelectedLocationStorage`,
  `InstalledForecastRepositoryFactory.create(...)`, and
  `OxygenAppStateHolder` for fallback-served Room restore and later
  Open-Meteo replacement. The connected tests assert MET Norway/Open-Meteo
  source, license, issued/fetched/model-estimate provenance, no sample data,
  empty Home alert state, and no rendered alert section. Existing
  `RoomForecastCacheStorageInstrumentedTest` covers Room forecast-only alert
  rejection.
- implemented: No production Kotlin behavior changed. The MET Norway provider
  contract wording now reflects the verified active installed-app fallback
  status and keeps later conditional GET/304, provider health/backoff, and
  release-candidate fallback behavior out of scope.
- verified: Baseline checks passed for app installed factory, app Home forecast
  state, core fallback repository, and `scripts/list-avds.sh`. Focused checks
  passed for app installed factory, app Home forecast state, core fallback
  repository, connected installed fallback class, and connected Room forecast
  cache class. `scripts/start-emulator.sh` and `scripts/install-debug.sh`
  passed against `oxygen_starter`. Broad compile, app/core unit tests,
  assemble, and `git diff --check` passed.
- committed: committed in this changeset.

Artifacts:
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/baseline-installed-forecast-repository-factory-test.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/baseline-home-forecast-state-holder-test.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/baseline-fallback-weather-repository-test.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/baseline-list-avds.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/focused-installed-forecast-repository-factory-test.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/focused-home-forecast-state-holder-test.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/focused-fallback-weather-repository-test.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/focused-installed-fallback-connected-test-final.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/focused-room-forecast-cache-connected-test.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/start-emulator.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/install-debug.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/broad-compile-debug-kotlin.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/broad-debug-unit-tests.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/broad-assemble-debug.log`
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/broad-git-diff-check.log`

Notes:
- Initial focused connected run failed because no connected device was
  available. After `scripts/start-emulator.sh` brought up `oxygen_starter`, a
  rerun exposed a test expectation mismatch for MET Norway's actual combined
  license string; the corrected connected rerun passed.
- Live manual Open-Meteo geocoding selection was not run; deterministic
  connected installed-boundary coverage exercised the default Open-Meteo
  success/replacement path without relying on provider/network availability.
