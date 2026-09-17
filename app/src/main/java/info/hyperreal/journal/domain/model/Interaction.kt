package info.hyperreal.journal.domain.model

data class SubstanceInteraction(
    val substanceA: String,
    val substanceB: String,
    val status: InteractionStatus,
    val note: String? = null
)

enum class InteractionStatus(val displayName: String) {
    LOW_RISK_SYNERGY("Low Risk & Synergy"),
    LOW_RISK_NO_SYNERGY("Low Risk & No Synergy"),
    LOW_RISK_DECREASE("Low Risk & Decrease"),
    CAUTION("Caution"),
    UNSAFE("Unsafe"),
    DANGEROUS("Dangerous"),
    UNKNOWN("Unknown");
    
    companion object {
        fun fromString(status: String): InteractionStatus {
            return entries.find { it.displayName.equals(status, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
