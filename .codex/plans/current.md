# Active Cycle

Status: planned
Cycle ID: 2026-08-29-home-paged-interaction-foundation
Mode: feature
Goal: Replace the continuous scrolling Home dashboard with the Standard semantic Home page container and accessible touch/swipe page navigation while preserving existing provider-backed Home behavior.
Roadmap slice: Slice 18A: Home Paged Interaction Foundation.
Branch or work context: local `oxygen` Android scaffold after committed Slice 18 Offline Launch From Last Forecast.

Specification anchors:
- `AGENTS.md`
- `README.md`
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `docs/data-sources/PROVIDER_TEMPLATE.md`
- `.codex/plans/mvp-roadmap.md`
- `.codex/cycles/history.md`
- `.codex/test-artifacts/README.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `core/build.gradle.kts`
- `scripts/android-env.sh`
- `scripts/install-debug.sh`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/theme/`
- `app/src/main/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapper.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/`

Prerequisites:
- Slice 18 Offline Launch From Last Forecast is committed.
- Cycle history records the UI screenshot feedback workflow as established; before 18A behavior work begins, the implementation cycle must resolve the current-tree screenshot-helper state. Either commit `docs/UI_DEVELOPMENT_WORKFLOW.md` and `scripts/capture-screen.sh` as a prerequisite workflow commit, or remove helper dependence from this cycle and record direct `adb exec-out screencap -p` capture commands in evidence logs.
- Existing provider-backed current/hourly/daily/metrics/sun/source/stale Home presentation is the functional baseline.
- The current installed app still uses the pre-18A Home interaction and provides the screenshot baseline for this cycle.
- Current Home production UI is a vertically scrolling dashboard whose existing section content must remain reachable after the paging foundation is introduced.
- Installed-app screenshot evidence must come from a deterministic provider-backed Home state. It must not depend on live provider timing, arbitrary emulator state, `SampleWeather`, or scaffold preview paths. The deterministic path must clear app data, seed a selected location through the production DataStore selected-location boundary, seed a provider-neutral forecast through the production Room forecast-cache boundary, launch Home with a deterministic refresh failure/offline repository path, and capture the restored cached Home state.

Selected behavior:
- Introduce semantic Standard Home page identities equivalent to Now, Hourly, Daily, and Details.
- Replace the one continuous Home page-level vertical dashboard with a semantic page container.
- Distribute existing Home sections among those page roles without substantial visual redesign.
- Support horizontal previous/next paging.
- Support deliberate touch advancement from appropriate non-interactive regions only if compatible with child interaction correctness.
- Provide visible page-state indication.
- Provide practical direct page selection through a visible labeled page selector for `Now`, `Hourly`, `Daily`, and `Details`; each selector must have a stable test tag and accessible label.
- Expose accessible current-page and next/previous semantics/actions: current page name, position among pages such as `Page 1 of 4`, `Next page` where applicable, `Previous page` where applicable, and bounded first/last page behavior.
- Preserve interactive child controls, including refresh, retry, Settings/About, alerts, links, buttons, and any existing local forecast interactions.
- Preserve all existing Home weather/state functionality.
- Use the installed application as the real rendering boundary.
- Capture baseline and final screenshots.

18A succeeds when the interaction architecture is correct and usable. The pages do not have to look visually finished.

Functional invariants:
- No provider behavior changes.
- No repository behavior changes.
- No forecast persistence behavior changes.
- No selected-location persistence changes.
- No weather-value changes.
- No condition-semantic changes.
- No fabricated weather.
- No removal of refresh/retry.
- No loss of stale/source/provenance information.
- No return to production sample data.
- No unrelated navigation redesign.
- Accessibility of existing interactive controls must not regress.
- Existing information may be redistributed between semantic pages when it remains readily accessible through the normal Home interaction model.

Visual objectives:
- Make each semantic page visibly distinct enough for interaction testing.
- Make current page identity obvious.
- Make the page indicator/navigation understandable.
- Avoid obvious broken spacing after decomposing the scrolling dashboard.
- Ensure information redistribution does not create severely empty or unusably overloaded pages.
- Retain existing design language where possible rather than redesign all components.

