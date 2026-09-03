package com.oxygen.weather.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import com.oxygen.weather.core.provider.cache.ForecastCacheStorage
import com.oxygen.weather.core.provider.metno.MetNoForecastClient
import com.oxygen.weather.core.provider.metno.MetNoHttpRequest
import com.oxygen.weather.core.provider.metno.MetNoHttpResponse
import com.oxygen.weather.core.provider.metno.MetNoHttpTransport
import com.oxygen.weather.core.provider.metno.MetNoWeatherRepository
import java.time.ZoneId
import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

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
