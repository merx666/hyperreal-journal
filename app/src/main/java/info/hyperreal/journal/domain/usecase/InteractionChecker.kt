package info.hyperreal.journal.domain.usecase

import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.model.InteractionStatus
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.model.SubstanceInteraction
import java.util.concurrent.TimeUnit
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
        knownSubstances: List<Substance>,
        sinInteractions: List<SubstanceInteraction> = emptyList(),
        maxWindowHours: Long = 24
    ): List<InteractionWarning> {
        val warnings = mutableListOf<InteractionWarning>()
        val cutoffTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(maxWindowHours)

        for (pastIngestion in recentIngestions) {
            // Only consider recent ingestions within the active window (unless timestamp is future/test)
            if (pastIngestion.timestamp < cutoffTime && pastIngestion.timestamp <= System.currentTimeMillis()) {
                continue
            }

            val pastSubstance = knownSubstances.find { it.id == pastIngestion.substanceId } ?: continue

            // 1. Check against the comprehensive SIN database
            val sinMatch = sinInteractions.find { sin ->
                val matchA = sin.substanceA.equals(newSubstance.name, ignoreCase = true) ||
                        newSubstance.aliases.any { sin.substanceA.equals(it, ignoreCase = true) }
                val matchB = sin.substanceB.equals(pastSubstance.name, ignoreCase = true) ||
                        pastSubstance.aliases.any { sin.substanceB.equals(it, ignoreCase = true) }

                val revA = sin.substanceA.equals(pastSubstance.name, ignoreCase = true) ||
                        pastSubstance.aliases.any { sin.substanceA.equals(it, ignoreCase = true) }
                val revB = sin.substanceB.equals(newSubstance.name, ignoreCase = true) ||
                        newSubstance.aliases.any { sin.substanceB.equals(it, ignoreCase = true) }

                (matchA && matchB) || (revA && revB)
            }

            if (sinMatch != null) {
                if (isWarningStatus(sinMatch.status.name) || sinMatch.status == InteractionStatus.DANGEROUS ||
                    sinMatch.status == InteractionStatus.UNSAFE || sinMatch.status == InteractionStatus.CAUTION
                ) {
                    warnings.add(
                        InteractionWarning(
                            pastIngestion = pastIngestion,
                            pastSubstanceName = pastSubstance.name,
                            status = sinMatch.status.displayName,
                            notes = sinMatch.note
                        )
                    )
                    continue
                }
            }

            // 2. Fallback: check substance-embedded interactions
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

        // Return sorted by severity (Dangerous > Unsafe > Caution)
        return warnings.sortedByDescending { getSeverity(it.status) }
    }

    private fun isWarningStatus(status: String): Boolean {
        val lower = status.lowercase()
        return lower.contains("caution") ||
                lower.contains("ostroż") ||
                lower.contains("unsafe") ||
                lower.contains("niebezpiecz") ||
                lower.contains("dangerous") ||
                lower.contains("zagrożenie") ||
                lower.contains("fatal") ||
                lower.contains("serotonin syndrome")
    }

    private fun getSeverity(status: String): Int {
        val lower = status.lowercase()
        return when {
            lower.contains("fatal") || lower.contains("dangerous") || lower.contains("zagrożenie") -> 3
            lower.contains("unsafe") || lower.contains("niebezpiecz") -> 2
            lower.contains("caution") || lower.contains("ostroż") -> 1
            else -> 0
        }
    }
}
