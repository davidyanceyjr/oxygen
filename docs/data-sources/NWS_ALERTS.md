# NWS Alerts Provider Contract

- **Provider:** NOAA/National Weather Service weather.gov API active alerts.
- **Purpose:** Active installed-app provider contract for selected-point
  official weather alerts in the United States, NWS-served territories, and
  relevant marine areas accepted by the NWS active-alert service. This
  document specifies the installed provider path and its data/privacy rules.
- **Coverage:** Coverage is the point coverage accepted by
  `GET /alerts/active?point={lat},{lon}`, not CONUS, country code, or forecast
  provider choice. Oxygen should attempt any locally valid WGS84 point and let
  provider acceptance determine support. NWS OpenAPI area codes include US
  states, District of Columbia, territories, freely associated states listed by
  the schema, and marine area/region filters. The point query resolves alerts
  for the active county and zone intersecting the coordinate according to the
  NWS Alerts Geolocation Guide. Unsupported handling is defined below.
- **Base endpoint:** `https://api.weather.gov/alerts/active?point={latitude},{longitude}`.
  Use HTTPS. Keep the base URL, query construction, and request identity
  provider-local/configurable and outside UI code.
- **Authentication:** No current API key, account, SDK, OAuth flow, or paid
  subscription is required. NWS requires a User-Agent header and documents that
  this mechanism may be replaced by an API key later.
- **Required headers:**
  - `User-Agent: OxygenWeather/0.1 (https://github.com/davidyanceyjr/oxygen/issues)`
  - `Accept: application/geo+json`

  The contact target is the issue tracker for the current Git origin. Re-review
  this value when repository ownership or app version changes. Do not reuse the
  current MET Norway default contact URL because it does not match this
  repository origin.
- **Request/rate limits:** NWS says the API is free/open data with reasonable
  rate limits, but the numeric rate limit is not public. NWS alerts guidance
  says clients should not refresh web feeds more often than every 30 seconds,
  and the general appropriate-use guidance says clients should know refresh
  frequency, request only needed data, and avoid tight retry loops. Oxygen must
  not poll from recomposition, location switching, or provider fallback; rate
  limits and provider-unavailable failures require backoff. Response freshness
  never forces Oxygen to poll at the HTTP `max-age` interval.
- **Caching rules:** Record response `Cache-Control`, `Expires`, `ETag`,
  `Last-Modified`, fetch time, provider ID, and request point when present and
  useful. NWS designed the API to be cache friendly and content can expire based
  on its information life cycle. Future alert cache storage must be independent
  from the forecast cache. Room forecast cache currently rejects alert-bearing
  forecast bundles; Slices 23 must not route alert persistence through that
  boundary.
- **Fields used:** Parse the GeoJSON `FeatureCollection`; each alert feature
  uses `id`, `geometry`, and `properties`. Required alert properties are
  `id`, `areaDesc`, `geocode`, `affectedZones`, `references`, `sent`,
  `effective`, `onset`, `expires`, `ends`, `status`, `messageType`, `category`,
  `severity`, `certainty`, `urgency`, `event`, `sender`, `senderName`,
  `headline`, `description`, `instruction`, `response`, `parameters`, `scope`,
  `code`, `language`, `web`, and `eventCode` where present. Preserve unknown
  provider fields in diagnostics only when needed; do not leak provider DTOs or
  raw response bodies into Composables.
- **Time format:** NWS API documentation states API times are ISO 8601. Parse
  `sent`, `effective`, `onset`, `expires`, `ends`, and any reference `sent` as
  absolute instants. Keep these fields distinct. Nullable provider timestamps
  remain absent; never substitute fetch time, selected-location time, phone time
  zone, or UTC. Presentation converts instants through the selected
  `WeatherLocation.zoneId`.
- **Unit format:** Not applicable for the selected active-alert fields. If
  future NWS parameters include units or measurements, add a fixture-backed
  parser contract before using them.
- **Weather-code mapping:** Not applicable. Official alerts must remain
  semantically distinct from forecast conditions and forecast-derived risk. A
  modeled thunderstorm or precipitation risk is never an official alert.
