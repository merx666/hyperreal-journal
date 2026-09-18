package info.hyperreal.journal.ui.matrix

import app.cash.turbine.test
import info.hyperreal.journal.domain.model.InteractionStatus
import info.hyperreal.journal.domain.model.SubstanceInteraction
import info.hyperreal.journal.domain.repository.InteractionRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MatrixExplorerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var interactionRepository: InteractionRepository

    private val sampleInteractions = listOf(
        SubstanceInteraction(
            substanceA = "Alkohol",
            substanceB = "Opioidy",
            status = InteractionStatus.DANGEROUS,
            note = "Depresja oddechowa, ryzyko zgonu"
        ),
        SubstanceInteraction(
            substanceA = "Alkohol",
            substanceB = "Kokaina",
            status = InteractionStatus.UNSAFE,
            note = "Powstaje kokaetylen, wysoka kardiotoksyczność"
        ),
        SubstanceInteraction(
            substanceA = "MDMA",
            substanceB = "LSD",
            status = InteractionStatus.LOW_RISK_SYNERGY,
            note = "Candyflip, silna synergia"
        ),
        SubstanceInteraction(
            substanceA = "Alkohol",
            substanceB = "Amfetamina",
            status = InteractionStatus.CAUTION,
            note = "Maskowanie upojenia alkoholowego"
        )
    )

    private val sampleSubstances = listOf("Alkohol", "Amfetamina", "Kokaina", "LSD", "MDMA", "Opioidy")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        interactionRepository = mockk()
        coEvery { interactionRepository.getAllInteractions() } returns sampleInteractions
        coEvery { interactionRepository.getMatrixSubstances() } returns sampleSubstances
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = MatrixExplorerViewModel(interactionRepository)

    @Test
    fun `initial state loads all interactions, substances, and counts correctly`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            // Initial default
            val initial = awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(4, loaded.allInteractions.size)
            assertEquals(6, loaded.matrixSubstances.size)
            assertEquals(4, loaded.filteredInteractions.size)
            assertEquals(1, loaded.statusCounts[InteractionStatus.DANGEROUS])
            assertEquals(1, loaded.statusCounts[InteractionStatus.UNSAFE])
            assertEquals(1, loaded.statusCounts[InteractionStatus.CAUTION])
            assertEquals(1, loaded.statusCounts[InteractionStatus.LOW_RISK_SYNERGY])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectSubstance filters interactions where substance is A or B`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()

            vm.selectSubstance("MDMA")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals("MDMA", state.selectedSubstance)
            assertEquals(1, state.filteredInteractions.size)
            assertEquals("LSD", state.filteredInteractions.first().substanceB)

            // Tapping again toggles off
            vm.selectSubstance("MDMA")
            testDispatcher.scheduler.advanceUntilIdle()
            val toggledState = awaitItem()
            assertNull(toggledState.selectedSubstance)
            assertEquals(4, toggledState.filteredInteractions.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setStatusFilter filters interactions by severity`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()

            vm.setStatusFilter(InteractionStatus.DANGEROUS)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(InteractionStatus.DANGEROUS, state.selectedStatusFilter)
            assertEquals(1, state.filteredInteractions.size)
            assertEquals("Alkohol", state.filteredInteractions.first().substanceA)
            assertEquals("Opioidy", state.filteredInteractions.first().substanceB)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setSearchQuery filters interactions by substance name or clinical note`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()

            vm.setSearchQuery("kokaetylen")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(1, state.filteredInteractions.size)
            assertEquals("Kokaina", state.filteredInteractions.first().substanceB)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showInteractionDetail updates selectedInteractionForDetail`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()

            val target = sampleInteractions.first()
            vm.showInteractionDetail(target)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertEquals(target, state.selectedInteractionForDetail)

            vm.showInteractionDetail(null)
            testDispatcher.scheduler.advanceUntilIdle()
            val closedState = awaitItem()
            assertNull(closedState.selectedInteractionForDetail)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetFilters resets all filters and search query`() = runTest {
        val vm = createViewModel()

        vm.uiState.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()

            vm.selectSubstance("Alkohol")
            vm.setStatusFilter(InteractionStatus.DANGEROUS)
            vm.setSearchQuery("zgonu")
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()

            vm.resetFilters()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertNull(state.selectedSubstance)
            assertNull(state.selectedStatusFilter)
            assertEquals("", state.searchQuery)
            assertEquals(4, state.filteredInteractions.size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
