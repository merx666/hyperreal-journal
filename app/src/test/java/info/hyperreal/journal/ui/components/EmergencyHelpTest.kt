package info.hyperreal.journal.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmergencyHelpTest {

    @Test
    fun `emergency contacts list contains 112 and all screenshot crisis numbers`() {
        assertEquals(5, EmergencyContactsList.size)

        // 1. Numer ratunkowy 112
        val contact112 = EmergencyContactsList.find { it.phoneNumber == "112" }
        assertNotNull(contact112)
        assertTrue(contact112!!.isEmergency112)
        assertTrue(contact112.title.contains("112") || contact112.title.contains("Ratunkowy"))

        // 2. Interwencja kryzysowa (514 202 619)
        val contactIntervention = EmergencyContactsList.find { it.phoneNumber.replace(" ", "") == "514202619" }
        assertNotNull(contactIntervention)
        assertEquals("Interwencja kryzysowa", contactIntervention!!.title)

        // 3. Kryzys samobójczy (511 200 200)
        val contactSuicideCrisis = EmergencyContactsList.find { it.phoneNumber.replace(" ", "") == "511200200" }
        assertNotNull(contactSuicideCrisis)
        assertEquals("Kryzys samobójczy", contactSuicideCrisis!!.title)

        // 4. Telefon Zaufania (116 123)
        val contactTrustLine = EmergencyContactsList.find { it.phoneNumber.replace(" ", "") == "116123" }
        assertNotNull(contactTrustLine)
        assertEquals("Telefon Zaufania", contactTrustLine!!.title)

        // 5. Dla dzieci i młodzieży (116 111)
        val contactYouth = EmergencyContactsList.find { it.phoneNumber.replace(" ", "") == "116111" }
        assertNotNull(contactYouth)
        assertEquals("Dla dzieci i młodzieży", contactYouth!!.title)
    }
}
