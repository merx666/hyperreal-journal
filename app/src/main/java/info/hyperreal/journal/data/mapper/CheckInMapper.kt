package info.hyperreal.journal.data.mapper

import info.hyperreal.journal.data.local.entity.CheckInEntity
import info.hyperreal.journal.domain.model.CheckIn
import info.hyperreal.journal.domain.model.ShulginRating
import info.hyperreal.journal.domain.usecase.TimelinePhase

fun CheckInEntity.toDomain(): CheckIn {
    val domainPhase = runCatching { TimelinePhase.valueOf(phase) }.getOrDefault(TimelinePhase.PEAK)
    val domainRating = runCatching { ShulginRating.valueOf(shulginRating) }.getOrDefault(ShulginRating.PLUS_TWO)
    return CheckIn(
        id = id,
        ingestionId = ingestionId,
        timestamp = timestamp,
        phase = domainPhase,
        shulginRating = domainRating,
        notes = notes
    )
}

fun CheckIn.toEntity(): CheckInEntity {
    return CheckInEntity(
        id = id,
        ingestionId = ingestionId,
        timestamp = timestamp,
        phase = phase.name,
        shulginRating = shulginRating.name,
        notes = notes
    )
}
