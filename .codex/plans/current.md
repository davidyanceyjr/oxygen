# Active Cycle

Status: ready
Cycle ID: 2026-09-01-standard-home-accessibility-visual-verification-gate
Mode: feature
Goal: Verify the completed Standard Home interaction and visual baseline as the
foundation for subsequent MVP work, fixing only accessibility or visual
correctness defects found inside that baseline.
Roadmap context: Slice 18H, Standard Home Accessibility and Visual Verification
Gate. Prerequisites are Slices 18A through 18G committed; Slice 18G is committed
at `fae63b3`.
Branch or work context: HEAD is `fae63b3`; current branch name is
`slice-18f-home-operational-state-integration`, retained from prior work. The
worktree already contains user documentation/status edits from the post-18G
sync.

## Contract

Selected behavior:
- Establish Standard Home Now, Hourly, Daily, and Details as the verified
  installed-app baseline for subsequent MVP slices.
- Prove semantic page navigation, current-page identity, accessible page
  movement, readable weather meaning, source/stale/error communication,
  adequate touch targets, long-location handling, large-font safety,
  non-overlap, and representative operational states.
- Preserve the existing provider, repository, Room, DataStore, forecast mapping,
  selected-location, manual-search, stale-cache, retry/refresh, and disclosure
  behavior.

Acceptance boundary:
- Current Home page identity is visible and exposed through semantics on each
  Standard Home page.
- Page movement is available through ordinary tabs/swipes and through named
  semantics actions on the existing pager container that let accessibility users
  move from Now to Hourly and from Details to Daily without relying on swipe
  gestures. Add visible previous/next controls only if custom semantics actions
  cannot be verified through the Compose accessibility boundary.
- Child controls such as Refresh, Settings/About, Change location, Retry, page
  tabs, and forecast rows remain independently usable and do not accidentally
  change pages.
- Refresh, Settings/About, Change location, Retry, page tabs, and any explicit
  page navigation controls expose independently clickable bounds of at least
  48dp in the relevant tested configurations.
- Important weather information remains readable with long location/provider
  strings, compact width, and large font.
- Important information is not clipped or overlapped in supported compact and
  ordinary presentation checks.
- Standard ordinary presentation does not require page-level vertical traversal
  of the whole Home surface; localized overflow may scroll inside the relevant
  page where needed.
- Effects-disabled rendering uses the same Home composable production path with
  `OxygenAppearance(effects = EffectsLevel.OFF)` or an equivalent app-local
  appearance input, and still exposes complete weather meaning through
  text/semantics: current page identity, current temperature, condition, high
  and low, source/update context, provider disclosure, and stale/error messages
  where present, without depending on gradients, transparency, animation, or
  atmospheric decoration.
- Stale, refresh-failed, loading, no-cache error, source/update, and provider
  disclosure communication remains understandable and reachable.
- Final installed-app screenshots exist for Now, Hourly, Daily, and Details.
- Installed-app operational evidence targets stale refresh-failed cached Home;
  if that state cannot be reached in the installed/debug path, capture no-cache
  retryable error and record the exact blocker for stale-state capture.
- Focused Compose/state tests and broad Android verification pass.

Explicitly out of scope:
- Simple, Detailed, or Meteorologist layouts.
- Persisted layout, theme, icon-pack, or effects selection.
- A full theme engine or new user-facing appearance settings.
- Foldable-specific UI.
- Provider, fallback, geocoding, repository, Room, DataStore, cache schema,
  weather mapping, app identity, dependency, Gradle, manifest, saved-location,
  unit-preference, official-alert, air-quality, radar, background-refresh,
  release-candidate, or MVP-readiness behavior changes.
- Treating `SampleWeather.bundle` as production Home behavior.

## Implementation Plan

1. Discover and baseline:
   - Re-read the Slice 18H roadmap entry, specification section 53, current
     Home composables, Home presentation/state tests, installed-app entry path,
     and recent cycle evidence.
   - Record `git status --short` before edits and preserve unrelated user
     documentation changes.
   - Run focused Home tests as a baseline where feasible:
     `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*'`
     and the existing Home Compose instrumentation class on a connected
     emulator.
   - Build a short Slice 18H evidence matrix before production edits. Mark each
     roadmap proof item as `already covered`, `needs focused test`, `needs
     installed evidence`, or `blocked with exact reason`; do not add duplicative
     tests for items already covered at a meaningful boundary.

2. Cover the gate before or alongside fixes:
   - Add or tighten Home Compose instrumentation tests for semantic current-page
     identity, named accessibility movement through custom semantics actions
     from Now to Hourly and Details to Daily without swipe gestures, child
     controls not paging accidentally, 48dp-or-larger clickable bounds for Home
     controls, compact
     long-location/long-provider rendering, large-font readable bounds,
     non-overlap, stale/source/error reachability, and effects-disabled weather
     meaning through the production Home composable path.
   - Prefer assertions at the Compose semantics, bounds, and rendered-pixel
     boundary already used by `HomeDashboardUiTest`.
   - Avoid tests that only assert tags, constructors, or static text exists
     without exercising the Home interaction or rendered boundary.

