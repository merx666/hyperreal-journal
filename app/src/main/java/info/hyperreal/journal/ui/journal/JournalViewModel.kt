package info.hyperreal.journal.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.repository.IngestionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import info.hyperreal.journal.domain.usecase.TimelineCalculator
import info.hyperreal.journal.domain.usecase.TimelineStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class JournalEntry(
    val ingestion: Ingestion,
    val substance: Substance?,
    val timelineStatus: TimelineStatus?
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val ingestionRepository: IngestionRepository,
    private val substanceRepository: SubstanceRepository,
    private val timelineCalculator: TimelineCalculator
) : ViewModel() {

    val entries: StateFlow<List<JournalEntry>> = combine(
        ingestionRepository.getAllIngestions(),
        substanceRepository.getAllSubstances()
    ) { ingestions, substances ->
        ingestions.map { ingestion ->
            val sub = substances.find { it.id == ingestion.substanceId }
            val status = sub?.let { 
                timelineCalculator.calculatePhase(it, ingestion.roa, ingestion.timestamp) 
            }
            JournalEntry(ingestion, sub, status)
        }.sortedByDescending { it.ingestion.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
