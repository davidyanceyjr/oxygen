package com.oxygen.weather.app

import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.GeocodingRepository
import com.oxygen.weather.core.provider.GeocodingRepositoryResult
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeForecastStateHolderTest {
    @Test
    fun `no selected location stays first-run and does not request weather`() {
        val weatherRepository = RecordingWeatherRepository(listOf(WeatherRepositoryResult.Loading))

        val stateHolder = OxygenAppStateHolder(
            weatherRepository = weatherRepository,
            forecastExecutor = DirectForecastExecutor,
        )

        assertTrue(stateHolder.presentationState.screen is OxygenAppScreen.FirstRunLocationEntry)
        assertEquals(emptyList<WeatherLocation>(), weatherRepository.locations)
        assertFalse(stateHolder.presentationState.isShowingHome)
    }

    @Test
    fun `selected manual candidate starts one home forecast load for exact location`() {
        val location = weatherLocation("manual-madison", "Madison")
        val weatherRepository = RecordingWeatherRepository(listOf(WeatherRepositoryResult.Loading))
        val stateHolder = OxygenAppStateHolder(
            geocodingRepository = StaticGeocodingRepository(location),
            weatherRepository = weatherRepository,
            searchExecutor = DirectForecastExecutor,
            forecastExecutor = DirectForecastExecutor,
        )

        stateHolder.onManualLocationQueryChanged("Madison")
        stateHolder.onManualLocationSearchSubmitted()
        val result = ((stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry)
            .searchState as ManualLocationSearchState.Results).candidates.single()
        stateHolder.onManualLocationCandidateSelected(result.id)

        val home = stateHolder.presentationState.screen as OxygenAppScreen.Home
        val loading = home.forecast as HomeForecastPresentationState.Loading
        assertEquals(listOf(result.location), weatherRepository.locations)
        assertSame(result.location, stateHolder.presentationState.selectedLocation)
        assertSame(result.location, loading.location)
        assertTrue(loading.statusText.contains("Madison"))
        assertTrue(loading.forecastDisclosure.contains("Open-Meteo"))
        assertTrue(loading.forecastPrivacyNote.contains("coordinates"))
        assertTrue(loading.forecastPrivacyNote.contains("timezone"))
    }

    @Test
    fun `repository success becomes visible non-loading terminal home state`() {
        val location = weatherLocation("manual-chicago", "Chicago")
        val bundle = weatherBundle(location)
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = RecordingWeatherRepository(
                listOf(
                    WeatherRepositoryResult.Loading,
                    WeatherRepositoryResult.Success(bundle),
                ),
            ),
            forecastExecutor = DirectForecastExecutor,
        )

        val home = stateHolder.presentationState.screen as OxygenAppScreen.Home
        val ready = home.forecast as HomeForecastPresentationState.ForecastReady
        assertSame(location, stateHolder.presentationState.selectedLocation)
        assertSame(location, ready.location)
        assertTrue(ready.statusText.contains("Dashboard display is coming in a later slice"))
        assertTrue(ready.forecastPrivacyNote.contains("Open-Meteo"))
    }

    @Test
    fun `repository failures map to retryable provider-neutral no-cache home errors`() {
        val location = weatherLocation("manual-error", "Error City")
        val cases = listOf(
            ForecastError.NetworkUnavailable to HomeForecastMessage.NetworkUnavailable,
            ForecastError.RateLimited("open-meteo") to HomeForecastMessage.RateLimited,
            ForecastError.ProviderUnavailable("open-meteo") to HomeForecastMessage.ProviderUnavailable,
            ForecastError.InvalidResponse("open-meteo") to HomeForecastMessage.InvalidResponse,
            ForecastError.ProviderRejectedRequest("open-meteo") to HomeForecastMessage.RejectedRequest,
            ForecastError.UnexpectedProviderFailure("open-meteo") to HomeForecastMessage.UnexpectedFailure,
        )

        cases.forEach { (error, expectedMessage) ->
            val stateHolder = OxygenAppStateHolder(
                selectedLocation = location,
                weatherRepository = RecordingWeatherRepository(
                    listOf(
                        WeatherRepositoryResult.Loading,
                        WeatherRepositoryResult.Failure(error),
                    ),
                ),
                forecastExecutor = DirectForecastExecutor,
            )

            val home = stateHolder.presentationState.screen as OxygenAppScreen.Home
            val forecastError = home.forecast as HomeForecastPresentationState.NoCacheError
            assertSame(location, forecastError.location)
            assertEquals(expectedMessage, forecastError.message)
            assertTrue(forecastError.canRetry)
            assertFalse(forecastError.message.text.contains("open-meteo"))
            assertFalse(forecastError.message.text.contains("providerId"))
            assertTrue(forecastError.forecastDisclosure.contains("Open-Meteo"))
        }
    }

    @Test
    fun `home retry requests weather again for same selected location`() {
        val location = weatherLocation("manual-retry", "Retry City")
        val weatherRepository = RecordingWeatherRepository(
            listOf(
                WeatherRepositoryResult.Loading,
                WeatherRepositoryResult.Failure(ForecastError.NetworkUnavailable),
            ),
            listOf(
                WeatherRepositoryResult.Loading,
                WeatherRepositoryResult.Failure(ForecastError.ProviderUnavailable("open-meteo")),
            ),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = weatherRepository,
            forecastExecutor = DirectForecastExecutor,
        )

        stateHolder.onHomeForecastRetry()

        val home = stateHolder.presentationState.screen as OxygenAppScreen.Home
        val error = home.forecast as HomeForecastPresentationState.NoCacheError
        assertEquals(listOf(location, location), weatherRepository.locations)
        assertSame(location, stateHolder.presentationState.selectedLocation)
        assertSame(location, error.location)
        assertEquals(HomeForecastMessage.ProviderUnavailable, error.message)
    }

    @Test
    fun `obsolete retry emissions cannot replace active home forecast state`() {
        val location = weatherLocation("manual-stale", "Stale City")
        val weatherRepository = ControlledWeatherRepository()
        val executor = Executors.newFixedThreadPool(2)
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = weatherRepository,
            forecastExecutor = executor,
        )

        try {
            val first = weatherRepository.awaitCall(0)
            first.emit(WeatherRepositoryResult.Loading)
            awaitHomeState<HomeForecastPresentationState.Loading>(stateHolder)

            stateHolder.onHomeForecastRetry()
            val second = weatherRepository.awaitCall(1)
            second.emit(WeatherRepositoryResult.Loading)
            second.emit(WeatherRepositoryResult.Success(weatherBundle(location)))
            val ready = awaitHomeState<HomeForecastPresentationState.ForecastReady>(stateHolder)

            first.emit(WeatherRepositoryResult.Failure(ForecastError.NetworkUnavailable))
            Thread.sleep(100)

            assertSame(location, ready.location)
            assertTrue(stateHolder.presentationState.screen is OxygenAppScreen.Home)
            assertTrue((stateHolder.presentationState.screen as OxygenAppScreen.Home).forecast is HomeForecastPresentationState.ForecastReady)
            assertEquals(listOf(location, location), weatherRepository.locations)
        } finally {
            weatherRepository.finishAll()
            executor.shutdownNow()
        }
    }
}

