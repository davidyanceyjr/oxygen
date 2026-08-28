UI Design Findings

1. Home does not yet match the specified visual/interaction model.

The spec calls for a current-condition hero, horizontal hourly forecast, weather marks, and a dashboard hierarchy. Current Home is a single vertical stack of generic cards, and hourly rows are rendered vertically inside one card. This is the biggest UI design gap for the next Home slice.

References:
- docs/OXYGEN_FULL_SPECIFICATION.md:929
- app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt:166
- app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt:192

2. Explicit refresh UI is not present for fresh Home data.

Roadmap Slice 11A requires pull-to-refresh or a visible refresh control. Current Home exposes retry only in error/stale paths, while fresh success has no visible refresh action.

References:
- .codex/plans/mvp-roadmap.md:227
- app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt:140

3. Appearance settings are modeled but not reachable or persisted.

OxygenAppearance defines theme/layout/effects/icon-pack choices, but production app state only keeps a remembered, non-persisted themeId and no UI control reaches it. This leaves Slices 26-29 as real UI work, not just settings plumbing.

References:
- app/src/main/kotlin/com/oxygen/weather/app/ui/theme/OxygenAppearance.kt:3
- app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt:23

4. The procedural weather scene and weather mark are not integrated into production Home.

The spec's core experience is "weather as artwork" with readable data independent of decoration. WeatherScene and WeatherConditionMark exist, but Home does not use them. The current default screen therefore reads more like a functional data list than Oxygen's intended identity.

References:
- docs/OXYGEN_FULL_SPECIFICATION.md:40
- app/src/main/kotlin/com/oxygen/weather/app/ui/weather/WeatherScene.kt:16
- app/src/main/kotlin/com/oxygen/weather/app/ui/components/WeatherConditionMark.kt:14

5. Layout stability and large-font risk are not yet designed through.

Forecast rows use a weighted text column plus an unconstrained trailing value, and metric rows are two flexible columns with no minimum/stable sizing. That is likely to become brittle with long provider names, converted units, large font, and narrow screens, all explicitly called out by the roadmap.

References:
- app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt:267
- app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt:288
- .codex/plans/mvp-roadmap.md:26

6. Accessibility evidence is mostly state/text level, not Compose/UI level.

State-holder tests cover visible text and mapping, but there is no app/src/androidTest tree and no Compose semantics/layout test coverage found. The spec requires meaningful semantics, TalkBack order, large font, RTL, touch targets, reduced motion, and high contrast. This should become a gate before claiming presentation slices verified.

References:
- docs/OXYGEN_FULL_SPECIFICATION.md:1146
- docs/OXYGEN_FULL_SPECIFICATION.md:1388

7. Units are hardcoded/mixed in presentation.

Temperature is always formatted as Fahrenheit, while wind is km/h and precipitation remains mm. That is acceptable as current scaffold behavior, but it is a UI design dependency for Slice 20 because every forecast row/card needs to survive unit changes.

References:
- app/src/main/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapper.kt:155
- app/src/main/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapper.kt:183

Roadmap Gap Summary

The roadmap has the right major UI slices: Home success, explicit refresh, saved locations, units, effects, layout density, theme selection, high contrast, accessibility verification, alerts, and release gates. The main gap is sequencing pressure: Home's current implementation is so generic that appearance, accessibility, refresh, and saved-location navigation will all touch the same screen. Treat the next UI design work as a dedicated Home component extraction slice: WeatherHero, horizontal hourly strip, daily row, metric card, source/stale banner, and accessibility semantics, with screenshots or Compose tests for narrow and large-font states.

Review Evidence

Reviewed with git status --short, rg, sed, and nl. Gradle tests were not run because this was a no-change design review before writing this findings file.
