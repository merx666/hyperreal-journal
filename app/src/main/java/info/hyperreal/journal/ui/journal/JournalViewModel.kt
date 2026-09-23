package info.hyperreal.journal.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.domain.model.CheckIn
import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.model.ShulginRating
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.model.SubstanceInteraction
import info.hyperreal.journal.domain.repository.CheckInRepository
import info.hyperreal.journal.domain.repository.IngestionRepository
import info.hyperreal.journal.domain.repository.InteractionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import info.hyperreal.journal.domain.usecase.PhaseCountdown
import info.hyperreal.journal.domain.usecase.TimelineCalculator
import info.hyperreal.journal.domain.usecase.TimelinePhase
import info.hyperreal.journal.domain.usecase.TimelineStatus
import info.hyperreal.journal.core.worker.PhaseReminderWorker
import info.hyperreal.journal.core.worker.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class JournalFilter {
    ACTIVE_ONLY,
    COMPLETED_ONLY,
    ALL
}

data class ActiveEntryInfo(
    val entry: JournalEntry,
    val countdown: PhaseCountdown,
    val progressPercent: Float
)

data class JournalCounts(
    val active: Int = 0,
    val completed: Int = 0,
    val all: Int = 0
)

data class JournalEntry(
    val ingestion: Ingestion,
    val substance: Substance?,
    val timelineStatus: TimelineStatus?,
    val checkIns: List<CheckIn> = emptyList()
) {
    val isArchived: Boolean
        get() = ingestion.experienceId == "ARCHIVED" || ingestion.experienceId == "COMPLETED"

    val isActive: Boolean
        get() {
            if (isArchived) return false
            val phase = timelineStatus?.phase ?: return false
            return phase != TimelinePhase.BASELINE && phase != TimelinePhase.NOT_STARTED
        }
}

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val ingestionRepository: IngestionRepository,
    private val substanceRepository: SubstanceRepository,
    private val timelineCalculator: TimelineCalculator,
    private val checkInRepository: CheckInRepository,
    private val reminderScheduler: ReminderScheduler,
    private val interactionRepository: InteractionRepository? = null
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Default to ACTIVE_ONLY: Main screen displays exclusively active ongoing sessions
    private val _filter = MutableStateFlow(JournalFilter.ACTIVE_ONLY)
    val filter: StateFlow<JournalFilter> = _filter.asStateFlow()

    private val scheduledReminderIds = mutableSetOf<Long>()

    private val rawEntries: StateFlow<List<JournalEntry>> = combine(
        ingestionRepository.getAllIngestions(),
        substanceRepository.getAllSubstances(),
        checkInRepository.getAllCheckIns()
    ) { ingestions, substances, checkIns ->
        val now = System.currentTimeMillis()
        val checkInsByIngestion = checkIns.groupBy { it.ingestionId }
        ingestions.map { ingestion ->
            val sub = substances.find { it.id == ingestion.substanceId }
            val status = sub?.let { 
                timelineCalculator.calculatePhase(it, ingestion.roa, ingestion.timestamp, now) 
            }
            val entryCheckIns = checkInsByIngestion[ingestion.id] ?: emptyList()
            JournalEntry(ingestion, sub, status, entryCheckIns)
        }.sortedByDescending { it.ingestion.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Summary counts for tabs (Aktywne, Zakończone, Wszystkie)
    val counts: StateFlow<JournalCounts> = rawEntries.map { list ->
        val active = list.count { it.isActive }
        val completed = list.size - active
        JournalCounts(active = active, completed = completed, all = list.size)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JournalCounts()
    )

    // Filtered entries for the main list based on current filter & search query
    val entries: StateFlow<List<JournalEntry>> = combine(
        rawEntries,
        _searchQuery,
        _filter
    ) { allEntries, query, currentFilter ->
        allEntries.filter { entry ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                val subName = entry.substance?.name ?: entry.ingestion.substanceId
                val notes = entry.ingestion.notes ?: ""
                subName.contains(query, ignoreCase = true) || notes.contains(query, ignoreCase = true)
            }

            val matchesFilter = when (currentFilter) {
                JournalFilter.ACTIVE_ONLY -> entry.isActive
                JournalFilter.COMPLETED_ONLY -> !entry.isActive
                JournalFilter.ALL -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active entries for live dashboard card & countdowns
    val activeEntries: StateFlow<List<ActiveEntryInfo>> = rawEntries.map { allEntries ->
        val now = System.currentTimeMillis()
        allEntries.filter { it.isActive }.mapNotNull { entry ->
            val sub = entry.substance ?: return@mapNotNull null
            val status = entry.timelineStatus ?: return@mapNotNull null
            val countdown = timelineCalculator.calculateCountdown(sub, entry.ingestion.roa, entry.ingestion.timestamp, now)
            ActiveEntryInfo(
                entry = entry,
                countdown = countdown,
                progressPercent = status.progressPercent
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active mix interactions between simultaneously active substances
    private val _activeMixInteractions = MutableStateFlow<List<SubstanceInteraction>>(emptyList())
    val activeMixInteractions: StateFlow<List<SubstanceInteraction>> = _activeMixInteractions.asStateFlow()

    init {
        viewModelScope.launch {
            activeEntries.collect { activeList ->
                val distinctSubstances = activeList.mapNotNull { it.entry.substance?.name }.distinct()
                if (distinctSubstances.size >= 2 && interactionRepository != null) {
                    val list = mutableListOf<SubstanceInteraction>()
                    for (i in 0 until distinctSubstances.size) {
                        for (j in i + 1 until distinctSubstances.size) {
                            val interaction = interactionRepository.getInteraction(distinctSubstances[i], distinctSubstances[j])
                            if (interaction != null) {
                                list.add(interaction)
                            }
                        }
                    }
                    _activeMixInteractions.value = list
                } else {
                    _activeMixInteractions.value = emptyList()
                }

                // Schedule peak reminders for active ingestions
                schedulePeakReminders(activeList)
            }
        }
    }

    /**
     * For each active ingestion that is currently in ONSET or COMEUP,
     * schedules a WorkManager notification to fire 80% into the Peak phase.
     * Already-past or already-PEAK entries are ignored to avoid duplicate spamming.
     * Cancels reminders for entries that are no longer active.
     */
    private fun schedulePeakReminders(activeList: List<ActiveEntryInfo>) {
        val currentActiveIds = activeList.map { it.entry.ingestion.id }.toSet()
        val removedIds = (scheduledReminderIds - currentActiveIds).toList()
        removedIds.forEach { id ->
            reminderScheduler.cancelReminder(id)
            scheduledReminderIds.remove(id)
        }

        val now = System.currentTimeMillis()
        activeList.forEach { info ->
            val phase = info.entry.timelineStatus?.phase ?: return@forEach
            if (phase != TimelinePhase.ONSET && phase != TimelinePhase.COMEUP) {
                return@forEach
            }

            val sub = info.entry.substance ?: return@forEach
            val ingestion = info.entry.ingestion
            val roa = sub.roas.find { it.name.equals(ingestion.roa, ignoreCase = true) }
            val duration = roa?.duration ?: return@forEach

            val onset = ((duration.onset ?: 0f) * 60_000L).toLong()
            val comeup = ((duration.comeup ?: 0f) * 60_000L).toLong()
            val peak = ((duration.peak ?: 0f) * 60_000L).toLong()

            // Peak starts at: ingestionTime + onset + comeup
            val peakStartMs = ingestion.timestamp + onset + comeup
            // Fire reminder 80% into peak
            val reminderMs = peakStartMs + (peak * 80L / 100L)

            // Only schedule if the reminder hasn't already passed
            if (reminderMs > now) {
                reminderScheduler.scheduleReminder(
                    ingestionId = ingestion.id,
                    substanceName = sub.name,
                    phaseLabel = PhaseReminderWorker.PHASE_PEAK,
                    triggerAtMs = reminderMs
                )
                scheduledReminderIds.add(ingestion.id)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: JournalFilter) {
        _filter.value = filter
    }

    fun archiveSession(ingestion: Ingestion) {
        viewModelScope.launch {
            reminderScheduler.cancelReminder(ingestion.id)
            scheduledReminderIds.remove(ingestion.id)
            ingestionRepository.updateIngestion(ingestion.copy(experienceId = "ARCHIVED"))
        }
    }

    fun restoreSession(ingestion: Ingestion) {
        viewModelScope.launch {
            ingestionRepository.updateIngestion(ingestion.copy(experienceId = null))
        }
    }

    fun updateIngestion(ingestion: Ingestion) {
        viewModelScope.launch {
            ingestionRepository.updateIngestion(ingestion)
        }
    }

    fun deleteIngestion(ingestion: Ingestion) {
        viewModelScope.launch {
            reminderScheduler.cancelReminder(ingestion.id)
            scheduledReminderIds.remove(ingestion.id)
            ingestionRepository.deleteIngestion(ingestion)
        }
    }

    fun addCheckIn(
        ingestionId: Long,
        phase: TimelinePhase,
        rating: ShulginRating,
        notes: String?,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            checkInRepository.insertCheckIn(
                CheckIn(
                    ingestionId = ingestionId,
                    timestamp = timestamp,
                    phase = phase,
                    shulginRating = rating,
                    notes = notes
                )
            )
        }
    }

    fun deleteCheckIn(id: Long) {
        viewModelScope.launch {
            checkInRepository.deleteCheckIn(id)
        }
    }
}
