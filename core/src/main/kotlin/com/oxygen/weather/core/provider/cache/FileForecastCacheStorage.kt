package com.oxygen.weather.core.provider.cache

import com.oxygen.weather.core.model.CurrentConditions
import com.oxygen.weather.core.model.DailyForecast
import com.oxygen.weather.core.model.DataProvenance
import com.oxygen.weather.core.model.DataType
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.HourlyForecast
import com.oxygen.weather.core.model.LocationId
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherCondition
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.model.Wind
import java.io.EOFException
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.ZoneId
import java.util.Base64

class FileForecastCacheStorage(
    private val directory: File,
    private val beforeCommit: () -> Unit = {},
) : ForecastCacheStorage {
    override fun replaceBundle(bundle: WeatherBundle) {
        directory.mkdirs()
        val target = fileFor(bundle.location.id)
        val temporary = File(directory, "${target.name}.tmp")

        ObjectOutputStream(temporary.outputStream().buffered()).use { output ->
            output.writeObject(bundle.toRecord())
        }

        beforeCommit()
        try {
            Files.move(
                temporary.toPath(),
                target.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(
                temporary.toPath(),
                target.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }

    override fun readBundle(locationId: LocationId): WeatherBundle? {
        val file = fileFor(locationId)
        if (!file.isFile) return null
        return try {
            ObjectInputStream(file.inputStream().buffered()).use { input ->
                (input.readObject() as? BundleRecord)?.toBundle()
            }
        } catch (_: EOFException) {
            null
        } catch (_: RuntimeException) {
            null
        } catch (_: ClassNotFoundException) {
            null
        }
    }

    private fun fileFor(locationId: LocationId): File {
        val key = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(locationId.value.toByteArray(Charsets.UTF_8))
        return File(directory, "$key.forecast")
    }
}

private data class BundleRecord(
    val location: LocationRecord,
    val current: CurrentRecord?,
    val hourly: List<HourlyRecord>,
    val daily: List<DailyRecord>,
    val fetchedAt: String,
) : Serializable {
    fun toBundle(): WeatherBundle =
        WeatherBundle(
            location = location.toLocation(),
            current = current?.toCurrent(),
            hourly = hourly.map { it.toHourly() },
            daily = daily.map { it.toDaily() },
            fetchedAt = Instant.parse(fetchedAt),
        )
}

private data class LocationRecord(
    val id: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double?,
    val zoneId: String,
) : Serializable {
    fun toLocation(): WeatherLocation =
        WeatherLocation(
            id = LocationId(id),
            displayName = displayName,
            point = GeoPoint(latitude, longitude),
            elevationMeters = elevationMeters,
            zoneId = ZoneId.of(zoneId),
        )
}

private data class ProvenanceRecord(
    val providerId: String,
    val sourceName: String,
    val issuedAt: String?,
    val fetchedAt: String,
    val type: String,
    val licenseId: String?,
) : Serializable {
    fun toProvenance(): DataProvenance =
        DataProvenance(
            providerId = providerId,
            sourceName = sourceName,
            issuedAt = issuedAt?.let(Instant::parse),
            fetchedAt = Instant.parse(fetchedAt),
            type = DataType.valueOf(type),
            licenseId = licenseId,
        )
}

private data class WindRecord(
    val speedMetersPerSecond: Double?,
    val gustMetersPerSecond: Double?,
    val directionDegrees: Double?,
) : Serializable {
    fun toWind(): Wind =
        Wind(
            speedMetersPerSecond = speedMetersPerSecond,
            gustMetersPerSecond = gustMetersPerSecond,
            directionDegrees = directionDegrees,
        )
}

private data class CurrentRecord(
    val time: String,
    val temperatureC: Double?,
    val apparentTemperatureC: Double?,
    val dewPointC: Double?,
    val humidityPercent: Int?,
    val pressureHpa: Double?,
    val visibilityMeters: Double?,
    val cloudCoverPercent: Int?,
    val wind: WindRecord?,
    val precipitationMm: Double?,
    val condition: String,
    val provenance: ProvenanceRecord,
) : Serializable {
    fun toCurrent(): CurrentConditions =
        CurrentConditions(
            time = Instant.parse(time),
            temperatureC = temperatureC,
            apparentTemperatureC = apparentTemperatureC,
            dewPointC = dewPointC,
            humidityPercent = humidityPercent,
            pressureHpa = pressureHpa,
            visibilityMeters = visibilityMeters,
            cloudCoverPercent = cloudCoverPercent,
            wind = wind?.toWind(),
            precipitationMm = precipitationMm,
            condition = WeatherCondition.valueOf(condition),
            provenance = provenance.toProvenance(),
        )
}

private data class HourlyRecord(
    val time: String,
    val temperatureC: Double?,
    val precipitationProbabilityPercent: Int?,
    val precipitationMm: Double?,
    val condition: String,
    val provenance: ProvenanceRecord,
) : Serializable {
    fun toHourly(): HourlyForecast =
        HourlyForecast(
            time = Instant.parse(time),
            temperatureC = temperatureC,
            precipitationProbabilityPercent = precipitationProbabilityPercent,
            precipitationMm = precipitationMm,
            condition = WeatherCondition.valueOf(condition),
            provenance = provenance.toProvenance(),
        )
}

private data class DailyRecord(
    val dateEpochDay: Long,
    val highC: Double?,
    val lowC: Double?,
    val precipitationProbabilityPercent: Int?,
    val condition: String,
    val sunrise: String?,
    val sunset: String?,
    val provenance: ProvenanceRecord,
) : Serializable {
    fun toDaily(): DailyForecast =
        DailyForecast(
            dateEpochDay = dateEpochDay,
            highC = highC,
            lowC = lowC,
            precipitationProbabilityPercent = precipitationProbabilityPercent,
            condition = WeatherCondition.valueOf(condition),
            sunrise = sunrise?.let(Instant::parse),
            sunset = sunset?.let(Instant::parse),
            provenance = provenance.toProvenance(),
        )
}

private fun WeatherBundle.toRecord(): BundleRecord =
    BundleRecord(
        location = location.toRecord(),
        current = current?.toRecord(),
        hourly = hourly.map { it.toRecord() },
        daily = daily.map { it.toRecord() },
        fetchedAt = fetchedAt.toString(),
    )

private fun WeatherLocation.toRecord(): LocationRecord =
    LocationRecord(
        id = id.value,
        displayName = displayName,
        latitude = point.latitude,
        longitude = point.longitude,
        elevationMeters = elevationMeters,
        zoneId = zoneId.id,
    )

private fun DataProvenance.toRecord(): ProvenanceRecord =
    ProvenanceRecord(
        providerId = providerId,
        sourceName = sourceName,
        issuedAt = issuedAt?.toString(),
        fetchedAt = fetchedAt.toString(),
        type = type.name,
        licenseId = licenseId,
    )

private fun Wind.toRecord(): WindRecord =
    WindRecord(
        speedMetersPerSecond = speedMetersPerSecond,
        gustMetersPerSecond = gustMetersPerSecond,
        directionDegrees = directionDegrees,
    )

private fun CurrentConditions.toRecord(): CurrentRecord =
    CurrentRecord(
        time = time.toString(),
        temperatureC = temperatureC,
        apparentTemperatureC = apparentTemperatureC,
        dewPointC = dewPointC,
        humidityPercent = humidityPercent,
        pressureHpa = pressureHpa,
        visibilityMeters = visibilityMeters,
        cloudCoverPercent = cloudCoverPercent,
        wind = wind?.toRecord(),
        precipitationMm = precipitationMm,
        condition = condition.name,
        provenance = provenance.toRecord(),
    )

private fun HourlyForecast.toRecord(): HourlyRecord =
    HourlyRecord(
        time = time.toString(),
        temperatureC = temperatureC,
        precipitationProbabilityPercent = precipitationProbabilityPercent,
        precipitationMm = precipitationMm,
        condition = condition.name,
        provenance = provenance.toRecord(),
    )

private fun DailyForecast.toRecord(): DailyRecord =
    DailyRecord(
        dateEpochDay = dateEpochDay,
        highC = highC,
        lowC = lowC,
        precipitationProbabilityPercent = precipitationProbabilityPercent,
        condition = condition.name,
        sunrise = sunrise?.toString(),
        sunset = sunset?.toString(),
        provenance = provenance.toRecord(),
    )
