# Active Cycle

Status: ready
Cycle ID: 2026-09-01-home-design-system-art-sheet-consolidation
Mode: feature
Goal: Implement Slice 18G by consolidating Home design-system roles and making the first visible, bounded runtime incorporation of the updated Oxygen art sheet without changing weather, provider, persistence, or page responsibilities.
Roadmap context: Slice 18G follows committed Slices 18B through 18F and precedes Slice 18H accessibility and visual verification.
Branch or work context: local `main` contains committed Slice 18G planning work (`c09b117`) on top of committed Slice 18F (`79bd830`). The worktree is clean at plan review; preserve any unrelated user changes if they appear during implementation.

## Contract

Selected behavior:
- Consolidate repeated Standard Home spacing, typography, shape, surface, weather-mark, and theme-translation choices into named Oxygen design-system roles where the role is already justified by existing Home pages.
- Make the updated art sheet visibly represented in the installed Home UI, not only documented or hidden behind token names.
- Replace the current generic blob-like weather mark presentation with a first art-sheet-aligned gold-line weather-mark treatment for existing provider-neutral `WeatherCondition` values.
- Apply art-sheet-aligned display numeral and section-label typography direction to Home weather values using available system fonts only.
- Apply art-sheet-aligned dark glass/surface and accent roles to Now, Hourly, Daily, Details, stale/status, metric, and source blocks while preserving their existing information hierarchy.
- Keep semantic distinction between Now, Hourly, Daily, and Details pages.
- Record any art-sheet elements deliberately deferred with a reason.

Acceptance boundary:
- Installed-app screenshots show visible art-sheet influence on the Home surface: gold-line weather marks, display-style weather numerals, stronger dark glass surfaces, compact metric/list treatment, and palette alignment.
- Existing Home content and interactions remain observable: page selector, Now, Hourly, Daily, Details, refresh, retry, Settings/About, Change location, source/update/provenance, stale/cache, and no-cache error.
- Focused Compose tests verify non-regression for page identity/content, compact phone width, large font behavior, sibling non-overlap, stale/error/source reachability, and supported theme rendering.
- State-holder tests continue to verify selected-location, refresh, retry, stale cache, and no-cache behavior without production sample fallback.
- No provider, repository, Room, DataStore, weather mapping, app identity, Gradle dependency, or saved-location/unit/alert behavior changes are required or claimed.
- `git diff --check` passes.

Explicitly out of scope:
- Full theme engine, persisted appearance/effects/layout settings, user-selectable icon packs, downloaded fonts, bundled production bitmap weather scenes, remote images, animations beyond existing behavior, radar, air quality, official alerts, saved-location switching, unit preferences, installed-app MET Norway fallback activation, release readiness, or MVP readiness.
- New provider data fields or fabricated weather values solely to match the art sheet.
- Generic surface primitives that erase the semantic differences between Now hero, Hourly tiles, Daily rows, Details metric groups, stale/status blocks, and source/provenance.
- Treating design-token extraction alone as implementation. If the installed Home screenshot does not visibly change toward the art sheet, the slice is not implemented.
- Updating dependency versions or adding icon/font/image libraries unless a separate dependency-maintenance slice is selected.

## Implementation Plan

1. Discover and baseline:
   - Inspect `docs/assets/oxygen-weather-visual-language-base-art-sheet-v0.1.png`; note that the current image title says v0.2 while the path/spec call it v0.1, and resolve documentation naming before final status claims if needed.
   - Compare existing Home screenshots from Slices 18B through 18F against the art sheet and record the gap explicitly.
   - Inspect `HomeLoadingScreen.kt`, `OxygenTheme.kt`, `WeatherConditionMark.kt`, `GlassPanel.kt`, `WeatherScene.kt`, and current Home Compose tests.
   - Run focused baseline checks before production edits:
     `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*'`
     `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`
   - If an emulator or Android command cannot run, record the exact command, failure, and lower-boundary evidence used.

2. Define bounded Home design-system roles:
   - Add or extend app-local theme roles for Home spacing, shapes, surface colors/alphas, type roles, and weather-mark roles.
   - Use Material 3 underneath and keep roles in `:app` UI/theme code.
   - Keep roles static and runtime-only; do not add persistence, settings UI, or a future-theme registry.
   - Name roles by semantic use, for example Home page margin, card padding, page gap, tile gap, compact mark, hero mark, display weather value, section heading, supporting label, ambient glass surface, strong status surface, and outline accent.

3. Implement visible art-sheet alignment:
   - Rework `WeatherConditionMark` to use a gold-line visual language for current provider-neutral conditions, including clear, mostly clear, partly cloudy, cloudy, fog, drizzle/rain/showers, snow, sleet/freezing rain, thunderstorm, and unknown.
   - Preserve Home-owned content descriptions and provider-neutral condition mapping.
   - Apply display numeral styling to current temperature, hourly temperatures, daily high/low values, and metric values where it improves readability.
   - Apply uppercase/small-label treatment to section labels and metric labels where already present.
   - Update Home card/tile surfaces to use named glass/surface roles while keeping the current page layout and operational-state placement.
   - Keep atmospheric scene imagery deferred unless a separate small asset slice is selected; do not fabricate scene images in 18G.

