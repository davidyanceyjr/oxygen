package com.oxygen.weather.core.provider.openmeteo

import com.oxygen.weather.core.provider.GeocodingError
import com.oxygen.weather.core.provider.GeocodingRepository
import com.oxygen.weather.core.provider.GeocodingRepositoryResult

class OpenMeteoGeocodingRepository(
    private val client: OpenMeteoGeocodingClient = OpenMeteoGeocodingClient(),
) : GeocodingRepository {
    override fun search(
        query: String,
        count: Int,
        language: String?,
        countryCode: String?,
    ): Sequence<GeocodingRepositoryResult> = sequence {
        yield(GeocodingRepositoryResult.Loading)

        val request = OpenMeteoGeocodingRequest(
            query = query,
            count = count,
            language = language,
            countryCode = countryCode,
        )

        when (val result = client.search(request)) {
            is OpenMeteoGeocodingClientResult.Success -> {
                try {
                    val candidates = OpenMeteoGeocodingMapper.map(result.response)
                    if (candidates.isEmpty()) {
                        yield(GeocodingRepositoryResult.Empty)
                    } else {
                        yield(GeocodingRepositoryResult.Success(candidates))
                    }
                } catch (error: OpenMeteoGeocodingException) {
                    yield(GeocodingRepositoryResult.Failure(GeocodingError.InvalidResponse(PROVIDER_ID)))
                }
            }
            is OpenMeteoGeocodingClientResult.Failure -> {
                yield(GeocodingRepositoryResult.Failure(result.error.toGeocodingError()))
            }
        }
    }

    private fun OpenMeteoGeocodingClientError.toGeocodingError(): GeocodingError =
        when (this) {
            OpenMeteoGeocodingClientError.InvalidRequest -> GeocodingError.InvalidQuery
            OpenMeteoGeocodingClientError.NetworkUnavailable -> GeocodingError.NetworkUnavailable
            OpenMeteoGeocodingClientError.RateLimited -> GeocodingError.RateLimited(PROVIDER_ID)
            OpenMeteoGeocodingClientError.InvalidResponse -> GeocodingError.InvalidResponse(PROVIDER_ID)
            is OpenMeteoGeocodingClientError.ProviderUnavailable -> {
                GeocodingError.ProviderUnavailable(PROVIDER_ID)
            }
            is OpenMeteoGeocodingClientError.ProviderRejectedRequest -> {
                GeocodingError.ProviderRejectedRequest(PROVIDER_ID)
            }
            is OpenMeteoGeocodingClientError.UnexpectedHttpFailure -> {
                GeocodingError.UnexpectedProviderFailure(PROVIDER_ID)
            }
        }

    private companion object {
        const val PROVIDER_ID = "open-meteo"
    }
}
