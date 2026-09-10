# Slice 30B1 — Home RTL Navigation and Chronology

**Status:** planned
**Cycle ID:** `2026-09-10-slice-30b1-home-rtl-navigation-and-chronology`
**Mode:** bounded Home RTL behavior implementation and evidence

## Selected behavior and acceptance boundary

Make the existing Standard and Simple Home page navigation and forecast
progression deliberately correct under RTL without changing chronological
weather data, spoken meaning, provider behavior, or persisted unrelated state.

The primary boundary is the installed Home surface at RTL, supported by no
more than five named connected cases. Directional layout and visible
previous/next affordances must mirror appropriately, while semantic page
identity and named actions continue to mean chronological backward/forward
movement. Hourly and daily data remain earliest-to-latest, with unchanged
spoken time/date meaning. At 360x640 dp and font scale 1.3, long location text
and both layouts retain usable page controls, at least 48dp targets, no
horizontal overlap, and overflow reachability.

## Basis and intended files

30A3B2 closed the retained Home speech/layout evidence boundary in
documentation commit `fb51f7b` without claiming TalkBack service traversal,
reduced-motion or appearance invariance,
alerts, localization, a complete large-font matrix, release readiness, MVP
completion, or the rest of Gate 30. Preserve those limits here.

Expected test boundary:

- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`

Conditional production files only after a failing RTL behavior assertion:

- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/theme/OxygenTheme.kt`

Do not change provider/domain/repository/cache/storage behavior, preferences,
alert behavior, forecast semantics, unit values, effects policy, themes or
contrast, localization, or TalkBack service setup in this slice.

## Focused evidence and verification

Use one emulator session and restore its direction after the run. Record the
exact environment, command results, screenshots, and UI hierarchies under
`.codex/test-artifacts/2026-09-10-slice-30b1-home-rtl-navigation-and-chronology/`.
The installed journey must use the production selected-location path rather
than sample or seeded production data.

Focused evidence: no more than five named connected RTL cases covering
Standard/Simple page progression, chronological hourly/daily order, semantic
previous/next actions, compact large-font bounds, and no-refetch layout/page
changes; plus one installed RTL Home journey.

Broad checks after focused green:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

If a production defect appears, add a failing assertion first and keep the
correction within the RTL Home boundary. Do not broaden this slice into the
reduced-motion/theme/contrast, alert, Settings, or TalkBack gates.