Deferred visual work:
- Canonical Now hero polish belongs to Slice 18B.
- Hourly visual design belongs to Slice 18C.
- Daily visual design belongs to Slice 18D.
- Details visual design belongs to Slice 18E.
- Cross-page design-system consolidation belongs to Slice 18G.

Layout and accessibility constraints:
- Current single emulator workflow only.
- Ordinary Home navigation uses semantic pages.
- No requirement for page-level vertical traversal of the entire forecast.
- Local overflow remains permissible where accessibility/content correctness requires it.
- Page indicator remains visible and stable.
- At the ordinary baseline emulator portrait viewport with `fontScale = 1.0f`, Standard Home success pages must not depend on one page-level vertical scroll through the entire forecast.
- At compact `360dp` width and `fontScale = 1.3f`, local overflow is allowed when needed, but page identity, page selector, refresh/About controls, and representative page content must remain reachable without clipping or sibling overlap.
- Interactive child controls remain usable.
- Page-navigation hit areas are deliberate.
- Large text must not be silently clipped.
- Long location context must remain reachable/readable.
- No unintended horizontal content overflow separate from deliberate paging.
- Effects cannot be required to understand page state.
- TalkBack or equivalent semantic inspection can determine page identity and navigation actions.
- Foldable and physical-device behavior are out of scope.

Acceptance boundary:
- Slice 18A is complete when production Home exposes semantic Standard pages equivalent to Now, Hourly, Daily, and Details; users can move forward and backward through those pages without traversing one long vertical dashboard; visible page state and practical direct page selection exist; if tap-to-advance is implemented, it is verified not to steal child input, and if it is not implemented, visible controls plus swipe/direct selection satisfy navigation; accessibility exposes page identity and next/previous navigation; all existing provider-backed Home current/hourly/daily/metrics/sun/source/stale/refresh/retry information remains reachable; production provider/repository/persistence behavior is unchanged; no production sample weather appears; and deterministic installed-app screenshots plus focused production-boundary tests prove the new interaction structure.

Concrete Home section mapping for 18A:
- Now: location/context, current conditions, condition identity, high/low, feels-like, near-term precipitation when present, important stale/restored/refreshing status, refresh, and Settings/About.
- Hourly: hourly forecast progression and local hourly horizontal movement where it remains subordinate to Home page navigation.
- Daily: daily forecast rows and daily-attached sunrise/sunset information where present.
- Details: metrics, sun summary, source/update/provenance, provider disclosure, and privacy note.
- Alerts: existing alert summary remains immediately visible on Now for safety reachability. Longer alert-detail behavior is out of scope unless already present.
- Loading and no-cache error: remain non-paged operational surfaces in 18A unless the implementation naturally shares the container without changing behavior. They must preserve selected-location text, Settings/About, retry where applicable, provider disclosure, and existing loading/error copy. Full paged operational-state refinement belongs to Slice 18F.
- Before production edits, record a baseline Home content inventory in the cycle artifacts and plan evidence. The inventory must include current reachable Home tags/content for location, stale/restored/refreshing status, alerts, current conditions, near-term precipitation, hourly, daily, metrics, sun, source, provenance footer, refresh, Settings/About, loading, no-cache error, and retry, with each item mapped to Now, Hourly, Daily, Details, or an operational surface.

Evidence plan:
- Save artifacts under `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/`.
- Establish deterministic installed-app screenshot state before final screenshots by clearing app data, seeding selected location through `DataStoreSelectedLocationStorage`, seeding cached forecast through the production Room forecast-cache storage, forcing a deterministic refresh failure/offline repository result, and launching Home from the restored cache. Do not use `SampleWeather`, scaffold preview paths, live provider timing, or an arbitrary existing emulator state as screenshot proof.
- Retain at minimum these planned screenshot filenames in that artifact directory:
  - `home-baseline.png`: pre-18A installed Home dashboard state from the deterministic provider-backed setup.
  - `home-now-foundation.png`: Now page identity, selected location/current conditions, refresh/About, alerts when present, and page selector with Now selected.
  - `home-hourly-foundation.png`: Hourly page identity, hourly content, and page selector with Hourly selected.
  - `home-daily-foundation.png`: Daily page identity, daily rows and daily-attached sunrise/sunset information where present, and page selector with Daily selected.
  - `home-details-foundation.png`: Details page identity, metrics/source/provenance/privacy note, and page selector with Details selected.
