package info.hyperreal.journal.data.local.datastore

data class UserPreferences(
    val hasAcceptedDisclaimer: Boolean = false,
    val isDarkMode: Boolean = true // True by default for dark UI aesthetic
)
