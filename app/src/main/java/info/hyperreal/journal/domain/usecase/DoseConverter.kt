package info.hyperreal.journal.domain.usecase

import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

class DoseConverter @Inject constructor() {

    fun calculateThcMg(dryHerbGrams: Float, thcPercent: Float): Float {
        if (dryHerbGrams <= 0f || thcPercent <= 0f) return 0f
        val rawMg = dryHerbGrams * (thcPercent / 100f) * 1000f
        return (rawMg * 10f).roundToInt() / 10f
    }

    fun formatThcNote(dryHerbGrams: Float, thcPercent: Float): String {
        return String.format(Locale.US, "[Kalkulator: %.2f g suszu (%.1f%% THC)]", dryHerbGrams, thcPercent)
    }

    fun calculateEthanolGrams(volumeMl: Float, alcoholPercent: Float): Float {
        if (volumeMl <= 0f || alcoholPercent <= 0f) return 0f
        val rawGrams = volumeMl * (alcoholPercent / 100f) * 0.8f
        return (rawGrams * 10f).roundToInt() / 10f
    }

    fun formatAlcoholNote(beverageName: String, volumeMl: Float, alcoholPercent: Float): String {
        val name = beverageName.ifBlank { "Alkohol" }
        return String.format(Locale.US, "[Kalkulator: %s %.0f ml (%.1f%% vol)]", name, volumeMl, alcoholPercent)
    }
}
