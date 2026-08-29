# Active Cycle

Status: committed
Cycle ID: 2026-08-28-persistence-architecture-gate
Mode: feature
Goal: Establish the first production Room-backed forecast persistence boundary before offline launch depends on local forecast data.
Roadmap gate: Persistence Architecture Gate.
Branch or work context: local `oxygen` Android scaffold after committed Slice 17C Home Presentation Accessibility Evidence Baseline.

Specification anchors:
- `AGENTS.md`
- `README.md`
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `.codex/plans/mvp-roadmap.md`
- `.codex/cycles/history.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `core/build.gradle.kts`
- `scripts/android-env.sh`
- `app/src/main/`
- `app/src/test/`
- `app/src/androidTest/`
- `core/src/main/`
- `core/src/test/`

Prerequisites:
- Repository Engineering Gate is committed.
- Slice 17B Explicit Home Refresh Control is committed.
- Slice 17C Home Presentation Accessibility Evidence Baseline is committed.
- Repository-level file-backed forecast cache and failed-refresh stale retention exist but are not wired as installed-app offline launch behavior.

Selected behavior:
- Oxygen has a first production Room-backed persistence boundary for one selected location's Home forecast data.
- Room is introduced as the production forecast persistence boundary for normalized forecast/location data, unless the specification is explicitly amended before implementation to approve a different architecture. This gate prepares the later Room source-of-truth path but does not claim source-of-truth behavior until Slice 18 wires lifecycle-aware Room/Home startup behavior.
- Production code exposes a Room-backed `ForecastCacheStorage` construction path using an Android `Context`; focused tests may use an in-memory database, but the Room boundary must not be test-only.
- Provider success data can be written transactionally to Room, read back through a Room-backed `ForecastCacheStorage` implementation, and emitted through `CachedWeatherRepository` as provider-neutral forecast data.
- The role, migration path, or removal plan for `FileForecastCacheStorage` is explicit before Slice 18 offline launch depends on production persistence.

Acceptance criteria:
- Room dependency/setup exists in the appropriate Android module and includes a first production database boundary for normalized forecast/location persistence.
- A production factory or equivalent production integration point creates the Room database/store and returns `ForecastCacheStorage` without requiring test-only code paths; this does not wire installed-app offline launch or Home startup yet.
- The module placement decision is explicit and preserves the current provider-neutral `:core` boundary; Android Room types do not leak into provider DTOs, provider clients, or Home presentation mappers.
- Forecast persistence preserves selected local `LocationId`, display name, coordinates, timezone, optional elevation where available, current conditions, hourly forecast rows, daily forecast rows, provider provenance, fetched/issued/update timestamps, canonical units, and null/missing values.
- Country, administrative areas, postcode, feature code, population, and selected-location app state are not required in this gate because they are not part of the current selected `WeatherLocation` forecast boundary; saved-location metadata and last-selected-location persistence remain deferred.
- Provider cache headers and provider-specific cache metadata are not required in this gate unless a provider-neutral cache metadata model is added and consumed by repository behavior in this same cycle. Without that production behavior, this gate preserves existing freshness/provenance fields only: `ForecastFreshness`, `WeatherBundle.fetchedAt`, `DataProvenance.issuedAt`, `DataProvenance.fetchedAt`, provider ID, source name, data type, and license ID.
- Persistence writes provider success data with transaction replacement semantics so a partial replacement cannot become visible as a valid forecast.
- Same-location scoping is enforced; persisted forecast data for one selected location must not satisfy another selected location.
- Provider-neutral boundaries remain intact: Open-Meteo DTOs, MET Norway DTOs, provider IDs as user-facing location identity, and provider-specific errors do not cross into app UI, Room consumers, or presentation mappers.
- Local persistence failures map to provider-neutral app/domain failure states and do not fabricate success.
- `FileForecastCacheStorage` has an explicit documented disposition: retained only as a temporary repository test/support path, migrated behind the new Room-backed store, or removed.
- Focused tests cover Room forecast read/write through the `ForecastCacheStorage` boundary, `CachedWeatherRepository` success emission from Room readback, transaction replacement, same-location scoping, missing/null preservation, provenance/freshness preservation, provider-neutral readback, and local failure mapping.
- Focused Room tests run as real `:core:connectedDebugAndroidTest` instrumentation tests against an in-memory Room database on the repo-local emulator unless implementation discovers a lower-risk explicit JVM Android runtime strategy and records that strategy before coding. A passing `connectedDebugAndroidTest` task with `NO-SOURCE` test compilation is only runner plumbing evidence, not coverage.
- `WeatherBundle.alerts` and `WeatherBundle.airQuality` handling is explicit: this forecast gate either preserves empty/default values only and rejects or ignores non-empty alert/air-quality payloads with tests documenting the behavior, or it preserves those fields intentionally without claiming official alert or air-quality lookup/cache behavior.
- Broad Android verification passes.

Acceptance boundary:
- The Persistence Architecture Gate is complete when a production Room-backed forecast persistence boundary exists, production code can construct a Room-backed `ForecastCacheStorage` through an Android `Context`, provider success data writes transactionally to Room, reads back through a Room-backed `ForecastCacheStorage`, and is emitted by `CachedWeatherRepository` as provider-neutral forecast data; same-location scoping and null/provenance/freshness preservation are verified by real focused tests; unsupported selected-location metadata and provider-specific cache metadata are either explicitly deferred or backed by new consumed domain behavior; `WeatherBundle.alerts` and `WeatherBundle.airQuality` handling is explicit; the `FileForecastCacheStorage` disposition is explicit; provider-neutral isolation is verified; and broad Android checks pass. This gate proves repository storage/readback behavior and production Room store construction, not installed-app Room Flow, source-of-truth, or UI startup behavior. This gate does not claim DataStore app-state persistence, lifecycle/ViewModel conversion, offline launch, installed-app stale restoration after process death, saved-location switching, unit conversion, alert lookup, air-quality lookup/cache behavior, installed-app MET Norway fallback, provider-specific fallback cache behavior, background refresh, persisted presentation settings, release readiness, or MVP readiness.

Evidence plan:
- Save focused persistence test logs under `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/`.
- Save any dependency/module-placement decision notes, schema inspection output, static boundary checks, and broad verification logs under the same artifact directory.
- Append completed cycle evidence to `.codex/cycles/history.md` only when the gate is ready or committed.

## Implementation Plan

### Planning Decisions

- Room placement: add Room to `:core` for this gate, under a persistence/database package, because `:core` is already an Android library and currently owns provider-neutral domain models, provider repository interfaces, fallback selection, and the file-backed forecast cache wrapper. Do not create new Gradle modules for this gate.
- Boundary shape: implement a Room-backed `ForecastCacheStorage` in `:core` that accepts and returns current provider-neutral `WeatherBundle` data keyed by selected local `LocationId`. Keep Room entities, DAOs, database classes, converters, and transaction helpers internal to the persistence package.
- Production construction path: add a production factory or equivalent integration point in `:core` that accepts an Android `Context`, creates the Room database/store, and returns `ForecastCacheStorage`. Do not wire this factory into `OxygenApp` or Home startup in this gate.
- Production path for this gate: provider success data is normalized into Room in one transaction, read back through `ForecastCacheStorage.readBundle`, and emitted by `CachedWeatherRepository` as provider-neutral forecast data. This proves only the Room-backed repository persistence boundary without wiring Room Flow, installed-app startup, source-of-truth collection, or offline launch.
- Metadata boundary: preserve the current selected `WeatherLocation` and forecast/provenance/freshness fields that exist in production domain models. Defer country/admin/postcode/feature/population persistence to saved-location work, and defer provider-specific cache headers and generic provider cache metadata unless a provider-neutral cache metadata type is added and consumed by repository behavior in this cycle.
- `FileForecastCacheStorage` disposition: retain it temporarily as repository test/support infrastructure for the already-verified cache slices, but do not extend it as the production forecast persistence path. Future offline launch work must move installed-app cache behavior to the Room-backed store.

### Phase Plan

0. Room test runtime feasibility gate
   - Run `scripts/list-avds.sh` and `. scripts/android-env.sh && ./gradlew :core:connectedDebugAndroidTest` before Room dependency/schema work.
   - Save the exact logs under `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/`.
   - If `:core:connectedDebugAndroidTest` is absent, empty, or currently passes with `NO-SOURCE` test compilation because `core/src/androidTest` has no tests/runtime setup yet, the first implementation step is to add the minimum core Android test dependencies and a minimal instrumented runtime smoke test before adding the Room store.
   - Record the smoke test log separately. Do not use a no-source instrumentation pass as evidence for `covered`, `implemented`, or `verified` Room persistence behavior.
   - If no emulator/device can run locally, stop before Room production implementation and either select an explicit lower-risk JVM Room test strategy before coding or record the emulator blocker. Do not report `implemented` or `verified` Room persistence while this gate is unresolved.
   - If Gradle, KSP, or Android test dependency setup fails, report that setup failure as the blocker instead of claiming persistence behavior.

1. Discovery and baseline
   - Inspect `WeatherModels.kt`, `WeatherProviders.kt`, `CachedWeatherRepository.kt`, `FileForecastCacheStorage.kt`, provider repository tests, and Home state tests.
   - Run baseline focused checks before dependency/schema changes:
     - `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*CachedWeatherRepositoryTest' --tests '*FileForecastCacheStorageTest'`
     - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'`
   - Save logs under `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/`.

