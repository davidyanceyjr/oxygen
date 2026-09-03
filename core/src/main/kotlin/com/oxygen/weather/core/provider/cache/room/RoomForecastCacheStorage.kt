package com.oxygen.weather.core.provider.cache.room

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.oxygen.weather.core.location.SavedLocationStorage
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
import com.oxygen.weather.core.provider.cache.ForecastCacheStorage
import java.time.Instant
import java.time.ZoneId

class RoomForecastCacheStorage internal constructor(
    private val database: OxygenForecastCacheDatabase,
    private val beforeCommit: () -> Unit = {},
) : ForecastCacheStorage {
    private val dao = database.forecastCacheDao()

    override fun replaceBundle(bundle: WeatherBundle) {
        require(bundle.alerts.isEmpty()) {
            "RoomForecastCacheStorage is forecast-only and does not persist weather alerts yet."
        }
        require(bundle.airQuality == null) {
            "RoomForecastCacheStorage is forecast-only and does not persist air quality yet."
        }
        dao.replaceBundle(bundle.toCachedBundleRecord(), beforeCommit)
    }

    override fun readBundle(locationId: LocationId): WeatherBundle? =
        dao.readBundle(locationId.value)?.toBundle()
}

object RoomForecastCacheStorageFactory {
    fun create(context: Context): ForecastCacheStorage =
        RoomForecastCacheStorage(
            Room.databaseBuilder(
                context.applicationContext,
                OxygenForecastCacheDatabase::class.java,
                "oxygen_forecast_cache.db",
            ).addMigrations(OXYGEN_DATABASE_MIGRATION_1_2).build(),
        )
}

class RoomSavedLocationStorage internal constructor(
    private val database: OxygenForecastCacheDatabase,
) : SavedLocationStorage {
    private val dao = database.savedLocationDao()

    override fun saveLocation(location: WeatherLocation) {
        dao.saveLocation(location)
    }

    override fun listLocations(): List<WeatherLocation> =
        dao.listLocations().map { it.toLocation() }

    override fun removeLocation(locationId: LocationId) {
        dao.removeLocation(locationId.value)
    }
}

object RoomSavedLocationStorageFactory {
    fun create(context: Context): SavedLocationStorage =
        RoomSavedLocationStorage(
            Room.databaseBuilder(
                context.applicationContext,
                OxygenForecastCacheDatabase::class.java,
                "oxygen_forecast_cache.db",
            ).addMigrations(OXYGEN_DATABASE_MIGRATION_1_2).build(),
        )
}

