# Active Cycle

Status: ready
Cycle ID: 2026-09-03-installed-app-fallback-wiring
Mode: feature
Slice: Slice 31A, Installed-App Fallback Wiring

Goal: Wire the installed app forecast path so Open-Meteo remains the default
forecast source and MET Norway can serve a Home forecast only after an explicit
fallback-eligible Open-Meteo failure, without changing saved-location behavior,
cache schema, provider DTO boundaries, or Home presentation semantics.

Basis:
- Slice 14 implemented and verified core fallback-selection behavior.
- Slice 18J-R restored the installed Open-Meteo ready forecast path at
  `15fc10e`.
- Slice 18J visual convergence evidence committed at `7950a42`.
- Slice 19A saved-location storage is committed at `d97e2ea`.
- Slice 19B saved-location selection/concurrency is committed at `0f649aa`.
- Slice 19C saved-location list/select UI is committed at `e2efdd3`.
- `.codex/plans/mvp-roadmap.md` selects Slice 31A as the next candidate and
  explicitly states that saved locations are not a prerequisite unless this
  implementation touches saved-location behavior.
- `docs/data-sources/MET_NORWAY_FORECAST.md` requires MET Norway attribution,
  identifying User-Agent behavior, provider-neutral mapping, provider-specific
  cache discipline, and no averaging or merging with Open-Meteo values.

## Contract

Selected behavior:
- The installed app composes the active forecast repository as Open-Meteo
  default plus MET Norway fallback through the existing provider-neutral
  `FallbackWeatherRepository`, behind the existing selected-location Home
  refresh path.
- Slice 31A activates MET Norway only as a foreground fallback after an eligible
  Open-Meteo terminal failure. Provider-specific MET Norway cache-header
  persistence, conditional revalidation, cached fallback restore, and stale
  fallback provenance remain later behavior unless the authority check below
  determines they are required before any active installed-app MET Norway
  request.
- Open-Meteo success remains terminal and does not call MET Norway.
- Open-Meteo failures that are not fallback-eligible, including offline/network
  failure and provider-rejected request, remain terminal and do not call MET
  Norway.
- Open-Meteo fallback-eligible failures, including provider unavailable, rate
  limited, invalid response, and unexpected provider failure, can call MET
  Norway and surface a MET Norway `WeatherBundle` through existing Home success
  presentation.
- Provider-specific DTOs, endpoint parameters, response headers, symbol codes,
  and provider-specific errors stay inside provider/repository code. UI and app
  state receive only provider-neutral `WeatherRepositoryResult`,
  `WeatherBundle`, provenance, freshness, and presentation state.
- Manual selected locations, saved-location selection, offline cache restore,
  foreground stale-cache retention, and retry behavior continue to use the same
  `LocationId`-scoped app-state path.

Acceptance boundary:
- Production code is limited to installed-app forecast repository composition
  and any small app-local factory needed to make that composition testable.
- Keep the existing Room forecast cache wrapper and storage format. Slice 31A
  may pass fallback success through the existing cache wrapper, but cached MET
  Norway restore, provider-specific cache metadata, conditional GET metadata,
  and stale fallback provenance are not completion claims for this slice.
- Authority check: if `docs/data-sources/MET_NORWAY_FORECAST.md` is interpreted
  to require provider-specific cache-header persistence before any active
  installed-app MET Norway request, stop and merge the minimum Slice 31B
  cache-header work into this cycle before implementing fallback wiring.
- Add focused app or core tests proving Open-Meteo success skips fallback,
  fallback-ineligible primary failures skip fallback, fallback-eligible primary
  failures call MET Norway, fallback success reaches provider-neutral Home
  ready state, and fallback diagnostics remain provider-neutral on double
  failure.
- Prove the installed MET Norway repository composition uses the production
  MET Norway client configuration with an identifying Oxygen User-Agent, or a
  test-visible factory default that resolves to the same value.
- Add installed-app or connected instrumentation coverage that controls the
  primary forecast path into an eligible failure, serves a MET Norway-mapped
  forecast through the fallback path, launches or drives Home, and observes a
  ready Home state with MET Norway source/provenance text. Controlled inputs are
  acceptable only if the observed Home state is produced through the installed
  app state holder, production fallback repository composition, and production
  MET Norway mapper or repository path; `SampleWeather` and provider-specific UI
  branches are not valid evidence.
- Update README, Data Sources, Privacy, and About disclosure text only after the
  behavior is implemented and verified, stating active installed-app fallback
  wiring truthfully while leaving Slice 31B/32 cache/provenance real-path
  release gates unclaimed.
- Save focused logs and any screenshots/UI-tree evidence under
  `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/`.

Out of scope:
- Saved-location save UI, saved-location removal UI, reorder/grouping,
  automatic multi-location refresh, background refresh, unit preferences,
  official alerts, air quality, radar/maps, widgets, notifications, device
  location expansion, provider preference UI, cache schema changes, Room
  migrations, DataStore format changes, MET Norway provider cache-header
  persistence, conditional GET behavior, cached MET Norway restore claims,
  stale fallback provenance claims, release-candidate status, MVP-readiness
  claims, or broad Home/UI redesign.

