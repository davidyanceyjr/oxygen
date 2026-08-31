# Active Cycle

Status: planned
Cycle ID: 2026-08-31-daily-page-visual-baseline
Mode: feature
Goal: Establish the canonical Oxygen Daily page as a dedicated, scannable multi-day forecast comparison experience.
Roadmap context: Slice 18D: Daily Page Visual Baseline, after committed Slice 18C Hourly Page Visual Baseline and before Slice 18E Details Page Visual Baseline.
Branch or work context: local `main` is aligned with `origin/main` at merge commit `6f243c3`; existing uncommitted Markdown status-sync edits in `README.md`, `.codex/plans/mvp-roadmap.md`, and `.codex/cycles/history.md` are user-owned and must be preserved unless explicitly selected later. `.codex/plans/current.md` is the active-cycle planning edit for Slice 18D.

## Contract

Selected behavior:
- Redesign only the Standard Home Daily page composition so provider-backed daily forecasts can be compared quickly across multiple days.
- Use `docs/assets/oxygen-weather-visual-language-base-art-sheet-v0.1.png` as visual-direction authority for Daily surface treatment, compact weather marks, strong numerals, and calm editorial density where it supports comparison.
- Communicate each day's date/day label, provider-neutral condition identity, high/low temperatures, available precipitation, and an explicit precipitation-unavailable state when provider data is absent.
- Give high/low temperatures a clear comparative structure with stable density. `HomeDailyPresentation` must carry nullable semantic high/low numeric values alongside existing formatted high/low display text so any comparison layout or later range visualization does not parse display strings.
- If Daily uses `WeatherConditionMark` or another condition visual, expose provider-neutral condition identity through `HomeDailyPresentation`; do not derive it by parsing formatted condition text.
- Preserve null honesty: unavailable high, low, precipitation, sunrise, or sunset values render as unavailable/omitted states, not fabricated zeroes or guessed values.
- Do not repeat verbose sunrise/sunset prose on every Daily row at normal density. Daily may include one compact sun summary or affordance only if four-day comparison density remains intact; complete sun information remains available on Details.
- Keep the Daily page a viewport-oriented comparison surface at ordinary supported phone sizes, not another long vertically scrolling forecast document.
- Keep large-font/accessibility fallback complete and readable, even if localized scrolling is needed for overflow.
- Preserve direct Home page navigation, horizontal swipe navigation, page identity, page position, refresh behavior, About navigation, loading, no-cache error, stale, refresh-in-progress surfaces, and source/provenance reachability.
- Preserve child-interaction isolation so any Daily-local controls or gestures do not accidentally advance the Home page.
- Keep effects-off or decoration-light presentation complete: required daily semantics must remain visible as text or stable symbols.
- Treat the Base Art Sheet v0.1 as a design reference, not an app runtime bitmap asset, theme-engine implementation, or permission to weaken accessibility/contrast requirements.
- Do not parse formatted display strings back into numbers in Composables.

