package info.hyperreal.journal.ui.journal

import app.cash.turbine.test
import info.hyperreal.journal.domain.model.DurationParameters
import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.model.Roa
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.repository.IngestionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import info.hyperreal.journal.domain.usecase.TimelineCalculator
import info.hyperreal.journal.domain.usecase.TimelinePhase
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var ingestionRepository: IngestionRepository
    private lateinit var substanceRepository: SubstanceRepository
    private lateinit var timelineCalculator: TimelineCalculator

    private val testSubstance = Substance(
        id = "mdma",
        name = "MDMA",
        roas = listOf(
            Roa(
                name = "Oral",
                duration = DurationParameters(
                    onset = 30f, comeup = 30f, peak = 120f,
                    offset = 90f, afterglow = 120f, total = 390f
                )
            )
        )
    )

    private val recentIngestion = Ingestion(
        id = 1,
        substanceId = "mdma",
        roa = "Oral",
        doseAmount = 100f,
        doseUnit = "mg",
        timestamp = System.currentTimeMillis() - 3600_000 // 1 hour ago
    )

    private val oldIngestion = Ingestion(
        id = 2,
        substanceId = "mdma",
        roa = "Oral",
        doseAmount = 75f,
        doseUnit = "mg",
        timestamp = System.currentTimeMillis() - 86400_000 // 1 day ago
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        ingestionRepository = mockk()
        substanceRepository = mockk()
        timelineCalculator = TimelineCalculator() // use real calculator
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `entries emits empty list when no ingestions`() = runTest {
        every { ingestionRepository.getAllIngestions() } returns flowOf(emptyList())
        every { substanceRepository.getAllSubstances() } returns flowOf(listOf(testSubstance))

        val vm = JournalViewModel(ingestionRepository, substanceRepository, timelineCalculator)

        vm.entries.test {
            val entries = awaitItem()
            assertTrue(entries.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `entries maps ingestions to journal entries with timeline status`() = runTest {
        every { ingestionRepository.getAllIngestions() } returns flowOf(listOf(recentIngestion))
        every { substanceRepository.getAllSubstances() } returns flowOf(listOf(testSubstance))

        val vm = JournalViewModel(ingestionRepository, substanceRepository, timelineCalculator)

        vm.entries.test {
            assertEquals(emptyList<JournalEntry>(), awaitItem())
            val entries = awaitItem()
            assertEquals(1, entries.size)
            assertEquals(testSubstance, entries[0].substance)
            assertNotNull(entries[0].timelineStatus)
            // 1 hour into a 30m onset + 30m comeup → should be in PEAK
            assertEquals(TimelinePhase.PEAK, entries[0].timelineStatus?.phase)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `entries are sorted by timestamp descending (newest first)`() = runTest {
        every { ingestionRepository.getAllIngestions() } returns flowOf(listOf(oldIngestion, recentIngestion))
        every { substanceRepository.getAllSubstances() } returns flowOf(listOf(testSubstance))

        val vm = JournalViewModel(ingestionRepository, substanceRepository, timelineCalculator)

        vm.entries.test {
            assertEquals(emptyList<JournalEntry>(), awaitItem())
            val entries = awaitItem()
            assertEquals(2, entries.size)
            assertTrue(entries[0].ingestion.timestamp > entries[1].ingestion.timestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `entry has null substance when substance not found`() = runTest {
        every { ingestionRepository.getAllIngestions() } returns flowOf(listOf(recentIngestion))
        every { substanceRepository.getAllSubstances() } returns flowOf(emptyList()) // no substances

        val vm = JournalViewModel(ingestionRepository, substanceRepository, timelineCalculator)

        vm.entries.test {
            assertEquals(emptyList<JournalEntry>(), awaitItem())
            val entries = awaitItem()
            assertEquals(1, entries.size)
            assertNull(entries[0].substance)
            assertNull(entries[0].timelineStatus)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchQuery filters entries by name or notes`() = runTest {
        val entryWithNotes = recentIngestion.copy(notes = "party with friends")
        every { ingestionRepository.getAllIngestions() } returns flowOf(listOf(entryWithNotes, oldIngestion))
        every { substanceRepository.getAllSubstances() } returns flowOf(listOf(testSubstance))

        val vm = JournalViewModel(ingestionRepository, substanceRepository, timelineCalculator)

        vm.setSearchQuery("party")
        vm.entries.test {
            assertEquals(emptyList<JournalEntry>(), awaitItem())
            val filtered = awaitItem()
            assertEquals(1, filtered.size)
            assertEquals("party with friends", filtered[0].ingestion.notes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filter ACTIVE_ONLY includes only active entries`() = runTest {
        // recentIngestion (1 hour ago, total 390 min) is active in PEAK
        // oldIngestion (24 hours ago) is at BASELINE
        every { ingestionRepository.getAllIngestions() } returns flowOf(listOf(recentIngestion, oldIngestion))
        every { substanceRepository.getAllSubstances() } returns flowOf(listOf(testSubstance))

        val vm = JournalViewModel(ingestionRepository, substanceRepository, timelineCalculator)

        vm.setFilter(JournalFilter.ACTIVE_ONLY)
        vm.entries.test {
            assertEquals(emptyList<JournalEntry>(), awaitItem())
            val active = awaitItem()
            assertEquals(1, active.size)
            assertEquals(recentIngestion.id, active[0].ingestion.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `activeEntries emits active substances with countdown`() = runTest {
        every { ingestionRepository.getAllIngestions() } returns flowOf(listOf(recentIngestion, oldIngestion))
        every { substanceRepository.getAllSubstances() } returns flowOf(listOf(testSubstance))

        val vm = JournalViewModel(ingestionRepository, substanceRepository, timelineCalculator)

        vm.activeEntries.test {
            assertEquals(emptyList<ActiveEntryInfo>(), awaitItem())
            val activeList = awaitItem()
            assertEquals(1, activeList.size)
            assertEquals("MDMA", activeList[0].entry.substance?.name)
            assertNotNull(activeList[0].countdown.minutesToBaseline)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
