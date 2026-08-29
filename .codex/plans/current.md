# Active Cycle

Status: planned
Cycle ID: 2026-08-29-home-paged-interaction-foundation
Mode: feature
Goal: Replace the continuous scrolling Home dashboard with the Standard semantic Home page container and accessible touch/swipe page navigation while preserving existing provider-backed Home behavior.
Roadmap slice: Slice 18A: Home Paged Interaction Foundation.
Branch or work context: local `oxygen` Android scaffold after committed Slice 18 offline launch and committed UI screenshot-development workflow.

Specification anchors:
- `AGENTS.md`
- `README.md`
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `docs/UI_DEVELOPMENT_WORKFLOW.md`
- `docs/data-sources/PROVIDER_TEMPLATE.md`
- `.codex/plans/mvp-roadmap.md`
- `.codex/plans/current.md`
- `.codex/cycles/history.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `core/build.gradle.kts`
- `scripts/android-env.sh`
- `scripts/install-debug.sh`
- `scripts/capture-screen.sh`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeScreen.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/theme/OxygenTheme.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/theme/OxygenAppearance.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapper.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastStateHolderTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`

Prerequisites:
- Slice 18 Offline Launch From Last Forecast is committed.
- The screenshot feedback workflow is established and committed in `docs/UI_DEVELOPMENT_WORKFLOW.md`, `AGENTS.md`, and `scripts/capture-screen.sh`.
- Existing provider-backed current/hourly/daily/metrics/sun/source/stale Home presentation is the functional baseline.
- The current installed app still uses the pre-18A Home interaction and provides the screenshot baseline for this cycle.
- Slice 18A depends on preserving the provider-backed Home behavior already established by Slices 11, 17, 17A, 17B, 17C, the Persistence Architecture Gate, and Slice 18.

Planning summary:
- Slice 18A establishes the Standard semantic Home page container and navigation model only.
- Standard Home page identities are semantic concepts equivalent to Now, Hourly, Daily, and Details. They are not unexplained numeric indexes and are not a mandate for other future layout presets.
- The existing scrolling Home dashboard is the baseline content source. 18A redistributes existing Home sections among page roles without substantial visual redesign.
- Ordinary Home navigation should no longer require page-level vertical traversal of the whole forecast. Local overflow remains permissible when accessibility or exceptional content length requires it.
- The installed application is the real rendering boundary for screenshot evidence.
- Canonical Now, Hourly, Daily, Details visual design remains specified for Slices 18B through 18E, not planned in this cycle.

Selected behavior:
- Introduce semantic Standard Home page identities equivalent to Now, Hourly, Daily, and Details.
- Replace the one continuous Home page-level vertical dashboard with a semantic page container.
- Distribute existing Home sections among those page roles without substantial visual redesign.
- Support horizontal previous/next paging.
- Support deliberate touch advancement from appropriate non-interactive regions if compatible with child interaction correctness.
- Provide visible page-state indication.
- Allow practical direct page selection from that visible navigation mechanism.
- Expose accessible current-page and next/previous semantics/actions.
- Preserve interactive child controls, including refresh, retry, settings/about, saved-location controls if present, alert controls, chart interactions, links, and buttons.
- Preserve all existing Home weather/state functionality.
- Capture baseline and final installed-app screenshots.

Functional invariants:
- No provider behavior changes.
- No repository behavior changes.
- No forecast persistence behavior changes.
- No selected-location persistence changes.
- No weather-value changes.
- No condition-semantic changes.
- No fabricated weather.
- No removal of refresh or retry.
- No loss of stale/source/provenance information.
- No return to production sample data.
- No unrelated navigation redesign.
- Accessibility of existing interactive controls must not regress.
- Provider-neutral forecast meaning, selected location identity, current/hourly/daily values, condition identity, refresh behavior, retry behavior, stale/offline behavior, source/provenance accessibility, Slice 18 selected-location and forecast persistence, provider/repository/domain boundaries, first-run/manual selection behavior, unrelated navigation, absence of `SampleWeather` from production Home success, and existing provider fallback behavior remain intact.

Existing information may be redistributed between semantic pages. Moving information is not dropping it if the information remains readily accessible through the normal Home interaction model.

Visual objectives:
- Make each semantic page visibly distinct enough for interaction testing.
- Make current page identity obvious.
- Make the page indicator/navigation understandable.
- Avoid obvious broken spacing after decomposing the scrolling dashboard.
- Ensure information redistribution does not create severely empty or unusably overloaded pages.
- Retain existing design language where possible rather than redesign all components.

Explicit visual deferrals:
- Canonical Now hero polish is Slice 18B.
- Hourly visual design is Slice 18C.
- Daily visual design is Slice 18D.
- Details visual design is Slice 18E.
- Cross-page design-system consolidation is Slice 18G.

