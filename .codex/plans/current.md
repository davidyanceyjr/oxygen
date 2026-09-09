# Slice 28A1 - Paper Theme Rendering Baseline

**Status:** verified
**Cycle ID:** `2026-09-08-slice-28a1-paper-theme-rendering-baseline`
**Mode:** bounded visual implementation
**Basis:** Slice 18G design roles (`fae63b3`), Slice 18I compact behavior
(`02f701`), and Slice 27B3 layout restoration (`9ce6de6`) are complete. The
roadmap requires Paper rendering quality before persisted theme selection.
**Next action:** commit the verified Paper Home rendering slice, then perform
the post-commit authority sync.

**Outcome:** pass. Paper renders as a warm, opaque, typography-first Home
translation with Effects Off; the final six-case connected suite and broad
checks passed. Retained evidence is under the cycle artifact directory below.

## Behavior and Boundary

Make `OxygenThemeId.PAPER` a deliberate, non-persisted Home translation:
typography-first, low-decoration, warm neutral, readable in Simple and Standard,
and complete with Effects Off. Current `PaperSpec` values are scaffold, not
implementation evidence.

Functional invariants:

- Identical presentation state yields identical weather values, condition,
  stale/error and alert text, source/provenance, disclosure, page names/counts,
  content descriptions, callbacks, and accessibility order/actions.
- Standard stays `Now -> Hourly -> Daily -> Details`; Simple stays
  `Now -> Forecast` with Hourly/Daily choices. Theme changes neither layout nor
  effects state and causes no provider request/refetch.
- Effects Off renders no `home-weather-scene` and leaves all meaning reachable
  on opaque surfaces without gradients, transparency, glow, or animation.
- Oxygen remains the installed default. Existing Oxygen and Terminal values and
  behavior do not change.

Paper rendering contract:

- Explicitly map background, strong/ambient surfaces, outlines, content,
  accent, precipitation, weather-mark, and warning/error roles to warm Paper
  colors. Cards are flat and opaque with restrained outlines and shallow
  corners; dark glass/cyan Oxygen styling must not leak into Effects Off.
- Use platform fonts only: serif display numerals/headings for editorial
  hierarchy and clear body/supporting text at accessibility sizes.
- Clear, rain, snow, storm, and unknown marks remain distinguishable against
  Paper surfaces. Warning color reinforces visible alert/severity/error text;
  color is never the only signal.
- Normal and warning/error text on their rendered surfaces meet at least 4.5:1
  contrast; weather-mark strokes have observable pixel contrast.
- Typography, opaque surface treatment, shape, and color together make Paper
  materially distinct from Oxygen, rather than a palette-only variant.

Executable Paper oracles:

- Define named opaque Paper foreground/background role pairs and calculate
  WCAG relative luminance/contrast; normal and error text pairs must each be
  at least 4.5:1 after compositing (never infer contrast from alpha alone).
- A Paper condition-mark test samples the rendered mark bounds and Paper
  surface from its bitmap, requiring non-background pixels with measurable
  luminance/color distance; record sampled coordinates. Do not reuse Oxygen's
  gold-pixel threshold.

Layout boundary: portrait `360x640`, font scale `1.3`, Effects Off, with a long
location/source, all Standard pages, both Simple forecast choices, stale plus
official-alert content, Loading, and no-cache error/retry. Content may use the
existing localized scrolling but must not overlap, clip required meaning, or
introduce horizontal scrolling; existing 48dp targets remain intact.

## Intended Files

- `app/src/main/kotlin/com/oxygen/weather/app/ui/theme/OxygenTheme.kt`: define
  Paper typography and Material/Home role values within existing theme/design
  boundaries; keep repeated values as tokens and preserve Oxygen/Terminal.
- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`:
  consume the existing semantic warning/supporting-content roles for Paper
  alert severity and opaque Effects-Off text without changing weather meaning,
  layout, or navigation.
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`:
  add a `themeId` parameter to the direct Home fixture, shared semantic-contract
  helpers, Paper cases, and a compact `OxygenApp` fixture for no-refetch checks.

No Home composable edit is planned because Home already consumes semantic
roles. If baseline evidence proves a hard-coded Oxygen value blocks Paper,
revise this plan before adding another production file.

## Implementation Plan

1. First record a focused documentation-sync action to remove only README's
   stale “persisted layout selection/restoration” wording; theme persistence
   remains unimplemented.
2. Add two explicit seams: direct `HomeLoadingScreen(themeId)` for visual and
   semantic cases, and a separately named compact `OxygenApp` fixture with an
   `OxygenAppStateHolder` counting repository for effective-appearance and
   zero-refetch assertions. Neither proves MainActivity or Settings reachability.
