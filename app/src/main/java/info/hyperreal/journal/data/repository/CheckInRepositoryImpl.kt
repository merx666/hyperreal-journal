package info.hyperreal.journal.data.repository

import info.hyperreal.journal.data.local.dao.CheckInDao
import info.hyperreal.journal.data.mapper.toDomain
import info.hyperreal.journal.data.mapper.toEntity
import info.hyperreal.journal.domain.model.CheckIn
import info.hyperreal.journal.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInRepositoryImpl @Inject constructor(
    private val checkInDao: CheckInDao
) : CheckInRepository {

    override suspend fun insertCheckIn(checkIn: CheckIn): Long {
        return checkInDao.insertCheckIn(checkIn.toEntity())
    }

    override suspend fun deleteCheckIn(id: Long) {
        checkInDao.deleteCheckIn(id)
    }

    override fun getCheckInsForIngestion(ingestionId: Long): Flow<List<CheckIn>> {
        return checkInDao.getCheckInsForIngestion(ingestionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllCheckIns(): Flow<List<CheckIn>> {
        return checkInDao.getAllCheckIns().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