3. Implement only defects required by the gate:
   - If baseline tests expose missing accessibility semantics, add the smallest
     Home UI semantics needed to make page identity and page movement
     discoverable. Prefer custom pager semantics actions over visible new
     controls so the existing layout and child-control behavior do not drift.
   - If compact/large-font/long-string evidence exposes clipping or overlap,
     adjust only the affected Home layout constraints, wrapping, or stable
     dimensions.
   - If effects-disabled rendering has no production path, introduce the
     narrowest app-local non-persisted rendering parameter needed for tests and
     previews to exercise no-effects presentation through the same composable
     path without creating user settings or a theme engine.
   - Do not change provider/domain/repository/persistence behavior to satisfy
     presentation tests.

4. Focused green:
   - Run focused state tests:
     `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*'`
   - Run focused Home Compose instrumentation:
     `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`
   - Save focused logs under
     `.codex/test-artifacts/2026-09-01-standard-home-accessibility-visual-verification-gate/`.

5. Real-path exercise:
   - Use `scripts/list-avds.sh`, `scripts/start-emulator.sh`, and
     `scripts/install-debug.sh` to launch the installed app.
   - For stale refresh-failed cached Home evidence, first reuse the deterministic
     seed/failure path from prior Home slices: seed or retain a selected
     location with a Room cached forecast, force the provider/network refresh to
     fail, relaunch or refresh the installed debug app, and capture the stale
     cached Now state. Record the exact seed/failure command or script used.
   - Capture installed-app screenshots and hierarchies for Now, Hourly, Daily,
     Details, and stale refresh-failed cached Home. If stale refresh-failed
     cached Home cannot be reached in the installed/debug path, capture no-cache
     retryable error and record the exact blocker.
   - Record the emulator/device identity, commands, and artifact paths.

6. Broad checks:
   - `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
   - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
   - `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
   - `git diff --check`

7. Review and ready:
   - Review `git diff` for scope drift, provider/persistence changes, hidden
     sample fallback, speculative settings, or status overclaims.
   - Convert the page-to-page visual coherence proof into concrete evidence:
     final installed screenshots for all four Standard Home pages, no
     overlap/clipping assertions, stable page selector/header bounds where
     practical, and a short review note naming any visible inconsistency found.
     Do not use subjective visual review as a substitute for tests or
     screenshots.
   - Do not resolve art-sheet asset naming drift in this cycle unless it
     directly blocks accessibility or installed-app visual verification.
   - Update this plan's phase results with commands actually run and artifact
     paths.
   - Append a concise cycle-history entry only when the implementation cycle is
     ready or committed.

## Phase Results

- specified: Slice 18H is specified in `.codex/plans/mvp-roadmap.md` and
  `docs/OXYGEN_FULL_SPECIFICATION.md` section 53 as the Standard Home
  Accessibility and Visual Verification Gate.
- planned: This cycle selects Slice 18H only. Acceptance is the verified
  Standard Home baseline across accessibility, visual, operational-state, and
  installed-app evidence boundaries without provider, persistence, settings, or
  release-readiness changes.
- covered: Added focused `HomeDashboardUiTest` coverage for named pager custom
  accessibility actions, pager child-control isolation, 48dp Home control
  bounds, and effects-disabled weather meaning through the production
  `HomeLoadingScreen` path. Existing Home tests continue to cover long
  location/provider strings, large-font readable bounds, non-overlap, stale
  cache, restored cache, loading, no-cache error, source/update disclosure,
  tabs, swipes, and provider-neutral weather marks.
- implemented: `HomeLoadingScreen` now accepts app-local `OxygenAppearance`,
  renders opaque Home surfaces when `effects = EffectsLevel.OFF`, exposes named
  previous/next custom accessibility actions on the existing pager container,
  and gives Standard Home page tabs a 48dp minimum height. No provider,
  repository, Room, DataStore, forecast mapping, selected-location,
  manual-search, stale-cache, retry/refresh, disclosure, Gradle, manifest,
  saved-location, unit-preference, fallback, alert, or release behavior changed.
- verified: Focused and broad checks passed with logs under
  `.codex/test-artifacts/2026-09-01-standard-home-accessibility-visual-verification-gate/`:
  `baseline-homeforecast-unit.log`,
  `focused-compile-after-pager-actions.log`,
  `focused-home-compose-instrumentation.log`,
  `broad-compile-debug-kotlin.log`,
  `broad-debug-unit-tests.log`,
  `broad-assemble-debug.log`, and `git-diff-check.log`.
  Focused Home instrumentation passed 19 tests on `oxygen_starter(AVD) - 17`.
  Installed-app evidence was captured for a real Open-Meteo selected location:
  `installed-final-home-now.png`/`.xml`,
  `installed-final-home-hourly.png`/`.xml`,
  `installed-final-home-daily.png`/`.xml`,
  `installed-final-home-details.png`/`.xml`, and
  `installed-operational-refresh-failed-attempt.png`/`.xml`. The operational
  capture shows stale cached Home with `Cached forecast`, `Refresh failed`,
  current condition, temperature, high/low, source/update, and no Retry.
  Broad commands passed:
  `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`;
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`;
  `. scripts/android-env.sh && ./gradlew :app:assembleDebug`; `git diff --check`.
- skipped: No Simple, Detailed, or Meteorologist layouts; no persisted
  appearance settings; no provider, persistence, fallback, saved-location,
  unit-preference, alert, air-quality, radar, release, or MVP-readiness changes.
  Android shell denied the `android.intent.action.AIRPLANE_MODE` broadcasts
  during operational-state setup, but `svc wifi disable` and `svc data disable`
  still forced the installed refresh failure; services were restored afterward.
