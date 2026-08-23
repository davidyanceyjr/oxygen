# Active Cycle

Status: verified
Cycle ID: 2026-08-23-met-norway-provider-contract
Mode: documentation-only
Goal: Specify the MET Norway Locationforecast provider contract for Oxygen's MVP forecast fallback before any MET Norway DTO, client, mapper, repository, fallback, cache, UI, or disclosure-active behavior is implemented.
Roadmap slice: Slice 12: MET Norway Provider Contract from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 1, 4, 5, 6, 12, 16, 17, 39, 40, 44, 46, and 48
- `.codex/plans/mvp-roadmap.md` Slice 12, plus fallback constraints in the Forecast Provider Scope and Release Gate
- `docs/data-sources/PROVIDER_TEMPLATE.md`
- `docs/data-sources/OPEN_METEO_FORECAST.md` failover boundary
- `AGENTS.md`

Pre-implementation authority gate:
- Slice 11 provider-backed Home success is recorded in `.codex/cycles/history.md` as verified and user accepted, but not committed. Slice 12 may proceed because it is a documentation-only provider-contract slice and does not depend on uncommitted Slice 11 code changes.
- README provider-status text is known stale: it still says no network weather provider has been wired, while `DATA_SOURCES.md` and cycle history record active Open-Meteo forecast/geocoding production paths. Do not use that stale README sentence to block Slice 12; resolve it in a separate corrective documentation slice or when the README is next deliberately touched.
- Confirm no nested `AGENTS.md` changes the rules for `docs/data-sources/`.
- Confirm MET Norway is still listed only as a specified roadmap fallback, not an active/current provider, in root disclosure documents before and after this slice.

Acceptance criteria:
- Create `docs/data-sources/MET_NORWAY_FORECAST.md` from `docs/data-sources/PROVIDER_TEMPLATE.md` with every template field completed.
- The contract identifies MET Norway Locationforecast as the specified MVP forecast fallback, not an active fallback implementation.
- The contract names the production endpoint, authentication model, required identifying `User-Agent` or header identity, request/rate-limit expectations, caching requirements, cache-header handling, attribution, license, privacy implications, official documentation, and last terms review date.
- The contract defines Home-path fields needed for current, hourly, daily, metric, sun/update/source, provenance, and stale/cache UI needs.
- The contract defines MET Norway time and unit semantics, including UTC/local timestamp handling and canonical Oxygen unit mapping.
- The contract defines weather-symbol mapping obligations to Oxygen's provider-neutral `WeatherCondition` taxonomy, including unknown-symbol fallback.
- The contract defines error and retry classifications needed by later client/repository work: network/offline, rate limited where detectable, provider unavailable, invalid response, unsupported or insufficient forecast data where applicable, and cache-not-modified behavior where applicable.
- The contract defines fallback behavior without averaging, merging, smoothing, or mixing MET Norway values with Open-Meteo values. Provenance must identify whichever provider served displayed data.
- MET Norway-specific product names, symbol codes, endpoint details, HTTP behavior, and provider error bodies remain contract/provider concerns; they must not be described as UI, Compose, Home state, saved-location, units, or cache-consumer inputs.
- Root disclosure files may mention MET Norway only as a specified roadmap provider unless production fallback behavior has already been implemented and verified in a later slice.
- The slice does not claim MET Norway can fetch, parse, map, cache, or serve production weather data.

Acceptance boundary: Slice 12 is complete when `docs/data-sources/MET_NORWAY_FORECAST.md` exists, completes the provider template, is reviewed against the Oxygen specification, roadmap fallback constraints, Open-Meteo failover boundary, and primary MET Norway documentation/terms/license/caching guidance, and records source-review evidence. Slice 12 is not verified by creating code stubs, adding dependencies, adding provider interfaces that do not fetch data, updating active-provider disclosures, or saying fallback exists.

Boundary decisions:
- This is documentation-only. Do not edit production Kotlin, tests, Gradle files, manifests, resources, screenshots, or app UI for this slice unless a higher-authority conflict makes the contract impossible without a documented correction.
- Do not add MET Norway fixtures in this slice. Fixtures belong to Slice 13A.
- Do not add MET Norway DTOs, parser code, mapper code, transport code, repository code, fallback-selection code, cache schema, Room/DataStore code, WorkManager code, or UI states.
- Do not update `DATA_SOURCES.md` to list MET Norway as active/current. If touched at all, it may only preserve or clarify roadmap-only status.
- If provider terms, attribution, caching, or required header guidance conflict with the roadmap or specification, stop and record the exact conflict before coding.
- Use primary sources for provider facts. If a primary source is unavailable, record the exact unavailable source and avoid filling contract fields with guesses.

In scope:
- Review MET Norway official API documentation, Locationforecast documentation, terms of service, license/attribution guidance, caching guidance, required identification/User-Agent guidance, response examples/schema guidance, and weather-symbol documentation.
- Review Oxygen authorities and existing Open-Meteo forecast contract to keep fallback semantics compatible without copying provider-specific Open-Meteo assumptions.
- Add the MET Norway forecast provider contract under `docs/data-sources/`.
- Record exact official source URLs and review date in the contract.
- Run documentation-focused checks and record evidence under `.codex/test-artifacts/2026-08-23-met-norway-provider-contract/`. `source-checks.log` must record each reviewed official URL, HTTP result, provider page title or section, contract fields supported, review notes, and review date.
- Update `.codex/cycles/history.md` only after the contract has passed review and checks.

