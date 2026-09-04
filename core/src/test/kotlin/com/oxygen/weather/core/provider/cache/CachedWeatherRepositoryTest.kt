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
import com.oxygen.weather.core.provider.ForecastError
import com.oxygen.weather.core.provider.ForecastFreshness
import com.oxygen.weather.core.provider.WeatherRepository
import com.oxygen.weather.core.provider.WeatherRepositoryResult
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CachedWeatherRepositoryTest {
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

    @Test
    fun providerSuccessIsWrittenThenEmittedFromStorageReadback() {
        val providerBundle = bundle(
            location = chicago,
            providerId = "open-meteo",
            temperatureC = 21.5,
        )
        val storedBundle = providerBundle.copy(
            current = requireNotNull(providerBundle.current).copy(temperatureC = 22.0),
        )
        val storage = RecordingForecastCacheStorage(storedReads = mapOf(chicago.id to storedBundle))

        val success = CachedWeatherRepository(
            upstream = FixedWeatherRepository(
                WeatherRepositoryResult.Loading,
                WeatherRepositoryResult.Success(providerBundle),
            ),
            storage = storage,
        ).refresh(chicago).terminalSuccess()

        assertEquals(listOf(providerBundle), storage.replacements)
        assertEquals(listOf(chicago.id), storage.reads)
        assertNotSame(providerBundle, success.weather)
        assertSame(storedBundle, success.weather)
    }

    @Test
    fun preservesOpenMeteoAndMetNorwayProvenanceAcrossReadback() {
        listOf(
            "open-meteo" to "Open-Meteo Forecast API",
            "met-norway" to "MET Norway Locationforecast",
        ).forEach { (providerId, sourceName) ->
            val providerBundle = bundle(chicago, providerId, sourceName = sourceName)
            val storedBundle = providerBundle.copy()
            val success = CachedWeatherRepository(
                upstream = FixedWeatherRepository(WeatherRepositoryResult.Success(providerBundle)),
                storage = RecordingForecastCacheStorage(storedReads = mapOf(chicago.id to storedBundle)),
            ).refresh(chicago).terminalSuccess()

            val provenance = requireNotNull(success.weather.current).provenance
            assertEquals(providerId, provenance.providerId)
            assertEquals(sourceName, provenance.sourceName)
            assertEquals(Instant.parse("2026-08-26T10:15:00Z"), provenance.issuedAt)
            assertEquals(Instant.parse("2026-08-26T10:20:00Z"), provenance.fetchedAt)
            assertEquals(DataType.FORECAST, provenance.type)
            assertEquals("CC-BY-4.0", provenance.licenseId)
        }
    }

    @Test
    fun providerSuccessWritesCacheMetadataWhenStorageSupportsIt() {
        val providerBundle = bundle(chicago, "met-norway", sourceName = "MET Norway")
        val cacheMetadata = ForecastCacheMetadata(
            providerId = "met-norway",
            expires = "Sun, 23 Aug 2026 15:00:00 GMT",
            lastModified = "Sun, 23 Aug 2026 14:00:00 GMT",
            etag = "\"metno-forecast\"",
            fetchedAt = Instant.parse("2026-08-26T10:20:00Z"),
            responseLatitude = 41.875,
            responseLongitude = -87.625,
            responseElevationMeters = 181.0,
            providerUpdatedAt = Instant.parse("2026-08-26T10:15:00Z"),
        )
        val storage = RecordingMetadataForecastCacheStorage(storedReads = mapOf(chicago.id to providerBundle))

        val success = CachedWeatherRepository(
            upstream = FixedWeatherRepository(
                WeatherRepositoryResult.Success(providerBundle, cacheMetadata = cacheMetadata),
            ),
            storage = storage,
        ).refresh(chicago).terminalSuccess()

        assertEquals(providerBundle, success.weather)
        assertEquals(listOf(providerBundle to cacheMetadata), storage.metadataReplacements)
        assertEquals(cacheMetadata, storage.readCacheMetadata(chicago.id))
    }

    @Test
    fun locationIdScopesReadback() {
        val chicagoBundle = bundle(chicago, "open-meteo", temperatureC = 22.0)
        val madisonBundle = bundle(madison, "open-meteo", temperatureC = 18.0)
        val storage = RecordingForecastCacheStorage(
            storedReads = mapOf(
                chicago.id to chicagoBundle,
                madison.id to madisonBundle,
            ),
        )

        val success = CachedWeatherRepository(
            upstream = FixedWeatherRepository(WeatherRepositoryResult.Success(chicagoBundle)),
            storage = storage,
        ).refresh(chicago).terminalSuccess()

        assertEquals(chicago.id, success.weather.location.id)
        assertEquals(22.0, requireNotNull(success.weather.current).temperatureC)
        assertEquals(listOf(chicago.id), storage.reads)
    }

    @Test
    fun providerFailureKeepsExistingNoCacheFailurePath() {
        val providerFailure = WeatherRepositoryResult.Failure(ForecastError.NetworkUnavailable)
        val storage = RecordingForecastCacheStorage(storedReads = emptyMap())

        val failure = CachedWeatherRepository(
            upstream = FixedWeatherRepository(WeatherRepositoryResult.Loading, providerFailure),
            storage = storage,
        ).refresh(chicago).terminalFailure()

        assertSame(providerFailure.error, failure.error)
        assertEquals(emptyList<WeatherBundle>(), storage.replacements)
        assertEquals(listOf(chicago.id), storage.reads)
    }

    @Test
    fun failedRefreshWithSameLocationCacheEmitsCachedStaleSuccess() {
        val cachedBundle = bundle(
            location = chicago,
            providerId = "open-meteo",
            sourceName = "Open-Meteo Forecast API",
            temperatureC = 19.0,
        )
        val failure = ForecastError.NetworkUnavailable
        val success = CachedWeatherRepository(
            upstream = FixedWeatherRepository(
                WeatherRepositoryResult.Loading,
                WeatherRepositoryResult.Failure(failure),
            ),
            storage = RecordingForecastCacheStorage(storedReads = mapOf(chicago.id to cachedBundle)),
            clock = fixedClock("2026-08-26T11:05:00Z"),
        ).refresh(chicago).terminalSuccess()

        assertSame(cachedBundle, success.weather)
        assertEquals(chicago, success.weather.location)
        assertEquals(chicago.id, success.weather.location.id)
        assertEquals(19.0, requireNotNull(success.weather.current).temperatureC)
        assertEquals(cachedBundle.hourly, success.weather.hourly)
        assertEquals(cachedBundle.daily, success.weather.daily)
        assertEquals(Instant.parse("2026-08-26T10:20:00Z"), success.weather.fetchedAt)
        assertEquals("open-meteo", requireNotNull(success.weather.current).provenance.providerId)
        assertEquals("Open-Meteo Forecast API", requireNotNull(success.weather.current).provenance.sourceName)

        val freshness = success.freshness as ForecastFreshness.StaleAfterFailedRefresh
        assertEquals(Duration.ofMinutes(45), freshness.staleAge)
        assertSame(failure, freshness.refreshFailure)
    }

    @Test
    fun successThenLaterFailedRefreshRetainsCachedForecastForSameLocation() {
        val providerBundle = bundle(chicago, "open-meteo", sourceName = "Open-Meteo Forecast API", temperatureC = 17.0)
        val storage = MutableForecastCacheStorage()
        val repository = CachedWeatherRepository(
            upstream = PerCallWeatherRepository(
                listOf(WeatherRepositoryResult.Success(providerBundle)),
                listOf(WeatherRepositoryResult.Loading, WeatherRepositoryResult.Failure(ForecastError.NetworkUnavailable)),
            ),
            storage = storage,
            clock = fixedClock("2026-08-26T11:05:00Z"),
        )

        val firstSuccess = repository.refresh(chicago).terminalSuccess()
        val retainedSuccess = repository.refresh(chicago).terminalSuccess()

        assertSame(providerBundle, firstSuccess.weather)
        assertEquals(providerBundle, retainedSuccess.weather)
        assertEquals(17.0, requireNotNull(retainedSuccess.weather.current).temperatureC)
        assertEquals("Open-Meteo Forecast API", requireNotNull(retainedSuccess.weather.current).provenance.sourceName)
        val freshness = retainedSuccess.freshness as ForecastFreshness.StaleAfterFailedRefresh
        assertEquals(Duration.ofMinutes(45), freshness.staleAge)
        assertSame(ForecastError.NetworkUnavailable, freshness.refreshFailure)
    }

    @Test
    fun failedRefreshRetainsMetNorwayCachedForecastWithTruthfulProvenance() {
        val cachedBundle = bundle(
            location = chicago,
            providerId = "met-norway",
            sourceName = "MET Norway",
            temperatureC = 13.0,
        )
        val failure = ForecastError.ProviderUnavailable("open-meteo")

        val success = CachedWeatherRepository(
            upstream = FixedWeatherRepository(WeatherRepositoryResult.Failure(failure)),
            storage = RecordingForecastCacheStorage(storedReads = mapOf(chicago.id to cachedBundle)),
            clock = fixedClock("2026-08-26T11:05:00Z"),
        ).refresh(chicago).terminalSuccess()

        val provenance = requireNotNull(success.weather.current).provenance
        assertEquals("met-norway", provenance.providerId)
        assertEquals("MET Norway", provenance.sourceName)
        assertEquals("CC-BY-4.0", provenance.licenseId)
        val freshness = success.freshness as ForecastFreshness.StaleAfterFailedRefresh
        assertEquals(Duration.ofMinutes(45), freshness.staleAge)
        assertSame(failure, freshness.refreshFailure)
    }

    @Test
    fun openMeteoRefreshReplacesCachedMetNorwayForecastThroughCacheStorage() {
        val metNorwayBundle = bundle(chicago, "met-norway", sourceName = "MET Norway", temperatureC = 13.0)
        val openMeteoBundle = bundle(chicago, "open-meteo", sourceName = "Open-Meteo", temperatureC = 21.0)
        val storage = MutableForecastCacheStorage().apply {
            replaceBundle(metNorwayBundle)
        }

        val success = CachedWeatherRepository(
            upstream = FixedWeatherRepository(WeatherRepositoryResult.Success(openMeteoBundle)),
            storage = storage,
        ).refresh(chicago).terminalSuccess()

        assertEquals(openMeteoBundle, success.weather)
        assertEquals(openMeteoBundle, storage.readBundle(chicago.id))
        assertEquals("open-meteo", requireNotNull(success.weather.current).provenance.providerId)
    }

    @Test
    fun failedRefreshDoesNotReadAnotherLocationsCache() {
        val madisonBundle = bundle(madison, "open-meteo", temperatureC = 12.0)
        val failure = CachedWeatherRepository(
            upstream = FixedWeatherRepository(
                WeatherRepositoryResult.Failure(ForecastError.ProviderUnavailable("open-meteo")),
            ),
            storage = RecordingForecastCacheStorage(storedReads = mapOf(madison.id to madisonBundle)),
            clock = fixedClock("2026-08-26T11:05:00Z"),
        ).refresh(chicago).terminalFailure()

        assertEquals(ForecastError.ProviderUnavailable("open-meteo"), failure.error)
    }

    @Test
    fun failedRefreshCacheReadFailureEmitsLocalCacheFailure() {
        val failure = CachedWeatherRepository(
            upstream = FixedWeatherRepository(
                WeatherRepositoryResult.Failure(ForecastError.InvalidResponse("open-meteo")),
            ),
            storage = RecordingForecastCacheStorage(
                storedReads = mapOf(chicago.id to bundle(chicago, "open-meteo")),
                readFailure = IllegalStateException("read failed"),
            ),
            clock = fixedClock("2026-08-26T11:05:00Z"),
        ).refresh(chicago).terminalFailure()

        assertSame(ForecastError.LocalCacheFailure, failure.error)
    }

    @Test
    fun failedRefreshCacheReadIoFailureEmitsLocalCacheFailure() {
        val failure = CachedWeatherRepository(
            upstream = FixedWeatherRepository(
                WeatherRepositoryResult.Failure(ForecastError.NetworkUnavailable),
            ),
            storage = RecordingForecastCacheStorage(
                storedReads = mapOf(chicago.id to bundle(chicago, "open-meteo")),
                readFailure = IOException("stream failed"),
            ),
            clock = fixedClock("2026-08-26T11:05:00Z"),
        ).refresh(chicago).terminalFailure()

        assertSame(ForecastError.LocalCacheFailure, failure.error)
    }

    @Test
    fun providerRejectedRequestDoesNotRetainCachedForecast() {
        val rejected = ForecastError.ProviderRejectedRequest("open-meteo")
        val failure = CachedWeatherRepository(
            upstream = FixedWeatherRepository(WeatherRepositoryResult.Failure(rejected)),
            storage = RecordingForecastCacheStorage(storedReads = mapOf(chicago.id to bundle(chicago, "open-meteo"))),
            clock = fixedClock("2026-08-26T11:05:00Z"),
        ).refresh(chicago).terminalFailure()

        assertSame(rejected, failure.error)
    }

    @Test
    fun cacheWriteFailureKeepsProviderSuccessVisible() {
        val providerBundle = bundle(chicago, "open-meteo", temperatureC = 27.0)
        val storage = RecordingForecastCacheStorage(
            storedReads = emptyMap(),
            replaceFailure = IllegalStateException("disk full"),
        )

        val success = CachedWeatherRepository(
            upstream = FixedWeatherRepository(WeatherRepositoryResult.Success(providerBundle)),
            storage = storage,
        ).refresh(chicago).terminalSuccess()

        assertSame(providerBundle, success.weather)
        assertEquals(emptyList<WeatherBundle>(), storage.replacements)
        assertEquals(emptyList<LocationId>(), storage.reads)
    }

    @Test
    fun cacheWriteIoFailureKeepsProviderSuccessVisible() {
        val providerBundle = bundle(chicago, "open-meteo", temperatureC = 28.0)
        val storage = RecordingForecastCacheStorage(
            storedReads = mapOf(chicago.id to bundle(chicago, "open-meteo")),
            replaceFailure = IOException("disk failed"),
        )

        val success = CachedWeatherRepository(
            upstream = FixedWeatherRepository(WeatherRepositoryResult.Success(providerBundle)),
            storage = storage,
        ).refresh(chicago).terminalSuccess()

        assertSame(providerBundle, success.weather)
        assertEquals(emptyList<WeatherBundle>(), storage.replacements)
        assertEquals(emptyList<LocationId>(), storage.reads)
    }

    @Test
    fun cacheReadbackIoFailureAfterWriteKeepsProviderSuccessVisible() {
        val providerBundle = bundle(chicago, "open-meteo", temperatureC = 29.0)
        val storage = RecordingForecastCacheStorage(
            storedReads = mapOf(chicago.id to bundle(chicago, "open-meteo")),
            readFailure = IOException("readback failed"),
        )

        val success = CachedWeatherRepository(
            upstream = FixedWeatherRepository(WeatherRepositoryResult.Success(providerBundle)),
            storage = storage,
        ).refresh(chicago).terminalSuccess()

        assertSame(providerBundle, success.weather)
        assertEquals(listOf(providerBundle), storage.replacements)
        assertEquals(emptyList<LocationId>(), storage.reads)
    }

    @Test
    fun missingReadbackAfterWriteKeepsProviderSuccessVisible() {
        val providerBundle = bundle(chicago, "open-meteo", temperatureC = 30.0)

        val success = CachedWeatherRepository(
            upstream = FixedWeatherRepository(WeatherRepositoryResult.Success(providerBundle)),
            storage = RecordingForecastCacheStorage(storedReads = emptyMap()),
        ).refresh(chicago).terminalSuccess()

        assertSame(providerBundle, success.weather)
    }

    private fun bundle(
        location: WeatherLocation,
        providerId: String,
        sourceName: String = "Forecast source",
        temperatureC: Double? = 21.5,
    ): WeatherBundle {
        val provenance = DataProvenance(
            providerId = providerId,
            sourceName = sourceName,
            issuedAt = Instant.parse("2026-08-26T10:15:00Z"),
            fetchedAt = Instant.parse("2026-08-26T10:20:00Z"),
            type = DataType.FORECAST,
            licenseId = "CC-BY-4.0",
        )
        return WeatherBundle(
            location = location,
            current = CurrentConditions(
                time = Instant.parse("2026-08-26T10:00:00Z"),
                temperatureC = temperatureC,
                apparentTemperatureC = null,
                dewPointC = 12.0,
                humidityPercent = null,
                pressureHpa = 1012.5,
                visibilityMeters = null,
                cloudCoverPercent = 64,
                wind = null,
                precipitationMm = null,
                condition = WeatherCondition.PARTLY_CLOUDY,
                provenance = provenance,
            ),
            hourly = listOf(
                HourlyForecast(
                    time = Instant.parse("2026-08-26T11:00:00Z"),
                    temperatureC = null,
                    precipitationProbabilityPercent = null,
                    precipitationMm = 0.2,
                    condition = WeatherCondition.RAIN,
                    provenance = provenance,
                ),
            ),
            daily = listOf(
                DailyForecast(
                    dateEpochDay = 20691,
                    highC = 24.0,
                    lowC = null,
                    precipitationProbabilityPercent = null,
                    condition = WeatherCondition.RAIN_SHOWERS,
                    sunrise = null,
                    sunset = Instant.parse("2026-08-27T00:31:00Z"),
                    provenance = provenance,
                ),
            ),
            fetchedAt = Instant.parse("2026-08-26T10:20:00Z"),
        )
    }

    private fun fixedClock(instant: String): Clock =
        Clock.fixed(Instant.parse(instant), ZoneOffset.UTC)
}

