package info.hyperreal.journal.domain.repository

import info.hyperreal.journal.domain.model.SubstanceInteraction

interface InteractionRepository {
    suspend fun getInteraction(substanceA: String, substanceB: String): SubstanceInteraction?
    suspend fun getAllInteractions(): List<SubstanceInteraction>
}
