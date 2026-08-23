package com.oxygen.weather.core.provider.metno

import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.time.Instant

class MetNoWeatherRepository(
    private val client: MetNoForecastClient = MetNoForecastClient(),
    private val clock: () -> Instant = Instant::now,
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> = sequence {
        yield(WeatherRepositoryResult.Loading)

        val request = MetNoForecastRequest(
            latitude = location.point.latitude,
            longitude = location.point.longitude,
            altitudeMeters = location.elevationMeters,
        )

        when (val result = client.fetchForecast(request)) {
            is MetNoForecastClientResult.Success -> {
                val weather = try {
                    MetNoForecastMapper.map(
                        location = location,
                        response = result.response,
                        fetchedAt = clock(),
                    )
                } catch (error: MetNoMapperException) {
                    yield(WeatherRepositoryResult.Failure(ForecastError.InvalidResponse(PROVIDER_ID)))
                    return@sequence
                }
                yield(WeatherRepositoryResult.Success(weather))
            }
            is MetNoForecastClientResult.NotModified -> {
                yield(WeatherRepositoryResult.Failure(ForecastError.InvalidResponse(PROVIDER_ID)))
            }
            is MetNoForecastClientResult.Failure -> {
                yield(WeatherRepositoryResult.Failure(result.error.toForecastError()))
            }
        }
    }

    private fun MetNoForecastClientError.toForecastError(): ForecastError =
        when (this) {
            MetNoForecastClientError.NetworkUnavailable -> ForecastError.NetworkUnavailable
            MetNoForecastClientError.InvalidRequest -> ForecastError.ProviderRejectedRequest(PROVIDER_ID)
            is MetNoForecastClientError.RateLimited -> ForecastError.RateLimited(PROVIDER_ID)
            is MetNoForecastClientError.ProviderUnavailable -> ForecastError.ProviderUnavailable(PROVIDER_ID)
            is MetNoForecastClientError.IllegalIdentification -> ForecastError.ProviderRejectedRequest(PROVIDER_ID)
            is MetNoForecastClientError.InvalidResponse -> ForecastError.InvalidResponse(PROVIDER_ID)
            is MetNoForecastClientError.UnsupportedForecastData -> ForecastError.ProviderRejectedRequest(PROVIDER_ID)
            is MetNoForecastClientError.UnexpectedHttpFailure -> ForecastError.UnexpectedProviderFailure(PROVIDER_ID)
        }

    private companion object {
        const val PROVIDER_ID = "met-norway"
    }
}
