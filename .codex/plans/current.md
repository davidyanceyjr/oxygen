# Slice 30C2 — Official-Alert Detail Accessibility

**Status:** planned
**Cycle ID:** `2026-09-13-slice-30c2-official-alert-detail-accessibility`
**Prerequisite:** Slice 30C1, committed at `4ffc507`.

## Selected behavior and acceptance boundary

Make the existing Home official-alert detail route a complete, logically
ordered, scrollable reading surface. For an available alert, the installed
detail screen must expose event, explicit severity, issuer, effective time,
expiry, affected area, official description and instructions without
paraphrase, source-check time, attribution, and the validated official-source
action. Multiple-alert selection, Back, and source actions must be meaningful,
selected where applicable, and at least 48dp. Return to Home must preserve the
same forecast/alert state and request count.

The deterministic boundary owns detail rendering and interaction. A live NWS
alert is not a fixture; the installed attempt is observational and must report
truthful no-alert availability.

## Implementation limits

- Preserve `WeatherAlert`, `AlertLookupStatus`, NWS routing, validated source
  URLs, official text, cache policy, canonical weather, and the existing
  summary route.
- Do not parse display strings into data, abbreviate critical instructions, or
  add a second alert model/selection state machine.
- Start with the existing `AlertDetailScreen.kt`; change only the smallest
  demonstrated detail rendering/semantics defect. Do not reopen C1 summary
  behavior, provider transport, persistence, notifications, or Settings.

## Planned focused tests

Add or extend deterministic fixtures in
`app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`:

1. One complete detail document case at 360x640, font 1.3, LTR, Effects Off:
   reading order, every required field, exact multiline text, source action,
   Back, and 48dp targets.
2. Multiple-alert selection case: selected state, distinct event/issuer/text,
   source URI, and return behavior.
3. Long detail-content case at font 2.0, RTL, Effects Off, and High contrast:
   event/area/description/instructions remain scroll-reachable and controls
   remain usable without overlap.
4. No-alert/unavailable detail guard case: no fabricated document and Home
   summary remains truthful.

Run affected unit methods before connected tests. Use no more than six named
connected cases and `run-connected-method.sh`; do not run the historical Home
class. Use one emulator session and retain screenshots, semantics/UI
hierarchies, exact results, and rerun decisions under:

`.codex/test-artifacts/2026-09-13-slice-30c2-official-alert-detail-accessibility/`

## Verification commands

```text
sh -n scripts/run-connected-method.sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests 'com.oxygen.weather.app.HomeForecastPresentationMapperTest' --tests 'com.oxygen.weather.app.HomeForecastStateHolderTest'
. scripts/android-env.sh && ./gradlew :app:compileDebugAndroidTestKotlin
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

After focused green, install the debug APK once per APK change and attempt a
real manual location. If no alert is live, record the no-alert result and do
not seed installed alert data. Restore any changed device settings.

## Intended files and out of scope

- `app/src/main/kotlin/com/oxygen/weather/app/ui/alerts/AlertDetailScreen.kt`
  only if a red rendered boundary requires it;
- `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapperTest.kt`
  and `HomeForecastStateHolderTest.kt` only for missing detail contract seams;
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`
  for the named rendered boundaries.

Out of scope: alert transport, cache/persistence, background polling,
notifications, additional providers, summary changes, appearance redesign,
localization, service-level TalkBack traversal, and release readiness.

## Next action

Inspect the existing detail tests and capture a 360x640/font-scale-1.3/
Effects-Off baseline screenshot and semantics tree before adding the first
missing detail-reading boundary.
