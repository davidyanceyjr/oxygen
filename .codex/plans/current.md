# Slice 30B1A4 — RTL Compact Layout and No-Refetch Evidence

**Status:** implemented (connected acceptance incomplete)
**Cycle ID:** `2026-09-11-slice-30b1a4-rtl-compact-layout-no-refetch`
**Umbrella:** Slice 30B1 — Home RTL Navigation and Chronology
**Mode:** bounded Home RTL compact-layout and request-count implementation/test

## Execution status

The two planned connected cases and the default-LTR-preserving test helper
parameter are implemented in `HomeDashboardUiTest.kt`. Android test
compilation, app/core unit tests, debug assembly, and `git diff --check` pass.
The Standard case first exposed an assertion-input mismatch, which was
corrected to the existing full provider/fetch text. Its corrected connected
run installed and started on API 37 but stalled at `Tests 0/1 completed`
without a test result; the Simple case was not run. No production correction
was made, and the slice is not verified or ready for the next gate.

## Selected behavior

At Compose-local `LayoutDirection.Rtl`, the production selected-location Home
composition remains usable in a 360x640 dp root at font scale 1.3 with Effects
Off and deliberately long selected-location and provider-attribution input.
Standard and Simple Home controls must stay horizontally within the root,
non-overlapping, and at least 48dp in both dimensions where exposed. Long Home
content must remain reachable through existing vertical scrolling. Changing
Standard Home pages, switching the in-session layout through Appearance, and
choosing Simple Hourly or Daily must not issue another forecast request.

The acceptance boundary is the existing `OxygenApp` selected-location path
with `OxygenAppStateHolder`, `DirectExecutor`, and
`RecordingWeatherRepository`. It is not a static `HomeLoadingScreen` test:
the request invariant must cover the production composition handling layout
change and page/choice interaction. This slice does not re-prove physical
mirroring, gestures, chronology, or spoken-weather equivalence; those contracts
are committed in 30B1A1, 30B1A2, and 30B1A3A1–30B1A3B1.

## Contract and limits on implementation

- Provide RTL only in the Android test composition. Do not change device
  direction, locale, resources, or the production RTL policy.
- Use the existing `fullWeatherBundle` fixture with a long
  `WeatherLocation` name and long `forecastProvenance` source name. They
  are stress inputs; canonical weather, mapper-owned descriptions, and
  production strings must not change.
- Save the ready baseline as the exact repository location list, normally
  `listOf(location)`, and compare it after every acceptance interaction. A
  page, layout session update, or Simple choice must not call
  `WeatherRepository.refresh`.
- Layout switching proves the existing in-session path only. Persistence,
  Activity recreation, and failed-write/retry behavior are out of scope.
- A production correction is allowed only after a failing focused assertion and
  must be the smallest correction in the existing Home layout/request path,
  normally `HomeLoadingScreen.kt`. Do not change providers, core models,
  cache, persistence schema, resources, manifest, dependencies, navigation
  model, or gesture implementation.
- If a correction needs another state machine or platform boundary, stop and
  create a separately named roadmap slice.

## Intended files

- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`
  — two focused connected cases; extend only
  `setCompactOxygenAppContent` to accept a `layoutDirection` parameter that
  defaults to LTR, then provide it via `LocalLayoutDirection`.
- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`
  — only if red rendered evidence needs a minimal Home-only correction.
- `.codex/test-artifacts/2026-09-11-slice-30b1a4-rtl-compact-layout-no-refetch/`
  — ignored focused/broad logs, result XML/textproto, relevant semantics or UI
  hierarchy, emulator metadata, and a concise rerun ledger.

No other production or contract file changes are planned before a demonstrated
failure.

## Focused acceptance tests

Add exactly these two named connected cases to `HomeDashboardUiTest`. Each
uses density 1, a 360x640 dp root, font scale 1.3, Effects Off,
`LayoutDirection.Rtl`, a fresh state holder, and one deterministic successful
repository fixture.

1. `rtlStandardHomeCompactLongContentKeepsControlsReachableWithoutRefetch`

   Start Standard `OxygenApp`, wait for ready state, and save the exact
   repository request list. Assert `Now` / `Page 1 of 4`. For the Standard
   page tabs and Home secondary actions, assert positive compact-root bounds,
   48dp targets, and no pairwise overlap within each visible group. Reach the
   long location and source/update text on Now. Visit Hourly, Daily, and
   Details through existing tabs; on each, assert page identity and
   representative forecast or provider/detail content is reachable. Apply the
   existing scroll-aware bounds/non-overlap assertions to rendered forecast
   rows and Details sections brought into view. Compare the exact repository
   location list after ready state and each page transition.

