# Slice 30B1A3 — RTL Chronology and Spoken-Meaning Preservation

**Status:** planned
**Cycle ID:** `2026-09-10-slice-30b1a3-rtl-chronology-spoken-meaning`
**Mode:** bounded Home RTL chronology and spoken-meaning test implementation

## Selected behavior and acceptance boundary

Under a Compose RTL layout direction, Standard and Simple Home must preserve
the chronological order and meaning of forecast content already established in
LTR. Hourly and daily entries remain earliest-to-latest, visible time/date
labels remain mapper-owned presentation values, and semantic descriptions do
not change because the layout direction changes.

This is one Compose-local RTL content contract. It does not change provider
data, page navigation, localized strings, persistence, or device direction.

Acceptance is limited to:

- Standard RTL Hourly and Daily entries remain earliest-to-latest by their
  visible time/date labels;
- Simple RTL Forecast choices preserve the existing chronological labels and
  mapper-owned descriptions for the selected hourly/daily content; and
- the tested RTL descriptions and visible labels match the corresponding LTR
  deterministic fixture without altering weather values or page controls.

## Intended files

Expected:

- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`

Conditional only after a failing boundary assertion:

- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`

No provider, domain, repository, cache, preference, resource, manifest,
dependency, navigation, gesture, or production-only test-hook changes are in
scope.

## Focused evidence

Begin with red/baseline assertions before changing production code. Add one or
two named connected cases using the existing deterministic full-weather
fixture, `EffectsLevel.OFF`, and Compose-local `LayoutDirection.Rtl`. Compare
the RTL hourly/daily content order, visible labels, and semantic descriptions
with the corresponding LTR content. Keep the existing page and touch-target
contracts intact.

Focused command, limited to the new named cases:

```sh
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest#rtlHomeForecastChronologyAndSpokenMeaningRemainStable'
```

If the case is split into two named tests, update the filter before running it.
Acceptance requires every named case to complete with zero skipped and zero
failed on one ADB-ready emulator session. Save command output and the ledger
under `.codex/test-artifacts/2026-09-10-slice-30b1a3-rtl-chronology-spoken-meaning/`.

## Broad verification and limits

After focused green, run once as applicable:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

The acceptance boundary is the deterministic Compose Android test. Do not
add installed/manual RTL screenshots, UI hierarchies, device RTL, TalkBack
service traversal, compact/refetch, provider/network/cache/storage,
localization, alert, release, or MVP evidence here; installed RTL belongs to
Gate 30B1B1.