- **Severity, urgency, and certainty mapping:** Preserve severity, urgency, and
  certainty separately. Contracted NWS/OpenAPI values are:
  - Severity: `Extreme`, `Severe`, `Moderate`, `Minor`, `Unknown`
  - Urgency: `Immediate`, `Expected`, `Future`, `Past`, `Unknown`
  - Certainty: `Observed`, `Likely`, `Possible`, `Unlikely`, `Unknown`

  Unknown, missing, or unrecognized enum values map to provider-neutral
  `UNKNOWN`/unknown equivalents while preserving a diagnostic value for parser
  debugging. Do not synthesize a combined danger score or infer one field from
  another.
- **Identity, lifecycle, and deduplication:** Treat the provider alert `id` as
  opaque and deduplicate identical IDs before repository/UI composition. Preserve
  NWS/CAP `status`, `messageType`, `references`, `sent`, and `eventCode`.
  Message type values include `Alert`, `Update`, `Cancel`, `Ack`, and `Error`;
  active-query filters document `alert`, `update`, and `cancel`. Slice 23A must
  fixture Alert/Update/Cancel/reference behavior for mapper robustness, while
  avoiding a claim that one active snapshot always contains prior or
  cancellation messages.
- **Expiration handling:** Active responses may contain ongoing and near-future
  effective alerts per the NWS geolocation/current-active guidance. Future cache
  reads must filter expired, superseded, and cancelled cached input before
  presentation. `expires` is information expiry; `ends` is expected event end
  when supplied. Do not present expired cached alert data as active.
- **Geometry and affected area:** Preserve valid GeoJSON geometry when present,
  `areaDesc`, `geocode.UGC`, `geocode.SAME`, and `affectedZones`. The NWS
  geolocation guide says polygon warnings include geometry and county/zone-based
  alerts can have `geometry: null`; null geometry is therefore usable for a
  point-filtered result and must fall back to area/zones/geocodes for affected
  area. Fabricate no polygons or county/zone membership.
- **UI-required banner/detail fields:** Slices 24A and 24B need provider-neutral
  data for visible severity text, event name, headline or event fallback,
  issuer/source, effective/onset/expires/end timing, affected area,
  description, instructions, and source/provenance. Critical official
  instructions must not be paraphrased in a way that changes meaning. Severity
  must not be communicated by color alone.
- **Error responses:** Future client/result boundaries must distinguish:
  `Success(alerts)`, including empty success; `UnsupportedRegion`;
  local `InvalidPoint`; `NetworkUnavailable`; `RateLimited`;
  `ProviderUnavailable`; `InvalidRequest`; `InvalidResponse`;
  `IdentificationRejected`; and unexpected HTTP/provider failure. Preserve
  NWS `correlationId`/request identifiers in diagnostics where available.

  Local validation rejects non-finite coordinates and latitude outside
  `[-90, 90]` or longitude outside `[-180, 180]` before transport. For locally
  valid points:
  - HTTP 200 with a valid `FeatureCollection`, including zero features, is a
    supported success.
  - `UnsupportedRegion` requires HTTP 400 `application/problem+json` with type
    `https://api.weather.gov/problems/InvalidParameter`, title
    `Invalid Parameter`, and detail
    `Parameter "point" is invalid: out of bounds`.
  - Every other HTTP 400 is `InvalidRequest`, or `InvalidResponse` if the
    declared problem envelope is malformed.

  The OpenAPI currently documents a generic problem response and does not
  guarantee that out-of-bounds discriminator. The unsupported-region rule is a
  dated Oxygen interpretation of observed 2026-09-05 behavior and must be
  re-reviewed immediately before Slice 23B.
- **Attribution:** Display NOAA/National Weather Service attribution anywhere
  NWS alert content is displayed. Acceptable copy is equivalent to
  `Official alerts from NOAA/National Weather Service`, with a link to
  `https://www.weather.gov/` or the specific alert source. Do not imply NOAA or
  NWS endorses Oxygen.
- **License:** NWS says information presented on its pages is public information
  and may be distributed or copied with appropriate byline/photo/image credits
  requested. NWS also requires third-party works predominantly using NWS page
  material to identify incorporated NWS material and note that it is not subject
  to copyright protection as required by 17 U.S.C. 403. Third-party information
  on NWS pages may have separate licenses.
