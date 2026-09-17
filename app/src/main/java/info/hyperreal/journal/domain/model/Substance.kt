package info.hyperreal.journal.domain.model

data class Substance(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    val summary: String? = null,
    val harmReduction: String? = null,
    val classes: List<String> = emptyList(),
    val roas: List<Roa> = emptyList(),
    val interactions: List<Interaction> = emptyList()
)

data class Roa(
    val name: String,
    val dose: DoseParameters? = null,
    val duration: DurationParameters? = null
)

data class DoseParameters(
    val units: String,
    val threshold: Float? = null,
    val light: Float? = null,
    val common: Float? = null,
    val strong: Float? = null,
    val heavy: Float? = null
)

data class DurationParameters(
    val onset: Float? = null,
    val comeup: Float? = null,
    val peak: Float? = null,
    val offset: Float? = null,
    val afterglow: Float? = null,
    val total: Float? = null
)

data class Interaction(
    val substanceId: String,
    val status: String,
    val notes: String? = null
)
