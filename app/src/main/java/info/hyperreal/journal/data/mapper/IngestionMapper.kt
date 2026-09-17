package info.hyperreal.journal.data.mapper

import info.hyperreal.journal.data.local.entity.IngestionEntity
import info.hyperreal.journal.domain.model.Ingestion

fun IngestionEntity.toDomainModel(): Ingestion {
    return Ingestion(
        id = id,
        substanceId = substanceId,
        roa = roa,
        doseAmount = doseAmount,
        doseUnit = doseUnit,
        timestamp = timestamp,
        notes = notes,
        experienceId = experienceId
    )
}

fun Ingestion.toEntity(): IngestionEntity {
    return IngestionEntity(
        id = id,
        substanceId = substanceId,
        roa = roa,
        doseAmount = doseAmount,
        doseUnit = doseUnit,
        timestamp = timestamp,
        notes = notes,
        experienceId = experienceId
    )
}
