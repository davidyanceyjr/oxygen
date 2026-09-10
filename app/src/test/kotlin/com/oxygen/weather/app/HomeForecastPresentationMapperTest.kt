package com.oxygen.weather.app

import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.AlertSeverity
import com.oxygen.weather.core.model.DailyForecast
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.HourlyForecast
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.PrecipitationUnit
import com.oxygen.weather.core.model.PressureUnit
import com.oxygen.weather.core.model.TemperatureUnit
import com.oxygen.weather.core.model.UnitPreference
import com.oxygen.weather.core.model.UnitPreferencePreset
import com.oxygen.weather.core.model.VisibilityUnit
import com.oxygen.weather.core.model.WindSpeedUnit
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherAlert
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.model.Wind
import com.oxygen.weather.core.provider.AlertLookupStatus
import com.oxygen.weather.core.provider.AlertSuccessMetadata
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeForecastPresentationMapperTest {
    @Test
    fun `home presentation normalizes only legacy MET Norway license provenance`() {
        fun presentationSource(providerId: String, licenseId: String): String? =
            fullWeatherBundle().copy(
                current = requireNotNull(fullWeatherBundle().current).copy(
                    provenance = DataProvenance(
                        providerId = providerId,
                        sourceName = "Source",
                        issuedAt = Instant.parse("2026-08-22T11:45:00Z"),
                        fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
                        type = DataType.MODEL_ESTIMATE,
                        licenseId = licenseId,
                    ),
                ),
            ).toHomeSuccessPresentation(testLocation).source.license

        assertEquals("NLOD-2.0 AND CC-BY-4.0", presentationSource("met-norway", "NLOD-2.0 OR CC-BY-4.0"))
        assertEquals("NLOD-2.0 AND CC-BY-4.0", presentationSource("met-norway", "NLOD-2.0 AND CC-BY-4.0"))
        assertEquals("NLOD-2.0 OR CC-BY-4.0", presentationSource("other-provider", "NLOD-2.0 OR CC-BY-4.0"))
        assertEquals("Other license", presentationSource("met-norway", "Other license"))
    }

    @Test
    fun availableAlertSummaryUsesSelectedZoneMetadataCountAndSafeSourceLink() {
        val checkedAt = Instant.parse("2026-08-22T15:05:00Z")
        val alerts = listOf(
            mapperAlert().copy(
                event = "Flash Flood Warning",
                severity = AlertSeverity.SEVERE,
                expires = Instant.parse("2026-08-22T18:00:00Z"),
                web = " https://alerts.weather.gov/example ",
            ),
            mapperAlert(id = "alert-2", event = "Heat Advisory"),
            mapperAlert(id = "alert-3", event = "Wind Advisory"),
        )

        val presentation = fullWeatherBundle().copy(alerts = alerts).toHomeSuccessPresentation(
            selectedLocation = testLocation,
            alertStatus = AlertLookupStatus.Available(
                AlertSuccessMetadata(
                    requestPoint = testLocation.point,
                    providerId = "nws",
                    fetchedAt = checkedAt,
                ),
            ),
        )

        assertEquals(alerts, presentation.alerts)
        assertEquals("Flash Flood Warning", presentation.alertSummary?.event)
        assertEquals("Severe", presentation.alertSummary?.severity)
        assertEquals("Expires 1:00 PM", presentation.alertSummary?.expires)
        assertEquals(3, presentation.alertSummary?.activeAlertCount)
        assertEquals("Alert source checked Aug 22, 10:05 AM CDT", presentation.alertSummary?.sourceCheckedAt)
        assertEquals("https://alerts.weather.gov/example", presentation.alertSummary?.sourceLink)
        assertEquals(HomeSuccessSection.Current, presentation.sectionOrder[1])
        assertEquals(HomeSuccessSection.Alerts, presentation.sectionOrder[2])
    }

    @Test
    fun nonAvailableAlertStatusPreservesCompleteAlertsWithoutSummary() {
        val weather = fullWeatherBundle()
        val presentation = weather.toHomeSuccessPresentation(
            selectedLocation = testLocation,
            alertStatus = AlertLookupStatus.Failed(com.oxygen.weather.core.provider.AlertProviderError.Network),
        )

        assertEquals(weather.alerts, presentation.alerts)
        assertNull(presentation.alertSummary)
        assertEquals(HomeSuccessSection.Current, presentation.sectionOrder[1])
    }

    @Test
    fun invalidAlertSourceUrlUsesOfficialWeatherFallback() {
        listOf(null, "", "alerts.weather.gov", "http://alerts.weather.gov", "https:///missing-host", "mailto:nws@example.com")
            .forEach { url ->
                val weather = fullWeatherBundle().copy(
                    alerts = listOf(mapperAlert(web = url)),
                )
                val presentation = weather.toHomeSuccessPresentation(
                    selectedLocation = testLocation,
                    alertStatus = AlertLookupStatus.Available(
                        AlertSuccessMetadata(
                            requestPoint = testLocation.point,
                            providerId = "nws",
                            fetchedAt = Instant.parse("2026-08-22T15:05:00Z"),
                        ),
                    ),
                )
                assertEquals("https://www.weather.gov/", presentation.alertSummary?.sourceLink)
            }
    }

    @Test
    fun availableAlertsMapCompleteDetailsInSelectedZoneAndKeepSourceCheckSeparate() {
        val checkedAt = Instant.parse("2026-08-22T15:05:00Z")
        val alert = mapperAlert(web = "https://alerts.weather.gov/one").copy(
            headline = "Flooding is possible",
            urgency = com.oxygen.weather.core.model.AlertUrgency.IMMEDIATE,
            certainty = com.oxygen.weather.core.model.AlertCertainty.LIKELY,
            effective = Instant.parse("2026-08-22T12:00:00Z"),
            sent = Instant.parse("2026-08-22T11:30:00Z"),
            onset = Instant.parse("2026-08-22T13:00:00Z"),
            ends = Instant.parse("2026-08-22T20:00:00Z"),
            affectedArea = com.oxygen.weather.core.model.AlertAffectedArea(areaDescription = "Dane County"),
            description = "Line one\nLine two",
            instruction = "Move to higher ground.\nDo not drive.",
        )

        val detail = fullWeatherBundle().copy(alerts = listOf(alert)).toHomeSuccessPresentation(
            selectedLocation = testLocation,
            alertStatus = AlertLookupStatus.Available(
                AlertSuccessMetadata(testLocation.point, "nws", checkedAt),
            ),
        ).alertDetails.single()

        assertEquals("alert-1", detail.id)
        assertEquals("Flood Watch", detail.event)
        assertEquals("Flooding is possible", detail.headline)
        assertEquals("Moderate", detail.severity)
        assertEquals("Immediate", detail.urgency)
        assertEquals("Likely", detail.certainty)
        assertEquals("Aug 22, 7:00 AM CDT", detail.effective)
        assertEquals("Aug 22, 1:00 PM CDT", detail.expires)
        assertEquals("Aug 22, 6:30 AM CDT", detail.sent)
        assertEquals("Aug 22, 8:00 AM CDT", detail.onset)
        assertEquals("Aug 22, 3:00 PM CDT", detail.ends)
        assertEquals("Dane County", detail.affectedArea)
        assertEquals("Line one\nLine two", detail.description)
        assertEquals("Move to higher ground.\nDo not drive.", detail.instruction)
        assertEquals("Alert source checked Aug 22, 10:05 AM CDT", detail.sourceCheckedAt)
        assertEquals("https://alerts.weather.gov/one", detail.sourceLink)
        assertTrue(detail.sourceLinkLabel.contains("Flood Watch"))
    }

    @Test
    fun nonAvailableStatusesHaveNoAlertDetailsAndMissingValuesStayUnavailable() {
        val statuses = listOf(
            AlertLookupStatus.NotRequested,
            AlertLookupStatus.NoAlerts(AlertSuccessMetadata(testLocation.point, "nws", Instant.parse("2026-08-22T15:05:00Z"))),
            AlertLookupStatus.UnsupportedRegion,
            AlertLookupStatus.Failed(com.oxygen.weather.core.provider.AlertProviderError.Network),
            AlertLookupStatus.SkippedByRateLimit("nws", testLocation.point, Instant.parse("2026-08-22T15:06:00Z")),
        )
        statuses.forEach { status ->
            assertTrue(fullWeatherBundle().toHomeSuccessPresentation(testLocation, alertStatus = status).alertDetails.isEmpty())
        }

        val missing = mapperAlert().copy(
            effective = null,
            expires = null,
            affectedArea = null,
            description = null,
            instruction = null,
        )
        val detail = fullWeatherBundle().copy(alerts = listOf(missing)).toHomeSuccessPresentation(
            testLocation,
            alertStatus = AlertLookupStatus.Available(AlertSuccessMetadata(testLocation.point, "nws", Instant.parse("2026-08-22T15:05:00Z"))),
        ).alertDetails.single()
        assertEquals("Unavailable", detail.effective)
        assertEquals("Unavailable", detail.expires)
        assertEquals("Unavailable", detail.affectedArea)
        assertEquals("Unavailable", detail.description)
        assertEquals("Unavailable", detail.instruction)
    }

    @Test(expected = IllegalArgumentException::class)
    fun availableDuplicateAlertIdsAreRejected() {
        val alert = mapperAlert()
        fullWeatherBundle().copy(alerts = listOf(alert, alert)).toHomeSuccessPresentation(
            testLocation,
            alertStatus = AlertLookupStatus.Available(AlertSuccessMetadata(testLocation.point, "nws", Instant.parse("2026-08-22T15:05:00Z"))),
        )
    }
    @Test
    fun `mapper keeps canonical values separate from formatted Home text`() {
        val presentation = fullWeatherBundle().toHomeSuccessPresentation(testLocation)

        val current = requireNotNull(presentation.current)
        assertEquals("65 deg F", current.temperature)
        assertEquals(18.4, current.temperatureC)
        assertEquals("Feels like 63 deg F", current.apparentTemperature)
        assertEquals(17.2, current.apparentTemperatureC)
        assertEquals("H 73 deg F", current.highTemperature)
        assertEquals(22.7, current.highTemperatureC)
        assertEquals("L 54 deg F", current.lowTemperature)
        assertEquals(12.3, current.lowTemperatureC)
        assertEquals(WeatherCondition.RAIN_SHOWERS, current.conditionIdentity)

        assertEquals("64 deg F", presentation.hourly.single().temperature)
        assertEquals(18.0, presentation.hourly.single().temperatureC)
        assertEquals("60%", presentation.hourly.single().precipitationProbability)
        assertEquals(60, presentation.hourly.single().precipitationProbabilityPercent)
        assertEquals(WeatherCondition.RAIN, presentation.hourly.single().conditionIdentity)

        assertEquals("High 73 deg F", presentation.daily.single().high)
        assertEquals(22.7, presentation.daily.single().highC)
        assertEquals("40%", presentation.daily.single().precipitationProbability)
        assertEquals(40, presentation.daily.single().precipitationProbabilityPercent)
        assertEquals(WeatherCondition.RAIN_SHOWERS, presentation.daily.single().conditionIdentity)

        assertEquals(
            HomeMetricNumericValues.Wind(
                speedMetersPerSecond = 4.0,
                gustMetersPerSecond = 7.0,
                directionDegrees = 225.0,
            ),
            presentation.metrics.single { it.identity == HomeMetricIdentity.Wind }.numericValues,
        )
        assertEquals(
            HomeMetricNumericValues.DistanceMeters(9500.0),
            presentation.metrics.single { it.identity == HomeMetricIdentity.Visibility }.numericValues,
        )
        assertEquals(
            HomeMetricNumericValues.PressureHpa(1012.4),
            presentation.metrics.single { it.identity == HomeMetricIdentity.Pressure }.numericValues,
        )
    }

    @Test
    fun spokenDescriptionsUseResolvedTemperatureUnits() {
        val fahrenheit = fullWeatherBundle().toHomeSuccessPresentation(testLocation)
        val celsius = fullWeatherBundle().toHomeSuccessPresentation(
            testLocation,
            UnitPreference.Preset(UnitPreferencePreset.METRIC),
        )

        assertEquals(
            "Rain showers. 65 degrees Fahrenheit. Feels like 63 degrees Fahrenheit. High 73 degrees Fahrenheit. Low 54 degrees Fahrenheit.",
            requireNotNull(fahrenheit.current).spokenDescription,
        )
        assertEquals(
            "6 AM. Rain. 64 degrees Fahrenheit. 60 percent chance of precipitation.",
            fahrenheit.hourly.single().spokenDescription,
        )
        assertEquals(
            "Sat, Aug 22. Rain showers. High 73 degrees Fahrenheit. Low 54 degrees Fahrenheit. 40 percent chance of precipitation.",
            fahrenheit.daily.single().spokenDescription,
        )

        assertEquals(
            "Rain showers. 18 degrees Celsius. Feels like 17 degrees Celsius. High 23 degrees Celsius. Low 12 degrees Celsius.",
            requireNotNull(celsius.current).spokenDescription,
        )
        assertEquals(
            "6 AM. Rain. 18 degrees Celsius. 60 percent chance of precipitation.",
            celsius.hourly.single().spokenDescription,
        )
        assertEquals(
            "Sat, Aug 22. Rain showers. High 23 degrees Celsius. Low 12 degrees Celsius. 40 percent chance of precipitation.",
            celsius.daily.single().spokenDescription,
        )
    }

    @Test
    fun spokenDescriptionsHandleMissingValuesWithoutInventingZero() {
        val weather = fullWeatherBundle().copy(
            current = requireNotNull(fullWeatherBundle().current).copy(
                temperatureC = null,
                apparentTemperatureC = null,
            ),
            hourly = listOf(
                fullWeatherBundle().hourly.single().copy(
                    temperatureC = null,
                    precipitationProbabilityPercent = null,
                ),
            ),
            daily = listOf(
                fullWeatherBundle().daily.single().copy(
                    highC = 22.7,
                    lowC = null,
                    precipitationProbabilityPercent = null,
                ),
                fullWeatherBundle().daily.single().copy(
                    dateEpochDay = LocalDate.parse("2026-08-23").toEpochDay(),
                    highC = null,
                    lowC = null,
                    precipitationProbabilityPercent = null,
                ),
            ),
        )

        val presentation = weather.toHomeSuccessPresentation(testLocation)
        val current = requireNotNull(presentation.current)
        assertEquals(
            "Rain showers. Temperature unavailable. High 73 degrees Fahrenheit.",
            current.spokenDescription,
        )
        assertNull(current.temperatureC)
        assertEquals(WeatherCondition.RAIN_SHOWERS, current.conditionIdentity)
        assertEquals(
            "6 AM. Rain. Temperature unavailable.",
            presentation.hourly.single().spokenDescription,
        )
        assertNull(presentation.hourly.single().temperatureC)
        assertEquals(WeatherCondition.RAIN, presentation.hourly.single().conditionIdentity)
        assertEquals(
            "Sat, Aug 22. Rain showers. High 73 degrees Fahrenheit.",
            presentation.daily[0].spokenDescription,
        )
        assertEquals(
            "Sun, Aug 23. Rain showers. High and low unavailable.",
            presentation.daily[1].spokenDescription,
        )
        assertNull(presentation.daily[0].lowC)
        assertNull(presentation.daily[1].highC)
        assertNull(presentation.daily[1].lowC)
        assertNull(presentation.daily[0].precipitationProbabilityPercent)
        assertEquals(WeatherCondition.RAIN_SHOWERS, presentation.daily[1].conditionIdentity)
    }

    @Test
    fun `mapper retains missing semantic values as null instead of parsing unavailable text`() {
        val weather = fullWeatherBundle().copy(
            current = fullWeatherBundle().current?.copy(
                temperatureC = null,
                apparentTemperatureC = null,
                dewPointC = null,
                humidityPercent = null,
                pressureHpa = null,
                visibilityMeters = null,
                cloudCoverPercent = null,
                wind = null,
                precipitationMm = null,
            ),
            hourly = listOf(fullWeatherBundle().hourly.single().copy(
                temperatureC = null,
                precipitationProbabilityPercent = null,
            )),
            daily = listOf(fullWeatherBundle().daily.single().copy(
                highC = null,
                lowC = null,
                precipitationProbabilityPercent = null,
            )),
        )

        val presentation = weather.toHomeSuccessPresentation(testLocation)
        val current = requireNotNull(presentation.current)
        assertEquals("Unavailable", current.temperature)
        assertNull(current.temperatureC)
        assertEquals("Feels like unavailable", current.apparentTemperature)
        assertNull(current.apparentTemperatureC)
        assertNull(current.highTemperatureC)
        assertNull(current.lowTemperatureC)
        assertEquals(WeatherCondition.RAIN_SHOWERS, current.conditionIdentity)

        assertEquals("Unavailable", presentation.hourly.single().temperature)
        assertNull(presentation.hourly.single().temperatureC)
        assertNull(presentation.hourly.single().precipitationProbability)
        assertNull(presentation.hourly.single().precipitationProbabilityPercent)
        assertEquals(WeatherCondition.RAIN, presentation.hourly.single().conditionIdentity)

        assertEquals("High unavailable", presentation.daily.single().high)
        assertNull(presentation.daily.single().highC)
        assertNull(presentation.daily.single().lowC)
        assertNull(presentation.daily.single().precipitationProbabilityPercent)
        assertEquals(WeatherCondition.RAIN_SHOWERS, presentation.daily.single().conditionIdentity)

        assertEquals(
            HomeMetricNumericValues.TemperatureC(null),
            presentation.metrics.single { it.identity == HomeMetricIdentity.ApparentTemperature }.numericValues,
        )
    }

    @Test
    fun `metric preference converts every displayed weather category`() {
        val presentation = fullWeatherBundle().toHomeSuccessPresentation(
            testLocation,
            UnitPreference.Preset(UnitPreferencePreset.METRIC),
        )

        assertEquals("18 deg C", requireNotNull(presentation.current).temperature)
        assertEquals("Feels like 17 deg C", requireNotNull(presentation.current).apparentTemperature)
        assertEquals("H 23 deg C", requireNotNull(presentation.current).highTemperature)
        assertEquals("L 12 deg C", requireNotNull(presentation.current).lowTemperature)
        assertEquals("18 deg C", presentation.hourly.single().temperature)
        assertEquals("High 23 deg C", presentation.daily.single().high)
        assertEquals("Low 12 deg C", presentation.daily.single().low)
        assertEquals("14 km/h, gust 25 km/h, 225 deg", metric(presentation, HomeMetricIdentity.Wind).value)
        assertEquals("1012 hPa", metric(presentation, HomeMetricIdentity.Pressure).value)
        assertEquals("9.5 km", metric(presentation, HomeMetricIdentity.Visibility).value)
        assertEquals("12 deg C", metric(presentation, HomeMetricIdentity.DewPoint).value)
        assertEquals("0.4 mm", metric(presentation, HomeMetricIdentity.Precipitation).value)
    }

    @Test
    fun `US and UK presets preserve their distinct unit families`() {
        val us = fullWeatherBundle().toHomeSuccessPresentation(
            testLocation,
            UnitPreference.Preset(UnitPreferencePreset.US),
        )
        val uk = fullWeatherBundle().toHomeSuccessPresentation(
            testLocation,
            UnitPreference.Preset(UnitPreferencePreset.UK),
        )

        assertEquals("65 deg F", requireNotNull(us.current).temperature)
        assertEquals("9 mph, gust 16 mph, 225 deg", metric(us, HomeMetricIdentity.Wind).value)
        assertEquals("29.90 inHg", metric(us, HomeMetricIdentity.Pressure).value)
        assertEquals("5.9 mi", metric(us, HomeMetricIdentity.Visibility).value)
        assertEquals("0.02 in", metric(us, HomeMetricIdentity.Precipitation).value)
        assertEquals("18 deg C", requireNotNull(uk.current).temperature)
        assertEquals("9 mph, gust 16 mph, 225 deg", metric(uk, HomeMetricIdentity.Wind).value)
        assertEquals("1012 hPa", metric(uk, HomeMetricIdentity.Pressure).value)
        assertEquals("5.9 mi", metric(uk, HomeMetricIdentity.Visibility).value)
        assertEquals("0.4 mm", metric(uk, HomeMetricIdentity.Precipitation).value)
    }

    @Test
    fun `custom preference supports knots mmHg inches miles meters per second and inHg`() {
        val custom = UnitPreference.Custom(
            temperature = TemperatureUnit.CELSIUS,
            windSpeed = WindSpeedUnit.KNOTS,
            pressure = PressureUnit.MILLIMETERS_OF_MERCURY,
            precipitation = PrecipitationUnit.INCHES,
            visibility = VisibilityUnit.MILES,
        )
        val presentation = fullWeatherBundle().toHomeSuccessPresentation(testLocation, custom)

        assertEquals("8 kn, gust 14 kn, 225 deg", metric(presentation, HomeMetricIdentity.Wind).value)
        assertEquals("759 mmHg", metric(presentation, HomeMetricIdentity.Pressure).value)
        assertEquals("0.02 in", metric(presentation, HomeMetricIdentity.Precipitation).value)
        assertEquals("5.9 mi", metric(presentation, HomeMetricIdentity.Visibility).value)

        val metricWindAndInHg = fullWeatherBundle().toHomeSuccessPresentation(
            testLocation,
            custom.copy(windSpeed = WindSpeedUnit.METERS_PER_SECOND, pressure = PressureUnit.INCHES_OF_MERCURY),
        )
        assertEquals("4 m/s, gust 7 m/s, 225 deg", metric(metricWindAndInHg, HomeMetricIdentity.Wind).value)
        assertEquals("29.90 inHg", metric(metricWindAndInHg, HomeMetricIdentity.Pressure).value)
    }

    @Test
    fun `default compatibility keeps current strings including sub-kilometer visibility`() {
        val weather = fullWeatherBundle().copy(
            current = requireNotNull(fullWeatherBundle().current).copy(visibilityMeters = 999.5),
        )
        val presentation = weather.toHomeSuccessPresentation(testLocation)

        assertEquals("1000 m", metric(presentation, HomeMetricIdentity.Visibility).value)
        assertEquals("0.4 mm", metric(presentation, HomeMetricIdentity.Precipitation).value)
        assertEquals("14 km/h, gust 25 km/h, 225 deg", metric(presentation, HomeMetricIdentity.Wind).value)
    }

    @Test
    fun `conversion rounds half up and aggregates canonical precipitation before conversion`() {
        val weather = fullWeatherBundle().copy(
            current = requireNotNull(fullWeatherBundle().current).copy(
                temperatureC = -17.5,
                wind = Wind(0.5 / 3.6, 1.5 / 3.6, 12.5),
                pressureHpa = 1012.5,
                visibilityMeters = 500.0,
                precipitationMm = 0.05,
            ),
            hourly = listOf(
                fullWeatherBundle().hourly.single().copy(precipitationMm = 0.025),
                fullWeatherBundle().hourly.single().copy(precipitationMm = 0.025),
            ),
        )
        val presentation = weather.toHomeSuccessPresentation(
            testLocation,
            UnitPreference.Custom(
                temperature = TemperatureUnit.CELSIUS,
                windSpeed = WindSpeedUnit.KILOMETERS_PER_HOUR,
                pressure = PressureUnit.HECTOPASCALS,
                precipitation = PrecipitationUnit.MILLIMETERS,
                visibility = VisibilityUnit.KILOMETERS,
            ),
        )

        assertEquals("-18 deg C", requireNotNull(presentation.current).temperature)
        assertEquals("1 km/h, gust 2 km/h, 13 deg", metric(presentation, HomeMetricIdentity.Wind).value)
        assertEquals("1013 hPa", metric(presentation, HomeMetricIdentity.Pressure).value)
        assertEquals("0.1 mm", presentation.precipitationSummary?.substringAfter("; ")?.substringBefore(" possible"))
    }

    @Test
    fun `direction remains visible when speed and gust are absent`() {
        val weather = fullWeatherBundle().copy(
            current = requireNotNull(fullWeatherBundle().current).copy(wind = Wind(null, null, 270.5)),
        )
        val wind = metric(weather.toHomeSuccessPresentation(testLocation), HomeMetricIdentity.Wind)

        assertEquals("271 deg", wind.value)
        assertEquals(
            HomeMetricNumericValues.Wind(null, null, 270.5),
            wind.numericValues,
        )
    }

    @Test
    fun `unit conversion does not mutate canonical bundle or source presentation`() {
        val weather = fullWeatherBundle()
        val presentation = weather.toHomeSuccessPresentation(
            testLocation,
            UnitPreference.Preset(UnitPreferencePreset.US),
        )

        assertEquals(weather, fullWeatherBundle())
        assertEquals("Mapper Provider", presentation.source.sourceName)
        assertEquals("Model estimate", presentation.source.dataType)
        assertEquals(HomeMetricIdentity.Wind, metric(presentation, HomeMetricIdentity.Wind).identity)
        assertEquals(18.4, requireNotNull(presentation.current).temperatureC)
        assertEquals(22.7, requireNotNull(presentation.current).highTemperatureC)
    }

    private fun metric(
        presentation: HomeSuccessPresentation,
        identity: HomeMetricIdentity,
    ): HomeMetricPresentation = presentation.metrics.single { it.identity == identity }

    private fun fullWeatherBundle(): WeatherBundle =
        WeatherBundle(
            location = testLocation,
            current = CurrentConditions(
                time = Instant.parse("2026-08-22T10:30:00Z"),
                temperatureC = 18.4,
                apparentTemperatureC = 17.2,
                dewPointC = 11.6,
                humidityPercent = 72,
                pressureHpa = 1012.4,
                visibilityMeters = 9500.0,
                cloudCoverPercent = 88,
                wind = Wind(4.0, 7.0, 225.0),
                precipitationMm = 0.4,
                condition = WeatherCondition.RAIN_SHOWERS,
                provenance = provenance,
            ),
            hourly = listOf(
                HourlyForecast(
                    time = Instant.parse("2026-08-22T11:00:00Z"),
                    temperatureC = 18.0,
                    precipitationProbabilityPercent = 60,
                    condition = WeatherCondition.RAIN,
                    provenance = provenance.copy(type = DataType.FORECAST),
                ),
            ),
            daily = listOf(
                DailyForecast(
                    dateEpochDay = java.time.LocalDate.parse("2026-08-22").toEpochDay(),
                    highC = 22.7,
                    lowC = 12.3,
                    precipitationProbabilityPercent = 40,
                    condition = WeatherCondition.RAIN_SHOWERS,
                    provenance = provenance.copy(type = DataType.FORECAST),
                ),
            ),
            fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
        )

    private companion object {
        val testLocation = WeatherLocation(
            id = LocationId("mapper-test"),
            displayName = "Mapper City",
            point = GeoPoint(43.0731, -89.4012),
            zoneId = ZoneId.of("America/Chicago"),
        )
        val provenance = DataProvenance(
            providerId = "mapper-provider",
            sourceName = "Mapper Provider",
            fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
            type = DataType.MODEL_ESTIMATE,
        )
    }
}

private fun mapperAlert(
    id: String = "alert-1",
    event: String = "Flood Watch",
    web: String? = null,
): WeatherAlert = WeatherAlert(
    id = id,
    event = event,
    severity = AlertSeverity.MODERATE,
    expires = Instant.parse("2026-08-22T18:00:00Z"),
    issuer = "Test Weather Office",
    web = web,
    provenance = DataProvenance(
        providerId = "nws",
        sourceName = "NOAA/National Weather Service",
        fetchedAt = Instant.parse("2026-08-22T15:00:00Z"),
        type = DataType.OFFICIAL_ALERT,
    ),
)
