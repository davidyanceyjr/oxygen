# Slice 23A — NWS Alert Fixtures, Parsing, and Mapping

**Artifact:** implementation plan
**Roadmap slice:** 23A
**Prepared against:** `HEAD` `2ac01a8` (Slice 22 contract merged at `d0a7eb3`)
**Prepared:** 2026-09-05
**Status:** planned
**Cycle ID:** `2026-09-05-slice-23a-nws-alert-fixtures-parsing-mapping`

## Goal and boundary

Add the deterministic, offline NWS data boundary: committed GeoJSON and
problem-response fixtures flow through production parsing and mapping into the
expanded provider-neutral official-alert model. The slice proves field
retention, nullability, geometry/area fallback, lifecycle/reference retention,
and deterministic invalid-input handling. It makes no request and does not
activate NWS in the app.

Slice 22 is authoritative and committed. `docs/data-sources/NWS_ALERTS.md` is
the direct contract. The product specification requires official alerts to stay
distinct from forecast-derived risk.

Out of scope: HTTP/header/request/error-result classification (23B), alert
repository composition, deduplication, cached-input filtering, and persistence
(23C), and alert UI (24). In particular, this slice retains duplicate IDs and
expired/superseded/cancel lifecycle data; it does not decide which alert is
presentable. A malformed or unsupported-region `application/problem+json`
fixture is rejected as a non-`FeatureCollection`; 23B assigns its HTTP/result
classification.

No app, Gradle, Room, forecast-provider, selected-location, cache-format, or
`AlertProvider` result-boundary change is permitted.

## Contract decisions for this slice

Before the red test, expand the existing types in
`core/src/main/kotlin/com/oxygen/weather/core/model/WeatherModels.kt`; do not
create a parallel alert model or change `AlertProvider.getActiveAlerts`.

| Provider-neutral addition | Representation and validity rule |
| --- | --- |
| urgency and certainty | `AlertUrgency { IMMEDIATE, EXPECTED, FUTURE, PAST, UNKNOWN }` and `AlertCertainty { OBSERVED, LIKELY, POSSIBLE, UNLIKELY, UNKNOWN }`; missing or unrecognized provider values map to `UNKNOWN`. |
| lifecycle | `AlertStatus { ACTUAL, EXERCISE, SYSTEM, TEST, DRAFT, UNKNOWN }`, `AlertMessageType { ALERT, UPDATE, CANCEL, ACK, ERROR, UNKNOWN }`, plus nullable `sent`, `onset`, and `ends`. Missing/unknown status or message type maps to `UNKNOWN`; a present but unrecognized raw value is retained only in NWS parser diagnostics, not asserted as a known Oxygen state. |
| replacement references | `AlertReference(id: String, sender: String?, sent: Instant?)`; an alert has `references: List<AlertReference>`. Preserve every syntactically valid NWS/CAP reference in order. Invalid reference syntax or reference `sent` is a mapper `InvalidField` at its feature/index path. |
| affected area | `AlertAffectedArea(areaDescription: String?, ugcCodes: List<String>, sameCodes: List<String>, affectedZoneIds: List<String>)`. Lists preserve provider order and may be empty; area description stays nullable. No containment, zone retrieval, or synthesized area. |
| geometry | Provider-neutral `AlertGeometry` is a sealed GeoJSON value model for Point, MultiPoint, LineString, MultiLineString, Polygon, MultiPolygon, and GeometryCollection, composed of `GeoPoint` positions. The mapper accepts only a GeoJSON object whose type/coordinates (or `geometries`) match that model, every coordinate is finite and within WGS84 longitude/latitude bounds, every line has at least two positions, and every polygon ring has at least four positions with equal first/last longitude/latitude. `null` remains `null`; malformed or unsupported geometry fails with `InvalidField(features[i].geometry, ...)`, never with fabricated geometry. |
| other contracted lifecycle/display data | Add nullable `category`, `response`, `scope`, `code`, `language`, and `web: String?`, plus nullable `eventCodes` and `parameters: Map<String, List<String>>?`. Preserve optional absence; do not normalize values for UI. `event`, `issuer`, `headline`, `description`, and `instruction` retain their existing model fields. |

