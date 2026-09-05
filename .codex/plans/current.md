# Slice 22 Plan — NWS Alert Provider Contract

**Status:** committed — contract, source review, Markdown verification, and PR merge complete
**Cycle ID:** `2026-09-05-slice-22-nws-alert-provider-contract`
**Mode:** documentation / provider contract

## Selected Behavior and Acceptance Boundary

Add `docs/data-sources/NWS_ALERTS.md` as the sourced NOAA/National Weather
Service contract for selected-point official alerts. It must let Slices
23A–23C implement parsing, provider errors, and forecast/alert composition
without guessing, while keeping NWS roadmap-only and changing no runtime
behavior.

Slice 22 is ready when the contract covers every Slice 22 roadmap obligation,
records the decisions below, passes the bounded provider/source review and
Markdown checks, and contains no Kotlin, Gradle, resource, manifest,
persistence, permission, dependency, or UI change.

## Verified Prerequisites and Current Boundary

- `HEAD` is `3ea5ae6`; Slice 21 implementation and verification are committed
  in `c6febb6` and `3ea5ae6`.
- Room is the installed forecast persistence boundary. Forecast fallback is
  composed independently and must remain independent from alert lookup.
- `AlertProvider`, `WeatherAlert`, `AlertSeverity`,
  `DataType.OFFICIAL_ALERT`, `WeatherBundle.alerts`, and provenance already
  exist in `:core`.
- `WeatherAlert` can reuse ID, event, headline, severity, effective/expires,
  description, instruction, issuer, and provenance. It lacks urgency,
  certainty, affected-area/geometry, onset/end, sent time, message type,
  status, and update references.
- `AlertProvider.getActiveAlerts` returns only `List<WeatherAlert>` and cannot
  distinguish an empty success, unsupported coverage, or failures.
- Home maps and renders scaffold alerts through `HomeAlertPresentation` and
  `home-section-alert`; this is neither a live provider path nor Slice 24.
- Alert persistence does not exist. Room explicitly rejects alert-bearing
  forecast bundles; the retained file forecast format omits alerts. Slices 23
  must not route alert persistence through either forecast-cache boundary.
- No NWS DTO/parser/client/repository, alert result/error type, alert cache, or
  installed composition path exists.

The roadmap still labels the Persistence Architecture Gate and Slice 21
`specified`, and recent-history summary/last-cycle commit state predates the
commits above. Current code and Git history satisfy Slice 22 prerequisites.
Treat roadmap/history correction as a separate authority-sync task; do not
silently include it in the provider-contract diff.

## Decisions That Resolve Review Blockers

### Existing Domain and Result Boundaries

The NWS contract will inventory reusable and missing provider-neutral fields.
Slice 23A must expand the existing `WeatherAlert`/related enums to retain the
required semantics; it must not introduce a parallel alert hierarchy. Slice
23B must replace or evolve the existing `AlertProvider` return boundary so a
successful empty result remains distinct from `UnsupportedRegion`, network,
rate-limit, provider, invalid-request, and invalid-response outcomes. Slice
23C owns repository composition and independent forecast/alert freshness.
Existing names remain authoritative unless a higher-authority change explicitly
replaces them.

### Coverage and Unsupported Region

Coverage means the point coverage accepted by the NWS active-alert service,
not CONUS, a country-code test, or the selected forecast provider. Oxygen will
attempt any locally valid coordinate, including US states, District of
Columbia, NWS-served territories, and relevant coastal/offshore or other marine
points; provider acceptance owns the exact evolving boundary.

Slice 23B must first reject non-finite coordinates or latitude outside
`[-90, 90]` / longitude outside `[-180, 180]` as local invalid input. For a
locally valid point:

- HTTP 200 with a valid `FeatureCollection`, including zero features, is a
  supported success.
- `UnsupportedRegion` requires HTTP 400 `application/problem+json`, problem
  type `https://api.weather.gov/problems/InvalidParameter`, title
  `Invalid Parameter`, and detail `Parameter "point" is invalid: out of bounds`.
- Every other 400 is `InvalidRequest`, or `InvalidResponse` if its declared
  problem envelope is malformed.

The OpenAPI currently exposes only a generic error response and does not
promise that out-of-bounds discriminator. The narrow rule above is therefore a
dated, conservative Oxygen interpretation of observed behavior, not a provider
guarantee. Preserve diagnostics and add a re-review trigger; never infer
support from an empty list or forecast routing.

### Canonical Request Identity

The exact initial identity is:

```text
User-Agent: OxygenWeather/0.1 (https://github.com/davidyanceyjr/oxygen/issues)
Accept: application/geo+json
```

