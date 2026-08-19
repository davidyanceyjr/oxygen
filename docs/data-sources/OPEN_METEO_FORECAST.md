# Open-Meteo Forecast Provider Contract

- **Provider:** Open-Meteo
- **Purpose:** Default MVP general forecast provider for current conditions, hourly forecast, daily forecast, and Home source/provenance display. This contract does not make Open-Meteo active in production; activation requires DTO parsing, client, mapper, repository, and UI state slices.
- **Coverage:** Global forecast coverage through Open-Meteo's automatically selected best-match weather-model blend. Model availability, resolution, forecast length, update cadence, and field availability vary by geography and upstream national weather service model.
- **Base endpoint:** Free non-commercial endpoint `https://api.open-meteo.com/v1/forecast`. Commercial/customer endpoint is `https://customer-api.open-meteo.com/v1/forecast` with the same query shape plus `apikey`; Oxygen must keep the base URL configurable and outside UI code.
- **Authentication:** No API key, account, sign-up, or SDK for the free non-commercial endpoint. Commercial use requires a paid Open-Meteo subscription, customer endpoint, and `apikey` query parameter.
- **Required headers:** None documented for the free forecast API. Oxygen should still send a normal HTTPS client user agent when the Android HTTP stack provides one; do not depend on a custom header for correctness.
- **Request/rate limits:** Free/open-access use is non-commercial only and limited by Open-Meteo terms to fewer than 10,000 calls/day, 5,000 calls/hour, and 600 calls/minute. Pricing documentation states the free API has no uptime guarantee. Open-Meteo counts a typical HTTP request as one API call, but requests with more than 10 variables or more than 2 weeks for one location can count fractionally as multiple calls. Oxygen's MVP Home request should therefore stay narrowly scoped and cache-aware.
- **Caching rules:** Open-Meteo does not publish a strict forecast cache TTL in the forecast parameter documentation reviewed for this slice. Oxygen should apply the repository cache policy from `docs/OXYGEN_FULL_SPECIFICATION.md`: current conditions about 15 minutes foreground refresh with about 2 hours stale tolerance, hourly forecast about 30 minutes refresh with about 6 hours stale tolerance, and daily forecast about 2 hours refresh with about 24 hours stale tolerance. If Open-Meteo responses later expose usable HTTP cache headers, provider-specific metadata must be stored and respected where appropriate.
- **Fields used:** Use one forecast request for a single explicit `WeatherLocation` with WGS84 `latitude`, `longitude`, `timezone` set to that location's IANA zone, `timeformat=iso8601`, canonical metric units (`temperature_unit=celsius`, `wind_speed_unit=kmh`, `precipitation_unit=mm`), `forecast_days=10` where available for MVP daily rows, and `forecast_hours=48` for the first Home path where supported.

  Current fields for Home hero and metric grid:
  `temperature_2m`, `relative_humidity_2m`, `apparent_temperature`, `is_day`, `precipitation`, `rain`, `showers`, `snowfall`, `weather_code`, `cloud_cover`, `pressure_msl`, `surface_pressure`, `wind_speed_10m`, `wind_direction_10m`, `wind_gusts_10m`.

  Hourly fields for 24-48 hour Home forecast and near-term precipitation:
  `temperature_2m`, `relative_humidity_2m`, `dew_point_2m`, `apparent_temperature`, `precipitation_probability`, `precipitation`, `rain`, `showers`, `snowfall`, `weather_code`, `pressure_msl`, `surface_pressure`, `cloud_cover`, `visibility`, `uv_index`, `is_day`, `wind_speed_10m`, `wind_direction_10m`, `wind_gusts_10m`.

  Daily fields for 7-10 day Home forecast and sun/source blocks:
  `weather_code`, `temperature_2m_max`, `temperature_2m_min`, `apparent_temperature_max`, `apparent_temperature_min`, `uv_index_max`, `sunrise`, `sunset`, `daylight_duration`, `rain_sum`, `showers_sum`, `snowfall_sum`, `precipitation_sum`, `precipitation_hours`, `precipitation_probability_max`, `wind_speed_10m_max`, `wind_gusts_10m_max`, `wind_direction_10m_dominant`, plus daily mean/min/max fields later only when a verified UI need exists.
