# Active Cycle

Status: committed
Cycle ID: 2026-09-04-slice-19d-save-search-result-ui
Mode: feature
Slice: Slice 19D, Save Search Result UI
Commit: `8599640`

Goal: Let users save a searched place from the location-entry surface through
the production saved-location path without making save required for one-off
manual selection.

Basis:
- `docs/OXYGEN_FULL_SPECIFICATION.md` section 53 and
  `.codex/plans/mvp-roadmap.md` identify Slice 19D as the next implementation
  candidate.
- Slice 19C committed saved-location list/display/current marking/select UI for
  existing saved rows.
- `SavedLocationStorage.saveLocation(...)` and
  `RoomSavedLocationStorageFactory.create(...)` already exist; the installed UI
  does not yet expose save controls for search results.
- README still correctly lists search-result save UI as not implemented.

## Contract

Selected behavior:
- Search result rows expose a clear save control alongside the existing manual
  use-now/select action.
- Saving a result calls production `SavedLocationStorage.saveLocation(...)` with
  the candidate's provider-neutral `WeatherLocation`.
- Save success refreshes the saved-location list shown on the location-entry
  surface and does not select the location, write selected-location storage, or
  start a forecast by itself.
- Save failure surfaces as
  `SavedLocationsPresentationState.Failure(SavedLocationsMessage.LocalStateUnavailable)`.
- Manual use-now selection still works when saved-location storage is absent,
  unavailable, or a previous save failed.
- When saved-location storage is absent, search-result save controls are not
  offered on the installed UI; if the app-state save handler is called anyway,
  it surfaces `SavedLocationsPresentationState.Failure(SavedLocationsMessage.LocalStateUnavailable)`
  without selecting the location, writing selected-location storage, or starting
  a forecast.
- Saving an already-saved `WeatherLocation` refreshes the saved-location list
  without creating duplicate visible rows, preserving the Slice 19A storage
  duplicate policy.
- Compact and large-font layouts keep search, save, and use-now controls
  readable, reachable, and tagged for tests.

Acceptance boundary:
- Production changes are limited to app state/event handling and the
  location-entry Compose surface unless a focused test exposes a necessary
  smaller supporting change.
- The installed app continues to use `RoomSavedLocationStorageFactory.create(...)`
  from `MainActivity`; no fake or sample save path may satisfy production UI.
- Existing saved-location selection, selected-location persistence, forecast
  loading/cache/fallback behavior, provider disclosures, and About text remain
  behaviorally unchanged except where About/README status is updated after the
  slice is verified.
- Stable test tags include search-result rows and per-row controls, using names
  such as `location-entry-result-save-0` and
  `location-entry-result-use-now-0`.
- Save evidence under
  `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/`.

Out of scope:
- Adding saved-location removal UI.
- Saved-location reordering, folders, favorites, or automatic multi-location
  refresh.
- Unit preferences, device-location permission expansion, official alerts, air
  quality, radar/maps, appearance settings, widgets, background refresh,
  notifications, provider changes, release readiness, or MVP-readiness claims.

## Design

- Add an app-state handler such as `onManualLocationCandidateSaved(candidateId)`
  that finds the candidate in current search results, writes it to
  `SavedLocationStorage`, then reloads the saved-location list.
- Keep `onManualLocationCandidateSelected(...)` as the manual use-now path; it
  must not depend on saved-location storage.
- Extend `FirstRunLocationEntryScreen` result rows with two full-width
  minimum-48dp actions: save and use now. Preserve title, subtitle,
  coordinates, timezone, and disclosure text.
- Prefer focused additions to existing `FirstRunLocationStateHolderTest` for
  search-result save behavior, `HomeForecastStateHolderTest` for saved-location
  regression coverage, and `HomeDashboardUiTest` for Compose coverage; add a
  dedicated location-entry UI test only if it keeps Compose coverage clearer.
- Add or extend connected evidence through an installed-boundary path using
  `RoomSavedLocationStorageFactory.create(...)` to prove search-result save
  persists into Room and appears in the refreshed saved list.

## Workflow

Baseline:
- `git status --short`
- Inspect `OxygenAppStateHolder`, `OxygenApp`, `FirstRunLocationEntryScreen`,
  `FirstRunLocationStateHolderTest`, `HomeForecastStateHolderTest`,
  `HomeDashboardUiTest`, and connected Room saved-location tests.
- Run focused baseline tests for current location-entry and saved-location
  behavior before edits.

Red:
- Add focused state-holder tests for save success list refresh, save failure
  local saved-location failure, no selected-location write/forecast load on
  save, duplicate save list behavior, absent saved-location storage behavior,
  and use-now independence after save failure.
- Add Compose test coverage that each search result row displays separate save
  and use-now controls with stable tags and reachable text at large
  density/font settings.
- Add connected Room evidence that saving a searched result through the app
  state holder uses `RoomSavedLocationStorageFactory.create(...)` and refreshes
  the saved list.

