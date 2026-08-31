# Active Cycle

Status: verified
Cycle ID: 2026-08-31-hourly-page-visual-baseline
Mode: feature
Goal: Establish the canonical Oxygen Hourly page as a dedicated, scannable near-term forecast experience.
Roadmap context: Slice 18C: Hourly Page Visual Baseline, after committed Slice 18B Now Page Visual Baseline and before Daily/Details visual baseline slices.
Branch or work context: local `main` is ahead of `origin/main`; untracked root screenshot `2026-08-30-09:23_1920x1080.png` is unrelated to this active implementation plan and must be preserved unless explicitly selected later.

## Contract

Selected behavior:
- Redesign only the Standard Home Hourly page composition so it quickly answers "what happens next?" from provider-neutral presentation state.
- Communicate upcoming time, condition identity, temperature, and precipitation where available for each near-term entry.
- Make weather identity visually recognizable without requiring verbose condition text for every item, while preserving meaningful accessibility semantics.
- If Hourly uses `WeatherConditionMark` or another condition visual, expose provider-neutral condition identity through `HomeHourlyPresentation`; do not derive it by parsing formatted display text.
- Keep the Hourly page a viewport-oriented composition at ordinary supported phone sizes, not a long vertically scrolling hourly document.
- Allow local horizontal movement or paging inside Hourly only where it improves comparison and remains subordinate to Home's semantic page navigation.
- Any local Hourly horizontal movement or paging must expose visible or accessible non-gesture controls and must not be the only way to reach required hourly information.
- Preserve direct Home page navigation, horizontal swipe navigation, page identity, page position, refresh behavior, About navigation, loading, no-cache error, stale, and refresh-in-progress surfaces.
- Preserve child-interaction isolation so Hourly controls or local movement do not accidentally advance the Home page.
- Keep compact-width and large-font presentations readable and non-overlapping with representative long location/status/source text.
- Keep effects-off or decoration-light presentation complete: required hourly semantics must remain visible as text or stable symbols.
- Each Hourly entry exposes precipitation as either a real provider-backed probability value or an explicit unavailable state; do not fill missing precipitation with fabricated percentages or amounts.
- Do not parse formatted display strings back into numbers in Composables.
- If a programmatic temperature or precipitation visualization is introduced, deliberately evolve the presentation contract with numeric semantic values and focused tests.

Acceptance boundary:
- Production Home Hourly page renders provider-neutral presentation state only; no provider DTOs, provider-specific errors, hidden default location, fabricated weather values, or `SampleWeather.bundle` production path is introduced.
- Existing Now, Daily, and Details pages remain functionally reachable through the Slice 18A page model, but substantial visual redesign of those pages is out of scope.
- Existing selected-location persistence, Room forecast cache, offline restored stale Home, failed-refresh stale retention, live-success cache-failure display, source/provenance text, About navigation, refresh/retry actions, and Home page navigation behavior remain observable.
- At ordinary supported phone size, the first visible Hourly viewport shows multiple chronological near-term entries, and each available entry exposes time, condition identity, temperature, and precipitation without text overlap or clipped required semantics.
- At `360dp` width with `fontScale = 1.0`, the first visible Hourly viewport shows at least four chronological near-term entries, including varied condition identity, one real provider-backed precipitation probability, and one explicit precipitation-unavailable state.
- Deterministic Hourly test and installed-app screenshot data include enough chronological hourly entries to prove visual density, ordering, varied condition identity, present precipitation, and unavailable precipitation behavior.
- Hourly visual hierarchy and density are verified at an Android/Compose boundary with deterministic forecast data.
- Compact-width and large-font checks cover the Hourly page with representative long location/status/source text.
- Installed-app Hourly screenshot evidence is required for the post-change deterministic state. If emulator/device capture is blocked, record the exact command/output blocker and do not claim installed-app screenshot evidence for the cycle.
- `git diff --check` passes.
- Broad Android checks pass unless an explicit environment blocker is recorded with exact command output and the unverified status is not claimed.

Explicitly out of scope:
- Now page visual redesign.
- Daily page visual redesign.
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
   - Review current Home Hourly implementation, Home presentation mapper/state, existing page navigation model, Oxygen theme/components, and focused Home UI tests.
   - Inspect Slice 18A/18B screenshot and hierarchy artifacts to understand the current Hourly baseline and shared Home chrome constraints.

