package info.hyperreal.journal.ui.mixcalculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.domain.model.SubstanceInteraction
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.repository.InteractionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MixCalculatorUiState(
    val substances: List<Substance> = emptyList(),
    val substanceA: Substance? = null,
    val substanceB: Substance? = null,
    val interactionResult: SubstanceInteraction? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class MixCalculatorViewModel @Inject constructor(
    private val substanceRepository: SubstanceRepository,
    private val interactionRepository: InteractionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MixCalculatorUiState())
    val uiState: StateFlow<MixCalculatorUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val substances = substanceRepository.getAllSubstances().first().sortedBy { it.name }
            _uiState.update {
                it.copy(
                    substances = substances,
                    isLoading = false
                )
            }
        }
    }

    fun selectSubstanceA(substance: Substance) {
        _uiState.update { it.copy(substanceA = substance) }
        checkInteraction()
    }

    fun selectSubstanceB(substance: Substance) {
        _uiState.update { it.copy(substanceB = substance) }
        checkInteraction()
    }

    fun swapSubstances() {
        _uiState.update {
            it.copy(
                substanceA = it.substanceB,
                substanceB = it.substanceA
            )
        }
        checkInteraction()
    }

    private fun checkInteraction() {
        val subA = _uiState.value.substanceA?.name ?: return
        val subB = _uiState.value.substanceB?.name ?: return

        viewModelScope.launch {
            val interaction = interactionRepository.getInteraction(subA, subB)
            _uiState.update { it.copy(interactionResult = interaction) }
        }
    }
}
