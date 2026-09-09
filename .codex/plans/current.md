# Slice 29A — High-Contrast Rendering Contract

**Status:** committed at `0dccc94`
**Cycle ID:** `2026-09-09-slice-29a-high-contrast-rendering-contract`
**Mode:** bounded accessibility presentation and rendering implementation

**Basis:** Slices 18G/18I established the Standard Home visual and compact
accessibility baselines; Slices 28A1/28A2 and 28B1/28B2 established the three
rendered and persisted theme choices. Slice 29A now defines one non-persisted
high-contrast presentation axis and proves it through the real Home and alert
Composables before Slice 29B may expose or persist it.

**Authority correction completed before production edits:** specification
sections 20, 23, 37, and 51 now define high contrast as an independent
presentation axis layered over Oxygen, Paper, or Terminal.

**Next action:** select Slice 29B through the roadmap; do not treat it as
planned until a new active plan selects it.

## Selected behavior and acceptance boundary

When `HIGH` contrast is explicitly supplied through `OxygenAppearance`, the
selected Oxygen, Paper, or Terminal theme retains its identity while required
Home and official-alert information is rendered with opaque, luminance-distinct
content, surfaces, marks, controls, and boundaries. Weather values and units,
condition text, page identity, source/update/provenance, cache/refresh-failure
status, alert event/severity/issuer, and alert detail remain available as text
and semantics; color is only reinforcement.

`STANDARD` remains the default and must reproduce existing rendering and
behavior. Slice 29A deliberately provides no production Settings entry,
DataStore record, or `MainActivity` selection path. Its primary acceptance
boundary is deterministic connected Compose rendering of production
`OxygenApp`, Home, and alert-detail code at 360x640 portrait, font scale 1.3,
and Effects Off. This proves rendering readiness, not installed-user
reachability; Slice 29B owns reachability and restart persistence.

The rendered high-contrast contract must prove:

- normal, supporting, and warning text use opaque foreground/background role
  pairs with a project minimum contrast ratio of 7:1;
- meaningful component boundaries and weather-mark strokes reach at least 3:1
  against their adjacent surface, including after luminance-only comparison;
- selected alert/page state has a non-color cue (selected semantics plus
  visible text and/or a stronger outline), and alert severity remains explicit
  text;
- Standard Now, Hourly, Daily, and Details content remains readable and
  reachable without overlap at the compact large-font boundary;
- loading, no-cache error/retry, sparse/unavailable forecast, stale cached
  forecast, and official-alert states remain explicitly named and cannot be
  confused solely by removing hue;
- applying or removing high contrast does not recreate the app state holder,
  request provider data, reset the selected Home page/layout, change Effects,
  or change the persisted theme identity.

## Implementation contract

1. In `OxygenAppearance.kt`, add a separate two-value contrast type with
   `STANDARD` and `HIGH`, and append a defaulted `STANDARD` property to
   `OxygenAppearance`. Keep `OxygenThemeId` exactly Oxygen/Paper/Terminal and
   leave the existing theme preference codec and DataStore schema unchanged.
   Existing Kotlin call sites must continue to compile through trailing
   defaults.
2. In `OxygenTheme.kt`, append a defaulted contrast parameter to `OxygenTheme`
   and resolve the selected theme together with contrast through a pure,
   unit-testable role resolver. `STANDARD` must retain the current theme
   specifications. `HIGH` must retain the selected theme ID, typography family,
   sizing, and shape direction while substituting a deliberate opaque
   high-contrast palette and Home roles. Do not implement high contrast as a
   fourth theme or as brighter alpha variants.
3. Replace component-local outline alpha dilution with named resolved strong
   and quiet outline colors and resolved normal/selected border widths in the
   Home design roles. Standard values must reproduce the existing treatments;
   high-contrast values must remain opaque and meet the measurable boundary
   above. Supporting text and warning roles must likewise resolve explicitly,
   not fall back to reduced `onSurface` alpha in `HIGH`.
