package com.oxygen.weather.core.model

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT,
}

enum class WindSpeedUnit {
    KILOMETERS_PER_HOUR,
    MILES_PER_HOUR,
    METERS_PER_SECOND,
    KNOTS,
}

enum class PressureUnit {
    HECTOPASCALS,
    INCHES_OF_MERCURY,
    MILLIMETERS_OF_MERCURY,
}

enum class PrecipitationUnit {
    MILLIMETERS,
    INCHES,
}

enum class VisibilityUnit {
    KILOMETERS,
    MILES,
}

enum class UnitPreferencePreset {
    METRIC,
    US,
    UK,
}

sealed interface UnitPreference {
    data class Preset(val preset: UnitPreferencePreset) : UnitPreference

    data class Custom(
        val temperature: TemperatureUnit,
        val windSpeed: WindSpeedUnit,
        val pressure: PressureUnit,
        val precipitation: PrecipitationUnit,
        val visibility: VisibilityUnit,
    ) : UnitPreference
}

data class ResolvedUnitPreference(
    val temperature: TemperatureUnit,
    val windSpeed: WindSpeedUnit,
    val pressure: PressureUnit,
    val precipitation: PrecipitationUnit,
    val visibility: VisibilityUnit,
)

fun UnitPreference.resolve(): ResolvedUnitPreference =
    when (this) {
        is UnitPreference.Custom -> ResolvedUnitPreference(
            temperature = temperature,
            windSpeed = windSpeed,
            pressure = pressure,
            precipitation = precipitation,
            visibility = visibility,
        )
        is UnitPreference.Preset -> preset.resolve()
    }

fun UnitPreferencePreset.resolve(): ResolvedUnitPreference =
    when (this) {
        UnitPreferencePreset.METRIC -> ResolvedUnitPreference(
            temperature = TemperatureUnit.CELSIUS,
            windSpeed = WindSpeedUnit.KILOMETERS_PER_HOUR,
            pressure = PressureUnit.HECTOPASCALS,
            precipitation = PrecipitationUnit.MILLIMETERS,
            visibility = VisibilityUnit.KILOMETERS,
        )
        UnitPreferencePreset.US -> ResolvedUnitPreference(
            temperature = TemperatureUnit.FAHRENHEIT,
            windSpeed = WindSpeedUnit.MILES_PER_HOUR,
            pressure = PressureUnit.INCHES_OF_MERCURY,
            precipitation = PrecipitationUnit.INCHES,
            visibility = VisibilityUnit.MILES,
        )
        UnitPreferencePreset.UK -> ResolvedUnitPreference(
            temperature = TemperatureUnit.CELSIUS,
            windSpeed = WindSpeedUnit.MILES_PER_HOUR,
            pressure = PressureUnit.HECTOPASCALS,
            precipitation = PrecipitationUnit.MILLIMETERS,
            visibility = VisibilityUnit.MILES,
        )
    }
