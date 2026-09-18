package info.hyperreal.journal.ui.addingestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.data.local.datastore.UserPreferencesRepository
import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.model.Roa
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.model.ToleranceStatus
import info.hyperreal.journal.domain.repository.IngestionRepository
import info.hyperreal.journal.domain.repository.InteractionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import info.hyperreal.journal.domain.usecase.InteractionChecker
import info.hyperreal.journal.domain.usecase.InteractionWarning
import info.hyperreal.journal.domain.usecase.ToleranceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddIngestionViewModel @Inject constructor(
    private val substanceRepository: SubstanceRepository,
    private val ingestionRepository: IngestionRepository,
    private val interactionRepository: InteractionRepository,
    private val interactionChecker: InteractionChecker,
    private val toleranceCalculator: ToleranceCalculator,
    private val userPreferencesRepository: UserPreferencesRepository
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

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _interactionWarnings = MutableStateFlow<List<InteractionWarning>>(emptyList())
    val interactionWarnings: StateFlow<List<InteractionWarning>> = _interactionWarnings.asStateFlow()

    private val _toleranceStatus = MutableStateFlow<ToleranceStatus?>(null)
    val toleranceStatus: StateFlow<ToleranceStatus?> = _toleranceStatus.asStateFlow()

    fun selectSubstance(substance: Substance) {
        _selectedSubstance.value = substance
        _selectedRoa.value = null // reset subsequent steps
        _doseAmount.value = null
        checkTolerance(substance.id)
    }

    fun checkTolerance(substanceId: String? = _selectedSubstance.value?.id) {
        if (substanceId == null) {
            _toleranceStatus.value = null
            return
        }
        viewModelScope.launch {
            val ingestions = ingestionRepository.getAllIngestions().first()
            val overrides = userPreferencesRepository.toleranceOverridesFlow.first()
            val status = toleranceCalculator.getStatusForSubstance(
                substanceId = substanceId,
                ingestions = ingestions,
                manualOverrides = overrides,
                currentTimeMs = System.currentTimeMillis()
            )
            _toleranceStatus.value = status
        }
    }

    fun selectRoa(roa: Roa) {
        _selectedRoa.value = roa
    }

    fun setDoseAmount(amount: Float, calculatorNote: String? = null) {
        _doseAmount.value = amount
        if (!calculatorNote.isNullOrBlank()) {
            val current = _notes.value
            if (current.isBlank()) {
                _notes.value = calculatorNote
            } else if (!current.contains(calculatorNote)) {
                _notes.value = "$current\n$calculatorNote"
            }
        }
    }

    fun setIngestionTime(timeMs: Long) {
        _ingestionTime.value = timeMs
    }

    fun setNotes(notes: String) {
        _notes.value = notes
    }

    fun checkInteractions() {
        val newSubst = _selectedSubstance.value ?: return
        viewModelScope.launch {
            val allSubstances = substanceRepository.getAllSubstances().first()
            val recentIngestions = ingestionRepository.getAllIngestions().first()
            val allSinInteractions = interactionRepository.getAllInteractions()
            val warnings = interactionChecker.checkInteractions(
                newSubstance = newSubst,
                recentIngestions = recentIngestions,
                knownSubstances = allSubstances,
                sinInteractions = allSinInteractions
            )
            _interactionWarnings.value = warnings
        }
    }

    fun saveIngestion(onComplete: () -> Unit) {
        val sub = _selectedSubstance.value ?: return
        val roa = _selectedRoa.value ?: return
        val amount = _doseAmount.value ?: return
        val time = _ingestionTime.value
        val note = _notes.value

        viewModelScope.launch {
            val ingestion = Ingestion(
                id = 0,
                substanceId = sub.id,
                roa = roa.name,
                doseAmount = amount,
                doseUnit = roa.dose?.units ?: "mg",
                timestamp = time,
                notes = note
            )
            ingestionRepository.insertIngestion(ingestion)
            onComplete()
        }
    }
}