2. Dependency and build setup
   - Add Room runtime, Room Kotlin extensions if needed by the selected API, Room compiler/KSP setup, and AndroidX test dependencies only where required.
   - Add and configure the selected Room test runtime explicitly. Default strategy: `core/src/androidTest/` instrumentation tests using an in-memory Room database, run by `. scripts/android-env.sh && ./gradlew :core:connectedDebugAndroidTest`.
   - Configure Room schema export or record an explicit reason it is deferred for this first database gate; if schemas are exported, keep schema artifacts reviewable without introducing generated build outputs into source control accidentally.
   - Keep Room dependencies scoped to `:core`; do not add Room references to `:app` production code in this gate.
   - Run a compile check after dependency setup before schema work.

3. Schema and database boundary
   - Add normalized Room tables for:
     - forecast location row keyed by local `LocationId`, with display fields needed for one Home forecast;
     - forecast metadata/provenance;
     - current conditions;
     - hourly rows;
     - daily rows.
   - Preserve location ID, display name, coordinates, timezone, optional elevation where available, provider provenance, source/license fields, fetched/issued/update timestamps, data type, canonical units, and nullable weather values.
   - Do not add schema columns solely for geocoding candidate metadata or provider cache headers unless corresponding provider-neutral production domain behavior is added in this cycle.
   - Store timestamps and zone identifiers deterministically with converters or scalar columns; avoid lossy formatted presentation strings.

