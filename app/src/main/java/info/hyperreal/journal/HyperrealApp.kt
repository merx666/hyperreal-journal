package info.hyperreal.journal

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import info.hyperreal.journal.core.notification.NotificationHelper
import timber.log.Timber

@HiltAndroidApp
class HyperrealApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Create notification channels for reminders (required on Android 8+)
        NotificationHelper.createNotificationChannel(this)
    }
}
