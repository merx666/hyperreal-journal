package info.hyperreal.journal.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import info.hyperreal.journal.MainActivity
import info.hyperreal.journal.R
import info.hyperreal.journal.domain.repository.IngestionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import info.hyperreal.journal.domain.usecase.TimelineCalculator
import info.hyperreal.journal.domain.usecase.TimelinePhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

import info.hyperreal.journal.data.local.datastore.UserPreferencesRepository

class TimelineWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun ingestionRepository(): IngestionRepository
        fun substanceRepository(): SubstanceRepository
        fun timelineCalculator(): TimelineCalculator
        fun userPreferencesRepository(): UserPreferencesRepository
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val appContext = context.applicationContext
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    appContext,
                    WidgetEntryPoint::class.java
                )

                val userPrefs = entryPoint.userPreferencesRepository().userPreferencesFlow.first()
                val ingestions = entryPoint.ingestionRepository().getAllIngestions().first()
                val latestIngestion = ingestions.maxByOrNull { it.timestamp }

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_timeline)

                    // Open app on click
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                    if (latestIngestion != null) {
                        val substance = entryPoint.substanceRepository().getSubstanceById(latestIngestion.substanceId)
                        val now = System.currentTimeMillis()
                        val elapsedMs = (now - latestIngestion.timestamp).coerceAtLeast(0)
                        val elapsedMin = TimeUnit.MILLISECONDS.toMinutes(elapsedMs)
                        val elapsedHours = elapsedMin / 60
                        val elapsedRemMin = elapsedMin % 60

                        val status = substance?.let {
                            entryPoint.timelineCalculator().calculatePhase(it, latestIngestion.roa, latestIngestion.timestamp, now)
                        }

                        val phaseName = when (status?.phase) {
                            TimelinePhase.ONSET -> "Wejście (Onset)"
                            TimelinePhase.COMEUP -> "Wzrost (Comeup)"
                            TimelinePhase.PEAK -> "Szczyt (Peak)"
                            TimelinePhase.OFFSET -> "Zejście (Offset)"
                            TimelinePhase.AFTERGLOW -> "Powrót (Afterglow)"
                            TimelinePhase.BASELINE -> "Zakończone"
                            TimelinePhase.NOT_STARTED -> "Oczekuje"
                            null -> "Aktywny"
                        }

                        val timeElapsedText = if (elapsedHours > 0) {
                            "Od zażycia: ${elapsedHours}h ${elapsedRemMin}m"
                        } else {
                            "Od zażycia: ${elapsedRemMin}m"
                        }

                        val duration = substance?.roas?.find { it.name.equals(latestIngestion.roa, ignoreCase = true) }?.duration
                        val totalDurationMin = duration?.total ?: 360f
                        val progressPercent = ((elapsedMin.toFloat() / totalDurationMin) * 100).toInt().coerceIn(0, 100)

                        // Apply discrete mode (personalization)
                        val displayName = if (userPrefs.widgetDiscreteMode) {
                            "Aktywna sesja"
                        } else {
                            substance?.name ?: latestIngestion.substanceId
                        }

                        val displayDose = if (userPrefs.widgetDiscreteMode) {
                            "Faza działania: ${status?.phase?.name ?: "W toku"}"
                        } else {
                            "${latestIngestion.doseAmount} ${latestIngestion.doseUnit} • ${latestIngestion.roa}"
                        }

                        views.setTextViewText(R.id.widget_substance_name, displayName)
                        views.setTextViewText(R.id.widget_dose_info, displayDose)
                        views.setTextViewText(R.id.widget_phase_badge, phaseName)

                        if (userPrefs.widgetShowCountdown) {
                            views.setViewVisibility(R.id.widget_time_elapsed, android.view.View.VISIBLE)
                            views.setTextViewText(R.id.widget_time_elapsed, timeElapsedText)
                        } else {
                            views.setViewVisibility(R.id.widget_time_elapsed, android.view.View.GONE)
                        }

                        views.setProgressBar(R.id.widget_progress_bar, 100, progressPercent, false)
                    } else {
                        views.setTextViewText(R.id.widget_substance_name, "Brak aktywnej substancji")
                        views.setTextViewText(R.id.widget_dose_info, "Dziennik jest pusty")
                        views.setTextViewText(R.id.widget_phase_badge, "Brak wpisów")
                        views.setTextViewText(R.id.widget_time_elapsed, "Dotknij, aby dodać pierwsze przyjęcie")
                        views.setProgressBar(R.id.widget_progress_bar, 100, 0, false)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error updating TimelineWidget")
            }
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, TimelineWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val widgetManager = AppWidgetManager.getInstance(context)
            val ids = widgetManager.getAppWidgetIds(
                android.content.ComponentName(context, TimelineWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                context.sendBroadcast(intent)
            }
        }
    }
}
