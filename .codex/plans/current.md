# Active Cycle

Status: committed
Cycle ID: 2026-08-30-now-page-visual-baseline
Mode: feature
Goal: Establish the canonical Oxygen current-conditions experience on the Standard Home Now page.
Roadmap context: Slice 18B: Now Page Visual Baseline, after committed Slice 18A Home Paged Interaction Foundation and before Hourly/Daily/Details visual baseline slices.
Branch or work context: local `main` ahead of `origin/main`; existing README, roadmap, history, and untracked screenshot changes are unrelated to this active implementation plan and must be preserved unless explicitly selected later.

## Contract

Selected behavior:
- Redesign only the Standard Home Now page composition so current temperature and condition establish the primary visual hierarchy.
- Keep selected location/context immediately understandable without making it dominate the page.
- Keep Oxygen branding, page identity, and page navigation available but visually subordinate to the current-weather composition.
- Group feels-like, high/low, and immediately useful current-weather context into a coherent supporting Now-page area.
- Keep fresh update/source information visually tertiary while allowing stale or refresh-failed operational status to become more prominent.
- Preserve existing refresh behavior on fresh, stale, and refresh-in-progress Home success states.
- Preserve retry behavior for no-cache error states and existing loading/operational Home surfaces.
- Keep Now as one deliberate viewport-oriented composition at ordinary supported phone sizes, not a generic stack of unrelated cards.
- Ensure the first ordinary phone viewport is owned by the current-weather hero and supporting weather context, not by large navigation controls, prose, or operational chrome.
- Keep long selected-location names readable and non-overlapping.
- Keep the effects-off presentation complete: required weather semantics must remain visible as text or stable symbols without relying on gradients, transparency, animation, or decorative scene effects.
- Promote repeated Now-page visual values into existing or narrowly introduced Oxygen design tokens only where the implementation demonstrates actual reuse or removes local duplication.
- Retain installed-app before/after screenshot evidence.

Acceptance boundary:
- Production Home Now page renders provider-neutral presentation state only; no provider DTOs, provider-specific errors, hidden default location, fabricated weather values, or `SampleWeather.bundle` production path is introduced.
- Existing Hourly, Daily, and Details pages remain functionally reachable through the Slice 18A page model, but substantial visual redesign of those pages is out of scope.
- Existing selected-location persistence, Room forecast cache, offline restored stale Home, failed-refresh stale retention, live-success cache-failure display, source/provenance text, About navigation, refresh/retry actions, and page navigation behavior remain observable.
- Large-font and compact-width checks cover the Now page with representative long location/status/source text.
- Screenshot evidence includes a pre-change installed-app Now baseline and a post-change installed-app Now page for the same deterministic seeded or live state where practical.
- `git diff --check` passes.
- Broad Android checks pass unless an explicit environment blocker is recorded with exact command output and the unverified status is not claimed.

Explicitly out of scope:
- Hourly page visual redesign.
- Daily page visual redesign.
- Details page visual redesign.
- Full design-token consolidation.
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
   - Review current Home Now implementation, presentation mapper/state, Oxygen theme/components, existing Home UI tests, and Slice 18A screenshot artifacts.
   - Capture or identify the current installed-app Now baseline before production edits; if capture is blocked, record the blocker and use the best existing Slice 18A Now artifact as explicitly historical baseline only.

2. contract
   - Add or adjust focused Compose/Android UI tests that encode the Now hierarchy, subordinate page/navigation chrome, stable operational controls, long-location behavior, compact width, large font, effects-off completeness, and child-interaction preservation.
   - Keep assertions tied to observable UI semantics, bounds, and actions rather than implementation symbol existence.

3. build
   - Refactor the Now page into a deliberate current-conditions composition using existing app presentation state and theme/component patterns.
   - Rework the Now page header/navigation treatment so it remains understandable and accessible without pushing current conditions below the first-viewport priority area.
   - Introduce narrowly scoped design tokens/components only if they are used by the Now page and have clear reuse pressure.
   - Preserve non-Now page behavior and data boundaries.

