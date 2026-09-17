package info.hyperreal.journal.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.security.MessageDigest
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
        val IS_SECURITY_LOCK_ENABLED = booleanPreferencesKey("is_security_lock_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val WIDGET_THEME = stringPreferencesKey("widget_theme")
        val WIDGET_DISCRETE_MODE = booleanPreferencesKey("widget_discrete_mode")
        val WIDGET_SHOW_COUNTDOWN = booleanPreferencesKey("widget_show_countdown")
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
            val isSecurityLockEnabled = preferences[PreferencesKeys.IS_SECURITY_LOCK_ENABLED] ?: false
            val pinHash = preferences[PreferencesKeys.PIN_HASH]
            val widgetTheme = preferences[PreferencesKeys.WIDGET_THEME] ?: "EMERALD"
            val widgetDiscreteMode = preferences[PreferencesKeys.WIDGET_DISCRETE_MODE] ?: false
            val widgetShowCountdown = preferences[PreferencesKeys.WIDGET_SHOW_COUNTDOWN] ?: true
            UserPreferences(
                hasAcceptedDisclaimer = hasAcceptedDisclaimer,
                isDarkMode = isDarkMode,
                isSecurityLockEnabled = isSecurityLockEnabled,
                pinHash = pinHash,
                widgetTheme = widgetTheme,
                widgetDiscreteMode = widgetDiscreteMode,
                widgetShowCountdown = widgetShowCountdown
            )
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

    suspend fun updateSecurityLock(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SECURITY_LOCK_ENABLED] = enabled
        }
    }

    suspend fun updateWidgetTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIDGET_THEME] = theme
        }
    }

    suspend fun updateWidgetDiscreteMode(discrete: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIDGET_DISCRETE_MODE] = discrete
        }
    }

    suspend fun updateWidgetShowCountdown(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIDGET_SHOW_COUNTDOWN] = show
        }
    }

    suspend fun setPin(pin: String) {
        val hash = hashPin(pin)
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PIN_HASH] = hash
            preferences[PreferencesKeys.IS_SECURITY_LOCK_ENABLED] = true
        }
    }

    suspend fun removePin() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PIN_HASH] = ""
            preferences[PreferencesKeys.IS_SECURITY_LOCK_ENABLED] = false
        }
    }

    fun verifyPin(pin: String, storedHash: String?): Boolean {
        if (storedHash.isNullOrBlank()) return false
        return hashPin(pin) == storedHash
    }

    companion object {
        fun hashPin(pin: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest("hyperreal_salt_$pin".toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
