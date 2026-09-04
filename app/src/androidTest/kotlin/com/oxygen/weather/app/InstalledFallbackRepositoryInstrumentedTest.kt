package com.oxygen.weather.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DailyForecast
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.HourlyForecast
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.model.Wind
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import com.oxygen.weather.core.provider.cache.ForecastCacheStorage
import com.oxygen.weather.core.provider.cache.room.RoomForecastCacheStorageFactory
import com.oxygen.weather.core.provider.metno.MetNoForecastClient
import com.oxygen.weather.core.provider.metno.MetNoHttpRequest
import com.oxygen.weather.core.provider.metno.MetNoHttpResponse
import com.oxygen.weather.core.provider.metno.MetNoHttpTransport
import com.oxygen.weather.core.provider.metno.MetNoWeatherRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstalledFallbackRepositoryInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun eligibleOpenMeteoFailureRendersMetNorwayHomeReadyThroughInstalledFactory() {
        FailingOpenMeteoRepository.locations.clear()
        val location = WeatherLocation(
            id = LocationId("android-installed-metno-fallback"),
            displayName = "Android MET Norway Fallback City",
            point = GeoPoint(41.875, -87.625),
            elevationMeters = 181.6,
            zoneId = ZoneId.of("America/Chicago"),
        )
        val metNorwayTransport = RecordingMetNoTransport(
            MetNoHttpResponse(
                statusCode = 200,
                headers = mapOf(
                    "Expires" to "Sun, 23 Aug 2026 15:00:00 GMT",
                    "Last-Modified" to "Sun, 23 Aug 2026 10:15:00 GMT",
                ),
                body = metNorwayForecastBody,
            ),
        )
        val repository = InstalledForecastRepositoryFactory.create(
            storage = InMemoryForecastCacheStorage(),
            defaultRepository = FailingOpenMeteoRepository,
            fallbackRepository = MetNoWeatherRepository(
                client = MetNoForecastClient(transport = metNorwayTransport),
                clock = { java.time.Instant.parse("2026-08-23T13:20:00Z") },
            ),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = repository,
            forecastExecutor = InstalledFallbackDirectExecutor,
        )

        composeRule.setContent {
            OxygenApp(stateHolder = stateHolder)
        }

        composeRule.onNodeWithText("Android MET Norway Fallback City", substring = true).assertIsDisplayed()
        composeRule.onAllNodesWithText("MET Norway", substring = true).assertCountEquals(2)
        composeRule.onNodeWithText("Model estimate", substring = true).assertIsDisplayed()
        assertEquals(listOf(location), FailingOpenMeteoRepository.locations)
        assertNotNull(metNorwayTransport.request)
        assertEquals(MetNoForecastClient.DEFAULT_USER_AGENT, metNorwayTransport.request?.headers?.get("User-Agent"))
        assertTrue(composeRule.onRoot().printToString().contains("MET Norway"))
    }

    @Test
    fun fallbackServedRoomForecastRestoresOfflineWithMetNorwayProvenanceThroughInstalledPath() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase("oxygen_forecast_cache.db")
        val selectedLocationStorage = DataStoreSelectedLocationStorage(context)
        val forecastCacheStorage = RoomForecastCacheStorageFactory.create(context)
        val location = weatherLocation(
            id = "android-installed-metno-room-restore-${System.nanoTime()}",
            name = "Android MET Norway Room Restore City",
        )
        selectedLocationStorage.writeSelectedLocation(location)
        val metNorwayTransport = RecordingMetNoTransport(
            MetNoHttpResponse(
                statusCode = 200,
                headers = mapOf(
                    "Expires" to "Sun, 23 Aug 2026 15:00:00 GMT",
                    "Last-Modified" to "Sun, 23 Aug 2026 10:15:00 GMT",
                    "ETag" to "\"metno-installed-room\"",
                ),
                body = metNorwayForecastBody,
            ),
        )
        val firstRunRepository = InstalledForecastRepositoryFactory.create(
            storage = forecastCacheStorage,
            defaultRepository = FailingOpenMeteoRepository,
            fallbackRepository = MetNoWeatherRepository(
                client = MetNoForecastClient(transport = metNorwayTransport),
                clock = { Instant.parse("2026-08-23T13:20:00Z") },
            ),
            clock = Clock.fixed(Instant.parse("2026-08-23T13:20:00Z"), ZoneId.of("UTC")),
        )
        val firstRunStateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = firstRunRepository,
            forecastCacheStorage = forecastCacheStorage,
            forecastExecutor = InstalledFallbackDirectExecutor,
            clock = Clock.fixed(Instant.parse("2026-08-23T13:20:00Z"), ZoneId.of("UTC")),
        )
        val fallbackReady = firstRunStateHolder.readyForecast()

        assertMetNorwayForecast(location, fallbackReady)
        assertEquals(emptyList<Any>(), fallbackReady.dashboard.alerts)
        assertFalse(fallbackReady.dashboard.visibleText().contains("SampleWeather"))

        val offlineRepository = InstalledForecastRepositoryFactory.create(
            storage = forecastCacheStorage,
            defaultRepository = NetworkUnavailableOpenMeteoRepository,
            fallbackRepository = UnexpectedFallbackRepository,
            clock = Clock.fixed(Instant.parse("2026-08-23T13:50:00Z"), ZoneId.of("UTC")),
        )
        val restoredStateHolder = OxygenAppStateHolder(
            selectedLocationStorage = selectedLocationStorage,
            weatherRepository = offlineRepository,
            forecastCacheStorage = forecastCacheStorage,
            forecastExecutor = InstalledFallbackDirectExecutor,
            clock = Clock.fixed(Instant.parse("2026-08-23T13:50:00Z"), ZoneId.of("UTC")),
        )
        val restoredReady = restoredStateHolder.readyForecast()

        composeRule.setContent {
            OxygenApp(stateHolder = restoredStateHolder)
        }

        assertEquals(location, restoredStateHolder.presentationState.selectedLocation)
        assertMetNorwayForecast(location, restoredReady)
        assertEquals("30 minutes", (restoredReady.freshness as HomeForecastFreshness.StaleAfterFailedRefresh).staleAgeText)
        assertEquals(HomeRefreshFailureMessage.NetworkUnavailable, restoredReady.freshness.refreshFailureMessage)
        assertEquals(emptyList<Any>(), restoredReady.dashboard.alerts)
        composeRule.onNodeWithText("Android MET Norway Room Restore City", substring = true).assertIsDisplayed()
        composeRule.onAllNodesWithText("MET Norway", substring = true).assertCountEquals(2)
        assertFalse(composeRule.onRoot().printToString().contains("Alerts"))
        assertFalse(composeRule.onRoot().printToString().contains("SampleWeather"))
        context.deleteDatabase("oxygen_forecast_cache.db")
    }

    @Test
    fun laterOpenMeteoRefreshReplacesCachedMetNorwayForecastThroughInstalledPath() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase("oxygen_forecast_cache.db")
        val selectedLocationStorage = DataStoreSelectedLocationStorage(context)
        val forecastCacheStorage = RoomForecastCacheStorageFactory.create(context)
        val location = weatherLocation(
            id = "android-installed-openmeteo-replacement-${System.nanoTime()}",
            name = "Android Open-Meteo Replacement City",
        )
        selectedLocationStorage.writeSelectedLocation(location)
        val fallbackRepository = InstalledForecastRepositoryFactory.create(
            storage = forecastCacheStorage,
            defaultRepository = FailingOpenMeteoRepository,
            fallbackRepository = MetNoWeatherRepository(
                client = MetNoForecastClient(
                    transport = RecordingMetNoTransport(
                        MetNoHttpResponse(
                            statusCode = 200,
                            headers = mapOf("Expires" to "Sun, 23 Aug 2026 15:00:00 GMT"),
                            body = metNorwayForecastBody,
                        ),
                    ),
                ),
                clock = { Instant.parse("2026-08-23T13:20:00Z") },
            ),
            clock = Clock.fixed(Instant.parse("2026-08-23T13:20:00Z"), ZoneId.of("UTC")),
        )
        fallbackRepository.refresh(location).last()
        assertEquals("MET Norway", forecastCacheStorage.readBundle(location.id)?.current?.provenance?.sourceName)

        val openMeteoRepository = InstalledForecastRepositoryFactory.create(
            storage = forecastCacheStorage,
            defaultRepository = SuccessWeatherRepository(openMeteoBundle(location)),
            fallbackRepository = UnexpectedFallbackRepository,
            clock = Clock.fixed(Instant.parse("2026-08-23T14:00:00Z"), ZoneId.of("UTC")),
        )
        val stateHolder = OxygenAppStateHolder(
            selectedLocationStorage = selectedLocationStorage,
            weatherRepository = openMeteoRepository,
            forecastCacheStorage = forecastCacheStorage,
            forecastExecutor = InstalledFallbackDirectExecutor,
            clock = Clock.fixed(Instant.parse("2026-08-23T14:00:00Z"), ZoneId.of("UTC")),
        )
        val ready = stateHolder.readyForecast()

        composeRule.setContent {
            OxygenApp(stateHolder = stateHolder)
        }

        assertOpenMeteoForecast(location, ready)
        assertEquals("Open-Meteo", forecastCacheStorage.readBundle(location.id)?.current?.provenance?.sourceName)
        assertEquals(emptyList<Any>(), ready.dashboard.alerts)
        composeRule.onNodeWithText("Android Open-Meteo Replacement City", substring = true).assertIsDisplayed()
        composeRule.onAllNodesWithText("Open-Meteo", substring = true).assertCountEquals(2)
        assertFalse(composeRule.onRoot().printToString().contains("MET Norway"))
        assertFalse(composeRule.onRoot().printToString().contains("Alerts"))
        context.deleteDatabase("oxygen_forecast_cache.db")
    }
}