Out of scope:
- Any implementation or test that suggests MET Norway production fallback behavior exists.
- Active provider disclosure changes, release-candidate claims, fallback selection, provider health/backoff, cache writes, stale-cache display, offline launch, saved locations, units, alerts, radar, air quality, maps, settings/about UI, or screenshots.

Focused review command or procedure:
- Review `docs/data-sources/MET_NORWAY_FORECAST.md` against every field in `docs/data-sources/PROVIDER_TEMPLATE.md`.
- Review the contract against `docs/OXYGEN_FULL_SPECIFICATION.md` sections 5, 6.2, 6.3, 12, 16, 17, 39, 40, 44, 46, and 48.
- Review the contract against `.codex/plans/mvp-roadmap.md` Slice 12 and the roadmap Forecast Provider Scope.
- Review `docs/data-sources/OPEN_METEO_FORECAST.md` to ensure the fallback boundary remains consistent and no averaging/merging is introduced.
- Use primary-source HTTP checks for each official MET Norway source cited by the contract, saving the command output to `.codex/test-artifacts/2026-08-23-met-norway-provider-contract/source-checks.log`.
- Static no-implementation check, including untracked files: `git status --short | rg -v '^( M|A |\\?\\?) (DATA_SOURCES.md|docs/data-sources/MET_NORWAY_FORECAST.md|\\.codex/plans/current.md|\\.codex/cycles/history.md|\\.codex/test-artifacts/2026-08-23-met-norway-provider-contract/)'`
- Static no-active-provider-disclosure check: `rg -n "active.*MET Norway|MET Norway.*active|current.*MET Norway|MET Norway.*current" README.md DATA_SOURCES.md PRIVACY.md NOTICE THIRD_PARTY_LICENSES.md LICENSE`
- Static roadmap-only disclosure check: `rg -n "MET Norway.*roadmap|MET Norway.*specified|specified roadmap.*MET Norway|MET Norway.*not active" README.md DATA_SOURCES.md PRIVACY.md`

Real-path command or procedure:
- None. This is a documentation-only provider-contract slice. Do not perform live weather fetches as proof of implementation. Primary-source reachability checks are review evidence, not product behavior.

Broad verification commands:
- `git diff --check`
- Android build/test commands are not required for this documentation-only slice unless files outside documentation/planning/history are changed. If any production, test, Gradle, manifest, resource, or script file changes, run:
  - `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
  - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
  - `. scripts/android-env.sh && ./gradlew :app:assembleDebug`

Current gate: verified
Current phase: ready
Last result: Created `docs/data-sources/MET_NORWAY_FORECAST.md` as a documentation-only provider contract, reviewed it against the provider template, Oxygen specification, roadmap fallback constraints, Open-Meteo failover boundary, and primary MET Norway sources. Source-review evidence is saved at `.codex/test-artifacts/2026-08-23-met-norway-provider-contract/source-checks.log`.
Blocker: none.

## Implementation Plan

1. Gather MET Norway primary sources: API overview, Locationforecast endpoint docs, terms of service, license/attribution, required identifying headers/User-Agent, caching headers/guidance, response fields, and weather-symbol documentation.
2. Create `.codex/test-artifacts/2026-08-23-met-norway-provider-contract/` and save source reachability/review notes there.
3. Draft `docs/data-sources/MET_NORWAY_FORECAST.md` from `PROVIDER_TEMPLATE.md`, completing every field with cited, primary-source-backed facts or explicit unknown/unsupported notes where the provider docs do not specify a detail.
4. Define the fallback contract boundary: Open-Meteo remains default, MET Norway is a future fallback, values are not averaged or merged, and provenance must identify the serving provider.
5. Define mapping obligations only at the contract level: Locationforecast fields needed for Home, canonical units, timestamp handling, weather-symbol mapping, provenance, cache metadata, and null-preservation expectations.
6. Run focused review/static checks and `git diff --check`; run Android broad checks only if non-documentation files changed.
7. Review the diff for SLOP risks: no code stubs, no fake provider success, no active-fallback disclosure, no release claims, no unverified cache/fallback behavior, no provider-specific leakage into UI claims.
8. Update phase results and append factual cycle evidence to `.codex/cycles/history.md` only after the documentation contract and checks are complete.

## Phase Results

- planned: Selected Slice 12 from `.codex/plans/mvp-roadmap.md` after reviewing the specification, provider template, current cycle state, cycle history, build files, and roadmap. The plan is documentation-only.
- unblocked: Slice 11 provider-backed Home success is recorded in cycle history as verified and user accepted, though not committed. README provider-status text is stale and must not be used as Slice 12 provider-status evidence.
- source-reviewed: Primary-source HTTP checks for MET Weather API overview, Getting Started, Locationforecast docs, HOWTO, data model, Forecast JSON, OpenAPI schema, interface/errors, terms, FAQ, Locationforecast FAQ, license, MET privacy statement, and weathericons returned HTTP 200. Evidence saved at `.codex/test-artifacts/2026-08-23-met-norway-provider-contract/source-checks.log`.
- contract-created: Added `docs/data-sources/MET_NORWAY_FORECAST.md` from the provider template with completed fields for endpoint, authentication, required User-Agent/header identity, request/rate expectations, caching, Home-path fields, UTC/unit semantics, symbol mapping obligations, errors/retry/cache-not-modified handling, attribution, license, privacy, fallback behavior, fixture location, official docs, and last terms review date.
- boundary-reviewed: No Kotlin, Gradle, manifest, resource, fixture, active-provider disclosure, fallback-selection, cache schema, or UI files were changed for this documentation-only slice. `DATA_SOURCES.md` was narrowly clarified to keep MET Norway explicitly roadmap-only for the planned static disclosure check.
