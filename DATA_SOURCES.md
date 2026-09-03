# Oxygen Data Sources

This document records repository-level data-source disclosure for the providers
that have production paths in this repository. It is separate from Oxygen
source-code licensing.

## Active App Providers

### Forecasts: Open-Meteo With MET Norway Fallback

- Purpose: Installed-app default forecast provider for explicit selected
  locations, with MET Norway fallback after eligible Open-Meteo terminal
  forecast failures.
- Data shown by current production path: provider-neutral Home forecast success
  presentation with source, update, data type, and license provenance from the
  served provider data.
- Request data: selected location latitude, longitude, IANA timezone, requested
  forecast variables, and normal client network metadata such as IP address.
- Attribution: Weather data by Open-Meteo.com.
- Data license: Creative Commons Attribution 4.0 International (CC BY 4.0), as
  recorded in docs/data-sources/OPEN_METEO_FORECAST.md.
- Last terms review date: 2026-08-19.

### Location Search: Open-Meteo Geocoding API / GeoNames

- Purpose: Manual location search and selectable provider-neutral
  WeatherLocation candidates.
- Request data: typed place query, bounded result count, optional locale/filter
  parameters where implemented, and normal client network metadata such as IP
  address.
- Attribution: Location search by Open-Meteo, based on GeoNames data.
- Data license: Open-Meteo API data are offered under CC BY 4.0; GeoNames
  describes its geographical database as available under a Creative Commons
  attribution license, as recorded in docs/data-sources/OPEN_METEO_GEOCODING.md.
- Last terms review date: 2026-08-19.

### Forecasts: MET Norway

- Purpose: Active installed-app forecast fallback after eligible Open-Meteo
  terminal failures, using the provider-neutral fallback repository path.
- Current app status: wired behind Open-Meteo in the installed selected-location
  Home refresh path.
- Verified capability: Slice 31A covers installed factory fallback composition,
  Home ready presentation with MET Norway provenance, and identifying
  User-Agent behavior with controlled provider responses.
- Not yet implemented or verified: provider-specific cache-header persistence,
  conditional GET metadata, cached fallback restore claims, provider health or
  backoff state, and release-candidate fallback behavior.
- Request data when this provider path is used: selected location latitude and
  longitude, optional altitude when present, an identifying User-Agent/contact
  header, and normal client network metadata such as IP address.
- Attribution: Weather forecast from MET Norway.
- Data license: Norwegian Licence for Open Government Data (NLOD) 2.0, as
  recorded in docs/data-sources/MET_NORWAY_FORECAST.md.
- Last terms review date: 2026-08-23.

## Roadmap-Only Providers

These providers are specified by the roadmap or product specification but are
not active/current until their own contracts, production paths, app wiring, and
verification evidence are complete:

- NOAA/NWS — United States official alerts and later observations/radar.
- Environment and Climate Change Canada — Canadian official alerts.
- Open-Meteo/CAMS — air-quality path where appropriate.

Oxygen core includes a repository-level cache wrapper that persists one
provider-served forecast bundle as provider-neutral current/hourly/daily rows
scoped by local `LocationId`. When that wrapper is used, a foreground refresh
failure can retain the same selected location's cached forecast as a stale
success with explicit refresh-failed metadata at the repository and app-state
boundary. The installed app wires that durable Room cache for selected-location
forecasts and supports offline restoration of the last cached forecast for the
selected local `LocationId`.

The installed app also persists saved locations locally, shows saved rows on the
location-entry surface, marks the current saved location, and can select an
existing saved row through the local selected-location path. It does not yet
implement search-result save UI, saved-location removal UI, unit preferences,
official alert lookup, air-quality lookup, radar, provider-specific MET Norway
cache-header persistence, or release-candidate fallback behavior.

Before any additional provider becomes active, document its current terms,
attribution, rate/caching requirements, privacy implications, and last review
date under docs/data-sources/.
