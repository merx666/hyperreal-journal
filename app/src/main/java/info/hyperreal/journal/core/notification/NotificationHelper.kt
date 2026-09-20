package info.hyperreal.journal.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import info.hyperreal.journal.MainActivity
import info.hyperreal.journal.R
import javax.inject.Inject
import javax.inject.Singleton

const val CHANNEL_ID_REMINDERS = "hyperreal_reminders"

/**
 * Helper object that manages the reminder notification channel
 * and sends styled harm-reduction notifications.
 */
@Singleton
class NotificationHelper @Inject constructor() {

    companion object {
        /**
         * Must be called once at app startup (e.g., in Application.onCreate or MainActivity.onCreate)
         * to register the notification channel on Android 8+.
         */
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.notification_channel_name)
                val descriptionText = context.getString(R.string.notification_channel_desc)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID_REMINDERS, name, importance).apply {
                    description = descriptionText
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Sends a peak-phase reminder notification.
     *
     * @param context     Application context
     * @param notifId     Unique notification ID (use ingestionId.toInt() for uniqueness)
     * @param title       Notification title
     * @param body        Notification body text
     */
    fun sendReminder(context: Context, notifId: Int, title: String, body: String) {
        // Tap-to-open intent → brings user back to the journal
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notifId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted yet — silently swallow
        }
    }
}
