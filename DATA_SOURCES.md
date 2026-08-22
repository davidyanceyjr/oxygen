# Oxygen Data Sources

This document records repository-level data-source disclosure for the providers
that have production paths in this repository. It is separate from Oxygen
source-code licensing.

## Active Current Providers

### Forecasts: Open-Meteo

- Purpose: Default forecast provider for explicit selected locations.
- Data shown by current production path: provider-neutral forecast repository
  success data. Slice 10 reaches a terminal success carrier; Slice 11 is
  planned to render the provider-backed Home success dashboard.
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

## Specified Roadmap Providers

These providers are specified by the roadmap or product specification but are
not active/current until their own contracts, production paths, and verification
evidence are complete:

- MET Norway — forecast fallback.
- NOAA/NWS — United States official alerts and later observations/radar.
- Environment and Climate Change Canada — Canadian official alerts.
- Open-Meteo/CAMS — air-quality path where appropriate.

Before any additional provider becomes active, document its current terms,
attribution, rate/caching requirements, privacy implications, and last review
date under docs/data-sources/.
