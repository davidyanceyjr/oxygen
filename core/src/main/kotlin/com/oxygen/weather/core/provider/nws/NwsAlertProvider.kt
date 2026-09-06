package com.oxygen.weather.core.provider.nws

import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.provider.*
import java.time.Clock

class NwsAlertProvider(
    private val client: NwsAlertClient = NwsAlertClient(),
    private val clock: Clock = Clock.systemUTC(),
) : AlertProvider {
    override val id: String = "nws"
    override suspend fun getActiveAlerts(location: GeoPoint): AlertProviderResult = when (val result = client.fetch(location)) {
        is NwsAlertClientResult.Success -> {
            val fetchedAt = clock.instant()
            try {
                AlertProviderResult.Success(
                    alerts = NwsAlertMapper.map(result.collection, fetchedAt),
                    metadata = AlertSuccessMetadata(
                        requestPoint = location,
                        providerId = id,
                        fetchedAt = fetchedAt,
                        cacheControl = result.response.header("cache-control"),
                        expires = result.response.header("expires"),
                        etag = result.response.header("etag"),
                        lastModified = result.response.header("last-modified"),
                    ),
                )
            } catch (_: Exception) {
                AlertProviderResult.Failure(AlertProviderError.InvalidResponse)
            }
        }
        is NwsAlertClientResult.Failure -> AlertProviderResult.Failure(result.error.toProviderError())
    }
    private fun NwsAlertHttpResponse.header(name: String) = headers.entries.firstOrNull { it.key.equals(name, true) }?.value
    private fun NwsAlertClientError.toProviderError() = when (this) {
        NwsAlertClientError.InvalidPoint -> AlertProviderError.InvalidPoint
        NwsAlertClientError.InvalidRequest -> AlertProviderError.InvalidRequest
        NwsAlertClientError.UnsupportedRegion -> AlertProviderError.UnsupportedRegion
        NwsAlertClientError.IdentificationRejected -> AlertProviderError.IdentificationRejected
        NwsAlertClientError.Network -> AlertProviderError.Network
        is NwsAlertClientError.RateLimited -> AlertProviderError.RateLimited(retryAfter)
        NwsAlertClientError.ProviderUnavailable -> AlertProviderError.ProviderUnavailable
        NwsAlertClientError.InvalidResponse -> AlertProviderError.InvalidResponse
        NwsAlertClientError.UnexpectedProvider -> AlertProviderError.UnexpectedProvider
    }
}
