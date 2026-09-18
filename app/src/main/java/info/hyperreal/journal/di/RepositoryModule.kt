package info.hyperreal.journal.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import info.hyperreal.journal.data.repository.IngestionRepositoryImpl
import info.hyperreal.journal.data.repository.SubstanceRepositoryImpl
import info.hyperreal.journal.domain.repository.IngestionRepository
import info.hyperreal.journal.domain.repository.SubstanceRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindIngestionRepository(
        ingestionRepositoryImpl: IngestionRepositoryImpl
    ): IngestionRepository

    @Binds
    @Singleton
    abstract fun bindSubstanceRepository(
        substanceRepositoryImpl: SubstanceRepositoryImpl
    ): SubstanceRepository

    @Binds
    @Singleton
    abstract fun bindInteractionRepository(
        interactionRepositoryImpl: info.hyperreal.journal.data.repository.InteractionRepositoryImpl
    ): info.hyperreal.journal.domain.repository.InteractionRepository

    @Binds
    @Singleton
    abstract fun bindCheckInRepository(
        checkInRepositoryImpl: info.hyperreal.journal.data.repository.CheckInRepositoryImpl
    ): info.hyperreal.journal.domain.repository.CheckInRepository
}
