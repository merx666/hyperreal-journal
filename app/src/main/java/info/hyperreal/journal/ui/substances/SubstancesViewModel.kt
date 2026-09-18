package info.hyperreal.journal.ui.substances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.domain.model.DurationParameters
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.repository.SubstanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubstancesViewModel @Inject constructor(
    private val repository: SubstanceRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    val currentQuery: StateFlow<String> = searchQuery.asStateFlow()

    private val selectedClass = MutableStateFlow<String?>(null)
    val selectedClassFilter: StateFlow<String?> = selectedClass.asStateFlow()

    val availableClasses: StateFlow<List<String>> = repository.getAllSubstances()
        .map { list -> list.flatMap { it.classes }.filter { it.isNotBlank() }.distinct().sorted() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val substances: StateFlow<List<Substance>> = combine(
        repository.getAllSubstances(),
        searchQuery,
        selectedClass
    ) { allSubstances, query, cls ->
        allSubstances.filter { sub ->
            val matchesQuery = query.isBlank() ||
                    sub.name.contains(query, ignoreCase = true) ||
                    sub.aliases.any { it.contains(query, ignoreCase = true) }
            val matchesClass = cls == null || sub.classes.any { it.equals(cls, ignoreCase = true) }
            matchesQuery && matchesClass
        }.sortedBy { it.name }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onClassFilterSelected(cls: String?) {
        selectedClass.value = cls
    }

    fun addCustomSubstance(
        name: String,
        roaName: String,
        duration: DurationParameters,
        onCreated: (Substance) -> Unit = {}
    ) {
        viewModelScope.launch {
            val created = repository.addCustomSubstance(name, roaName, duration)
            onCreated(created)
        }
    }

    fun deleteCustomSubstance(id: String) {
        viewModelScope.launch {
            repository.deleteCustomSubstance(id)
        }
    }
}
