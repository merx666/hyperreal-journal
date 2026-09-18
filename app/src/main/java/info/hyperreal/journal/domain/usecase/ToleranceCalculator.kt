package info.hyperreal.journal.domain.usecase

import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.model.ReceptorGroup
import info.hyperreal.journal.domain.model.ToleranceRiskLevel
import info.hyperreal.journal.domain.model.ToleranceStatus
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp

@Singleton
class ToleranceCalculator @Inject constructor() {

    fun mapSubstanceToGroup(substanceId: String): ReceptorGroup? {
        val clean = substanceId.trim().lowercase()
        return when {
            clean.contains("lsd") || clean.contains("psilocybin") || clean.contains("grzyb") ||
                    clean.contains("dmt") || clean.contains("mescaline") || clean.contains("meskalina") ||
                    clean.contains("2c-b") || clean.contains("2c-e") || clean.contains("ayahuasca") ||
                    clean.contains("changa") || clean.contains("1p-lsd") -> ReceptorGroup.SEROTONIN_2A

            clean.contains("mdma") || clean.contains("mda") || clean.contains("5-mapb") ||
                    clean.contains("6-apb") || clean.contains("ecstasy") || clean.contains("piguł") -> ReceptorGroup.MDMA_SERT

            clean.contains("ketamine") || clean.contains("ketamin") || clean.contains("dxm") ||
                    clean.contains("mxe") || clean.contains("pcp") || clean.contains("esketamin") ||
                    clean.contains("dysocjant") -> ReceptorGroup.DISSOCIATIVE_NMDA

            clean.contains("morfin") || clean.contains("morphine") || clean.contains("oksykodon") ||
                    clean.contains("oxycodone") || clean.contains("kodein") || clean.contains("codeine") ||
                    clean.contains("fentanyl") || clean.contains("metadon") || clean.contains("methadone") ||
                    clean.contains("buprenorfin") || clean.contains("tramadol") || clean.contains("heroina") ||
                    clean.contains("heroin") || clean.contains("opium") || clean.contains("opioid") -> ReceptorGroup.OPIOID_MOR

            clean.contains("alprazolam") || clean.contains("diazepam") || clean.contains("clonazepam") ||
                    clean.contains("klonazepam") || clean.contains("lorazepam") || clean.contains("etizolam") ||
                    clean.contains("benzo") || clean.contains("etanol") || clean.contains("ethanol") ||
                    clean.contains("alkohol") || clean.contains("alcohol") -> ReceptorGroup.GABA_SEDATIVE

            else -> null
        }
    }

    fun calculateStatus(
        group: ReceptorGroup,
        ingestions: List<Ingestion>,
        manualOverrideTime: Long?,
        currentTimeMs: Long = System.currentTimeMillis()
    ): ToleranceStatus {
        val groupIngestions = ingestions
            .filter { mapSubstanceToGroup(it.substanceId) == group }
            .sortedByDescending { it.timestamp }

        val latestRoomTime = groupIngestions.firstOrNull()?.timestamp
        val effectiveLastTime = when {
            latestRoomTime != null && manualOverrideTime != null -> maxOf(latestRoomTime, manualOverrideTime)
            latestRoomTime != null -> latestRoomTime
            manualOverrideTime != null -> manualOverrideTime
            else -> null
        }

        if (effectiveLastTime == null) {
            return ToleranceStatus(
                group = group,
                lastIngestionTime = null,
                daysSinceLast = null,
                resetProgressPercent = 100,
                estimatedDoseMultiplier = 1.0f,
                riskLevel = ToleranceRiskLevel.SAFE,
                statusLabel = "Brak historii — układ w stanie bazowym",
                consecutiveDays = 0,
                harmReductionAdvice = "Układ receptorowy w pełni zregenerowany. Pamiętaj o przestrzeganiu przerw i testowaniu odczynnikami."
            )
        }

        val elapsedMs = (currentTimeMs - effectiveLastTime).coerceAtLeast(0L)
        val daysSinceLast = elapsedMs.toFloat() / TimeUnit.DAYS.toMillis(1)

        return when (group) {
            ReceptorGroup.SEROTONIN_2A -> calculate5Ht2aStatus(group, effectiveLastTime, daysSinceLast)
            ReceptorGroup.MDMA_SERT -> calculateMdmaStatus(group, effectiveLastTime, daysSinceLast)
            ReceptorGroup.DISSOCIATIVE_NMDA -> calculateDissociativeStatus(group, effectiveLastTime, daysSinceLast)
            ReceptorGroup.OPIOID_MOR -> calculateStreakDepressantStatus(group, effectiveLastTime, daysSinceLast, groupIngestions, currentTimeMs, "Opioidów")
            ReceptorGroup.GABA_SEDATIVE -> calculateStreakDepressantStatus(group, effectiveLastTime, daysSinceLast, groupIngestions, currentTimeMs, "Depresantów GABA")
        }
    }

