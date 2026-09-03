# Oxygen Privacy

Oxygen is designed around data minimization.

The standard build contains no advertising SDK, behavioral analytics, marketing
attribution, cross-app tracking, or mandatory account system.

Location permission is optional. A user can search for locations manually in
the current app build. Manual location search must not request Android location
permission.

## Active Provider Requests

Oxygen currently has active installed-app production paths for Open-Meteo
forecast requests, MET Norway forecast fallback requests, and Open-Meteo
geocoding search.

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

## Forecast Fallback Requests

The installed app uses MET Norway as a forecast fallback only after eligible
Open-Meteo terminal forecast failures. Open-Meteo success, offline/network
failure, and provider-rejected requests do not call MET Norway.

MET Norway fallback requests send selected coordinates,
optional altitude when present, an identifying User-Agent/contact header, and
normal client network metadata such as IP address to MET Norway. Provider logs
and privacy handling are governed by MET Norway's service terms and privacy
statement, as recorded in docs/data-sources/MET_NORWAY_FORECAST.md.

## Local Data

Core includes a repository-level forecast cache wrapper that stores one
provider-served forecast bundle locally when an app or test explicitly uses that
wrapper. When the wrapper is used, foreground refresh failure can show the same
selected location's cached forecast as stale instead of replacing it with a
no-cache error. The installed app wires this durable Room cache for the selected
location and can restore the last cached forecast offline.

The installed app also stores the last selected location and saved-location
records locally. Saved rows can be shown and selected from the location-entry
surface. The app does not currently include search-result save UI,
saved-location removal UI, unit preferences, alert lookup, air-quality lookup,
radar, provider-specific MET Norway cache-header persistence, conditional GET
metadata, cached fallback restore claims, or release-candidate fallback
behavior.
