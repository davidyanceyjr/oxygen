package com.oxygen.weather.core.provider

import com.oxygen.weather.core.model.WeatherLocation

class FallbackWeatherRepository(
    private val defaultRepository: WeatherRepository,
    private val fallbackRepository: WeatherRepository,
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> = sequence {
        yield(WeatherRepositoryResult.Loading)

        val defaultTerminal = defaultRepository.refresh(location).firstTerminalResult()
        when (defaultTerminal) {
            is WeatherRepositoryResult.Success -> yield(defaultTerminal)
            is WeatherRepositoryResult.Failure -> {
                if (!defaultTerminal.error.isFallbackEligible()) {
                    yield(defaultTerminal)
                    return@sequence
                }

                when (val fallbackTerminal = fallbackRepository.refresh(location).firstTerminalResult()) {
                    is WeatherRepositoryResult.Success -> yield(fallbackTerminal)
                    is WeatherRepositoryResult.Failure -> yield(
                        WeatherRepositoryResult.Failure(
                            error = fallbackTerminal.error,
                            diagnostics = listOf(defaultTerminal.error, fallbackTerminal.error),
                        ),
                    )
                    WeatherRepositoryResult.Loading -> error("Loading is not terminal")
                }
            }
            WeatherRepositoryResult.Loading -> error("Loading is not terminal")
        }
    }

    private fun ForecastError.isFallbackEligible(): Boolean =
        when (this) {
            ForecastError.NetworkUnavailable,
            ForecastError.LocalCacheFailure,
            is ForecastError.ProviderRejectedRequest,
            -> false
            is ForecastError.RateLimited,
            is ForecastError.ProviderUnavailable,
            is ForecastError.InvalidResponse,
            is ForecastError.UnexpectedProviderFailure,
            -> true
        }

    private fun Sequence<WeatherRepositoryResult>.firstTerminalResult(): WeatherRepositoryResult =
        first { it !is WeatherRepositoryResult.Loading }
}
