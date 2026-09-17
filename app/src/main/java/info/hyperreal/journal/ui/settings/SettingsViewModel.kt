package info.hyperreal.journal.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import info.hyperreal.journal.data.local.datastore.UserPreferencesRepository
import info.hyperreal.journal.domain.repository.IngestionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val ingestionRepository: IngestionRepository,
    private val gson: Gson
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = userPreferencesRepository.userPreferencesFlow
        .map { it.isDarkMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateIsDarkMode(enabled)
        }
    }

    fun exportData(context: Context) {
        viewModelScope.launch {
            val ingestions = ingestionRepository.getAllIngestions().first()
            val json = gson.toJson(ingestions)
            
            try {
                val file = File(context.cacheDir, "hyperreal_journal_export.json")
                file.writeText(json)
                
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Eksportuj dziennik"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
