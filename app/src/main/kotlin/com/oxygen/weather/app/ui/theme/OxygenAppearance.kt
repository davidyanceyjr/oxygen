package com.oxygen.weather.app.ui.theme

enum class LayoutPreset { SIMPLE, STANDARD, DETAILED, METEOROLOGIST }
enum class EffectsLevel { OFF, SUBTLE, FULL }
enum class WeatherIconPack { OXYGEN, MONOCHROME }

data class OxygenAppearance(
    val theme: OxygenThemeId = OxygenThemeId.OXYGEN,
    val layout: LayoutPreset = LayoutPreset.STANDARD,
    val effects: EffectsLevel = EffectsLevel.SUBTLE,
    val iconPack: WeatherIconPack = WeatherIconPack.OXYGEN,
)

fun LayoutPreset.displayName(): String = when (this) {
    LayoutPreset.SIMPLE -> "Simple"
    LayoutPreset.STANDARD -> "Standard"
    LayoutPreset.DETAILED -> "Detailed"
    LayoutPreset.METEOROLOGIST -> "Meteorologist"
}
