package com.oxygen.weather.core.provider

import com.oxygen.weather.core.model.AirQuality
import com.oxygen.weather.core.model.GeoPoint
import com.oxygen.weather.core.model.GeocodingLocationCandidate
import com.oxygen.weather.core.model.WeatherAlert
import com.oxygen.weather.core.model.WeatherBundle
import com.oxygen.weather.core.model.WeatherLocation
import com.oxygen.weather.core.provider.cache.ForecastCacheMetadata
import java.time.Duration
import java.time.ZoneId
import java.time.Instant

fun interface CoordinateTimeZoneResolver {
    fun resolve(point: GeoPoint): CoordinateTimeZoneResult
}

sealed interface CoordinateTimeZoneResult {
    data class Success(val point: GeoPoint, val zoneId: ZoneId) : CoordinateTimeZoneResult
    data class Failure(val error: CoordinateTimeZoneError) : CoordinateTimeZoneResult
}

enum class CoordinateTimeZoneError {
    InvalidPoint, NetworkUnavailable, RateLimited, ProviderUnavailable, RequestRejected, InvalidResponse,
}

interface ForecastProvider {
    val id: String
    suspend fun getWeather(location: WeatherLocation): WeatherBundle
}

interface AlertProvider {
    val id: String
    fun getActiveAlerts(location: GeoPoint): AlertProviderResult
}

data class AlertSuccessMetadata(
    val requestPoint: GeoPoint,
    val providerId: String,
    val fetchedAt: Instant,
    val cacheControl: String? = null,
    val expires: String? = null,
    val etag: String? = null,
    val lastModified: String? = null,
)

sealed interface AlertProviderResult {
    data class Success(
        val alerts: List<WeatherAlert>,
        val metadata: AlertSuccessMetadata,
    ) : AlertProviderResult

    data class Failure(val error: AlertProviderError) : AlertProviderResult
}

sealed interface AlertProviderError {
    data object InvalidPoint : AlertProviderError
    data object InvalidRequest : AlertProviderError
    data object UnsupportedRegion : AlertProviderError
    data object IdentificationRejected : AlertProviderError
    data object Network : AlertProviderError
    data class RateLimited(val retryAfter: String? = null) : AlertProviderError
    data object ProviderUnavailable : AlertProviderError
    data object InvalidResponse : AlertProviderError
    data object UnexpectedProvider : AlertProviderError
}

sealed interface AlertLookupStatus {
    data object NotRequested : AlertLookupStatus
    data object NoAlerts : AlertLookupStatus
    data object Available : AlertLookupStatus
    data object UnsupportedRegion : AlertLookupStatus
    data class Failed(val error: AlertProviderError) : AlertLookupStatus
}

interface AirQualityProvider {
    val id: String
    suspend fun getAirQuality(location: GeoPoint): AirQuality?
}

interface GeocodingProvider {
    val id: String
    suspend fun search(query: String): List<WeatherLocation>
}

interface RadarProvider {
    val id: String
    fun supports(location: GeoPoint): Boolean
}

sealed class ForecastError {
    data object NetworkUnavailable : ForecastError()

    data class RateLimited(
        val providerId: String,
    ) : ForecastError()

    data class ProviderUnavailable(
        val providerId: String,
    ) : ForecastError()

    data class InvalidResponse(
        val providerId: String,
    ) : ForecastError()

    data class ProviderRejectedRequest(
        val providerId: String,
    ) : ForecastError()

    data class UnexpectedProviderFailure(
        val providerId: String,
    ) : ForecastError()

    data object LocalCacheFailure : ForecastError()
}

sealed class WeatherRepositoryResult {
    data object Loading : WeatherRepositoryResult()

    data class Success(
        val weather: WeatherBundle,
        val freshness: ForecastFreshness = ForecastFreshness.Fresh,
        val cacheMetadata: ForecastCacheMetadata? = null,
        val alertStatus: AlertLookupStatus = AlertLookupStatus.NotRequested,
    ) : WeatherRepositoryResult()

    data class Failure(
        val error: ForecastError,
        val diagnostics: List<ForecastError> = listOf(error),
    ) : WeatherRepositoryResult()
}

interface WeatherRepository {
    fun refresh(location: WeatherLocation): Sequence<WeatherRepositoryResult>
}

sealed class ForecastFreshness {
    data object Fresh : ForecastFreshness()

    data class StaleAfterFailedRefresh(
        val staleAge: Duration,
        val refreshFailure: ForecastError,
    ) : ForecastFreshness()
}

sealed class GeocodingError {
    data object InvalidQuery : GeocodingError()
    data object NetworkUnavailable : GeocodingError()

    data class RateLimited(
        val providerId: String,
    ) : GeocodingError()

    data class ProviderUnavailable(
        val providerId: String,
    ) : GeocodingError()

    data class InvalidResponse(
        val providerId: String,
    ) : GeocodingError()

    data class ProviderRejectedRequest(
        val providerId: String,
    ) : GeocodingError()

    data class UnexpectedProviderFailure(
        val providerId: String,
    ) : GeocodingError()
}

sealed class GeocodingRepositoryResult {
    data object Loading : GeocodingRepositoryResult()
    data object Empty : GeocodingRepositoryResult()

    data class Success(
        val candidates: List<GeocodingLocationCandidate>,
    ) : GeocodingRepositoryResult()

    data class Failure(
        val error: GeocodingError,
    ) : GeocodingRepositoryResult()
}

interface GeocodingRepository {
    fun search(
        query: String,
        count: Int = 10,
        language: String? = null,
        countryCode: String? = null,
    ): Sequence<GeocodingRepositoryResult>
}
