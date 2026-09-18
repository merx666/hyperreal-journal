package info.hyperreal.journal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_substances")
data class CustomSubstanceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val roaName: String = "Doustnie",
    val onsetMinutes: Float? = null,
    val comeupMinutes: Float? = null,
    val peakMinutes: Float? = null,
    val offsetMinutes: Float? = null,
    val afterglowMinutes: Float? = null,
    val totalMinutes: Float? = null,
    val createdAt: Long = System.currentTimeMillis()
)