private object DirectForecastExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class StaticGeocodingRepository(
    private val location: WeatherLocation,
) : GeocodingRepository {
    override fun search(
        query: String,
        count: Int,
        language: String?,
        countryCode: String?,
    ): Sequence<GeocodingRepositoryResult> =
        sequenceOf(
            GeocodingRepositoryResult.Success(
                listOf(
                    com.oxygen.weather.core.model.GeocodingLocationCandidate(
                        locationId = location.id,
                        displayName = location.displayName,
                        point = location.point,
                        zoneId = location.zoneId,
                        country = "United States",
                        countryCode = "US",
                    ),
                ),
            ),
        )
}

private class RecordingWeatherRepository(
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

private class ControlledWeatherRepository : WeatherRepository {
    val locations = mutableListOf<WeatherLocation>()
    private val calls = mutableListOf<ControlledWeatherCall>()
    private val lock = ReentrantLock()
    private val callAdded = lock.newCondition()

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        locations += location
        val call = ControlledWeatherCall()
        lock.withLock {
            calls += call
            callAdded.signalAll()
        }
        return call.results()
    }

    fun awaitCall(index: Int): ControlledWeatherCall {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2)
        lock.withLock {
            while (calls.size <= index && System.nanoTime() < deadline) {
                callAdded.await(10, TimeUnit.MILLISECONDS)
            }
            if (calls.size <= index) error("Timed out waiting for weather repository call $index")
            return calls[index]
        }
    }

    fun finishAll() {
        lock.withLock {
            calls.forEach { it.finish() }
        }
    }
}

private class ControlledWeatherCall {
    private val results = LinkedBlockingQueue<ControlledWeatherResult>()

    fun results(): Sequence<WeatherRepositoryResult> = sequence {
        while (true) {
            when (val result = results.take()) {
                ControlledWeatherResult.Finished -> return@sequence
                is ControlledWeatherResult.Next -> yield(result.value)
            }
        }
    }

    fun emit(result: WeatherRepositoryResult) {
        results += ControlledWeatherResult.Next(result)
    }

    fun finish() {
        results += ControlledWeatherResult.Finished
    }
}

private sealed interface ControlledWeatherResult {
    data class Next(val value: WeatherRepositoryResult) : ControlledWeatherResult
    data object Finished : ControlledWeatherResult
}

private inline fun <reified T : HomeForecastPresentationState> awaitHomeState(
    stateHolder: OxygenAppStateHolder,
): T {
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2)
    while (System.nanoTime() < deadline) {
        val forecast = (stateHolder.presentationState.screen as? OxygenAppScreen.Home)?.forecast
        if (forecast is T) return forecast
        Thread.sleep(10)
    }
    error("Timed out waiting for Home forecast state ${T::class.java.simpleName}")
}

private fun weatherLocation(
    id: String,
    name: String,
): WeatherLocation =
    WeatherLocation(
        id = LocationId(id),
        displayName = name,
        point = GeoPoint(43.0731, -89.4012),
        zoneId = ZoneId.of("America/Chicago"),
    )

private fun weatherBundle(location: WeatherLocation): WeatherBundle =
    WeatherBundle(
        location = location,
        fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
    )
