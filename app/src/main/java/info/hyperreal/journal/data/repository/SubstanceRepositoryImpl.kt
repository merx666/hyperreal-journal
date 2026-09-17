package info.hyperreal.journal.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import info.hyperreal.journal.domain.model.Substance
import info.hyperreal.journal.domain.repository.SubstanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.InputStreamReader
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubstanceRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) : SubstanceRepository {

    private var cachedSubstances: List<Substance>? = null

    override fun getAllSubstances(): Flow<List<Substance>> = flow {
        val substances = getSubstances()
        emit(substances)
    }.flowOn(Dispatchers.IO)

    override suspend fun getSubstanceById(id: String): Substance? {
        return getSubstances().find { it.id == id }
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
