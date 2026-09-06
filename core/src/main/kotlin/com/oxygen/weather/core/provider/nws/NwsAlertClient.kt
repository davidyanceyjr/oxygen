package com.oxygen.weather.core.provider.nws

import com.oxygen.weather.core.model.GeoPoint
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class NwsAlertConfiguration(
    val baseUri: URI = URI("https://api.weather.gov/alerts/active"),
    val userAgent: String = "OxygenWeather/0.1 (https://github.com/davidyanceyjr/oxygen/issues)",
    val accept: String = "application/geo+json",
)

data class NwsAlertHttpResponse(
    val statusCode: Int,
    val body: String,
    val contentType: String? = null,
    val headers: Map<String, String> = emptyMap(),
)

fun interface NwsAlertHttpTransport {
    @Throws(IOException::class)
    fun get(url: URL, headers: Map<String, String>): NwsAlertHttpResponse
}

class NwsAlertClient(
    private val configuration: NwsAlertConfiguration = NwsAlertConfiguration(),
    private val transport: NwsAlertHttpTransport = UrlConnectionNwsAlertHttpTransport(),
) {
    internal fun fetch(point: GeoPoint): NwsAlertClientResult {
        if (!point.isValid()) return NwsAlertClientResult.Failure(NwsAlertClientError.InvalidPoint)
        val url = try {
            require(configuration.baseUri.isAbsolute)
            require(configuration.baseUri.scheme == "https")
            require(!configuration.baseUri.host.isNullOrBlank())
            require(configuration.baseUri.query == null && configuration.baseUri.fragment == null)
            configuration.baseUri.resolve("?point=${point.latitude},${point.longitude}").toURL()
        } catch (_: Exception) {
            return NwsAlertClientResult.Failure(NwsAlertClientError.InvalidRequest)
        }
        val response = try {
            transport.get(url, mapOf("User-Agent" to configuration.userAgent, "Accept" to configuration.accept))
        } catch (_: IOException) {
            return NwsAlertClientResult.Failure(NwsAlertClientError.Network)
        } catch (_: Exception) {
            return NwsAlertClientResult.Failure(NwsAlertClientError.UnexpectedProvider)
        }
        return when (response.statusCode) {
            in 200..299 -> if (response.contentType?.substringBefore(';')?.trim()?.equals("application/geo+json", true) != true) {
                NwsAlertClientResult.Failure(NwsAlertClientError.InvalidResponse)
            } else try { NwsAlertClientResult.Success(NwsAlertParser.parseActiveAlerts(response.body), response) } catch (_: Exception) {
                NwsAlertClientResult.Failure(NwsAlertClientError.InvalidResponse)
            }
            400 -> classifyBadRequest(response)
            401, 403 -> NwsAlertClientResult.Failure(NwsAlertClientError.IdentificationRejected)
            429 -> NwsAlertClientResult.Failure(NwsAlertClientError.RateLimited(response.header("retry-after")))
            in 500..599 -> NwsAlertClientResult.Failure(NwsAlertClientError.ProviderUnavailable)
            else -> NwsAlertClientResult.Failure(NwsAlertClientError.UnexpectedProvider)
        }
    }

    private fun classifyBadRequest(response: NwsAlertHttpResponse): NwsAlertClientResult {
        if (!response.contentType.orEmpty().substringBefore(';').trim().equals("application/problem+json", true))
            return NwsAlertClientResult.Failure(NwsAlertClientError.InvalidRequest)
        return try {
            val body = kotlinx.serialization.json.Json.parseToJsonElement(response.body).jsonObject
            val type = body["type"]?.jsonPrimitive?.content
            val title = body["title"]?.jsonPrimitive?.content
            val detail = body["detail"]?.jsonPrimitive?.content
            if (type == null || title == null || detail == null) NwsAlertClientResult.Failure(NwsAlertClientError.InvalidResponse)
            else if (type == UNSUPPORTED_TYPE && title == "Invalid Parameter" && detail == UNSUPPORTED_DETAIL)
                NwsAlertClientResult.Failure(NwsAlertClientError.UnsupportedRegion)
            else NwsAlertClientResult.Failure(NwsAlertClientError.InvalidRequest)
        } catch (_: Exception) { NwsAlertClientResult.Failure(NwsAlertClientError.InvalidResponse) }
    }

    private fun NwsAlertHttpResponse.header(name: String) = headers.entries.firstOrNull { it.key.equals(name, true) }?.value
    private fun GeoPoint.isValid() = latitude.isFinite() && latitude in -90.0..90.0 && longitude.isFinite() && longitude in -180.0..180.0
    private companion object { const val UNSUPPORTED_TYPE = "https://api.weather.gov/problems/InvalidParameter"; const val UNSUPPORTED_DETAIL = "Parameter \"point\" is invalid: out of bounds" }
}

internal sealed interface NwsAlertClientResult {
    data class Success(val collection: NwsAlertCollection, val response: NwsAlertHttpResponse) : NwsAlertClientResult
    data class Failure(val error: NwsAlertClientError) : NwsAlertClientResult
}
internal sealed interface NwsAlertClientError {
    data object InvalidPoint : NwsAlertClientError
    data object InvalidRequest : NwsAlertClientError
    data object UnsupportedRegion : NwsAlertClientError
    data object IdentificationRejected : NwsAlertClientError
    data object Network : NwsAlertClientError
    data class RateLimited(val retryAfter: String?) : NwsAlertClientError
    data object ProviderUnavailable : NwsAlertClientError
    data object InvalidResponse : NwsAlertClientError
    data object UnexpectedProvider : NwsAlertClientError
}

class UrlConnectionNwsAlertHttpTransport(private val connectTimeoutMs: Int = 10_000, private val readTimeoutMs: Int = 10_000) : NwsAlertHttpTransport {
    override fun get(url: URL, headers: Map<String, String>): NwsAlertHttpResponse {
        val connection = (url.openConnection() as HttpURLConnection).apply { requestMethod = "GET"; connectTimeout = connectTimeoutMs; readTimeout = readTimeoutMs; headers.forEach { (k, v) -> setRequestProperty(k, v) } }
        return try {
            val status = connection.responseCode
            val body = (if (status in 200..399) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
            NwsAlertHttpResponse(status, body, connection.contentType, connection.headerFields.filterKeys { it != null }.mapValues { it.value?.joinToString(", ") ?: "" })
        } finally { connection.disconnect() }
    }
}
