# Active Cycle

Status: ready
Cycle ID: 2026-09-03-saved-location-selection-state
Mode: feature
Slice: Slice 19B, Saved Location Selection and Concurrency

Goal: Wire production saved-location storage into app state so selecting a
saved location drives the existing Home forecast lifecycle. Preserve
`SelectedLocationStorage`/DataStore as the selected-location source of truth.
Do not add saved-location UI management.

Basis:
- Slice 19A saved-location storage is committed at `d97e2ea`.
- `docs/OXYGEN_FULL_SPECIFICATION.md` Phase 2 requires saved locations,
  Room/DataStore persistence, cached forecasts, and offline source-of-truth
  behavior.
- Spec section 53 and `.codex/plans/mvp-roadmap.md` Slice 19B require saved
  switching to use local `LocationId`, preserve provider-neutral refresh, reject
  wrong-location cache, and isolate obsolete async work.

## Contract

Selected behavior:
- Load saved locations on demand through production `SavedLocationStorage`.
- Select an existing saved `WeatherLocation` by provider-neutral `LocationId`.
- Write the selected saved location to `SelectedLocationStorage` before Home
  starts cache restore or provider refresh for that location.
- Reuse the existing Home selected-location lifecycle: loading, matching-cache
  restore, refresh, stale-after-refresh-failure, retry, and obsolete refresh
  isolation.
- Allow matching cache for the newly selected saved location to appear before
  refresh completes.
- Never let wrong-location cache or stale emissions satisfy Home after saved
  selection.
- Unknown saved ids are no-ops: no selected-location write, provider request, or
  Home forecast change.
- Saved-storage failures and selected-location write failures surface as
  local-state failures; they must not become sample success, fake rows, or
  fabricated forecasts.
- Refresh remains explicit and provider-neutral.

Acceptance boundary:
- Add `SavedLocationStorage` to `OxygenAppStateHolder` or a narrow app-state
  collaborator with backward-compatible defaults.
- Tests cover saved-list load, select by `LocationId`, selected-location
  persistence, matching-cache restore, provider refresh start, unknown-id no-op,
  saved-storage read failure, selected-location write failure, wrong-cache
  rejection, and older-completes-after-newer-selection isolation.
- Wire Room saved-location storage in `MainActivity` only if this slice adds a
  production app-state consumer.
- Home renders forecasts only for `presentationState.selectedLocation`; saved
  rows and forecast-cache rows remain separate.

Out of scope:
- Saved-location picker/list/edit/delete/reorder UI, current-selection badge,
  save button, visual work, screenshots, or large-font visual verification.
- Automatic replacement when the removed saved row is selected.
- Unit preferences, device-location expansion, alerts, air quality, radar,
  maps, widgets, notifications, appearance settings, installed-app MET Norway
  fallback wiring, release readiness, or MVP-readiness claims.

## Design

- Keep `SelectedLocationStorage` as the persisted current-location source of
  truth; saved-location storage is only the saved-list source.
- Do not change Room schema, saved-location order/duplicate policy, DataStore
  format, forecast-cache format, provider request construction, or Home
  presentation mapping.
- Add saved-location presentation state only when current production app-state
  behavior consumes it and tests observe it. Do not add future-UI-only state.
- On explicit saved-list load, read `SavedLocationStorage` and publish saved
  locations or a local-state failure.
- On saved selection, read `SavedLocationStorage`, match exact `LocationId`,
  write `SelectedLocationStorage`, then start the existing Home lifecycle for
  that `WeatherLocation`.
- Invalidate or isolate active forecast/cache work before or during a location
  switch so late work for the previous location cannot update visible Home.
- Same-location refresh may retain visible cache during refresh; different
  saved-location selection must not inherit prior cached or stale presentation.

## Workflow

Baseline:
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'`
- `. scripts/android-env.sh && ./gradlew :core:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.core.provider.cache.room.RoomSavedLocationStorageInstrumentedTest`

Build and focused evidence:
- Add failing app-state tests for the acceptance boundary before production
  code.
- Implement the minimal app-state dependency, saved-list load, saved selection,
  selected-location persistence handoff, and obsolete-work isolation.
- Run new saved-location app-state tests, affected Home forecast tests, affected
  selected-location persistence tests, and Room saved-location instrumentation
  if production wiring uses the Room factory.

Real-path exercise:
- Exercise saved-location selection through production app-state with real
  `SavedLocationStorage` semantics.
- Exercise older-location-completes-after-newer-selection and record the final
  selected Home location.
- If `MainActivity` wiring changes, run a connected construction or launch
  boundary proving Room forecast cache and Room saved-location storage both
  initialize.

Broad checks:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Artifacts target:
- `.codex/test-artifacts/2026-09-03-saved-location-selection-state/`

## Phase Results

- specified: Slice 19B is defined by the roadmap and spec saved-location
  lifecycle requirement.
- planned: Bounded to app-state saved-list load, saved selection, persistence
  handoff, cache/refresh lifecycle reuse, and concurrency isolation.
- covered: `HomeForecastStateHolderTest`, `OxygenAppContractTest`, and
  `OfflineLaunchPersistenceInstrumentedTest` cover saved-list load, saved
  selection by `LocationId`, DataStore handoff ordering, matching Room/cache
  restore, provider refresh start, local-state failures, unknown-id no-op,
  wrong-cache rejection, and obsolete forecast isolation.
- implemented: `OxygenAppStateHolder` consumes `SavedLocationStorage` with
  backward-compatible defaults; `MainActivity` wires
  `RoomSavedLocationStorageFactory`; no saved-location management UI was added.
- verified: passed focused, connected, installed launch, and broad checks with
  logs under
  `.codex/test-artifacts/2026-09-03-saved-location-selection-state/`.
- committed: committed in this changeset.
