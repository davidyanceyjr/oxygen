# Oxygen Privacy

Oxygen is designed around data minimization.

The standard build contains no advertising SDK, behavioral analytics, marketing
attribution, cross-app tracking, or mandatory account system.

Location permission is optional. A user can search for locations manually in
the current app build. Manual location search must not request Android location
permission.

## Active Provider Requests

Oxygen currently has active installed-app production paths for Open-Meteo
forecast requests and Open-Meteo geocoding search.

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

## Implemented Provider Capability

The repository also includes an implemented MET Norway forecast provider path
and a core fallback-selection capability verified in Slice 14. MET Norway is not
currently wired as the active installed-app forecast fallback.

When the MET Norway provider path is used, requests send selected coordinates,
optional altitude when present, an identifying User-Agent/contact header, and
normal client network metadata such as IP address to MET Norway. Provider logs
and privacy handling are governed by MET Norway's service terms and privacy
statement, as recorded in docs/data-sources/MET_NORWAY_FORECAST.md.

## Local Data

Selected locations and cached forecasts are intended to be stored locally when
those features are implemented. This repository does not currently include
saved-location persistence, unit preferences, alert lookup, air-quality lookup,
radar, active installed-app forecast fallback wiring, or offline forecast cache
behavior.
