package info.hyperreal.journal.ui.substances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.repository.SubstanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SubstancesViewModel @Inject constructor(
    repository: SubstanceRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val substances: StateFlow<List<Substance>> = combine(
        repository.getAllSubstances(),
        searchQuery
    ) { allSubstances, query ->
        if (query.isBlank()) {
            allSubstances
        } else {
            allSubstances.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.aliases.any { alias -> alias.contains(query, ignoreCase = true) }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }
}