## Design

- Prefer direct composition over new provider-selection abstractions:
  `FallbackWeatherRepository(defaultRepository = OpenMeteoWeatherRepository(),
  fallbackRepository = MetNoWeatherRepository())` should become the upstream for
  the existing `CachedWeatherRepository` in the installed app.
- If tests need visibility into installed composition, add one app-local factory
  with injectable repositories and storage rather than changing
  `OxygenAppStateHolder` behavior or provider interfaces.
- Do not add settings, feature flags, provider preference UI, or a generalized
  provider-selection framework for this slice.
- Keep fallback eligibility owned by core fallback selection. Do not duplicate
  eligibility rules in UI or `MainActivity`.
- Preserve the existing Home presentation mapper. MET Norway source text should
  come from `DataProvenance`, not provider-specific UI branches.
- Ensure fallback cannot be repeatedly triggered by recomposition or unrelated
  Home redraw; retry behavior must remain explicit and provider-neutral.
- Treat disclosure strings as product status surfaces. Update them only after
  the verified installed path makes MET Norway an active fallback.

## Workflow

Baseline:
- `git status --short`
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*FallbackWeatherRepositoryTest'`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'`
- `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.OfflineLaunchPersistenceInstrumentedTest`

Build and focused evidence:
- Add or extend tests around the installed repository composition/factory and
  Home state holder with fake repositories so eligible versus ineligible
  fallback behavior is deterministic and does not require live provider
  outages.
- Add focused coverage that the factory/default MET Norway path uses the
  identifying Oxygen User-Agent required by the provider contract, without
  leaking headers into UI or app state.
- Implement the smallest production wiring change needed for the installed app
  to use Open-Meteo default plus MET Norway fallback.
- Run the new and affected app/core unit tests.

Real-path exercise:
- Use connected instrumentation or a controlled debug/test composition to drive
  the installed Home forecast path with an eligible primary failure and a
  MET Norway fallback success produced by the production MET Norway mapper or
  repository path.
- Do not use sample weather, mocked Home presentation success, or
  provider-specific Compose branches as fallback evidence.
- Verify Home reaches success, the displayed source/provenance identifies
  MET Norway, and no saved-location/manual-location behavior regresses.
- Capture at least one installed Home fallback evidence artifact if the
  acceptance test does not already retain a reviewable UI artifact.

Broad checks:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Artifacts target:
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/`

## Phase Results

- specified: Slice 31A is defined by the roadmap as installed-app fallback
  wiring after the committed saved-location slices.
- planned: Bounded to installed forecast repository wiring, deterministic
  fallback behavior coverage, one controlled installed Home fallback exercise,
  identifying User-Agent evidence, explicit cache/provenance deferral, and
  truthful status/disclosure updates after verification.
- covered: Added focused app factory/state coverage in
  `app/src/test/kotlin/com/oxygen/weather/app/InstalledForecastRepositoryFactoryTest.kt`
  for Open-Meteo success skipping fallback, fallback-ineligible primary failure
  skipping fallback, fallback-eligible primary failure reaching Home ready with
  MET Norway provenance, double-failure diagnostics, and the identifying
  MET Norway User-Agent factory default. Existing core
  `FallbackWeatherRepositoryTest` remains green for fallback eligibility.
- implemented: `MainActivity` now receives
  `InstalledForecastRepositoryFactory.create(storage = forecastCacheStorage)`.
  The factory composes `CachedWeatherRepository(upstream =
  FallbackWeatherRepository(defaultRepository = OpenMeteoWeatherRepository(),
  fallbackRepository = MetNoWeatherRepository()))` with the existing Room cache
  storage.
- verified: Baseline checks passed for core fallback, app Home state holder, and
  connected offline persistence. Focused checks passed for installed factory,
  app contract, About disclosure, app Home state holder, core fallback, and
  connected installed fallback Home exercise. Broad checks passed for
  compileDebugKotlin, full app/core unit tests, assembleDebug, and
  `git diff --check`.
- committed: not yet.

Artifacts:
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/baseline-core-fallback.log`
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/baseline-app-home-state.log`
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/baseline-connected-offline-persistence.log`
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/focused-installed-forecast-factory-rerun-2.log`
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/focused-app-contract.log`
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/focused-about-disclosure.log`
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/focused-app-home-state.log`
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/focused-core-fallback.log`
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/connected-installed-fallback-home-rerun.log`
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/broad-compile-debug-kotlin.log`
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/broad-unit-tests.log`
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/broad-assemble-debug.log`
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/broad-git-diff-check.log`

Notes:
- Initial focused and connected attempts failed only because of test-helper
  access/name assumptions and a too-unique MET Norway text assertion; reruns
  passed after test-only fixes.
- Provider-specific MET Norway cache-header persistence, conditional GET
  metadata, cached fallback restore claims, stale fallback provenance, provider
  preference UI, unit preferences, alerts, air quality, radar, release-candidate
  status, and MVP-readiness remain out of scope and unimplemented.