The contact is the issue tracker for the repository named by `git origin`.
Keep it provider-local/configurable and review it when repository ownership or
the application version changes. Do not copy the mismatched MET Norway default
URL or invent an email/API-key path.

## Contract Coverage

| Roadmap obligation | Required contract decision/evidence |
| --- | --- |
| Endpoint/auth | `GET https://api.weather.gov/alerts/active?point={lat},{lon}`; no current API key; exact headers above. |
| Rate/requests | No published numeric quota; NWS recommends no more often than 30 seconds. No recomposition polling, tight retries, or request storms; back off rate limits. |
| Caching | Record `Cache-Control`, `Expires`, `ETag`, and `Last-Modified` only when present/useful. Response freshness never dictates Oxygen polling cadence; invent no TTL. |
| Fields/UI needs | Inventory existing fields and contract only identity, lifecycle, severity/urgency/certainty, affected area/geometry, provenance, diagnostics, and specified banner/detail needs. No presentation strings in domain/provider types. |
| Timestamps | Parse absolute instants and keep sent/effective/onset/expires/end distinct; optional values stay absent; presentation uses selected-location timezone. |
| CAP values | Preserve severity, urgency, and certainty separately with `UNKNOWN` fallback; do not synthesize danger scores. |
| Identity/lifecycle | Treat provider ID as opaque; deduplicate identical IDs. Define CAP Alert/Update/Cancel and references for mapper robustness without claiming the active snapshot returns prior or cancellation messages. |
| Expiration | Filter expired/superseded/cancelled cached input. The NWS geolocation guide, not a live observation, supports active results that are ongoing or near-future-effective. |
| Area/geometry | Preserve valid geometry, zones/geocodes, and area text when present; null geometry remains usable for a point-filtered result; fabricate nothing. |
| Outcomes/errors | Use the coverage rule above and classify empty success, unsupported, offline/network, rate limit, unavailable, invalid request/response, identification rejection, and other HTTP failure separately. Raw provider copy is diagnostic only. |
| Reuse/privacy | Record public-domain/disclaimer limits, source attribution/provenance, selected coordinates, IP/network metadata, and the identifying User-Agent sent to NWS. No new permission or collection is introduced. |
| Independence | Official alerts never derive from forecast risk; forecast choice/fallback cannot disable lookup; alert failure cannot invalidate forecast; freshness stays separate. |
| Fixtures | Assign no/one/many, missing optionals, unknown enums, geometry/null geometry, duplicates, update/cancel references, near-future-effective, expired, and malformed-envelope fixtures to Slice 23A. |

## Source Boundaries

Review and date each normative statement in the provider contract against:

- NWS API service docs and OpenAPI for base URL, identification, media type,
  point parameter, response schema/enums, generic error envelope, rate, and
  cache orientation: <https://www.weather.gov/documentation/services-web-api>
  and <https://api.weather.gov/openapi.json>.
- NWS Alerts Web Service for alert purpose, point lookup, 30-second guidance,
  and NWS CAP context:
  <https://www.weather.gov/documentation/services-web-alerts>.
- NWS Geolocation Guide for point lookup and ongoing/near-future active scope:
  <https://www.weather.gov/media/documentation/docs/NWS_Geolocation.pdf>.
- NWS CAP documentation first, and OASIS CAP 1.2 only where NWS delegates
  semantic definitions: <https://www.weather.gov/alerting> and
  <https://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2.html>.
- NWS disclaimer and privacy pages for reuse, attribution, disclaimers, and
  network-data handling: <https://www.weather.gov/disclaimer> and
  <https://www.weather.gov/privacy>.
- Exact PNS26-62 notice for the proposed CAP-primary/VTEC change:
  <https://www.weather.gov/media/notification/pdf_2026/PNS26-62_CAP_Transition.pdf>.

State `not publicly specified` and choose conservative Oxygen behavior where
official documentation is silent. Label bounded live responses as dated
observations, never fixtures or guaranteed provider behavior. Re-review service
notices immediately before Slice 23.

## Files and Workflow

Intended changes:

- add `docs/data-sources/NWS_ALERTS.md`;
- update `.codex/plans/current.md` only as execution evidence changes;
- amend the specification only for a demonstrated higher-authority gap.

Keep `DATA_SOURCES.md` roadmap-only and `PRIVACY.md` unchanged because no live
NWS path exists. Use `discover -> contract/document -> review -> ready`.

Do not add production code, dependencies, alert persistence, background work,
notifications, UI, VTEC coupling, global routing, active-provider claims, or
runtime verification claims. Those belong to Slices 23A–23C, 24, or later.

## Execution Evidence

Cycle artifacts:
`.codex/test-artifacts/2026-09-05-slice-22-nws-alert-provider-contract/`.

