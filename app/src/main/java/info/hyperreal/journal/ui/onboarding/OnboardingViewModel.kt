package info.hyperreal.journal.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.data.local.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val hasAcceptedDisclaimer = userPreferencesRepository.userPreferencesFlow
        .map { it.hasAcceptedDisclaimer }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // null means loading
        )

    fun acceptDisclaimer() {
        viewModelScope.launch {
            userPreferencesRepository.updateHasAcceptedDisclaimer(true)
        }
    }
}
