# Active Cycle

Status: ready
Cycle ID: 2026-09-05-slice-20c-persisted-unit-selection
Mode: feature
Slice: Slice 20C, Persisted Alternate-Unit Reachability
Commit: not committed

## Objective

Make a persisted unit preference selectable from the installed app and apply it
through the existing Home presentation mapper, without changing forecast
requests, canonical weather/cache data, provenance, location behavior, or the
compatibility output for installs with no saved preference.

## Scope

- Add an app-local `UnitPreferenceStorage` backed by a dedicated Preferences
  DataStore, plus a no-op test/default implementation.
- Persist and decode all existing provider-neutral `UnitPreference` variants:
  preset and custom. Missing, malformed, incomplete, or unknown persisted data
  resolves to no stored selection and therefore the existing compatibility
  default; it is never rewritten during a read.
- Wire the storage in `MainActivity` and inject it into `OxygenAppStateHolder`.
- Add one Units surface reachable from the existing Settings / About overview.
  This slice exposes the compatibility default plus Metric, US, and UK presets;
  it does not add a five-field custom-unit editor.
- Store the active canonical `WeatherBundle` in the state holder only while it
  is the selected location's visible Home forecast. After a successful unit
  write, remap that bundle through `toHomeSuccessPresentation(...)` immediately,
  without a network refresh or cache write.
- Use the loaded selection for initial live results, restored-cache results,
  refreshed results, and retained stale-cache presentation.
- Add focused storage, state-holder, and Compose installed-path coverage.

Expected production files:

- `app/src/main/kotlin/com/oxygen/weather/app/UnitPreferenceStorage.kt`;
- `app/src/main/kotlin/com/oxygen/weather/MainActivity.kt`;
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt`;
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt`;
- `app/src/main/kotlin/com/oxygen/weather/app/ui/units/UnitPreferencesScreen.kt`;
- only the existing Settings / About composable/state files required to expose
  and return from that surface.

Expected tests:

- `app/src/test/kotlin/com/oxygen/weather/app/UnitPreferenceStorageTest.kt`;
- `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastStateHolderTest.kt`;
- `app/src/test/kotlin/com/oxygen/weather/app/AboutDisclosureStateHolderTest.kt`;
- `app/src/androidTest/kotlin/com/oxygen/weather/app/OfflineLaunchPersistenceInstrumentedTest.kt`;
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`.

## Contract

- The existing no-selection path remains the explicit Slice 20B compatibility
  default: Fahrenheit, km/h, hPa, mm, and km, including sub-kilometer meter
  formatting. Existing installed output must not change before a user selects a
  different option.
- The Units surface has four mutually exclusive choices: Oxygen default,
  Metric, US, and UK. Selecting Oxygen default clears the persisted selection;
  the other three save their existing `UnitPreference.Preset` value. A selected
  row has a semantic selected state and a minimum 48dp touch target.
- A successful selection is durable across a fresh state-holder/app start and
  remaps the currently displayed selected-location weather immediately. It does
  not call `WeatherRepository.refresh`, mutate `WeatherBundle`, write Room
  cache data, alter the selected location, or change source/provenance,
  freshness, condition/metric identities, nullable semantic values, or section
  ordering.
- Persisted custom values must round-trip through storage even though this
  surface does not offer custom editing. The encoding is a small versioned,
  discriminated Preferences DataStore record using enum names, with no provider
  literals. Invalid stored records must safely fall back to the compatibility
  default and must not crash startup.
- If a user-initiated write fails, leave the in-memory selection and Home
  presentation unchanged and show a provider-neutral local-preferences failure
  on the Units surface. Do not claim that the preference was saved.
- Loading and error Home states remain valid while Units is open. A future
  weather success maps with the active selection; no synthetic forecast is
  created merely to demonstrate a unit choice.
- The Units surface must preserve the current Settings / About back behavior:
  Units returns to its overview, and the overview returns to the exact Home or
  location-entry state that opened it.

## Acceptance Evidence

Focused tests must prove:

- Storage round-trips Metric, US, UK, and a fully populated custom preference;
  absent, incomplete, unknown, and invalid records return no selection without
  modifying stored data.
- State-holder startup with no stored preference preserves Slice 20B default
  strings. Startup with each preset maps the same canonical bundle through the
  corresponding existing mapper output.
- Choosing Metric/US/UK writes exactly the selected preference, updates the
  visible ready/cached/stale Home dashboard without refresh or cache mutation,
  and retains source, provenance, freshness, selected location, canonical
  semantic fields, nulls, identities, and section ordering. A failed write
  retains the old selection/dashboard and exposes the local failure.
- A production DataStore readback survives a new storage/state-holder instance.
- The Compose installed path opens Settings / About, opens Units, selects an
  alternate preset, returns to Home, and exposes changed unit-bearing Home
  strings (including Celsius from the deterministic fixture) while source and
  Home page interaction remain reachable. Compact and large-font Units layouts
  keep every choice and Back control readable and tappable.

## Workflow

1. Run and save focused baselines before edits:

   ```bash
   . scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastPresentationMapperTest' --tests '*HomeForecastStateHolderTest' --tests '*AboutDisclosureStateHolderTest'
   . scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.OfflineLaunchPersistenceInstrumentedTest
   ```

2. Add red tests for storage codec/read failures and state-holder preference
   selection/remapping before production code. Preserve the current mapper as
   the single conversion authority; do not duplicate unit conversion or
   formatting logic in storage, state, or Compose.

3. Implement the dedicated DataStore storage and inject it from `MainActivity`.
   Keep its record app-local, versioned, and independent of selected-location
   DataStore and Room schemas.

4. Thread an optional selected `UnitPreference` through the state holder to the
   existing mapper. Retain the canonical active bundle privately only for
   remapping; clear/replace it on selected-location transitions so no obsolete
   location can be remapped. Apply a new selection only after its storage write
   succeeds.

5. Add the narrow Units surface and state transitions, then connect it to
   `OxygenApp`. Use radio-style selection controls, stable test tags, and
   existing theme/layout conventions. Do not refactor the existing Settings /
   About model into generalized navigation.

6. Run focused unit tests, then add/run the production DataStore instrumented
   test and the Compose installed-path test. Capture the Units and changed Home
   semantics/screenshot evidence under
   `.codex/test-artifacts/2026-09-05-slice-20c-persisted-unit-selection/`.

7. Exercise the installed app after the focused connected test:

   ```bash
   scripts/start-emulator.sh
   . scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest
   scripts/install-debug.sh
   ```

8. Run broad verification and review the diff for behavior outside this slice:

   ```bash
   . scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
   . scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
   . scripts/android-env.sh && ./gradlew :app:assembleDebug
   git diff --check
   ```

## Out of Scope

Custom-unit editing UI; provider request units or mappings; core domain model
changes; forecast/cache/selected-location/saved-location schema changes;
location permission flow; alerts; appearance settings other than the selected
unit preference; provider wiring; theme completion; maps/radar/air quality;
widgets; background refresh; notifications; translations beyond directly
touched strings; release readiness; and MVP claims.

## Execution Result

Implemented and verified in the current changeset. Production changes add the
dedicated DataStore storage/codec, MainActivity wiring, state-holder loading and
remapping, Units surface, and About/Home connections. Tests cover codec
round-trips and invalid records, startup and immediate remapping invariants,
failed writes, production DataStore readback, and the installed Compose path.

Evidence artifacts:

- `.codex/test-artifacts/2026-09-05-slice-20c-persisted-unit-selection/`
- `installed-units-metric-selected.png`
- `installed-home-metric-persisted.png`
- `units-selection-home.png`
- `units-selection-home-semantics.txt`

Verification passed: focused unit tests, OfflineLaunchPersistenceInstrumentedTest
(10 tests), HomeDashboardUiTest (34 tests), direct single-test rerun with
screenshot capture, `:app:compileDebugKotlin`, full app/core unit tests,
`:app:assembleDebug`, and `git diff --check`. The first HomeDashboard connected
attempt was interrupted by the emulator going offline after four tests; the
restarted rerun passed all 34 tests.
