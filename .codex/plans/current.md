# Active Cycle

Status: committed
Cycle ID: 2026-09-02-mobile-one-handed-home-ergonomics
Mode: feature
Goal: Plan Slice 18I, Mobile One-Handed Home Ergonomics, as a bounded UI
adjustment slice before saved-location persistence. This cycle applies the
installed-emulator review recommendations for thumb-reachable controls,
first-run/location recovery, Home content clearance, stale-status visual weight,
and provenance placement without changing weather providers, persistence
semantics, saved-location behavior, or forecast data.
Roadmap context: Slice 18I, Mobile One-Handed Home Ergonomics. Slice 18H is
committed at `4f5f383`; Slice 19A saved-location storage/startup resolution is
deferred until this UI ergonomics slice is ready or explicitly superseded.

## Contract

Selected Behavior:
- Keep primary Home navigation and recurring Home actions reachable from the
  bottom thumb zone on handheld phones.
- Keep first-run/manual location entry and change-location recovery optimized
  for one-handed use by placing primary/secondary actions in a bottom-aligned
  action area while preserving manual search as the no-permission path.
- Keep About/disclosure recovery reachable through a bottom-aligned Back action.
- Ensure scrollable Home content has enough bottom clearance that final visible
  cards/text are not hidden behind the bottom navigation/actions.
- Reduce routine cached/stale/source/provenance blocks to appropriate visual
  weight: operational stale/failure state remains visible, fresh provenance is
  tertiary, and full provider/privacy/license explanation remains in About.
- Preserve accessibility: adequate touch targets, logical TalkBack order,
  meaningful labels, large-font readability, and no overlap on narrow screens.

Acceptance Boundary:
- On a clean first-run launch, the location screen shows the manual search field
  and privacy/provider disclosure, with Search, Use my location, and
  Settings/About reachable near the lower thumb zone without requiring a top
  reach. Manual search still does not request location permission.
- When opening Location from Home, Back is reachable from the lower thumb zone
  and returns to the previous Home without starting a new search or forecast
  request.
- Home Now, Hourly, Daily, and Details retain bottom page tabs and lower
  Location/Refresh/Settings actions with at least 48dp touch targets.
- Home page scroll containers leave enough bottom padding for the last visible
  card or disclosure text to clear the footer; no first-viewport content is
  obscured by the footer on the installed emulator.
- Now shows current weather as the dominant useful signal. Cached/restored or
  refresh-failed state remains visible but does not consume more first-viewport
  visual priority than current weather unless no current forecast is available.
- Details presents weather metrics before routine source/update details when
  data is otherwise usable. Source/update/provenance remains reachable from
  Details and fully explained in About.
- About overview and About detail surfaces keep their Back/recovery action
  reachable near the lower thumb zone while preserving legal/privacy text
  readability through vertical scrolling where needed.
- Focused Compose tests cover bottom action placement/reachability, footer
  clearance, stale-status weighting, Details provenance ordering, About Back
  reachability, and large-font/narrow-screen non-overlap.
- Installed-app screenshots/hierarchies cover first-run Location, Home Now,
  Hourly, Daily, Details, About overview, and one About detail page on
  `oxygen_starter`.

Out of Scope:
- Saved-location storage/list/select/remove behavior, Room/DataStore schema
  changes, legacy selected-location migration, and in-flight saved-location
  switching isolation.
- Device-location lookup beyond the existing explicit Use my location request
  affordance.
- Unit preferences, official alerts, air quality, radar/maps, widgets,
  background refresh, persisted appearance/effects/layout settings,
  installed-app MET Norway fallback wiring, provider behavior changes,
  Gradle/dependency upgrades, release-readiness, MVP-readiness, and
  sample-weather production fallback.
- Moving all provenance only to About. Operational source/update/stale context
  must remain reachable from Home.

## Implementation Plan

1. Discover and baseline:
   - Re-read specification sections 30, 31, 34, 37, and 53; roadmap UI rule,
     Slice 18H, Slice 18I, and Slice 19; current Home/Location/About
     Composables; and focused Home instrumentation tests.
   - Record `git status --short` before production edits.
   - Run focused baseline tests where feasible:
     `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*'`.
   - Use existing review artifacts under
     `.codex/test-artifacts/2026-09-02-mobile-ui-ergonomics-review/` as
     baseline observations, not implementation evidence.

2. Cover intended UI behavior before production edits:
   - Add or adjust Compose instrumentation tests for first-run bottom action
     reachability, change-location Back reachability, Home footer touch bounds,
     Home footer/content clearance, Details metrics-before-routine-provenance
     ordering, About bottom Back reachability, and large-font/narrow-screen
     non-overlap.
   - Keep tests focused on observable UI bounds/order/semantics rather than
     symbol existence.

