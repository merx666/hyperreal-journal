package info.hyperreal.journal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import info.hyperreal.journal.data.local.entity.CustomSubstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomSubstanceDao {
    @Query("SELECT * FROM custom_substances ORDER BY name ASC")
    fun getAllCustomSubstances(): Flow<List<CustomSubstanceEntity>>

    @Query("SELECT * FROM custom_substances WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CustomSubstanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomSubstance(entity: CustomSubstanceEntity)

    @Query("DELETE FROM custom_substances WHERE id = :id")
    suspend fun deleteCustomSubstance(id: String)
}
