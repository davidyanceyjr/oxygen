# Slice 14 Plan Review Findings

## Review Metadata

- Review date: 2026-08-25
- Reviewer: Codex
- Repository: `/home/opsman/project_git/oxygen`
- Reviewed cycle: `2026-08-23-forecast-fallback-selection`
- Scope: Pre-implementation review of `.codex/plans/current.md` for gaps, blockers, and slop
- Build/test commands run: none; this review updates review notes only

## Findings

1. `.codex/plans/current.md:27` narrows the retry-loop proof below the roadmap wording. `.codex/plans/mvp-roadmap.md:317` says repeated failures must not cause wasteful retry loops from repository refresh calls or location changes, while the current plan proves only that one `refresh(location)` does not recursively retry.

   Recommendation: keep Slice 14 core-only, but make the boundary explicit. Add a focused test that two separate `refresh` calls, including distinct locations, perform exactly one Open-Meteo attempt and at most one MET Norway attempt per call, with no retained automatic retry state. Also add a plan note that app/state-holder location-change loop behavior remains out of scope until fallback is wired into app behavior. This avoids claiming UI-level loop prevention without evidence.

2. `WeatherRepositoryResult.Failure` currently carries a single `ForecastError` at `core/src/main/kotlin/com/oxygen/weather/core/provider/WeatherProviders.kt:66`, so the current model cannot preserve both Open-Meteo and MET Norway failure causes required by `.codex/plans/current.md:25` and `.codex/plans/mvp-roadmap.md:316`.

   Recommendation: use a non-breaking additive result-surface change: add an optional provider-neutral diagnostics field to `WeatherRepositoryResult.Failure`, for example `val diagnostics: List<ForecastError> = listOf(error)`. Existing call sites that read `failure.error` keep working, existing tests compile with the default value, and fallback tests can assert that both causes are preserved without adding provider-specific UI copy or logs-as-behavior.

3. `.codex/plans/current.md:67` leaves room for a default `FallbackWeatherRepository` constructor that instantiates `OpenMeteoWeatherRepository` and `MetNoWeatherRepository`. That is unnecessary for the accepted fake-repository evidence and risks coupling provider-neutral selection to provider-specific production classes before app wiring intentionally uses it.

   Recommendation: remove the default-constructor step from Slice 14. Implement only constructor injection of two `WeatherRepository` instances and test the selection behavior through that boundary. Let the later app-wiring/disclosure slice decide how production repositories are composed, with its own observable evidence.

4. The static provider-boundary check at `.codex/plans/current.md:43` catches DTO/client/parser/header leakage but would not catch provider-neutral fallback code importing `OpenMeteoWeatherRepository` or `MetNoWeatherRepository`.

   Recommendation: extend the static check for this slice to flag provider-specific repository imports or package references outside their provider packages, unless they appear only in tests. This enforces the intended composition boundary and keeps the fallback selector provider-neutral without relying on review memory.

5. `.codex/plans/current.md:25` says the both-provider failure should be "retryable," but retryability is not currently a first-class repository contract. The app presently derives retry UI from provider-neutral forecast failure messages, not from a dedicated retry flag.

   Recommendation: do not add a retryability abstraction in Slice 14. Define "retryable" for this slice as returning `WeatherRepositoryResult.Failure` through the existing repository failure path, preserving app behavior. If retryability later needs a typed policy, add it in the cache/error-state slice where UI behavior can be exercised.

## Summary

The current plan is mostly well bounded: it selects one repository boundary, avoids live-provider theater, keeps MET Norway out of active app disclosure, and excludes UI/cache/saved-location claims. The main implementation risk is overstating what core-only tests prove.

Recommended plan edits before implementation: add the non-breaking diagnostics field, remove the default-constructor step, tighten the static boundary check, and clarify the retry-loop acceptance boundary.
