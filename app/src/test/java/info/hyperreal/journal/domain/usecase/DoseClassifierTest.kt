package info.hyperreal.journal.domain.usecase

import info.hyperreal.journal.domain.model.DoseParameters
import info.hyperreal.journal.domain.model.Roa
import info.hyperreal.journal.domain.model.Substance
import org.junit.Assert.assertEquals
import org.junit.Test

class DoseClassifierTest {

    private val classifier = DoseClassifier()

    private val mockRoa = Roa(
        name = "Oral",
        dose = DoseParameters(
            units = "mg",
            threshold = 10f,
            light = 20f,
            common = 50f,
            strong = 100f,
            heavy = 150f
        ),
        duration = null
    )

    private val mockSubstance = Substance(
        id = "test",
        name = "Test",
        aliases = emptyList(),
        classes = emptyList(),
        summary = null,
        roas = listOf(mockRoa),
        interactions = emptyList()
    )

    @Test
    fun `classify Heavy dose`() {
        val result = classifier.classify(mockSubstance, "Oral", 160f)
        assertEquals(DoseClassification.HEAVY, result)
    }

    @Test
    fun `classify Strong dose`() {
        val result = classifier.classify(mockSubstance, "Oral", 120f)
        assertEquals(DoseClassification.STRONG, result)
    }

    @Test
    fun `classify Common dose`() {
        val result = classifier.classify(mockSubstance, "Oral", 75f)
        assertEquals(DoseClassification.COMMON, result)
    }

    @Test
    fun `classify Light dose`() {
        val result = classifier.classify(mockSubstance, "Oral", 30f)
        assertEquals(DoseClassification.LIGHT, result)
    }

    @Test
    fun `classify Threshold dose`() {
        val result = classifier.classify(mockSubstance, "Oral", 15f)
        assertEquals(DoseClassification.THRESHOLD, result)
    }

    @Test
    fun `classify Below Threshold dose`() {
        val result = classifier.classify(mockSubstance, "Oral", 5f)
        assertEquals(DoseClassification.BELOW_THRESHOLD, result)
    }

    @Test
    fun `classify Unknown ROA returns Unknown`() {
        val result = classifier.classify(mockSubstance, "Unknown", 100f)
        assertEquals(DoseClassification.UNKNOWN, result)
    }
}
