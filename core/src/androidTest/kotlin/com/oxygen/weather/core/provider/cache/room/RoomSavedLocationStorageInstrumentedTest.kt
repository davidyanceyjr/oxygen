package com.oxygen.weather.core.provider.cache.room

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
import java.io.File
import java.time.Instant
import java.time.ZoneId
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomSavedLocationStorageInstrumentedTest {
    private lateinit var database: OxygenForecastCacheDatabase
    private lateinit var savedLocations: RoomSavedLocationStorage
    private lateinit var forecastCache: RoomForecastCacheStorage

    @Before
    fun setUp() {
        database = inMemoryDatabase()
        savedLocations = RoomSavedLocationStorage(database)
        forecastCache = RoomForecastCacheStorage(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun productionFactoryCreatesUsableSavedLocationStorageFromContext() {
        val context = targetContext()
        context.deleteDatabase("oxygen_forecast_cache.db")
        val storage = RoomSavedLocationStorageFactory.create(context)

        storage.saveLocation(chicago)

        assertEquals(listOf(chicago), storage.listLocations())
        context.deleteDatabase("oxygen_forecast_cache.db")
    }

    @Test
    fun addListAndRemoveSavedLocationsByProviderNeutralLocationId() {
        savedLocations.saveLocation(chicago)
        savedLocations.saveLocation(madison)

        assertEquals(listOf(chicago, madison), savedLocations.listLocations())

        savedLocations.removeLocation(chicago.id)

        assertEquals(listOf(madison), savedLocations.listLocations())
        savedLocations.removeLocation(LocationId("provider-row-that-does-not-exist"))
        assertEquals(listOf(madison), savedLocations.listLocations())
    }

    @Test
    fun duplicateSaveReplacesSameIdAndMovesItToNewestPosition() {
        val renamedChicago = chicago.copy(
            displayName = "Chicago Near North, Illinois",
            point = GeoPoint(41.9, -87.63),
            elevationMeters = 190.0,
        )

        savedLocations.saveLocation(chicago)
        savedLocations.saveLocation(madison)
        savedLocations.saveLocation(renamedChicago)

        assertEquals(listOf(madison, renamedChicago), savedLocations.listLocations())
    }

    @Test
    fun forecastCacheRowsDoNotAppearAsSavedLocations() {
        val bundle = fullBundle(chicago)

        forecastCache.replaceBundle(bundle)

        assertEquals(emptyList<WeatherLocation>(), savedLocations.listLocations())
        assertEquals(bundle, forecastCache.readBundle(chicago.id))
    }

    @Test
    fun savedLocationRowsDoNotSatisfyForecastCacheReads() {
        savedLocations.saveLocation(chicago)

        assertEquals(listOf(chicago), savedLocations.listLocations())
        assertNull(forecastCache.readBundle(chicago.id))
    }

    @Test
    fun versionOneForecastCacheDatabaseMigratesToSavedLocationStorageWithoutDataLoss() {
        val context = targetContext()
        val databaseName = "oxygen_migration_${System.nanoTime()}.db"
        context.deleteDatabase(databaseName)
        val databaseFile = context.getDatabasePath(databaseName)
        createVersionOneForecastCacheDatabase(databaseFile, fullBundle(chicago))

        val migratedDatabase = Room.databaseBuilder(
            context,
            OxygenForecastCacheDatabase::class.java,
            databaseName,
        ).addMigrations(OXYGEN_DATABASE_MIGRATION_1_2).build()

        try {
            val migratedForecastCache = RoomForecastCacheStorage(migratedDatabase)
            val migratedSavedLocations = RoomSavedLocationStorage(migratedDatabase)

            assertEquals(fullBundle(chicago), migratedForecastCache.readBundle(chicago.id))
            assertEquals(emptyList<WeatherLocation>(), migratedSavedLocations.listLocations())

            migratedSavedLocations.saveLocation(madison)
            assertEquals(listOf(madison), migratedSavedLocations.listLocations())
            assertTrue(savedLocationsTableExists(migratedDatabase.openHelper.writableDatabase))
        } finally {
            migratedDatabase.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun inMemoryDatabase(): OxygenForecastCacheDatabase =
        Room.inMemoryDatabaseBuilder(
            targetContext(),
            OxygenForecastCacheDatabase::class.java,
        ).build()

    private fun targetContext(): Context =
        InstrumentationRegistry.getInstrumentation().targetContext

    private fun createVersionOneForecastCacheDatabase(
        databaseFile: File,
        bundle: WeatherBundle,
    ) {
        databaseFile.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { db ->
            db.execSQL("PRAGMA foreign_keys=ON")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `cached_forecast_locations` (
                    `id` TEXT NOT NULL,
                    `displayName` TEXT NOT NULL,
                    `latitude` REAL NOT NULL,
                    `longitude` REAL NOT NULL,
                    `elevationMeters` REAL,
                    `zoneId` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `cached_forecast_metadata` (
                    `location_id` TEXT NOT NULL,
                    `bundleFetchedAt` TEXT NOT NULL,
                    PRIMARY KEY(`location_id`),
                    FOREIGN KEY(`location_id`) REFERENCES `cached_forecast_locations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `cached_current_conditions` (
                    `location_id` TEXT NOT NULL,
                    `time` TEXT NOT NULL,
                    `temperatureC` REAL,
                    `apparentTemperatureC` REAL,
                    `dewPointC` REAL,
                    `humidityPercent` INTEGER,
                    `pressureHpa` REAL,
                    `visibilityMeters` REAL,
                    `cloudCoverPercent` INTEGER,
                    `windSpeedMetersPerSecond` REAL,
                    `windGustMetersPerSecond` REAL,
                    `windDirectionDegrees` REAL,
                    `precipitationMm` REAL,
                    `condition` TEXT NOT NULL,
                    `provenanceProviderId` TEXT NOT NULL,
                    `provenanceSourceName` TEXT NOT NULL,
                    `provenanceIssuedAt` TEXT,
                    `provenanceFetchedAt` TEXT NOT NULL,
                    `provenanceType` TEXT NOT NULL,
                    `provenanceLicenseId` TEXT,
                    PRIMARY KEY(`location_id`),
                    FOREIGN KEY(`location_id`) REFERENCES `cached_forecast_locations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `cached_hourly_forecasts` (
                    `location_id` TEXT NOT NULL,
                    `row_index` INTEGER NOT NULL,
                    `time` TEXT NOT NULL,
                    `temperatureC` REAL,
                    `precipitationProbabilityPercent` INTEGER,
                    `precipitationMm` REAL,
                    `condition` TEXT NOT NULL,
                    `provenanceProviderId` TEXT NOT NULL,
                    `provenanceSourceName` TEXT NOT NULL,
                    `provenanceIssuedAt` TEXT,
                    `provenanceFetchedAt` TEXT NOT NULL,
                    `provenanceType` TEXT NOT NULL,
                    `provenanceLicenseId` TEXT,
                    PRIMARY KEY(`location_id`, `row_index`),
                    FOREIGN KEY(`location_id`) REFERENCES `cached_forecast_locations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `cached_daily_forecasts` (
                    `location_id` TEXT NOT NULL,
                    `row_index` INTEGER NOT NULL,
                    `dateEpochDay` INTEGER NOT NULL,
                    `highC` REAL,
                    `lowC` REAL,
                    `precipitationProbabilityPercent` INTEGER,
                    `condition` TEXT NOT NULL,
                    `sunrise` TEXT,
                    `sunset` TEXT,
                    `provenanceProviderId` TEXT NOT NULL,
                    `provenanceSourceName` TEXT NOT NULL,
                    `provenanceIssuedAt` TEXT,
                    `provenanceFetchedAt` TEXT NOT NULL,
                    `provenanceType` TEXT NOT NULL,
                    `provenanceLicenseId` TEXT,
                    PRIMARY KEY(`location_id`, `row_index`),
                    FOREIGN KEY(`location_id`) REFERENCES `cached_forecast_locations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            insertBundle(db, bundle)
            db.version = 1
        }
    }

    private fun insertBundle(db: SQLiteDatabase, bundle: WeatherBundle) {
        val location = bundle.location
        db.execSQL(
            """
            INSERT INTO cached_forecast_locations
            (id, displayName, latitude, longitude, elevationMeters, zoneId)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                location.id.value,
                location.displayName,
                location.point.latitude,
                location.point.longitude,
                location.elevationMeters,
                location.zoneId.id,
            ),
        )
        db.execSQL(
            "INSERT INTO cached_forecast_metadata (location_id, bundleFetchedAt) VALUES (?, ?)",
            arrayOf<Any?>(location.id.value, bundle.fetchedAt.toString()),
        )
        bundle.current?.let { current ->
            db.execSQL(
                """
                INSERT INTO cached_current_conditions
                (location_id, time, temperatureC, apparentTemperatureC, dewPointC, humidityPercent,
                    pressureHpa, visibilityMeters, cloudCoverPercent, windSpeedMetersPerSecond,
                    windGustMetersPerSecond, windDirectionDegrees, precipitationMm, condition,
                    provenanceProviderId, provenanceSourceName, provenanceIssuedAt,
                    provenanceFetchedAt, provenanceType, provenanceLicenseId)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any?>(
                    location.id.value,
                    current.time.toString(),
                    current.temperatureC,
                    current.apparentTemperatureC,
                    current.dewPointC,
                    current.humidityPercent,
                    current.pressureHpa,
                    current.visibilityMeters,
                    current.cloudCoverPercent,
                    current.wind?.speedMetersPerSecond,
                    current.wind?.gustMetersPerSecond,
                    current.wind?.directionDegrees,
                    current.precipitationMm,
                    current.condition.name,
                    current.provenance.providerId,
                    current.provenance.sourceName,
                    current.provenance.issuedAt?.toString(),
                    current.provenance.fetchedAt.toString(),
                    current.provenance.type.name,
                    current.provenance.licenseId,
                ),
            )
        }
        bundle.hourly.forEachIndexed { index, hourly ->
            db.execSQL(
                """
                INSERT INTO cached_hourly_forecasts
                (location_id, row_index, time, temperatureC, precipitationProbabilityPercent,
                    precipitationMm, condition, provenanceProviderId, provenanceSourceName,
                    provenanceIssuedAt, provenanceFetchedAt, provenanceType, provenanceLicenseId)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any?>(
                    location.id.value,
                    index,
                    hourly.time.toString(),
                    hourly.temperatureC,
                    hourly.precipitationProbabilityPercent,
                    hourly.precipitationMm,
                    hourly.condition.name,
                    hourly.provenance.providerId,
                    hourly.provenance.sourceName,
                    hourly.provenance.issuedAt?.toString(),
                    hourly.provenance.fetchedAt.toString(),
                    hourly.provenance.type.name,
                    hourly.provenance.licenseId,
                ),
            )
        }
        bundle.daily.forEachIndexed { index, daily ->
            db.execSQL(
                """
                INSERT INTO cached_daily_forecasts
                (location_id, row_index, dateEpochDay, highC, lowC,
                    precipitationProbabilityPercent, condition, sunrise, sunset,
                    provenanceProviderId, provenanceSourceName, provenanceIssuedAt,
                    provenanceFetchedAt, provenanceType, provenanceLicenseId)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any?>(
                    location.id.value,
                    index,
                    daily.dateEpochDay,
                    daily.highC,
                    daily.lowC,
                    daily.precipitationProbabilityPercent,
                    daily.condition.name,
                    daily.sunrise?.toString(),
                    daily.sunset?.toString(),
                    daily.provenance.providerId,
                    daily.provenance.sourceName,
                    daily.provenance.issuedAt?.toString(),
                    daily.provenance.fetchedAt.toString(),
                    daily.provenance.type.name,
                    daily.provenance.licenseId,
                ),
            )
        }
    }

    private fun savedLocationsTableExists(db: androidx.sqlite.db.SupportSQLiteDatabase): Boolean =
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'saved_locations'",
        ).use { cursor ->
            cursor.moveToFirst()
        }

    private val chicago = WeatherLocation(
        id = LocationId("manual-chicago"),
        displayName = "Chicago, Illinois",
        point = GeoPoint(41.875, -87.625),
        elevationMeters = 181.0,
        zoneId = ZoneId.of("America/Chicago"),
    )

    private val madison = WeatherLocation(
        id = LocationId("manual-madison"),
        displayName = "Madison, Wisconsin",
        point = GeoPoint(43.0747, -89.3844),
        zoneId = ZoneId.of("America/Chicago"),
    )

    private fun fullBundle(location: WeatherLocation): WeatherBundle {
        val provenance = DataProvenance(
            providerId = "open-meteo",
            sourceName = "Open-Meteo",
            issuedAt = Instant.parse("2026-08-26T10:15:00Z"),
            fetchedAt = Instant.parse("2026-08-26T10:20:00Z"),
            type = DataType.FORECAST,
            licenseId = "CC-BY-4.0",
        )
        return WeatherBundle(
            location = location,
            current = CurrentConditions(
                time = Instant.parse("2026-08-26T10:00:00Z"),
                temperatureC = 21.5,
                condition = WeatherCondition.PARTLY_CLOUDY,
                provenance = provenance,
            ),
            hourly = listOf(
                HourlyForecast(
                    time = Instant.parse("2026-08-26T11:00:00Z"),
                    temperatureC = 23.0,
                    precipitationProbabilityPercent = 30,
                    precipitationMm = 0.2,
                    condition = WeatherCondition.RAIN,
                    provenance = provenance,
                ),
            ),
            daily = listOf(
                DailyForecast(
                    dateEpochDay = 20691,
                    highC = 25.0,
                    lowC = 17.0,
                    precipitationProbabilityPercent = 55,
                    condition = WeatherCondition.RAIN_SHOWERS,
                    sunrise = Instant.parse("2026-08-26T11:10:00Z"),
                    sunset = Instant.parse("2026-08-27T00:31:00Z"),
                    provenance = provenance,
                ),
            ),
            fetchedAt = Instant.parse("2026-08-26T10:20:00Z"),
        )
    }
}