private object InstalledFallbackDirectExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private object FailingOpenMeteoRepository : WeatherRepository {
    val locations = mutableListOf<WeatherLocation>()

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        locations += location
        return sequenceOf(WeatherRepositoryResult.Failure(ForecastError.ProviderUnavailable("open-meteo")))
    }
}

private object NetworkUnavailableOpenMeteoRepository : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> =
        sequenceOf(
            WeatherRepositoryResult.Loading,
            WeatherRepositoryResult.Failure(ForecastError.NetworkUnavailable),
        )
}

private object UnexpectedFallbackRepository : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        error("Fallback repository must not be called for this Slice 32 scenario.")
    }
}

private class SuccessWeatherRepository(
    private val bundle: WeatherBundle,
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> =
        sequenceOf(
            WeatherRepositoryResult.Loading,
            WeatherRepositoryResult.Success(bundle),
        )
}

private class RecordingMetNoTransport(
    private val response: MetNoHttpResponse,
) : MetNoHttpTransport {
    var request: MetNoHttpRequest? = null

    override fun get(request: MetNoHttpRequest): MetNoHttpResponse {
        this.request = request
        return response
    }
}

private class InMemoryForecastCacheStorage : ForecastCacheStorage {
    private val bundles = mutableMapOf<LocationId, WeatherBundle>()

