# Active Cycle

Status: ready
Cycle ID: 2026-09-03-saved-location-list-select-ui
Mode: feature
Slice: Slice 19C, Saved Location List and Selection UI

Goal: Show existing saved locations on the location-entry surface and let a user
select one through the committed Slice 19B app-state path while preserving the
manual search, selected-location, forecast refresh, Room cache, DataStore, and
provider paths.

Basis:
- Slice 19A saved-location storage is committed at `d97e2ea`.
- Slice 19B saved-location selection and concurrency is committed at `0f649aa`.
- `.codex/plans/mvp-roadmap.md` specifies Slice 19C as the saved-location
  list/selection UI sub-slice after 19B, with save and remove UI split into
  later saved-location slices.
- `docs/OXYGEN_FULL_SPECIFICATION.md` requires multiple saved locations, manual
  location functionality without permission, provider-neutral local
  `LocationId`, visible source/update behavior, and distinct saved-location
  switching that does not conflict with Home paging.

## Contract

Selected behavior:
- Home's existing `Location` action opens the location-entry surface with saved
  locations loaded from production `SavedLocationStorage`.
- The location-entry surface keeps manual place search fully usable without
  Android location permission.
- Saved rows show enough place detail to disambiguate similar names, including
  region/country context and existing coordinate/time-zone detail where
  available.
- The currently selected location is obvious in the saved list and is not
  presented as a different local identity from Home.
- Saved rows expose a visible select control.
- Selecting a saved row reuses the Slice 19B app-state path so cache restore,
  provider refresh, stale metadata, and obsolete-work isolation remain scoped to
  the selected local `LocationId`.
- Saved-location list/load failures surface as local-state UI failures and must
  not become sample success, fake rows, or fabricated forecasts.

Acceptance boundary:
- Wire the existing saved-list state and saved-selection callback through
  `OxygenApp` into the location-entry surface, or a narrow extracted
  saved-location composable owned by that surface.
- Keep `SavedLocationStorage` nullable/defaulted for scaffold and test
  compatibility.
- Add Compose tests proving saved rows render, similar names disambiguate,
  current selection is marked, select controls are visible, saved-list failure is
  visible, and manual search still selects a location when saved storage is
  unavailable or failing.
- Add Compose coverage for an empty saved list proving no fake saved rows render
  and manual search remains usable.
- When the current selected `LocationId` exists in the saved list, mark it
  clearly; when it is not saved, do not fabricate a saved-row identity or
  current badge.
- Add focused app-state or integration coverage only where UI wiring exposes a
  new app-state boundary beyond the committed Slice 19B load/select behavior.
- Capture baseline, compact, and large-font rendered UI evidence for the
  saved-location list/select surface.
- Update README saved-location status only if Slice 19C reaches verified
  installed-app behavior, separating list/select from save/remove.

Out of scope:
- Search-result save UI, saved-location remove UI, remove confirmation, drag
  reorder, folders/groups, automatic multi-location refresh, background refresh,
  current device-location expansion, unit preferences, alerts, air quality,
  radar/maps, widgets, notifications, persisted appearance settings,
  installed-app MET Norway fallback wiring, Room schema changes, DataStore
  format changes, forecast-cache format changes, provider request changes,
  release readiness, MVP-readiness claims, or broad navigation redesign.

## Design

- Treat the existing location-entry screen as the bounded management surface so
  Home paging and saved-location switching remain distinct interactions.
- Keep manual search and one-off selection independent from saved-list
  availability.
- Use local `LocationId` for all saved-row actions. Provider IDs must remain
  provenance only and must not appear as UI identity.
- Prefer existing Material3 components, test tags, bottom actions, touch-target
  sizing, and scroll behavior from `FirstRunLocationEntryScreen` and Home.
- Avoid visual churn outside the saved-location surface.

## Workflow

Baseline:
- `git status --short`
- Capture a baseline installed screenshot of the current location-entry surface
  before visual changes.
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'`
- `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`

Build and focused evidence:
- Add failing Compose tests for saved-list rendering, disambiguation, current
  selection, visible select controls, saved-list failure, compact layout, and
  large-font layout.
- Add focused app-state tests only if new state-holder behavior is required
  beyond existing saved-list load and saved selection.
- Implement the minimal UI/callback wiring.
- Run the new and affected app unit tests and the affected Home/location-entry
  Compose instrumentation tests.

Real-path exercise:
- Use a connected instrumentation boundary backed by production Room
  saved-location storage to seed at least two saved locations, open Home's
  `Location` action, verify rows appear, select one by visible control, and
  verify Home switches through the selected local `LocationId` forecast path.
- After saved selection reaches Home, verify source/update or stale context
  remains visible according to the existing Home forecast state.
- Capture compact and large-font saved-location list/select screenshots.
- Verify manual search selection still works without granting location
  permission.

Broad checks:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Artifacts target:
- `.codex/test-artifacts/2026-09-03-saved-location-list-select-ui/`

Completion note:
- When appending the completed 19C entry, keep the live history to the recent
  summary plus the latest three usable cycle entries, archiving before any
  non-append rewrite.

## Phase Results

- specified: Slice 19C is defined by the roadmap as Saved Location List and
  Selection UI.
- planned: Bounded to saved-location list/select UI on the existing
  location-entry surface, with baseline, compact, and large-font rendered
  evidence.
- covered: `HomeDashboardUiTest` covers saved rows, similar-name
  disambiguation, current-selection marking, visible select controls, empty
  saved-list behavior, saved-list failure, manual search preservation, compact
  layout, and app-level saved selection. `OfflineLaunchPersistenceInstrumentedTest`
  covers a production Room-backed saved-list/select path through local
  `LocationId`.
- implemented: `OxygenApp` now passes selected location, saved-location state,
  and saved-selection callbacks into `FirstRunLocationEntryScreen`. The
  first-run/location-entry screen renders loaded saved rows with disambiguating
  display/coordinate/time-zone detail, current markers, select controls, and
  local failure state. `OxygenAppStateHolder` loads/selects saved locations off
  the Compose main thread so production Room storage works from the installed UI.
- verified: passed focused unit, focused connected, raw installed-app
  seed/launch/capture, broad compile, broad app/core unit tests, assemble, and
  `git diff --check`. Evidence is under
  `.codex/test-artifacts/2026-09-03-saved-location-list-select-ui/`.
- committed: committed in this changeset.
