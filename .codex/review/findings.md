# Slice 16 Plan Review Findings

## Review Metadata

- Review date: 2026-08-26
- Reviewer: Codex
- Repository: `/home/opsman/project_git/oxygen`
- Reviewed cycle: `2026-08-26-cache-one-forecast-bundle`
- Scope: Pre-implementation review of `.codex/plans/current.md` for gaps, blockers, and LLM slop
- Build/test commands run: none; this review updates review notes only

## Findings

1. `.codex/plans/current.md:75` leaves a slop opening around storage. The spec and roadmap point at Room as the intended local source of truth (`docs/OXYGEN_FULL_SPECIFICATION.md:495`, `.codex/plans/mvp-roadmap.md:339`), but the plan allows a fallback "narrow storage interface" without saying it must be durable production storage. That could let an in-memory fake pass repository tests while not implementing cache persistence.

   Recommendation: tighten this to Room unless blocked. If Room is blocked, the fallback must still be durable local production storage, not an in-memory test store, and the blocker must be recorded explicitly.

2. `.codex/plans/current.md:23` requires "provider cache metadata inputs," but the current domain surface only exposes `DataProvenance` fields at `core/src/main/kotlin/com/oxygen/weather/core/model/WeatherModels.kt:52` and `WeatherRepositoryResult.Success(weather)` at `core/src/main/kotlin/com/oxygen/weather/core/provider/WeatherProviders.kt:62`. HTTP cache headers are not available at that boundary. The plan avoids fabrication, which is good, but the metadata contract is still too vague.

   Recommendation: define the Slice 16 metadata record explicitly as provider ID, source name, license ID, data type, issued time, fetched time, plus nullable HTTP/cache fields that remain null until provider clients expose richer values. Tests should prove null preservation, not vague metadata existence.

3. `.codex/plans/current.md:24` and `.codex/plans/current.md:77` are mildly ambiguous about "production repository path." App default wiring is still direct `OpenMeteoWeatherRepository()` at `app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt:21`. If Slice 16 keeps app wiring unchanged, it must not imply installed-app Open-Meteo success is cache-wrapped.

   Recommendation: either wire the cache wrapper into app default forecast behavior and produce installed-app evidence, or explicitly state that the cache wrapper is production core repository code exercised by a live repository/state-holder path while installed-app cache behavior remains unimplemented.

4. `.codex/plans/current.md:49` hardcodes static-check paths `core/src/main/kotlin/com/oxygen/weather/core/cache` and `core/src/main/kotlin/com/oxygen/weather/core/storage`. If implementation chooses different package names, the check can miss leaks or fail on missing paths.

   Recommendation: decide package names before implementation or define the static check against the actual created cache/storage production paths plus all app production Kotlin.

5. `.codex/review/findings.md` previously contained Slice 15 review findings while the active cycle is Slice 16. This is not a feature blocker because `.codex/plans/current.md:92` called it unrelated, but it is status drift if review findings are treated as current evidence.

   Recommendation: replace the review findings with this Slice 16 review, and keep future review files aligned with the active reviewed cycle or clearly archive older findings by cycle ID.

## Blockers

No hard blocker found. The active plan is mostly well bounded: it excludes offline launch, failed-refresh retention, saved locations, unit preferences, alerts, air quality, radar, background work, release behavior, and active installed-app MET Norway fallback.

## Summary

The main LLM-slop risk is passing tests around a cache-shaped abstraction without proving a real persisted forecast path. Clarify durable storage, define exact nullable cache metadata, and make the app-wiring claim precise before implementation.
