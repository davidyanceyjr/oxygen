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
    }
}
