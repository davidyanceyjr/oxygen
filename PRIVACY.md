# Oxygen Privacy

Oxygen is designed around data minimization.

The standard build contains no advertising SDK, behavioral analytics, marketing
attribution, cross-app tracking, or mandatory account system.

Location permission is optional. A user can search for locations manually in
the current app build. Manual location search must not request Android location
permission.

Use my location requests only coarse foreground permission after an explicit
tap and obtains one approximate position. Its coordinates and normal network
metadata go first to Open-Meteo for IANA timezone resolution, then through the
normal forecast request path. The selected approximate position is stored
locally and can restore cached weather offline; it is not automatically saved
as a saved place. Launch, restart, refresh, and background paths never acquire
a new device fix. A new explicit action is required to relocate.

## Active Provider Requests

Oxygen currently has active installed-app production paths for Open-Meteo
forecast requests, MET Norway forecast fallback requests, Open-Meteo
geocoding search, and foreground NOAA/NWS selected-point alert lookup.

Forecast requests send the selected location coordinates, IANA timezone,
requested weather variables, and normal client network metadata such as IP
address to Open-Meteo.

Geocoding requests send the typed place query, bounded result count, optional
locale/filter parameters where implemented, and normal client network metadata
such as IP address to Open-Meteo. Open-Meteo geocoding uses location data based
on GeoNames.

Foreground NWS alert requests send the selected coordinates, required
Oxygen-identifying User-Agent/contact header, and normal client network metadata
such as IP address. NWS lookup is independent of forecast retrieval, is gated
per exact selected point in process memory for 30 seconds, and is not persisted
or run in the background.

Opening the alert detail surface reuses the same foreground alert result and
does not add a separate network request.

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

The installed app also stores the last selected location, saved-location
records, unit choices, and the selected Off/Subtle effects preference locally.
Saved rows can be shown and selected from the location-entry surface, with
search-result save and confirmed removal. The effects preference does not send
data to a provider and does not grant or request location access.
The app includes alert detail navigation, but not alert persistence/cache,
background alert polling or notifications, air-quality lookup, radar,
conditional GET requests, 304 not-modified handling, or release-candidate
fallback behavior.
