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

@HiltViewModel
class SubstanceDetailViewModel @Inject constructor(
    private val repository: SubstanceRepository,
    private val interactionRepository: InteractionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val substanceId: String = checkNotNull(savedStateHandle["substanceId"])

    private val _substance = MutableStateFlow<Substance?>(null)
    val substance: StateFlow<Substance?> = _substance.asStateFlow()

    private val _interactions = MutableStateFlow<List<SubstanceInteraction>>(emptyList())
    val interactions: StateFlow<List<SubstanceInteraction>> = _interactions.asStateFlow()

    init {
        viewModelScope.launch {
            val sub = repository.getSubstanceById(substanceId)
            _substance.value = sub
            
            // Load interactions from interactions_sin.json
            if (sub != null) {
                val allInteractions = interactionRepository.getAllInteractions()
                _interactions.value = allInteractions.filter {
                    it.substanceA.equals(sub.name, ignoreCase = true) ||
                    it.substanceB.equals(sub.name, ignoreCase = true)
                }
            }
        }
    }
}