4. In `OxygenApp`, pass the effective contrast value into `OxygenTheme` while
   retaining it in `sessionAppearance`/`effectiveAppearance`. Do not add
   contrast to state-holder construction or `remember` keys: recomposing this
   presentation-only input must not rebuild the holder or refetch data.
5. Update Home cards, forecast rows/tiles, page selectors, weather marks, and
   Effects-Off role replacement only where needed to consume the resolved
   roles. Preserve existing visible condition/value/status text, semantics,
   paging, scrolling fallback, callbacks, and scene suppression.
6. Update the shared glass panel and alert selector/detail presentation to use
   the same resolved surface, outline, and warning roles. Keep explicit alert
   severity/event/issuer text, `Current alert`/`Select alert` text, selected
   semantics, source link behavior, and Back behavior unchanged.
7. Add no provider/domain models, bitmap assets, dependencies, test-only
   production switches, automatic Android contrast detection, or Settings
   controls.

## Intended files

- `docs/OXYGEN_FULL_SPECIFICATION.md` — pre-code architecture correction and
  post-verification implementation status.
- `app/src/main/kotlin/com/oxygen/weather/app/ui/theme/OxygenAppearance.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/theme/OxygenTheme.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/alerts/AlertDetailScreen.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/components/GlassPanel.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/HighContrastThemeContractTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`
- `README.md`, `.codex/plans/mvp-roadmap.md`, `.codex/cycles/history.md`, and
  this plan for the post-commit authority sync.

Do not modify `MainActivity`, `OxygenAppStateHolder`, Settings controls,
preference storage/codecs, provider clients, repositories, Room, location,
units, or existing theme IDs. If implementation appears to require one of
those changes, stop and split or re-plan rather than widening 29A.

## Tests and focused evidence

1. Before production edits, run the existing
   `officialAlertSummaryIsReadableEffectsOffAndOpensValidatedSourceLinks`
   connected case and retain its Oxygen/Standard/Effects-Off PNG and semantics
   as the visual baseline. Record the serial, AVD, size, and font scale.
2. Add JVM `HighContrastThemeContractTest` coverage that evaluates every
   Oxygen/Paper/Terminal + `HIGH` combination for opaque roles, the named 7:1
   text pairs, 3:1 outlines/marks, retained theme ID/typography family, and
   unchanged default `STANDARD` resolution. These are contract calculations,
   not a substitute for rendered evidence.
3. Add four focused methods to the existing `HomeDashboardUiTest` so its real
   production fixtures/helpers are reused instead of copied:

   - `highContrastStandardHomePreservesMeaningAcrossPagesEffectsOff` verifies
     Now/Hourly/Daily/Details, stale failure, severe alert, source/provenance,
     compact readable bounds, no weather scene, a matching semantic-content
     contract against Standard, rendered mark/boundary luminance, and final
     PNG/semantics artifacts.
   - `highContrastOperationalAndSparseStatesRemainDistinct` drives loading,
     no-cache error/retry, and a ready sparse/unavailable forecast; verifies
     truthful state labels, actions and 48dp targets, absence of fabricated
     values, compact scrolling, and separate final artifacts.
   - `highContrastAlertDetailKeepsHazardMeaningAndSelectionNonColorCues`
     navigates through `OxygenApp` from a multi-alert Home summary to detail;
     verifies explicit severity/event/issuer, verbatim description/instruction,
     `Current alert`/`Select alert`, selected semantics/outline treatment,
     source link, Back, readable bounds, and PNG/semantics artifacts.
   - `highContrastRecompositionPreservesAppearanceAndRequestCount` toggles only
     the non-persisted contrast input around a ready Paper + Simple + Effects-Off
     app fixture; verifies the same holder, forecast state, selected page,
     theme/layout/effects values, and repository request count.

   Record the actual compile/assertion failure from the first new test before
   implementing the contract; do not manufacture a red result if the worktree
   already contains candidate code.
