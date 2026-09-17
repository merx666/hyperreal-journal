package info.hyperreal.journal.ui.addingestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.model.Roa
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.repository.IngestionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import info.hyperreal.journal.domain.usecase.InteractionChecker
import info.hyperreal.journal.domain.usecase.InteractionWarning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddIngestionViewModel @Inject constructor(
    private val substanceRepository: SubstanceRepository,
    private val ingestionRepository: IngestionRepository,
    private val interactionChecker: InteractionChecker
) : ViewModel() {

    val allSubstances: StateFlow<List<Substance>> = substanceRepository.getAllSubstances()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedSubstance = MutableStateFlow<Substance?>(null)
    val selectedSubstance: StateFlow<Substance?> = _selectedSubstance.asStateFlow()

    private val _selectedRoa = MutableStateFlow<Roa?>(null)
    val selectedRoa: StateFlow<Roa?> = _selectedRoa.asStateFlow()

    private val _doseAmount = MutableStateFlow<Float?>(null)
    val doseAmount: StateFlow<Float?> = _doseAmount.asStateFlow()

    private val _ingestionTime = MutableStateFlow<Long>(System.currentTimeMillis())
    val ingestionTime: StateFlow<Long> = _ingestionTime.asStateFlow()

    private val _interactionWarnings = MutableStateFlow<List<InteractionWarning>>(emptyList())
    val interactionWarnings: StateFlow<List<InteractionWarning>> = _interactionWarnings.asStateFlow()

    fun selectSubstance(substance: Substance) {
        _selectedSubstance.value = substance
        _selectedRoa.value = null // reset subsequent steps
        _doseAmount.value = null
    }

    fun selectRoa(roa: Roa) {
        _selectedRoa.value = roa
    }

    fun setDoseAmount(amount: Float) {
        _doseAmount.value = amount
    }

    fun setIngestionTime(timeMs: Long) {
        _ingestionTime.value = timeMs
    }

    fun checkInteractions() {
        val newSubst = _selectedSubstance.value ?: return
        viewModelScope.launch {
            val allSubstances = substanceRepository.getAllSubstances().first()
            val recentIngestions = ingestionRepository.getAllIngestions().first()
            val warnings = interactionChecker.checkInteractions(newSubst, recentIngestions, allSubstances)
            _interactionWarnings.value = warnings
        }
    }

    fun saveIngestion(onComplete: () -> Unit) {
        val sub = _selectedSubstance.value ?: return
        val roa = _selectedRoa.value ?: return
        val amount = _doseAmount.value ?: return
        val time = _ingestionTime.value

        viewModelScope.launch {
            val ingestion = Ingestion(
                id = 0,
                substanceId = sub.id,
                roa = roa.name,
                doseAmount = amount,
                doseUnit = roa.dose?.units ?: "mg",
                timestamp = time,
                notes = ""
            )
            ingestionRepository.insertIngestion(ingestion)
            onComplete()
        }
    }
}