private class FixedWeatherRepository(
    private vararg val results: WeatherRepositoryResult,
) : WeatherRepository {
    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> = results.asSequence()
}

private class PerCallWeatherRepository(
    private vararg val responses: List<WeatherRepositoryResult>,
) : WeatherRepository {
    private var callIndex = 0

    override fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult> {
        val response = responses.getOrElse(callIndex) { responses.last() }
        callIndex += 1
        return response.asSequence()
    }
}

private class MutableForecastCacheStorage : ForecastCacheStorage {
    private val bundles = mutableMapOf<LocationId, WeatherBundle>()

    override fun replaceBundle(bundle: WeatherBundle) {
        bundles[bundle.location.id] = bundle
    }

    override fun readBundle(locationId: LocationId): WeatherBundle? = bundles[locationId]
}

private class RecordingForecastCacheStorage(
    private val storedReads: Map<LocationId, WeatherBundle>,
    private val replaceFailure: Exception? = null,
    private val readFailure: Exception? = null,
) : ForecastCacheStorage {
    val replacements = mutableListOf<WeatherBundle>()
    val reads = mutableListOf<LocationId>()

    override fun replaceBundle(bundle: WeatherBundle) {
        replaceFailure?.let { throw it }
        replacements += bundle
    }

    override fun readBundle(locationId: LocationId): WeatherBundle? {
        readFailure?.let { throw it }
        reads += locationId
        return storedReads[locationId]
    }
}

