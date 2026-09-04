# Active Cycle

Status: committed
Cycle ID: 2026-09-04-slice-19e-remove-saved-location-ui
Mode: feature
Slice: Slice 19E, Remove Saved Location UI
Commit: committed in this changeset

Goal: Let users remove saved locations from the location-entry surface through
the production saved-location path without accidental deletion and without
changing the current Home forecast, selected-location DataStore row, or forecast
cache rows.

Basis:
- `docs/OXYGEN_FULL_SPECIFICATION.md` section 53 and
  `.codex/plans/mvp-roadmap.md` identify Slice 19E as the next implementation
  candidate.
- Slice 19D committed search-result save UI at `8599640`; the post-19D
  authority sync is committed at `0fb2ce6`.
- `SavedLocationStorage.removeLocation(...)` and
  `RoomSavedLocationStorageFactory.create(...)` already exist and have core Room
  removal coverage.
- The installed location-entry UI lists/selects saved rows but does not expose
  remove controls or confirmation.
- README correctly lists saved-location removal UI as not implemented. About
  still uses the coarse phrase `saved-location save/remove UI`; the 19E review
  must split or update that wording after removal behavior is verified without
  claiming broader saved-location completion before Gate 19F.

## Contract

Selected behavior:
- Saved-location rows expose a visible, per-row remove control alongside the
  existing select action.
- Activating remove opens an explicit confirmation/cancel step for that saved
  row before any production storage deletion.
- Cancel dismisses confirmation and does not call
  `SavedLocationStorage.removeLocation(...)`.
- Confirmed removal calls production `SavedLocationStorage.removeLocation(...)`
  for the row's provider-neutral `LocationId`, then refreshes only
  `SavedLocationsPresentationState`.
- Remove failure surfaces
  `SavedLocationsPresentationState.Failure(SavedLocationsMessage.LocalStateUnavailable)`.
- Removing the currently selected location does not clear or rewrite
  `DataStoreSelectedLocationStorage`, remove or rewrite `ForecastCacheStorage`,
  start a provider refresh, or replace the visible Home forecast.
- Selecting saved rows, saving searched rows, manual use-now selection,
  offline restore, fallback provenance, and Data Sources/Privacy provider
  claims remain behaviorally unchanged. About saved-location status copy may
  change only after verified removal behavior.
- Compact and large-font layouts keep saved-row select/remove controls and the
  confirmation/cancel step readable, reachable, and tagged for tests.

Acceptance boundary:
- Production changes are limited to `OxygenAppStateHolder`, `OxygenApp`, and
  `FirstRunLocationEntryScreen` unless a focused test exposes a necessary
  smaller supporting change.
- The installed app continues to use `RoomSavedLocationStorageFactory.create(...)`
  from `MainActivity`; no fake, sample, or UI-only delete path may satisfy the
  behavior.
- No Room schema, forecast-cache storage, DataStore format, provider request,
  forecast repository, fallback-selection, or Home forecast presentation change
  is allowed for this slice.
- Stable test tags should identify saved rows, remove controls, confirmation,
  confirm, and cancel actions, for example
  `location-entry-saved-location-remove-0`,
  `location-entry-saved-remove-confirmation-0`,
  `location-entry-saved-remove-confirm-0`, and
  `location-entry-saved-remove-cancel-0`.
- Evidence belongs under
  `.codex/test-artifacts/2026-09-04-slice-19e-remove-saved-location-ui/`.

Out of scope:
- Saved-location drag reorder, folders, favorites, search/filter management, or
  automatic multi-location refresh.
- Automatic replacement when the removed row is currently selected.
- Clearing selected-location state, forecast cache cleanup, provider preference
  UI, unit preferences, device-location permission expansion, official alerts,
  air quality, radar/maps, appearance settings, widgets, background refresh,
  notifications, release readiness, or MVP-readiness claims.

## Design

- Add app-state removal handling such as `onSavedLocationRemoveRequested(...)`,
  `onSavedLocationRemoveCanceled(...)`, and
  `onSavedLocationRemoveConfirmed(...)`, or an equivalent single explicit
  confirmation state that keeps deletion impossible until confirm.
- Store pending confirmation as presentation state, not as hidden UI-only
  mutation. The state should identify one saved `LocationId` and clear on cancel,
  successful removal refresh, or failure.
- Implement confirmed removal on the existing forecast executor because saved
  location storage is synchronous and already used there. Do not call
  `nextForecastRequestId()`, selected-location storage, forecast cache storage,
  or weather repository from the remove path.
- Extend saved-row Compose UI with separate full-width or otherwise minimum
  48dp select/remove actions. Use Material destructive styling only as
  reinforcement; the confirmation text/action labels must carry the meaning.
- Keep confirmation inline with the row or as a simple dialog only if Compose
  tests can exercise cancel/confirm reliably at compact and large-font sizes.
  Prefer the smallest UI addition that remains readable and testable.
