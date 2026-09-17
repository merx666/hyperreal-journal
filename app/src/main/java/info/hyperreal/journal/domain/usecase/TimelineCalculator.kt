package info.hyperreal.journal.domain.usecase

import info.hyperreal.journal.domain.model.Roa
import info.hyperreal.journal.domain.model.Substance
import javax.inject.Inject

enum class TimelinePhase {
    NOT_STARTED,
    ONSET,
    COMEUP,
    PEAK,
    OFFSET,
    AFTERGLOW,
    BASELINE
}

data class TimelineStatus(
    val phase: TimelinePhase,
    val progressPercent: Float // 0.0 to 1.0 within the current phase
)

data class PhaseCountdown(
    val currentPhase: TimelinePhase,
    val minutesToNextPhase: Long?,
    val nextPhase: TimelinePhase?,
    val minutesToBaseline: Long?
)

class TimelineCalculator @Inject constructor() {

    fun calculatePhase(
        substance: Substance,
        roaName: String,
        ingestionTimeMs: Long,
        currentTimeMs: Long = System.currentTimeMillis()
    ): TimelineStatus {
        val roa = substance.roas.find { it.name.equals(roaName, ignoreCase = true) }
        val duration = roa?.duration ?: return TimelineStatus(TimelinePhase.BASELINE, 1.0f)

        // DurationParameters stores values in MINUTES, so elapsed must also be in minutes
        val elapsedMinutes = (currentTimeMs - ingestionTimeMs) / (1000f * 60f)

        if (elapsedMinutes < 0) return TimelineStatus(TimelinePhase.NOT_STARTED, 0f)

        var currentThreshold = 0f

        // Onset
        val onset = duration.onset ?: 0f
        if (elapsedMinutes < currentThreshold + onset) {
            val progress = if (onset > 0) (elapsedMinutes - currentThreshold) / onset else 1f
            return TimelineStatus(TimelinePhase.ONSET, progress.coerceIn(0f, 1f))
        }
        currentThreshold += onset

        // Comeup
        val comeup = duration.comeup ?: 0f
        if (elapsedMinutes < currentThreshold + comeup) {
            val progress = if (comeup > 0) (elapsedMinutes - currentThreshold) / comeup else 1f
            return TimelineStatus(TimelinePhase.COMEUP, progress.coerceIn(0f, 1f))
        }
        currentThreshold += comeup

        // Peak
        val peak = duration.peak ?: 0f
        if (elapsedMinutes < currentThreshold + peak) {
            val progress = if (peak > 0) (elapsedMinutes - currentThreshold) / peak else 1f
            return TimelineStatus(TimelinePhase.PEAK, progress.coerceIn(0f, 1f))
        }
        currentThreshold += peak

        // Offset
        val offset = duration.offset ?: 0f
        if (elapsedMinutes < currentThreshold + offset) {
            val progress = if (offset > 0) (elapsedMinutes - currentThreshold) / offset else 1f
            return TimelineStatus(TimelinePhase.OFFSET, progress.coerceIn(0f, 1f))
        }
        currentThreshold += offset

        // Afterglow
        val afterglow = duration.afterglow ?: 0f
        if (elapsedMinutes < currentThreshold + afterglow) {
            val progress = if (afterglow > 0) (elapsedMinutes - currentThreshold) / afterglow else 1f
            return TimelineStatus(TimelinePhase.AFTERGLOW, progress.coerceIn(0f, 1f))
        }

        return TimelineStatus(TimelinePhase.BASELINE, 1.0f)
    }

    fun calculateCountdown(
        substance: Substance,
        roaName: String,
        ingestionTimeMs: Long,
        currentTimeMs: Long = System.currentTimeMillis()
    ): PhaseCountdown {
        val roa = substance.roas.find { it.name.equals(roaName, ignoreCase = true) }
        val duration = roa?.duration ?: return PhaseCountdown(TimelinePhase.BASELINE, null, null, 0L)

        val elapsedMinutes = (currentTimeMs - ingestionTimeMs) / (1000f * 60f)
        val onset = duration.onset ?: 0f
        val comeup = duration.comeup ?: 0f
        val peak = duration.peak ?: 0f
        val offset = duration.offset ?: 0f
        val afterglow = duration.afterglow ?: 0f
        val total = duration.total ?: (onset + comeup + peak + offset + afterglow)

        if (elapsedMinutes < 0) {
            val toStart = (-elapsedMinutes).toLong().coerceAtLeast(1)
            val toBase = (total + toStart).toLong()
            return PhaseCountdown(TimelinePhase.NOT_STARTED, toStart, TimelinePhase.ONSET, toBase)
        }

        val tOnset = onset
        val tComeup = tOnset + comeup
        val tPeak = tComeup + peak
        val tOffset = tPeak + offset
        val tAfterglow = tOffset + afterglow

        val minToBaseline = (total - elapsedMinutes).toLong().coerceAtLeast(0)

        return when {
            elapsedMinutes < tOnset -> {
                val toNext = (tOnset - elapsedMinutes).toLong().coerceAtLeast(1)
                PhaseCountdown(TimelinePhase.ONSET, toNext, TimelinePhase.COMEUP, minToBaseline)
            }
            elapsedMinutes < tComeup -> {
                val toNext = (tComeup - elapsedMinutes).toLong().coerceAtLeast(1)
                PhaseCountdown(TimelinePhase.COMEUP, toNext, TimelinePhase.PEAK, minToBaseline)
            }
            elapsedMinutes < tPeak -> {
                val toNext = (tPeak - elapsedMinutes).toLong().coerceAtLeast(1)
                PhaseCountdown(TimelinePhase.PEAK, toNext, TimelinePhase.OFFSET, minToBaseline)
            }
            elapsedMinutes < tOffset -> {
                val toNext = (tOffset - elapsedMinutes).toLong().coerceAtLeast(1)
                PhaseCountdown(TimelinePhase.OFFSET, toNext, TimelinePhase.AFTERGLOW, minToBaseline)
            }
            elapsedMinutes < tAfterglow -> {
                val toNext = (tAfterglow - elapsedMinutes).toLong().coerceAtLeast(1)
                PhaseCountdown(TimelinePhase.AFTERGLOW, toNext, TimelinePhase.BASELINE, minToBaseline)
            }
            else -> {
                PhaseCountdown(TimelinePhase.BASELINE, null, null, 0L)
            }
        }
    }
}
