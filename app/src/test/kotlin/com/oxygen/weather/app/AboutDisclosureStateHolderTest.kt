package com.oxygen.weather.app

import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.GeocodingLocationCandidate
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.GeocodingRepository
import com.oxygen.weather.core.provider.GeocodingRepositoryResult
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AboutDisclosureStateHolderTest {
    @Test
    fun `settings about is reachable from first-run and preserves search state on return`() {
        val stateHolder = OxygenAppStateHolder(
            geocodingRepository = StaticResultGeocodingRepository(
                candidate = candidate("manual-madison", "Madison"),
            ),
            searchExecutor = DirectAboutExecutor,
        )

        stateHolder.onManualLocationQueryChanged("Madison")
        stateHolder.onManualLocationSearchSubmitted()
        val beforeAbout = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertTrue(beforeAbout.searchState is ManualLocationSearchState.Results)

        stateHolder.onOpenAbout()

        val about = stateHolder.presentationState.screen as OxygenAppScreen.About
        assertEquals("Settings / About", about.title)
        assertEquals(
            listOf(AboutSurfaceId.DataSources, AboutSurfaceId.Privacy, AboutSurfaceId.OpenSourceLicenses),
            about.surfaceOptions,
        )

        stateHolder.onAboutBack()

        val returned = stateHolder.presentationState.screen as OxygenAppScreen.FirstRunLocationEntry
        assertEquals("Madison", returned.query)
        assertEquals("Madison", returned.submittedQuery)
        assertTrue(returned.searchState is ManualLocationSearchState.Results)
        assertEquals(null, stateHolder.presentationState.selectedLocation)
    }

    @Test
    fun `settings about is reachable from Home and preserves current forecast presentation on return`() {
        val location = weatherLocation("manual-home", "Home City")
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = StaticWeatherRepository(
                WeatherRepositoryResult.Success(openMeteoBundle(location)),
            ),
            forecastExecutor = DirectAboutExecutor,
        )
        val readyBefore = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady

        stateHolder.onOpenAbout()
        stateHolder.onAboutSurfaceSelected(AboutSurfaceId.Privacy)

        val privacy = stateHolder.presentationState.screen as OxygenAppScreen.About
        assertEquals(AboutSurfaceId.Privacy, privacy.selectedSurface)
        assertTrue(privacy.surfaceState.visibleText().contains("no advertising SDK"))

        stateHolder.onAboutBack()
        assertTrue((stateHolder.presentationState.screen as OxygenAppScreen.About).selectedSurface == null)
        stateHolder.onAboutBack()

        val readyAfter = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady
        assertSame(location, stateHolder.presentationState.selectedLocation)
        assertEquals(readyBefore.dashboard.source, readyAfter.dashboard.source)
        assertEquals("Open-Meteo", readyAfter.dashboard.source.sourceName)
    }

    @Test
    fun `data sources disclosure separates active implemented and roadmap-only providers`() {
        val text = aboutSurfaceState(AboutSurfaceId.DataSources).visibleText()

        assertTrue(text.contains("Open-Meteo is the installed-app default forecast provider"))
        assertTrue(text.contains("Open-Meteo Geocoding API"))
        assertTrue(text.contains("GeoNames"))
        assertTrue(text.contains("MET Norway can serve Home forecasts"))
        assertTrue(text.contains("MET Norway fallback is wired"))
        assertTrue(text.contains("Open-Meteo success"))
        assertTrue(text.contains("cache persistence"))
        assertTrue(text.contains("foreground failed-refresh stale retention"))
        assertTrue(text.contains("Saved-location storage, list display"))
        assertTrue(text.contains("saved-location selection are implemented"))
        assertTrue(text.contains("Installed-app durable Room cache wiring"))
        assertTrue(text.contains("offline cache launch behavior are implemented"))
        assertTrue(text.contains("Provider-specific MET Norway cache headers are persisted"))
        assertTrue(text.contains("cached fallback provenance remains provider-neutral"))
        assertTrue(text.contains("304 not-modified handling"))
        assertTrue(text.contains("NOAA/NWS alerts"))
        assertTrue(text.contains("Environment and Climate Change Canada alerts"))
        assertTrue(text.contains("Open-Meteo/CAMS air quality"))
        assertTrue(text.contains("roadmap-only"))
        assertTrue(text.contains("saved-location save/remove UI"))
        assertFalse(text.contains("unit settings are implemented"))
        assertFalse(text.contains("alerts are implemented"))
    }

    @Test
    fun `privacy disclosure contains active request facts and MET Norway capability facts`() {
        val text = aboutSurfaceState(AboutSurfaceId.Privacy).visibleText()

        assertTrue(text.contains("no advertising SDK"))
        assertTrue(text.contains("behavioral tracking"))
        assertTrue(text.contains("mandatory account"))
        assertTrue(text.contains("Location permission is optional"))
        assertTrue(text.contains("Manual search works without Android location permission"))
        assertTrue(text.contains("selected coordinates"))
        assertTrue(text.contains("timezone"))
        assertTrue(text.contains("typed place query"))
        assertTrue(text.contains("GeoNames"))
        assertTrue(text.contains("optional altitude"))
        assertTrue(text.contains("identifying User-Agent/contact header"))
        assertTrue(text.contains("IP address"))
        assertTrue(text.contains("provider logs"))
        assertTrue(text.contains("used only after eligible Open-Meteo terminal forecast failures"))
    }

    @Test
    fun `open source licenses separate source code license from weather data attribution`() {
        val text = aboutSurfaceState(AboutSurfaceId.OpenSourceLicenses).visibleText()

        assertTrue(text.contains("Oxygen source code is licensed under the repository LICENSE file"))
        assertTrue(text.contains("Weather-data attribution and licensing are separate"))
        assertTrue(text.contains("Open-Meteo forecast and geocoding disclosures"))
        assertTrue(text.contains("GeoNames attribution"))
        assertTrue(text.contains("does not imply endorsement"))
        assertFalse(text.contains("complete dependency license inventory"))
    }

    @Test
    fun `controlled MET Norway Home success keeps served provider provenance visible`() {
        val location = weatherLocation("manual-metno", "Met Norway Fixture City")
        val stateHolder = OxygenAppStateHolder(
            selectedLocation = location,
            weatherRepository = StaticWeatherRepository(
                WeatherRepositoryResult.Success(metNorwayBundle(location)),
            ),
            forecastExecutor = DirectAboutExecutor,
        )

        val ready = (stateHolder.presentationState.screen as OxygenAppScreen.Home)
            .forecast as HomeForecastPresentationState.ForecastReady

        assertEquals("MET Norway", ready.dashboard.source.sourceName)
        assertEquals("Model estimate", ready.dashboard.source.dataType)
        assertEquals("Fetched Aug 22, 7:00 AM CDT", ready.dashboard.source.fetchedAt)
        assertEquals("NLOD 2.0", ready.dashboard.source.license)
        assertFalse(ready.dashboard.visibleText().contains("metno-provider-id"))
    }

    @Test
    fun `disclosure paragraphs stay bounded for narrow large-text rendering`() {
        val allParagraphs = AboutSurfaceId.entries.flatMap { aboutSurfaceState(it).sections }.flatMap { it.body }

        assertTrue(allParagraphs.isNotEmpty())
        allParagraphs.forEach { paragraph ->
            assertTrue("Paragraph too long for compact disclosure UI: $paragraph", paragraph.length <= 210)
        }
    }
}