2. contract
   - Add or adjust focused Compose/Android UI tests that encode Hourly page identity, near-term order, visible time/condition/temperature/precipitation semantics, scannable density, compact-width/large-font bounds, child-interaction preservation, and Home page navigation preservation.
   - Add presentation-state tests if Hourly gains provider-neutral condition identity or numeric values for visualization; these tests must prove fields are passed from domain state, not parsed from display strings.
   - Include deterministic Hourly fixture data with multiple chronological entries, varied weather conditions, at least one real precipitation probability, and at least one unavailable precipitation case.
   - If `WeatherConditionMark` or any condition visual is used, add a focused presentation test proving `HomeHourlyPresentation.conditionIdentity` is populated directly from `HourlyForecast.condition` alongside the display string.
   - Keep assertions tied to observable UI semantics, bounds, actions, and data boundaries rather than implementation symbol existence.

3. design-if-needed
   - Decide whether the Hourly page can be built from existing display fields or needs a deliberate presentation contract extension for numeric visualization.
   - Prefer existing package structure and narrowly scoped components; introduce design tokens/components only where actual reuse or complexity reduction is demonstrated.

4. red-or-baseline
   - Run existing focused Home UI tests before production edits and save the baseline log unless the emulator/test environment is blocked with exact command output. Add new failing tests only after this baseline.
   - Capture or identify an installed-app Hourly baseline from Slice 18A where practical; if blocked, record the blocker and use existing historical artifacts only as labeled baseline context.

5. build
   - Refactor the Hourly page into a dedicated near-term forecast composition using provider-neutral Home presentation state.
   - Preserve Home header/page chrome hierarchy established by Slice 18B without letting Hourly become a generic vertical list.
   - Keep weather values honest: omit unavailable fields instead of filling visual space with fabricated values.

6. focused-green
   - Run targeted Home state/presentation tests affected by Hourly changes.
   - Run targeted Home Compose/connected UI tests for Hourly visual baseline behavior and existing page navigation/refresh/retry preservation.
   - Save logs under `.codex/test-artifacts/2026-08-31-hourly-page-visual-baseline/`.

7. real-path-exercise
   - Expand the deterministic installed-app selected-location plus Room cached forecast seed before capture so it contains multiple chronological hourly entries with varied condition identity, present precipitation, and unavailable precipitation. Live provider state is fallback only and must be labeled as live/non-deterministic evidence.
   - Keep deterministic Hourly seed data in test/seeding paths that exercise the existing DataStore selected-location plus Room cached-forecast restore path; do not add a fake production provider, hidden default location, or sample-weather production route for screenshot setup.
   - Install and launch the debug app against the deterministic seeded selected location plus Room cached forecast state by default.
   - Capture post-change installed-app Hourly screenshot and hierarchy evidence at ordinary configuration.
   - Capture compact/large-font Hourly screenshot or hierarchy/bounds evidence where practical.

8. broad-checks
   - `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
   - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
   - `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
   - `git diff --check`

9. review
   - Review production/test diffs for scope creep, provider/domain leakage, fabricated values, unrelated roadmap/docs churn, and accidental claims beyond Slice 18C.
   - Before any commit, run `git status --short` and confirm root screenshot `2026-08-30-09:23_1920x1080.png` remains untracked and unstaged unless the owner explicitly selects it.
   - Update this file with evidence and phase results only after commands are actually run.

## Evidence Plan

Focused evidence to collect:
- New Hourly evidence: Home Compose/Android UI tests proving Hourly page identity, multiple chronological near-term entries in the first ordinary viewport, visible or accessible condition identity, condition visual semantics where used, temperature, real precipitation probability, explicit unavailable precipitation state, visual density, compact-width/large-font readability, and child-control isolation for any local Hourly movement.
- New presentation evidence: presentation or state-holder tests proving any new Hourly presentation contract fields are provider-neutral, honest about null/unavailable data, and not derived by parsing formatted display strings in Composables.
- Reused regression evidence: existing or updated focused tests proving Home page navigation, refresh/retry, loading/no-cache surfaces, stale/refresh-in-progress preservation, About navigation, and source/provenance reachability remain observable after the Hourly redesign.

