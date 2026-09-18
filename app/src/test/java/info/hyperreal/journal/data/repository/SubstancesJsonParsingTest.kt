package info.hyperreal.journal.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import info.hyperreal.journal.domain.model.Substance
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SubstancesJsonParsingTest {

    private val gson = Gson()

    @Test
    fun testSubstancesJsonParsesAndContainsRequiredOpioids() {
        val file = File("src/main/assets/substances.json")
        assertTrue("substances.json should exist", file.exists())

        val type = object : TypeToken<List<Substance>>() {}.type
        val substances: List<Substance> = gson.fromJson(file.readText(), type)
        assertTrue("Substances list should not be empty", substances.isNotEmpty())

        val requiredOpioids = listOf(
            "Kodeina",
            "Morfina",
            "Oksykodon",
            "Fentanyl",
            "Buprenorfina",
            "Metadon",
            "Tramadol"
        )

        for (opioidId in requiredOpioids) {
            val found = substances.find { it.id.equals(opioidId, ignoreCase = true) }
            assertNotNull("Opioid $opioidId should exist in substances.json", found)
            assertTrue("Opioid $opioidId should have ROAs defined", found!!.roas.isNotEmpty())
            val roa = found.roas.first()
            assertNotNull("Opioid $opioidId ROA should have dose defined", roa.dose)
            assertNotNull("Opioid $opioidId ROA should have duration defined", roa.duration)
            assertTrue("Opioid $opioidId should have harm reduction notes", !found.harmReduction.isNullOrBlank())
        }
    }
}