4. In the final connected filter, run those four cases plus the existing Paper
   and Terminal Standard rendering regressions and the existing Oxygen alert
   summary baseline/regression: seven relevant connected cases total, within
   the default eight-case budget. Do not run the full 50-plus-case
   `HomeDashboardUiTest` class.
5. Screenshot and semantics files written in app-private test storage must be
   pulled into
   `.codex/test-artifacts/2026-09-09-slice-29a-high-contrast-rendering-contract/`.
   Record exact fixture provenance. No live forecast, production preference,
   or user-reachable installed high-contrast claim is permitted in this slice.

## Verification budget and commands

Budget: one emulator session; one pre-change connected baseline case; one JVM
red/green cycle; one four-case connected green run; one final seven-case
connected regression filter after visual convergence; one broad pass. Do not
rerun a passing command unless production code, test input, or the emulator
environment changed in a relevant way. If a connected attempt reaches a
bounded platform timeout, record it once and stop repeating that attempt.

Planned commands (the connected runner argument must name individual methods,
not the whole class):

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest \
  --tests 'com.oxygen.weather.app.HighContrastThemeContractTest'
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest#highContrastStandardHomePreservesMeaningAcrossPagesEffectsOff,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#highContrastOperationalAndSparseStatesRemainDistinct,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#highContrastAlertDetailKeepsHazardMeaningAndSelectionNonColorCues,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#highContrastRecompositionPreservesAppearanceAndRequestCount'
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest#highContrastStandardHomePreservesMeaningAcrossPagesEffectsOff,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#highContrastOperationalAndSparseStatesRemainDistinct,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#highContrastAlertDetailKeepsHazardMeaningAndSelectionNonColorCues,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#highContrastRecompositionPreservesAppearanceAndRequestCount,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#paperStandardHomePreservesMeaningAcrossPagesEffectsOff,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#terminalStandardHomePreservesMeaningAcrossPagesEffectsOff,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#officialAlertSummaryIsReadableEffectsOffAndOpensValidatedSourceLinks'
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

## Actual evidence

- Pre-edit baseline: `scripts/list-avds.sh`, one `oxygen_starter` emulator on
  `emulator-5554`, physical 1080x2400, font scale 1.0; the required alert
  summary passed 1/1 and its Oxygen/Standard/Effects-Off 360x640,
  font-scale-1.3 PNG and semantics are retained in the 29A artifact directory.
- Red JVM contract phase: `:app:testDebugUnitTest --tests
  com.oxygen.weather.app.HighContrastThemeContractTest` failed with the
  expected missing `ContrastLevel`/resolver API before production edits.
- Green focused JVM contract and connected evidence passed. The focused
  connected filter passed the four named 29A cases on
  `oxygen_starter`/`emulator-5554`; the final seven-case filter passed those
  four plus Paper, Terminal, and the existing alert-summary regression.
- Direct instrumentation reran the four cases to retain app-private PNG and
  semantics artifacts. Visual inspection confirmed opaque black/white roles,
  readable stale/sparse/alert wording, and stronger selected boundaries at
  360x640/font-scale-1.3 with Effects Off.
- Broad checks passed: `:app:compileDebugKotlin`, app/core debug unit tests,
  `:app:assembleDebug`, and `git diff --check`.
- Implementation commit: `0dccc94` (`Implement high contrast rendering
  contract`).

## Required document and commit sync

Post-commit authority sync: specification sections 20, 23, 37, and 51 now
define the independent contrast axis; README, roadmap, and this plan record
the verified non-installed 29A boundary; the next action remains selecting
29B through the roadmap. The cycle-history entry is appended with the exact
evidence and artifact path.

## Out of scope

- High-contrast DataStore/schema/state holder, Settings UI, automatic system
  detection, Activity recreation, force-stop restoration, or installed-user
  reachability.
- A fourth theme, new icon pack, theme redesign, Full effects, new layout, RTL,
  service-level TalkBack traversal, or Gate 30 completion.
- Provider/repository/cache/location/unit/alert-domain changes, new values,
  new assets/dependencies, release readiness, or MVP-complete claims.