Added `docs/data-sources/NWS_ALERTS.md` as the provider contract. No Kotlin,
Gradle, resource, manifest, persistence, permission, dependency, UI,
`DATA_SOURCES.md`, `PRIVACY.md`, specification, or roadmap behavior/status
change was made.

Live evidence collected on 2026-09-05 with:

```text
User-Agent: OxygenWeather/0.1 (https://github.com/davidyanceyjr/oxygen/issues)
Accept: application/geo+json
```

- Madison, Wisconsin point `43.0731,-89.4012`: HTTP 200
  `application/geo+json`, valid `FeatureCollection`, zero features, observed
  `ETag`, `Cache-Control`, and `Expires`.
- London, United Kingdom point `51.5074,-0.1278`: HTTP 400
  `application/problem+json`, `Invalid Parameter`, type
  `https://api.weather.gov/problems/InvalidParameter`, detail
  `Parameter "point" is invalid: out of bounds`.
- `https://api.weather.gov/openapi.json`: HTTP 200
  `application/vnd.oai.openapi+json;version=3.1`; inspected `/alerts/active`,
  `AlertPoint`, response media types, alert properties/enums/references, and
  generic problem schema.

Source review covered NWS API service docs, OpenAPI, Alerts Web Service, NWS
Geolocation Guide, NWS CAP landing page, OASIS CAP 1.2, NWS disclaimer, NWS
privacy policy, and PNS26-62. The verification ledger is
`.codex/test-artifacts/2026-09-05-slice-22-nws-alert-provider-contract/verification-ledger.md`.

Broad checks passed:

```bash
git diff --check
git diff --stat
git diff -- .codex/plans/current.md docs/data-sources/NWS_ALERTS.md \
  docs/OXYGEN_FULL_SPECIFICATION.md DATA_SOURCES.md PRIVACY.md \
  .codex/plans/mvp-roadmap.md .codex/cycles/history.md
git diff --no-index -- /dev/null docs/data-sources/NWS_ALERTS.md
git status --short
git ls-files --others --exclude-standard \
  .codex/test-artifacts/2026-09-05-slice-22-nws-alert-provider-contract \
  docs/data-sources/NWS_ALERTS.md
```

The `--no-index` command exits `1` for the expected new-file diff; it was used
only to inspect the untracked contract content before staging. Artifact payloads
remain ignored.

Android compile, unit, connected, assemble, install, emulator, and screenshot
checks were not run because the accepted Slice 22 diff is Markdown-only.

The slice merged in PR `#10` as commit `d0a7eb3`, and local `main` now matches
`origin/main`.

## Verification Budget and Ledger

Budget: one bounded live evidence pass, one official-source/OpenAPI review, and
one final Markdown diff pass. Do not repeat a passing request/check unless the
input or environment affecting it changes. Save response headers/bodies and
the ledger under `.codex/test-artifacts/2026-09-05-slice-22-nws-alert-provider-contract/`;
artifact payloads remain untracked.

Required focused evidence using the exact User-Agent:

1. One in-coverage point: record status, content type, valid
   `FeatureCollection`, feature count, and observed cache headers.
2. One locally valid out-of-coverage point: record the actual problem status,
   content type, title/type/detail, and whether it satisfies the narrow rule.
3. Inspect OpenAPI point parameter, alert properties/enums/references, response
   media types, and generic error schema.
4. Map every rate, cache, reuse, privacy, future-effective, and lifecycle claim
   to the exact source class above.

Required broad checks:

```bash
git diff --check
git diff --stat
git diff -- .codex/plans/current.md docs/data-sources/NWS_ALERTS.md \
  docs/OXYGEN_FULL_SPECIFICATION.md DATA_SOURCES.md PRIVACY.md \
  .codex/plans/mvp-roadmap.md .codex/cycles/history.md
```

Android compile, unit, connected, assemble, install, emulator, and screenshot
checks are intentionally excluded because the accepted diff is Markdown-only.
If the diff escapes that boundary, stop and re-plan instead of using Android
checks to legitimize scope drift.

## Completion Gate

- The provider contract covers every table row with exact citations and a
  current review date.
- Domain expansion is assigned to 23A; result/error evolution to 23B;
  independent merge/freshness to 23C; existing alert names are preserved.
- Coverage, local validation, empty success, and unsupported classification are
  deterministic and honestly distinguish observation from guarantee.
- The exact User-Agent/contact is recorded and used in live evidence.
- NWS remains roadmap-only; forecast risk remains non-official; no alert data
  is sent through forecast cache storage.
- The evidence ledger and final diff review pass, with unrelated files and the
  separate roadmap/history authority-sync left untouched.
