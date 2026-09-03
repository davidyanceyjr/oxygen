# Active Cycle

Status: ready
Cycle ID: 2026-09-03-saved-location-storage-model
Mode: feature
Slice: Slice 19A, Saved Location Storage Model
Goal: Persist a provider-neutral saved-location list independently from the
current selected-location state, with deterministic storage behavior and no
Home switching, UI management, provider, forecast, or cache behavior changes.
Roadmap context: Slice 18J is committed and Slice 19A is the next
implementation candidate. Slice 19B selection/concurrency and Slice 19C saved
locations UI remain deferred.

## Contract

Selected Behavior:
- Oxygen can add, list, and remove saved `WeatherLocation` records through a
  production storage path.
- Saved-location identity is the local provider-neutral `LocationId`.
- Saved-location persistence does not expose provider IDs as user-facing
  identity and does not derive identity from display labels.
- Duplicate saves are deterministic: saving a location whose `LocationId`
  already exists replaces that saved row in place instead of creating a second
  row.
- Listing saved locations is deterministic and stable by insertion/update
  order: newest saved or replaced location appears last.
- Removing a saved location deletes only that saved-location row. If the
  removed row is also the current selected location, the existing DataStore
  selected location remains unchanged in Slice 19A, Home can continue using the
  selected location, and selection replacement policy remains deferred to Slice
  19B.
- Forecast cache records remain forecast cache records. Saved-location rows do
  not satisfy Home forecast cache reads and forecast cache rows do not appear in
  saved-location lists.

Acceptance Boundary:
- A provider-neutral saved-location storage interface exists outside Compose and
  provider clients.
- A Room-backed saved-location storage implementation exists and is exercised
  through an Android instrumentation boundary, but app construction wiring is
  deferred until a production app-state consumer exists in Slice 19B or 19C.
- Existing selected-location DataStore persistence remains the selected-location
  source of truth.
- Existing Room forecast-cache behavior remains compatible with existing
  installed databases through an explicit Room migration, not destructive
  reset.
- Focused tests prove add/list/remove, duplicate replacement, stable local
  identity, selected-location independence, and forecast-cache separation.
- Existing Home forecast state tests continue to pass unchanged or with only
  narrowly justified constructor wiring updates.
- Slice 19A implements storage-bound add/list/remove only; user-facing
  save/select/remove behavior remains split across Slice 19B and Slice 19C per
  the roadmap.

Out of Scope:
- Saved-location UI, selection controls, current-selection badges, destructive
  confirmation UI, or large-font visual verification.
- Switching Home to another saved location.
- Cancellation, obsolete refresh isolation, or race handling for overlapping
  location changes.
- Saved-location reordering, folders, groups, favorites, background refresh, or
  cache refresh for all saved locations.
- Unit preferences, device-location permission flow, alerts, air quality, radar,
  maps, widgets, notifications, appearance settings, MET Norway installed-app
  fallback wiring, release readiness, or MVP readiness.

## Design

Production boundaries:
- Add a small saved-location storage contract using existing core
  `WeatherLocation` and `LocationId` types.
- Store saved locations in the existing Room database next to the forecast
  cache, using a separate saved-location table with the same provider-neutral
  location fields already persisted for cached forecast locations:
  `id`, `displayName`, `latitude`, `longitude`, `elevationMeters`, and `zoneId`.
- Keep `DataStoreSelectedLocationStorage` responsible only for the one selected
  location. Do not move selected-location state into Room during Slice 19A.
- Keep forecast cache DAOs scoped to forecast cache tables. Do not reuse cached
  forecast rows as the saved-location list.

Migration and compatibility:
- Bump the Room schema version and add a non-destructive migration that creates
  the saved-location table.
- Preserve all existing forecast-cache tables and read/write behavior.
- Do not enable destructive migration.
- Because the current Room database has no exported schema files, verify the
  migration by creating a representative version-1 database with the current
  forecast-cache tables, inserting forecast-cache data, running the migration,
  then proving forecast-cache readback and saved-location storage still work.

Duplicate and ordering policy:
- Use `LocationId.value` as the Room primary key.
- Add/replace uses Room upsert/replace semantics for the matching local id.
- Maintain a monotonic `sortOrder: Long` field owned by saved-location storage.
  The first save gets the next order, duplicate replacement keeps the same id
  and moves that row to the newest order, and listing orders by `sortOrder ASC`.
  Tests must not depend on wall-clock time for ordering.