Acceptance boundary:
- Production Home Daily page renders provider-neutral presentation state only; no provider DTOs, provider-specific errors, hidden default location, fabricated weather values, or `SampleWeather.bundle` production path is introduced.
- Existing Now, Hourly, and Details pages remain functionally reachable through the Slice 18A page model, but substantial visual redesign of those pages is out of scope.
- Existing selected-location persistence, Room forecast cache, offline restored stale Home, failed-refresh stale retention, live-success cache-failure display, source/provenance text, About navigation, refresh/retry actions, and Home page navigation behavior remain observable.
- At ordinary supported phone size, the first visible Daily viewport intentionally shows multiple chronological daily entries with condition identity, comparative high/low structure, and precipitation where available.
- At `360dp x 640dp` with `fontScale = 1.0`, the first visible Daily viewport intentionally shows at least four chronological days using stable row/card dimensions, including varied condition identity, differing high/low ranges, one real provider-backed precipitation probability, and one explicit precipitation-unavailable state. Passing this by accidental clipping, pre-scrolled state, or hidden semantics is not acceptable.
- `HomeDailyPresentation` carries nullable semantic high/low numeric fields alongside existing formatted display text; focused tests prove they are copied from `DailyForecast.highC` and `DailyForecast.lowC` and preserve nulls.
- If a range bar is introduced, focused Compose tests prove it is driven by numeric semantic fields and still exposes the formatted high/low text.
- Deterministic Daily test and installed-app screenshot data include enough chronological daily entries to prove visual density, ordering, varied condition identity, high/low comparison, present precipitation, and unavailable precipitation behavior.
- Daily visual hierarchy and density are verified at an Android/Compose boundary with deterministic forecast data.
- Required Compose/Android UI bounds checks cover the Daily page at compact width and large font with representative long location/status/source text.
- Installed-app Daily screenshot evidence is required for the post-change deterministic state: seed selected location through DataStore plus Room cached forecast using the existing deterministic Android test/seeding path, install the debug app, disable emulator/device network, launch `com.oxygen.weather/.MainActivity`, navigate to Daily, then save screenshot and hierarchy under `.codex/test-artifacts/2026-08-31-daily-page-visual-baseline/`. If emulator/device capture is blocked, record the exact command/output blocker and do not claim installed-app screenshot evidence for the cycle.
- `git diff --check` passes.
- Broad Android checks pass unless an explicit environment blocker is recorded with exact command output and the unverified status is not claimed.

Explicitly out of scope:
- Now page visual redesign.
- Hourly page visual redesign.
- Details page visual redesign.
- Full charting system or generic visualization framework.
- Theme engine implementation.
- Persisted appearance, layout, or effects settings.
- Unit preferences or unit conversion behavior.
- Saved-location list switching/removal.
- Official alert lookup.
- Air quality, radar, widgets, notifications, background refresh, or new provider behavior.
- Installed-app MET Norway fallback activation.
- Release-candidate, MVP-ready, beta, or release claims.

## Implementation Plan

1. discover
   - Review current Home Daily implementation, Home presentation mapper/state, existing page navigation model, Oxygen theme/components, and focused Home UI tests.
   - Inspect Slice 18A/18B/18C screenshot and hierarchy artifacts to understand current Daily baseline and shared Home chrome constraints.
   - Inspect `docs/assets/oxygen-weather-visual-language-base-art-sheet-v0.1.png` and the related specification/roadmap language before selecting Daily visual details.

2. contract
   - Add or adjust focused Compose/Android UI tests that encode Daily page identity, chronological multi-day ordering, visible condition identity, high/low comparative structure, precipitation present/unavailable behavior, `360dp x 640dp` compact density, large-font readability, and Home page navigation preservation.
   - Add presentation-state tests for provider-neutral Daily condition identity and nullable numeric high/low fields; these tests must prove fields are passed from domain state, preserve nulls, and are not parsed from display strings.
   - Include deterministic Daily fixture data with multiple chronological days, varied weather conditions, differing low/high values, at least one real precipitation probability, and at least one unavailable precipitation case.
   - If `WeatherConditionMark` or a range bar is used, add focused tests for its semantics and data source.
   - Keep assertions tied to observable UI semantics, bounds, actions, and data boundaries rather than implementation symbol existence.

3. design-if-needed
   - Use nullable numeric semantic high/low fields for temperature comparison while retaining formatted high/low display text for existing callers.
   - Prefer existing package structure and narrowly scoped components; introduce shared primitives only where actual reuse or complexity reduction is demonstrated.

4. red-or-baseline
   - Run existing focused Home UI/state tests before production edits and save the baseline log unless the emulator/test environment is blocked with exact command output. Add new failing Daily tests only after this baseline.
   - Capture or identify an installed-app Daily baseline from Slice 18A where practical; if blocked, record the blocker and use existing historical artifacts only as labeled baseline context.

