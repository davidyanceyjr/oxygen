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
- Cycle history records the UI screenshot feedback workflow as established; the current implementation cycle must confirm the current-tree screenshot capture helper or restore the committed workflow before relying on it for evidence.
- Existing provider-backed current/hourly/daily/metrics/sun/source/stale Home presentation is the functional baseline.
- The current installed app still uses the pre-18A Home interaction and provides the screenshot baseline for this cycle.
- Current Home production UI is a vertically scrolling dashboard whose existing section content must remain reachable after the paging foundation is introduced.

Selected behavior:
- Introduce semantic Standard Home page identities equivalent to Now, Hourly, Daily, and Details.
- Replace the one continuous Home page-level vertical dashboard with a semantic page container.
- Distribute existing Home sections among those page roles without substantial visual redesign.
- Support horizontal previous/next paging.
- Support deliberate touch advancement from appropriate non-interactive regions if compatible with child interaction correctness.
- Provide visible page-state indication.
- Allow practical direct page selection from that visible navigation mechanism.
- Expose accessible current-page and next/previous semantics/actions.
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
- Interactive child controls remain usable.
- Page-navigation hit areas are deliberate.
- Large text must not be silently clipped.
- Long location context must remain reachable/readable.
- No unintended horizontal content overflow separate from deliberate paging.
- Effects cannot be required to understand page state.
- TalkBack or equivalent semantic inspection can determine page identity and navigation actions.
- Foldable and physical-device behavior are out of scope.

Acceptance boundary:
- Slice 18A is complete when production Home exposes semantic Standard pages equivalent to Now, Hourly, Daily, and Details; users can move forward and backward through those pages without traversing one long vertical dashboard; visible page state and practical direct page selection exist; deliberate non-interactive touch advancement works if adopted without stealing child input; accessibility exposes page identity and next/previous navigation; all existing provider-backed Home current/hourly/daily/metrics/sun/source/stale/refresh/retry information remains reachable; production provider/repository/persistence behavior is unchanged; no production sample weather appears; and installed-app screenshots plus focused tests prove the new interaction structure.

Evidence plan:
- Save artifacts under `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/`.
- Retain at minimum these planned screenshot filenames in that artifact directory:
  - `home-baseline.png`
  - `home-now-foundation.png`
  - `home-hourly-foundation.png`
  - `home-daily-foundation.png`
  - `home-details-foundation.png`
- Also retain focused Compose/state test logs, compile/build logs, page-navigation semantics evidence, broad verification logs, and screenshot review notes when required by the UI workflow.
- Artifact payloads remain governed by `.codex/test-artifacts/README.md` and are not committed unless repository rules change.
- Do not fabricate screenshot or emulator evidence during planning; baseline screenshot capture belongs to the future 18A implementation cycle.

## Implementation Plan

### Phase 0 - Baseline and contract

- Inspect current production Home composition and presentation models.
- Inspect the current UI workflow documentation/history and confirm the current-tree screenshot capture helper before relying on it.
- Inspect relevant Home Compose/state tests.
- Run appropriate focused baseline tests.
- Build/install the current application.
- Capture `home-baseline.png`.
- Record the observable current scrolling behavior.
- Identify the exact existing Home sections that will map to Now, Hourly, Daily, and Details.
- Do not perform visual redesign in this phase.

### Phase 1 - Semantic pager foundation

- Introduce the minimum semantic page model.
- Introduce the minimum page container/navigation state.
- Map existing Home content into the four semantic destinations.
- Remove the requirement to traverse one full vertical Home document.
- Preserve weather/state inputs unchanged.
- Add focused tests for page model/state/navigation.
- Do not substantially restyle the underlying content.

### Phase 2 - Interaction and accessibility

- Implement and verify horizontal page navigation.
- Implement and verify visible current-page indication.
- Implement and verify practical direct page selection.
- Implement appropriate tap-to-advance behavior only if it does not conflict with child controls.
- Verify child interaction isolation.
- Expose and verify accessible page identity.
- Expose and verify accessible next/previous navigation.
- Verify first/last page behavior.
- Install and capture all four page-foundation screenshots.
- If screenshots expose broken composition caused by the structural migration, make only the minimum layout correction needed for a usable foundation.
- Do not begin the 18B through 18E redesigns.

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
