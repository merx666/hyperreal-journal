package info.hyperreal.journal.ui.substances

import info.hyperreal.journal.domain.model.DurationParameters
import info.hyperreal.journal.domain.model.Roa
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.repository.SubstanceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubstancesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<SubstanceRepository>()
    private lateinit var viewModel: SubstancesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val initialSubstances = listOf(
            Substance(id = "sub1", name = "Amphetamine", classes = listOf("Stimulant")),
            Substance(id = "sub2", name = "Custom Noo", classes = listOf("Własne"))
        )
        every { repository.getAllSubstances() } returns flowOf(initialSubstances)
        viewModel = SubstancesViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `availableClasses contains Wlasne`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.availableClasses.collect()
        }
        testScheduler.advanceUntilIdle()
        val classes = viewModel.availableClasses.value
        assertTrue(classes.contains("Własne"))
        assertTrue(classes.contains("Stimulant"))
    }

    @Test
    fun `addCustomSubstance delegates to repository`() = runTest {
        val duration = DurationParameters(onset = 10f, total = 60f)
        val created = Substance(
            id = "custom_test",
            name = "Test RC",
            classes = listOf("Własne"),
            roas = listOf(Roa(name = "Doustnie", duration = duration))
        )
        coEvery { repository.addCustomSubstance("Test RC", "Doustnie", duration) } returns created

        var callbackResult: Substance? = null
        viewModel.addCustomSubstance("Test RC", "Doustnie", duration) {
            callbackResult = it
        }

        testScheduler.advanceUntilIdle()
        coVerify { repository.addCustomSubstance("Test RC", "Doustnie", duration) }
        assertEquals("Test RC", callbackResult?.name)
    }

    @Test
    fun `deleteCustomSubstance delegates to repository`() = runTest {
        coEvery { repository.deleteCustomSubstance("custom_test") } returns Unit

        viewModel.deleteCustomSubstance("custom_test")

        testScheduler.advanceUntilIdle()
        coVerify { repository.deleteCustomSubstance("custom_test") }
    }
}