private class RecordingMetadataForecastCacheStorage(
    private val storedReads: Map<LocationId, WeatherBundle>,
) : ForecastCacheMetadataStorage {
    private val metadata = mutableMapOf<LocationId, ForecastCacheMetadata>()
    val metadataReplacements = mutableListOf<Pair<WeatherBundle, ForecastCacheMetadata>>()

    override fun replaceBundle(bundle: WeatherBundle) {
        error("Expected metadata-aware replacement")
    }

    override fun replaceBundle(
        bundle: WeatherBundle,
        cacheMetadata: ForecastCacheMetadata,
    ) {
        metadataReplacements += bundle to cacheMetadata
        metadata[bundle.location.id] = cacheMetadata
    }

    override fun readBundle(locationId: LocationId): WeatherBundle? = storedReads[locationId]

    override fun readCacheMetadata(locationId: LocationId): ForecastCacheMetadata? = metadata[locationId]
}

private fun Sequence<WeatherRepositoryResult>.terminalSuccess(): WeatherRepositoryResult.Success =
    first { it !is WeatherRepositoryResult.Loading } as WeatherRepositoryResult.Success

private fun Sequence<WeatherRepositoryResult>.terminalFailure(): WeatherRepositoryResult.Failure =
    first { it !is WeatherRepositoryResult.Loading } as WeatherRepositoryResult.Failure
