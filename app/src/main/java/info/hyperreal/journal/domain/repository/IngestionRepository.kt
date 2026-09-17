package info.hyperreal.journal.domain.repository

import info.hyperreal.journal.domain.model.Ingestion
import kotlinx.coroutines.flow.Flow

interface IngestionRepository {
    suspend fun insertIngestion(ingestion: Ingestion): Long
    suspend fun updateIngestion(ingestion: Ingestion)
    suspend fun deleteIngestion(ingestion: Ingestion)
    suspend fun getIngestionById(id: Long): Ingestion?
    fun getAllIngestions(): Flow<List<Ingestion>>
    fun getIngestionsSince(timestamp: Long): Flow<List<Ingestion>>
}
