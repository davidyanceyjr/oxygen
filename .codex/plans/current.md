# Oxygen Standard Now — CD-05 Celestial Halo and Now-local Atmosphere

**Status:** verified; implementation committed in `e8a24a1`; CD-06 selected
**Cycle ID:** `2026-09-18-cd-05-celestial-halo-now-atmosphere`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Implementation-slice count:** 2 of 2 since the CD-03 checkpoint reset; CD-06
is the active test/documentation-only checkpoint.

## Accepted outcome

The installed Standard Now page now renders a static condition-profile
atmosphere and a distinct concentric gold/cyan halo only when effects are
effective and the active page is Now. The scene is clipped to the Now page;
Hourly, Daily, Details, Simple Now/Forecast, and effective-Off states expose
neither treatment. The 150dp dial, satellites, current spoken description,
weather values, controls, page movement, refresh, alert, cache, source, and
provenance paths remain unchanged. Both Canvas hosts are hidden from
accessibility and expose no spoken content.

`WeatherCondition` mapping is explicit for every condition, including the
distinct `RAIN_SHOWERS` profile and neutral `UNKNOWN` fallback. No animation,
downloaded imagery, intensity/day-night inference, or provider/domain change
was introduced.

## Changed files

- `app/src/main/kotlin/com/oxygen/weather/app/ui/weather/WeatherScene.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/ui/weather/WeatherSceneTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/EffectsPreferenceInstrumentedTest.kt`

## Evidence

All artifacts are retained under
`.codex/test-artifacts/2026-09-18-cd-05-celestial-halo-now-atmosphere/`:

- same-route Room-cache Chicago baseline/final Off and Subtle PNG/XML pairs;
- `focused-unit.log` and `broad-checks.log`;
- accepted `connected-now-local/` and `connected-disabled-motion/` bundles;
- emulator recovery preflight/serial; and
- `verification-ledger.md` with commands, visual inspection, reruns, and limits.

Focused and broad results: the profile unit test passed; both named connected
methods passed 1/1 with zero failures, errors, or skips; app/core unit suites,
Kotlin compilation, debug assembly/install, and `git diff --check` passed.

## Closure and limits

The first recovery command needed the scoped artifact parent created; the first
baseline waited through location-entry/splash timing; and the first focused
compile lacked the new dial flag. Each changed retry passed, and all evidence
is preserved in the ledger. Connected runners removed app data after testing;
the required installed captures were taken before those runners.

Provider/repository/cache exercises were inapplicable because those boundaries
did not change. Full effects, other themes/high contrast, responsive/RTL,
localization, TalkBack service traversal, alert redesign, and release checks
remain out of scope or unverified.

**Next action:** execute CD-06's test/documentation checkpoint contract and
reset the implementation-slice count only after its accepted evidence and
documentation closure.
