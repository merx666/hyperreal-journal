package info.hyperreal.journal.domain.usecase

import info.hyperreal.journal.domain.model.DurationParameters
import info.hyperreal.journal.domain.model.Roa
import info.hyperreal.journal.domain.model.Substance
import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineCalculatorTest {

    private val calculator = TimelineCalculator()

    // Standard substance with well-defined duration phases (in minutes)
    private val standardDuration = DurationParameters(
        onset = 30f,
        comeup = 30f,
        peak = 120f,
        offset = 90f,
        afterglow = 120f,
        total = 390f
    )

    private val standardRoa = Roa(
        name = "Oral",
        dose = null,
        duration = standardDuration
    )

    private val substance = Substance(
        id = "test-sub",
        name = "Test Substance",
        roas = listOf(standardRoa)
    )

    private val baseTime = 1_000_000_000L // arbitrary fixed base time in ms

    private fun minutesToMs(minutes: Float): Long = (minutes * 60_000).toLong()

    // --- Phase identification tests ---

    @Test
    fun `NOT_STARTED when current time is before ingestion`() {
        val result = calculator.calculatePhase(
            substance, "Oral", baseTime,
            currentTimeMs = baseTime - 60_000 // 1 minute before
        )
        assertEquals(TimelinePhase.NOT_STARTED, result.phase)
        assertEquals(0f, result.progressPercent, 0.01f)
    }

    @Test
    fun `ONSET at t=0`() {
        val result = calculator.calculatePhase(
            substance, "Oral", baseTime,
            currentTimeMs = baseTime // exactly at ingestion
        )
        assertEquals(TimelinePhase.ONSET, result.phase)
        assertEquals(0f, result.progressPercent, 0.01f)
    }

    @Test
    fun `ONSET at 50 percent through onset phase`() {
        val result = calculator.calculatePhase(
            substance, "Oral", baseTime,
            currentTimeMs = baseTime + minutesToMs(15f) // 15 min into 30 min onset
        )
        assertEquals(TimelinePhase.ONSET, result.phase)
        assertEquals(0.5f, result.progressPercent, 0.01f)
    }

    @Test
    fun `COMEUP starts right after onset ends`() {
        val result = calculator.calculatePhase(
            substance, "Oral", baseTime,
            currentTimeMs = baseTime + minutesToMs(31f) // 1 min into comeup
        )
        assertEquals(TimelinePhase.COMEUP, result.phase)
    }

    @Test
    fun `PEAK starts after onset + comeup`() {
        val result = calculator.calculatePhase(
            substance, "Oral", baseTime,
            currentTimeMs = baseTime + minutesToMs(61f) // 1 min into peak
        )
        assertEquals(TimelinePhase.PEAK, result.phase)
    }

    @Test
    fun `PEAK progress at midpoint`() {
        // onset(30) + comeup(30) + peak midpoint(60) = 120 min
        val result = calculator.calculatePhase(
            substance, "Oral", baseTime,
            currentTimeMs = baseTime + minutesToMs(120f)
        )
        assertEquals(TimelinePhase.PEAK, result.phase)
        assertEquals(0.5f, result.progressPercent, 0.01f)
    }

    @Test
    fun `OFFSET starts after onset + comeup + peak`() {
        // onset(30) + comeup(30) + peak(120) = 180 min
        val result = calculator.calculatePhase(
            substance, "Oral", baseTime,
            currentTimeMs = baseTime + minutesToMs(181f)
        )
        assertEquals(TimelinePhase.OFFSET, result.phase)
    }

    @Test
    fun `AFTERGLOW starts after onset + comeup + peak + offset`() {
        // onset(30) + comeup(30) + peak(120) + offset(90) = 270 min
        val result = calculator.calculatePhase(
            substance, "Oral", baseTime,
            currentTimeMs = baseTime + minutesToMs(271f)
        )
        assertEquals(TimelinePhase.AFTERGLOW, result.phase)
    }

    @Test
    fun `BASELINE after all phases complete`() {
        // total = 390 min
        val result = calculator.calculatePhase(
            substance, "Oral", baseTime,
            currentTimeMs = baseTime + minutesToMs(400f)
        )
        assertEquals(TimelinePhase.BASELINE, result.phase)
        assertEquals(1.0f, result.progressPercent, 0.01f)
    }

    // --- Edge cases ---

    @Test
    fun `BASELINE when ROA not found`() {
        val result = calculator.calculatePhase(
            substance, "Intravenous", baseTime,
            currentTimeMs = baseTime + minutesToMs(10f)
        )
        assertEquals(TimelinePhase.BASELINE, result.phase)
    }

    @Test
    fun `handles null duration fields gracefully`() {
        val nullDuration = DurationParameters(
            onset = null,
            comeup = null,
            peak = 60f,
            offset = null,
            afterglow = null,
            total = 60f
        )
        val nullRoa = Roa("Oral", null, nullDuration)
        val sub = Substance(id = "null-test", name = "Null Test", roas = listOf(nullRoa))

        // With onset=0, comeup=0, should jump straight to PEAK at t=0
        val result = calculator.calculatePhase(sub, "Oral", baseTime, baseTime)
        assertEquals(TimelinePhase.PEAK, result.phase)
    }

    @Test
    fun `handles zero-duration phases`() {
        val zeroDuration = DurationParameters(
            onset = 0f,
            comeup = 0f,
            peak = 60f,
            offset = 0f,
            afterglow = 0f,
            total = 60f
        )
        val zeroRoa = Roa("Oral", null, zeroDuration)
        val sub = Substance(id = "zero-test", name = "Zero Test", roas = listOf(zeroRoa))

        val result = calculator.calculatePhase(sub, "Oral", baseTime, baseTime + minutesToMs(30f))
        assertEquals(TimelinePhase.PEAK, result.phase)
        assertEquals(0.5f, result.progressPercent, 0.01f)
    }

    @Test
    fun `handles substance with no roas`() {
        val emptySubstance = Substance(id = "empty", name = "Empty", roas = emptyList())
        val result = calculator.calculatePhase(emptySubstance, "Oral", baseTime, baseTime)
        assertEquals(TimelinePhase.BASELINE, result.phase)
    }

    @Test
    fun `ROA name matching is case insensitive`() {
        val result = calculator.calculatePhase(
            substance, "oral", baseTime,
            currentTimeMs = baseTime + minutesToMs(15f)
        )
        assertEquals(TimelinePhase.ONSET, result.phase)
    }

    @Test
    fun `progress is clamped to 0-1 range`() {
        val result = calculator.calculatePhase(
            substance, "Oral", baseTime,
            currentTimeMs = baseTime + minutesToMs(29.9f) // almost end of onset
        )
        assertEquals(TimelinePhase.ONSET, result.phase)
        assert(result.progressPercent in 0f..1f) { "Progress ${result.progressPercent} not in [0, 1]" }
    }

    // --- Countdown tests ---

    @Test
    fun `calculateCountdown in ONSET predicts COMEUP and baseline correctly`() {
        // baseTime + 10 min (20 min remaining in onset, 380 min remaining in total 390 min)
        val countdown = calculator.calculateCountdown(
            substance, "Oral", baseTime,
            currentTimeMs = baseTime + minutesToMs(10f)
        )
        assertEquals(TimelinePhase.ONSET, countdown.currentPhase)
        assertEquals(20L, countdown.minutesToNextPhase)
        assertEquals(TimelinePhase.COMEUP, countdown.nextPhase)
        assertEquals(380L, countdown.minutesToBaseline)
    }

    @Test
    fun `calculateCountdown in PEAK predicts OFFSET correctly`() {
        // onset(30) + comeup(30) = 60. Peak lasts 120 min (until 180 min).
        // At 80 min: 180 - 80 = 100 min left in peak, 390 - 80 = 310 min to baseline
        val countdown = calculator.calculateCountdown(
            substance, "Oral", baseTime,
            currentTimeMs = baseTime + minutesToMs(80f)
        )
        assertEquals(TimelinePhase.PEAK, countdown.currentPhase)
        assertEquals(100L, countdown.minutesToNextPhase)
        assertEquals(TimelinePhase.OFFSET, countdown.nextPhase)
        assertEquals(310L, countdown.minutesToBaseline)
    }

    @Test
    fun `calculateCountdown at BASELINE returns zero remaining`() {
        val countdown = calculator.calculateCountdown(
            substance, "Oral", baseTime,
            currentTimeMs = baseTime + minutesToMs(400f)
        )
        assertEquals(TimelinePhase.BASELINE, countdown.currentPhase)
        assertEquals(null, countdown.minutesToNextPhase)
        assertEquals(0L, countdown.minutesToBaseline)
    }
}
