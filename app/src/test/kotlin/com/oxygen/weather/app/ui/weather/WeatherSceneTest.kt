package com.oxygen.weather.app.ui.weather

import com.oxygen.weather.core.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherSceneTest {
    @Test
    fun everyWeatherConditionSelectsAnExplicitStaticProfile() {
        val expected = mapOf(
            WeatherCondition.CLEAR to WeatherSceneProfile.CLEAR,
            WeatherCondition.MOSTLY_CLEAR to WeatherSceneProfile.CLEAR,
            WeatherCondition.PARTLY_CLOUDY to WeatherSceneProfile.CLOUDY,
            WeatherCondition.CLOUDY to WeatherSceneProfile.CLOUDY,
            WeatherCondition.FOG to WeatherSceneProfile.FOG,
            WeatherCondition.DRIZZLE to WeatherSceneProfile.RAIN,
            WeatherCondition.FREEZING_DRIZZLE to WeatherSceneProfile.RAIN,
            WeatherCondition.RAIN to WeatherSceneProfile.RAIN,
            WeatherCondition.FREEZING_RAIN to WeatherSceneProfile.RAIN,
            WeatherCondition.RAIN_SHOWERS to WeatherSceneProfile.RAIN_SHOWERS,
            WeatherCondition.SNOW to WeatherSceneProfile.FROZEN_PRECIPITATION,
            WeatherCondition.SNOW_SHOWERS to WeatherSceneProfile.FROZEN_PRECIPITATION,
            WeatherCondition.SLEET to WeatherSceneProfile.FROZEN_PRECIPITATION,
            WeatherCondition.HAIL to WeatherSceneProfile.FROZEN_PRECIPITATION,
            WeatherCondition.THUNDERSTORM to WeatherSceneProfile.THUNDERSTORM,
            WeatherCondition.THUNDERSTORM_HAIL to WeatherSceneProfile.THUNDERSTORM,
            WeatherCondition.UNKNOWN to WeatherSceneProfile.NEUTRAL,
        )

        assertEquals(WeatherCondition.entries.toSet(), expected.keys)
        expected.forEach { (condition, profile) ->
            assertEquals(profile, weatherSceneProfile(condition))
        }
    }
}
