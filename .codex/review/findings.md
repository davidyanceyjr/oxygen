# Slice 11 Plan Review Findings

## Review Metadata

- Review date: 2026-08-22
- Reviewer: Codex
- Repository: `/home/opsman/project_git/oxygen`
- Reviewed plan: `.codex/plans/current.md`
- Cycle ID: `2026-08-22-provider-backed-home-success-dashboard`
- Scope: Slice 11 plan review for unresolved gaps, blockers, and SLOP risk
- Worktree state at review: `.codex/plans/current.md` modified; `.codex/review/` untracked before this overwrite
- Build/test commands run: none; this was a plan review only
- Review commands run:
  - `pwd`
  - `git status --short`
  - `git diff -- .codex/plans/current.md`
  - `git log --oneline -5 -- .codex/cycles/history.md .codex/plans/current.md LICENSE NOTICE THIRD_PARTY_LICENSES.md DATA_SOURCES.md PRIVACY.md`
  - `rg --files -g 'AGENTS.md' -g 'README.md' -g 'settings.gradle.kts' -g 'build.gradle.kts' -g 'app/build.gradle.kts' -g 'core/build.gradle.kts' -g 'scripts/android-env.sh' -g 'docs/OXYGEN_FULL_SPECIFICATION.md' -g 'docs/data-sources/PROVIDER_TEMPLATE.md' -g '.codex/plans/current.md' -g '.codex/cycles/history.md'`
  - `sed`/`nl` reads of `AGENTS.md`, `README.md`, `.codex/plans/current.md`, `.codex/cycles/history.md`, `.codex/plans/mvp-roadmap.md`, `docs/OXYGEN_FULL_SPECIFICATION.md`, `docs/data-sources/PROVIDER_TEMPLATE.md`, `docs/data-sources/OPEN_METEO_FORECAST.md`, `app/build.gradle.kts`, `app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt`, `app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt`, `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`, `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastStateHolderTest.kt`, and `core/src/main/kotlin/com/oxygen/weather/core/model/WeatherModels.kt`
  - `rg` checks for spec anchors, app/core model and state-holder symbols, provider/sample/placeholder references, Compose test dependencies, and source directory structure
  - `find .codex/review -maxdepth 2 -type f -print`
  - `find app/src -maxdepth 3 -type d -print | sort`

## Unresolved Findings

1. Primary real-path screenshot evidence remains ambiguous.

   `.codex/plans/current.md` requires the acceptance boundary to include "a log plus screenshot showing real provider-backed Home success," but the real-path procedure allows emulator screenshots to block and only mandates Compose assertions for constrained readability. Tighten this so Slice 11 cannot be marked `verified` unless either a real production Home success screenshot exists, or the plan explicitly accepts a named substitute for that primary screenshot.

2. Compose-boundary evidence may require infrastructure that is not currently planned.

   The plan requires Compose-boundary assertions as the fallback when emulator screenshot automation blocks, but `app/build.gradle.kts` currently has only JVM `testImplementation(libs.junit)` and there is no `app/src/androidTest` tree or Compose UI test dependency. This is not a blocker, but it is a planning gap. Slice 11 should explicitly include adding minimal Compose UI test infrastructure if emulator screenshot automation blocks again.

3. Empty-success provenance is underspecified.

   The plan asks returned success UI to show source/provenance using `DataProvenance` and `WeatherBundle.fetchedAt`, but `WeatherBundle` has no bundle-level provenance. If `current == null` and hourly/daily are empty, there may be no `DataProvenance` to display. Define the expected empty-success output precisely, such as fetched time plus "source unavailable/unknown," or add a provider-neutral bundle provenance field with core tests if source provenance is required.

4. Relative age formatting is underspecified.

   The plan requires fetched/updated "age or local timestamp" and focused tests for formatting, but it does not require an injected clock. To avoid flaky tests or accidental device-time behavior, choose deterministic local timestamps, or require clock injection in the presentation mapper.

5. Slice 11 remains broad enough to invite shallow dashboard shells.

   The slice combines presentation model, full Home dashboard, alerts-from-domain, null handling, timezone formatting, no-leak checks, regression preservation, live provider exercise, emulator screenshot evidence, and accessibility/readability evidence. This is coherent, but the main SLOP risk is implementing section shells that satisfy breadth without proving values. Keep tests value-specific, not section-existence-only.

## Blockers

No hard blocker found.

Gate 3A is present in history and `git log` shows commit `71c45ee Backfill repository license and privacy baseline`.

## SLOP Watch

The current plan is stronger than the previous stale review note. The remaining SLOP risks are evidence substitution around screenshots, missing Compose test infrastructure planning, provenance expectations for an empty successful bundle, nondeterministic relative-age formatting, and breadth-driven placeholder dashboard sections.
