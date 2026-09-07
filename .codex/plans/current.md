# Slice 25A - Settings Information Architecture

**Status:** committed
**Commit state:** committed
**Implementation commit:** `2484e90`
**Cycle ID:** `2026-09-06-slice-25a-settings-information-architecture`
**Planning basis:** `cab3b29` after committed Slice 24B completion evidence at
`b7e3514`. The worktree was clean at discovery.

**Authority reconciliation:** The 2026-09-06 pre-implementation documentation
sync aligns the roadmap, specification next-task text, README status, and live
cycle summary with committed behavior through Slice 24B. Gate 25 remains
specified and incomplete; Slice 25A is committed in `2484e90`.

## Scope resolution

The roadmap contains two different numbered items: `Gate 25`, the disclosure
baseline audit, and `Slice 25A`, the Settings information-architecture feature.
This plan selects only Slice 25A because the requested work is an implementation
slice. Gate 25 remains a separate documentation/audit cycle and must complete
before Slice 26 or release work claims its disclosure prerequisite.

Slice 25A was sequenced before or with Slice 20C but was never separately
planned, verified, or committed. Slice 20C added a real Units destination to the
older `Settings / About` surface; it did not complete the seven-destination
Settings contract. This cycle closes that gap without reopening unit behavior.
The committed changeset implements and verifies the slice.

## Observed baseline

- Home and location entry can open one `Settings / About` route.
- The route exposes Units, Data Sources, Privacy, and Open Source Licenses.
- Units is backed by the production unit-preference path.
- Saved locations, search, selection, removal, and optional device location are
  real behavior, but they are reached through the separate location-entry flow.
- Appearance is not persisted or user-selectable. The installed app currently
  renders the Oxygen theme, Standard Home layout, and the supplied runtime
  `OxygenAppearance.effects` value.
- There is no separately reachable About destination; About copy is rendered on
  the root surface.

## Acceptance boundary

Replace the mixed `Settings / About` surface with a coherent, scrollable
`Settings` root that exposes these distinct destinations:

- Appearance;
- Units;
- Locations;
- Data Sources;
- Privacy;
- Open Source Licenses;
- About.

Each destination must lead to truthful installed behavior:

- Appearance is a read-only summary of the effective theme, Standard layout,
  and runtime effects level. It has no selector, disabled control, future-work
  placeholder, or persistence claim.
- Units retains the existing persisted Oxygen default, Metric, US, and UK
  selection behavior unchanged.
- Locations enters the existing production location surface, including saved
  rows, manual search, optional device location, selection, save, and removal.
  Back returns to the same Settings root; choosing a location continues through
  the existing selected-location Home path.
- Data Sources, Privacy, and Open Source Licenses retain their existing content
  and remain independently reachable.
- About contains the current Oxygen identity/product copy that is presently on
  the mixed root.

The root groups destinations by meaning, uses visible text rather than icon-only
navigation, exposes at least 48 dp touch targets, and remains readable at
`360dp x 640dp`, density `1`, font scale `1.3`, and `EffectsLevel.OFF`.
In-surface Back and Android Back return from a destination to Settings, then to
the exact originating Home or first-run/location state.

## Compatibility contract

- Do not change forecast, alert, fallback, geocoding, location acquisition,
  saved-location, cache, unit-conversion, or persistence semantics.
- Do not add dependencies, permissions, storage keys, Room entities/migrations,
  provider requests, analytics, accounts, background work, or network calls.
- Do not implement theme, layout, contrast, custom-unit, or effects selection.
  Those remain Slices 26 through 29.
- Do not create empty routes, disabled future controls, TODO copy, or duplicate
  location/unit implementations.
- Preserve every existing disclosure statement unless Gate 25 later proves a
  factual correction is required.
- Preserve Home refresh and unit-remap updates to the Home state beneath an open
  Settings route. Preserve alert detail, stale-cache, retry, and location-switch
  behavior.
- Preserve first-run manual search state when Settings is opened and closed.
  Entering Locations from Settings may use a fresh location-entry state, but
  backing out through Settings must restore the original first-run state.
- Keep `OxygenAppearance` and canonical weather data unchanged. The Appearance
  summary reports effective presentation; it does not become preference state.
- Treat unused theme/layout enum values as scaffold, not selectable or effective
  settings. Report only the theme and layout the installed surface actually uses.

## Production design

### Settings model and route

Evolve the current About-specific names into one Settings route and a small
semantic destination enum. Migrate all production and test consumers in the
same changeset; do not retain a parallel legacy route.

The Settings route owns one `selectedDestination` and its exact `returnScreen`.
Selecting a normal destination changes only `selectedDestination`. Back from a
destination clears it; Back from the root restores `returnScreen`.

Keep the existing recursive visible/return-screen handling so forecast refresh,
unit remapping, and other valid Home replacements update the one Home state
beneath Settings. Extend route tests rather than introducing a navigation
library for this bounded app.

### Locations handoff

Generalize the location-entry return target from Home-only to an app screen so
Settings can open the existing location surface and receive Back. Keep a
separate `canReturn` presentation fact for Back visibility; retain Home-specific
logic only where Home is actually required.

