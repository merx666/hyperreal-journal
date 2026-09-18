package info.hyperreal.journal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "check_ins",
    foreignKeys = [
        ForeignKey(
            entity = IngestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingestionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ingestionId"])]
)
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ingestionId: Long,
    val timestamp: Long,
    val phase: String,
    val shulginRating: String,
    val notes: String?
)
