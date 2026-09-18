package info.hyperreal.journal.domain.model

enum class ReceptorGroup(val displayName: String, val subtitle: String) {
    SEROTONIN_2A(
        displayName = "Psychodeliki (5-HT2A)",
        subtitle = "LSD, Psylocybina, DMT, Meskalina, 2C-B"
    ),
    MDMA_SERT(
        displayName = "Entaktogeny (Receptory SERT)",
        subtitle = "MDMA, MDA, 5-MAPB, 6-APB"
    ),
    DISSOCIATIVE_NMDA(
        displayName = "Dysocjanty (Receptory NMDA)",
        subtitle = "Ketamina, Esketamina, DXM, MXE, PCP"
    ),
    OPIOID_MOR(
        displayName = "Opioidy (Receptory μ-opioidowe)",
        subtitle = "Morfina, Oksykodon, Kodeina, Fentanyl, Tramadol"
    ),
    GABA_SEDATIVE(
        displayName = "Depresanty (Układ GABA-A)",
        subtitle = "Benzodiazepiny, Alkohol"
    )
}

enum class ToleranceRiskLevel {
    SAFE,       // Pełny reset, brak wykrytych ciągów
    NOTICE,     // Łagodna tolerancja lub początek odstępu
    WARNING,    // Wyraźna tolerancja, suboptymalny odstęp
    DANGER      // Ciąg wielodniowy lub wysokie ryzyko neurotoksyczności / tolerancji
}

data class ToleranceStatus(
    val group: ReceptorGroup,
    val lastIngestionTime: Long?,
    val daysSinceLast: Float?,
    val resetProgressPercent: Int,          // 0 to 100
    val estimatedDoseMultiplier: Float?,     // Np. 1.8x dla 5-HT2A
    val riskLevel: ToleranceRiskLevel,
    val statusLabel: String,
    val consecutiveDays: Int = 0,            // Liczba dni z rzędu dla opioidów/GABA
    val harmReductionAdvice: String
)
