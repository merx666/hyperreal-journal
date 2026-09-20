package info.hyperreal.journal.ui.journal

import app.cash.turbine.test
import info.hyperreal.journal.domain.model.CheckIn
import info.hyperreal.journal.domain.model.DurationParameters
import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.model.Roa
import info.hyperreal.journal.domain.model.ShulginRating
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.repository.CheckInRepository
import info.hyperreal.journal.domain.repository.IngestionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import info.hyperreal.journal.domain.usecase.TimelineCalculator
import info.hyperreal.journal.domain.usecase.TimelinePhase
import info.hyperreal.journal.core.worker.ReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
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
    private lateinit var checkInRepository: CheckInRepository
    private lateinit var timelineCalculator: TimelineCalculator
    private lateinit var reminderScheduler: ReminderScheduler

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
        checkInRepository = mockk()
        reminderScheduler = mockk(relaxed = true)
        timelineCalculator = TimelineCalculator() // use real calculator

        every { ingestionRepository.getAllIngestions() } returns flowOf(emptyList())
        coEvery { ingestionRepository.deleteIngestion(any()) } returns Unit
        every { substanceRepository.getAllSubstances() } returns flowOf(emptyList())
        every { checkInRepository.getAllCheckIns() } returns flowOf(emptyList())
        coEvery { checkInRepository.insertCheckIn(any()) } returns 1L
        coEvery { checkInRepository.deleteCheckIn(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = JournalViewModel(
        ingestionRepository = ingestionRepository,
        substanceRepository = substanceRepository,
        timelineCalculator = timelineCalculator,
        checkInRepository = checkInRepository,
        reminderScheduler = reminderScheduler
    )

    @Test
    fun `entries emits empty list when no ingestions`() = runTest {
        every { ingestionRepository.getAllIngestions() } returns flowOf(emptyList())
        every { substanceRepository.getAllSubstances() } returns flowOf(listOf(testSubstance))

        val vm = createViewModel()

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

        val vm = createViewModel()

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

        val vm = createViewModel()

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

        val vm = createViewModel()

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

        val vm = createViewModel()

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

        val vm = createViewModel()

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

        val vm = createViewModel()

        vm.activeEntries.test {
            assertEquals(emptyList<ActiveEntryInfo>(), awaitItem())
            val activeList = awaitItem()
            assertEquals(1, activeList.size)
            assertEquals("MDMA", activeList[0].entry.substance?.name)
            assertNotNull(activeList[0].countdown.minutesToBaseline)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `entries associates check-ins with correct ingestion`() = runTest {
        val checkIn1 = CheckIn(
            id = 10,
            ingestionId = recentIngestion.id,
            timestamp = System.currentTimeMillis() - 1800_000,
            phase = TimelinePhase.COMEUP,
            shulginRating = ShulginRating.PLUS_TWO,
            notes = "Feeling energetic"
        )
        val checkIn2 = CheckIn(
            id = 11,
            ingestionId = oldIngestion.id,
            timestamp = oldIngestion.timestamp + 3600_000,
            phase = TimelinePhase.PEAK,
            shulginRating = ShulginRating.PLUS_THREE,
            notes = "Intense peak"
        )

        every { ingestionRepository.getAllIngestions() } returns flowOf(listOf(recentIngestion, oldIngestion))
        every { substanceRepository.getAllSubstances() } returns flowOf(listOf(testSubstance))
        every { checkInRepository.getAllCheckIns() } returns flowOf(listOf(checkIn1, checkIn2))

        val vm = createViewModel()

        vm.entries.test {
            assertEquals(emptyList<JournalEntry>(), awaitItem())
            val entries = awaitItem()
            assertEquals(2, entries.size)

            val recentEntry = entries.find { it.ingestion.id == recentIngestion.id }
            val oldEntry = entries.find { it.ingestion.id == oldIngestion.id }

            assertEquals(1, recentEntry?.checkIns?.size)
            assertEquals(checkIn1, recentEntry?.checkIns?.first())

            assertEquals(1, oldEntry?.checkIns?.size)
            assertEquals(checkIn2, oldEntry?.checkIns?.first())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addCheckIn calls repository insertCheckIn with correct parameters`() = runTest {
        val capturedSlot = slot<CheckIn>()
        coEvery { checkInRepository.insertCheckIn(capture(capturedSlot)) } returns 99L

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.addCheckIn(
            ingestionId = recentIngestion.id,
            phase = TimelinePhase.PEAK,
            rating = ShulginRating.PLUS_THREE,
            notes = "Very visual",
            timestamp = 123456789L
        )
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { checkInRepository.insertCheckIn(any()) }
        assertEquals(recentIngestion.id, capturedSlot.captured.ingestionId)
        assertEquals(TimelinePhase.PEAK, capturedSlot.captured.phase)
        assertEquals(ShulginRating.PLUS_THREE, capturedSlot.captured.shulginRating)
        assertEquals("Very visual", capturedSlot.captured.notes)
        assertEquals(123456789L, capturedSlot.captured.timestamp)
    }

    @Test
    fun `deleteCheckIn calls repository deleteCheckIn`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.deleteCheckIn(42L)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { checkInRepository.deleteCheckIn(42L) }
    }

    @Test
    fun `deleteIngestion cancels scheduled reminder and calls repository deleteIngestion`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.deleteIngestion(recentIngestion)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 1) { reminderScheduler.cancelReminder(recentIngestion.id) }
        coVerify(exactly = 1) { ingestionRepository.deleteIngestion(recentIngestion) }
    }

    @Test
    fun `schedulePeakReminders schedules only for ONSET or COMEUP and calculates with fractional minutes`() = runTest {
        val now = System.currentTimeMillis()
        // Ingestion that started 5 minutes ago (300_000ms ago)
        val ingestionOnset = Ingestion(
            id = 10,
            substanceId = "mdma",
            roa = "Oral",
            doseAmount = 100f,
            doseUnit = "mg",
            timestamp = now - 5 * 60_000L
        )

        // Substance with fractional onset 0.5f minutes
        val substanceWithFractionalDuration = Substance(
            id = "mdma",
            name = "MDMA",
            roas = listOf(
                Roa(
                    name = "Oral",
                    duration = DurationParameters(
                        onset = 0.5f,
                        comeup = 20f,
                        peak = 60f,
                        offset = 60f,
                        afterglow = 60f,
                        total = 200.5f
                    )
                )
            )
        )

        every { ingestionRepository.getAllIngestions() } returns flowOf(listOf(ingestionOnset))
        every { substanceRepository.getAllSubstances() } returns flowOf(listOf(substanceWithFractionalDuration))

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify reminder was scheduled with expected trigger timestamp
        // onset = 0.5 * 60_000 = 30_000L
        // comeup = 20 * 60_000 = 1_200_000L
        // peak = 60 * 60_000 = 3_600_000L
        // peakStart = timestamp + 1_230_000L
        // reminderMs = peakStart + (3_600_000 * 80 / 100) = peakStart + 2_880_000L
        val expectedPeakStart = ingestionOnset.timestamp + 30_000L + 1_200_000L
        val expectedReminderMs = expectedPeakStart + (3_600_000L * 80L / 100L)

        verify(atLeast = 1) {
            reminderScheduler.scheduleReminder(
                ingestionId = 10L,
                substanceName = "MDMA",
                phaseLabel = any(),
                triggerAtMs = expectedReminderMs
            )
        }
    }

    @Test
    fun `schedulePeakReminders cancels reminder when ingestion leaves active list`() = runTest {
        val now = System.currentTimeMillis()
        val activeIngestion = Ingestion(
            id = 20,
            substanceId = "mdma",
            roa = "Oral",
            doseAmount = 100f,
            doseUnit = "mg",
            timestamp = now - 5 * 60_000L
        )

        val ingestionsFlow = kotlinx.coroutines.flow.MutableStateFlow(listOf(activeIngestion))
        every { ingestionRepository.getAllIngestions() } returns ingestionsFlow
        every { substanceRepository.getAllSubstances() } returns flowOf(listOf(testSubstance))

        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(atLeast = 1) { reminderScheduler.scheduleReminder(ingestionId = 20L, any(), any(), any()) }

        // Remove from active ingestions
        ingestionsFlow.value = emptyList()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(atLeast = 1) { reminderScheduler.cancelReminder(20L) }
    }
}
