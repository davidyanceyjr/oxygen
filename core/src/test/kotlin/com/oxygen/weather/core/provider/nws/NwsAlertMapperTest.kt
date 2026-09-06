package com.oxygen.weather.core.provider.nws

import com.oxygen.weather.core.model.AlertCertainty
import com.oxygen.weather.core.model.AlertGeometry
import com.oxygen.weather.core.model.AlertMessageType
import com.oxygen.weather.core.model.AlertSeverity
import com.oxygen.weather.core.model.AlertStatus
import com.oxygen.weather.core.model.AlertUrgency
import com.oxygen.weather.core.model.DataType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class NwsAlertMapperTest {
    private val fetchedAt = Instant.parse("2026-09-05T18:05:00Z")

    @Test
    fun mapsFullAlertFixtureToProviderNeutralDomain() {
        val alert = mappedFixture("alerts_active_one.json").single()

        assertEquals("urn:oid:2.49.0.1.840.0.full", alert.id)
        assertEquals("Flash Flood Warning", alert.event)
        assertEquals("Flash Flood Warning for Madison County", alert.headline)
        assertEquals(AlertSeverity.SEVERE, alert.severity)
        assertEquals(AlertUrgency.IMMEDIATE, alert.urgency)
        assertEquals(AlertCertainty.OBSERVED, alert.certainty)
        assertEquals(Instant.parse("2026-09-05T18:00:00Z"), alert.sent)
        assertEquals(Instant.parse("2026-09-05T18:02:00Z"), alert.effective)
        assertEquals(Instant.parse("2026-09-05T18:10:00Z"), alert.onset)
        assertEquals(Instant.parse("2026-09-05T20:00:00Z"), alert.expires)
        assertEquals(Instant.parse("2026-09-05T21:00:00Z"), alert.ends)
        assertEquals(AlertStatus.ACTUAL, alert.status)
        assertEquals(AlertMessageType.ALERT, alert.messageType)
        assertEquals("National Weather Service Milwaukee/Sullivan WI", alert.issuer)
        assertEquals("Avoid flooded roads.", alert.instruction)
        assertEquals("Meteorological", alert.category)
        assertEquals("Shelter", alert.response)
        assertEquals("Public", alert.scope)
        assertEquals("IPAWSv1.0", alert.code)
        assertEquals("en-US", alert.language)
        assertEquals("https://forecast.weather.gov/wwamap/wwatxtget.php?cwa=mkx&wwa=flash%20flood%20warning", alert.web)
        assertEquals(listOf("WIC025"), alert.affectedArea?.ugcCodes)
        assertEquals(listOf("055025"), alert.affectedArea?.sameCodes)
        assertEquals(listOf("https://api.weather.gov/zones/county/WIC025"), alert.affectedArea?.affectedZoneIds)
        assertEquals("Madison County", alert.affectedArea?.areaDescription)
        assertEquals("FFW", alert.eventCodes?.get("SAME")?.single())
        assertEquals(listOf("Flash Flood"), alert.parameters?.get("NWSheadline"))
        assertEquals("nws", alert.provenance.providerId)
        assertEquals("NOAA/National Weather Service", alert.provenance.sourceName)
        assertEquals(Instant.parse("2026-09-05T18:00:00Z"), alert.provenance.issuedAt)
        assertEquals(fetchedAt, alert.provenance.fetchedAt)
        assertEquals(DataType.OFFICIAL_ALERT, alert.provenance.type)
        assertNull(alert.provenance.licenseId)

        val polygon = alert.geometry as AlertGeometry.Polygon
        assertEquals(1, polygon.rings.size)
        assertEquals(-89.5, polygon.rings.single().first().longitude, 0.0)
        assertEquals(43.0, polygon.rings.single().first().latitude, 0.0)
    }

    @Test
    fun mapsNoAlertAndManyAlertFixturesWithoutDeduplication() {
        assertEquals(emptyList<Any>(), mappedFixture("alerts_active_none.json"))

        val many = mappedFixture("alerts_active_many.json")
        assertEquals(listOf("alert-one", "alert-two"), many.map { it.id })

        val duplicate = mappedFixture("alerts_active_duplicate_id.json")
        assertEquals(listOf("duplicate-alert", "duplicate-alert"), duplicate.map { it.id })
    }

    @Test
    fun keepsMissingOptionalFieldsNullOrEmpty() {
        val alert = mappedFixture("alerts_active_missing_optional.json").single()

        assertEquals(AlertSeverity.UNKNOWN, alert.severity)
        assertEquals(AlertUrgency.UNKNOWN, alert.urgency)
        assertEquals(AlertCertainty.UNKNOWN, alert.certainty)
        assertEquals(AlertStatus.UNKNOWN, alert.status)
        assertEquals(AlertMessageType.UNKNOWN, alert.messageType)
        assertNull(alert.effective)
        assertNull(alert.expires)
        assertNull(alert.sent)
        assertNull(alert.onset)
        assertNull(alert.ends)
        assertEquals(emptyList<Any>(), alert.references)
        assertNull(alert.affectedArea)
        assertNull(alert.geometry)
        assertNull(alert.eventCodes)
        assertNull(alert.parameters)
    }

    @Test
    fun mapsUnknownEnumsToUnknownStates() {
        val alert = mappedFixture("alerts_active_unknown_enums.json").single()

        assertEquals(AlertSeverity.UNKNOWN, alert.severity)
        assertEquals(AlertUrgency.UNKNOWN, alert.urgency)
        assertEquals(AlertCertainty.UNKNOWN, alert.certainty)
        assertEquals(AlertStatus.UNKNOWN, alert.status)
        assertEquals(AlertMessageType.UNKNOWN, alert.messageType)
    }

    @Test
    fun mapsPolygonAndNullGeometryFixtures() {
        assertEquals(
            AlertGeometry.Polygon::class.java,
            requireNotNull(mappedFixture("alerts_active_geometry_polygon.json").single().geometry).javaClass,
        )

        val nullGeometry = mappedFixture("alerts_active_null_geometry.json").single()
        assertNull(nullGeometry.geometry)
        assertEquals("Dane County", nullGeometry.affectedArea?.areaDescription)
        assertEquals(listOf("WIC025"), nullGeometry.affectedArea?.ugcCodes)
    }

    @Test
    fun failsDeterministicallyForMalformedGeometry() {
        val error = assertThrows(NwsAlertMapperException.InvalidField::class.java) {
            mappedFixture("alerts_active_invalid_geometry.json")
        }

        assertEquals("features[0].geometry.coordinates[0][0][0]", error.fieldPath)
    }

    @Test
    fun keepsUpdateCancelFutureAndExpiredLifecycleData() {
        val update = mappedFixture("alerts_active_update_references.json").single()
        assertEquals(AlertMessageType.UPDATE, update.messageType)
        assertEquals("previous-alert", update.references.single().id)
        assertEquals("w-nws.webmaster@noaa.gov", update.references.single().sender)
        assertEquals(Instant.parse("2026-09-05T17:30:00Z"), update.references.single().sent)

        val cancel = mappedFixture("alerts_active_cancel_references.json").single()
        assertEquals(AlertMessageType.CANCEL, cancel.messageType)
        assertEquals("cancelled-alert", cancel.references.single().id)

        val future = mappedFixture("alerts_active_near_future_effective.json").single()
        assertEquals(Instant.parse("2026-09-05T19:00:00Z"), future.effective)
        assertEquals(Instant.parse("2026-09-05T19:15:00Z"), future.onset)

        val expired = mappedFixture("alerts_active_expired_superseded_cached.json")
        assertEquals(AlertMessageType.UPDATE, expired[0].messageType)
        assertEquals(AlertMessageType.CANCEL, expired[1].messageType)
        assertEquals(Instant.parse("2026-09-05T12:00:00Z"), expired[0].expires)
    }

    @Test
    fun failsDeterministicallyForInvalidTimestampAndReferenceTimestamp() {
        val timestamp = assertThrows(NwsAlertMapperException.InvalidField::class.java) {
            mappedFixture("alerts_active_invalid_timestamp.json")
        }
        assertEquals("features[0].properties.sent", timestamp.fieldPath)

        val referenceTimestamp = assertThrows(NwsAlertMapperException.InvalidField::class.java) {
            mappedFixture("alerts_active_invalid_reference_timestamp.json")
        }
        assertEquals("features[0].properties.references[0].sent", referenceTimestamp.fieldPath)
    }

    private fun mappedFixture(name: String) =
        NwsAlertMapper.map(
            collection = NwsAlertParser.parseActiveAlerts(fixture(name)),
            fetchedAt = fetchedAt,
        )

    private fun fixture(name: String): String {
        val resource = requireNotNull(javaClass.classLoader?.getResource("providers/nws/$name")) {
            "Missing fixture $name"
        }
        return resource.readText()
    }
}
