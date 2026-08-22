package com.oxygen.weather.app

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OxygenAppContractTest {
    @Test
    fun `production oxygen app source does not import or pass sample weather`() {
        val source = Files.readString(Path.of("src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt"))

        assertFalse(source.contains("SampleWeather"))
        assertFalse(source.contains("SampleWeather.bundle"))
    }

    @Test
    fun `default production app state has first-run copy for manual search and location action`() {
        val firstRun = OxygenAppStateHolder().presentationState.screen as OxygenAppScreen.FirstRunLocationEntry

        assertTrue(firstRun.title.contains("Choose a location"))
        assertTrue(firstRun.searchLabel.contains("Search"))
        assertTrue(firstRun.useMyLocationLabel.contains("Use my location"))
        assertTrue(firstRun.geocodingDisclosure.contains("Open-Meteo"))
        assertTrue(firstRun.geocodingDisclosure.contains("GeoNames"))
        assertTrue(firstRun.geocodingPrivacyNote.contains("typed search"))
    }

    @Test
    fun `app UI state and composables do not expose Open-Meteo geocoding internals`() {
        val productionBoundaryFiles = listOf(
            Path.of("src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt"),
            Path.of("src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt"),
            Path.of("src/main/kotlin/com/oxygen/weather/app/ui/firstrun/FirstRunLocationEntryScreen.kt"),
            Path.of("src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt"),
        )
        val source = productionBoundaryFiles.joinToString("\n") { Files.readString(it) }

        assertFalse(source.contains("OpenMeteoGeocodingDto"))
        assertFalse(source.contains("OpenMeteoGeocodingResult"))
        assertFalse(source.contains("OpenMeteoForecastResponse"))
        assertFalse(source.contains("OpenMeteoForecastClientResult"))
        assertFalse(source.contains("providerId"))
    }
}
