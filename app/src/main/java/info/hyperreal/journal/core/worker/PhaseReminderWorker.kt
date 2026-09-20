package info.hyperreal.journal.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import info.hyperreal.journal.R
import info.hyperreal.journal.core.notification.NotificationHelper

/**
 * WorkManager worker that fires a phase-based harm-reduction reminder.
 *
 * Input data keys:
 *   KEY_NOTIF_ID      — unique notification ID (Int)
 *   KEY_SUBSTANCE     — substance name (String)
 *   KEY_PHASE_LABEL   — phase label key, e.g. "peak" (String)
 */
@HiltWorker
class PhaseReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_NOTIF_ID = "notif_id"
        const val KEY_SUBSTANCE = "substance_name"
        const val KEY_PHASE_LABEL = "phase_label"
        const val PHASE_PEAK = "peak"
    }

    override suspend fun doWork(): Result {
        val notifId = inputData.getInt(KEY_NOTIF_ID, 0)
        val substanceName = inputData.getString(KEY_SUBSTANCE) ?: context.getString(R.string.app_name)
        val phaseLabel = inputData.getString(KEY_PHASE_LABEL) ?: PHASE_PEAK

        val title: String
        val body: String

        when (phaseLabel) {
            PHASE_PEAK -> {
                title = context.getString(R.string.notification_peak_title)
                body = context.getString(R.string.notification_peak_body, substanceName)
            }
            else -> {
                title = context.getString(R.string.notification_hydration_title)
                body = context.getString(R.string.notification_hydration_body, substanceName)
            }
        }

        notificationHelper.sendReminder(context, notifId, title, body)
        return Result.success()
    }
}