4. focused-green
   - Run targeted app unit tests for Home state/presentation regressions where affected.
   - Run targeted Home Compose/connected UI tests for Now visual baseline behavior and existing page navigation/refresh/retry preservation.
   - Save logs under `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/`.

5. real-path-exercise
   - Install and launch the debug app against a deterministic selected location and cached/provider forecast state.
   - Capture post-change installed-app Now screenshots and hierarchy dumps at ordinary and compact/large-font configurations where practical.
   - Confirm refresh/About child controls do not accidentally change pages.

6. broad-checks
   - `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
   - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
   - `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
   - `git diff --check`

7. review
   - Review production/test diffs for scope creep, provider/domain leakage, fabricated values, unrelated roadmap/docs churn, and accidental claims beyond Slice 18B.
   - Update this file with evidence and phase results only after commands are actually run.

## Evidence Plan

Focused evidence to collect:
- Home Compose/Android UI tests proving Now-page visual hierarchy, current temperature/condition dominance, subordinate page/navigation chrome, supporting feels-like/high-low/current context grouping, tertiary fresh source/update placement, prominent stale/refresh-failed status, refresh control behavior, compact-width/large-font bounds, effects-off completeness, and long-location handling.
- Regression tests or existing focused tests proving page navigation, child control isolation, retry/error surfaces, source/provenance, stale/offline behavior, and non-Now pages remain reachable.

Real-path evidence to collect:
- Pre-change installed-app Now screenshot or explicitly labeled historical Slice 18A baseline artifact.
- Post-change installed-app Now screenshot for the same deterministic seeded or live state where practical.
- Post-change compact/large-font Now screenshot or hierarchy/bounds evidence.

Broad verification:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Artifact directory:
- `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/`

## Phase Results

- specified: Slice 18B is defined in `.codex/plans/mvp-roadmap.md` and `docs/OXYGEN_FULL_SPECIFICATION.md`.
- planned: This active cycle selects only the Now Page Visual Baseline behavior and defines acceptance, out-of-scope limits, focused evidence, real-path evidence, broad checks, and review obligations.
- covered: Focused Home Compose instrumentation tests now encode Now-page order with current conditions before alerts on fresh/stale/refreshing success states, current hero bounds larger than location/action chrome, compact `360dp`/`fontScale = 1.3f` long-location/status/source readability, refresh/About child-control isolation, page navigation, loading, no-cache retry, and source/provenance reachability.
- implemented: `HomeLoadingScreen.kt` keeps the Slice 18A page model while compacting the Home header/page chrome, making selected-location controls subordinate, rendering the Now current-condition hero with prominent condition/temperature/mark, grouping feels-like/high-low/humidity/wind/update/source context, and keeping stale/refresh-failed status prominent above the hero.
- verified: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed; `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed; `git diff --check` passed.
- verified: Focused `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*'` passed, and focused Home connected instrumentation passed 9 tests on `oxygen_starter(AVD) - 17` with `-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`.
- verified: Real installed-app exercise manually installed debug app and androidTest APKs, cleared `com.oxygen.weather`, seeded deterministic selected-location plus Room cache with `OfflineLaunchPersistenceInstrumentedTest#seedDeterministicInstalledHomeScreenshotState`, disabled emulator network, launched `com.oxygen.weather/.MainActivity`, and captured the post-change cached Now page at `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/now-after-installed-final.png` with hierarchy `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/now-after-installed-final.xml`.
- verified: Historical pre-change Slice 18A success baseline remains `.codex/test-artifacts/2026-08-29-home-paged-interaction-foundation/home-now-foundation.png`; the 18B attempted pre-change `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/now-before.png` is an installed operational error screen and is not claimed as a successful Now baseline.

Artifact logs:
- `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/focused-home-compose-instrumentation-after-typography.log`
- `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/focused-home-unit.log`
- `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/broad-compile-debug-kotlin-final.log`
- `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/broad-unit-tests-final.log`
- `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/broad-assemble-debug-final.log`
- `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/git-diff-check-final.log`
- `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/manual-seed-installed-screenshot-state-final.log`
- `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/manual-launch-post-change-now-final.log`
- `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/manual-capture-now-after-final.log`