- **Privacy implications:** Alert lookup sends the selected coordinates, request
  URL, Oxygen's identifying User-Agent/contact header, and normal network
  metadata such as IP address to NWS. NWS privacy policy says site management
  logs include date/time, originating IP, browser/OS if provided, referrer if
  provided, requested object, completion status, and pages visited, and those
  logs may be preserved indefinitely for security and service-integrity needs.
  Oxygen discloses NWS as an active provider in the installed app. No new
  Android permission, account, telemetry, background collection, or
  advertising SDK is introduced by this contract.
- **Failover behavior:** Official alert lookup is independent from forecast
  provider selection and fallback. Open-Meteo/MET Norway forecast success or
  failure cannot create, suppress, or invalidate official alerts. NWS alert
  failure cannot invalidate the displayed forecast. Forecast and alert freshness
  remain separate; stale forecast with fresh alerts and fresh forecast with
  alert failure must both be representable.
- **Fixture/sample response location:** Future fixtures live under
  `core/src/test/resources/providers/nws/`. Slice 23A must add no-alert,
  one-alert, many-alert, missing-optionals, unknown-enums, geometry polygon,
  null-geometry, duplicate ID, update references, cancel references,
  near-future-effective, expired/superseded cached input, malformed envelope,
  malformed problem envelope, and unsupported-region problem fixtures. Parser
  tests must not require live internet.
- **Official documentation:**
  - API service docs: https://www.weather.gov/documentation/services-web-api
  - OpenAPI schema: https://api.weather.gov/openapi.json
  - Alerts Web Service: https://www.weather.gov/documentation/services-web-alerts
  - NWS Alerts Geolocation Guide: https://www.weather.gov/media/documentation/docs/NWS_Geolocation.pdf
  - NWS CAP documentation landing page: https://www.weather.gov/alerting
  - OASIS CAP 1.2: https://docs.oasis-open.org/emergency/cap/v1.2/CAP-v1.2.html
  - NWS disclaimer: https://www.weather.gov/disclaimer
  - NWS privacy policy: https://www.weather.gov/privacy
  - PNS26-62 CAP transition notice: https://www.weather.gov/media/notification/pdf_2026/PNS26-62_CAP_Transition.pdf
- **Last terms review date:** 2026-09-05

## Existing Oxygen Boundary

`WeatherAlert`, `AlertSeverity`, `DataType.OFFICIAL_ALERT`,
`WeatherBundle.alerts`, provenance, and `AlertProvider` already exist. Reuse and
evolve those names rather than adding a parallel alert hierarchy. The current
domain model can retain ID, event, headline, severity, effective/expires,
description, instruction, issuer, and provenance, but it lacks urgency,
certainty, affected-area/geometry, onset/end, sent time, message type, status,
and update references. Slice 23A owns that domain expansion.

`AlertProvider.getActiveAlerts` now has a synchronous blocking boundary and
returns the provider-neutral `AlertProviderResult`, so empty success,
unsupported region, rate limit, network/offline, provider unavailable, invalid
request, invalid response, and identification rejection remain observable.
Slice 23C owns forecast/alert repository composition and separate freshness;
the core merge is implemented, while installed-app alert presentation remains
out of scope.

Home currently renders live NWS alert summary and detail presentation through
the installed alert path. Alert persistence does not exist. Forecast cache
storage is forecast-only and rejects alert-bearing bundles.

## Dated Evidence

On 2026-09-05, using the exact User-Agent and `Accept: application/geo+json`:

- Madison, Wisconsin point `43.0731,-89.4012` returned HTTP 200
  `application/geo+json`, a valid `FeatureCollection`, zero features, `ETag`,
  `Cache-Control: public, max-age=5, s-maxage=5`, and `Expires`.
- London, United Kingdom point `51.5074,-0.1278` returned HTTP 400
  `application/problem+json` with title `Invalid Parameter`, type
  `https://api.weather.gov/problems/InvalidParameter`, and detail
  `Parameter "point" is invalid: out of bounds`.
- `https://api.weather.gov/openapi.json` returned HTTP 200
  `application/vnd.oai.openapi+json;version=3.1` and documented
  `/alerts/active`, the `point` query parameter, `Alert` properties/enums,
  GeoJSON response media type, and generic problem response.

Saved evidence lives under
`.codex/test-artifacts/2026-09-05-slice-22-nws-alert-provider-contract/`.
