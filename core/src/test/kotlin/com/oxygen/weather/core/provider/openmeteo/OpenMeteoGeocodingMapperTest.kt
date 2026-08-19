package com.oxygen.weather.core.provider.openmeteo

import com.oxygen.weather.core.model.GeoPoint
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class OpenMeteoGeocodingMapperTest {
    @Test
    fun mapsNormalResultToProviderNeutralLocationCandidate() {
        val candidate = mappedFixture("search_normal.json").single()

        assertEquals("Chicago, Illinois, United States", candidate.displayName)
        assertEquals(GeoPoint(41.85003, -87.65005), candidate.point)
        assertEquals(ZoneId.of("America/Chicago"), candidate.zoneId)
        assertEquals("United States", candidate.country)
        assertEquals("US", candidate.countryCode)
        assertEquals(listOf("Illinois", "Cook County", "Chicago"), candidate.administrativeAreas)
        assertEquals(179.0, requireNotNull(candidate.elevationMeters), 0.0)
        assertEquals(candidate.displayName, candidate.location.displayName)
        assertEquals(candidate.point, candidate.location.point)
        assertEquals(candidate.zoneId, candidate.location.zoneId)
        assertEquals(candidate.elevationMeters, candidate.location.elevationMeters)
        assertEquals(candidate.location.id, candidate.locationId)
    }

    @Test
    fun mapsEmptyResultsToEmptyCandidates() {
        assertEquals(emptyList<Any>(), mappedFixture("search_empty.json"))
    }

    @Test
    fun keepsAmbiguousPlacesDistinctWithoutProviderIds() {
        val candidates = mappedFixture("search_ambiguous.json")

        assertEquals(2, candidates.size)
        assertEquals("New York, New York, United States", candidates[0].displayName)
        assertEquals("New York, New Jersey, United States", candidates[1].displayName)
        assertNotEquals(candidates[0].locationId, candidates[1].locationId)
        assertNotEquals(candidates[0].point, candidates[1].point)
    }

    @Test
    fun mapsBoundedPostalCodePlaceQueryShape() {
        val candidate = mappedFixture("search_postal_bounded.json").single()

        assertEquals("Washington, District of Columbia, United States", candidate.displayName)
        assertEquals(listOf("20001"), candidate.postcodes)
    }

    @Test
    fun preservesMissingOptionalFields() {
        val candidate = mappedFixture("search_missing_optional.json").single()

        assertEquals("Tokyo, Japan", candidate.displayName)
        assertEquals(emptyList<String>(), candidate.administrativeAreas)
        assertEquals(emptyList<String>(), candidate.postcodes)
        assertNull(candidate.elevationMeters)
        assertNull(candidate.featureCode)
        assertNull(candidate.population)
    }

    @Test
    fun locationIdIgnoresProviderIdAndResultIndex() {
        val original = OpenMeteoGeocodingResult(
            id = 1,
            name = "Springfield",
            latitude = 39.78172,
            longitude = -89.65015,
            timezone = "America/Chicago",
            country = "United States",
            countryCode = "US",
            admin1 = "Illinois",
        )
        val changedProviderId = original.copy(id = 999)

        val originalId = OpenMeteoGeocodingMapper.map(OpenMeteoGeocodingResponse(listOf(original))).single().locationId
        val changedProviderIdId = OpenMeteoGeocodingMapper.map(
            OpenMeteoGeocodingResponse(listOf(changedProviderId, original)),
        ).first().locationId

        assertEquals(originalId, changedProviderIdId)
    }

    @Test
    fun locationIdUsesDocumentedRoundedCoordinates() {
        val original = OpenMeteoGeocodingResult(
            id = 1,
            name = "Chicago",
            latitude = 41.850031,
            longitude = -87.650049,
            timezone = "America/Chicago",
            country = "United States",
            countryCode = "US",
            admin1 = "Illinois",
        )
        val sameRoundedPoint = original.copy(latitude = 41.850034, longitude = -87.650046)
        val differentRoundedPoint = original.copy(latitude = 41.85008)

        val originalId = OpenMeteoGeocodingMapper.map(OpenMeteoGeocodingResponse(listOf(original))).single().locationId
        val sameRoundedId = OpenMeteoGeocodingMapper.map(OpenMeteoGeocodingResponse(listOf(sameRoundedPoint))).single().locationId
        val differentRoundedId = OpenMeteoGeocodingMapper.map(
            OpenMeteoGeocodingResponse(listOf(differentRoundedPoint)),
        ).single().locationId

        assertEquals(originalId, sameRoundedId)
        assertNotEquals(originalId, differentRoundedId)
    }

    @Test
    fun reportsInvalidCoordinate() {
        val error = assertThrows(OpenMeteoGeocodingException.InvalidCoordinate::class.java) {
            mappedFixture("search_invalid_coordinate.json")
        }

        assertEquals("results[0].latitude", error.fieldPath)
    }

    @Test
    fun reportsInvalidTimezone() {
        val error = assertThrows(OpenMeteoGeocodingException.InvalidTimezone::class.java) {
            mappedFixture("search_invalid_timezone.json")
        }

        assertEquals("results[0].timezone", error.fieldPath)
    }

    private fun mappedFixture(name: String) =
        OpenMeteoGeocodingMapper.map(OpenMeteoGeocodingParser.parseSearch(fixture(name)))

    private fun fixture(name: String): String {
        val resource = requireNotNull(javaClass.classLoader?.getResource("providers/openmeteo/geocoding/$name")) {
            "Missing fixture $name"
        }
        return resource.readText()
    }
}
