package info.hyperreal.journal.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import info.hyperreal.journal.data.local.dao.CheckInDao
import info.hyperreal.journal.data.local.dao.CustomSubstanceDao
import info.hyperreal.journal.data.local.dao.IngestionDao
import info.hyperreal.journal.data.local.database.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides
    @Singleton
    fun provideIngestionDao(db: AppDatabase): IngestionDao {
        return db.ingestionDao
    }

    @Provides
    @Singleton
    fun provideCheckInDao(db: AppDatabase): CheckInDao {
        return db.checkInDao
    }

    @Provides
    @Singleton
    fun provideCustomSubstanceDao(db: AppDatabase): CustomSubstanceDao {
        return db.customSubstanceDao
    }
}
