package info.hyperreal.journal.data.local

import info.hyperreal.journal.data.local.entity.CustomSubstanceEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CustomSubstanceEntityTest {

    @Test
    fun testCustomSubstanceEntityCreation() {
        val entity = CustomSubstanceEntity(
            id = "custom_123",
            name = "Test Substance",
            roaName = "Doustnie",
            onsetMinutes = 30f,
            comeupMinutes = 45f,
            peakMinutes = 120f,
            offsetMinutes = 90f,
            afterglowMinutes = 60f,
            totalMinutes = 345f,
            createdAt = 1700000000000L
        )

        assertEquals("custom_123", entity.id)
        assertEquals("Test Substance", entity.name)
        assertEquals("Doustnie", entity.roaName)
        assertEquals(30f, entity.onsetMinutes)
        assertEquals(45f, entity.comeupMinutes)
        assertEquals(120f, entity.peakMinutes)
        assertEquals(90f, entity.offsetMinutes)
        assertEquals(60f, entity.afterglowMinutes)
        assertEquals(345f, entity.totalMinutes)
        assertNotNull(entity.createdAt)
    }
}
