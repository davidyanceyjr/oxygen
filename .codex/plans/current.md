# Active Cycle

Status: committed
Cycle ID: 2026-08-26-cache-one-forecast-bundle
Mode: feature
Goal: Implement Slice 16 by adding one production forecast-cache path that normalizes a successfully fetched provider `WeatherBundle`, persists current/hourly/daily forecast data scoped by stable local `LocationId`, and emits repository success from the persisted rows without claiming failed-refresh retention or offline launch behavior.
Roadmap slice: Slice 16: Cache One Forecast Bundle Through Repository.
Branch or work context: local `oxygen` Android scaffold on top of committed Slice 15r1 `06cef7a`.
Specification anchors:
- `AGENTS.md`
- `README.md`
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 1, 4, 5, 6.1, 6.2, 6.3, 10, 12, 12.1, 15, 16, 17, 40, 41, 48, and 50
- `.codex/plans/mvp-roadmap.md` Slice 16, Forecast Provider Scope, MVP Acceptance Boundary, Release Gate, and Cache/Offline/Stale gates
- `docs/data-sources/OPEN_METEO_FORECAST.md`
- `docs/data-sources/MET_NORWAY_FORECAST.md`
- `DATA_SOURCES.md` and `PRIVACY.md`
- Existing `WeatherRepository`, `WeatherRepositoryResult`, `WeatherBundle`, `WeatherLocation`, `DataProvenance`, `OpenMeteoWeatherRepository`, `MetNoWeatherRepository`, and `FallbackWeatherRepository` behavior

Acceptance criteria:
- Before cache implementation, run a bounded Room feasibility gate in `:core`: add only the smallest Room dependencies/configuration needed for an in-memory forecast-cache test and verify it with the repo-local Android environment. If Room cannot compile or run without broad dependency churn, record the exact command/output blocker in Phase Results before using a narrow durable local production storage fallback. An in-memory store may be used only as a test fixture and does not satisfy Slice 16 production behavior.
- A repository refresh success writes one normalized forecast bundle in a single storage transaction and emits `WeatherRepositoryResult.Success` reconstructed from persisted current/hourly/daily forecast rows rather than directly returning the provider object.
- Persisted forecast data is scoped by the stable local `WeatherLocation.id` / `LocationId`; refreshing one location must not overwrite or emit another location's forecast.
- Persisted entities or records preserve provider-neutral location identity/display/coordinates/elevation/timezone, bundle `fetchedAt`, current/hourly/daily timestamps, canonical metric values, weather conditions, all nullable weather fields, and `DataProvenance` fields: provider ID, source name, issued time, fetched time, data type, and license ID.
- Slice 16 forecast metadata stores only fields available from `WeatherBundle` and `DataProvenance`: `LocationId`, bundle `fetchedAt`, provider ID, source name, issued time, provenance fetched time, data type, and license ID. HTTP cache headers, ETag, Expires, Last-Modified, provider DTO metadata, and raw response metadata are out of scope unless first exposed through a provider-neutral production boundary. Do not create all-null HTTP/cache-header records or fabricate cache headers merely to satisfy a future schema shape.
- Open-Meteo success through the production core repository cache path is wrapped by the cache repository and emits persisted Open-Meteo provenance. Controlled MET Norway success through the same cache repository emits persisted MET Norway provenance. Values must not be averaged, merged, or provider-normalized beyond existing domain models.
- Provider DTOs, raw JSON, HTTP headers, provider-specific error bodies, WMO weather codes, MET Norway symbol codes, and provider client result/error classes do not cross into cache consumers, app state, or Composables.
- Failed refresh behavior remains current no-cache error behavior for this slice unless a provider success has already been emitted in the same refresh transaction. Slice 16 must not claim stale-cache retention after failed refresh, offline launch from cache, provider health/backoff, installed-app MET Norway fallback wiring, saved-location persistence, unit preferences, alert cache, air-quality cache, radar cache, background work, migrations beyond the first schema, or release behavior.
- If provider success occurs but the cache transaction or post-write readback fails, the cache wrapper must emit a provider-neutral local failure and must not return the original provider `WeatherBundle` as success.
- Add or use an explicit provider-neutral local cache/storage failure in the repository error model before mapping cache transaction/readback failure. Do not report local cache/storage failure as provider unavailable, provider invalid response, or unexpected provider failure.
- Slice 16 implements production core repository cache capability only. App default wiring remains `OpenMeteoWeatherRepository()` unless this plan is revised before implementation to include Android context/database lifecycle integration and installed-app evidence. Do not wire MET Norway as active installed-app fallback in this slice unless the plan is revised and real app evidence is added.
- If Room is introduced, dependency and schema changes must be limited to the smallest set needed for the forecast cache path. Do not create new Gradle modules unless Room cannot be introduced safely in the existing `:core` boundary.
- Acceptable Room feasibility changes are limited to Room runtime, Room KTX if needed, Room compiler/KSP or annotation processor, and the smallest AndroidX/JVM test dependency needed to execute a real storage test. Any broader Gradle plugin, Android toolchain, module, or dependency-family change must be recorded as a blocker before fallback storage is considered.
- Repository disclosure documents may be updated only to state the newly implemented forecast-cache behavior after it is verified. They must not claim offline usability, stale failed-refresh retention, saved locations, or cache-backed app relaunch until later slices prove those behaviors.

