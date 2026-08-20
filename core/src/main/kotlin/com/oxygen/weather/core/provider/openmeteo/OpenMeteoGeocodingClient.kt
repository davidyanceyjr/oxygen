package com.oxygen.weather.core.provider.openmeteo

import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

data class OpenMeteoGeocodingRequest(
    val query: String,
    val count: Int = DEFAULT_COUNT,
    val language: String? = null,
    val countryCode: String? = null,
) {
    internal fun validated(): OpenMeteoValidatedGeocodingRequest? {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return null

        val normalizedCountryCode = countryCode?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { code ->
                if (!code.matches(Regex("[A-Za-z]{2}"))) return null
                code.uppercase(Locale.ROOT)
            }

        return OpenMeteoValidatedGeocodingRequest(
            query = normalizedQuery,
            count = count.coerceIn(MIN_COUNT, MAX_COUNT),
            language = language?.trim()?.takeIf { it.isNotEmpty() },
            countryCode = normalizedCountryCode,
        )
    }

    private companion object {
        const val DEFAULT_COUNT = 10
        const val MIN_COUNT = 1
        const val MAX_COUNT = 20
    }
}

internal data class OpenMeteoValidatedGeocodingRequest(
    val query: String,
    val count: Int,
    val language: String?,
    val countryCode: String?,
) {
    fun queryParameters(): List<Pair<String, String>> =
        buildList {
            add("name" to query)
            add("count" to count.toString())
            add("format" to "json")
            language?.let { add("language" to it) }
            countryCode?.let { add("countryCode" to it) }
        }
}

class OpenMeteoGeocodingClient(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val transport: OpenMeteoHttpTransport = UrlConnectionOpenMeteoHttpTransport(),
) {
    fun search(request: OpenMeteoGeocodingRequest): OpenMeteoGeocodingClientResult {
        val validatedRequest = request.validated()
            ?: return OpenMeteoGeocodingClientResult.Failure(OpenMeteoGeocodingClientError.InvalidRequest)

        val response = try {
            transport.get(buildUrl(validatedRequest))
        } catch (error: IOException) {
            return OpenMeteoGeocodingClientResult.Failure(OpenMeteoGeocodingClientError.NetworkUnavailable)
        }

        return when (response.statusCode) {
            HTTP_OK -> parseSuccessfulBody(response.body)
            HTTP_TOO_MANY_REQUESTS -> OpenMeteoGeocodingClientResult.Failure(OpenMeteoGeocodingClientError.RateLimited)
            in HTTP_SERVER_ERROR_START..HTTP_SERVER_ERROR_END -> {
                OpenMeteoGeocodingClientResult.Failure(
                    OpenMeteoGeocodingClientError.ProviderUnavailable(response.statusCode),
                )
            }
            in HTTP_CLIENT_ERROR_START..HTTP_CLIENT_ERROR_END -> classifyProviderRejection(response)
            else -> {
                OpenMeteoGeocodingClientResult.Failure(
                    OpenMeteoGeocodingClientError.UnexpectedHttpFailure(response.statusCode),
                )
            }
        }
    }

    private fun parseSuccessfulBody(body: String): OpenMeteoGeocodingClientResult =
        try {
            OpenMeteoGeocodingClientResult.Success(OpenMeteoGeocodingParser.parseSearch(body))
        } catch (error: OpenMeteoGeocodingException.ProviderErrorBody) {
            OpenMeteoGeocodingClientResult.Failure(
                OpenMeteoGeocodingClientError.ProviderRejectedRequest(
                    statusCode = HTTP_OK,
                    reason = error.reason,
                ),
            )
        } catch (error: OpenMeteoGeocodingException) {
            OpenMeteoGeocodingClientResult.Failure(OpenMeteoGeocodingClientError.InvalidResponse)
        }

    private fun classifyProviderRejection(response: OpenMeteoHttpResponse): OpenMeteoGeocodingClientResult =
        try {
            OpenMeteoGeocodingParser.parseSearch(response.body)
            OpenMeteoGeocodingClientResult.Failure(
                OpenMeteoGeocodingClientError.UnexpectedHttpFailure(response.statusCode),
            )
        } catch (error: OpenMeteoGeocodingException.ProviderErrorBody) {
            OpenMeteoGeocodingClientResult.Failure(
                OpenMeteoGeocodingClientError.ProviderRejectedRequest(
                    statusCode = response.statusCode,
                    reason = error.reason,
                ),
            )
        } catch (error: OpenMeteoGeocodingException) {
            OpenMeteoGeocodingClientResult.Failure(
                OpenMeteoGeocodingClientError.UnexpectedHttpFailure(response.statusCode),
            )
        }

    private fun buildUrl(request: OpenMeteoValidatedGeocodingRequest): URL {
        val separator = if (baseUrl.contains("?")) "&" else "?"
        val query = request.queryParameters().joinToString("&") { (key, value) ->
            "${key.encode()}=${value.encode()}"
        }
        return URL("$baseUrl$separator$query")
    }

    private fun String.encode(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8.name())

    private companion object {
        const val DEFAULT_BASE_URL = "https://geocoding-api.open-meteo.com/v1/search"
        const val HTTP_OK = 200
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val HTTP_CLIENT_ERROR_START = 400
        const val HTTP_CLIENT_ERROR_END = 499
        const val HTTP_SERVER_ERROR_START = 500
        const val HTTP_SERVER_ERROR_END = 599
    }
}

sealed class OpenMeteoGeocodingClientResult {
    data class Success(
        val response: OpenMeteoGeocodingResponse,
    ) : OpenMeteoGeocodingClientResult()

    data class Failure(
        val error: OpenMeteoGeocodingClientError,
    ) : OpenMeteoGeocodingClientResult()
}

sealed class OpenMeteoGeocodingClientError {
    data object InvalidRequest : OpenMeteoGeocodingClientError()
    data object NetworkUnavailable : OpenMeteoGeocodingClientError()
    data object RateLimited : OpenMeteoGeocodingClientError()
    data object InvalidResponse : OpenMeteoGeocodingClientError()

    data class ProviderUnavailable(
        val statusCode: Int,
    ) : OpenMeteoGeocodingClientError()

    data class ProviderRejectedRequest(
        val statusCode: Int,
        val reason: String,
    ) : OpenMeteoGeocodingClientError()

    data class UnexpectedHttpFailure(
        val statusCode: Int,
    ) : OpenMeteoGeocodingClientError()
}