- **Time format:** Request `timeformat=iso8601` and `timezone=<WeatherLocation IANA ZoneId>`. Provider timestamps enter Oxygen domain as `Instant`; presentation converts to the selected location's local clock. Do not interpret remote weather using the phone timezone. For provider parsing, retain `utc_offset_seconds`, `timezone`, `timezone_abbreviation`, `generationtime_ms`, and response coordinates/elevation where present as provider metadata.
- **Unit format:** Request canonical metric values from the provider: Celsius, km/h, millimeters, hPa, meters, degrees, percent, seconds, and centimeters for Open-Meteo snowfall where documented. Oxygen converts units only for presentation. Missing/null provider values remain null/unknown, never zero.
- **Weather-code mapping:** Open-Meteo uses WMO weather interpretation codes. Map provider codes to Oxygen `WeatherCondition` as follows:
  - `0` -> `CLEAR`
  - `1` -> `MOSTLY_CLEAR`
  - `2` -> `PARTLY_CLOUDY`
  - `3` -> `CLOUDY`
  - `45`, `48` -> `FOG`
  - `51`, `53`, `55` -> `DRIZZLE`
  - `56`, `57` -> `FREEZING_DRIZZLE`
  - `61`, `63`, `65` -> `RAIN`
  - `66`, `67` -> `FREEZING_RAIN`
  - `71`, `73`, `75`, `77` -> `SNOW`
  - `80`, `81`, `82` -> `RAIN_SHOWERS`
  - `85`, `86` -> `SNOW_SHOWERS`
  - `95` -> `THUNDERSTORM`
  - `96`, `99` -> `THUNDERSTORM_HAIL`
  - any unknown, undocumented, malformed, or null code -> `UNKNOWN`

  Oxygen's domain also includes `SLEET` and `HAIL`; Open-Meteo forecast codes reviewed for this slice do not provide direct standalone mappings for those states.
- **Error responses:** Open-Meteo forecast docs state that invalid URL parameters return HTTP 400 with a JSON object containing `error: true` and `reason`. Oxygen's future client must classify at least invalid response/request, network/offline failure, provider unavailable/5xx, HTTP/rate-limit where detectable, and unexpected body shape. Provider-specific error details must not cross into Composables.
- **Attribution:** Display Open-Meteo attribution anywhere Open-Meteo weather data is displayed. Required app copy should be equivalent to `Weather data by Open-Meteo.com` with a link to `https://open-meteo.com/`. Do not imply Open-Meteo endorses Oxygen.
- **License:** Open-Meteo API data are offered under Creative Commons Attribution 4.0 International (CC BY 4.0). Open-Meteo server source code is AGPLv3-or-later; that is separate from the data license and does not by itself license Oxygen source code.
- **Privacy implications:** Forecast requests send the selected location coordinates, requested variables, timezone, and client network metadata such as IP address to Open-Meteo. Open-Meteo terms/privacy reviewed for this slice state that free API service may collect non-personal technical information such as IP addresses, and troubleshooting logs may contain sensitive information such as geographical coordinates and are deleted after 90 days. Oxygen must disclose this before presenting Open-Meteo as an active provider. Manual location support must remain fully functional without Android location permission.
- **Failover behavior:** Open-Meteo is the default MVP forecast provider. MET Norway is the specified MVP forecast fallback but is not active until its provider contract, production client/mapper/repository path, and fallback selection behavior are implemented and verified. Do not average or merge Open-Meteo values with fallback provider values. Provenance must identify whichever provider served displayed forecast data.
- **Fixture/sample response location:** Future parser fixtures must live under `core/src/test/resources/providers/openmeteo/` unless the provider implementation is placed in a different testable module. Required fixture set for the next slice: normal Home forecast response, missing optional values, malformed envelope, invalid weather code, rate/error response body, and timezone-sensitive response.
- **Official documentation:**
  - Forecast API docs: https://open-meteo.com/en/docs
  - Terms and privacy: https://open-meteo.com/en/terms
  - License: https://open-meteo.com/en/licence
  - Pricing/rate-limit details: https://open-meteo.com/en/pricing
  - Open-Meteo source project: https://github.com/open-meteo/open-meteo
- **Last terms review date:** 2026-08-19

## Oxygen Semantics

Open-Meteo current-condition values are based on 15-minute weather model data according to the provider documentation reviewed for this slice. Oxygen must therefore label Open-Meteo current conditions as `DataType.MODEL_ESTIMATE`, not `OBSERVATION`, unless a future provider review identifies a documented observation source for a specific value.

Forecast-domain and UI consumers may receive only provider-neutral Oxygen models and provenance. Provider query names, WMO codes, Open-Meteo unit metadata, and Open-Meteo error bodies are provider implementation details.