`WeatherAlert` therefore gains `urgency`, `certainty`, `status`, `messageType`,
`sent`, `onset`, `ends`, `references`, `affectedArea`, `geometry`, and the
nullable metadata above. `affectedArea` is null only when every affected-area
source is absent; an explicitly empty provider list remains an empty list.
Its existing required `id`, `event`, `severity`, `issuer`, and provenance remain.
The mapper creates `DataProvenance(providerId = "nws", sourceName =
"NOAA/National Weather Service", type = DataType.OFFICIAL_ALERT, licenseId =
null)` with `issuedAt = sent` and the injected `fetchedAt`; it never calls a
clock. Exact provider/source/license constants must be copied from the committed
contract if that contract is amended before implementation.

The NWS DTOs remain under `core/.../provider/nws/`; only the mapper exposes
core model types. The parser follows the local `InvalidJson`, `MissingField`,
and `InvalidField(fieldPath, detail)` convention, validates a GeoJSON
`FeatureCollection` with a `features` array, and leaves timestamp conversion to
the mapper. Unknown JSON keys are ignored. Required contracted fields fail at a
precise feature/property path; optional JSON `null` stays nullable.

## Acceptance and fixture matrix

Fixtures live in `core/src/test/resources/providers/nws/`; they are minimal,
faithful documents, not live dumps. Tests load the fixture through production
code rather than constructing DTOs.

| Fixture | Parser/mapper assertion | Deliberately deferred |
| --- | --- | --- |
| `alerts_active_none.json` | valid empty FeatureCollection maps to no alerts | HTTP success classification |
| `alerts_active_one.json` | retains identity, all baseline display fields, severity/urgency/certainty, sent/effective/onset/expires/ends, lifecycle, area, geometry, provenance, and other contracted metadata | UI formatting |
| `alerts_active_many.json` | retains distinct feature records and order | UI ordering |
| `alerts_active_missing_optional.json` | optional strings/times/geometry/metadata remain absent, not empty/defaulted | fallback copy |
| `alerts_active_unknown_enums.json` | unrecognized severity, urgency, certainty, status, and message type map safely to `UNKNOWN` | provider diagnostics display |
| `alerts_active_geometry_polygon.json` | valid polygon maps to `AlertGeometry.Polygon` | spatial query/rendering |
| `alerts_active_null_geometry.json` | null geometry retains area description, UGC/SAME, and zones | geometry synthesis |
| `alerts_active_duplicate_id.json` | parser and mapper retain both records and exact IDs | deduplication (23C) |
| `alerts_active_update_references.json` | Update lifecycle and parsed references are retained | replacement decision (23C) |
| `alerts_active_cancel_references.json` | Cancel lifecycle and parsed references are retained | cancellation filtering (23C) |
| `alerts_active_near_future_effective.json` | future `effective`/`onset` map exactly | active-now decision (23C) |
| `alerts_active_expired_superseded_cached.json` | expired times, Update/Cancel lifecycle, and references map exactly | cache-read filtering (23C) |
| `alerts_active_invalid_timestamp.json` | invalid present timestamp fails mapper at its path | transport invalid-response result |
| `alerts_active_malformed_envelope.json` | malformed/missing/wrong-type FeatureCollection field fails parser deterministically | HTTP classification |
| `alerts_problem_malformed.json` | nonconforming problem body fails active-collection parser deterministically | problem parsing/classification (23B) |
| `alerts_problem_unsupported_region.json` | well-formed unsupported-region problem body fails active-collection parser deterministically | `UnsupportedRegion` result (23B) |

The one-alert fixture is the only full-field fixture. Other fixtures change the
minimum field(s) necessary for their case. No fixture represents forecast risk.

## Implementation sequence

1. Confirm `HEAD`, the Slice 22 contract, current model, existing
   Open-Meteo/MET Norway parser/mapper conventions, no nested instructions, and
   a clean understanding of unrelated working-tree changes. Stop on a
   higher-authority conflict.
2. Add focused fixture-backed parser/mapper tests first. The new tests must
   fail because the domain/parser/mapper behavior is absent, not due to setup.
3. Add the domain expansion above and tests for null/default, enum, geometry,
   reference, lifecycle, and provenance semantics.
4. Add the minimum NWS DTO/parser (`NwsAlertDtos.kt`, `NwsAlertParser.kt`) and
   mapper (`NwsAlertMapper.kt`). Keep parsing, mapping, and domain validation
   deterministic and Android/network-free.
5. Run focused tests, then the existing provider parser/mapper regression set.
   The fixture-to-production-parser-to-mapper tests are the valid real-path
   exercise for this no-transport slice.
