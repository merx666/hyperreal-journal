package info.hyperreal.journal.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import info.hyperreal.journal.data.local.entity.IngestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngestion(ingestion: IngestionEntity): Long

    @Update
    suspend fun updateIngestion(ingestion: IngestionEntity)

    @Delete
    suspend fun deleteIngestion(ingestion: IngestionEntity)

    @Query("SELECT * FROM ingestions WHERE id = :id")
    suspend fun getIngestionById(id: Long): IngestionEntity?

    @Query("SELECT * FROM ingestions ORDER BY timestamp DESC")
    fun getAllIngestions(): Flow<List<IngestionEntity>>

    @Query("SELECT * FROM ingestions WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getIngestionsSince(sinceTimestamp: Long): Flow<List<IngestionEntity>>

    @Query("DELETE FROM ingestions")
    suspend fun deleteAll()
}
