package info.hyperreal.journal.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import info.hyperreal.journal.data.local.dao.CheckInDao
import info.hyperreal.journal.data.local.dao.CustomSubstanceDao
import info.hyperreal.journal.data.local.dao.IngestionDao
import info.hyperreal.journal.data.local.entity.CheckInEntity
import info.hyperreal.journal.data.local.entity.CustomSubstanceEntity
import info.hyperreal.journal.data.local.entity.IngestionEntity

@Database(
    entities = [IngestionEntity::class, CheckInEntity::class, CustomSubstanceEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val ingestionDao: IngestionDao
    abstract val checkInDao: CheckInDao
    abstract val customSubstanceDao: CustomSubstanceDao

    companion object {
        const val DATABASE_NAME = "hyperreal_journal_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS check_ins (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        ingestionId INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        phase TEXT NOT NULL,
                        shulginRating TEXT NOT NULL,
                        notes TEXT,
                        FOREIGN KEY(ingestionId) REFERENCES ingestions(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_check_ins_ingestionId ON check_ins(ingestionId)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS custom_substances (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        roaName TEXT NOT NULL,
                        onsetMinutes REAL,
                        comeupMinutes REAL,
                        peakMinutes REAL,
                        offsetMinutes REAL,
                        afterglowMinutes REAL,
                        totalMinutes REAL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
