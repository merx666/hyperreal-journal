package info.hyperreal.journal.domain.repository

import info.hyperreal.journal.domain.model.CheckIn
import kotlinx.coroutines.flow.Flow

interface CheckInRepository {
    suspend fun insertCheckIn(checkIn: CheckIn): Long
    suspend fun deleteCheckIn(id: Long)
    fun getCheckInsForIngestion(ingestionId: Long): Flow<List<CheckIn>>
    fun getAllCheckIns(): Flow<List<CheckIn>>
}
