package com.oxygen.weather.core.model

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class UnitPreferenceTest {
    @Test
    fun presetsResolveEveryUnitCategory() {
        assertEquals(
            ResolvedUnitPreference(
                temperature = TemperatureUnit.CELSIUS,
                windSpeed = WindSpeedUnit.KILOMETERS_PER_HOUR,
                pressure = PressureUnit.HECTOPASCALS,
                precipitation = PrecipitationUnit.MILLIMETERS,
                visibility = VisibilityUnit.KILOMETERS,
            ),
            UnitPreference.Preset(UnitPreferencePreset.METRIC).resolve(),
        )

        assertEquals(
            ResolvedUnitPreference(
                temperature = TemperatureUnit.FAHRENHEIT,
                windSpeed = WindSpeedUnit.MILES_PER_HOUR,
                pressure = PressureUnit.INCHES_OF_MERCURY,
                precipitation = PrecipitationUnit.INCHES,
                visibility = VisibilityUnit.MILES,
            ),
            UnitPreference.Preset(UnitPreferencePreset.US).resolve(),
        )

        assertEquals(
            ResolvedUnitPreference(
                temperature = TemperatureUnit.CELSIUS,
                windSpeed = WindSpeedUnit.MILES_PER_HOUR,
                pressure = PressureUnit.HECTOPASCALS,
                precipitation = PrecipitationUnit.MILLIMETERS,
                visibility = VisibilityUnit.MILES,
            ),
            UnitPreference.Preset(UnitPreferencePreset.UK).resolve(),
        )
    }

    @Test
    fun customPreservesEveryExplicitChoice() {
        val custom = UnitPreference.Custom(
            temperature = TemperatureUnit.FAHRENHEIT,
            windSpeed = WindSpeedUnit.KNOTS,
            pressure = PressureUnit.MILLIMETERS_OF_MERCURY,
            precipitation = PrecipitationUnit.INCHES,
            visibility = VisibilityUnit.MILES,
        )

        assertEquals(
            ResolvedUnitPreference(
                temperature = TemperatureUnit.FAHRENHEIT,
                windSpeed = WindSpeedUnit.KNOTS,
                pressure = PressureUnit.MILLIMETERS_OF_MERCURY,
                precipitation = PrecipitationUnit.INCHES,
                visibility = VisibilityUnit.MILES,
            ),
            custom.resolve(),
        )
    }

    @Test
    fun resolvingUnitPreferencesDoesNotMutateCanonicalWeatherBundleValues() {
        val bundle = representativeWeatherBundle()
        val preferences = listOf(
            UnitPreference.Preset(UnitPreferencePreset.METRIC),
            UnitPreference.Preset(UnitPreferencePreset.US),
            UnitPreference.Preset(UnitPreferencePreset.UK),
            UnitPreference.Custom(
                temperature = TemperatureUnit.FAHRENHEIT,
                windSpeed = WindSpeedUnit.METERS_PER_SECOND,
                pressure = PressureUnit.INCHES_OF_MERCURY,
                precipitation = PrecipitationUnit.INCHES,
                visibility = VisibilityUnit.MILES,
            ),
        )

        preferences.forEach { preference ->
            preference.resolve()

            val current = requireNotNull(bundle.current)
            assertEquals(21.5, requireNotNull(current.temperatureC), 0.0)
            assertEquals(5.4, requireNotNull(requireNotNull(current.wind).speedMetersPerSecond), 0.0)
            assertEquals(1008.7, requireNotNull(current.pressureHpa), 0.0)
            assertEquals(1.2, requireNotNull(current.precipitationMm), 0.0)
            assertEquals(12000.0, requireNotNull(current.visibilityMeters), 0.0)
            assertEquals(22.0, requireNotNull(bundle.hourly[0].temperatureC), 0.0)
            assertEquals(0.3, requireNotNull(bundle.hourly[0].precipitationMm), 0.0)
            assertEquals(26.0, requireNotNull(bundle.daily[0].highC), 0.0)
            assertEquals(14.0, requireNotNull(bundle.daily[0].lowC), 0.0)
        }
    }

    @Test
    fun weatherBundleDoesNotCarryParallelDisplayUnitFields() {
        val propertyNames = WeatherBundle::class.java.declaredFields.map { it.name }.toSet()

        assertFalse("WeatherBundle must not store display temperature fields", "temperatureUnit" in propertyNames)
        assertFalse("WeatherBundle must not store display wind fields", "windSpeedUnit" in propertyNames)
        assertFalse("WeatherBundle must not store display pressure fields", "pressureUnit" in propertyNames)
        assertFalse("WeatherBundle must not store display precipitation fields", "precipitationUnit" in propertyNames)
        assertFalse("WeatherBundle must not store display visibility fields", "visibilityUnit" in propertyNames)
    }

    private fun representativeWeatherBundle(): WeatherBundle {
        val fetchedAt = Instant.parse("2026-09-04T12:00:00Z")
        val provenance = DataProvenance(
            providerId = "test-provider",
            sourceName = "Test Provider",
            fetchedAt = fetchedAt,
            type = DataType.FORECAST,
        )
        val location = WeatherLocation(
            id = LocationId("manual-test"),
            displayName = "Test City",
            point = GeoPoint(latitude = 41.88, longitude = -87.63),
            zoneId = ZoneId.of("America/Chicago"),
        )

        return WeatherBundle(
            location = location,
            current = CurrentConditions(
                time = fetchedAt,
                temperatureC = 21.5,
                pressureHpa = 1008.7,
                visibilityMeters = 12000.0,
                wind = Wind(speedMetersPerSecond = 5.4),
                precipitationMm = 1.2,
                condition = WeatherCondition.RAIN,
                provenance = provenance,
            ),
            hourly = listOf(
                HourlyForecast(
                    time = fetchedAt.plusSeconds(3600),
                    temperatureC = 22.0,
                    precipitationMm = 0.3,
                    condition = WeatherCondition.RAIN_SHOWERS,
                    provenance = provenance,
                ),
            ),
            daily = listOf(
                DailyForecast(
                    dateEpochDay = 20670,
                    highC = 26.0,
                    lowC = 14.0,
                    condition = WeatherCondition.PARTLY_CLOUDY,
                    provenance = provenance,
                ),
            ),
            fetchedAt = fetchedAt,
        )
    }
}
