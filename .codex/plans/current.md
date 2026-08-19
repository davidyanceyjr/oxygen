# Active Cycle

Status: committed
Cycle ID: 2026-08-19-open-meteo-provider-contract
Mode: documentation-only
Goal: Specify the default Open-Meteo forecast provider contract before code is added.
Roadmap slice: Slice 1: Open-Meteo Provider Contract from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors: `docs/OXYGEN_FULL_SPECIFICATION.md`
Acceptance criteria:
- The provider contract completes every field in `docs/data-sources/PROVIDER_TEMPLATE.md`, including endpoint, authentication, required headers, request/rate limits, caching rules, fields used, time/unit format, weather-code mapping, error responses, attribution, license, privacy implications, failover behavior, fixture locations, official documentation, and last terms review date.
- Contracted fields support Home current, hourly, daily, metrics, sun/update/source, provenance, and stale UI needs.
- Open-Meteo current-condition values are labeled model estimates unless provider documentation proves otherwise.
- Provider-specific fields are separated from provider-neutral Oxygen semantics.
Acceptance boundary: `docs/data-sources/OPEN_METEO_FORECAST.md` specifies the Open-Meteo forecast provider contract and does not mark a production provider path as implemented.
In scope: Open-Meteo forecast provider documentation, terms/license/source review, active-cycle evidence, and cycle history.
Out of scope: Kotlin DTOs, parser tests, weather-code mapper code, HTTP client code, repository/UI wiring, fixtures, Room persistence, geocoding, alerts, and release-ready provider activation.
Focused test command: documentation review against Open-Meteo docs, terms, license, pricing, and Oxygen provider template.
Real-path command or procedure: primary-source documentation review using Open-Meteo forecast docs, terms/privacy, license, and pricing pages on 2026-08-19.
Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
Current gate: ready
Current phase: committed
Last result: Open-Meteo provider contract added, verified against provider-template requirements and primary Open-Meteo documentation, and committed in the current HEAD.
Blocker: none
Next phase: Slice 2 planning

## Phase Results

- discover: Read required repository authorities and confirmed no nested `AGENTS.md` applies.
- contract: Selected Slice 1 only; implementation must remain documentation-only.
- implemented: Added `docs/data-sources/OPEN_METEO_FORECAST.md` with endpoint, authentication, headers, request/rate limits, caching rules, fields, time/unit format, weather-code mapping, error responses, attribution, license, privacy, failover behavior, fixture location, official documentation, and last terms review date.
- focused review: Reviewed against `docs/data-sources/PROVIDER_TEMPLATE.md`, `docs/OXYGEN_FULL_SPECIFICATION.md`, and Open-Meteo forecast docs, terms/privacy, license, and pricing pages on 2026-08-19.
- real-path documentation exercise: `curl -I -L --max-time 20 https://open-meteo.com/en/docs`, `https://open-meteo.com/en/terms`, `https://open-meteo.com/en/licence`, and `https://open-meteo.com/en/pricing` all returned HTTP 200 on 2026-08-19.
- broad checks: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed with both test tasks `NO-SOURCE`; `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed; `git diff --check` passed.
- committed: current HEAD (`Document Open-Meteo forecast contract`).