- Update README removal status and split/update the stale About
  `saved-location save/remove UI` wording only after verification proves removal
  UI is implemented. Defer broader saved-location status reconciliation to
  Gate 19F.

## Workflow

Baseline:
- `git status --short`
- Inspect `OxygenAppStateHolder`, `OxygenApp`, `FirstRunLocationEntryScreen`,
  `HomeForecastStateHolderTest`, `HomeDashboardUiTest`,
  `OfflineLaunchPersistenceInstrumentedTest`, and Room saved-location storage
  tests.
- Existing direct-storage connected coverage already proves Room saved-location
  removal does not clear DataStore selected-location state; 19E must add
  app-state/UI-path coverage.
- Run focused baseline checks for current saved-location list/select/save
  behavior before production edits.

Red:
- Add state-holder tests proving remove request only enters confirmation,
  cancel does not call storage, confirm calls `removeLocation(...)` and
  refreshes saved rows, failure reports local saved-location failure, unknown
  IDs do not mutate state, and removing the currently selected row leaves
  selected-location writes, forecast cache calls, weather repository calls, and
  visible Home forecast unchanged.
- Add Compose tests proving saved rows expose separate select/remove controls,
  confirmation/cancel/confirm are visible with stable tags, cancel preserves the
  row, confirm invokes the callback, and large-font compact layout keeps
  controls reachable.
- Add connected installed-boundary evidence using
  `RoomSavedLocationStorageFactory.create(...)` that removes a saved row through
  `OxygenAppStateHolder`, refreshes the saved list, and leaves
  `DataStoreSelectedLocationStorage` plus forecast cache data unchanged when the
  removed ID is also the selected ID.

Build:
- Implement the smallest app-state confirmation/removal path and Compose
  callback wiring required by the failing tests.
- Keep existing selection/save handlers intact and avoid shared refactors unless
  needed to remove real duplication introduced by the slice.

Focused green:
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*FirstRunLocationStateHolderTest' --tests '*HomeForecastStateHolderTest'`
- `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.OfflineLaunchPersistenceInstrumentedTest`
- `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`

Real-path exercise:
- `scripts/list-avds.sh`
- `scripts/start-emulator.sh`
- `scripts/install-debug.sh`
- In the installed app, create or reuse two saved locations, open the
  location-entry surface, start removal for one saved row, cancel and confirm
  the row remains, start removal again, confirm, verify only that row leaves the
  saved list, then verify the current Home forecast is still visible/unchanged
  when the removed row was current.
- If live geocoding/provider access blocks creating saved rows, seed production
  Room saved-location storage through existing deterministic test/app-state
  paths and record the blocker. Do not claim live provider verification for any
  step that was not exercised.

Broad checks:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Review:
- Confirm changed production files are limited to the planned app-state and
  location-entry UI surface unless evidence justifies a small support change.
- Confirm README/About removal status is updated only after verified behavior
  exists, and that Data Sources/Privacy/provider claims remain unchanged.
- Confirm no generated build outputs, SDK files, Gradle caches, emulator state,
  or local runtime directories are staged.

Artifacts target:
- `.codex/test-artifacts/2026-09-04-slice-19e-remove-saved-location-ui/`

## Phase Results

- specified: Slice 19E is defined by the specification and roadmap as removing
  saved locations from the location-entry surface with explicit confirmation
  and without changing current Home forecast state.
- planned: Bounded to app-state confirmation/removal handling, saved-row Compose
  UI, focused state/UI tests, connected Room/DataStore/cache evidence, installed
  real-path exercise, broad Android checks, and narrow status documentation
  updates after verification.
- covered: Added app-state unit coverage for remove request, cancel, confirm,
  failure, unknown ID, and current-selected preservation; connected Compose
  coverage for row remove controls, confirmation/cancel/confirm tags, compact
  large-font reachability, and app callback behavior; connected persistence
  coverage through `RoomSavedLocationStorageFactory.create(...)`,
  `DataStoreSelectedLocationStorage`, and Room forecast cache storage.
- implemented: `OxygenAppStateHolder` now tracks a pending saved-location
  removal in `SavedLocationsPresentationState.Loaded`, deletes only on explicit
  confirmation through `SavedLocationStorage.removeLocation(...)`, refreshes
  only saved-location presentation state, and leaves selected-location,
  forecast cache, provider refresh, and visible Home forecast paths untouched.
  `FirstRunLocationEntryScreen` exposes separate select/remove controls and an
  inline confirmation/cancel step with stable test tags.
- verified: Focused state-holder unit tests, connected persistence tests,
  connected Compose UI tests, broad compile/unit/assemble checks, `git diff
  --check`, and installed debug launch passed. Evidence logs and screenshots
  are under
  `.codex/test-artifacts/2026-09-04-slice-19e-remove-saved-location-ui/`.
- committed: Verified Slice 19E work is committed in this changeset.
