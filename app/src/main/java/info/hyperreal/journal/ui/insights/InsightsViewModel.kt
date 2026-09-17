package info.hyperreal.journal.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.repository.IngestionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import info.hyperreal.journal.domain.repository.SubstanceRepository
import javax.inject.Inject

data class InsightsData(
    val totalIngestions: Int,
    val uniqueSubstances: Int,
    val topSubstance: String?
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val ingestionRepository: IngestionRepository,
    private val substanceRepository: SubstanceRepository
) : ViewModel() {

    val insights: StateFlow<InsightsData> = combine(
        ingestionRepository.getAllIngestions(),
        substanceRepository.getAllSubstances()
    ) { ingestions, substances ->
        val total = ingestions.size
        val unique = ingestions.distinctBy { it.substanceId }.size
        
        val counts = ingestions.groupingBy { it.substanceId }.eachCount()
        val topId = counts.maxByOrNull { it.value }?.key
        val topName = substances.find { it.id == topId }?.name ?: topId

        InsightsData(total, unique, topName)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InsightsData(0, 0, null)
        )
}
