package info.hyperreal.journal.data.repository

import android.content.Context
import android.content.res.AssetManager
import info.hyperreal.journal.domain.model.InteractionStatus
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class InteractionRepositoryTest {

    private lateinit var context: Context
    private lateinit var assetManager: AssetManager
    private lateinit var repository: InteractionRepositoryImpl

    @Before
    fun setUp() {
        context = mockk()
        assetManager = mockk()
        every { context.assets } returns assetManager
        every { assetManager.open("interactions_sin.json") } answers {
            File("src/main/assets/interactions_sin.json").inputStream()
        }
        repository = InteractionRepositoryImpl(context)
    }

    @Test
    fun `getAllInteractions parses all interactions from interactions_sin_json`() = runTest {
        val interactions = repository.getAllInteractions()
        assertEquals(333, interactions.size)
    }

    @Test
    fun `getMatrixSubstances returns exactly 27 unique sorted substances`() = runTest {
        val substances = repository.getMatrixSubstances()
        assertEquals(27, substances.size)
        assertTrue(substances.contains("Alkohol"))
        assertTrue(substances.contains("MDMA"))
        assertTrue(substances.contains("Opioidy"))
        assertTrue(substances.contains("Benzodiazepiny"))
        assertTrue(substances.contains("Ketamina"))
        assertTrue(substances.contains("LSD"))
        assertTrue(substances.contains("SSRI/SNRI"))

        // Verify sorted alphabetically
        assertEquals(substances.sortedWith(String.CASE_INSENSITIVE_ORDER), substances)
    }

    @Test
    fun `getInteractionsForSubstance returns all interactions involving target substance sorted by severity`() = runTest {
        val alcoholInteractions = repository.getInteractionsForSubstance("Alkohol")
        assertTrue(alcoholInteractions.isNotEmpty())
        assertTrue(alcoholInteractions.all {
            it.substanceA.equals("Alkohol", ignoreCase = true) ||
            it.substanceB.equals("Alkohol", ignoreCase = true)
        })

        // Verify that the most dangerous interactions are ranked first
        val firstStatus = alcoholInteractions.first().status
        assertEquals(InteractionStatus.DANGEROUS, firstStatus)
    }

    @Test
    fun `getInteraction finds interaction regardless of argument order`() = runTest {
        val forward = repository.getInteraction("Alkohol", "Opioidy")
        val backward = repository.getInteraction("Opioidy", "Alkohol")

        assertNotNull(forward)
        assertNotNull(backward)
        assertEquals(InteractionStatus.DANGEROUS, forward?.status)
        assertEquals(InteractionStatus.DANGEROUS, backward?.status)
    }

    @Test
    fun `getInteraction resolves Metylofenidat IR to Amfetamina category interactions`() = runTest {
        val interaction = repository.getInteraction("Metylofenidat IR", "MAOI")
        assertNotNull("Metylofenidat IR + MAOI should resolve to Amfetamina + MAOI interaction", interaction)
        assertEquals(InteractionStatus.DANGEROUS, interaction?.status)
    }

    @Test
    fun `getInteractionsForSubstance resolves Metylofenidat to Amfetamina interactions`() = runTest {
        val interactions = repository.getInteractionsForSubstance("Metylofenidat CR")
        assertTrue("Metylofenidat CR should return interactions via Amfetamina alias", interactions.isNotEmpty())
        assertTrue("Should contain MAOI interaction", interactions.any { it.substanceA == "MAOI" || it.substanceB == "MAOI" })
    }
}
