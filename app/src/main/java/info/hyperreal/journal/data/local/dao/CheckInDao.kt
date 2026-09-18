package info.hyperreal.journal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import info.hyperreal.journal.data.local.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckInEntity): Long

    @Query("DELETE FROM check_ins WHERE id = :id")
    suspend fun deleteCheckIn(id: Long)

    @Query("SELECT * FROM check_ins WHERE ingestionId = :ingestionId ORDER BY timestamp ASC")
    fun getCheckInsForIngestion(ingestionId: Long): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins ORDER BY timestamp ASC")
    fun getAllCheckIns(): Flow<List<CheckInEntity>>
}
