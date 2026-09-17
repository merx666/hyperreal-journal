package info.hyperreal.journal.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val HAS_ACCEPTED_DISCLAIMER = booleanPreferencesKey("has_accepted_disclaimer")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val hasAcceptedDisclaimer = preferences[PreferencesKeys.HAS_ACCEPTED_DISCLAIMER] ?: false
            val isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: true
            UserPreferences(hasAcceptedDisclaimer, isDarkMode)
        }

    suspend fun updateHasAcceptedDisclaimer(hasAccepted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_ACCEPTED_DISCLAIMER] = hasAccepted
        }
    }

    suspend fun updateIsDarkMode(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDarkMode
        }
    }
}
