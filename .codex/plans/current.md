# Active Cycle

Status: committed
Cycle ID: 2026-08-19-open-meteo-geocoding-fixtures-mapping
Mode: feature
Goal: Parse Open-Meteo geocoding fixtures and map valid results into provider-neutral Oxygen location models while rejecting invalid required fields with explicit domain errors.
Roadmap slice: Slice 7: Geocoding Fixtures and Domain Mapping from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `docs/data-sources/OPEN_METEO_GEOCODING.md`
- `docs/data-sources/PROVIDER_TEMPLATE.md`
- `.codex/plans/mvp-roadmap.md`
Acceptance criteria:
- Fixtures cover normal, empty, ambiguous, bounded postal-code/place query where supported by provider response shape, malformed, missing-optional, invalid-coordinate, invalid-timezone, and provider-error cases under `core/src/test/resources/providers/openmeteo/geocoding/`.
- Parser reads only the contracted Open-Meteo geocoding fields needed for MVP manual location search and keeps provider DTOs inside the Open-Meteo implementation package.
- Mapper returns provider-neutral selectable location data with display name, `GeoPoint`, validated IANA `ZoneId`, country, country code, administrative area data where present, optional elevation in meters, and a `WeatherLocation` suitable for later Home handoff.
- Stable local `LocationId` values are generated from normalized provider-neutral location fields: display name, country code, administrative area labels, latitude/longitude rounded to a documented precision, and timezone. Provider `id`, admin provider IDs, provider ranking, and result array index are not ID inputs.
- Ambiguous places remain distinct through admin/country/coordinate/timezone data and do not collapse to provider IDs.
- Missing optional fields remain absent instead of being fabricated.
- Invalid required fields, malformed envelopes, provider error bodies, invalid coordinates, and invalid timezones map to explicit geocoding domain errors at the parser/mapper boundary: invalid JSON/envelope, provider error body, missing required field, invalid field value, invalid coordinate, and invalid timezone.
Acceptance boundary: Slice 7 is complete when fixture-backed `:core` tests exercise the production Open-Meteo geocoding parser and mapper, proving valid fixture mapping and invalid fixture error classification without live internet. This slice does not add a production geocoding HTTP client, repository boundary, Compose search UI, first-run flow, saved-location persistence, active provider disclosure, or live provider calls.
In scope:
- Add Open-Meteo geocoding DTO/parser code in `:core`.
- Add provider-neutral geocoding candidate/result/error types only as needed for parser/mapper behavior, including country/admin data required by the roadmap.
- Add Open-Meteo geocoding mapper code that constructs selectable provider-neutral location candidates and their embedded `WeatherLocation` values.
- Add fixture-backed parser and mapper unit tests for the accepted cases.
- Keep provider IDs as implementation metadata only; do not expose them as `LocationId`.
Out of scope:
- Network transport, query construction, debounce/cancel behavior, rate-limit handling from HTTP status, repository loading/success/error state, UI search/results, first-run navigation, persistence/cache, live API calls, emulator/manual verification, and any change that presents geocoding as active in the app.
Focused review command or procedure:
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*Geocoding*'`
Real-path command or procedure:
- Fixture-backed production parser/mapper tests in `:core`; no live geocoding provider call is part of this slice.
Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
Current gate: ready
Current phase: committed
Last result: Slice 7 fixture-backed Open-Meteo geocoding parser and mapper are implemented in `:core`. Focused geocoding tests and broad Android checks passed; no live provider call, client, repository, UI, persistence, or active-provider disclosure was added. Commit is current HEAD.
Blocker: none
Next phase: plan Slice 8

## Implementation Plan

1. Discover the existing forecast parser/mapper/test patterns and current `WeatherLocation` model shape.
2. Add geocoding fixtures for normal, empty, ambiguous, bounded postal-code/place query where supported, missing-optional, malformed, invalid-coordinate, invalid-timezone, and provider-error responses.
3. Add failing focused tests for Open-Meteo geocoding parsing, mapping, country/admin preservation, stable local `LocationId` generation from documented provider-neutral inputs, ambiguity preservation, optional-field null preservation, and explicit error classification.
4. Implement the smallest `:core` parser/DTO/mapper/candidate/error surface needed to satisfy those tests.
5. Run focused geocoding tests, broad Android checks, `git diff --check`, review the diff for scope, and update phase evidence.

## Phase Results

- planned: Selected Slice 7 from `.codex/plans/mvp-roadmap.md`. Planned scope is limited to fixture-backed Open-Meteo geocoding parser and mapper behavior in `:core`; no network client, repository, UI, persistence, live API call, or active-provider disclosure is included.
- covered: Added fixture-backed parser and mapper tests for normal, empty, ambiguous, bounded postal-code/place query shape, missing optional, malformed envelope, invalid required field, invalid coordinate, invalid timezone, and provider error cases. Initial focused run failed at `:core:compileDebugUnitTestKotlin` because the geocoding production parser/mapper types did not exist yet.
- implemented: Added provider-neutral `GeocodingLocationCandidate`; Open-Meteo geocoding DTOs/parser; and mapper producing selectable location candidates with embedded `WeatherLocation`, country/admin data, optional elevation, validated coordinates/timezone, and stable local `LocationId` values generated from normalized provider-neutral fields with coordinates rounded to 4 decimal places.
- verified: `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*Geocoding*'` passed. `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed. `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed with `:app:testDebugUnitTest` `NO-SOURCE` and `:core:testDebugUnitTest` executed. `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed. `git diff --check` passed.
- committed: `current HEAD` (`Add Open-Meteo geocoding mapping`) created locally.
