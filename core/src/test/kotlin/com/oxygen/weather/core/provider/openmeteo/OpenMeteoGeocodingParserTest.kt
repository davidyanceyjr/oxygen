package com.oxygen.weather.core.provider.openmeteo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class OpenMeteoGeocodingParserTest {
    @Test
    fun parsesNormalGeocodingFixture() {
        val response = OpenMeteoGeocodingParser.parseSearch(fixture("search_normal.json"))

        assertEquals(1, response.results.size)
        val result = response.results.single()
        assertEquals(4887398, result.id)
        assertEquals("Chicago", result.name)
        assertEquals(41.85003, result.latitude, 0.0)
        assertEquals(-87.65005, result.longitude, 0.0)
        assertEquals("America/Chicago", result.timezone)
        assertEquals("United States", result.country)
        assertEquals("US", result.countryCode)
        assertEquals("Illinois", result.admin1)
        assertEquals("Cook County", result.admin2)
        assertEquals("Chicago", result.admin3)
        assertEquals(179.0, requireNotNull(result.elevation), 0.0)
        assertEquals(listOf("60601", "60602"), result.postcodes)
    }

    @Test
    fun parsesEmptyResults() {
        val response = OpenMeteoGeocodingParser.parseSearch(fixture("search_empty.json"))

        assertEquals(emptyList<OpenMeteoGeocodingResult>(), response.results)
    }

    @Test
    fun parsesProviderEmptyBodyWithoutResultsAsEmptyResults() {
        val response = OpenMeteoGeocodingParser.parseSearch(fixture("search_malformed_envelope.json"))

        assertEquals(emptyList<OpenMeteoGeocodingResult>(), response.results)
    }

    @Test
    fun preservesMissingOptionalFieldsAsNullOrEmpty() {
        val result = OpenMeteoGeocodingParser.parseSearch(fixture("search_missing_optional.json")).results.single()

        assertNull(result.elevation)
        assertNull(result.admin1)
        assertNull(result.admin2)
        assertNull(result.admin3)
        assertNull(result.admin4)
        assertEquals(emptyList<String>(), result.postcodes)
    }

    @Test
    fun reportsMalformedEnvelopeAsInvalidJsonEnvelope() {
        val error = assertThrows(OpenMeteoGeocodingException.InvalidJsonEnvelope::class.java) {
            OpenMeteoGeocodingParser.parseSearch("""{"results":{}}""")
        }

        assertEquals("results", error.fieldPath)
    }

    @Test
    fun reportsProviderErrorBody() {
        val error = assertThrows(OpenMeteoGeocodingException.ProviderErrorBody::class.java) {
            OpenMeteoGeocodingParser.parseSearch(fixture("search_provider_error.json"))
        }

        assertEquals("Parameter 'name' is invalid", error.reason)
    }

    @Test
    fun reportsMissingRequiredResultField() {
        val error = assertThrows(OpenMeteoGeocodingException.MissingRequiredField::class.java) {
            OpenMeteoGeocodingParser.parseSearch("""{"results":[{"name":"Chicago"}]}""")
        }

        assertEquals("results[0].latitude", error.fieldPath)
    }

    @Test
    fun reportsInvalidResultFieldValue() {
        val error = assertThrows(OpenMeteoGeocodingException.InvalidFieldValue::class.java) {
            OpenMeteoGeocodingParser.parseSearch("""{"results":[{"name":"Chicago","latitude":"north"}]}""")
        }

        assertEquals("results[0].latitude", error.fieldPath)
    }

    private fun fixture(name: String): String {
        val resource = requireNotNull(javaClass.classLoader?.getResource("providers/openmeteo/geocoding/$name")) {
            "Missing fixture $name"
        }
        return resource.readText()
    }
}
