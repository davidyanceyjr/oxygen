# Active Cycle

Status: planned
Cycle ID: 2026-08-23-forecast-fallback-selection
Mode: feature
Goal: Implement Slice 14 by adding a provider-neutral forecast fallback repository that attempts Open-Meteo first and, only for explicitly eligible Open-Meteo failures, attempts MET Norway once without hiding the served provider provenance or introducing UI, cache, saved-location, alert, unit, or disclosure behavior.
Roadmap slice: Slice 14: Forecast Fallback Selection from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 1, 4, 5, 6.1, 6.2, 6.3, 16, 17, 39, 40, 41, 44, 46, and 48
- `.codex/plans/mvp-roadmap.md` Slice 14 and Forecast Provider Scope
- `docs/data-sources/OPEN_METEO_FORECAST.md`
- `docs/data-sources/MET_NORWAY_FORECAST.md`
- Existing `OpenMeteoWeatherRepository` and `MetNoWeatherRepository` as verified provider-specific repository inputs
- `AGENTS.md`

Acceptance criteria:
- Add a provider-neutral fallback forecast repository in `:core` that implements the existing `WeatherRepository` boundary by composing an Open-Meteo/default `WeatherRepository` and a MET Norway/fallback `WeatherRepository`.
- `refresh(location)` must use the exact `WeatherLocation` supplied by the caller for both provider attempts. It must not introduce a hidden default location, scaffold/sample location, fallback location, selected-location state, app routing behavior, or location mutation.
- Emit one top-level `WeatherRepositoryResult.Loading` before terminal fallback-selection work. Suppress child repository loading emissions so fallback selection does not expose duplicate loading transitions as functional provider attempts.
- On Open-Meteo success, return that success and do not call MET Norway.
- Treat only these Open-Meteo/default failures as fallback-eligible in this slice: `ForecastError.RateLimited`, `ForecastError.ProviderUnavailable`, `ForecastError.InvalidResponse`, and `ForecastError.UnexpectedProviderFailure`.
- Treat `ForecastError.NetworkUnavailable` and `ForecastError.ProviderRejectedRequest` as not fallback-eligible in this slice because they indicate likely local connectivity or request/configuration failure where immediate provider fan-out would be wasteful or could hide an implementation defect.
- On fallback-eligible Open-Meteo failure followed by MET Norway success, return the MET Norway `WeatherRepositoryResult.Success` with the MET Norway provenance already present in the provider-neutral `WeatherBundle`.
- On fallback-eligible Open-Meteo failure followed by MET Norway failure, return a provider-neutral `WeatherRepositoryResult.Failure` through the existing retry-capable repository failure path while preserving both provider failure causes at the repository boundary for diagnostics. The top-level `Failure.error` must be the MET Norway/fallback terminal error because it is the final provider actually served to the existing app retry/error path. In this slice, "retryable" means the existing failure result shape remains usable by current app retry UI; do not add a separate retryability policy or UI abstraction.
- Preserve both-provider diagnostics with a non-breaking additive result-surface change: add an optional provider-neutral diagnostics field to `WeatherRepositoryResult.Failure`, such as `val diagnostics: List<ForecastError> = listOf(error)`. Existing call sites that read `failure.error` must keep working, and fallback tests must assert both causes without adding provider-specific UI copy, logs-as-behavior, or a new typed retry policy. For both-provider failures, diagnostics must be ordered by attempted provider: first the Open-Meteo/default failure, then the MET Norway/fallback failure.
- On non-fallback-eligible Open-Meteo failure, return the Open-Meteo failure without calling MET Norway.
- Repeated provider failures must not cause repository-level retry loops. A single `refresh(location)` may perform at most one Open-Meteo attempt and, if eligible, one MET Norway attempt. Two separate `refresh` calls, including calls for distinct locations, must each perform exactly one Open-Meteo attempt and at most one MET Norway attempt, with no retained automatic retry state, recursive retry, or backoff in this slice.
- Provider-specific DTOs, clients, parser errors, HTTP headers, MET Norway symbols, Open-Meteo weather codes, raw response bodies, and cache metadata must not cross the fallback repository boundary.
- Keep app default production wiring unchanged unless implementation proves that core-only fallback selection cannot be verified. This slice verifies forecast repository fallback selection only; it does not claim active app fallback behavior.

Acceptance boundary: Slice 14 is complete when focused core tests prove that fallback selection attempts Open-Meteo first, skips MET Norway on Open-Meteo success, attempts MET Norway exactly once for explicitly eligible Open-Meteo failures, preserves the exact selected location across both attempts, returns MET Norway success with MET Norway provenance after eligible Open-Meteo failure, returns a provider-neutral failure whose top-level `error` is the MET Norway/fallback terminal error and whose ordered diagnostics preserve the Open-Meteo/default cause followed by the MET Norway/fallback cause after both providers fail, does not fallback for non-eligible Open-Meteo failures, suppresses duplicate child loading emissions, and avoids retained or recursive repository retry loops across one refresh and across repeated refresh calls including distinct locations. Static checks must show no provider-specific implementation details or repository classes leaking through provider-neutral core or app UI boundaries. Slice 14 is not verified by Home UI rendering, active app wiring, in-app About/Data Sources disclosure, cache persistence, stale data, saved locations, app/state-holder location-change loop behavior, alert lookup independence, unit presentation, or live provider fetches.

