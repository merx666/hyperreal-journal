package info.hyperreal.journal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingestions")
data class IngestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val substanceId: String,
    val roa: String,
    val doseAmount: Float,
    val doseUnit: String,
    val timestamp: Long,
    val notes: String?,
    val experienceId: String?
)
