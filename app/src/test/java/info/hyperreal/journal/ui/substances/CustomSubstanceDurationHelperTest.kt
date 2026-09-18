package info.hyperreal.journal.ui.substances

import org.junit.Assert.assertEquals
import org.junit.Test

class CustomSubstanceDurationHelperTest {

    @Test
    fun `parses minutes correctly`() {
        val duration = CustomSubstanceDurationHelper.calculateDuration(
            isHours = false,
            onset = "30",
            comeup = "45",
            peak = "120",
            offset = "90",
            afterglow = "60"
        )

        assertEquals(30f, duration.onset)
        assertEquals(45f, duration.comeup)
        assertEquals(120f, duration.peak)
        assertEquals(90f, duration.offset)
        assertEquals(60f, duration.afterglow)
        assertEquals(345f, duration.total)
    }

    @Test
    fun `parses hours and converts to minutes correctly`() {
        val duration = CustomSubstanceDurationHelper.calculateDuration(
            isHours = true,
            onset = "0.5",
            comeup = "1.0",
            peak = "2.0",
            offset = "1.5",
            afterglow = "1.0"
        )

        assertEquals(30f, duration.onset)
        assertEquals(60f, duration.comeup)
        assertEquals(120f, duration.peak)
        assertEquals(90f, duration.offset)
        assertEquals(60f, duration.afterglow)
        assertEquals(360f, duration.total)
    }

    @Test
    fun `handles empty and invalid values gracefully`() {
        val duration = CustomSubstanceDurationHelper.calculateDuration(
            isHours = false,
            onset = "",
            comeup = "abc",
            peak = "60",
            offset = null,
            afterglow = ""
        )

        assertEquals(null, duration.onset)
        assertEquals(null, duration.comeup)
        assertEquals(60f, duration.peak)
        assertEquals(null, duration.offset)
        assertEquals(null, duration.afterglow)
        assertEquals(60f, duration.total)
    }
}
