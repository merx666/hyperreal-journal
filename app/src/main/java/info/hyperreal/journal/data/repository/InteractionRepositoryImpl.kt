package info.hyperreal.journal.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.hilt.android.qualifiers.ApplicationContext
import info.hyperreal.journal.domain.model.SubstanceInteraction
import info.hyperreal.journal.domain.model.InteractionStatus
import info.hyperreal.journal.domain.repository.InteractionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStreamReader
import javax.inject.Inject

private data class SinInteractionJson(
    @SerializedName("substance_a") val substanceA: String,
    @SerializedName("substance_b") val substanceB: String,
    val status: String,
    val note: String? = null
)

private data class SinDatabaseJson(
    val interactions: List<SinInteractionJson> = emptyList()
)

class InteractionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson = Gson()
) : InteractionRepository {

    private var cachedInteractions: List<SubstanceInteraction>? = null

    override suspend fun getInteraction(substanceA: String, substanceB: String): SubstanceInteraction? {
        val interactions = getAllInteractions()
        return interactions.find { 
            (it.substanceA.equals(substanceA, ignoreCase = true) && it.substanceB.equals(substanceB, ignoreCase = true)) ||
            (it.substanceA.equals(substanceB, ignoreCase = true) && it.substanceB.equals(substanceA, ignoreCase = true))
        }
    }

    override suspend fun getAllInteractions(): List<SubstanceInteraction> = withContext(Dispatchers.IO) {
        cachedInteractions?.let { return@withContext it }

        val interactionsList = mutableListOf<SubstanceInteraction>()
        try {
            val inputStream = context.assets.open("interactions_sin.json")
            val reader = InputStreamReader(inputStream)
            val db = gson.fromJson(reader, SinDatabaseJson::class.java)

            db?.interactions?.forEach { item ->
                interactionsList.add(
                    SubstanceInteraction(
                        substanceA = item.substanceA,
                        substanceB = item.substanceB,
                        status = InteractionStatus.fromString(item.status),
                        note = item.note
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load interactions_sin.json")
        }

        cachedInteractions = interactionsList
        interactionsList
    }

    override suspend fun getMatrixSubstances(): List<String> {
        val interactions = getAllInteractions()
        val substancesSet = mutableSetOf<String>()
        interactions.forEach {
            substancesSet.add(it.substanceA)
            substancesSet.add(it.substanceB)
        }
        return substancesSet.sortedWith(String.CASE_INSENSITIVE_ORDER)
    }

    override suspend fun getInteractionsForSubstance(substance: String): List<SubstanceInteraction> {
        val interactions = getAllInteractions()
        return interactions.filter {
            it.substanceA.equals(substance, ignoreCase = true) ||
            it.substanceB.equals(substance, ignoreCase = true)
        }.sortedWith(compareBy(
            { getSeverityRank(it.status) },
            { if (it.substanceA.equals(substance, ignoreCase = true)) it.substanceB else it.substanceA }
        ))
    }

    private fun getSeverityRank(status: InteractionStatus): Int {
        return when (status) {
            InteractionStatus.DANGEROUS -> 0
            InteractionStatus.UNSAFE -> 1
            InteractionStatus.CAUTION -> 2
            InteractionStatus.LOW_RISK_SYNERGY -> 3
            InteractionStatus.LOW_RISK_DECREASE -> 4
            InteractionStatus.LOW_RISK_NO_SYNERGY -> 5
            InteractionStatus.UNKNOWN -> 6
        }
    }
}