Acceptance boundary: Slice 16 is complete when the Room feasibility gate is resolved with either working bounded Room storage or an explicitly recorded Room blocker, focused repository/storage tests fail before the cache wrapper/storage implementation and pass after it, production repository code persists one provider-served forecast bundle and emits success reconstructed from the stored rows for the selected `LocationId`, real storage evidence proves transaction atomicity without partial current/hourly/daily/location writes, static leak checks show provider implementation details do not cross the cache/repository/UI boundary, the repository-only real-path exercise passes, and broad Android verification passes. Slice 16 does not prove failed-refresh retention, stale UI, offline launch, saved-location persistence, unit settings, alert lookup/cache, air-quality lookup/cache, radar, background work, dependency license generation, release readiness, or installed-app MET Norway fallback.

Boundary decisions:
- Prefer a small cache repository decorator, for example `CachedWeatherRepository`, around an injected `WeatherRepository`, so provider fetch/mapping remains isolated and the cache boundary consumes only `WeatherBundle`.
- Keep cache storage provider-neutral. Names may mention forecast/cache/weather, not Open-Meteo or MET Norway, except in tests that construct provider-like fixture provenance.
- Use one forecast bundle transaction boundary: metadata/location/current/hourly/daily rows are replaced together for the selected `LocationId` only after a provider success exists.
- Keep alerts and air quality out of the first cache write even though `WeatherBundle` has those fields. They are separate provider/storage slices.
- Do not introduce generic persistence frameworks, DAO abstractions, sync policies, migration frameworks, or settings storage beyond what is required to prove one persisted forecast bundle.
- If Room cannot be installed or verified in the current toolchain without broad dependency churn, implement the same repository/cache contract behind a narrow durable local production storage interface and record any Room blocker explicitly. Fallback storage must be file-backed or database-backed production storage. In-memory maps, static singletons, process-local stores, fake stores, and test-only stores do not satisfy Slice 16 production behavior. Fallback storage evidence must prove a new repository/storage instance can read data written by a prior instance using the same backing file or database.
- Keep app wiring unchanged for Slice 16 and report verified behavior as production core repository cache capability, not installed-app cache behavior. A later app-wiring slice must add Android context/database lifecycle integration before claiming installed-app cache behavior.
- Use artifact directory `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/` for all new logs. Do not modify or replace prior Slice 15 evidence.