    override fun replaceBundle(bundle: WeatherBundle) {
        bundles[bundle.location.id] = bundle
    }

    override fun readBundle(locationId: LocationId): WeatherBundle? = bundles[locationId]
}

private fun OxygenAppStateHolder.readyForecast(): HomeForecastPresentationState.ForecastReady =
    ((presentationState.screen as OxygenAppScreen.Home).forecast as HomeForecastPresentationState.ForecastReady)

private fun assertMetNorwayForecast(
    location: WeatherLocation,
    ready: HomeForecastPresentationState.ForecastReady,
) {
    assertEquals(location, ready.location)
    assertEquals("MET Norway", ready.dashboard.source.sourceName)
    assertEquals("NLOD-2.0 OR CC-BY-4.0", ready.dashboard.source.license)
    assertEquals("Model estimate", ready.dashboard.source.dataType)
    assertTrue(ready.dashboard.source.fetchedAt.contains("Aug 23"))
    assertTrue(ready.dashboard.source.issuedAt.orEmpty().contains("Aug 23"))
    assertTrue(ready.forecastDisclosure.contains("MET Norway"))
    assertTrue(ready.forecastPrivacyNote.contains("MET Norway"))
}

private fun assertOpenMeteoForecast(
    location: WeatherLocation,
    ready: HomeForecastPresentationState.ForecastReady,
) {
    assertEquals(location, ready.location)
    assertEquals("Open-Meteo", ready.dashboard.source.sourceName)
    assertEquals("CC BY 4.0", ready.dashboard.source.license)
    assertEquals("Model estimate", ready.dashboard.source.dataType)
    assertEquals(HomeForecastFreshness.Fresh, ready.freshness)
    assertTrue(ready.forecastDisclosure.contains("Open-Meteo"))
    assertTrue(ready.forecastPrivacyNote.contains("Open-Meteo"))
    assertFalse(ready.dashboard.visibleText().contains("SampleWeather"))
}

private fun weatherLocation(
    id: String,
    name: String,
): WeatherLocation =
    WeatherLocation(
        id = LocationId(id),
        displayName = name,
        point = GeoPoint(41.875, -87.625),
        elevationMeters = 181.6,
        zoneId = ZoneId.of("America/Chicago"),
    )

