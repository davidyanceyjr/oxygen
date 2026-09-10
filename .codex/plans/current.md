# Slice 30B1A2 — RTL Directional Affordances and Gesture Behavior

**Status:** planned
**Cycle ID:** `2026-09-10-slice-30b1a2-rtl-directional-affordances-gesture`
**Mode:** bounded Home RTL controls and pager-gesture implementation/evidence

## Selected behavior and acceptance boundary

Under an RTL Compose layout direction, Standard and Simple Home must present
their visible page controls and horizontal pager movement with appropriate RTL
directional affordances while preserving the semantic page contract from
30B1A1. Semantic “previous” and “next” continue to mean the immediately
earlier and later page; they are not redefined as physical left/right.

The bounded acceptance boundary is one connected case for Standard and one for
Simple. Each case must exercise the RTL page controls and horizontal pager
gesture, confirm the resulting page titles and `Page N of M` positions, and
confirm the named custom actions remain chronological. Every exercised page
control must retain a measured minimum 48dp touch target.

30B1A1 already proved the Compose-local RTL semantic action progression and
required no production correction. This slice begins with directional behavior
coverage; if a new assertion fails, preserve the red evidence and make the
smallest correction in the existing Home navigation/layout path.

## Intended files

Expected:

- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`

Conditional after a behavior-red result:

- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`

Do not introduce a parallel navigation model, raw left/right semantic labels,
new gesture framework, provider/domain changes, direction-dependent data
ordering, or production-only test hooks.

## Focused connected tests

Add exactly these two named cases:

- `rtlStandardHomeDirectionalAffordancesMirrorAndGestures`: render Standard
  Home with `LayoutDirection.Rtl` and `EffectsLevel.OFF`; verify the visible
  page selector direction/positions and 48dp targets, use the RTL pager gesture
  to move through an intermediate page, and confirm the existing named action
  labels and semantic destinations remain chronological.
- `rtlSimpleHomeDirectionalAffordancesMirrorAndGestures`: render
  `LayoutPreset.SIMPLE` with `LayoutDirection.Rtl`; verify the two-page control
  direction and 48dp targets, exercise the pager gesture in both directions,
  and confirm Now/Forecast titles, positions, and named actions.

Do not duplicate forecast chronology, compact/font-scale, request-count,
installed-path, screenshot, or device-level RTL work owned by 30B1A3–30B1B1.

Focused command:

```sh
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest#rtlStandardHomeDirectionalAffordancesMirrorAndGestures,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#rtlSimpleHomeDirectionalAffordancesMirrorAndGestures'
```

Acceptance requires no more than two completed, zero skipped, and zero failed
cases on the same ADB-ready emulator session used for this cycle unless the
environment changes. RTL remains Compose-local and no device direction state
is changed.

## Broad verification

After focused green:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

Save the ledger and command output under
`.codex/test-artifacts/2026-09-10-slice-30b1a2-rtl-directional-affordances-gesture/`.
Do not rerun passing checks unless production code, test inputs, or the
execution environment changes in a way that can affect the result.

## Required closure

After a verified implementation/test commit, append a concise self-contained
history entry with exact evidence, artifacts, skipped installed evidence,
limits, and commit hash. Mark 30B1A2 committed in the roadmap and replace this
plan with the next bounded candidate only after the evidence supports closure.
Review README and `docs/OXYGEN_FULL_SPECIFICATION.md`; do not claim complete
RTL support before Gate 30B1B1.

## Out of scope

Forecast values/order, spoken time/date meaning, provider/repository/cache/
storage/preferences, unit/effects/theme/contrast/localization/alerts,
compact layout and refetch behavior, screenshots/UI hierarchies, installed
manual RTL journey, TalkBack service behavior, release readiness, and MVP
completion claims remain unchanged.
