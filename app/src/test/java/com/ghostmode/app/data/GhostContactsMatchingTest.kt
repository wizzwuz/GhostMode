package com.ghostmode.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GhostContactsMatchingTest {

    private lateinit var store: KeyValueStore
    private lateinit var state: GhostStateRepository

    @Before
    fun setUp() {
        store = InMemoryStore()
        state = GhostStateRepository(store)
    }

    @Test
    fun addAndRemoveContacts() {
        val contact1 = state.addGhostContact("Alice", "+15551234567")
        val contact2 = state.addGhostContact("Bob", "+15559876543")

        assertEquals(2, state.ghostContacts.value.size)

        state.removeGhostContact(contact1.id)
        assertEquals(1, state.ghostContacts.value.size)
        assertEquals("Bob", state.ghostContacts.value.first().name)
    }

    @Test
    fun toggleContact() {
        val contact = state.addGhostContact("Alice", "+15551234567")
        assertTrue(state.ghostContacts.value.first().enabled)

        state.toggleGhostContact(contact.id)
        assertFalse(state.ghostContacts.value.first().enabled)

        state.toggleGhostContact(contact.id)
        assertTrue(state.ghostContacts.value.first().enabled)
    }

    @Test
    fun numberBlockingMatchingLogic() {
        state.setGhostContactsEnabled(true)
        state.addGhostContact("Alice", "+7 (999) 123-45-67")

        // Exact match with formatting stripped
        assertTrue(state.isNumberBlocked("+79991234567"))
        assertTrue(state.isNumberBlocked("89991234567")) // National vs international prefix
        assertTrue(state.isNumberBlocked("79991234567"))
        assertTrue(state.isNumberBlocked("+7 999 123 45 67"))

        // Different number
        assertFalse(state.isNumberBlocked("+79997654321"))
    }

    @Test
    fun blockedOnlyWhenEnabled() {
        val contact = state.addGhostContact("Alice", "+15551234567")

        // Feature master switch is OFF
        state.setGhostContactsEnabled(false)
        assertFalse(state.isNumberBlocked("+15551234567"))

        // Feature master switch is ON
        state.setGhostContactsEnabled(true)
        assertTrue(state.isNumberBlocked("+15551234567"))

        // Specific contact disabled
        state.toggleGhostContact(contact.id)
        assertFalse(state.isNumberBlocked("+15551234567"))
    }

    @Test
    fun persistenceSurvivesRepositoryRecreation() {
        state.setGhostContactsEnabled(true)
        state.setGhostContactsSilenceOnly(true)
        state.setGhostContactsLogCalls(false)
        state.addGhostContact("Alice", "+15551234567")

        // New repository instance with the same store
        val restored = GhostStateRepository(store)
        assertTrue(restored.ghostContactsEnabled.value)
        assertTrue(restored.ghostContactsSilenceOnly.value)
        assertFalse(restored.ghostContactsLogCalls.value)
        assertEquals(1, restored.ghostContacts.value.size)
        assertEquals("Alice", restored.ghostContacts.value.first().name)
        assertEquals("+15551234567", restored.ghostContacts.value.first().phoneNumber)
    }
}