Focused evidence to produce:
- A Room feasibility log showing either successful bounded `:core` Room setup or the exact Room blocker that justified a fallback.
- A pre-fix red/baseline focused test log after adding cache repository/storage tests and before implementing the production cache path. The log may include initial compile failures while introducing the test seam, but before production implementation is reported, at least one focused test must compile and fail on a behavioral assertion proving repository success is not reconstructed from persisted rows.
- Focused core tests proving provider success calls storage in one transaction and then emits success reconstructed from stored rows.
- Focused storage tests against the real storage implementation proving atomic replacement: an injected write failure cannot leave a partially replaced bundle visible for the selected `LocationId`.
- Focused core tests proving Open-Meteo-like and MET Norway-like provenance fields survive the cache round trip, including provider ID, source name, issued/fetched timestamps, data type, and license ID.
- Focused core tests proving current/hourly/daily nullable fields survive the cache round trip without zero/default fabrication.
- Focused core tests proving rows are scoped by `LocationId` and that refreshing one location does not emit another location's cached forecast.
- Focused core tests proving provider failure without a successful persisted write still emits the existing failure path and does not claim stale cached success.
- Focused core tests proving provider success followed by cache transaction/readback failure emits the explicit provider-neutral local cache/storage failure and does not return the provider bundle directly.
- Static no-provider-detail-leak check: after implementation, run this against `app/src/main/kotlin` and every production cache/storage path actually created under `core/src/main/kotlin`: `rg -n "OpenMeteoForecastResponse|OpenMeteoCurrent|OpenMeteoHourly|OpenMeteoDaily|OpenMeteoForecastClientResult|OpenMeteoForecastClientError|OpenMeteoGeocodingDto|OpenMeteoGeocodingResult|MetNoForecastResponse|MetNoGeometry|MetNoMeta|MetNoTimeStep|MetNoInstant|MetNoPeriod|MetNoForecastClientResult|MetNoForecastClientError|X-ErrorClass|symbolCode|weather_code" app/src/main/kotlin <actual-core-cache-storage-paths>`. Record the exact command in Phase Results before reporting verified.
- Static cache-claim check for user-facing/docs drift: `rg -n "offline|stale|failed refresh|failed-refresh|saved location|saved-location|Room|cache|cached|fallback" README.md DATA_SOURCES.md PRIVACY.md app/src/main/kotlin core/src/main/kotlin`
- Static plan-audit check for scoped cache/offline language: `rg -n "offline|stale|failed refresh|failed-refresh|saved location|saved-location|Room|cache|cached|fallback" .codex/plans/current.md`; allowed matches are the planned Slice 16 scope, explicit exclusions, Room feasibility gate, fallback-provider references, and this static-check definition.
- Save focused test and static-check logs under `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/` and record project-local paths in Phase Results before reporting the slice ready.

Real-path command or procedure:
- Run a live JVM or Android state-holder exercise through the cache-wrapped Open-Meteo repository path for one explicit `WeatherLocation` and record the emitted loading/success sequence plus provenance. This is repository real-path evidence only, not installed-app cache UX evidence.
- No offline-mode, process-restart, failed-refresh-with-cache, MET Norway live fallback, or emulator stale UI evidence is required or accepted for this slice.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
- Save broad verification logs under `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/` and record project-local paths in Phase Results before reporting the slice ready.

Current gate: committed
Current phase: ready
Last result: Slice 16 repository-level forecast cache path is implemented, verified, and ready to commit. Provider success through `CachedWeatherRepository` writes one provider-neutral forecast bundle to durable file-backed storage scoped by `LocationId`, reads it back, and emits repository success from the persisted current/hourly/daily rows. Installed-app cache wiring, failed-refresh stale retention, offline launch, saved-location persistence, unit preferences, alerts, air quality, radar, background work, release behavior, and active installed-app MET Norway fallback remain out of scope and unimplemented.
Blocker: none.

## Implementation Plan

1. Inspect the current provider repository tests and domain model fields to select the smallest cache/storage API that can round-trip one `WeatherBundle`.
2. Run the bounded Room feasibility gate in `:core` and save the log. Use Room if it compiles and can support a real in-memory storage test; otherwise record the exact blocker before using a narrow durable local production fallback storage implementation.
3. Add focused failing tests for `CachedWeatherRepository` and forecast storage: transaction write on success, stored-row emission, real-storage atomicity, cache failure behavior, provenance preservation for Open-Meteo-like and MET Norway-like bundles, null preservation, `LocationId` scoping, and failure-without-stale-success behavior.
4. Save the pre-fix red focused-test log under `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/`.
5. Add the minimal forecast cache storage implementation selected by the Room gate. Keep stored metadata limited to values actually available at the repository/domain boundary.
6. Implement the cache repository wrapper so provider success writes the selected bundle transactionally, reads the stored bundle back, and emits the reconstructed `WeatherRepositoryResult.Success`; cache write/read failure emits a provider-neutral failure.
7. Keep app wiring unchanged and verify/report the cache wrapper at the repository boundary only.
8. Run focused tests and static no-leak/cache-claim checks, saving logs in the Slice 16 artifact directory.
9. Run the repository-only real-path exercise and save evidence.
10. Run broad Android verification commands and `git diff --check`, saving logs in the Slice 16 artifact directory.
11. Review `README.md`, `DATA_SOURCES.md`, `PRIVACY.md`, and in-app disclosure for cache/offline/status drift; make only minimal wording updates required by verified Slice 16 behavior.
12. Append `.codex/cycles/history.md` only when Slice 16 is actually verified or committed.

