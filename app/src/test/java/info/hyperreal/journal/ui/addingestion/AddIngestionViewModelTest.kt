package info.hyperreal.journal.ui.addingestion

import info.hyperreal.journal.data.local.datastore.UserPreferencesRepository
import info.hyperreal.journal.domain.model.DurationParameters
import info.hyperreal.journal.domain.model.Roa
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.repository.IngestionRepository
import info.hyperreal.journal.domain.repository.InteractionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import info.hyperreal.journal.domain.usecase.InteractionChecker
import info.hyperreal.journal.domain.usecase.ToleranceCalculator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddIngestionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val substanceRepository = mockk<SubstanceRepository>()
    private val ingestionRepository = mockk<IngestionRepository>()
    private val interactionRepository = mockk<InteractionRepository>()
    private val interactionChecker = mockk<InteractionChecker>()
    private val toleranceCalculator = mockk<ToleranceCalculator>()
    private val userPreferencesRepository = mockk<UserPreferencesRepository>()

    private lateinit var viewModel: AddIngestionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { substanceRepository.getAllSubstances() } returns flowOf(emptyList())
        viewModel = AddIngestionViewModel(
            substanceRepository,
            ingestionRepository,
            interactionRepository,
            interactionChecker,
            toleranceCalculator,
            userPreferencesRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addCustomSubstance creates substance and selects it with roa`() = runTest {
        val duration = DurationParameters(onset = 15f, peak = 90f, total = 240f)
        val roa = Roa(name = "Doustnie", duration = duration)
        val created = Substance(
            id = "custom_100",
            name = "Kava Extra",
            classes = listOf("Własne"),
            roas = listOf(roa)
        )

        coEvery { substanceRepository.addCustomSubstance("Kava Extra", "Doustnie", duration) } returns created
        every { ingestionRepository.getAllIngestions() } returns flowOf(emptyList())
        every { userPreferencesRepository.toleranceOverridesFlow } returns flowOf(emptyMap())
        coEvery { toleranceCalculator.getStatusForSubstance(any(), any(), any(), any()) } returns null

        var callbackCalled = false
        viewModel.addCustomSubstance("Kava Extra", "Doustnie", duration) {
            callbackCalled = true
        }

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { substanceRepository.addCustomSubstance("Kava Extra", "Doustnie", duration) }
        assertEquals(true, callbackCalled)
        assertEquals("custom_100", viewModel.selectedSubstance.value?.id)
        assertEquals("Doustnie", viewModel.selectedRoa.value?.name)
    }
}
