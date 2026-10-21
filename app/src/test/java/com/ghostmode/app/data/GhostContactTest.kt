package com.ghostmode.app.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GhostContactTest {

    @Test
    fun serializationRoundTrip() {
        val original = GhostContact(
            id = "test-id-123",
            name = "John Doe",
            phoneNumber = "+1234567890",
            enabled = true
        )

        val json = original.toJson()
        val restored = GhostContact.fromJson(json)

        assertNotNull(restored)
        assertEquals(original.id, restored?.id)
        assertEquals(original.name, restored?.name)
        assertEquals(original.phoneNumber, restored?.phoneNumber)
        assertEquals(original.enabled, restored?.enabled)
    }

    @Test
    fun disabledContactRoundTrip() {
        val original = GhostContact(
            id = "test-id-456",
            name = "Jane Smith",
            phoneNumber = "9876543210",
            enabled = false
        )

        val json = original.toJson()
        val restored = GhostContact.fromJson(json)

        assertNotNull(restored)
        assertEquals(false, restored?.enabled)
    }

    @Test
    fun fromJsonWithMissingIdGeneratesNewId() {
        val json = JSONObject().apply {
            put("name", "Bob")
            put("phoneNumber", "555-1234")
            put("enabled", true)
        }

        val contact = GhostContact.fromJson(json)
        assertNotNull(contact)
        assertEquals("Bob", contact?.name)
        assertEquals("555-1234", contact?.phoneNumber)
        assertTrue(contact?.id?.isNotEmpty() == true)
    }
}
