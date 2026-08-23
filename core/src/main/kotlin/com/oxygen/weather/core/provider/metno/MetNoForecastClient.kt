package com.oxygen.weather.core.provider.metno

import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt

data class MetNoForecastRequest(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double? = null,
    val cachedLastModified: String? = null,
)

class MetNoForecastClient(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val userAgent: String = DEFAULT_USER_AGENT,
    private val transport: MetNoHttpTransport = UrlConnectionMetNoHttpTransport(),
) {
    fun fetchForecast(request: MetNoForecastRequest): MetNoForecastClientResult {
        val httpRequest = buildRequestOrFailure(request)
            ?: return MetNoForecastClientResult.Failure(MetNoForecastClientError.InvalidRequest)

        if (!userAgent.isValidMetNoUserAgent()) {
            return MetNoForecastClientResult.Failure(MetNoForecastClientError.IllegalIdentification(null, null))
        }

        val response = try {
            transport.get(httpRequest)
        } catch (error: IOException) {
            return MetNoForecastClientResult.Failure(MetNoForecastClientError.NetworkUnavailable)
        }

        return classifyResponse(response)
    }

    private fun buildRequestOrFailure(request: MetNoForecastRequest): MetNoHttpRequest? {
        if (!request.latitude.isValidLatitude() || !request.longitude.isValidLongitude()) return null
        if (request.altitudeMeters != null && !request.altitudeMeters.isFinite()) return null

        val baseUri = try {
            URI(baseUrl)
        } catch (error: IllegalArgumentException) {
            return null
        }
        if (!baseUri.query.isNullOrBlank() || !baseUri.fragment.isNullOrBlank()) return null

        val queryParameters = buildList {
            add("lat" to request.latitude.formatCoordinate())
            add("lon" to request.longitude.formatCoordinate())
            request.altitudeMeters?.let { add("altitude" to it.roundToInt().toString()) }
        }
        val query = queryParameters.joinToString("&") { (key, value) ->
            "${key.encode()}=${value.encode()}"
        }
        val separator = if (baseUrl.endsWith("?")) "" else "?"
        val url = try {
            URL("$baseUrl$separator$query")
        } catch (error: IllegalArgumentException) {
            return null
        }

        val headers = buildMap {
            put("User-Agent", userAgent)
            put("Accept", "application/json")
            request.cachedLastModified?.takeIf { it.isNotBlank() }?.let {
                put("If-Modified-Since", it)
            }
        }
        return MetNoHttpRequest(url = url, headers = headers)
    }

    private fun classifyResponse(response: MetNoHttpResponse): MetNoForecastClientResult {
        val errorClass = response.headerValue(ERROR_CLASS_HEADER)
        val cacheHeaders = response.cacheHeaders()

        return when {
            response.statusCode == HTTP_OK -> parseSuccessfulBody(response.body, cacheHeaders)
            response.statusCode == HTTP_NOT_MODIFIED -> MetNoForecastClientResult.NotModified(cacheHeaders)
            errorClass.isRateLimitClass() || response.statusCode == HTTP_TOO_MANY_REQUESTS -> {
                MetNoForecastClientResult.Failure(MetNoForecastClientError.RateLimited(response.statusCode, errorClass))
            }
            errorClass.isIllegalUserAgentClass() || response.statusCode == HTTP_FORBIDDEN -> {
                MetNoForecastClientResult.Failure(MetNoForecastClientError.IllegalIdentification(response.statusCode, errorClass))
            }
            errorClass.isProviderUnavailableClass() || response.statusCode in HTTP_SERVER_FAILURES -> {
                MetNoForecastClientResult.Failure(MetNoForecastClientError.ProviderUnavailable(response.statusCode, errorClass))
            }
            errorClass.isInvalidRequestClass() || response.statusCode == HTTP_BAD_REQUEST -> {
                MetNoForecastClientResult.Failure(MetNoForecastClientError.InvalidResponse(response.statusCode, errorClass))
            }
            errorClass.isUnsupportedForecastClass() -> {
                MetNoForecastClientResult.Failure(MetNoForecastClientError.UnsupportedForecastData(response.statusCode, errorClass))
            }
            else -> {
                MetNoForecastClientResult.Failure(MetNoForecastClientError.UnexpectedHttpFailure(response.statusCode, errorClass))
            }
        }
    }

    private fun parseSuccessfulBody(
        body: String,
        cacheHeaders: MetNoForecastCacheHeaders,
    ): MetNoForecastClientResult =
        try {
            MetNoForecastClientResult.Success(
                response = MetNoForecastParser.parseForecast(body),
                cacheHeaders = cacheHeaders,
            )
        } catch (error: MetNoParseException) {
            MetNoForecastClientResult.Failure(MetNoForecastClientError.InvalidResponse(HTTP_OK, null))
        }

    private fun MetNoHttpResponse.cacheHeaders(): MetNoForecastCacheHeaders =
        MetNoForecastCacheHeaders(
            expires = headerValue("Expires"),
            lastModified = headerValue("Last-Modified"),
            etag = headerValue("ETag"),
        )

    private fun MetNoHttpResponse.headerValue(name: String): String? =
        headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value

    private fun Double.isValidLatitude(): Boolean =
        isFinite() && this >= -90.0 && this <= 90.0

    private fun Double.isValidLongitude(): Boolean =
        isFinite() && this >= -180.0 && this <= 180.0

    private fun Double.formatCoordinate(): String =
        BigDecimal.valueOf(this)
            .setScale(MAX_COORDINATE_DECIMALS, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()

    private fun String.encode(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8.name())

    private fun String.isValidMetNoUserAgent(): Boolean {
        val trimmed = trim()
        if (trimmed.isBlank()) return false
        val lower = trimmed.lowercase()
        val genericPrefixes = listOf(
            "java",
            "dalvik",
            "android",
            "okhttp",
            "mozilla/",
            "chrome/",
            "safari/",
            "curl/",
        )
        if (genericPrefixes.any { lower.startsWith(it) }) return false
        return "OxygenWeather/" in trimmed &&
            ("https://" in trimmed || "http://" in trimmed || "mailto:" in trimmed)
    }

    private fun String?.isRateLimitClass(): Boolean =
        equals("Ratelimitation", ignoreCase = true)

    private fun String?.isIllegalUserAgentClass(): Boolean =
        equals("IllegalUserAgent", ignoreCase = true)

    private fun String?.isProviderUnavailableClass(): Boolean =
        this?.lowercase() in setOf(
            "backenderror",
            "backend",
            "internalerror",
            "internal",
            "serviceunavailable",
            "temporarilyunavailable",
        )

    private fun String?.isInvalidRequestClass(): Boolean =
        this?.lowercase() in setOf(
            "parametererror",
            "parameters",
            "formaterror",
            "format",
            "validationerror",
            "validation",
            "invalidrequest",
        )

    private fun String?.isUnsupportedForecastClass(): Boolean =
        this?.lowercase() in setOf(
            "nodata",
            "no-data",
            "outsidearea",
            "outside-area",
            "outsidetime",
            "outsidetimerange",
            "outside-time-range",
            "outsideforecastrange",
        )

    companion object {
        const val DEFAULT_BASE_URL = "https://api.met.no/weatherapi/locationforecast/2.0/compact"
        const val DEFAULT_USER_AGENT = "OxygenWeather/0.1.0 https://github.com/oxygen-weather/oxygen"

        private const val MAX_COORDINATE_DECIMALS = 4
        private const val ERROR_CLASS_HEADER = "X-ErrorClass"
        private const val HTTP_OK = 200
        private const val HTTP_NOT_MODIFIED = 304
        private const val HTTP_BAD_REQUEST = 400
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_TOO_MANY_REQUESTS = 429
        private val HTTP_SERVER_FAILURES = setOf(500, 502, 503)
    }
}

sealed class MetNoForecastClientResult {
    data class Success(
        val response: MetNoForecastResponse,
        val cacheHeaders: MetNoForecastCacheHeaders,
    ) : MetNoForecastClientResult()

    data class NotModified(
        val cacheHeaders: MetNoForecastCacheHeaders,
    ) : MetNoForecastClientResult()

    data class Failure(
        val error: MetNoForecastClientError,
    ) : MetNoForecastClientResult()
}

sealed class MetNoForecastClientError {
    data object InvalidRequest : MetNoForecastClientError()
    data object NetworkUnavailable : MetNoForecastClientError()

    data class RateLimited(
        val statusCode: Int,
        val errorClass: String?,
    ) : MetNoForecastClientError()

    data class ProviderUnavailable(
        val statusCode: Int,
        val errorClass: String?,
    ) : MetNoForecastClientError()

    data class IllegalIdentification(
        val statusCode: Int?,
        val errorClass: String?,
    ) : MetNoForecastClientError()

    data class InvalidResponse(
        val statusCode: Int,
        val errorClass: String?,
    ) : MetNoForecastClientError()

    data class UnsupportedForecastData(
        val statusCode: Int,
        val errorClass: String?,
    ) : MetNoForecastClientError()

    data class UnexpectedHttpFailure(
        val statusCode: Int,
        val errorClass: String?,
    ) : MetNoForecastClientError()
}

data class MetNoForecastCacheHeaders(
    val expires: String? = null,
    val lastModified: String? = null,
    val etag: String? = null,
)

data class MetNoHttpRequest(
    val url: URL,
    val headers: Map<String, String>,
)

data class MetNoHttpResponse(
    val statusCode: Int,
    val headers: Map<String, String> = emptyMap(),
    val body: String = "",
)

fun interface MetNoHttpTransport {
    @Throws(IOException::class)
    fun get(request: MetNoHttpRequest): MetNoHttpResponse
}

class UrlConnectionMetNoHttpTransport(
    private val connectTimeoutMs: Int = 10_000,
    private val readTimeoutMs: Int = 10_000,
) : MetNoHttpTransport {
    override fun get(request: MetNoHttpRequest): MetNoHttpResponse {
        val connection = (request.url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            request.headers.forEach { (name, value) -> setRequestProperty(name, value) }
        }

        return try {
            val statusCode = connection.responseCode
            val body = if (statusCode == HTTP_NOT_MODIFIED) {
                ""
            } else if (statusCode in 200..399) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }
            MetNoHttpResponse(
                statusCode = statusCode,
                headers = connection.headerFields
                    .filterKeys { it != null }
                    .mapValues { (_, values) -> values.orEmpty().joinToString(",") },
                body = body,
            )
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val HTTP_NOT_MODIFIED = 304
    }
}