6. Run selected broad checks, inspect the diff for scope creep, and record
   evidence/results in this plan and the cycle history only when the cycle is
   ready. Do not report `covered`, `implemented`, or `verified` before their
   respective evidence exists.

Expected code/test diff:

```text
core/src/main/kotlin/com/oxygen/weather/core/model/WeatherModels.kt
core/src/main/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertDtos.kt
core/src/main/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertParser.kt
core/src/main/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertMapper.kt
core/src/test/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertParserTest.kt
core/src/test/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertMapperTest.kt
core/src/test/resources/providers/nws/*.json
```

## Verification ledger (selected before work)

**Budget:** 35 minutes / 12k tokens after implementation; one execution of each
passing command. Re-run only after a relevant production/test/environment change
or a transient infrastructure failure, recording why. Artifact directory:
`.codex/test-artifacts/2026-09-05-slice-23a-nws-alert-fixtures-parsing-mapping/`.

| Command | Evidence |
| --- | --- |
| `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*OpenMeteoForecastParserTest' --tests '*OpenMeteoForecastMapperTest' --tests '*MetNoForecastParserTest' --tests '*MetNoForecastMapperTest'` | baseline and post-change regression of the existing provider boundary |
| `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*NwsAlertParserTest' --tests '*NwsAlertMapperTest'` | fixture → production parser → mapper behavior, including invalid cases |
| `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` | required app compilation check |
| `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` | full unit regression |
| `. scripts/android-env.sh && ./gradlew :app:assembleDebug` | debug assembly |
| `git diff --check` | whitespace integrity |

No emulator, installation, live NWS call, or connected test is selected:
there is no installed or transport path in 23A. Do not treat a curl request as
evidence for this production boundary.

## Execution evidence

**Status:** ready, not committed.

Implemented the offline NWS fixture -> production parser -> mapper boundary in
`:core`. The domain model now retains provider-neutral official-alert urgency,
certainty, lifecycle, references, affected area, geometry, metadata,
timestamps, and NWS provenance without changing `AlertProvider.getActiveAlerts`.

Changed production files:

```text
core/src/main/kotlin/com/oxygen/weather/core/model/WeatherModels.kt
core/src/main/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertDtos.kt
core/src/main/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertParser.kt
core/src/main/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertMapper.kt
```

Changed test/fixture files:

```text
core/src/test/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertParserTest.kt
core/src/test/kotlin/com/oxygen/weather/core/provider/nws/NwsAlertMapperTest.kt
core/src/test/resources/providers/nws/*.json
```

Verification results:

| Command | Result |
| --- | --- |
| `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*NwsAlertParserTest' --tests '*NwsAlertMapperTest'` | Failed once because the malformed-problem parser assertion expected `InvalidField(type)` while production correctly returned `MissingField(type)` for a body without `type`; rerun passed after test-only correction. After adding invalid-geometry coverage, failed once because the test expected a less precise geometry path; rerun passed after test-only correction to `features[0].geometry.coordinates[0][0][0]`. |
| `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*OpenMeteoForecastParserTest' --tests '*OpenMeteoForecastMapperTest' --tests '*MetNoForecastParserTest' --tests '*MetNoForecastMapperTest'` | Passed. |
| `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` | Passed. |
| `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` | Passed; rerun passed after the invalid-geometry test/fixture addition. |
| `. scripts/android-env.sh && ./gradlew :app:assembleDebug` | Passed. |
| `git diff --check` | Passed. |

Artifacts:

```text
.codex/test-artifacts/2026-09-05-slice-23a-nws-alert-fixtures-parsing-mapping/
```

No emulator, installation, live NWS request, connected test, app UI, Room,
forecast-provider, selected-location, cache-format, Gradle, dependency, or
`AlertProvider` result-boundary change was run or made for this slice.

## Review and follow-up

Before ready, inspect `git status --short`, `git diff --stat`, and `git diff`.
Reject DTO leakage, `Instant.now()`, fake/defaulted values, raw response bodies,
generic provider abstractions, dependency churn, and any transport/repository/
UI/cache wiring.

The roadmap currently says Slices 22 and 23A are `specified`; that is stale
against Slice 22's committed status and this active `planned` document. The
next documentation-only authority-sync must correct those roadmap labels. This
plan, not the stale roadmap, is the status evidence for 23A until that sync.