@Database(
    entities = [
        CachedForecastLocationEntity::class,
        CachedForecastMetadataEntity::class,
        CachedCurrentConditionsEntity::class,
        CachedHourlyForecastEntity::class,
        CachedDailyForecastEntity::class,
        SavedLocationEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
internal abstract class OxygenForecastCacheDatabase : RoomDatabase() {
    abstract fun forecastCacheDao(): ForecastCacheDao
    abstract fun savedLocationDao(): SavedLocationDao
}

internal val OXYGEN_DATABASE_MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `saved_locations` (
                `id` TEXT NOT NULL,
                `displayName` TEXT NOT NULL,
                `latitude` REAL NOT NULL,
                `longitude` REAL NOT NULL,
                `elevationMeters` REAL,
                `zoneId` TEXT NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_saved_locations_sortOrder` ON `saved_locations` (`sortOrder`)")
    }
}

@Dao
internal abstract class ForecastCacheDao {
    @Transaction
    open fun replaceBundle(record: CachedBundleRecord, beforeCommit: () -> Unit) {
        val locationId = record.location.id
        deleteCurrent(locationId)
        deleteHourly(locationId)
        deleteDaily(locationId)
        deleteMetadata(locationId)
        insertLocation(record.location)
        insertMetadata(record.metadata)
        record.current?.let(::insertCurrent)
        insertHourly(record.hourly)
        insertDaily(record.daily)
        beforeCommit()
    }

    @Transaction
    open fun readBundle(locationId: String): CachedBundleRecord? {
        val location = readLocation(locationId) ?: return null
        val metadata = readMetadata(locationId) ?: return null
        return CachedBundleRecord(
            location = location,
            metadata = metadata,
            current = readCurrent(locationId),
            hourly = readHourly(locationId),
            daily = readDaily(locationId),
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertLocation(entity: CachedForecastLocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertMetadata(entity: CachedForecastMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertCurrent(entity: CachedCurrentConditionsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertHourly(entities: List<CachedHourlyForecastEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertDaily(entities: List<CachedDailyForecastEntity>)

    @Query("DELETE FROM cached_current_conditions WHERE location_id = :locationId")
    protected abstract fun deleteCurrent(locationId: String)

    @Query("DELETE FROM cached_hourly_forecasts WHERE location_id = :locationId")
    protected abstract fun deleteHourly(locationId: String)

    @Query("DELETE FROM cached_daily_forecasts WHERE location_id = :locationId")
    protected abstract fun deleteDaily(locationId: String)

    @Query("DELETE FROM cached_forecast_metadata WHERE location_id = :locationId")
    protected abstract fun deleteMetadata(locationId: String)

    @Query("SELECT * FROM cached_forecast_locations WHERE id = :locationId")
    protected abstract fun readLocation(locationId: String): CachedForecastLocationEntity?

    @Query("SELECT * FROM cached_forecast_metadata WHERE location_id = :locationId")
    protected abstract fun readMetadata(locationId: String): CachedForecastMetadataEntity?

    @Query("SELECT * FROM cached_current_conditions WHERE location_id = :locationId")
    protected abstract fun readCurrent(locationId: String): CachedCurrentConditionsEntity?

    @Query("SELECT * FROM cached_hourly_forecasts WHERE location_id = :locationId ORDER BY row_index ASC")
    protected abstract fun readHourly(locationId: String): List<CachedHourlyForecastEntity>

    @Query("SELECT * FROM cached_daily_forecasts WHERE location_id = :locationId ORDER BY row_index ASC")
    protected abstract fun readDaily(locationId: String): List<CachedDailyForecastEntity>
}

@Dao
internal abstract class SavedLocationDao {
    @Transaction
    open fun saveLocation(location: WeatherLocation) {
        insertLocation(location.toSavedLocationEntity(nextSortOrder()))
    }

    @Query("SELECT COALESCE(MAX(sortOrder), 0) + 1 FROM saved_locations")
    protected abstract fun nextSortOrder(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertLocation(entity: SavedLocationEntity)

    @Query("SELECT * FROM saved_locations ORDER BY sortOrder ASC")
    abstract fun listLocations(): List<SavedLocationEntity>

    @Query("DELETE FROM saved_locations WHERE id = :locationId")
    abstract fun removeLocation(locationId: String)
}

@Entity(tableName = "cached_forecast_locations")
internal data class CachedForecastLocationEntity(
    @PrimaryKey
    val id: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double?,
    val zoneId: String,
)

@Entity(
    tableName = "cached_forecast_metadata",
    foreignKeys = [
        ForeignKey(
            entity = CachedForecastLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["location_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class CachedForecastMetadataEntity(
    @PrimaryKey
    @androidx.room.ColumnInfo(name = "location_id")
    val locationId: String,
    val bundleFetchedAt: String,
)

@Entity(
    tableName = "cached_current_conditions",
    foreignKeys = [
        ForeignKey(
            entity = CachedForecastLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["location_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class CachedCurrentConditionsEntity(
    @PrimaryKey
    @androidx.room.ColumnInfo(name = "location_id")
    val locationId: String,
    val time: String,
    val temperatureC: Double?,
    val apparentTemperatureC: Double?,
    val dewPointC: Double?,
    val humidityPercent: Int?,
    val pressureHpa: Double?,
    val visibilityMeters: Double?,
    val cloudCoverPercent: Int?,
    val windSpeedMetersPerSecond: Double?,
    val windGustMetersPerSecond: Double?,
    val windDirectionDegrees: Double?,
    val precipitationMm: Double?,
    val condition: String,
    val provenanceProviderId: String,
    val provenanceSourceName: String,
    val provenanceIssuedAt: String?,
    val provenanceFetchedAt: String,
    val provenanceType: String,
    val provenanceLicenseId: String?,
)

@Entity(
    tableName = "cached_hourly_forecasts",
    primaryKeys = ["location_id", "row_index"],
    foreignKeys = [
        ForeignKey(
            entity = CachedForecastLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["location_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class CachedHourlyForecastEntity(
    @androidx.room.ColumnInfo(name = "location_id")
    val locationId: String,
    @androidx.room.ColumnInfo(name = "row_index")
    val rowIndex: Int,
    val time: String,
    val temperatureC: Double?,
    val precipitationProbabilityPercent: Int?,
    val precipitationMm: Double?,
    val condition: String,
    val provenanceProviderId: String,
    val provenanceSourceName: String,
    val provenanceIssuedAt: String?,
    val provenanceFetchedAt: String,
    val provenanceType: String,
    val provenanceLicenseId: String?,
)

@Entity(
    tableName = "cached_daily_forecasts",
    primaryKeys = ["location_id", "row_index"],
    foreignKeys = [
        ForeignKey(
            entity = CachedForecastLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["location_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class CachedDailyForecastEntity(
    @androidx.room.ColumnInfo(name = "location_id")
    val locationId: String,
    @androidx.room.ColumnInfo(name = "row_index")
    val rowIndex: Int,
    val dateEpochDay: Long,
    val highC: Double?,
    val lowC: Double?,
    val precipitationProbabilityPercent: Int?,
    val condition: String,
    val sunrise: String?,
    val sunset: String?,
    val provenanceProviderId: String,
    val provenanceSourceName: String,
    val provenanceIssuedAt: String?,
    val provenanceFetchedAt: String,
    val provenanceType: String,
    val provenanceLicenseId: String?,
)

@Entity(
    tableName = "saved_locations",
    indices = [Index(value = ["sortOrder"])],
)
internal data class SavedLocationEntity(
    @PrimaryKey
    val id: String,
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double?,
    val zoneId: String,
    val sortOrder: Long,
)

internal data class CachedBundleRecord(
    val location: CachedForecastLocationEntity,
    val metadata: CachedForecastMetadataEntity,
    val current: CachedCurrentConditionsEntity?,
    val hourly: List<CachedHourlyForecastEntity>,
    val daily: List<CachedDailyForecastEntity>,
) {
    fun toBundle(): WeatherBundle =
        WeatherBundle(
            location = location.toLocation(),
            current = current?.toCurrent(),
            hourly = hourly.map { it.toHourly() },
            daily = daily.map { it.toDaily() },
            fetchedAt = Instant.parse(metadata.bundleFetchedAt),
        )
}

private fun WeatherBundle.toCachedBundleRecord(): CachedBundleRecord =
    CachedBundleRecord(
        location = location.toEntity(),
        metadata = CachedForecastMetadataEntity(
            locationId = location.id.value,
            bundleFetchedAt = fetchedAt.toString(),
        ),
        current = current?.toEntity(location.id.value),
        hourly = hourly.mapIndexed { index, forecast -> forecast.toEntity(location.id.value, index) },
        daily = daily.mapIndexed { index, forecast -> forecast.toEntity(location.id.value, index) },
    )

private fun WeatherLocation.toEntity(): CachedForecastLocationEntity =
    CachedForecastLocationEntity(
        id = id.value,
        displayName = displayName,
        latitude = point.latitude,
        longitude = point.longitude,
        elevationMeters = elevationMeters,
        zoneId = zoneId.id,
    )

private fun WeatherLocation.toSavedLocationEntity(sortOrder: Long): SavedLocationEntity =
    SavedLocationEntity(
        id = id.value,
        displayName = displayName,
        latitude = point.latitude,
        longitude = point.longitude,
        elevationMeters = elevationMeters,
        zoneId = zoneId.id,
        sortOrder = sortOrder,
    )

private fun CachedForecastLocationEntity.toLocation(): WeatherLocation =
    WeatherLocation(
        id = LocationId(id),
        displayName = displayName,
        point = GeoPoint(latitude, longitude),
        elevationMeters = elevationMeters,
        zoneId = ZoneId.of(zoneId),
    )

private fun SavedLocationEntity.toLocation(): WeatherLocation =
    WeatherLocation(
        id = LocationId(id),
        displayName = displayName,
        point = GeoPoint(latitude, longitude),
        elevationMeters = elevationMeters,
        zoneId = ZoneId.of(zoneId),
    )

private fun CurrentConditions.toEntity(locationId: String): CachedCurrentConditionsEntity =
    CachedCurrentConditionsEntity(
        locationId = locationId,
        time = time.toString(),
        temperatureC = temperatureC,
        apparentTemperatureC = apparentTemperatureC,
        dewPointC = dewPointC,
        humidityPercent = humidityPercent,
        pressureHpa = pressureHpa,
        visibilityMeters = visibilityMeters,
        cloudCoverPercent = cloudCoverPercent,
        windSpeedMetersPerSecond = wind?.speedMetersPerSecond,
        windGustMetersPerSecond = wind?.gustMetersPerSecond,
        windDirectionDegrees = wind?.directionDegrees,
        precipitationMm = precipitationMm,
        condition = condition.name,
        provenanceProviderId = provenance.providerId,
        provenanceSourceName = provenance.sourceName,
        provenanceIssuedAt = provenance.issuedAt?.toString(),
        provenanceFetchedAt = provenance.fetchedAt.toString(),
        provenanceType = provenance.type.name,
        provenanceLicenseId = provenance.licenseId,
    )

private fun CachedCurrentConditionsEntity.toCurrent(): CurrentConditions =
    CurrentConditions(
        time = Instant.parse(time),
        temperatureC = temperatureC,
        apparentTemperatureC = apparentTemperatureC,
        dewPointC = dewPointC,
        humidityPercent = humidityPercent,
        pressureHpa = pressureHpa,
        visibilityMeters = visibilityMeters,
        cloudCoverPercent = cloudCoverPercent,
        wind = Wind(
            speedMetersPerSecond = windSpeedMetersPerSecond,
            gustMetersPerSecond = windGustMetersPerSecond,
            directionDegrees = windDirectionDegrees,
        ).takeIf {
            it.speedMetersPerSecond != null ||
                it.gustMetersPerSecond != null ||
                it.directionDegrees != null
        },
        precipitationMm = precipitationMm,
        condition = WeatherCondition.valueOf(condition),
        provenance = toProvenance(),
    )

private fun HourlyForecast.toEntity(locationId: String, rowIndex: Int): CachedHourlyForecastEntity =
    CachedHourlyForecastEntity(
        locationId = locationId,
        rowIndex = rowIndex,
        time = time.toString(),
        temperatureC = temperatureC,
        precipitationProbabilityPercent = precipitationProbabilityPercent,
        precipitationMm = precipitationMm,
        condition = condition.name,
        provenanceProviderId = provenance.providerId,
        provenanceSourceName = provenance.sourceName,
        provenanceIssuedAt = provenance.issuedAt?.toString(),
        provenanceFetchedAt = provenance.fetchedAt.toString(),
        provenanceType = provenance.type.name,
        provenanceLicenseId = provenance.licenseId,
    )

private fun CachedHourlyForecastEntity.toHourly(): HourlyForecast =
    HourlyForecast(
        time = Instant.parse(time),
        temperatureC = temperatureC,
        precipitationProbabilityPercent = precipitationProbabilityPercent,
        precipitationMm = precipitationMm,
        condition = WeatherCondition.valueOf(condition),
        provenance = toProvenance(),
    )

private fun DailyForecast.toEntity(locationId: String, rowIndex: Int): CachedDailyForecastEntity =
    CachedDailyForecastEntity(
        locationId = locationId,
        rowIndex = rowIndex,
        dateEpochDay = dateEpochDay,
        highC = highC,
        lowC = lowC,
        precipitationProbabilityPercent = precipitationProbabilityPercent,
        condition = condition.name,
        sunrise = sunrise?.toString(),
        sunset = sunset?.toString(),
        provenanceProviderId = provenance.providerId,
        provenanceSourceName = provenance.sourceName,
        provenanceIssuedAt = provenance.issuedAt?.toString(),
        provenanceFetchedAt = provenance.fetchedAt.toString(),
        provenanceType = provenance.type.name,
        provenanceLicenseId = provenance.licenseId,
    )

private fun CachedDailyForecastEntity.toDaily(): DailyForecast =
    DailyForecast(
        dateEpochDay = dateEpochDay,
        highC = highC,
        lowC = lowC,
        precipitationProbabilityPercent = precipitationProbabilityPercent,
        condition = WeatherCondition.valueOf(condition),
        sunrise = sunrise?.let(Instant::parse),
        sunset = sunset?.let(Instant::parse),
        provenance = toProvenance(),
    )

private fun CachedCurrentConditionsEntity.toProvenance(): DataProvenance =
    DataProvenance(
        providerId = provenanceProviderId,
        sourceName = provenanceSourceName,
        issuedAt = provenanceIssuedAt?.let(Instant::parse),
        fetchedAt = Instant.parse(provenanceFetchedAt),
        type = DataType.valueOf(provenanceType),
        licenseId = provenanceLicenseId,
    )

private fun CachedHourlyForecastEntity.toProvenance(): DataProvenance =
    DataProvenance(
        providerId = provenanceProviderId,
        sourceName = provenanceSourceName,
        issuedAt = provenanceIssuedAt?.let(Instant::parse),
        fetchedAt = Instant.parse(provenanceFetchedAt),
        type = DataType.valueOf(provenanceType),
        licenseId = provenanceLicenseId,
    )

private fun CachedDailyForecastEntity.toProvenance(): DataProvenance =
    DataProvenance(
        providerId = provenanceProviderId,
        sourceName = provenanceSourceName,
        issuedAt = provenanceIssuedAt?.let(Instant::parse),
        fetchedAt = Instant.parse(provenanceFetchedAt),
        type = DataType.valueOf(provenanceType),
        licenseId = provenanceLicenseId,
    )