private object DirectAboutExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

private class StaticResultGeocodingRepository(
    private val candidate: GeocodingLocationCandidate,
) : GeocodingRepository {
    override fun search(
        query: String,
        count: Int,
        language: String?,
        countryCode: String?,
    ): Sequence<GeocodingRepositoryResult> =
        sequenceOf(GeocodingRepositoryResult.Success(listOf(candidate)))
}

private class StaticWeatherRepository(
    private val result: WeatherRepositoryResult,
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> =
        sequenceOf(result)
}

private fun candidate(
    id: String,
    name: String,
): GeocodingLocationCandidate =
    GeocodingLocationCandidate(
        locationId = LocationId(id),
        displayName = name,
        point = GeoPoint(43.0731, -89.4012),
        zoneId = ZoneId.of("America/Chicago"),
        country = "United States",
        countryCode = "US",
        administrativeAreas = listOf("Wisconsin"),
    )

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

private fun openMeteoBundle(location: WeatherLocation): WeatherBundle =
    WeatherBundle(
        location = location,
        current = CurrentConditions(
            time = Instant.parse("2026-08-22T12:00:00Z"),
            temperatureC = 20.0,
            condition = WeatherCondition.CLEAR,
            provenance = provenance("open-meteo-provider-id", "Open-Meteo", "CC BY 4.0"),
        ),
        fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
    )

private fun metNorwayBundle(location: WeatherLocation): WeatherBundle =
    WeatherBundle(
        location = location,
        current = CurrentConditions(
            time = Instant.parse("2026-08-22T12:00:00Z"),
            temperatureC = 17.0,
            condition = WeatherCondition.CLOUDY,
            provenance = provenance("metno-provider-id", "MET Norway", "NLOD 2.0"),
        ),
        fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
    )

private fun provenance(
    providerId: String,
    sourceName: String,
    licenseId: String,
): DataProvenance =
    DataProvenance(
        providerId = providerId,
        sourceName = sourceName,
        issuedAt = Instant.parse("2026-08-22T11:45:00Z"),
        fetchedAt = Instant.parse("2026-08-22T12:00:00Z"),
        type = DataType.MODEL_ESTIMATE,
        licenseId = licenseId,
    )

private fun AboutSurfaceState.visibleText(): String =
    buildString {
        append(title).append('\n')
        sections.forEach { section ->
            append(section.heading).append('\n')
            section.body.forEach { append(it).append('\n') }
        }
    }

private fun HomeSuccessPresentation.visibleText(): String =
    listOf(
        locationName,
        locationSubtitle,
        current?.temperature.orEmpty(),
        current?.condition.orEmpty(),
        source.sourceName,
        source.dataType,
        source.fetchedAt,
        source.issuedAt.orEmpty(),
        source.license.orEmpty(),
    ).joinToString(" ")
