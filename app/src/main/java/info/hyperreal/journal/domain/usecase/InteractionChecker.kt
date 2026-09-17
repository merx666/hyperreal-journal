package info.hyperreal.journal.domain.usecase

import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.model.Substance
import javax.inject.Inject

data class InteractionWarning(
    val pastIngestion: Ingestion,
    val pastSubstanceName: String,
    val status: String,
    val notes: String?
)

class InteractionChecker @Inject constructor() {

    fun checkInteractions(
        newSubstance: Substance,
        recentIngestions: List<Ingestion>,
        knownSubstances: List<Substance>
    ): List<InteractionWarning> {
        val warnings = mutableListOf<InteractionWarning>()

        for (pastIngestion in recentIngestions) {
            val pastSubstance = knownSubstances.find { it.id == pastIngestion.substanceId }
            if (pastSubstance != null) {
                
                // Check if new substance defines an interaction with the past substance
                val forwardInteraction = newSubstance.interactions.find { it.substanceId == pastSubstance.id }
                if (forwardInteraction != null && isWarningStatus(forwardInteraction.status)) {
                    warnings.add(
                        InteractionWarning(
                            pastIngestion = pastIngestion,
                            pastSubstanceName = pastSubstance.name,
                            status = forwardInteraction.status,
                            notes = forwardInteraction.notes
                        )
                    )
                    continue
                }

                // Check if past substance defines an interaction with the new substance (bidirectional)
                val backwardInteraction = pastSubstance.interactions.find { it.substanceId == newSubstance.id }
                if (backwardInteraction != null && isWarningStatus(backwardInteraction.status)) {
                    warnings.add(
                        InteractionWarning(
                            pastIngestion = pastIngestion,
                            pastSubstanceName = pastSubstance.name,
                            status = backwardInteraction.status,
                            notes = backwardInteraction.notes
                        )
                    )
                }
            }
        }
        
        // Return sorted by severity (e.g. Dangerous first, Caution later)
        return warnings.sortedByDescending { getSeverity(it.status) }
    }

    private fun isWarningStatus(status: String): Boolean {
        // e.g. "Low Risk & Synergy", "Caution", "Unsafe", "Dangerous"
        val lower = status.lowercase()
        return lower.contains("caution") || 
               lower.contains("unsafe") || 
               lower.contains("dangerous") || 
               lower.contains("fatal") ||
               lower.contains("serotonin syndrome")
    }

    private fun getSeverity(status: String): Int {
        val lower = status.lowercase()
        return when {
            lower.contains("fatal") || lower.contains("dangerous") -> 3
            lower.contains("unsafe") -> 2
            lower.contains("caution") -> 1
            else -> 0
        }
    }
}
