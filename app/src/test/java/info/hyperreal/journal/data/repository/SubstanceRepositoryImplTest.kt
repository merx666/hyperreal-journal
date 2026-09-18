package info.hyperreal.journal.data.repository

import android.content.Context
import android.content.res.AssetManager
import com.google.gson.Gson
import info.hyperreal.journal.data.local.dao.CustomSubstanceDao
import info.hyperreal.journal.data.local.entity.CustomSubstanceEntity
import info.hyperreal.journal.domain.model.DurationParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class SubstanceRepositoryImplTest {

    private val context = mockk<Context>()
    private val assets = mockk<AssetManager>()
    private val dao = mockk<CustomSubstanceDao>()
    private val gson = Gson()
    private lateinit var repository: SubstanceRepositoryImpl

    @Before
    fun setup() {
        val sampleJson = """
            [
                {
                    "id": "caffeine",
                    "name": "Kofeina",
                    "classes": ["Stymulant"],
                    "roas": []
                }
            ]
        """.trimIndent()

        every { context.assets } returns assets
        every { assets.open("substances.json") } returns ByteArrayInputStream(sampleJson.toByteArray())
        repository = SubstanceRepositoryImpl(context, gson, dao)
    }

    @Test
    fun `getAllSubstances combines bundled and custom substances`() = runTest {
        val customEntity = CustomSubstanceEntity(
            id = "custom_1",
            name = "Własny Nootropik",
            roaName = "Doustnie",
            onsetMinutes = 20f,
            comeupMinutes = 30f,
            peakMinutes = 90f,
            offsetMinutes = 60f,
            afterglowMinutes = 30f,
            totalMinutes = 230f
        )
        every { dao.getAllCustomSubstances() } returns flowOf(listOf(customEntity))

        val result = repository.getAllSubstances().first()

        assertEquals(2, result.size)
        val custom = result.find { it.id == "custom_1" }
        assertTrue(custom != null)
        assertEquals("Własny Nootropik", custom?.name)
        assertTrue(custom?.classes?.contains("Własne") == true)
        val roa = custom?.roas?.firstOrNull()
        assertEquals("Doustnie", roa?.name)
        assertEquals(20f, roa?.duration?.onset)
        assertEquals(230f, roa?.duration?.total)
    }

    @Test
    fun `addCustomSubstance inserts into DAO and returns Substance`() = runTest {
        coEvery { dao.insertCustomSubstance(any()) } returns Unit

        val duration = DurationParameters(onset = 15f, peak = 60f, total = 120f)
        val created = repository.addCustomSubstance("Moja Substancja", "Waporyzacja", duration)

        coVerify { dao.insertCustomSubstance(match { it.name == "Moja Substancja" && it.roaName == "Waporyzacja" }) }
        assertEquals("Moja Substancja", created.name)
        assertEquals("Waporyzacja", created.roas.first().name)
        assertEquals(15f, created.roas.first().duration?.onset)
    }

    @Test
    fun `deleteCustomSubstance calls DAO delete`() = runTest {
        coEvery { dao.deleteCustomSubstance("custom_1") } returns Unit

        repository.deleteCustomSubstance("custom_1")

        coVerify { dao.deleteCustomSubstance("custom_1") }
    }
}