    private fun calculate5Ht2aStatus(group: ReceptorGroup, lastTime: Long, days: Float): ToleranceStatus {
        val multiplier = if (days >= 14f) 1.0f else (1.0f + 1.8f * exp(-0.35f * days))
        val resetPercent = (((2.8f - multiplier) / 1.8f) * 100f).toInt().coerceIn(0, 100)

        val (risk, label, advice) = when {
            days < 2f -> Triple(
                ToleranceRiskLevel.DANGER,
                "Tolerancja maksymalna (~${(multiplier * 100).toInt()}% dawki)",
                "Ekstremalna tolerancja receptorowa. Przyjęcie dawki teraz przyniesie znikomy efekt mistyczny, a narazi na wyczerpanie psychiczne."
            )
            days < 7f -> Triple(
                ToleranceRiskLevel.WARNING,
                "Wysoka tolerancja (~${(multiplier * 100).toInt()}% dawki)",
                "Receptory 5-HT2A są w trakcie downregulacji. Zaleca się odczekanie minimum 14 dni do pełnego resetu."
            )
            days < 14f -> Triple(
                ToleranceRiskLevel.NOTICE,
                "Łagodna tolerancja (~${(multiplier * 100).toInt()}% dawki)",
                "Większość tolerancji ustąpiła (${resetPercent}%). Pełen reset receptorowy za ok. ${(14 - days).toInt() + 1} dni."
            )
            else -> Triple(
                ToleranceRiskLevel.SAFE,
                "Pełny reset (100%)",
                "Receptory 5-HT2A w pełni zregenerowane. Tolerancja powróciła do poziomu bazowego."
            )
        }

        return ToleranceStatus(
            group = group,
            lastIngestionTime = lastTime,
            daysSinceLast = days,
            resetProgressPercent = resetPercent,
            estimatedDoseMultiplier = multiplier,
            riskLevel = risk,
            statusLabel = label,
            consecutiveDays = 0,
            harmReductionAdvice = advice
        )
    }

    private fun calculateMdmaStatus(group: ReceptorGroup, lastTime: Long, days: Float): ToleranceStatus {
        val resetPercent = ((days / 90f) * 100f).toInt().coerceIn(0, 100)

        val (risk, label, advice) = when {
            days < 30f -> Triple(
                ToleranceRiskLevel.DANGER,
                "Krytyczny brak odstępu (${days.toInt()}/90 dni)",
                "Wysokie ryzyko neurotoksyczności i zespołu serotoninowego. Aksony SERT potrzebują min. 3 miesięcy (90 dni) na regenerację."
            )
            days < 60f -> Triple(
                ToleranceRiskLevel.WARNING,
                "Częściowa regeneracja (${days.toInt()}/90 dni)",
                "Układ serotoninergiczny nadal się odbudowuje. Ponowna dawka osłabi 'magię' substancji i pogłębi zjazd."
            )
            days < 90f -> Triple(
                ToleranceRiskLevel.NOTICE,
                "Końcówka regeneracji (${days.toInt()}/90 dni)",
                "Zbliżasz się do pełnego 3-miesięcznego okna regeneracji. Odczekaj jeszcze ${(90 - days).toInt()} dni."
            )
            else -> Triple(
                ToleranceRiskLevel.SAFE,
                "Zasada 3 miesięcy spełniona (${days.toInt()} dni)",
                "Układ SERT w pełni zresetowany. Zachowaj rozsądne dawkowanie i odpowiednie nawodnienie z elektrolitami."
            )
        }

        return ToleranceStatus(
            group = group,
            lastIngestionTime = lastTime,
            daysSinceLast = days,
            resetProgressPercent = resetPercent,
            estimatedDoseMultiplier = null,
            riskLevel = risk,
            statusLabel = label,
            consecutiveDays = 0,
            harmReductionAdvice = advice
        )
    }

