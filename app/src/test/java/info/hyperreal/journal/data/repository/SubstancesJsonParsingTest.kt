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

    @Test
    fun testSubstancesJsonContainsMethylphenidateIRWithAllRoas() {
        val file = File("src/main/assets/substances.json")
        val type = object : TypeToken<List<Substance>>() {}.type
        val substances: List<Substance> = gson.fromJson(file.readText(), type)

        val mphIr = substances.find { it.id == "Metylofenidat IR" }
        assertNotNull("Metylofenidat IR should exist in substances.json", mphIr)
        assertTrue("Metylofenidat IR must have harm reduction notes", !mphIr!!.harmReduction.isNullOrBlank())

        val roaNames = mphIr.roas.map { it.name }
        val expectedRoas = listOf("Oral", "Insufflated", "Sublingual", "Rectal", "Intravenous")
        for (expected in expectedRoas) {
            assertTrue("Metylofenidat IR must include $expected ROA", roaNames.contains(expected))
            val roa = mphIr.roas.find { it.name == expected }
            assertNotNull("ROA $expected must have dose", roa?.dose)
            assertNotNull("ROA $expected must have duration", roa?.duration)
            assertTrue("ROA $expected threshold dose must be > 0", (roa?.dose?.threshold ?: 0f) > 0f)
            assertTrue("ROA $expected duration total must be > 0", (roa?.duration?.total ?: 0f) > 0f)
        }
    }

    @Test
    fun testSubstancesJsonContainsMethylphenidateCR() {
        val file = File("src/main/assets/substances.json")
        val type = object : TypeToken<List<Substance>>() {}.type
        val substances: List<Substance> = gson.fromJson(file.readText(), type)

        val mphCr = substances.find { it.id == "Metylofenidat CR" }
        assertNotNull("Metylofenidat CR should exist in substances.json", mphCr)
        assertTrue("Metylofenidat CR must have harm reduction notes", !mphCr!!.harmReduction.isNullOrBlank())
        assertTrue("Metylofenidat CR harm reduction must warn about dose dumping/kruszenie", mphCr.harmReduction!!.contains("dose dumping", ignoreCase = true))

        val oral = mphCr.roas.find { it.name == "Oral" }
        assertNotNull("Metylofenidat CR must have Oral ROA", oral)
        assertNotNull("Metylofenidat CR Oral dose must exist", oral?.dose)
        assertNotNull("Metylofenidat CR Oral duration must exist", oral?.duration)
        assertTrue("Metylofenidat CR duration should reflect extended release (>= 480 min)", (oral?.duration?.total ?: 0f) >= 480f)
    }
}
