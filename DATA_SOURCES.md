# Oxygen Data Sources

This document records repository-level data-source disclosure for the providers
that have production paths in this repository. It is separate from Oxygen
source-code licensing.

## Active App Providers

### Forecasts: Open-Meteo

- Purpose: Installed-app default forecast provider for explicit selected
  locations.
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

## Implemented Provider Paths And Capabilities

### Forecasts: MET Norway

- Purpose: Implemented forecast provider path and core fallback-selection
  capability.
- Current app status: not wired as the active installed-app forecast fallback in
  this build.
- Verified capability: Slice 14 covers repository-level fallback selection with
  MET Norway provenance preserved when the controlled fallback repository serves
  forecast data.
- Not yet implemented or verified: installed-app fallback wiring, live fallback
  Home UI screenshots, installed-app cache wiring, stale offline UI, provider
  health or backoff state, and release-candidate fallback behavior.
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
scoped by local `LocationId`. The installed app does not yet wire that cache and
does not implement offline forecast cache behavior, failed-refresh stale
retention, saved-location persistence, unit preferences, official alert lookup,
air-quality lookup, or radar.

Before any additional provider becomes active, document its current terms,
attribution, rate/caching requirements, privacy implications, and last review
date under docs/data-sources/.
