# Gate 30A3 — Home Speech/Layout Evidence and Document Sync

**Status:** planned
**Cycle ID:** `2026-09-09-gate-30a3-home-speech-layout-evidence`
**Mode:** bounded test-only evidence and documentation sync

**Basis:** Slice 30A2 is committed at `1a8e14f`. Its three named connected
cases and the installed Standard/Simple/large-font journey passed without a
production Home change. Gate 30A3 is the required third-cycle evidence and
documentation session before Slice 30B1.

**Discovery finding:** tests whose `setHomeContent(...)` call omits
`heightDp` render in a 3200dp root. Existing tests named `compact` therefore
do not establish a 360x640dp boundary. 30A2 must pass `heightDp = 640` at every
new or corrected compact assertion and use page scroll reachability rather than
requiring all forecast rows in one viewport.

**Next action:** review the 30A1/30A2 artifacts and run the minimum selected
speech/layout connected cases plus applicable repository checks. If a
regression is found, stop the gate and create a separately named repair slice;
do not alter production behavior inside this documentation gate.

## Gate 30A3 acceptance boundary

Rerun the selected 30A1 and 30A2 Home cases on one emulator, reconcile the
retained installed screenshots and hierarchies with the current contracts, and
sync cycle history, roadmap sequencing, README/spec status where directly
supported, and this active plan. Do not claim TalkBack service traversal, RTL,
reduced motion, theme/contrast invariance, alerts, or release readiness.

Use one `oxygen_starter` emulator session and the normal selected-location path;
do not use `SampleWeather.bundle` or seeded production state. Retain gate
artifacts under `.codex/test-artifacts/2026-09-09-gate-30a3-home-speech-layout-evidence/`.

## Completed Slice 30A2 record

## Behavior and acceptance boundary

At 360x640dp and font scale 1.3, a selected location with long location and
provider text keeps Standard `Now`, `Hourly`, `Daily`, and `Details` reachable,
and keeps Simple `Now`, `Forecast`, and both `Hourly`/`Daily` choices reachable.
At font scale 2.0, the representative long-provider Standard Details path stays
scroll-reachable. The representative 2.0 case is deliberately not a full
layout/theme matrix.

For each selected path, page identity, weather values, mapper-owned
descriptions, named page actions, source/provenance text, and 48dp navigation
and choice controls remain usable. Long text must wrap or be reachable by the
page's existing vertical scroll; it must not cover a sibling, extend outside the
360dp width, or make information inaccessible. The real Home screen must retain
the footer controls while page content scrolls. Celsius/Fahrenheit remapping and
layout/page changes must not issue a new forecast request or modify canonical
forecast data.

Primary automated boundary: `HomeDashboardUiTest` renders the production Home
path inside an explicit `Box(360.dp, 640.dp)` at density 1.0, verifies
scroll-reachability/bounds/touch targets, and uses the existing recording
repository for the Simple no-refetch check. The installed boundary is a normal
manual selected-location journey, never `SampleWeather.bundle` or seeded
app-private production state.

## Implementation

1. Correct the compact test setup first. New 30A2 tests, and any existing
   compact test reused as acceptance evidence, pass `widthDp = 360`,
   `heightDp = 640`, and the stated font scale. Replace any assertion that all
   rows fit simultaneously with `performScrollTo()` followed by positive,
   horizontally in-root bounds and non-overlap checks for the visible siblings.
2. Add exactly these focused connected cases in the existing Home class; retain
   its real presentation fixture, test tags, screenshot/semantics helpers, and
   `RecordingWeatherRepository`:

   - `standardCompactHomeAtFontScale13KeepsLongContentAndAllPagesReachable`:
     long location, long Open-Meteo attribution, stale/source context, and
     Fahrenheit; visit Now/Hourly/Daily/Details. Assert page title and named
     actions, exact existing mapper descriptions, scroll reachability of the
     long location/source/disclosure and representative forecast rows, valid
     horizontal bounds, and 48dp page/footer targets.
   - `simpleCompactHomeAtFontScale13KeepsCelsiusForecastChoicesReachableWithoutRefetch`:
     use the production `OxygenApp` state-holder fixture, select Simple and
     Celsius through its existing path, visit Forecast and both choice chips,
     assert their selected state, visible Celsius presentation and existing
     speech semantics, 48dp targets, scroll reachability, and an unchanged
     recording-repository request count.
   - `standardDetailsAtFontScale20KeepsLongProviderContentScrollReachable`:
     use the same truthful long-provider fixture at 2.0, navigate to Details,
     and prove the source values, metrics, sun/provenance footer, and Standard
     page controls are individually reachable without horizontal clipping or
     overlapping the item currently brought into view.

