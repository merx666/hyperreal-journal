package info.hyperreal.journal.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import info.hyperreal.journal.data.local.dao.CustomSubstanceDao
import info.hyperreal.journal.data.local.entity.CustomSubstanceEntity
import info.hyperreal.journal.domain.model.DoseParameters
import info.hyperreal.journal.domain.model.DurationParameters
import info.hyperreal.journal.domain.model.Roa
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.repository.SubstanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.InputStreamReader
import java.util.UUID
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubstanceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val customSubstanceDao: CustomSubstanceDao
) : SubstanceRepository {

    private var cachedSubstances: List<Substance>? = null

    override fun getAllSubstances(): Flow<List<Substance>> {
        val bundled = getSubstances()
        return customSubstanceDao.getAllCustomSubstances().map { entities ->
            val custom = entities.map { it.toSubstance() }
            bundled + custom
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getSubstanceById(id: String): Substance? {
        val bundled = getSubstances().find { it.id == id }
        if (bundled != null) return bundled
        return customSubstanceDao.getById(id)?.toSubstance()
    }

    override suspend fun addCustomSubstance(
        name: String,
        roaName: String,
        duration: DurationParameters
    ): Substance {
        val id = "custom_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val totalMinutes = duration.total ?: listOfNotNull(
            duration.onset,
            duration.comeup,
            duration.peak,
            duration.offset,
            duration.afterglow
        ).sum().takeIf { it > 0f }

        val entity = CustomSubstanceEntity(
            id = id,
            name = name.trim(),
            roaName = roaName.ifBlank { "Doustnie" }.trim(),
            onsetMinutes = duration.onset,
            comeupMinutes = duration.comeup,
            peakMinutes = duration.peak,
            offsetMinutes = duration.offset,
            afterglowMinutes = duration.afterglow,
            totalMinutes = totalMinutes,
            createdAt = System.currentTimeMillis()
        )
        customSubstanceDao.insertCustomSubstance(entity)
        return entity.toSubstance()
    }

    override suspend fun deleteCustomSubstance(id: String) {
        customSubstanceDao.deleteCustomSubstance(id)
    }

    private fun getSubstances(): List<Substance> {
        cachedSubstances?.let { return it }

        return try {
            val inputStream = context.assets.open("substances.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<Substance>>() {}.type
            val substances: List<Substance> = gson.fromJson(reader, type)
            cachedSubstances = substances
            substances
        } catch (e: Exception) {
            Timber.e(e, "Failed to load substances.json")
            emptyList()
        }
    }
}

fun CustomSubstanceEntity.toSubstance(): Substance {
    val durationParams = DurationParameters(
        onset = onsetMinutes,
        comeup = comeupMinutes,
        peak = peakMinutes,
        offset = offsetMinutes,
        afterglow = afterglowMinutes,
        total = totalMinutes
    )
    return Substance(
        id = id,
        name = name,
        aliases = listOf("Własna"),
        summary = "Własna substancja dodana przez użytkownika.",
        classes = listOf("Własne"),
        roas = listOf(
            Roa(
                name = roaName,
                dose = DoseParameters(units = "mg"),
                duration = durationParams
            )
        )
    )
}