Layout and accessibility constraints:
- Current single emulator workflow only.
- Ordinary Home navigation uses semantic pages.
- No requirement for page-level vertical traversal of the entire forecast.
- Local overflow remains permissible where accessibility/content correctness requires it.
- Page indicator remains visible and stable.
- Interactive child controls remain usable.
- Page-navigation hit areas are deliberate.
- Large text must not be silently clipped.
- Long location context must remain reachable/readable.
- No unintended horizontal content overflow separate from deliberate paging.
- Effects cannot be required to understand page state.
- TalkBack or equivalent semantic inspection can determine page identity and navigation actions.
- Foldable and physical-device behavior are out of scope.

Evidence plan:
- Save artifacts under `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/`.
- Retain installed-app screenshots, with exact names allowed to vary by repository convention:
  - `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/home-baseline.png`
  - `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/home-now-foundation.png`
  - `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/home-hourly-foundation.png`
  - `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/home-daily-foundation.png`
  - `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/home-details-foundation.png`
- Retain focused Compose/state test logs, compile/build logs, page-navigation semantics evidence, broad verification logs, and screenshot review notes if required by `docs/UI_DEVELOPMENT_WORKFLOW.md`.
- Do not fabricate screenshot or emulator evidence during planning. Baseline screenshot capture belongs to the future 18A implementation cycle.

## Implementation Plan

### Phase 0 - Baseline and contract

- Inspect current production Home composition and presentation models.
- Inspect current UI workflow documentation.
- Inspect relevant Home Compose/state tests.
- Run appropriate focused baseline tests.
- Build/install the current application.
- Capture `home-baseline.png`.
- Record the observable current scrolling behavior.
- Identify the exact existing Home sections that will map to Now, Hourly, Daily, and Details.

Do not perform visual redesign in this phase.

### Phase 1 - Semantic pager foundation

- Introduce the minimum semantic page model.
- Introduce the minimum page container/navigation state.
- Map existing Home content into the four semantic destinations.
- Remove the requirement to traverse one full vertical Home document.
- Preserve weather/state inputs unchanged.
- Add focused tests for page model/state/navigation.

Do not substantially restyle the underlying content.

### Phase 2 - Interaction and accessibility

- Implement and verify horizontal page navigation.
- Implement and verify visible current-page indication.
- Implement and verify practical direct page selection.
- Implement appropriate tap-to-advance behavior only if it does not conflict with child controls.
- Verify child interaction isolation.
- Expose accessible page identity.
- Expose accessible next/previous navigation.
- Verify first/last page behavior.
- Install and capture all four page-foundation screenshots.

If screenshots expose broken composition caused by the structural migration, make only the minimum layout correction needed for a usable foundation. Do not begin the 18B-E redesigns.

### Phase 3 - Verification and commit readiness

- Verify all existing Home information remains reachable.
- Verify refresh/retry behavior remains reachable.
- Verify stale/source/provenance information remains reachable.
- Verify existing provider/persistence behavior remains unchanged.
- Verify no production sample data appears.
- Verify no obvious clipping/overlap is introduced.
- Verify page navigation works in the installed application.
- Verify screenshots prove the new structure.
- Verify focused tests pass.
- Verify broad repository checks pass.
- Review the final diff for accidental Now/Hourly/Daily/Details redesign work.

Only then mark the cycle verified/ready according to repository vocabulary.

Broad verification commands planned:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Explicitly out of scope:
- Slice 18B Now page redesign.
- Slice 18C Hourly redesign.
- Slice 18D Daily redesign.
- Slice 18E Details redesign.
- Slice 18F operational-state visual consolidation beyond what is necessary to preserve existing functionality.
- Slice 18G design-system cleanup beyond minimal needs of the pager foundation.
- Slice 18H final accessibility/visual gate.
- Saved Locations.
- Units.
- Device location.
- Alerts-provider work.
- New providers.
- Persisted appearance settings.
- Simple/Detailed/Meteorologist layouts.
- Elaborate charts.
- Elaborate weather effects.
- Theme catalog work.
- Foldable work.
- Physical-device work.
- Multi-device ADB handling.
- Golden screenshot frameworks.
- Broad unrelated refactoring.

When work belongs to 18B-H, record it for that specified slice rather than absorbing it into 18A.

## Phase Results

- planned: Selected Slice 18A after committed Slice 18 offline launch and committed UI screenshot-development workflow.
- specified: The authoritative specification and roadmap now define Standard Home as semantic viewport-oriented pages equivalent to Now, Hourly, Daily, and Details, with discrete page navigation, visible page state, accessible next/previous navigation, child-control isolation, and scrolling reserved as fallback for long content or accessibility/content pressure.
- not implemented: This planning cycle intentionally does not implement Slice 18A or modify production Kotlin/Compose/test behavior.
