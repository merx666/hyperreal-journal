package info.hyperreal.journal.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class DoseConverterTest {

    private val converter = DoseConverter()

    @Test
    fun calculateThcMg_correctCalculations() {
        // 0.25g suszu przy 18% THC = 0.25 * 0.18 * 1000 = 45.0 mg
        val result1 = converter.calculateThcMg(dryHerbGrams = 0.25f, thcPercent = 18f)
        assertEquals(45.0f, result1, 0.05f)

        // 0.50g suszu przy 20% THC = 100.0 mg
        val result2 = converter.calculateThcMg(dryHerbGrams = 0.50f, thcPercent = 20f)
        assertEquals(100.0f, result2, 0.05f)

        // 0.10g suszu przy 15% THC = 15.0 mg
        val result3 = converter.calculateThcMg(dryHerbGrams = 0.10f, thcPercent = 15f)
        assertEquals(15.0f, result3, 0.05f)
    }

    @Test
    fun calculateThcMg_edgeCases() {
        assertEquals(0f, converter.calculateThcMg(0f, 20f), 0.01f)
        assertEquals(0f, converter.calculateThcMg(1f, 0f), 0.01f)
    }

    @Test
    fun formatThcNote_generatesValidFormat() {
        val note = converter.formatThcNote(dryHerbGrams = 0.25f, thcPercent = 18.0f)
        assertEquals("[Kalkulator: 0.25 g suszu (18.0% THC)]", note)
    }

    @Test
    fun calculateEthanolGrams_presets() {
        // Piwo 500ml 5% = 500 * 0.05 * 0.8 = 20.0 g
        val beer = converter.calculateEthanolGrams(volumeMl = 500f, alcoholPercent = 5.0f)
        assertEquals(20.0f, beer, 0.05f)

        // Wino 150ml 12% = 150 * 0.12 * 0.8 = 14.4 g
        val wine = converter.calculateEthanolGrams(volumeMl = 150f, alcoholPercent = 12.0f)
        assertEquals(14.4f, wine, 0.05f)

        // Wódka 50ml 40% = 50 * 0.40 * 0.8 = 16.0 g
        val shot = converter.calculateEthanolGrams(volumeMl = 50f, alcoholPercent = 40.0f)
        assertEquals(16.0f, shot, 0.05f)
    }

    @Test
    fun formatAlcoholNote_generatesValidFormat() {
        val note = converter.formatAlcoholNote("Piwo", 500f, 5.0f)
        assertEquals("[Kalkulator: Piwo 500 ml (5.0% vol)]", note)
    }
}