Real-path evidence to collect:
- Historical or pre-change Hourly baseline artifact, explicitly labeled.
- Post-change installed-app Hourly screenshot for a deterministic seeded selected-location plus Room cached forecast state. Live provider screenshot evidence is fallback only and must be labeled as live/non-deterministic evidence.
- Post-change compact/large-font Hourly screenshot or hierarchy/bounds evidence.

Broad verification:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Artifact directory:
- `.codex/test-artifacts/2026-08-31-hourly-page-visual-baseline/`

## Phase Results

- specified: Slice 18C is defined in `.codex/plans/mvp-roadmap.md` and `docs/OXYGEN_FULL_SPECIFICATION.md`.
- planned: This active cycle selects only the Hourly Page Visual Baseline behavior and defines acceptance, out-of-scope limits, focused evidence, real-path evidence, broad checks, and review obligations.
- covered: Focused state-holder tests now prove Hourly presentation carries provider-neutral `conditionIdentity` from `HourlyForecast.condition`, preserves chronological time/condition/temperature, and keeps missing precipitation as `null`; focused Home Compose instrumentation now proves the compact Hourly page exposes four chronological entries with varied condition identity, real precipitation probability, explicit unavailable precipitation, page identity, viewport bounds, and preserved Home navigation/loading/error/refresh surfaces. Logs: `.codex/test-artifacts/2026-08-31-hourly-page-visual-baseline/focused-home-state-holder.log`, `.codex/test-artifacts/2026-08-31-hourly-page-visual-baseline/focused-home-compose-instrumentation-final.log`.
- implemented: Production Hourly now renders a dedicated `Next hours` grid from `HomeHourlyPresentation`, uses `WeatherConditionMark` from provider-neutral condition identity, exposes each entry's time/condition/temperature/precipitation through accessibility content descriptions, and renders missing precipitation as `Precipitation unavailable` without fabricated values. The deterministic installed-app seeding path now writes six chronological hourly entries with varied conditions, present precipitation, and unavailable precipitation through DataStore selected-location plus Room forecast-cache storage.
- verified: Baseline Home state-holder test passed before production edits: `.codex/test-artifacts/2026-08-31-hourly-page-visual-baseline/baseline-home-state-holder.log`. Focused Home state-holder test passed after changes. Focused Home Compose instrumentation passed 10 tests on `oxygen_starter(AVD) - 17`. Installed-app seed test passed, then the debug app was launched offline from the seeded selected location plus Room cached forecast; post-change Hourly screenshot and hierarchy were captured at `.codex/test-artifacts/2026-08-31-hourly-page-visual-baseline/hourly-after-installed.png` and `.codex/test-artifacts/2026-08-31-hourly-page-visual-baseline/hourly-after-installed.xml`. The hierarchy includes `Hourly, Page 2 of 4`, `Next hours`, `6 AM, Rain, 64 deg F, 60%`, `7 AM, Cloudy, 67 deg F, Precipitation unavailable`, `8 AM, Partly cloudy, 68 deg F, 20%`, `9 AM, Mostly clear, 70 deg F, 10%`, `10 AM, Thunderstorm, 72 deg F, Precipitation unavailable`, and `11 AM, Rain showers, 71 deg F, 40%`. Broad checks passed: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`, `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`, `. scripts/android-env.sh && ./gradlew :app:assembleDebug`, and `git diff --check`; logs are saved in `.codex/test-artifacts/2026-08-31-hourly-page-visual-baseline/`.
- ready: Diff review found the behavior scoped to Hourly presentation/UI, deterministic Android seed data, and focused tests. No provider behavior, repository/cache behavior, selected-location persistence behavior, production sample-weather path, Now/Daily/Details redesign, saved-location switching, unit preferences, official alert lookup, air quality, radar, background refresh, installed-app MET Norway fallback activation, persisted appearance settings, release readiness, or MVP readiness was added or claimed. `git status --short` still shows the pre-existing untracked root screenshot `2026-08-30-09:23_1920x1080.png` unstaged and preserved.
