package com.oxygen.weather.core.provider

import com.oxygen.weather.core.model.GeoPoint
import java.time.Instant
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertProviderContractTest {
    @Test
    fun suspendBoundaryCarriesProviderNeutralSuccessAndFailureValues() {
        val successMetadata = AlertSuccessMetadata(
            requestPoint = GeoPoint(41.875, -87.625),
            providerId = "test-provider",
            fetchedAt = Instant.parse("2026-09-05T18:05:00Z"),
            cacheControl = "public, max-age=5",
            expires = "Wed, 05 Sep 2026 18:10:00 GMT",
            etag = "\"abc123\"",
            lastModified = "Wed, 05 Sep 2026 18:00:00 GMT",
        )
        val successProvider = FixedAlertProvider(
            AlertProviderResult.Success(
                alerts = emptyList(),
                metadata = successMetadata,
            ),
        )
        val success = runSuspend { successProvider.getActiveAlerts(GeoPoint(41.875, -87.625)) }
        assertEquals(AlertProviderResult.Success(emptyList(), successMetadata), success)

        val failureProvider = FixedAlertProvider(
            AlertProviderResult.Failure(AlertProviderError.RateLimited("120")),
        )
        val failure = runSuspend { failureProvider.getActiveAlerts(GeoPoint(41.875, -87.625)) }
        assertEquals(AlertProviderResult.Failure(AlertProviderError.RateLimited("120")), failure)
    }
}

private class FixedAlertProvider(
    private val result: AlertProviderResult,
) : AlertProvider {
    override val id: String = "fixed"

    override suspend fun getActiveAlerts(location: GeoPoint): AlertProviderResult = result
}

private fun <T> runSuspend(block: suspend () -> T): T {
    var outcome: Result<T>? = null
    block.startCoroutine(object : kotlin.coroutines.Continuation<T> {
        override val context = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            outcome = result
        }
    })
    return requireNotNull(outcome) { "Suspending block did not complete synchronously" }.getOrThrow()
}