2. `rtlSimpleHomeCompactLayoutAndForecastChoicesDoNotRefetch`

   Start a fresh Standard `OxygenApp` path, save its exact ready request list,
   navigate through Settings to Appearance, select Simple, and return Home.
   Assert Simple selection plus `Now` / `Page 1 of 2`. Apply the same
   compact-root, 48dp, and non-overlap assertions to the Simple page tabs and
   Home secondary actions; reach long location and source/update content.
   Open Forecast, verify Hourly selection, then use existing scroll-aware
   bounds and touch-target helpers for both Hourly/Daily choices. Assert
   representative Hourly content is reachable; choose Daily and assert its
   selected state and representative Daily content/row. Compare the exact
   repository location list after layout change, Home return, Forecast
   navigation, and both choice states.

Both cases must fail for horizontal clipping, a checked overlap, a checked
target below 48dp, unreachable required content, wrong page/choice state, or
an extra refresh. Reuse `assertWithinRootBounds`,
`assertMinimumTouchTarget`, `assertMinimumTouchTargetAfterScroll`,
`assertReadableBoundsAfterScroll`, `assertTextWithinRootBoundsAfterScroll`,
and `assertNoSiblingOverlap`; add a generic helper only if necessary. Do not
duplicate 30B1A2 left/right or swipe assertions, or turn this into a Settings
layout test.

## Implementation sequence

1. Review the committed 30A2 compact/no-refetch and 30B1 RTL cases; retain them
   as baseline evidence rather than modifying them.
2. Add the default-LTR compact `OxygenApp` helper parameter and the two cases.
   The first focused execution is the red-or-baseline check.
3. If green, leave production Kotlin unchanged: this is a test/evidence
   implementation slice. If red, make the smallest Home-only correction, rerun
   only the affected named case after an APK change, and reconfirm exact
   request-list and weather-meaning invariants.
4. Preserve focused results, any failure evidence supporting a correction,
   final semantics or hierarchy needed to explain geometry/reachability,
   emulator/API metadata, and command/rerun ledger in the cycle artifact
   directory.

## Verification budget

Use one ADB-ready emulator session. The focused suite is exactly two connected
test cases, run with one named method filter per command; do not run the
historical Home class. Install only when a changed APK requires it. Do not
rerun a passing command unless source, test inputs, APK, or environment changed;
record the reason and result in the ledger.

Focused real-path evidence:

```sh
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest#rtlStandardHomeCompactLongContentKeepsControlsReachableWithoutRefetch
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest#rtlSimpleHomeCompactLayoutAndForecastChoicesDoNotRefetch
```

After focused green, run once:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

The connected cases are the real-path exercise. They do not establish
device-wide or installed/manual RTL, screenshot parity, TalkBack traversal,
provider/network behavior, or release readiness.

## Required closure documentation

Only after focused and applicable broad checks pass, make the implementation
commit with its required descriptive subject and evidence/limits body. Then
perform the post-commit authoritative documentation sync:

- update this plan to the actual completed/committed state and select Gate
  30B1B1 only when A4 evidence is complete;
- update the A4 roadmap entry with exact verified behavior, artifact path,
  commit, and limits;
- append a concise self-contained cycle-history entry with both exact results,
  emulator/API/density/font-scale/Compose-local RTL conditions, commands,
  artifacts, correction-or-baseline outcome, skipped checks, limits, and
  commit state;
- review `README.md` and `docs/OXYGEN_FULL_SPECIFICATION.md`. Change them
  only if retained evidence supports a narrower factual claim; do not claim
  complete RTL, installed RTL, TalkBack, localization, reduced-motion,
  provider, or release coverage.

No provider, core, persistence, or dependency documentation update is required
unless a demonstrated failure expands scope.

## Out of scope

Device-wide or installed/manual RTL, screenshots as acceptance evidence,
font-scale-2.0 coverage, TalkBack service traversal, reduced motion,
theme/contrast matrices, localization, alerts, provider/network behavior,
persistence correctness or schema changes, release readiness, and MVP
completion remain out of scope. The committed 30B1A1, 30B1A2, and
30B1A3A1–30B1A3B1 slices remain the authorities for semantic navigation,
directional affordances, chronology, and RTL/LTR spoken-meaning equivalence.
Gate 30B1B1 owns the later installed RTL and documentation closure.
