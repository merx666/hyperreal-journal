package info.hyperreal.journal.data.repository

import info.hyperreal.journal.data.local.dao.IngestionDao
import info.hyperreal.journal.data.mapper.toDomainModel
import info.hyperreal.journal.data.mapper.toEntity
import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.repository.IngestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IngestionRepositoryImpl @Inject constructor(
    private val ingestionDao: IngestionDao
) : IngestionRepository {

    override suspend fun insertIngestion(ingestion: Ingestion): Long {
        return ingestionDao.insertIngestion(ingestion.toEntity())
    }

    override suspend fun updateIngestion(ingestion: Ingestion) {
        ingestionDao.updateIngestion(ingestion.toEntity())
    }

    override suspend fun deleteIngestion(ingestion: Ingestion) {
        ingestionDao.deleteIngestion(ingestion.toEntity())
    }

    override suspend fun getIngestionById(id: Long): Ingestion? {
        return ingestionDao.getIngestionById(id)?.toDomainModel()
    }

    override fun getAllIngestions(): Flow<List<Ingestion>> {
        return ingestionDao.getAllIngestions().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun getIngestionsSince(timestamp: Long): Flow<List<Ingestion>> {
        return ingestionDao.getIngestionsSince(timestamp).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun deleteAll() {
        ingestionDao.deleteAll()
    }
}