Boundary decisions:
- Implement fallback as composition of verified repository paths, not by reaching into Open-Meteo or MET Norway DTO/client layers from the fallback selector.
- Prefer a narrow class such as `FallbackWeatherRepository` under the provider-neutral core provider package. Inject the default and fallback `WeatherRepository` instances through the constructor only; do not add a default constructor that instantiates provider-specific repositories in this slice. Do not create broad provider registries, preference systems, health tracking, WorkManager jobs, retry schedulers, cache policies, or service locators.
- Do not modify provider-specific repository behavior unless a focused fallback test exposes a real contract violation in that provider repository.
- Do not make MET Norway current/active in `OxygenAppStateHolder`, Home UI, `DATA_SOURCES.md`, `PRIVACY.md`, README, or in-app disclosure surfaces in this slice. Slice 15 owns active disclosure after fallback behavior is intentionally wired into app product behavior.
- Do not add sample success, fake production weather data, TODO-only branches, placeholder diagnostics, dormant feature flags, or tests that only prove constructors/symbols exist.
- Do not perform live Open-Meteo or MET Norway requests as acceptance evidence. This selection slice is deterministic and should use fake repositories at the core boundary.

Focused evidence to produce:
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*Fallback*Repository*'`
- Static provider-boundary check: `rg -n "OpenMeteoWeatherRepository|OpenMeteoForecastClient|OpenMeteoForecastClientResult|OpenMeteoForecastClientError|OpenMeteoForecastResponse|OpenMeteoCurrent|OpenMeteoHourly|OpenMeteoDaily|MetNoWeatherRepository|MetNoForecastClient|MetNoForecastClientResult|MetNoForecastClientError|MetNoForecastResponse|MetNoGeometry|MetNoMeta|MetNoTimeStep|MetNoInstant|MetNoPeriod|MetNoHttp|X-ErrorClass|symbolCode|weather_code|core\\.provider\\.openmeteo|core\\.provider\\.metno" core/src/main/kotlin/com/oxygen/weather/core/provider -g '!core/src/main/kotlin/com/oxygen/weather/core/provider/openmeteo/**' -g '!core/src/main/kotlin/com/oxygen/weather/core/provider/metno/**'`
- Static app-leakage check: `rg -n "FallbackWeatherRepository|MetNoWeatherRepository|MetNoForecast|MetNoHttp|MET Norway.*active|active.*MET Norway|current.*MET Norway|symbolCode|X-ErrorClass" app/src/main/kotlin README.md DATA_SOURCES.md PRIVACY.md`
- Save focused test and static-check logs under `.codex/test-artifacts/2026-08-23-forecast-fallback-selection/` and record the project-local paths in Phase Results before reporting the slice ready.

Real-path command or procedure:
- None required. This repository-selection slice is verified with fake provider repositories and provider-neutral fixtures/results only. Do not describe final evidence as live Open-Meteo, live MET Norway, Home UI, or active app fallback behavior.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
- Save broad verification logs under `.codex/test-artifacts/2026-08-23-forecast-fallback-selection/` and record the project-local paths in Phase Results before reporting the slice ready.

Current gate: planned
Current phase: ready-to-implement
Last result: Slice 14 selected as the next bounded implementation slice after verified Slice 13D. The plan confines work to provider-neutral forecast fallback selection and explicitly excludes UI, cache, disclosure, saved-location, alert, unit, and live-provider claims.
Blocker: none.

## Implementation Plan

1. Add failing/covering core tests for fallback selection using fake `WeatherRepository` implementations: loading behavior, exact location propagation, Open-Meteo success without MET Norway call, eligible Open-Meteo failures triggering exactly one MET Norway attempt, non-eligible Open-Meteo failures returning immediately, MET Norway success provenance, both-provider failure top-level `error` equal to the MET Norway/fallback terminal error, ordered diagnostics containing Open-Meteo/default failure then MET Norway/fallback failure, no recursive retry behavior within one refresh, and no retained automatic retry state across two refresh calls including distinct locations.
2. Add the non-breaking provider-neutral diagnostics field to `WeatherRepositoryResult.Failure`, defaulting to `listOf(error)`, and update only tests/call sites required by the compiler while preserving existing app retry behavior and avoiding provider-specific UI copy.
3. Implement the minimal fallback repository by composing two `WeatherRepository` instances and a local fallback-eligibility predicate. Consume child repository terminal results, suppress child loading emissions, and return one terminal result.
4. Run focused fallback repository tests and static provider/app leakage checks, including checks for provider-specific repository imports or package references outside provider-specific production packages.
5. Run broad Android verification commands and `git diff --check`.
6. Record only command-backed evidence in `.codex/plans/current.md`; append `.codex/cycles/history.md` only when the slice is actually verified or committed.

## Known Non-Blocking Drift

- `README.md` still contains scaffold-era wording that says no network weather provider has been wired yet and the screen displays `SampleWeather.bundle`. Do not fix it in Slice 14 because this cycle intentionally avoids unrelated disclosure/documentation changes.
- Prior MET Norway slices are verified but not recorded as committed in `.codex/cycles/history.md`. Do not rewrite history or claim committed status without an actual commit.

## Phase Results

- planned: Selected Slice 14 as the next bounded implementation slice after verified Slice 13D. Acceptance is limited to deterministic core repository fallback selection with fake provider repositories and provider-neutral state.
- planned-review-resolved: Incorporated `.codex/review/findings.md` recommendations by tightening retry-loop proof across repeated refresh calls, selecting the additive `WeatherRepositoryResult.Failure.diagnostics` result-surface change, removing the default-constructor step, extending static provider-boundary checks to provider-specific repository/package references, and defining retryable as the existing repository failure path for this slice.
- planned-review-resolved: Removed both-provider failure ambiguity without changing the existing `Failure.error` contract: Slice 14 now requires the top-level error to be the MET Norway/fallback terminal error and diagnostics to be ordered by attempted provider, Open-Meteo/default first and MET Norway/fallback second.