- Also retain focused Compose/state test logs, compile/build logs, page-navigation semantics evidence, compact-width/large-font layout evidence, broad verification logs, and screenshot review notes when required by the UI workflow.
- Artifact payloads remain governed by `.codex/test-artifacts/README.md` and are not committed unless repository rules change.
- Do not fabricate screenshot or emulator evidence during planning; baseline screenshot capture belongs to the future 18A implementation cycle.

## Implementation Plan

### Phase 0 - Baseline and contract

- Inspect current production Home composition and presentation models.
- Inspect the current UI workflow documentation/history and resolve the screenshot helper state before relying on it: either commit `docs/UI_DEVELOPMENT_WORKFLOW.md` and `scripts/capture-screen.sh` as project workflow or remove helper dependence from the evidence path and use logged direct adb capture commands.
- Inspect relevant Home Compose/state tests.
- Run focused baseline tests before production UI edits and save logs:
  - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest*'`
  - `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest --tests '*HomeDashboardUiTest*'`
  - If connected-test filtering is unsupported, record the exact fallback connected-test command used.
- Build/install the current application.
- Establish deterministic installed-app setup for screenshot evidence through production DataStore selected-location storage and production Room forecast-cache storage, after clearing app data.
- Capture `home-baseline.png` from the current pre-18A installed Home state.
- Record the observable current scrolling behavior.
- Confirm the concrete Home section mapping above against current production tags/content and save the baseline inventory artifact.
- Do not perform visual redesign in this phase.

### Phase 1 - Semantic pager foundation

- Introduce the minimum semantic page model.
- Introduce the minimum page container/navigation state.
- Map existing Home content into the four semantic destinations.
- Remove the requirement to traverse one full vertical Home document.
- Preserve weather/state inputs unchanged.
- Add focused tests for page model/state/navigation.
- Add production Compose or app-boundary tests that render the actual Home surface and prove page labels/identities exist, the labeled page selector changes visible content, previous/next navigation changes page, first/last page behavior is bounded, accessibility semantics expose page name/position/navigation actions, and refresh/About callbacks remain isolated and callable.
- Do not substantially restyle the underlying content.

### Phase 2 - Interaction and accessibility

- Implement and verify horizontal page navigation.
- Implement and verify visible current-page indication.
- Implement and verify a visible labeled selector for `Now`, `Hourly`, `Daily`, and `Details`, with stable test tags such as `home-page-tab-now`, `home-page-tab-hourly`, `home-page-tab-daily`, and `home-page-tab-details`.
- Implement tap-to-advance behavior only if it can be verified without conflicting with child controls; otherwise leave it out and rely on visible controls plus swipe/direct selection.
- Verify child interaction isolation.
- Verify the Hourly page's local horizontal forecast movement does not accidentally trigger Home page navigation, and that Home page previous/next controls still work with the hourly row present.
- Expose and verify accessible page identity using page name and position text/semantics.
- Expose and verify accessible next/previous navigation actions or equivalent accessible controls with `Next page` and `Previous page` labels, absent or disabled at the appropriate ends.
- Verify first/last page behavior.
- Install and capture all four page-foundation screenshots.
- If screenshots expose broken composition caused by the structural migration, make only the minimum layout correction needed for a usable foundation.
- Do not begin the 18B through 18E redesigns.

### Phase 3 - Verification and commit readiness