3. Preserve the existing current/hourly/daily `spokenDescription` contract and
   visible text. Do not infer weather semantics from display strings or loosen
   assertions to node counts, screenshots, or source text. Test bounds prove
   geometry/reachability; final screenshots provide the human visual review.
4. Change `HomeLoadingScreen.kt` only if one of those observable tests exposes
   a production failure. Limit a fix to the failing Home container, page
   scroll, header/footer, row, or text constraint. Prefer the existing
   `verticalScroll`, `heightIn`, `widthIn`, wrapping, and `LocalOxygenHomeDesign`
   roles. Add a design token in `OxygenTheme.kt` only for a repeated layout value
   needed by the production fix; do not change theme palettes or typography
   policy merely to pass geometry checks.

## Files and explicit limits

Required test file:

- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`

Conditional production files, only after a retained failing behavior boundary:

- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/theme/OxygenTheme.kt`

Do not change `:core`, provider/domain/repository/cache/storage behavior,
DataStore formats, selected-location flow, requests, navigation semantics,
alerts, effects/reduced-motion policy, themes/contrast, RTL, localization,
resources, manifests, dependencies, or test-only production routes.

Artifacts and the command/result/rerun ledger belong under
`.codex/test-artifacts/2026-09-09-slice-30a2-home-compact-large-font-resilience/`
and remain untracked.

## Evidence and verification budget

Budget: one emulator session; one pre-edit and one final installed exercise;
three named connected cases (well below the six-case limit); focused reruns only
when production code, fixture input, or the execution environment changed; and
one broad pass. Stop after one bounded platform timeout and record the exact
blocker instead of substituting fixture success.

1. Start one `oxygen_starter` session and record AVD/serial, physical display,
   `wm size`, `wm density`, logical 360x640 override and density 160, font
   scale, LTR, animation scales, theme, contrast, layout, effects, unit,
   selected location, and forecast/cache state. Capture pre-edit Standard and
   Simple screenshots plus UI hierarchies at 1.3 and the Details 2.0 baseline
   when the normal path is available. Restore every platform setting at the end.
2. Run the named 30A2 connected filter after the test setup change. A baseline
   pass is valid evidence. If an assertion exposes a Home defect, save its red
   result, make one bounded correction, then rerun only the affected named case
   plus the focused three-case filter.
3. After the final APK change, install once. Via normal manual search/saved
   selection, set Effects Off and exercise Standard at 1.3 through all pages,
   Simple at 1.3 through both Forecast choices, and the representative 2.0
   Details overflow path. Retain screenshots and `uiautomator dump` hierarchies;
   inspect that important text and controls are visible or scrollable. This is
   presentation evidence, not TalkBack traversal proof.
4. Run the broad commands once after focused green. Keep the ledger separate
   from ephemeral Gradle/device output and record every rerun reason.

```sh
scripts/list-avds.sh
scripts/start-emulator.sh
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest#standardCompactHomeAtFontScale13KeepsLongContentAndAllPagesReachable,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#simpleCompactHomeAtFontScale13KeepsCelsiusForecastChoicesReachableWithoutRefetch,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#standardDetailsAtFontScale20KeepsLongProviderContentScrollReachable'
scripts/install-debug.sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

## Required document updates after evidence

This plan selection is the only documentation change now. Do not update
`README.md` or the specification with an unverified resilience claim.

After an implementation commit, perform the mandatory authoritative doc sync:

- append the self-contained 30A2 result, exact evidence, artifacts, blockers,
  and commit state to `.codex/cycles/history.md`;
- update `.codex/plans/current.md` to select Gate 30A3 and update
  `.codex/plans/mvp-roadmap.md` only with the actual 30A2 commit/next-candidate
  state;
- update `README.md` and specification sections 46/53 only if the installed
  evidence proves the precise compact/large-font behavior stated above; retain
  the limits on TalkBack, RTL, reduced motion, alert layout, and theme/contrast
  invariance; and
- leave provider, privacy, license, attribution, and provider-contract
  documents unchanged because this slice does not alter those facts.

Gate 30A3 remains the no-production-change, third-cycle session that reruns the
selected 30A1/30A2 cases and reconciles the broader Home accessibility evidence
and documents. A defect found there creates a separately named repair slice.

## Out of scope and ready criteria

Out of scope: service-level TalkBack traversal/pronunciation or focus order;
RTL; disabled-animation/reduced-motion; theme/contrast matrix; alerts;
Appearance/Settings controls; localization; release or MVP claims; and Gate
30A3 closure.

30A2 is ready only when the explicit 360x640 connected cases pass (or a real
red production defect has been corrected and passes), the final installed normal
path covers the stated Standard/Simple/2.0 journeys, mapper-owned weather
meaning and request counts remain unchanged, the ledger names all commands and
skips, broad checks pass, and the post-commit authority sync is complete.
