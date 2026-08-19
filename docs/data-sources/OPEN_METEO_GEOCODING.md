# Open-Meteo Geocoding Provider Contract

- **Provider:** Open-Meteo Geocoding API, using location data based on GeoNames.
- **Purpose:** Initial MVP place-search provider for manual location selection. This contract specifies the provider before code is added; it does not make geocoding active in production.
- **Coverage:** Global place search from the GeoNames database. Result quality, translated names, postal-code support, administrative hierarchy, population, and elevation availability vary by region and source data quality.
- **Base endpoint:** Free non-commercial search endpoint `https://geocoding-api.open-meteo.com/v1/search`. ID lookup endpoint `https://geocoding-api.open-meteo.com/v1/get?id=<provider id>` may resolve a previously returned provider ID inside the provider implementation. Commercial/customer usage uses Open-Meteo customer API resources with `apikey`; Oxygen must keep the base URL configurable and outside UI code.
- **Authentication:** No API key, account, sign-up, or SDK for the free non-commercial endpoint. Commercial use requires a paid Open-Meteo subscription and `apikey` query parameter.
- **Required headers:** None documented for the free geocoding API. Oxygen should still send a normal HTTPS client user agent when the Android HTTP stack provides one; do not depend on a custom header for correctness.
- **Request/rate limits:** Free/open-access use is non-commercial only and limited by Open-Meteo terms to fewer than 10,000 calls/day, 5,000 calls/hour, and 600 calls/minute. Pricing documentation states the free API has no uptime guarantee. Oxygen must debounce foreground search, cancel superseded queries, cache selected geocoding results long term, and avoid repeated identical requests while a cached result remains suitable.
- **Caching rules:** Geocoding is user initiated and selected results are long lived per `docs/OXYGEN_FULL_SPECIFICATION.md`. Cache selected provider-neutral `WeatherLocation` values and enough provider metadata for diagnostics/provenance, not provider DTOs. If HTTP cache headers are present in future responses, respect them where they are stricter than Oxygen's local cache policy. Do not cache failed transient responses as successful empty results.
- **Fields used:** Search requests should use `name=<user query>`, `count=<bounded result count>`, `format=json`, optional `language=<app/search locale>`, and optional `countryCode=<ISO-3166-1 alpha-2>` when the UI has an explicit country filter. The provider documents up to 100 returned results; Oxygen's UI path should request a smaller bounded count appropriate for autocomplete/search results.

  Required response fields for a selectable Oxygen location:
  `name`, `latitude`, `longitude`, `timezone`, `country`, and `country_code`.

  Administrative/disambiguation fields where present:
  `admin1`, `admin2`, `admin3`, `admin4`, their provider IDs, `feature_code`, `population`, `postcodes`, and provider `id`.

  Optional enrichment fields:
  `elevation` and translated names where returned by the provider.

  Provider `id` is provider metadata only. It must not become the user-facing or persisted stable Oxygen `LocationId`.
- **Time format:** Geocoding responses provide an IANA timezone string for a result where available. Oxygen must validate it as a `ZoneId` before accepting a selectable location. Provider timestamps are not part of this geocoding contract.
- **Unit format:** Coordinates are WGS84 latitude/longitude decimal degrees. Elevation, where returned, is interpreted as meters. No weather units are part of this contract.
- **Weather-code mapping:** Not applicable. This provider returns places, not weather conditions.
- **Error responses:** Open-Meteo geocoding docs state that invalid URL parameters return HTTP 400 with a JSON object containing `error: true` and `reason`. Oxygen's future geocoding client must classify at least empty results, invalid request/response, network/offline failure, provider unavailable/5xx, HTTP/rate-limit where detectable, and unexpected body shape. Provider-specific error bodies must not cross into Composables.
- **Attribution:** Display Open-Meteo and GeoNames attribution anywhere Open-Meteo geocoding data is disclosed as an active data source. Required app copy should be equivalent to `Location search by Open-Meteo, based on GeoNames data`, with links to `https://open-meteo.com/` and `https://www.geonames.org/`. Do not imply either provider endorses Oxygen.
- **License:** Open-Meteo API data are offered under Creative Commons Attribution 4.0 International (CC BY 4.0). GeoNames describes its geographical database as available under a Creative Commons attribution license. Oxygen must keep data-source attribution and license disclosure separate from Oxygen source-code licensing.
- **Privacy implications:** Search requests send the user's typed place query, optional country/language filters, and client network metadata such as IP address to Open-Meteo. A selected result may also be stored locally as a saved location without requiring Android location permission. Oxygen must disclose provider request data before presenting Open-Meteo geocoding as active, and manual location search must remain separate from device-location permission.
- **Failover behavior:** Open-Meteo geocoding is the initial specified MVP geocoder, not a structural dependency. The geocoding repository must remain replaceable and provider-neutral. Oxygen must not hard-wire the public OSM Nominatim service as the only production autocomplete backend; the public Nominatim usage policy states autocomplete is not supported on that service and client-side autocomplete must not be implemented against it. Future fallbacks may include another permitted hosted geocoder, a self-hosted geocoder, an Oxygen relay, or a bundled/indexed location dataset after their own contracts are written.
- **Fixture/sample response location:** Future parser fixtures must live under `core/src/test/resources/providers/openmeteo/geocoding/` unless the provider implementation is placed in a different testable module. Required fixture set for the next slice: normal city search, empty results, ambiguous same-name places, missing optional fields, malformed envelope, invalid coordinates, invalid timezone, provider error body, and bounded postal-code/place query where supported.
- **Official documentation:**
  - Geocoding API docs: https://open-meteo.com/en/docs/geocoding-api
  - Terms and privacy: https://open-meteo.com/en/terms
  - License: https://open-meteo.com/en/licence
  - Pricing/rate-limit details: https://open-meteo.com/en/pricing
  - Open-Meteo geocoding source project: https://github.com/open-meteo/geocoding-api
  - GeoNames about/license summary: https://www.geonames.org/about.html
  - OSMF public Nominatim usage policy, used only as a constraint against public-server autocomplete dependency: https://operations.osmfoundation.org/policies/nominatim/
- **Last terms review date:** 2026-08-19

## Oxygen Semantics

Geocoding maps user search text to a provider-neutral `WeatherLocation` candidate. Provider DTOs, provider IDs, and provider ranking internals must remain inside the geocoding implementation. Ambiguous places must remain distinct through country, administrative area, coordinate, and timezone data.

Oxygen owns stable local `LocationId` generation. A provider ID may be stored only as provenance or refresh metadata and must not be exposed as the local identifier that saved locations, Home routing, or UI state depend on.