4. DAO transaction behavior
   - Implement replace-for-location semantics with a single Room transaction.
   - Delete/replace rows for the target location only.
   - Ensure failed writes cannot leave a partially visible valid forecast.
   - Ensure reads are scoped by local `LocationId`; a cache for one selected location cannot satisfy another selected location.

5. Provider-neutral adapter
   - Add a Room-backed `ForecastCacheStorage` implementation that maps domain forecast bundles to Room entities and maps Room rows back to domain forecast bundles.
   - Add the production Android `Context` construction path for the Room-backed store; focused tests may instantiate an in-memory database directly, but production code must have a non-test factory/integration point.
   - Exercise the Room store through `CachedWeatherRepository`, not only through DAO or adapter tests.
   - Map local persistence failures to `ForecastError.LocalCacheFailure` or the existing provider-neutral failure path; never fabricate `Success`.
   - Define and test how non-empty `WeatherBundle.alerts` and `WeatherBundle.airQuality` are handled by this forecast-only persistence boundary.
   - Keep Open-Meteo DTOs, MET Norway DTOs, provider-specific client errors, and app presentation types out of the Room package boundary.

6. Focused tests
   - Add an intentional failing focused test or failing instrumentation test for Room-backed forecast readback before implementing the Room store, and save the red log when practical.
   - Add Room-backed tests for:
     - provider success write followed by provider-neutral readback through `ForecastCacheStorage`;
     - `CachedWeatherRepository` emitting success from Room readback after an upstream provider success;
     - transaction replacement and failed replacement atomicity;
     - same-location scoping and wrong-location miss;
     - null/missing value preservation;
     - current/hourly/daily row ordering preservation;
     - provenance, fetched/issued/update timestamp, source/license, and existing freshness/provenance preservation;
     - explicit alert/air-quality preservation, rejection, or omission behavior for this forecast-only boundary;
     - local Room/adapter failure mapping without fabricated success.
   - Run `. scripts/android-env.sh && ./gradlew :core:connectedDebugAndroidTest` for Room instrumentation coverage unless an explicit JVM Android runtime strategy was selected and recorded.
   - Re-run existing cache and Home focused tests to prove no regression in stale-refresh behavior.

7. Static boundary checks
   - Check that Room classes do not appear in `app/src/main`, Home presentation mappers, provider DTO packages, or provider clients.
   - Check that provider DTO/client error types do not appear in Room-facing consumers or app UI.
   - Check that `SampleWeather` remains scaffold/preview-only and is not used by the production persistence path.

8. Deferred work confirmation
   - Confirm these items remain deferred and covered by roadmap slices:
     - Slice 18: selected-location small-state persistence and lifecycle-aware Home startup/refresh boundary.
     - Slice 19: lifecycle-safe saved-location switching and obsolete refresh isolation.
     - Slice 20: unit preference persistence.
     - Slices 26-29: persisted presentation settings.
     - Slice 31: provider-specific Open-Meteo/MET Norway fallback cache metadata and provenance behavior.

9. Broad verification
   - `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
   - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
   - `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
   - `git diff --check`

10. Review and ready state
    - Review the diff for scope and provider-neutral boundary preservation.
    - Update Phase Results with commands actually run and artifact paths.
    - Append completed cycle evidence to `.codex/cycles/history.md` only when the gate is ready or committed.

## Phase Results

