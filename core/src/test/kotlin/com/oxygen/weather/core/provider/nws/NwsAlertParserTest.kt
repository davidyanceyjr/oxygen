package com.oxygen.weather.core.provider.nws

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class NwsAlertParserTest {
    @Test
    fun parsesEmptyActiveFeatureCollection() {
        val collection = parsedFixture("alerts_active_none.json")

        assertEquals("FeatureCollection", collection.type)
        assertEquals(0, collection.features.size)
    }

    @Test
    fun parsesOneAlertFixtureAndRetainsProviderFields() {
        val alert = parsedFixture("alerts_active_one.json").features.single()

        assertEquals("https://api.weather.gov/alerts/urn:oid:2.49.0.1.840.0.full", alert.id)
        assertEquals("urn:oid:2.49.0.1.840.0.full", alert.properties.id)
        assertEquals("Flash Flood Warning", alert.properties.event)
        assertEquals("Severe", alert.properties.severity)
        assertEquals("Immediate", alert.properties.urgency)
        assertEquals("Observed", alert.properties.certainty)
        assertEquals("2026-09-05T18:00:00+00:00", alert.properties.sent)
        assertEquals("Actual", alert.properties.status)
        assertEquals("Alert", alert.properties.messageType)
        assertEquals("Madison County", alert.properties.areaDesc)
        assertEquals(listOf("WIC025"), alert.properties.geocode?.get("UGC"))
        assertEquals(listOf("055025"), alert.properties.geocode?.get("SAME"))
        assertEquals(listOf("https://api.weather.gov/zones/county/WIC025"), alert.properties.affectedZones)
        assertEquals("FFW", alert.properties.eventCode?.get("SAME")?.single())
        assertEquals(listOf("Flash Flood"), alert.properties.parameters?.get("NWSheadline"))
        assertEquals("Polygon", alert.geometry?.get("type").toString().trim('"'))
    }

    @Test
    fun parsesManyAlertsInProviderOrder() {
        val collection = parsedFixture("alerts_active_many.json")

        assertEquals(listOf("alert-one", "alert-two"), collection.features.map { it.properties.id })
    }

    @Test
    fun keepsMissingOptionalProviderFieldsAbsent() {
        val alert = parsedFixture("alerts_active_missing_optional.json").features.single()

        assertNull(alert.geometry)
        assertNull(alert.properties.areaDesc)
        assertNull(alert.properties.geocode)
        assertNull(alert.properties.affectedZones)
        assertNull(alert.properties.references)
        assertNull(alert.properties.headline)
        assertNull(alert.properties.description)
        assertNull(alert.properties.instruction)
        assertNull(alert.properties.parameters)
        assertNull(alert.properties.eventCode)
    }

    @Test
    fun problemResponsesAreNotAcceptedAsActiveCollections() {
        listOf("alerts_problem_malformed.json", "alerts_problem_unsupported_region.json").forEach { name ->
            val error = assertThrows(NwsAlertParseException::class.java) {
                parsedFixture(name)
            }

            when (error) {
                is NwsAlertParseException.InvalidField -> assertEquals("type", error.fieldPath)
                is NwsAlertParseException.MissingField -> assertEquals("type", error.fieldPath)
                is NwsAlertParseException.InvalidJson -> throw AssertionError("Expected envelope failure, got invalid JSON")
            }
        }
    }

    @Test
    fun failsDeterministicallyForMalformedActiveEnvelope() {
        val error = assertThrows(NwsAlertParseException.InvalidField::class.java) {
            parsedFixture("alerts_active_malformed_envelope.json")
        }

        assertEquals("features", error.fieldPath)
    }

    private fun parsedFixture(name: String): NwsAlertCollection =
        NwsAlertParser.parseActiveAlerts(fixture(name))

    private fun fixture(name: String): String {
        val resource = requireNotNull(javaClass.classLoader?.getResource("providers/nws/$name")) {
            "Missing fixture $name"
        }
        return resource.readText()
    }
}