The Settings -> Locations transition must:

1. preserve the Settings route and its original return screen;
2. load saved rows through the existing storage method;
3. perform no forecast/provider request merely by opening or backing out;
4. route a selected search/saved/device location through the existing Home
   selection path, intentionally leaving Settings;
5. suppress redundant Settings-within-Settings entry on this nested location
   surface.

### Compose surface

Move the mixed About UI into `ui/settings/SettingsScreen.kt`. Render a compact
root with clear category headings and full-width labelled rows. Reuse the
existing units and disclosure bodies. Render the Appearance summary from the
actual `OxygenThemeId` and `OxygenAppearance.effects` passed to the app; label
the fixed current layout as Standard. Render the prior root product copy only in
About.

Use existing Material 3 and Oxygen theme roles. Do not introduce a new design
system, icon set, animation, preference widget abstraction, or responsive
navigation framework in this slice.

Expected production files:

- `app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/AboutDisclosureContent.kt`
  (rename only if all consumers move atomically)
- `app/src/main/kotlin/com/oxygen/weather/app/ui/settings/SettingsScreen.kt`
  (new Settings surface; the old About surface is removed)
- `app/src/main/kotlin/com/oxygen/weather/app/ui/firstrun/FirstRunLocationEntryScreen.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`

## Red-first focused coverage

Extend or rename existing tests only where their responsibility changes.

1. State-holder coverage proves all seven destinations are distinct and
   reachable from Home and first run; destination/root Back restores the exact
   prior state; unknown or out-of-context events are no-ops.
2. Location routing coverage proves Settings -> Locations loads real saved rows,
   Back returns to Settings without a repository refresh or storage mutation,
   and saved/manual selection still reaches Home through existing identity and
   stale-request protections.
3. Unit regression coverage proves selection still persists and immediately
   remaps the Home return state without provider refresh or cache mutation.
4. Route-helper coverage proves refresh/loading/success replacement while
   Settings is visible updates its return Home and does not dismiss the selected
   non-location destination.
5. Compose coverage follows Home -> Settings -> each destination, verifies the
   read-only effective Appearance values and absence of preference controls,
   follows Locations into the real location surface and back, and exercises
   both in-surface and Android Back.
6. Compact connected coverage asserts root rows and Back controls have readable
   bounds, no sibling overlap, usable scroll reachability, and minimum touch
   targets at the acceptance configuration.

Primary test files:

- `app/src/test/kotlin/com/oxygen/weather/app/AboutDisclosureStateHolderTest.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastStateHolderTest.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/FirstRunLocationStateHolderTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`

## Visual and real-path evidence

Created
`.codex/test-artifacts/2026-09-06-slice-25a-settings-information-architecture/`
and start `ledger.md` before verification. Capture the installed Settings/About
baseline before meaningful UI edits using the single task emulator session and
`scripts/capture-screen.sh`. Start or reuse that session through the repo-local
emulator helpers; no device was connected during the documentation sync.

After convergence, install the changed APK once, launch through the normal app
entry, and capture:

- `settings-root-360x640-font-1.3.png`;
- `settings-appearance-effects-off-360x640-font-1.3.png`;
- `settings-locations-360x640-font-1.3.png`;
- matching semantics text for the Settings root and nested Locations journey.

The deterministic connected journey is the acceptance boundary when installed
provider availability or saved state cannot supply every destination state.
Screenshots prove presentation only; the state and connected interaction tests
prove navigation behavior.

## Verification budget

Budget: one emulator session, one APK install after the final APK change,
60 minutes, and 10,000 agent tokens. Record each command, result, and reason for
any rerun. Do not rerun a passing command unless relevant code, fixtures, or the
environment changed.

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*AboutDisclosureStateHolderTest' --tests '*HomeForecastStateHolderTest' --tests '*FirstRunLocationStateHolderTest'
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

## Completion and authority sync

Review completed: no duplicate route, placeholder UI, accidental preference
behavior, or compatibility-contract expansion was found. The changed
production/test files and actual command evidence are recorded in the cycle
ledger.

Verification result: all seven destinations are reachable; Units and
disclosures retain their existing behavior; Locations uses the real
location-entry surface and returns through Settings; compact connected coverage
passed with both in-surface and Android Back. Appearance reports the actual
theme, Standard layout, and runtime effects without adding preference controls.

After an implementation commit, append one concise self-contained cycle entry
and synchronize:

- `.codex/plans/current.md` to the actual committed/verified state;
- `.codex/cycles/history.md` recent summary;
- `.codex/plans/mvp-roadmap.md` for the factual 25A status and current next
  candidate, without marking Gate 25 complete;
- `README.md` and `docs/OXYGEN_FULL_SPECIFICATION.md` only where installed
  Settings reachability/status changed.

Do not change `DATA_SOURCES.md`, `PRIVACY.md`, `THIRD_PARTY_LICENSES.md`,
`NOTICE`, or provider contracts merely to make Gate 25 appear complete. Their
accuracy audit belongs to the separate Gate 25 cycle.