Build:
- Implement the smallest app-state event and UI callback wiring required by the
  failing tests.
- Keep changes out of provider, forecast cache, Room schema, DataStore format,
  and Home forecast presentation unless a test proves a real regression.

Focused green:
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*FirstRunLocationStateHolderTest' --tests '*HomeForecastStateHolderTest'`
- `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.OfflineLaunchPersistenceInstrumentedTest`
- `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`

Real-path exercise:
- `scripts/list-avds.sh`
- `scripts/start-emulator.sh`
- `scripts/install-debug.sh`
- In the installed app, search for a real place, save the result, confirm it
  appears in saved locations, and confirm use-now still opens Home for a
  searched result.
- If live provider/network access blocks the real-place search, record the
  exact blocker and do not report the manual installed-app real-path exercise as
  verified; deterministic connected Room evidence may still support covered and
  implemented status for the production save path.

Broad checks:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Review:
- Update README and any user-visible About/Data Sources/Privacy status text only
  if they contain claims affected by verified Slice 19D behavior. Do not add
  About status prose just to mirror the plan.
- Confirm no generated build outputs, SDK files, Gradle caches, emulator state,
  or local runtime directories are staged.

Artifacts target:
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/`

## Phase Results

- specified: Slice 19D is defined by the specification and roadmap as saving a
  searched place from the location-entry surface without making save required
  for one-off manual selection.
- planned: Bounded to app state/event handling, location-entry Compose UI,
  focused state/UI tests, connected Room saved-location evidence, installed
  real-path exercise, and broad Android checks.
- covered: Focused app state tests cover search-result save success, save
  failure, absent saved-location storage, duplicate save refresh behavior, and
  no selected-location write or forecast load on save. Connected Compose tests
  cover separate search-result Save and Use now controls with stable tags and
  hidden Save controls when saved-location storage is unavailable. Connected
  app-state evidence covers saving a searched result through
  `RoomSavedLocationStorageFactory.create(...)`.
- implemented: `OxygenAppStateHolder.onManualLocationCandidateSaved(...)`
  writes the candidate `WeatherLocation` to `SavedLocationStorage`, refreshes
  saved rows, reports local saved-location failures, and leaves the manual
  Use now path as the only selected-location/forecast handoff. The
  location-entry screen renders per-result Save and Use now controls with
  stable row/control tags.
- verified: Baseline, red, focused, connected, installed-app, and broad checks
  were run. The installed app searched live Open-Meteo geocoding for Chicago,
  showed per-result Save and Use now controls, saved Chicago into the visible
  saved-location list without leaving location entry, then Use now opened Home
  with an Open-Meteo forecast for Chicago.

Evidence artifacts:
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/baseline-focused-unit.log`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/red-focused-unit.log`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/focused-unit.log`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/list-avds.log`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/start-emulator.log`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/connected-offline-persistence-rerun.log`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/connected-home-dashboard-ui.log`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/install-debug.log`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/installed-chicago-results-clean-window.xml`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/installed-after-save-window.xml`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/installed-after-use-now-window.xml`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/installed-after-use-now-home.png`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/compile-debug-kotlin.log`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/debug-unit-tests.log`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/assemble-debug.log`
- `.codex/test-artifacts/2026-09-04-slice-19d-save-search-result-ui/git-diff-check.log`

Command results:
- `git status --short`: showed pre-existing modified `.codex/plans/current.md`
  and `.codex/cycles/history.md`; later implementation files were modified for
  this cycle.
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*FirstRunLocationStateHolderTest' --tests '*HomeForecastStateHolderTest'`:
  baseline passed before production edits.
- Same focused unit command after red tests failed at compile on missing
  `onManualLocationCandidateSaved`.
- Same focused unit command after implementation passed.
- `scripts/list-avds.sh`: found `oxygen_starter`.
- `scripts/start-emulator.sh`: started the emulator after no connected device
  was present.
- `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.OfflineLaunchPersistenceInstrumentedTest`:
  first run failed because the new connected test used persistent DataStore
  state and started on Home; rerun passed after isolating selected-location
  storage while preserving production Room saved-location storage.
- `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`:
  passed.
- `scripts/install-debug.sh`: passed and launched the installed app.
- Installed-app exercise: passed after dismissing an emulator Pixel Launcher
  ANR dialog unrelated to Oxygen.
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`: passed.
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`:
  passed.
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`: passed.
- `git diff --check`: passed.

Changed files:
- Production: `app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt`,
  `app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt`,
  `app/src/main/kotlin/com/oxygen/weather/app/ui/firstrun/FirstRunLocationEntryScreen.kt`.
- Tests: `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastStateHolderTest.kt`,
  `app/src/androidTest/kotlin/com/oxygen/weather/app/OfflineLaunchPersistenceInstrumentedTest.kt`,
  `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`.
- Documentation/status: `README.md`, `.codex/plans/current.md`,
  `.codex/cycles/history.md`.
