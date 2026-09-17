package info.hyperreal.journal.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import info.hyperreal.journal.domain.model.SubstanceInteraction
import info.hyperreal.journal.domain.model.InteractionStatus
import info.hyperreal.journal.domain.repository.InteractionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class InteractionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
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
            val jsonString = context.assets.open("interactions_sin.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("interactions")

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                interactionsList.add(
                    SubstanceInteraction(
                        substanceA = item.getString("substance_a"),
                        substanceB = item.getString("substance_b"),
                        status = InteractionStatus.fromString(item.getString("status")),
                        note = if (item.has("note")) item.getString("note") else null
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load interactions_sin.json")
        }

        cachedInteractions = interactionsList
        interactionsList
    }
}
