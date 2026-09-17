package info.hyperreal.journal.ui.mixcalculator

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
class MixCalculatorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var substanceRepository: SubstanceRepository
    private lateinit var interactionRepository: InteractionRepository

    private val mdma = Substance(id = "mdma", name = "MDMA")
    private val alcohol = Substance(id = "alcohol", name = "Alkohol")
    private val thc = Substance(id = "thc", name = "THC")

    private val dangerousInteraction = SubstanceInteraction(
        "MDMA", "Alkohol", InteractionStatus.DANGEROUS, "Dehydration risk"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        substanceRepository = mockk()
        interactionRepository = mockk()

        coEvery { substanceRepository.getAllSubstances() } returns flowOf(listOf(mdma, alcohol, thc))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MixCalculatorViewModel {
        return MixCalculatorViewModel(substanceRepository, interactionRepository)
    }

    @Test
    fun `initial state loads substances and clears loading`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.substances.size)
        assertEquals("Alkohol", state.substances[0].name) // sorted by name
    }

    @Test
    fun `selecting substance A updates state`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectSubstanceA(mdma)
        assertEquals(mdma, vm.uiState.value.substanceA)
    }

    @Test
    fun `selecting both substances triggers interaction check`() = runTest {
        coEvery { interactionRepository.getInteraction("MDMA", "Alkohol") } returns dangerousInteraction

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectSubstanceA(mdma)
        vm.selectSubstanceB(alcohol)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = vm.uiState.value.interactionResult
        assertNotNull(result)
        assertEquals(InteractionStatus.DANGEROUS, result!!.status)
    }

    @Test
    fun `swapSubstances swaps A and B`() = runTest {
        coEvery { interactionRepository.getInteraction(any(), any()) } returns null

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectSubstanceA(mdma)
        vm.selectSubstanceB(alcohol)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.swapSubstances()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(alcohol, vm.uiState.value.substanceA)
        assertEquals(mdma, vm.uiState.value.substanceB)
    }

    @Test
    fun `no interaction result when only one substance selected`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectSubstanceA(mdma)
        // substanceB is null → checkInteraction should return early
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.uiState.value.interactionResult)
    }

    @Test
    fun `interaction result is null when no interaction found`() = runTest {
        coEvery { interactionRepository.getInteraction("MDMA", "THC") } returns null

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectSubstanceA(mdma)
        vm.selectSubstanceB(thc)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.uiState.value.interactionResult)
    }
}
