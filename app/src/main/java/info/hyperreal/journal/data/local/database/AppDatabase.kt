package info.hyperreal.journal.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import info.hyperreal.journal.data.local.dao.IngestionDao
import info.hyperreal.journal.data.local.entity.IngestionEntity

@Database(
    entities = [IngestionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val ingestionDao: IngestionDao

    companion object {
        const val DATABASE_NAME = "hyperreal_journal_db"
    }
}