private fun openMeteoBundle(location: WeatherLocation): WeatherBundle {
    val provenance = DataProvenance(
        providerId = "open-meteo",
        sourceName = "Open-Meteo",
        issuedAt = Instant.parse("2026-08-23T13:45:00Z"),
        fetchedAt = Instant.parse("2026-08-23T14:00:00Z"),
        type = DataType.MODEL_ESTIMATE,
        licenseId = "CC BY 4.0",
    )
    return WeatherBundle(
        location = location,
        current = CurrentConditions(
            time = Instant.parse("2026-08-23T14:00:00Z"),
            temperatureC = 25.0,
            apparentTemperatureC = 26.0,
            dewPointC = 14.0,
            humidityPercent = 54,
            pressureHpa = 1014.0,
            visibilityMeters = 12000.0,
            cloudCoverPercent = 20,
            wind = Wind(speedMetersPerSecond = 3.0, gustMetersPerSecond = 5.0, directionDegrees = 210.0),
            precipitationMm = 0.0,
            condition = WeatherCondition.MOSTLY_CLEAR,
            provenance = provenance,
        ),
        hourly = listOf(
            HourlyForecast(
                time = Instant.parse("2026-08-23T15:00:00Z"),
                temperatureC = 26.0,
                precipitationProbabilityPercent = 5,
                precipitationMm = 0.0,
                condition = WeatherCondition.CLEAR,
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
        ),
        daily = listOf(
            DailyForecast(
                dateEpochDay = LocalDate.parse("2026-08-23").toEpochDay(),
                highC = 27.0,
                lowC = 18.0,
                precipitationProbabilityPercent = 10,
                condition = WeatherCondition.MOSTLY_CLEAR,
                sunrise = Instant.parse("2026-08-23T11:05:00Z"),
                sunset = Instant.parse("2026-08-24T00:42:00Z"),
                provenance = provenance.copy(type = DataType.FORECAST),
            ),
        ),
        fetchedAt = Instant.parse("2026-08-23T14:00:00Z"),
    )
}

private fun HomeSuccessPresentation.visibleText(): String =
    buildString {
        append(locationName).append('\n')
        alerts.forEach { append("${it.event} ${it.headline}\n") }
        current?.let { append("${it.temperature} ${it.condition} ${it.dataTypeLabel}\n") }
        hourly.forEach { append("${it.time} ${it.temperature}\n") }
        daily.forEach { append("${it.date} ${it.high} ${it.low}\n") }
        append("${source.sourceName} ${source.dataType} ${source.license}\n")
    }

private val metNorwayForecastBody = """
{
  "type": "Feature",
  "geometry": {
    "type": "Point",
    "coordinates": [-87.625, 41.875, 181.0]
  },
  "properties": {
    "meta": {
      "updated_at": "2026-08-23T10:15:00Z",
      "units": {
        "air_temperature": "celsius",
        "air_temperature_min": "celsius",
        "air_temperature_max": "celsius",
        "air_pressure_at_sea_level": "hPa",
        "cloud_area_fraction": "%",
        "relative_humidity": "%",
        "wind_from_direction": "degrees",
        "wind_speed": "m/s",
        "precipitation_amount": "mm",
        "probability_of_precipitation": "%"
      }
    },
    "timeseries": [
      {
        "time": "2026-08-23T11:00:00Z",
        "data": {
          "instant": {
            "details": {
              "air_temperature": 22.4,
              "air_pressure_at_sea_level": 1012.3,
              "cloud_area_fraction": 37.5,
              "relative_humidity": 68.0,
              "wind_from_direction": 220.0,
              "wind_speed": 4.1
            }
          },
          "next_1_hours": {
            "summary": { "symbol_code": "partlycloudy_day" },
            "details": {
              "precipitation_amount": 0.2,
              "probability_of_precipitation": 35.0
            }
          },
          "next_6_hours": {
            "summary": { "symbol_code": "rain" },
            "details": {
              "precipitation_amount": 2.4,
              "probability_of_precipitation": 72.0,
              "air_temperature_min": 19.1,
              "air_temperature_max": 24.3
            }
          }
        }
      },
      {
        "time": "2026-08-23T12:00:00Z",
        "data": {
          "instant": {
            "details": {
              "air_temperature": 23.0,
              "air_pressure_at_sea_level": 1011.8,
              "cloud_area_fraction": 45.0,
              "relative_humidity": 64.0,
              "wind_from_direction": 225.0,
              "wind_speed": 4.5
            }
          },
          "next_1_hours": {
            "summary": { "symbol_code": "rainshowers_day" },
            "details": {
              "precipitation_amount": 0.5,
              "probability_of_precipitation": 48.0
            }
          }
        }
      }
    ]
  }
}
""".trimIndent()
