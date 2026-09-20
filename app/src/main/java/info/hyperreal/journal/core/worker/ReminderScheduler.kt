package info.hyperreal.journal.core.worker

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and cancels phase-based WorkManager reminders.
 *
 * Each ingestion can have up to one active reminder tagged as
 * "reminder_<ingestionId>". Re-scheduling replaces the old one.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    private val workManager: WorkManager
) {

    /**
     * Schedules a [PhaseReminderWorker] to fire at [triggerAtMs] (epoch millis).
     *
     * @param ingestionId   Unique ingestion identifier (used as work name + notif ID)
     * @param substanceName Display name of the substance
     * @param phaseLabel    Phase key (e.g. [PhaseReminderWorker.PHASE_PEAK])
     * @param triggerAtMs   Absolute time in ms when the notification should fire
     */
    fun scheduleReminder(
        ingestionId: Long,
        substanceName: String,
        phaseLabel: String,
        triggerAtMs: Long
    ) {
        val delayMs = (triggerAtMs - System.currentTimeMillis()).coerceAtLeast(0)

        val inputData = Data.Builder()
            .putInt(PhaseReminderWorker.KEY_NOTIF_ID, ingestionId.toInt())
            .putString(PhaseReminderWorker.KEY_SUBSTANCE, substanceName)
            .putString(PhaseReminderWorker.KEY_PHASE_LABEL, phaseLabel)
            .build()

        val request = OneTimeWorkRequestBuilder<PhaseReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("reminder_$ingestionId")
            .build()

        workManager.enqueueUniqueWork(
            "reminder_$ingestionId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /**
     * Cancels any pending reminder for the given ingestion.
     */
    fun cancelReminder(ingestionId: Long) {
        workManager.cancelUniqueWork("reminder_$ingestionId")
    }
}