3. On one pinned `oxygen_starter` ADB serial, run named
   `paperBaselineNowEffectsOff` (Standard, Paper, Effects Off, `360x640`, font
   `1.3`) before styling; retain screenshot/semantics and record inherited or
   low-contrast treatment without calling it a pass.
4. Encode three connected acceptance cases:
   - `paperStandardHomePreservesMeaningAcrossPagesEffectsOff`: deterministic
     ready state with stale refresh failure and a severe official alert;
     verify scene absence, named contrast/mark oracles, shared semantic
     contract equality against the same Oxygen render, and usable
     Now/Hourly/Daily/Details.
   - `paperSimpleHomePreservesMeaningAndForecastChoicesEffectsOff`: verify
     Simple Now/Forecast and Hourly/Daily, source/provenance, stale/alert meaning,
     bounds/targets, and no refetch via the compact OxygenApp fixture. If this
     cannot reuse the shared contract concisely, split it into the next planned
     slice rather than silently omitting it.
   - `paperOperationalHomeStatesRemainReadableEffectsOff`: verify Loading and
     no-cache error/retry text, disclosure, actions, and bounds.
5. Inventory exact Home role consumers and hard-coded clipping before adding
   production roles. Baseline inventory found the existing semantic warning
   role is not consumed by OfficialAlertSummary and that supporting text uses
   alpha overlays; the bounded HomeLoadingScreen role-consumer update is now
   included above. Keep existing 48dp targets and localized scrolling intact.
6. Implement only Paper's warm opaque palette, typography, surface/shape,
   outline, weather-mark/precipitation, and warning roles. Iterate using the
   repository's edit/build/install/capture/inspect loop and Base Art Sheet v0.2.
   Do not alter weather copy, values, semantics, layout, or navigation to make
   a screenshot look better.
7. Run the named baseline, three Paper cases, existing Oxygen Effects-Off and
   weather-mark regressions, plus targeted
   `terminalEffectsOffReadyMarkSmoke` (or an all-theme role-construction unit
   test). Use shared semantic equality for invariance; visual comparison is
   presentation evidence only.
8. Run broad checks once after convergence and review the diff for unchanged
   Oxygen/Terminal output, no dependency/assets, no component magic values, and
   no persistence/Settings/provider/layout/effects scope leakage. If Paper does
   not meet the boundary, retain evidence and report it unverified/deferred;
   do not unlock Slice 28B.

Connected instrumentation is the installed rendering boundary for this
pre-persistence slice: it runs production app/Home composables on Android with
Paper injected. It does not prove MainActivity or Settings theme reachability.

## Evidence and Commands

Artifacts:

```text
.codex/test-artifacts/2026-09-08-slice-28a1-paper-theme-rendering-baseline/
```

Keep `ledger.md`, `manifest.tsv`, baseline/final screenshots, and matching
concise semantics. Pin and record one ADB serial. Each manifest row maps
`state,page,dimensions,fontScale,theme,effects,testMethod,screenshot,semantics`;
mark rows also record sampled coordinates and contrast result. Since helpers
write private `context.filesDir`, after each run copy artifacts from the
debuggable package with `adb -s <serial> shell run-as com.oxygen.weather ...`
(`adb pull` where applicable), then verify filenames/checksums in the ledger.
Baseline is `paperBaselineNowEffectsOff`; final focused evidence is six named
case executions total (within the eight-case limit). Do not run the full
historical Home test class.

Focused final command:

```sh
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  "-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest#paperBaselineNowEffectsOff,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#paperStandardHomePreservesMeaningAcrossPagesEffectsOff,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#paperSimpleHomePreservesMeaningAndForecastChoicesEffectsOff,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#paperOperationalHomeStatesRemainReadableEffectsOff,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#effectsDisabledHomePathKeepsCompleteWeatherMeaningReachable,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#terminalEffectsOffReadyMarkSmoke"
```

Broad commands:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

Use `scripts/list-avds.sh` and one `scripts/start-emulator.sh` session as needed.
Do not rerun a pass unless production code, test input, or environment changed.

## Out of Scope

- Theme storage/state, Settings selection, migration/restoration, and
  MainActivity reachability (28B1/28B2).
- Terminal quality (28A2), high contrast (29), icon packs, Full effects, new
  layouts, or layout/effects persistence.
- Provider/repository/cache/location/unit/alert semantics; weather copy/values;
  navigation; packaged fonts/assets; dependency/Gradle changes.
- Whole-app Paper polish outside Home, release, or MVP claims.

## Phase State

- `specified`: roadmap/specification define Paper's direction and constraints.
- `planned`: this file selected only the Paper Home rendering baseline.
- `covered`: named connected cases encode semantic, contrast, mark, layout,
  operational, and no-refetch behavior.
- `implemented`: Paper theme roles and bounded Home consumers exist in the
  production rendering path.
- `verified`: final connected and broad checks passed on `emulator-5554`.
