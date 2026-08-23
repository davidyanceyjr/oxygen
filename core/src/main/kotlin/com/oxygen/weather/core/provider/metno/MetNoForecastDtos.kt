package com.oxygen.weather.core.provider.metno

data class MetNoForecastResponse(
    val type: String,
    val geometry: MetNoGeometry,
    val meta: MetNoMeta,
    val timeseries: List<MetNoTimeStep>,
)

data class MetNoGeometry(
    val type: String?,
    val coordinates: List<Double>,
) {
    val longitude: Double = coordinates[0]
    val latitude: Double = coordinates[1]
    val altitudeMeters: Double? = coordinates.getOrNull(2)
}

data class MetNoMeta(
    val updatedAt: String,
    val units: Map<String, String>,
)

data class MetNoTimeStep(
    val time: String,
    val instant: MetNoInstant,
    val next1Hours: MetNoPeriodForecast?,
    val next6Hours: MetNoPeriodForecast?,
    val next12Hours: MetNoPeriodForecast?,
)

data class MetNoInstant(
    val details: MetNoInstantDetails,
)

data class MetNoInstantDetails(
    val airTemperature: Double?,
    val airPressureAtSeaLevel: Double?,
    val cloudAreaFraction: Double?,
    val cloudAreaFractionHigh: Double?,
    val cloudAreaFractionMedium: Double?,
    val cloudAreaFractionLow: Double?,
    val dewPointTemperature: Double?,
    val fogAreaFraction: Double?,
    val relativeHumidity: Double?,
    val ultravioletIndexClearSky: Double?,
    val windFromDirection: Double?,
    val windSpeed: Double?,
    val windSpeedOfGust: Double?,
)

data class MetNoPeriodForecast(
    val summary: MetNoPeriodSummary?,
    val details: MetNoPeriodDetails?,
)

data class MetNoPeriodSummary(
    val symbolCode: String?,
)

data class MetNoPeriodDetails(
    val precipitationAmount: Double?,
    val precipitationAmountMin: Double?,
    val precipitationAmountMax: Double?,
    val probabilityOfPrecipitation: Double?,
    val probabilityOfThunder: Double?,
    val airTemperatureMin: Double?,
    val airTemperatureMax: Double?,
    val ultravioletIndexClearSkyMax: Double?,
)