5. build
   - Refactor the Daily page into a dedicated multi-day comparison composition using provider-neutral Home presentation state and stable dimensions that intentionally fit at least four days in the first `360dp x 640dp` viewport at `fontScale = 1.0`.
   - Preserve Home header/page chrome hierarchy established by Slices 18B and 18C without letting Daily become a verbose vertical list.
   - Keep weather values honest: omit or label unavailable fields instead of filling visual space with fabricated values.

6. focused-green
   - Run targeted Home state/presentation tests affected by Daily changes.
   - Run targeted Home Compose/connected UI tests for Daily visual baseline behavior and existing page navigation/refresh/retry preservation.
   - Save logs under `.codex/test-artifacts/2026-08-31-daily-page-visual-baseline/`.

7. real-path-exercise
   - Expand deterministic installed-app selected-location plus Room cached forecast seed data so it contains multiple chronological daily entries with varied condition identity, differing high/low ranges, present precipitation, unavailable precipitation, and sun data sufficient to prove Daily does not repeat verbose row-level sunrise/sunset prose.
   - Keep deterministic Daily seed data in test/seeding paths that exercise the existing DataStore selected-location plus Room cached-forecast restore path; do not add a fake production provider, hidden default location, or sample-weather production route for screenshot setup.
   - Install the debug app, disable emulator/device network, launch `com.oxygen.weather/.MainActivity` against the deterministic seeded selected location plus Room cached forecast state, and navigate to Daily.
   - Capture post-change installed-app Daily screenshot and hierarchy evidence at ordinary configuration under `.codex/test-artifacts/2026-08-31-daily-page-visual-baseline/`.
   - Capture required compact/large-font Daily hierarchy/bounds evidence through Compose/Android UI tests; installed-app compact/large-font screenshots are optional additional evidence and must be explicitly labeled if omitted.

8. broad-checks
   - `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
   - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
   - `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
   - `git diff --check`

9. review
   - Review production/test diffs for scope creep, provider/domain leakage, fabricated values, string-parsed numeric visuals, unrelated roadmap/docs churn, and accidental claims beyond Slice 18D.
   - Before any commit, run `git status --short` and confirm unrelated user-owned Markdown changes remain preserved unless the owner explicitly selects them.
   - Update this file with evidence and phase results only after commands are actually run.

## Evidence Plan

Focused evidence to collect:
- New Daily evidence: Home Compose/Android UI tests proving Daily page identity, multiple chronological days in the first ordinary viewport, visible or accessible condition identity, high/low comparative structure, real precipitation probability, explicit unavailable precipitation state, visual density, `360dp x 640dp` compact readability, large-font bounds/readability, and child-control isolation for any local Daily movement.
- New presentation evidence: presentation or state-holder tests proving any new Daily presentation contract fields are provider-neutral, honest about null/unavailable data, and not derived by parsing formatted display strings in Composables.
- Reused regression evidence: existing or updated focused tests proving Home page navigation, refresh/retry, loading/no-cache surfaces, stale/refresh-in-progress preservation, About navigation, and source/provenance reachability remain observable after the Daily redesign.

Real-path evidence to collect:
- Historical or pre-change Daily baseline artifact, explicitly labeled.
- Post-change installed-app Daily screenshot for a deterministic seeded selected-location plus Room cached forecast state. Live provider screenshot evidence is fallback only and must be labeled as live/non-deterministic evidence.
- Post-change compact/large-font Daily hierarchy/bounds evidence from required Compose/Android UI checks; installed-app compact/large-font screenshots are optional additional evidence and must be explicitly labeled if omitted.

Broad verification:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Artifact directory:
- `.codex/test-artifacts/2026-08-31-daily-page-visual-baseline/`

## Phase Results

- specified: Slice 18D is defined in `.codex/plans/mvp-roadmap.md` and `docs/OXYGEN_FULL_SPECIFICATION.md`.
- planned: This active cycle selects only the Daily Page Visual Baseline behavior and defines acceptance, out-of-scope limits, focused evidence, real-path evidence, broad checks, and review obligations.
