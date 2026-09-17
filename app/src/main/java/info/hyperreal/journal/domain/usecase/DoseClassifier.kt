package info.hyperreal.journal.domain.usecase

import info.hyperreal.journal.domain.model.DoseParameters
import info.hyperreal.journal.domain.model.Roa
import info.hyperreal.journal.domain.model.Substance
import javax.inject.Inject

enum class DoseClassification {
    BELOW_THRESHOLD,
    THRESHOLD,
    LIGHT,
    COMMON,
    STRONG,
    HEAVY,
    UNKNOWN
}

class DoseClassifier @Inject constructor() {

    fun classify(substance: Substance, roaName: String, amount: Float): DoseClassification {
        val roa = substance.roas.find { it.name.equals(roaName, ignoreCase = true) } ?: return DoseClassification.UNKNOWN
        val dose = roa.dose ?: return DoseClassification.UNKNOWN

        // If threshold isn't defined, we fallback to 0.0 for comparison logic
        val threshold = dose.threshold ?: 0f
        
        return when {
            dose.heavy != null && amount >= dose.heavy -> DoseClassification.HEAVY
            dose.strong != null && amount >= dose.strong -> DoseClassification.STRONG
            dose.common != null && amount >= dose.common -> DoseClassification.COMMON
            dose.light != null && amount >= dose.light -> DoseClassification.LIGHT
            amount >= threshold && threshold > 0f -> DoseClassification.THRESHOLD
            amount < threshold -> DoseClassification.BELOW_THRESHOLD
            else -> DoseClassification.UNKNOWN
        }
    }
}
