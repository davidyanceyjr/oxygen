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
    fun `main activity wires DataStore selected location Room saved locations and installed fallback repository`() {
        val source = Files.readString(Path.of("src/main/kotlin/com/oxygen/weather/MainActivity.kt"))

        assertTrue(source.contains("DataStoreSelectedLocationStorage"))
        assertTrue(source.contains("RoomForecastCacheStorageFactory"))
        assertTrue(source.contains("RoomSavedLocationStorageFactory"))
        assertTrue(source.contains("savedLocationStorage = savedLocationStorage"))
        assertTrue(source.contains("InstalledForecastRepositoryFactory.create"))
        assertFalse(source.contains("FileForecastCacheStorage"))
        assertFalse(source.contains("SampleWeather"))
    }

    @Test
    fun `installed forecast repository factory composes default fallback and cache repositories`() {
        val source = Files.readString(Path.of("src/main/kotlin/com/oxygen/weather/app/InstalledForecastRepositoryFactory.kt"))

        assertTrue(source.contains("OpenMeteoWeatherRepository"))
        assertTrue(source.contains("MetNoWeatherRepository"))
        assertTrue(source.contains("FallbackWeatherRepository"))
        assertTrue(source.contains("CachedWeatherRepository"))
        assertTrue(source.contains("MetNoForecastClient.DEFAULT_USER_AGENT"))
        assertFalse(source.contains("SampleWeather"))
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

    @Test
    fun `home UI semantic decisions do not depend on metric display labels or formatted numeric parsing`() {
        val source = Files.readString(Path.of("src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt"))

        assertFalse(source.contains("it.label =="))
        assertFalse(source.contains("label =="))
        assertFalse(source.contains("firstOrNull { it.label"))
        assertFalse(source.contains("toDouble("))
        assertFalse(source.contains("toInt("))
        assertFalse(source.contains("Regex("))
    }
}
