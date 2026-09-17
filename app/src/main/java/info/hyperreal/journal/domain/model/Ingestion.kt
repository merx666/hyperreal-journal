package info.hyperreal.journal.domain.model

data class Ingestion(
    val id: Long = 0,
    val substanceId: String,
    val roa: String,
    val doseAmount: Float,
    val doseUnit: String,
    val timestamp: Long,
    val notes: String? = null,
    val experienceId: String? = null
)
