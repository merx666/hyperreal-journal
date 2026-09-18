package info.hyperreal.journal.ui.matrix

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.domain.model.InteractionStatus
import info.hyperreal.journal.domain.model.SubstanceInteraction
import info.hyperreal.journal.domain.repository.InteractionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatrixExplorerUiState(
    val isLoading: Boolean = true,
    val allInteractions: List<SubstanceInteraction> = emptyList(),
    val matrixSubstances: List<String> = emptyList(),
    val selectedSubstance: String? = null,
    val selectedStatusFilter: InteractionStatus? = null,
    val searchQuery: String = "",
    val filteredInteractions: List<SubstanceInteraction> = emptyList(),
    val selectedInteractionForDetail: SubstanceInteraction? = null,
    val statusCounts: Map<InteractionStatus, Int> = emptyMap()
)

private data class FilterParams(
    val selectedSubstance: String? = null,
    val selectedStatus: InteractionStatus? = null,
    val query: String = "",
    val detail: SubstanceInteraction? = null
)

@HiltViewModel
class MatrixExplorerViewModel @Inject constructor(
    private val interactionRepository: InteractionRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _allInteractions = MutableStateFlow<List<SubstanceInteraction>>(emptyList())
    private val _matrixSubstances = MutableStateFlow<List<String>>(emptyList())
    private val _filterParams = MutableStateFlow(FilterParams())

    val uiState: StateFlow<MatrixExplorerUiState> = combine(
        _isLoading,
        _allInteractions,
        _matrixSubstances,
        _filterParams
    ) { loading, allInteractions, substances, filters ->
        val counts = InteractionStatus.entries.associateWith { status ->
            allInteractions.count { it.status == status }
        }

        val filtered = allInteractions.filter { interaction ->
            // 1. Filter by selected substance (if any)
            val matchesSubstance = if (filters.selectedSubstance.isNullOrBlank()) {
                true
            } else {
                interaction.substanceA.equals(filters.selectedSubstance, ignoreCase = true) ||
                interaction.substanceB.equals(filters.selectedSubstance, ignoreCase = true)
            }

            // 2. Filter by status (if any)
            val matchesStatus = if (filters.selectedStatus == null) {
                true
            } else {
                interaction.status == filters.selectedStatus
            }

            // 3. Filter by search query
            val matchesQuery = if (filters.query.isBlank()) {
                true
            } else {
                interaction.substanceA.contains(filters.query, ignoreCase = true) ||
                interaction.substanceB.contains(filters.query, ignoreCase = true) ||
                (interaction.note?.contains(filters.query, ignoreCase = true) == true)
            }

            matchesSubstance && matchesStatus && matchesQuery
        }.sortedWith(compareBy(
            { getSeverityRank(it.status) },
            { it.substanceA },
            { it.substanceB }
        ))

        MatrixExplorerUiState(
            isLoading = loading,
            allInteractions = allInteractions,
            matrixSubstances = substances,
            selectedSubstance = filters.selectedSubstance,
            selectedStatusFilter = filters.selectedStatus,
            searchQuery = filters.query,
            filteredInteractions = filtered,
            selectedInteractionForDetail = filters.detail,
            statusCounts = counts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MatrixExplorerUiState()
    )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            val interactions = interactionRepository.getAllInteractions()
            val substances = interactionRepository.getMatrixSubstances()
            _allInteractions.value = interactions
            _matrixSubstances.value = substances
            _isLoading.value = false
        }
    }

    fun selectSubstance(substance: String?) {
        _filterParams.update {
            it.copy(selectedSubstance = if (it.selectedSubstance == substance) null else substance)
        }
    }

    fun setStatusFilter(status: InteractionStatus?) {
        _filterParams.update {
            it.copy(selectedStatus = if (it.selectedStatus == status) null else status)
        }
    }

    fun setSearchQuery(query: String) {
        _filterParams.update {
            it.copy(query = query)
        }
    }

    fun showInteractionDetail(interaction: SubstanceInteraction?) {
        _filterParams.update {
            it.copy(detail = interaction)
        }
    }

    fun resetFilters() {
        _filterParams.update {
            FilterParams()
        }
    }

    private fun getSeverityRank(status: InteractionStatus): Int {
        return when (status) {
            InteractionStatus.DANGEROUS -> 0
            InteractionStatus.UNSAFE -> 1
            InteractionStatus.CAUTION -> 2
            InteractionStatus.LOW_RISK_SYNERGY -> 3
            InteractionStatus.LOW_RISK_DECREASE -> 4
            InteractionStatus.LOW_RISK_NO_SYNERGY -> 5
            InteractionStatus.UNKNOWN -> 6
        }
    }
}
