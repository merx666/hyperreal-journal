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
}