    private fun calculateDissociativeStatus(group: ReceptorGroup, lastTime: Long, days: Float): ToleranceStatus {
        val resetPercent = ((days / 21f) * 100f).toInt().coerceIn(0, 100)

        val (risk, label, advice) = when {
            days < 7f -> Triple(
                ToleranceRiskLevel.DANGER,
                "Aktywna tolerancja NMDA (${days.toInt()}/21 dni)",
                "Dysocjanty wywołują trwałą tolerancję (perma-tolerance). Częste stosowanie obciąża pęcherz moczowy i nerki."
            )
            days < 21f -> Triple(
                ToleranceRiskLevel.NOTICE,
                "Regeneracja receptorów NMDA (${days.toInt()}/21 dni)",
                "Odczekaj pełne 3-4 tygodnie, aby chronić układ moczowy i wrażliwość receptorów NMDA."
            )
            else -> Triple(
                ToleranceRiskLevel.SAFE,
                "Zresetowany odstęp (${days.toInt()} dni)",
                "Bezpieczny odstęp zachowany. Pamiętaj o piciu zielonej herbaty (EGCG) dla ochrony pęcherza przy ketaminie."
            )
        }

        return ToleranceStatus(
            group = group,
            lastIngestionTime = lastTime,
            daysSinceLast = days,
            resetProgressPercent = resetPercent,
            estimatedDoseMultiplier = null,
            riskLevel = risk,
            statusLabel = label,
            consecutiveDays = 0,
            harmReductionAdvice = advice
        )
    }

    private fun calculateStreakDepressantStatus(
        group: ReceptorGroup,
        lastTime: Long,
        days: Float,
        ingestions: List<Ingestion>,
        currentTimeMs: Long,
        categoryName: String
    ): ToleranceStatus {
        val streakDays = calculateConsecutiveDays(ingestions, currentTimeMs)
        val resetPercent = if (days >= 14f) 100 else ((days / 14f) * 100f).toInt().coerceIn(0, 100)

        val (risk, label, advice) = when {
            streakDays >= 3 -> Triple(
                ToleranceRiskLevel.DANGER,
                "Wykryto ciąg ($streakDays dni z rzędu!)",
                "UWAGA: Zażywanie $categoryName przez $streakDays dni z rzędu gwałtownie buduje zależność fizyczną i tolerancję na depresję oddechową."
            )
            streakDays == 2 -> Triple(
                ToleranceRiskLevel.WARNING,
                "Początek ciągu (2 dni z rzędu)",
                "Kolejny dzień z dawką $categoryName. Przerwij używanie na co najmniej 7-14 dni, aby uniknąć zespołu odstawiennego."
            )
            days < 3f -> Triple(
                ToleranceRiskLevel.NOTICE,
                "Niedawne zażycie (${days.toInt()} dni temu)",
                "Zachowaj minimum tydzień odstępu między dawkami $categoryName."
            )
            else -> Triple(
                ToleranceRiskLevel.SAFE,
                "Brak ciągu (${days.toInt()} dni przerwy)",
                "Bezpieczny odstęp. Nigdy nie łącz z innymi depresantami OUN (alkohol + opioidy / benzo to śmiertelne ryzyko)."
            )
        }

        return ToleranceStatus(
            group = group,
            lastIngestionTime = lastTime,
            daysSinceLast = days,
            resetProgressPercent = resetPercent,
            estimatedDoseMultiplier = null,
            riskLevel = risk,
            statusLabel = label,
            consecutiveDays = streakDays,
            harmReductionAdvice = advice
        )
    }

    private fun calculateConsecutiveDays(ingestions: List<Ingestion>, currentTimeMs: Long): Int {
        if (ingestions.isEmpty()) return 0

        val calendar = Calendar.getInstance()
        val uniqueDaysEpoch = ingestions.map {
            calendar.timeInMillis = it.timestamp
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
        }.toSet()

        calendar.timeInMillis = currentTimeMs
        var currentYear = calendar.get(Calendar.YEAR)
        var currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        var streak = 0
        while (true) {
            val key = "$currentYear-$currentDayOfYear"
            if (uniqueDaysEpoch.contains(key)) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                currentYear = calendar.get(Calendar.YEAR)
                currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            } else {
                break
            }
        }
        return streak
    }

    fun calculateAll(
        ingestions: List<Ingestion>,
        manualOverrides: Map<ReceptorGroup, Long>,
        currentTimeMs: Long = System.currentTimeMillis()
    ): Map<ReceptorGroup, ToleranceStatus> {
        return ReceptorGroup.entries.associateWith { group ->
            calculateStatus(
                group = group,
                ingestions = ingestions,
                manualOverrideTime = manualOverrides[group],
                currentTimeMs = currentTimeMs
            )
        }
    }

    fun getStatusForSubstance(
        substanceId: String,
        ingestions: List<Ingestion>,
        manualOverrides: Map<ReceptorGroup, Long>,
        currentTimeMs: Long = System.currentTimeMillis()
    ): ToleranceStatus? {
        val group = mapSubstanceToGroup(substanceId) ?: return null
        return calculateStatus(
            group = group,
            ingestions = ingestions,
            manualOverrideTime = manualOverrides[group],
            currentTimeMs = currentTimeMs
        )
    }
}