- Verify all existing Home information remains reachable.
- Verify refresh/retry behavior remains reachable.
- Verify stale/source/provenance information remains reachable.
- Verify loading and no-cache error operational surfaces preserve existing selected-location, Settings/About, retry, disclosure, and copy behavior.
- Verify existing provider/persistence behavior remains unchanged.
- Verify no production sample data appears.
- Verify no obvious clipping/overlap is introduced.
- Verify compact layout at `360dp` width and `fontScale = 1.3f`; page indicator, current page content, refresh/About controls, and representative Hourly/Daily/Details content must have positive bounds and no sibling overlap.
- Verify ordinary baseline emulator portrait layout with `fontScale = 1.0f` does not require one page-level vertical scroll through the entire forecast to navigate Home success content.
- Verify page navigation works in the installed application.
- Verify screenshots against the filename-specific criteria in the evidence plan.
- Verify focused tests pass.
- Verify broad repository checks pass.
- Review the final diff for accidental Now/Hourly/Daily/Details redesign work.
- Only then mark the cycle verified/ready according to repository vocabulary.

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
- Simple, Detailed, or Meteorologist layouts.
- Elaborate charts.
- Elaborate weather effects.
- Theme catalog work.
- Foldable work.
- Physical-device work.
- Multi-device ADB handling.
- Golden screenshot frameworks.
- Broad unrelated refactoring.

When work belongs to 18B through 18H, record it for that specified slice rather than absorbing it into 18A.

## Phase Results

- planned: Selected Slice 18A only.
- specified: Roadmap Slices 18A through 18H define the bounded Standard Home interaction, visual baseline, design-system consolidation, and verification gate sequence.
- specified: Authoritative Home specification now defines semantic, viewport-oriented Home pages as the normal interaction model and reserves vertical scrolling for long-form, list, legal/settings, or accessibility/content overflow cases.
- covered: Focused Home state-holder baseline and final tests passed. App connected instrumentation now covers semantic page labels, direct page selector tags, previous/next bounded controls, horizontal swipe, compact large-font reachability across Now/Hourly/Daily/Details, and retained refresh/About child controls.
- implemented: Production Home success state now uses a Standard semantic Home page container with `Now`, `Hourly`, `Daily`, and `Details`, while loading and no-cache error remain operational surfaces. Existing current, stale, alert, precipitation, hourly, daily, metrics, sun, source, disclosure, refresh, and Settings/About content is redistributed according to the 18A mapping without provider/repository/persistence changes.
- verified: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed; `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed; `git diff --check` passed; clean full `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest` passed on `oxygen_starter(AVD) - 17`.
- verified: Deterministic installed-app screenshot state was seeded through `DataStoreSelectedLocationStorage` and `RoomForecastCacheStorageFactory.create(context)` by `OfflineLaunchPersistenceInstrumentedTest.seedDeterministicInstalledHomeScreenshotState`; network services were disabled before launching `com.oxygen.weather/.MainActivity`; final screenshots were captured with direct `adb exec-out screencap -p`.
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/home-content-inventory.md`
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/home-now-foundation.png`
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/home-hourly-foundation.png`
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/home-daily-foundation.png`
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/home-details-foundation.png`
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/installed-hourly-uiautomator.xml`
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/installed-daily-uiautomator.xml`
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/installed-details-uiautomator.xml`
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/focused-home-state-holder.log`
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/focused-app-connected-debug-android-test-r5.log`
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/final-compile-debug-kotlin.log`
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/final-debug-unit-tests.log`
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/final-assemble-debug.log`
- evidence: `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/final-git-diff-check.log`
- not-run: `home-baseline.png` pre-18A installed dashboard screenshot was not captured before production UI edits, so it is not claimed as evidence. The initial filtered connected-test command was attempted and failed because `connectedDebugAndroidTest` does not support `--tests`; the fallback full connected command initially failed with no connected devices, then passed after starting `oxygen_starter`.
- review: Final diff is limited to Home ready-state paging, Home UI instrumentation coverage, deterministic installed screenshot seeding coverage, and this cycle evidence update. The untracked `docs/UI_DEVELOPMENT_WORKFLOW.md` and `scripts/capture-screen.sh` files were left uncommitted and not used for screenshot evidence.
