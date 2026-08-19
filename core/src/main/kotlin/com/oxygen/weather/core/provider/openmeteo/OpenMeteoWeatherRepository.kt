package com.oxygen.weather.core.provider.openmeteo

import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.time.Instant

class OpenMeteoWeatherRepository(
    private val client: OpenMeteoForecastClient = OpenMeteoForecastClient(),
    private val clock: () -> Instant = Instant::now,
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> = sequence {
        yield(WeatherRepositoryResult.Loading)

        val request = OpenMeteoForecastRequest(
            latitude = location.point.latitude,
            longitude = location.point.longitude,
            timezone = location.zoneId.id,
        )

        when (val result = client.fetchForecast(request)) {
            is OpenMeteoForecastClientResult.Success -> {
                yield(
                    WeatherRepositoryResult.Success(
                        OpenMeteoForecastMapper.map(
                            location = location,
                            response = result.response,
                            fetchedAt = clock(),
                        ),
                    ),
                )
            }
            is OpenMeteoForecastClientResult.Failure -> {
                yield(WeatherRepositoryResult.Failure(result.error.toForecastError()))
            }
        }
    }

    private fun OpenMeteoForecastClientError.toForecastError(): ForecastError =
        when (this) {
            OpenMeteoForecastClientError.NetworkUnavailable -> ForecastError.NetworkUnavailable
            OpenMeteoForecastClientError.RateLimited -> ForecastError.RateLimited(PROVIDER_ID)
            OpenMeteoForecastClientError.InvalidResponse -> ForecastError.InvalidResponse(PROVIDER_ID)
            is OpenMeteoForecastClientError.ProviderUnavailable -> ForecastError.ProviderUnavailable(PROVIDER_ID)
            is OpenMeteoForecastClientError.ProviderRejectedRequest -> {
                ForecastError.ProviderRejectedRequest(PROVIDER_ID)
            }
            is OpenMeteoForecastClientError.UnexpectedHttpFailure -> {
                ForecastError.UnexpectedProviderFailure(PROVIDER_ID)
            }
        }

    private companion object {
        const val PROVIDER_ID = "open-meteo"
    }
}