- planned: Selected Persistence Architecture Gate after committed Repository Engineering Gate, Slice 17B, and Slice 17C.
- covered: Added real `:core:connectedDebugAndroidTest` instrumentation coverage in `core/src/androidTest/kotlin/com/oxygen/weather/core/provider/cache/room/RoomForecastCacheStorageInstrumentedTest.kt`. The tests cover production factory construction from Android `Context`, Room-backed `ForecastCacheStorage` write/readback, `CachedWeatherRepository` success emission from Room readback, transaction rollback for existing and empty locations, same-location scoping and wrong-location miss, null/missing preservation, current/hourly/daily row ordering, location/provenance/fetched/issued timestamp preservation, explicit rejection of non-empty alert and air-quality payloads for this forecast-only boundary, and local Room transaction failure mapping to `ForecastError.LocalCacheFailure`.
- implemented: Added Room runtime/compiler/KSP setup in `gradle/libs.versions.toml`, root `build.gradle.kts`, and `core/build.gradle.kts`. Added `RoomForecastCacheStorage`, `RoomForecastCacheStorageFactory`, `OxygenForecastCacheDatabase`, Room DAO, and normalized location/metadata/current/hourly/daily entities under `core/src/main/kotlin/com/oxygen/weather/core/provider/cache/room/`. The production factory returns the existing provider-neutral `ForecastCacheStorage` interface from an Android `Context`; installed-app Home startup and offline launch wiring remain deferred.
- implemented: `FileForecastCacheStorage` disposition remains temporary repository test/support infrastructure for already verified cache slices. Future installed-app offline launch work must use the Room-backed store as the production forecast persistence boundary rather than extending the file-backed path.
- implemented: Room schema export is deferred for this first gate with `exportSchema = false` to avoid committing generated schema/build output before migration policy is needed. The first database version is covered by instrumentation tests; future migration work must enable reviewable schema history before versioned migrations are claimed.
- verified: Phase 0 runner plumbing check found `:core:connectedDebugAndroidTest` passing with `compileDebugAndroidTestKotlin NO-SOURCE`; log saved at `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/phase0-connected-no-source.log`. This was not used as persistence coverage.
- verified: Baseline focused checks passed before Room behavior changes: `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*CachedWeatherRepositoryTest' --tests '*FileForecastCacheStorageTest'` and `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'`; logs saved at `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/baseline-core-cache-tests.log` and `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/baseline-home-state-tests.log`.
- verified: First Room compile attempt failed on a KSP generated-code visibility issue for a private DAO record type; log saved at `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/post-room-core-compile.log`. The corrected compile passed; log saved at `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/post-room-core-compile-r2.log`.
- verified: Focused Room instrumentation first failed with no connected device, then failed once for an incorrect failure-path test expectation, then passed 10 tests on `oxygen_starter(AVD) - 17`: `. scripts/android-env.sh && ./gradlew :core:connectedDebugAndroidTest`; logs saved at `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/focused-room-connected-tests.log`, `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/focused-room-connected-tests-r2.log`, and `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/focused-room-connected-tests-r3.log`.
- verified: Existing cache and Home focused regression checks passed after Room implementation: `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*CachedWeatherRepositoryTest' --tests '*FileForecastCacheStorageTest'` and `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'`; logs saved at `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/focused-existing-core-cache-tests.log` and `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/focused-existing-home-state-tests.log`.
- verified: Static boundary checks passed: no Room classes in `app/src/main` or provider client packages; no provider DTO/client package leakage into the Room cache package; no `SampleWeather` in the production cache path. Logs saved at `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/static-room-boundary-check.log`, `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/static-provider-package-boundary-check.log`, and `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/static-sample-production-check.log`. A broader provider-name search found expected active app wiring to Open-Meteo repositories; log saved at `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/static-provider-detail-leak-check.log`.
- verified: Broad checks passed: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`, `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`, `. scripts/android-env.sh && ./gradlew :app:assembleDebug`, and `git diff --check`; logs saved under `.codex/test-artifacts/2026-08-28-persistence-architecture-gate/`.
- verified: Pre-commit diff review found no blocking findings. Reviewed scope, provider-neutral boundary isolation, Room/app/provider leakage checks, Room read/write mapping, transaction tests, repository failure mapping, build-file drift, generated-output risk, and saved verification logs. Non-blocking residual risks recorded for future slices: alert/air-quality payloads are intentionally rejected by this forecast-only boundary, and Slice 18 should own the app-lifetime Room construction path.
- verified: This gate does not claim DataStore app-state persistence, lifecycle/ViewModel conversion, offline launch, installed-app Room/Home startup wiring, installed-app stale restoration after process death, saved-location switching, unit conversion, official alert lookup/cache behavior, air-quality lookup/cache behavior, installed-app MET Norway fallback, provider-specific fallback cache metadata behavior, background refresh, persisted presentation settings, release readiness, or MVP readiness.
