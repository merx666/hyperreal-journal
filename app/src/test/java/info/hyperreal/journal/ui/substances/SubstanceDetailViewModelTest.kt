package info.hyperreal.journal.ui.substances

import app.cash.turbine.test
import info.hyperreal.journal.domain.model.InteractionStatus
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.model.SubstanceInteraction
import info.hyperreal.journal.domain.repository.InteractionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubstanceDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var substanceRepository: SubstanceRepository
    private lateinit var interactionRepository: InteractionRepository

    private val testSubstance = Substance(
        id = "mdma",
        name = "MDMA",
        aliases = listOf("Molly", "Ecstasy"),
        summary = "Test summary",
        classes = listOf("Stymulant", "Empatogen")
    )

    private val testInteractions = listOf(
        SubstanceInteraction("MDMA", "Alkohol", InteractionStatus.DANGEROUS, "Dehydration"),
        SubstanceInteraction("Amfetamina", "MDMA", InteractionStatus.CAUTION, "Cardiotoxicity"),
        SubstanceInteraction("THC", "LSD", InteractionStatus.LOW_RISK_SYNERGY, null) // unrelated
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        substanceRepository = mockk()
        interactionRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(substanceId: String = "mdma"): SubstanceDetailViewModel {
        coEvery { substanceRepository.getSubstanceById(substanceId) } returns testSubstance
        coEvery { interactionRepository.getAllInteractions() } returns testInteractions

        val savedStateHandle = androidx.lifecycle.SavedStateHandle(mapOf("substanceId" to substanceId))
        return SubstanceDetailViewModel(substanceRepository, interactionRepository, savedStateHandle)
    }

    @Test
    fun `initial state is null substance and empty interactions`() = runTest {
        val vm = createViewModel()
        // Before advancing dispatcher
        assertNull(vm.substance.value)
        assertTrue(vm.interactions.value.isEmpty())
    }

    @Test
    fun `loads substance after init`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(testSubstance, vm.substance.value)
    }

    @Test
    fun `filters interactions for current substance`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val interactions = vm.interactions.value
        // Should include MDMA-Alkohol and Amfetamina-MDMA but NOT THC-LSD
        assertEquals(2, interactions.size)
        assertTrue(interactions.any { it.substanceA == "MDMA" || it.substanceB == "MDMA" })
        assertFalse(interactions.any { it.substanceA == "THC" })
    }

    @Test
    fun `handles substance not found`() = runTest {
        coEvery { substanceRepository.getSubstanceById("unknown") } returns null
        coEvery { interactionRepository.getAllInteractions() } returns testInteractions

        val savedStateHandle = androidx.lifecycle.SavedStateHandle(mapOf("substanceId" to "unknown"))
        val vm = SubstanceDetailViewModel(substanceRepository, interactionRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.substance.value)
        assertTrue(vm.interactions.value.isEmpty())
    }
}
