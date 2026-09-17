package info.hyperreal.journal.domain.repository

import info.hyperreal.journal.domain.model.Substance
import kotlinx.coroutines.flow.Flow

interface SubstanceRepository {
    fun getAllSubstances(): Flow<List<Substance>>
    suspend fun getSubstanceById(id: String): Substance?
}
