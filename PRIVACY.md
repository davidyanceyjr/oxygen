# Oxygen Privacy

Oxygen is designed around data minimization.

The standard build contains no advertising SDK, behavioral analytics, marketing
attribution, cross-app tracking, or mandatory account system.

Location permission is optional. A user must be able to search for and save
locations manually. Manual location search must not request Android location
permission.

## Active Provider Requests

Oxygen currently has production paths for Open-Meteo forecast requests and
Open-Meteo geocoding search.

Forecast requests send the selected location coordinates, IANA timezone,
requested weather variables, and normal client network metadata such as IP
address to Open-Meteo.

Geocoding requests send the typed place query, bounded result count, optional
locale/filter parameters where implemented, and normal client network metadata
such as IP address to Open-Meteo. Open-Meteo geocoding uses location data based
on GeoNames.

Open-Meteo provider privacy implications and reviewed terms are recorded in
docs/data-sources/OPEN_METEO_FORECAST.md and
docs/data-sources/OPEN_METEO_GEOCODING.md.

## Local Data

Selected locations and cached forecasts are intended to be stored locally when
those features are implemented. This repository does not currently include
saved-location persistence or offline forecast cache behavior.
