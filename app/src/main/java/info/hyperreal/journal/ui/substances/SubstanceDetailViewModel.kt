package info.hyperreal.journal.ui.substances

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.model.SubstanceInteraction
import info.hyperreal.journal.domain.repository.InteractionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SubstanceDetailUiState {
    data object Loading : SubstanceDetailUiState
    data class Success(
        val substance: Substance,
        val interactions: List<SubstanceInteraction>
    ) : SubstanceDetailUiState
    data class Error(val message: String) : SubstanceDetailUiState
}

@HiltViewModel
class SubstanceDetailViewModel @Inject constructor(
    private val repository: SubstanceRepository,
    private val interactionRepository: InteractionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val substanceId: String = checkNotNull(savedStateHandle["substanceId"])

    private val _uiState = MutableStateFlow<SubstanceDetailUiState>(SubstanceDetailUiState.Loading)
    val uiState: StateFlow<SubstanceDetailUiState> = _uiState.asStateFlow()

    // Backward-compatibility properties
    private val _substance = MutableStateFlow<Substance?>(null)
    val substance: StateFlow<Substance?> = _substance.asStateFlow()

    private val _interactions = MutableStateFlow<List<SubstanceInteraction>>(emptyList())
    val interactions: StateFlow<List<SubstanceInteraction>> = _interactions.asStateFlow()

    init {
        loadSubstance()
    }

    fun retry() {
        loadSubstance()
    }

    private fun loadSubstance() {
        viewModelScope.launch {
            _uiState.value = SubstanceDetailUiState.Loading
            try {
                val sub = repository.getSubstanceById(substanceId)
                _substance.value = sub

                if (sub == null) {
                    _uiState.value = SubstanceDetailUiState.Error("Nie znaleziono substancji o ID: $substanceId")
                    _interactions.value = emptyList()
                    return@launch
                }

                // Load interactions from interactions_sin.json (matching name or aliases)
                val allInteractions = interactionRepository.getAllInteractions()
                val filtered = allInteractions.filter { interaction ->
                    interaction.substanceA.equals(sub.name, ignoreCase = true) ||
                    interaction.substanceB.equals(sub.name, ignoreCase = true) ||
                    sub.aliases.any { alias ->
                        interaction.substanceA.equals(alias, ignoreCase = true) ||
                        interaction.substanceB.equals(alias, ignoreCase = true)
                    }
                }

                _interactions.value = filtered
                _uiState.value = SubstanceDetailUiState.Success(sub, filtered)
            } catch (e: Exception) {
                _uiState.value = SubstanceDetailUiState.Error("Błąd podczas ładowania substancji: ${e.localizedMessage ?: "Nieznany błąd"}")
            }
        }
    }
}