4. Focused tests:
   - Preserve existing Home state-holder tests for provider/persistence behavior.
   - Add or adjust Compose tests to verify that art-sheet-aligned marks render for representative provider-neutral conditions without relying on provider-specific symbols.
   - Verify Home pages still expose the same test tags and semantic content descriptions after visual refactoring.
   - Exercise Oxygen, Paper, and Terminal theme IDs only enough to verify Home semantics/content remain reachable after role consolidation. Do not add persistence, selectors, a theme registry, or new theme behavior.
   - Keep tests behavior-facing: page content, bounds, semantics, interactions, and representative visual-role application. Do not add tests that only prove constants/classes exist.
   - Run focused post-change checks:
     `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*'`
     `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`

5. Installed-app exercise:
   - Use the deterministic DataStore/Room seed path from prior Home slices.
   - Capture installed-app screenshots and hierarchy dumps for at least Now, Hourly, Daily, Details, cached/stale Now, and retryable no-cache error under:
     `.codex/test-artifacts/2026-09-01-home-design-system-art-sheet-consolidation/`
   - Include compact-width or large-font evidence if focused Compose tests reveal risk.
   - Compare screenshots against the prior 18B-18F artifacts and record whether the art-sheet influence is visible.
   - Record a concise before/after visual evidence note listing the prior screenshot path used for comparison, the new screenshot path, the observed concrete change, and any art-sheet element deferred with reason.

6. Broad verification:
   - `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
   - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
   - `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
   - `git diff --check`

7. Review and ready:
   - Review production and test diffs for scope creep, provider leakage, fabricated values, dependency churn, and generic abstraction that hides page semantics.
   - Before ready status, resolve or explicitly document the art-sheet naming drift: the tracked asset path/spec say Base Art Sheet v0.1 while the rendered image title says v0.2. Do not rename the PNG or introduce a replacement asset in this cycle unless a separate asset-governance decision is made.
   - Reject completion if evidence is only renamed constants, screenshots without described observable changes, tests that only assert token/class existence, or build success without Home boundary exercise.
   - Update `.codex/plans/current.md` phase results with changed production/test files, focused evidence, installed-app artifact paths, broad evidence, and deferred art-sheet elements.
   - Append `.codex/cycles/history.md` only after the cycle is ready or committed.
   - Do not claim Slice 18H accessibility gate, persisted presentation settings, full icon packs, production bitmap scenes, release readiness, or MVP readiness.

## Phase Results

- specified: Slice 18G is specified in `.codex/plans/mvp-roadmap.md`; the full specification section 53 selects Oxygen Home Design-System Consolidation and requires preservation of provider behavior, persistence behavior, and page responsibilities.
- planned: This cycle selects a bounded Home design-system/art-sheet incorporation slice. The prior installed UI shows only partial palette alignment; visible art-sheet incorporation remains unimplemented until production Home changes and installed-app screenshots prove it.
- covered: Focused Home state-holder unit tests pass, and `HomeDashboardUiTest` now includes a rendered-pixel assertion that representative provider-neutral weather marks expose the art-sheet gold-line treatment while existing Home page, compact, large-font, stale/error/source, and interaction tests remain in the same instrumentation boundary.
- implemented: Added app-local Home design roles in `app/src/main/kotlin/com/oxygen/weather/app/ui/theme/OxygenTheme.kt`; applied roles in `HomeLoadingScreen.kt` and `GlassPanel.kt`; replaced the generic blob condition mark in `WeatherConditionMark.kt` with provider-neutral gold-line marks for all current `WeatherCondition` values; added the focused rendered-mark Compose test in `HomeDashboardUiTest.kt`.
- verified: Baseline focused unit command passed before edits. Initial baseline connected Home instrumentation failed with `No connected devices`; after starting `oxygen_starter`, focused Home instrumentation passed 16 tests. Final focused commands passed: `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*'`; `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`. Broad commands passed: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh && ./gradlew :app:assembleDebug`; `git diff --check`.
- artifacts: Verification logs and screenshots are under `.codex/test-artifacts/2026-09-01-home-design-system-art-sheet-consolidation/`. Valid installed Home evidence: `installed-final-home-now.png`/`.xml`, `installed-final-home-hourly.png`/`.xml`, `installed-final-home-daily.png`/`.xml`, `installed-final-home-details.png`/`.xml`, and `installed-final-cached-stale-now.png`/`.xml`. Focused logs: `focused-homeforecast-unit.log`, `focused-home-compose-instrumentation-final.log`. Broad logs: `broad-compile-debug-kotlin.log`, `broad-debug-unit-tests.log`, `broad-assemble-debug.log`, `git-diff-check.log`.
- visual comparison: Compared against prior Slice 18F cached Now evidence at `.codex/test-artifacts/2026-09-01-home-operational-state-integration/installed-cached-now.png`; final installed Home screenshots visibly replace the white blob-like mark with gold-line marks, use stronger dark glass cards with accented outlines, and use a lighter display numeral treatment for the current temperature while retaining page selector, source/update, refresh/change-location, stale/cache, and page content.
- deferred: Atmospheric scene imagery, production bitmap scenes, downloaded fonts, full icon packs, persisted presentation settings, and full theme engine remain deferred by 18G scope. The art-sheet naming drift is documented but not renamed: the tracked path/spec say Base Art Sheet v0.1 while the rendered image title says v0.2.
- limitation: A final installed retryable no-cache screenshot was not captured; the attempted path failed after the targeted instrumentation run left the debug activity unavailable. Retryable no-cache behavior remains covered by `HomeDashboardUiTest.noCacheErrorKeepsAboutDisclosureAndRetryReachable`, `HomeForecastStateHolderTest`, and `OfflineLaunchPersistenceInstrumentedTest#startupWithSelectedLocationAndNoRoomCacheRendersRetryableNoCacheError`.
