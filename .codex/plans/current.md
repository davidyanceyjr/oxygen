# Active Cycle

Status: committed
Cycle ID: 2026-08-19-open-meteo-geocoding-contract
Mode: documentation-only
Goal: Specify the MVP geocoding provider contract before adding geocoding code, while preserving provider replaceability and avoiding public Nominatim as the only production autocomplete backend.
Roadmap slice: Slice 6: Geocoding Provider Contract from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `docs/data-sources/PROVIDER_TEMPLATE.md`
- `.codex/plans/mvp-roadmap.md`
Acceptance criteria:
- The provider contract completes every field in `docs/data-sources/PROVIDER_TEMPLATE.md`, including endpoint, authentication, required headers, request/rate limits, caching rules, fields used, time/unit format, error responses, attribution, license, privacy implications, failover behavior, fixture locations, official documentation, and last terms review date.
- Provider fields support place search, coordinates, timezone, country, administrative area, and optional elevation.
- Provider identifiers are not user-facing `LocationId` values.
- The contract avoids making a public OSM Nominatim server the only production autocomplete backend.
Acceptance boundary: `docs/data-sources/OPEN_METEO_GEOCODING.md` exists as a provider contract for the initial MVP geocoding provider. This documentation-only slice does not add geocoding production code, fixtures, parser/mapper code, repository behavior, UI search, saved locations, provider activation, or release/user-facing data-source disclosure.
In scope:
- Review current official Open-Meteo geocoding documentation, Open-Meteo terms/privacy/license/pricing, GeoNames license/attribution material, and OSM public Nominatim policy constraints.
- Document endpoint shape, authentication, rate limits, caching expectations, response fields, validation requirements, errors, attribution/license, privacy implications, and future fixture locations.
- State that provider IDs are provider metadata only and Oxygen owns stable local `LocationId` values.
- State that public OSM Nominatim must not be the only production autocomplete backend.
Out of scope:
- Kotlin geocoding models, DTOs, parser/mapper, client, repository, UI state, Compose search, first-run location flow, persistence/cache implementation, live API calls, emulator/manual verification, provider activation, and root privacy/data-source release documents.
Focused review command or procedure:
- Review `docs/data-sources/OPEN_METEO_GEOCODING.md` against `docs/data-sources/PROVIDER_TEMPLATE.md`, `docs/OXYGEN_FULL_SPECIFICATION.md`, official Open-Meteo geocoding docs/terms/license/pricing, GeoNames attribution/license information, and OSM public Nominatim policy.
Real-path command or procedure:
- Documentation-only slice; no production path exists or is exercised.
Broad verification commands:
- `git diff --check`
Current gate: ready
Current phase: committed
Last result: Slice 6 provider contract added in `docs/data-sources/OPEN_METEO_GEOCODING.md`; official provider/policy pages browsed, reviewed against the template/spec/roadmap, `git diff --check` passed, and the work was committed at current HEAD.
Blocker: none
Next phase: plan Slice 7

## Implementation Plan

1. Confirm the current specification and roadmap still require a geocoding provider contract before code.
2. Browse official/current provider sources for Open-Meteo geocoding, terms/privacy, license, pricing/rate limits, GeoNames attribution/license material, and OSM public Nominatim policy.
3. Add `docs/data-sources/OPEN_METEO_GEOCODING.md` using every field from the provider template.
4. Ensure the contract covers place search, coordinates, timezone, country, administrative area, optional elevation, provider-neutral `LocationId`, privacy, attribution, and replaceability.
5. Run `git diff --check`, review the diff for scope, update phase evidence, and append cycle history when ready.

## Phase Results

- planned: Selected Slice 6 from `.codex/plans/mvp-roadmap.md` after Slice 5 was committed. Planned acceptance boundary is an Open-Meteo geocoding provider contract only, with no production geocoding code or UI activation.
- verified: Added `docs/data-sources/OPEN_METEO_GEOCODING.md` with every provider-template field, covering place search fields, coordinates, timezone, country/admin data, optional elevation, provider-neutral local `LocationId`, attribution/license, privacy, fixture locations, official documentation, and Nominatim public-server autocomplete constraints. Official source HEAD checks for Open-Meteo geocoding docs, Open-Meteo terms, Open-Meteo license, GeoNames about/license summary, and OSM public Nominatim policy returned HTTP 200. `git diff --check` passed. Android build/test commands were not run because this documentation-only slice changed no production or test code.
- committed: `current HEAD` (`Add Open-Meteo geocoding contract`) created locally.