Failure behavior:
- Storage failures should surface as storage failures at the storage boundary.
  Do not convert failed saved-location persistence into sample success or Home
  forecast success.
- Existing selected-location local-state error behavior must remain unchanged.

## Planned Workflow

1. Baseline:
   - Run focused existing storage/state checks before production edits:
     `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'`
   - Run focused existing core cache checks before production edits:
     `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*CachedWeatherRepositoryTest'`
   - Record the baseline log under
     `.codex/test-artifacts/2026-09-03-saved-location-storage-model/`.

2. Red/coverage:
   - Add focused tests for the saved-location storage contract.
   - Add Room instrumentation coverage proving production Room readback,
     duplicate replacement, deterministic list order, removal, selected-location
     independence, and separation from forecast cache records.
   - Add a version-1 to version-2 Room migration test that keeps representative
     forecast-cache data readable, creates the saved-location table, and proves
     destructive migration is not required.

3. Build:
   - Implement the saved-location storage interface and Room-backed
     implementation.
   - Add the Room saved-location table and migration.
   - Do not wire saved-location storage into `MainActivity` or app state until
     Slice 19B or 19C adds a production consumer.

4. Focused green:
   - Run the new saved-location focused unit/instrumented tests.
   - Re-run affected existing selected-location, offline persistence, and Room
     forecast-cache tests.

5. Real-path exercise:
   - Exercise the production Room saved-location implementation through an
     Android instrumentation boundary, not a fake-only unit boundary.
   - Confirm selected-location DataStore readback still succeeds after removing
     the same id from saved-location storage.

6. Broad checks:
   - `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
   - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
   - `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
   - `git diff --check`

## Phase Results

- specified: Slice 19A is defined by the roadmap and specification as saved
  location storage only.
- planned: This plan limits work to the saved-location persistence model,
  deterministic behavior, migration, wiring, and tests.
- covered: `RoomSavedLocationStorageInstrumentedTest` covers Room-backed
  add/list/remove, duplicate replacement/order, forecast-cache separation,
  factory construction, and v1-to-v2 migration. `OfflineLaunchPersistenceInstrumentedTest`
  covers selected-location DataStore independence after saved-location removal.
- implemented: Added the provider-neutral core `SavedLocationStorage`
  contract, Room-backed saved-location storage, the `saved_locations` table, and
  explicit non-destructive Room migration `1 -> 2`.
- verified: Baseline, focused, connected, and broad checks passed; logs are
  under the artifact target.
- committed: committed in this changeset.
- artifacts target:
  `.codex/test-artifacts/2026-09-03-saved-location-storage-model/`.
- evidence:
  `.codex/test-artifacts/2026-09-03-saved-location-storage-model/baseline-home-forecast-state-holder.log`;
  `.codex/test-artifacts/2026-09-03-saved-location-storage-model/baseline-cached-weather-repository.log`;
  `.codex/test-artifacts/2026-09-03-saved-location-storage-model/focused-compile-after-saved-location-storage.log`;
  `.codex/test-artifacts/2026-09-03-saved-location-storage-model/focused-core-room-instrumentation.log`;
  `.codex/test-artifacts/2026-09-03-saved-location-storage-model/focused-app-offline-persistence-instrumentation.log`;
  `.codex/test-artifacts/2026-09-03-saved-location-storage-model/focused-home-forecast-state-holder.log`;
  `.codex/test-artifacts/2026-09-03-saved-location-storage-model/focused-cached-weather-repository.log`;
  `.codex/test-artifacts/2026-09-03-saved-location-storage-model/broad-compile-debug-kotlin.log`;
  `.codex/test-artifacts/2026-09-03-saved-location-storage-model/broad-debug-unit-tests.log`;
  `.codex/test-artifacts/2026-09-03-saved-location-storage-model/broad-assemble-debug.log`;
  `.codex/test-artifacts/2026-09-03-saved-location-storage-model/git-diff-check.log`.
- boundary: No saved-location UI, Home switching, selection replacement policy,
  provider, forecast, unit preference, alert, air quality, radar, appearance, or
  release behavior was added.