3. Build:
   - Introduce app-local bottom action layout primitives only if they remove
     duplication between first-run Location and About surfaces.
   - Update first-run/change-location layout so actions are bottom-aligned on a
     handheld viewport while search/disclosure content remains readable and
     scroll-safe.
   - Update About surfaces so Back/recovery remains bottom-reachable while long
     legal/privacy/provider text remains scrollable.
   - Add bottom clearance to Home page content containers so footer overlap is
     avoided.
   - Rebalance Now stale-status placement/weight and Details source/update
     placement without dropping source/update/provenance from Home.

4. Exercise and verify:
   - Real path: clean app data -> launch first-run Location -> capture; perform
     manual search/select real geocoding result -> Home Now -> navigate Hourly,
     Daily, Details -> Location Back -> About overview/detail -> Back.
   - Confirm on-device that primary actions are lower-screen reachable and that
     Home content is not obscured by the footer.
   - Run broad checks: `:app:compileDebugKotlin`,
     `:app:testDebugUnitTest :core:testDebugUnitTest`, `:app:assembleDebug`,
     and `git diff --check`.
   - Save evidence under
     `.codex/test-artifacts/2026-09-02-mobile-one-handed-home-ergonomics/`.

5. Review:
   - Check for drift: no persistence/storage/schema work, no saved-location
     management behavior, no provider or forecast mapping changes, no unit or
     permission-flow expansion, no all-provenance-to-About move, no appearance
     persistence, no sample fallback, and no readiness claims.
   - Update phase results and cycle history only with evidence actually run.

## Phase Results

- specified: Slice 18I is specified in `.codex/plans/mvp-roadmap.md` as Mobile
  One-Handed Home Ergonomics, based on the installed-emulator UI review
  artifacts from
  `.codex/test-artifacts/2026-09-02-mobile-ui-ergonomics-review/`.
- planned: This document selects Slice 18I only: bottom-reachable
  first-run/location/About actions, Home footer/content clearance, stale-status
  visual weighting, Details provenance ordering, and accessibility/large-font
  preservation.
- covered: Focused Compose instrumentation coverage added in
  `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`
  for first-run bottom actions, change-location Back reachability/no-refresh
  return, About overview/detail bottom Back reachability, Now stale-status
  ordering after current weather, Details metrics/source/status ordering,
  footer touch bounds, and compact/large-font non-overlap.
- implemented: Production UI changes are limited to
  `app/src/main/kotlin/com/oxygen/weather/app/ui/firstrun/FirstRunLocationEntryScreen.kt`,
  `app/src/main/kotlin/com/oxygen/weather/app/ui/about/AboutScreen.kt`, and
  `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`.
  First-run/change-location content now scrolls above bottom-aligned actions;
  About content scrolls above a bottom Back action; Now stale/refresh status
  follows the current-weather hero; Details presents metrics and source before
  stale status; scrollable Home pages get footer clearance while compact Daily
  retains its established fixed composition.
- verified: Passed baseline focused unit test
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*'`.
  Focused Home Compose instrumentation passed 24 tests on
  `oxygen_starter(AVD) - 17` with `. scripts/android-env.sh && ./gradlew
  :app:connectedDebugAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`.
  Installed-app real-path evidence used `scripts/list-avds.sh`,
  `scripts/start-emulator.sh`, and `scripts/install-debug.sh`, then clean app
  data, first-run capture, manual Open-Meteo geocoding search/select for
  Madison, Home Now/Hourly/Daily/Details captures, change-location Back
  capture/return, About overview capture, and Privacy detail capture.
  Broad checks passed: `. scripts/android-env.sh && ./gradlew
  :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh &&
  ./gradlew :app:assembleDebug`; `git diff --check`.
- artifacts: `.codex/test-artifacts/2026-09-02-mobile-one-handed-home-ergonomics/`
  contains focused and broad logs plus installed screenshots/hierarchies:
  `installed-first-run-location`, `installed-location-results`,
  `installed-home-now`, `installed-home-hourly`, `installed-home-daily`,
  `installed-home-details`, `installed-change-location`,
  `installed-after-location-back`, `installed-about-overview`, and
  `installed-about-privacy`.
- deferred: Slice 19A saved-location storage/startup resolution is deferred
  until this UI ergonomics slice is ready or explicitly superseded.
- skipped: No saved-location, storage/schema, provider, unit, alert, air
  quality, radar, widget, background refresh, persisted appearance, MET Norway
  fallback, release, or MVP readiness work.
- committed: Slice 18I is committed in this changeset. Next implementation
  candidate returns to Slice 19: Saved Locations Persistence.
