package info.hyperreal.journal.domain.model

import info.hyperreal.journal.domain.usecase.TimelinePhase

enum class ShulginRating(val symbol: String, val label: String, val description: String) {
    PLUS_MINUS("±", "Plus / Minus (Stan progowy)", "Subtelny alert, ledwo wyczuwalny początek działania."),
    PLUS_ONE("+", "Plus Jeden (+)", "Wyraźne i bezdyskusyjne działanie, które jednak łatwo zignorować lub stłumić."),
    PLUS_TWO("++", "Plus Dwa (++)", "Nieodparte działanie, zmiana percepcji, ale zdolność do rozmowy i zachowania kontroli."),
    PLUS_THREE("+++", "Plus Trzy (+++)", "Maksymalna intensywność doświadczenia, całkowite zaabsorbowanie uwagą i zmysłami."),
    PLUS_FOUR("++++", "Plus Cztery (++++)", "Rzadki stan mistyczny, transcendentalny, poczucie jedności ze wszechświatem.")
}

data class CheckIn(
    val id: Long = 0,
    val ingestionId: Long,
    val timestamp: Long,
    val phase: TimelinePhase,
    val shulginRating: ShulginRating,
    val notes: String? = null
)