## Known Starting Conditions

- The current app production forecast wiring constructs `OpenMeteoWeatherRepository()` by default and does not persist forecasts.
- `:core` has provider-neutral domain models and repository interfaces, but no forecast cache/storage implementation.
- `gradle/libs.versions.toml` currently has no Room, KSP, coroutine test, or AndroidX test dependencies.
- Open-Meteo and MET Norway provider repository paths already return provider-neutral `WeatherBundle` values with provenance.
- `FallbackWeatherRepository` exists in core but is not wired as the active installed-app forecast fallback.
- `README.md`, `DATA_SOURCES.md`, and `PRIVACY.md` currently say offline forecast cache behavior is not implemented. That remains true until Slice 16 is verified, and even after Slice 16 they must not claim offline launch or stale failed-refresh UI.
- `.codex/review/findings.md` currently contains Slice 16 pre-implementation review findings and is in scope only for review status, not implementation behavior.

## Phase Results

- planned: Selected Slice 16 as the next roadmap slice and bounded it to one successful provider forecast bundle persisted and re-emitted through the repository path. The plan explicitly excludes failed-refresh retention, offline launch, saved-location persistence, unit preferences, alert/air-quality/radar caches, background work, release behavior, and active installed-app MET Norway fallback.
- verified: Room feasibility gate log saved at `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/room-feasibility.log`. The command `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*Room*'` found no bounded Room feasibility test in the existing `:core` local JVM setup, so Slice 16 used the planned durable file-backed fallback storage without adding Room/KSP dependencies or broader AndroidX test infrastructure.
- covered: Pre-fix red focused cache repository log saved at `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/pre-fix-red-cache-repository-tests.log`; after the test seam compiled, focused tests failed on behavioral assertions proving the incomplete wrapper returned the provider bundle directly, did not read back by `LocationId`, and did not map cache write/readback failure to `ForecastError.LocalCacheFailure`.
- implemented: Added `CachedWeatherRepository`, `ForecastCacheStorage`, and `FileForecastCacheStorage` under `core/src/main/kotlin/com/oxygen/weather/core/provider/cache/`. Added provider-neutral `ForecastError.LocalCacheFailure`, kept app default forecast wiring unchanged, and updated app error presentation only for exhaustive handling if a cache-wrapped repository is injected later.
- verified: Focused tests passed with `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*CachedWeatherRepositoryTest*' --tests '*FileForecastCacheStorageTest*'`; logs saved at `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/focused-cache-tests.log` and `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/focused-cache-tests-final.log`. Tests cover stored-row emission, Open-Meteo-like and MET Norway-like provenance preservation, nullable current/hourly/daily field preservation, `LocationId` scoping, provider failure preserving the no-cache failure path, local cache failure mapping, durable readback across storage instances, and failed replacement atomicity without exposing partial rows.
- verified: Repository-only real-path exercise passed through `CachedWeatherRepository(OpenMeteoWeatherRepository(), FileForecastCacheStorage(...))` for `manual-madison-real-path`; logs saved at `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/real-path-cache-open-meteo.log` and `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/real-path-cache-open-meteo-junit.xml`. The JUnit XML records `Loading` then `Success` with `provider=open-meteo`, `source=Open-Meteo`, and Open-Meteo provenance.
- verified: Static no-provider-detail-leak check returned no matches; final log saved at `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/static-no-provider-detail-leak-final.log`. Static cache-claim and plan-audit checks were reviewed for scoped cache/offline language; logs saved at `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/static-cache-claim-check-final.log` and `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/static-plan-audit-check.log`.
- verified: Disclosure wording in `README.md`, `DATA_SOURCES.md`, `PRIVACY.md`, and in-app About content now distinguishes verified core repository cache persistence from unimplemented installed-app cache wiring, failed-refresh stale retention, offline forecast cache behavior, and saved-location persistence.
- verified: Broad checks passed: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`, `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`, `. scripts/android-env.sh && ./gradlew :app:assembleDebug`, and `git diff --check`. Logs saved at `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/broad-compile-debug-kotlin.log`, `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/broad-unit-tests.log`, `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/broad-assemble-debug.log`, and `.codex/test-artifacts/2026-08-26-cache-one-forecast-bundle/git-diff-check.log`.
- committed: Slice 16 is being committed as one focused changeset after diff review.
