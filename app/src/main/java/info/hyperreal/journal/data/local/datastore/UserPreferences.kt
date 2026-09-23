package info.hyperreal.journal.data.local.datastore

data class UserPreferences(
    val hasAcceptedDisclaimer: Boolean = false,
    val hasSeenWelcomeV2: Boolean = false,
    val isDarkMode: Boolean = true,
    val isSecurityLockEnabled: Boolean = false,
    val pinHash: String? = null,
    val widgetTheme: String = "EMERALD",
    val widgetDiscreteMode: Boolean = false,
    val widgetShowCountdown: Boolean = true
)
