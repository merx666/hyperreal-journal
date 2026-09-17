package info.hyperreal.journal.ui.substances

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.repository.SubstanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubstanceDetailViewModel @Inject constructor(
    private val repository: SubstanceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val substanceId: String = checkNotNull(savedStateHandle["substanceId"])

    private val _substance = MutableStateFlow<Substance?>(null)
    val substance: StateFlow<Substance?> = _substance.asStateFlow()

    init {
        viewModelScope.launch {
            _substance.value = repository.getSubstanceById(substanceId)
        }
    }
}
