package info.hyperreal.journal.data.local.datastore

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesRepositoryTest {

    @Test
    fun `hashPin generates consistent non-empty hash`() {
        val hash1 = UserPreferencesRepository.hashPin("1234")
        val hash2 = UserPreferencesRepository.hashPin("1234")

        assertTrue(hash1.isNotBlank())
        assertTrue(hash1 == hash2)
    }

    @Test
    fun `hashPin generates different hashes for different pins`() {
        val hash1 = UserPreferencesRepository.hashPin("1234")
        val hash2 = UserPreferencesRepository.hashPin("4321")

        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `verifyPin returns true for correct pin and false for incorrect pin`() {
        val pin = "2580"
        val hash = UserPreferencesRepository.hashPin(pin)

        val repo = UserPreferencesRepository(io.mockk.mockk(relaxed = true))

        assertTrue(repo.verifyPin("2580", hash))
        assertFalse(repo.verifyPin("0000", hash))
        assertFalse(repo.verifyPin("2581", hash))
        assertFalse(repo.verifyPin("", hash))
        assertFalse(repo.verifyPin("2580", null))
        assertFalse(repo.verifyPin("2580", ""))
    }
}
