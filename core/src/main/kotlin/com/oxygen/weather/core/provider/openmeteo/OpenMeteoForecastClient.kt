package com.oxygen.weather.core.provider.openmeteo

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class OpenMeteoForecastRequest(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val forecastDays: Int = 10,
    val forecastHours: Int = 48,
) {
    internal fun queryParameters(): List<Pair<String, String>> =
        listOf(
            "latitude" to latitude.toString(),
            "longitude" to longitude.toString(),
            "timezone" to timezone,
            "timeformat" to "iso8601",
            "temperature_unit" to "celsius",
            "wind_speed_unit" to "kmh",
            "precipitation_unit" to "mm",
            "forecast_days" to forecastDays.toString(),
            "forecast_hours" to forecastHours.toString(),
            "current" to currentFields.joinToString(","),
            "hourly" to hourlyFields.joinToString(","),
            "daily" to dailyFields.joinToString(","),
        )

    companion object {
        val currentFields = listOf(
            "temperature_2m",
            "relative_humidity_2m",
            "apparent_temperature",
            "is_day",
            "precipitation",
            "rain",
            "showers",
            "snowfall",
            "weather_code",
            "cloud_cover",
            "pressure_msl",
            "surface_pressure",
            "wind_speed_10m",
            "wind_direction_10m",
            "wind_gusts_10m",
        )

        val hourlyFields = listOf(
            "temperature_2m",
            "relative_humidity_2m",
            "dew_point_2m",
            "apparent_temperature",
            "precipitation_probability",
            "precipitation",
            "rain",
            "showers",
            "snowfall",
            "weather_code",
            "pressure_msl",
            "surface_pressure",
            "cloud_cover",
            "visibility",
            "uv_index",
            "is_day",
            "wind_speed_10m",
            "wind_direction_10m",
            "wind_gusts_10m",
        )

        val dailyFields = listOf(
            "weather_code",
            "temperature_2m_max",
            "temperature_2m_min",
            "apparent_temperature_max",
            "apparent_temperature_min",
            "uv_index_max",
            "sunrise",
            "sunset",
            "daylight_duration",
            "rain_sum",
            "showers_sum",
            "snowfall_sum",
            "precipitation_sum",
            "precipitation_hours",
            "precipitation_probability_max",
            "wind_speed_10m_max",
            "wind_gusts_10m_max",
            "wind_direction_10m_dominant",
        )
    }
}

class OpenMeteoForecastClient(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val transport: OpenMeteoHttpTransport = UrlConnectionOpenMeteoHttpTransport(),
) {
    fun fetchForecast(request: OpenMeteoForecastRequest): OpenMeteoForecastClientResult {
        val response = try {
            transport.get(buildUrl(request))
        } catch (error: IOException) {
            return OpenMeteoForecastClientResult.Failure(OpenMeteoForecastClientError.NetworkUnavailable)
        }

        return when (response.statusCode) {
            HTTP_OK -> parseSuccessfulBody(response.body)
            HTTP_TOO_MANY_REQUESTS -> OpenMeteoForecastClientResult.Failure(OpenMeteoForecastClientError.RateLimited)
            in HTTP_SERVER_ERROR_START..HTTP_SERVER_ERROR_END -> {
                OpenMeteoForecastClientResult.Failure(
                    OpenMeteoForecastClientError.ProviderUnavailable(response.statusCode),
                )
            }
            in HTTP_CLIENT_ERROR_START..HTTP_CLIENT_ERROR_END -> classifyProviderRejection(response)
            else -> {
                OpenMeteoForecastClientResult.Failure(
                    OpenMeteoForecastClientError.UnexpectedHttpFailure(response.statusCode),
                )
            }
        }
    }

    private fun parseSuccessfulBody(body: String): OpenMeteoForecastClientResult =
        try {
            OpenMeteoForecastClientResult.Success(OpenMeteoForecastParser.parseForecast(body))
        } catch (error: OpenMeteoParseException.ProviderError) {
            OpenMeteoForecastClientResult.Failure(
                OpenMeteoForecastClientError.ProviderRejectedRequest(
                    statusCode = HTTP_OK,
                    reason = error.reason,
                ),
            )
        } catch (error: OpenMeteoParseException) {
            OpenMeteoForecastClientResult.Failure(OpenMeteoForecastClientError.InvalidResponse)
        }

    private fun classifyProviderRejection(response: OpenMeteoHttpResponse): OpenMeteoForecastClientResult =
        try {
            OpenMeteoForecastParser.parseForecast(response.body)
            OpenMeteoForecastClientResult.Failure(
                OpenMeteoForecastClientError.UnexpectedHttpFailure(response.statusCode),
            )
        } catch (error: OpenMeteoParseException.ProviderError) {
            OpenMeteoForecastClientResult.Failure(
                OpenMeteoForecastClientError.ProviderRejectedRequest(
                    statusCode = response.statusCode,
                    reason = error.reason,
                ),
            )
        } catch (error: OpenMeteoParseException) {
            OpenMeteoForecastClientResult.Failure(
                OpenMeteoForecastClientError.UnexpectedHttpFailure(response.statusCode),
            )
        }

    private fun buildUrl(request: OpenMeteoForecastRequest): URL {
        val separator = if (baseUrl.contains("?")) "&" else "?"
        val query = request.queryParameters().joinToString("&") { (key, value) ->
            "${key.encode()}=${value.encode()}"
        }
        return URL("$baseUrl$separator$query")
    }

    private fun String.encode(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8.name())

    private companion object {
        const val DEFAULT_BASE_URL = "https://api.open-meteo.com/v1/forecast"
        const val HTTP_OK = 200
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val HTTP_CLIENT_ERROR_START = 400
        const val HTTP_CLIENT_ERROR_END = 499
        const val HTTP_SERVER_ERROR_START = 500
        const val HTTP_SERVER_ERROR_END = 599
    }
}

sealed class OpenMeteoForecastClientResult {
    data class Success(
        val response: OpenMeteoForecastResponse,
    ) : OpenMeteoForecastClientResult()

    data class Failure(
        val error: OpenMeteoForecastClientError,
    ) : OpenMeteoForecastClientResult()
}

sealed class OpenMeteoForecastClientError {
    data object NetworkUnavailable : OpenMeteoForecastClientError()
    data object RateLimited : OpenMeteoForecastClientError()
    data object InvalidResponse : OpenMeteoForecastClientError()

    data class ProviderUnavailable(
        val statusCode: Int,
    ) : OpenMeteoForecastClientError()

    data class ProviderRejectedRequest(
        val statusCode: Int,
        val reason: String,
    ) : OpenMeteoForecastClientError()

    data class UnexpectedHttpFailure(
        val statusCode: Int,
    ) : OpenMeteoForecastClientError()
}

data class OpenMeteoHttpResponse(
    val statusCode: Int,
    val body: String,
)

fun interface OpenMeteoHttpTransport {
    @Throws(IOException::class)
    fun get(url: URL): OpenMeteoHttpResponse
}

class UrlConnectionOpenMeteoHttpTransport(
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 10_000,
) : OpenMeteoHttpTransport {
    override fun get(url: URL): OpenMeteoHttpResponse {
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
        }

        return try {
            val statusCode = connection.responseCode
            val body = if (statusCode in 200..399) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            OpenMeteoHttpResponse(
                statusCode = statusCode,
                body = body,
            )
        } finally {
            connection.disconnect()
        }
    }
}
