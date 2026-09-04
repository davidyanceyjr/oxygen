package com.oxygen.weather.app

import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import com.oxygen.weather.core.provider.cache.ForecastCacheStorage
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class InstalledForecastRepositoryFactoryTest {
    private val location = WeatherLocation(
        id = LocationId("manual-installed-fallback"),
        displayName = "Installed Fallback City",
        point = GeoPoint(41.875, -87.625),
        elevationMeters = 181.6,
        zoneId = ZoneId.of("America/Chicago"),
    )

    @Test
    fun `open meteo success is terminal and does not call met norway fallback`() {
        val openMeteoBundle = bundle("open-meteo", "Open-Meteo")
        val openMeteo = InstalledRecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Loading, WeatherRepositoryResult.Success(openMeteoBundle)),
        )
        val metNorway = InstalledRecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Success(bundle("met-norway", "MET Norway"))),
        )
        val repository = InstalledForecastRepositoryFactory.create(
            storage = InMemoryForecastCacheStorage(),
            defaultRepository = openMeteo,
            fallbackRepository = metNorway,
        )

        val success = repository.refresh(location).terminalSuccess()

        assertSame(openMeteoBundle, success.weather)
        assertEquals(listOf(location), openMeteo.locations)
        assertEquals(emptyList<WeatherLocation>(), metNorway.locations)
    }

    @Test
    fun `fallback ineligible open meteo failure is terminal and skips met norway`() {
        val openMeteo = InstalledRecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Failure(ForecastError.NetworkUnavailable)),
        )
        val metNorway = InstalledRecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Success(bundle("met-norway", "MET Norway"))),
        )
        val repository = InstalledForecastRepositoryFactory.create(
            storage = InMemoryForecastCacheStorage(),
            defaultRepository = openMeteo,
            fallbackRepository = metNorway,
        )

        val failure = repository.refresh(location).terminalFailure()

        assertEquals(ForecastError.NetworkUnavailable, failure.error)
        assertEquals(listOf(ForecastError.NetworkUnavailable), failure.diagnostics)
        assertEquals(emptyList<WeatherLocation>(), metNorway.locations)
    }

    @Test
    fun `fallback eligible open meteo failure reaches Home ready state with met norway provenance`() {
        val metNorwayBundle = bundle("met-norway", "MET Norway")
        val openMeteo = InstalledRecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Failure(ForecastError.ProviderUnavailable("open-meteo"))),
        )
        val metNorway = InstalledRecordingWeatherRepository(
            listOf(WeatherRepositoryResult.Loading, WeatherRepositoryResult.Success(metNorwayBundle)),
        )
        val repository = InstalledForecastRepositoryFactory.create(
            storage = InMemoryForecastCacheStorage(),
            defaultRepository = openMeteo,
            fallbackRepository = metNorway,
        )

        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            forecastExecutor = InstalledDirectExecutor,
        )

        val ready = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady
        assertEquals("MET Norway", ready.dashboard.source.sourceName)
        assertEquals("NLOD 2.0", ready.dashboard.source.license)
        assertEquals(listOf(location), openMeteo.locations)
        assertEquals(listOf(location), metNorway.locations)
    }

    @Test
    fun `double failure keeps provider neutral terminal diagnostics`() {
        val openMeteoError = ForecastError.RateLimited("open-meteo")
        val metNorwayError = ForecastError.InvalidResponse("met-norway")
        val repository = InstalledForecastRepositoryFactory.create(
            storage = InMemoryForecastCacheStorage(),
            defaultRepository = InstalledRecordingWeatherRepository(listOf(WeatherRepositoryResult.Failure(openMeteoError))),
            fallbackRepository = InstalledRecordingWeatherRepository(listOf(WeatherRepositoryResult.Failure(metNorwayError))),
        )

        val failure = repository.refresh(location).terminalFailure()

        assertEquals(metNorwayError, failure.error)
        assertEquals(listOf(openMeteoError, metNorwayError), failure.diagnostics)
    }

    @Test
    fun `factory exposes identifying met norway user agent default`() {
        val userAgent = InstalledForecastRepositoryFactory.metNorwayUserAgent

        assertTrue(userAgent.contains("OxygenWeather/"))
        assertTrue(userAgent.contains("https://") || userAgent.contains("http://") || userAgent.contains("mailto:"))
    }

    private fun Sequence<WeatherRepositoryResult>.terminalSuccess(): WeatherRepositoryResult.Success =
        toList().last() as WeatherRepositoryResult.Success

    private fun Sequence<WeatherRepositoryResult>.terminalFailure(): WeatherRepositoryResult.Failure =
        toList().last() as WeatherRepositoryResult.Failure

    private fun bundle(providerId: String, sourceName: String): WeatherBundle =
        WeatherBundle(
            location = location,
            current = CurrentConditions(
                time = fetchedAt,
                temperatureC = 22.0,
                condition = WeatherCondition.PARTLY_CLOUDY,
                provenance = DataProvenance(
                    providerId = providerId,
                    sourceName = sourceName,
                    fetchedAt = fetchedAt,
                    type = DataType.MODEL_ESTIMATE,
                    licenseId = if (providerId == "met-norway") "NLOD 2.0" else "CC BY 4.0",
                ),
            ),
            fetchedAt = fetchedAt,
        )

    private companion object {
        val fetchedAt: Instant = Instant.parse("2026-08-23T13:20:00Z")
    }
}

private object InstalledDirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class InstalledRecordingWeatherRepository(
    private vararg val responses: List<WeatherRepositoryResult>,
) : WeatherRepository {
    val locations = mutableListOf<WeatherLocation>()
    private var callIndex = 0

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        locations += location
        val response = responses.getOrElse(callIndex) { responses.last() }
        callIndex += 1
        return response.asSequence()
    }
}

private class InMemoryForecastCacheStorage : ForecastCacheStorage {
    private val bundles = mutableMapOf<LocationId, WeatherBundle>()

    override fun replaceBundle(bundle: WeatherBundle) {
        bundles[bundle.location.id] = bundle
    }

    override fun readBundle(locationId: LocationId): WeatherBundle? = bundles[locationId]
}
