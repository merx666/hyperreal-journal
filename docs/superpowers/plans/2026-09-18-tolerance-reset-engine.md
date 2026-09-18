# Tolerance & Reset Engine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement an autonomous, offline-first Tolerance & Reset Engine for Hyperreal Journal that models receptor dynamics (5-HT2A exponential curve, MDMA 90-day rule, NMDA 14-day window, Opioid/GABA consecutive days detection), provides manual date overrides via DataStore, displays a recovery dashboard in `InsightsScreen`, and presents contextual harm reduction warnings in `EnterDoseScreen`.

**Architecture:** Clean Architecture with MVI in Jetpack Compose. Pure domain use-case `ToleranceCalculator` taking Room ingestion history and DataStore manual overrides, exposed via reactive `StateFlow` in `InsightsViewModel` and `AddIngestionViewModel`.

**Tech Stack:** Kotlin 1.9+, Jetpack Compose (Material 3), Kotlin Coroutines & Flow, Room DB, Jetpack DataStore Preferences, Dagger Hilt, JUnit 4.

## Global Constraints

- **Language & Locale:** All user-facing strings must be in Polish (`pl`).
- **Offline First:** 100% offline, zero network requests.
- **Java Home for Builds:** `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.
- **Test Integrity:** All 65 existing tests must remain 100% green at every step.
- **TDD:** Write failing tests first, run to verify failure, implement minimal code, verify green, commit.

---

### Task 1: Domain Models for Receptor Groups and Tolerance Status

**Files:**
- Create: `app/src/main/java/info/hyperreal/journal/domain/model/ToleranceModels.kt`

**Interfaces:**
- Produces:
  - `enum class ReceptorGroup { SEROTONIN_2A, MDMA_SERT, DISSOCIATIVE_NMDA, OPIOID_MOR, GABA_SEDATIVE }`
  - `enum class ToleranceRiskLevel { SAFE, NOTICE, WARNING, DANGER }`
  - `data class ToleranceStatus(...)`

- [ ] **Step 1: Create the domain models file**

Create `app/src/main/java/info/hyperreal/journal/domain/model/ToleranceModels.kt`:
```kotlin
package info.hyperreal.journal.domain.model

enum class ReceptorGroup(val displayName: String, val subtitle: String) {
    SEROTONIN_2A(
        displayName = "Psychodeliki (5-HT2A)",
        subtitle = "LSD, Psylocybina, DMT, Meskalina, 2C-B"
    ),
    MDMA_SERT(
        displayName = "Entaktogeny (Receptory SERT)",
        subtitle = "MDMA, MDA, 5-MAPB, 6-APB"
    ),
    DISSOCIATIVE_NMDA(
        displayName = "Dysocjanty (Receptory NMDA)",
        subtitle = "Ketamina, Esketamina, DXM, MXE, PCP"
    ),
    OPIOID_MOR(
        displayName = "Opioidy (Receptory μ-opioidowe)",
        subtitle = "Morfina, Oksykodon, Kodeina, Fentanyl, Tramadol"
    ),
    GABA_SEDATIVE(
        displayName = "Depresanty (Układ GABA-A)",
        subtitle = "Benzodiazepiny, Alkohol"
    )
}

enum class ToleranceRiskLevel {
    SAFE,       // Pełny reset, brak wykrytych ciągów
    NOTICE,     // Łagodna tolerancja lub początek odstępu
    WARNING,    // Wyraźna tolerancja, suboptymalny odstęp
    DANGER      // Ciąg wielodniowy lub wysokie ryzyko neurotoksyczności / tolerancji
}

data class ToleranceStatus(
    val group: ReceptorGroup,
    val lastIngestionTime: Long?,
    val daysSinceLast: Float?,
    val resetProgressPercent: Int,          // 0 to 100
    val estimatedDoseMultiplier: Float?,     // Np. 1.8x dla 5-HT2A
    val riskLevel: ToleranceRiskLevel,
    val statusLabel: String,
    val consecutiveDays: Int = 0,            // Liczba dni z rzędu dla opioidów/GABA
    val harmReductionAdvice: String
)
```

- [ ] **Step 2: Verify compilation**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/domain/model/ToleranceModels.kt
git commit -m "feat: add domain models for tolerance and receptor groups"
```

---

### Task 2: ToleranceCalculator Use Case with Unit Tests (TDD)

**Files:**
- Create: `app/src/test/java/info/hyperreal/journal/domain/usecase/ToleranceCalculatorTest.kt`
- Create: `app/src/main/java/info/hyperreal/journal/domain/usecase/ToleranceCalculator.kt`

**Interfaces:**
- Consumes: `Ingestion`, `ReceptorGroup`, `ToleranceRiskLevel`, `ToleranceStatus`
- Produces:
  - `class ToleranceCalculator @Inject constructor() {`
    - `fun mapSubstanceToGroup(substanceId: String): ReceptorGroup?`
    - `fun calculateStatus(group: ReceptorGroup, ingestions: List<Ingestion>, manualOverrideTime: Long?, currentTimeMs: Long): ToleranceStatus`
    - `fun calculateAll(ingestions: List<Ingestion>, manualOverrides: Map<ReceptorGroup, Long>, currentTimeMs: Long): Map<ReceptorGroup, ToleranceStatus>`
    - `fun getStatusForSubstance(substanceId: String, ingestions: List<Ingestion>, manualOverrides: Map<ReceptorGroup, Long>, currentTimeMs: Long): ToleranceStatus?`
  - `}`

- [ ] **Step 1: Write failing unit tests for ToleranceCalculator**

Create `app/src/test/java/info/hyperreal/journal/domain/usecase/ToleranceCalculatorTest.kt`:
```kotlin
package info.hyperreal.journal.domain.usecase

import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.model.ReceptorGroup
import info.hyperreal.journal.domain.model.ToleranceRiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class ToleranceCalculatorTest {

    private lateinit var calculator: ToleranceCalculator
    private val now = 1_700_000_000_000L // arbitrary fixed time

    @Before
    fun setup() {
        calculator = ToleranceCalculator()
    }

    @Test
    fun `mapSubstanceToGroup maps psychedelics correctly`() {
        assertEquals(ReceptorGroup.SEROTONIN_2A, calculator.mapSubstanceToGroup("lsd"))
        assertEquals(ReceptorGroup.SEROTONIN_2A, calculator.mapSubstanceToGroup("psilocybin"))
        assertEquals(ReceptorGroup.SEROTONIN_2A, calculator.mapSubstanceToGroup("grzyby"))
        assertEquals(ReceptorGroup.SEROTONIN_2A, calculator.mapSubstanceToGroup("2c-b"))
        assertEquals(ReceptorGroup.SEROTONIN_2A, calculator.mapSubstanceToGroup("dmt"))
    }

    @Test
    fun `mapSubstanceToGroup maps mdma correctly`() {
        assertEquals(ReceptorGroup.MDMA_SERT, calculator.mapSubstanceToGroup("mdma"))
        assertEquals(ReceptorGroup.MDMA_SERT, calculator.mapSubstanceToGroup("mda"))
    }

    @Test
    fun `mapSubstanceToGroup maps dissociatives correctly`() {
        assertEquals(ReceptorGroup.DISSOCIATIVE_NMDA, calculator.mapSubstanceToGroup("ketamine"))
        assertEquals(ReceptorGroup.DISSOCIATIVE_NMDA, calculator.mapSubstanceToGroup("dxm"))
    }

    @Test
    fun `mapSubstanceToGroup maps opioids correctly`() {
        assertEquals(ReceptorGroup.OPIOID_MOR, calculator.mapSubstanceToGroup("morphine"))
        assertEquals(ReceptorGroup.OPIOID_MOR, calculator.mapSubstanceToGroup("oxycodone"))
        assertEquals(ReceptorGroup.OPIOID_MOR, calculator.mapSubstanceToGroup("codeine"))
        assertEquals(ReceptorGroup.OPIOID_MOR, calculator.mapSubstanceToGroup("fentanyl"))
        assertEquals(ReceptorGroup.OPIOID_MOR, calculator.mapSubstanceToGroup("tramadol"))
    }

    @Test
    fun `mapSubstanceToGroup maps gaba sedatives correctly`() {
        assertEquals(ReceptorGroup.GABA_SEDATIVE, calculator.mapSubstanceToGroup("alprazolam"))
        assertEquals(ReceptorGroup.GABA_SEDATIVE, calculator.mapSubstanceToGroup("diazepam"))
        assertEquals(ReceptorGroup.GABA_SEDATIVE, calculator.mapSubstanceToGroup("ethanol"))
        assertEquals(ReceptorGroup.GABA_SEDATIVE, calculator.mapSubstanceToGroup("alkohol"))
    }

    @Test
    fun `empty ingestions and no override returns baseline SAFE status`() {
        val status = calculator.calculateStatus(
            group = ReceptorGroup.SEROTONIN_2A,
            ingestions = emptyList(),
            manualOverrideTime = null,
            currentTimeMs = now
        )

        assertEquals(100, status.resetProgressPercent)
        assertEquals(1.0f, status.estimatedDoseMultiplier ?: 0f, 0.01f)
        assertEquals(ToleranceRiskLevel.SAFE, status.riskLevel)
        assertNull(status.daysSinceLast)
    }

    @Test
    fun `psychedelic tolerance curve 0 days returns ~280 percent dose multiplier and DANGER`() {
        val ingestion = Ingestion(
            id = 1,
            substanceId = "lsd",
            roa = "oral",
            doseAmount = 100f,
            doseUnit = "ug",
            timestamp = now
        )

        val status = calculator.calculateStatus(
            group = ReceptorGroup.SEROTONIN_2A,
            ingestions = listOf(ingestion),
            manualOverrideTime = null,
            currentTimeMs = now
        )

        assertEquals(0, status.resetProgressPercent)
        assertEquals(2.80f, status.estimatedDoseMultiplier ?: 0f, 0.05f)
        assertEquals(ToleranceRiskLevel.DANGER, status.riskLevel)
    }

    @Test
    fun `psychedelic tolerance curve 7 days returns ~115 percent dose multiplier and NOTICE`() {
        val sevenDaysAgo = now - TimeUnit.DAYS.toMillis(7)
        val ingestion = Ingestion(
            id = 1,
            substanceId = "psilocybin",
            roa = "oral",
            doseAmount = 2f,
            doseUnit = "g",
            timestamp = sevenDaysAgo
        )

        val status = calculator.calculateStatus(
            group = ReceptorGroup.SEROTONIN_2A,
            ingestions = listOf(ingestion),
            manualOverrideTime = null,
            currentTimeMs = now
        )

        assertTrue(status.resetProgressPercent in 80..95)
        assertEquals(1.15f, status.estimatedDoseMultiplier ?: 0f, 0.06f)
        assertEquals(ToleranceRiskLevel.NOTICE, status.riskLevel)
    }

    @Test
    fun `psychedelic cross-tolerance applies between psilocybin and lsd`() {
        val twoDaysAgo = now - TimeUnit.DAYS.toMillis(2)
        val ingestion = Ingestion(
            id = 1,
            substanceId = "grzyby",
            roa = "oral",
            doseAmount = 2.5f,
            doseUnit = "g",
            timestamp = twoDaysAgo
        )

        val statusForLsd = calculator.getStatusForSubstance(
            substanceId = "lsd",
            ingestions = listOf(ingestion),
            manualOverrides = emptyMap(),
            currentTimeMs = now
        )

        assertNotNull(statusForLsd)
        assertEquals(ReceptorGroup.SEROTONIN_2A, statusForLsd?.group)
        assertTrue(statusForLsd!!.estimatedDoseMultiplier!! > 1.5f)
    }

    @Test
    fun `mdma 3 month rule evaluates risk levels accurately`() {
        // 10 days ago -> DANGER
        val tenDaysAgo = now - TimeUnit.DAYS.toMillis(10)
        val ing1 = Ingestion(id = 1, substanceId = "mdma", roa = "oral", doseAmount = 120f, doseUnit = "mg", timestamp = tenDaysAgo)
        val status1 = calculator.calculateStatus(ReceptorGroup.MDMA_SERT, listOf(ing1), null, now)
        assertEquals(ToleranceRiskLevel.DANGER, status1.riskLevel)

        // 45 days ago -> WARNING
        val fortyFiveDaysAgo = now - TimeUnit.DAYS.toMillis(45)
        val ing2 = Ingestion(id = 2, substanceId = "mdma", roa = "oral", doseAmount = 120f, doseUnit = "mg", timestamp = fortyFiveDaysAgo)
        val status2 = calculator.calculateStatus(ReceptorGroup.MDMA_SERT, listOf(ing2), null, now)
        assertEquals(ToleranceRiskLevel.WARNING, status2.riskLevel)

        // 95 days ago -> SAFE
        val ninetyFiveDaysAgo = now - TimeUnit.DAYS.toMillis(95)
        val ing3 = Ingestion(id = 3, substanceId = "mdma", roa = "oral", doseAmount = 120f, doseUnit = "mg", timestamp = ninetyFiveDaysAgo)
        val status3 = calculator.calculateStatus(ReceptorGroup.MDMA_SERT, listOf(ing3), null, now)
        assertEquals(ToleranceRiskLevel.SAFE, status3.riskLevel)
        assertEquals(100, status3.resetProgressPercent)
    }

    @Test
    fun `opioid consecutive days detects streaks and flags danger`() {
        val day0 = now - TimeUnit.DAYS.toMillis(2)
        val day1 = now - TimeUnit.DAYS.toMillis(1)
        val day2 = now

        val ing0 = Ingestion(id = 1, substanceId = "oxycodone", roa = "oral", doseAmount = 20f, doseUnit = "mg", timestamp = day0)
        val ing1 = Ingestion(id = 2, substanceId = "morphine", roa = "oral", doseAmount = 30f, doseUnit = "mg", timestamp = day1)
        val ing2 = Ingestion(id = 3, substanceId = "codeine", roa = "oral", doseAmount = 150f, doseUnit = "mg", timestamp = day2)

        val status = calculator.calculateStatus(ReceptorGroup.OPIOID_MOR, listOf(ing0, ing1, ing2), null, now)
        assertEquals(3, status.consecutiveDays)
        assertEquals(ToleranceRiskLevel.DANGER, status.riskLevel)
    }

    @Test
    fun `manual override takes precedence when more recent than database`() {
        val tenDaysAgo = now - TimeUnit.DAYS.toMillis(10)
        val twoDaysAgo = now - TimeUnit.DAYS.toMillis(2)

        val ing = Ingestion(id = 1, substanceId = "mdma", roa = "oral", doseAmount = 120f, doseUnit = "mg", timestamp = tenDaysAgo)
        val status = calculator.calculateStatus(
            group = ReceptorGroup.MDMA_SERT,
            ingestions = listOf(ing),
            manualOverrideTime = twoDaysAgo,
            currentTimeMs = now
        )

        assertEquals(twoDaysAgo, status.lastIngestionTime)
        assertEquals(ToleranceRiskLevel.DANGER, status.riskLevel)
    }
}
```

- [ ] **Step 2: Run test to verify it fails (class ToleranceCalculator does not exist yet)**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest --tests info.hyperreal.journal.domain.usecase.ToleranceCalculatorTest`
Expected: Compilation failure or FAIL.

- [ ] **Step 3: Implement ToleranceCalculator**

Create `app/src/main/java/info/hyperreal/journal/domain/usecase/ToleranceCalculator.kt`:
```kotlin
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

        // Extract day-of-year epochs
        val calendar = Calendar.getInstance()
        val uniqueDaysEpoch = ingestions.map {
            calendar.timeInMillis = it.timestamp
            "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
        }.distinct()

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
```

- [ ] **Step 4: Run test to verify it passes**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest --tests info.hyperreal.journal.domain.usecase.ToleranceCalculatorTest`
Expected: 100% PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/domain/usecase/ToleranceCalculator.kt app/src/test/java/info/hyperreal/journal/domain/usecase/ToleranceCalculatorTest.kt
git commit -m "feat: implement ToleranceCalculator with pharmacodynamic curves and unit tests"
```

---

### Task 3: DataStore Persistence for Manual Date Overrides

**Files:**
- Modify: `app/src/main/java/info/hyperreal/journal/data/local/datastore/UserPreferencesRepository.kt`

**Interfaces:**
- Consumes: `ReceptorGroup`
- Produces:
  - `val toleranceOverridesFlow: Flow<Map<ReceptorGroup, Long>>`
  - `suspend fun setToleranceOverride(group: ReceptorGroup, timestampMs: Long)`
  - `suspend fun clearToleranceOverride(group: ReceptorGroup)`

- [ ] **Step 1: Add tolerance override preferences to UserPreferencesRepository**

Edit `app/src/main/java/info/hyperreal/journal/data/local/datastore/UserPreferencesRepository.kt`:
Add preference keys for each `ReceptorGroup`:
- `tolerance_override_5ht2a`
- `tolerance_override_mdma`
- `tolerance_override_dissociative`
- `tolerance_override_opioid`
- `tolerance_override_gaba`
Expose `val toleranceOverridesFlow: Flow<Map<ReceptorGroup, Long>>` and methods `setToleranceOverride`, `clearToleranceOverride`.

- [ ] **Step 2: Run all unit tests to ensure compilation and no regression**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest`
Expected: All tests PASS.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/data/local/datastore/UserPreferencesRepository.kt
git commit -m "feat: add tolerance date override persistence in UserPreferencesRepository"
```

---

### Task 4: Insights Screen Receptor Recovery Dashboard

**Files:**
- Modify: `app/src/main/java/info/hyperreal/journal/ui/insights/InsightsViewModel.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/ui/insights/InsightsScreen.kt`

**Interfaces:**
- Consumes: `ToleranceCalculator`, `IngestionRepository`, `UserPreferencesRepository`
- Produces:
  - In `InsightsViewModel`:
    - `val toleranceStatuses: StateFlow<List<ToleranceStatus>>`
    - `fun setToleranceOverride(group: ReceptorGroup, timestamp: Long)`
    - `fun clearToleranceOverride(group: ReceptorGroup)`
  - In `InsightsScreen`:
    - UI section "Stan Receptorów i Odstępy (Harm Reduction)" with animated progress bars, multiplier badges, risk chips, and DatePickerDialog for manual date adjustment.

- [ ] **Step 1: Update InsightsViewModel**

Inject `ToleranceCalculator` and observe both `getAllIngestions()` and `toleranceOverridesFlow`, combining them into `toleranceStatuses: StateFlow<List<ToleranceStatus>>`.

- [ ] **Step 2: Update InsightsScreen with Receptor Recovery Cards**

Add composable `ReceptorStatusCard` and list in `InsightsScreen`. Include date picker for manual adjustment.

- [ ] **Step 3: Verify compilation & run tests**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew compileDebugKotlin testDebugUnitTest`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/ui/insights/InsightsViewModel.kt app/src/main/java/info/hyperreal/journal/ui/insights/InsightsScreen.kt
git commit -m "feat: add Receptor Recovery dashboard to InsightsScreen"
```

---

### Task 5: EnterDoseScreen Harm Reduction Contextual Alert

**Files:**
- Modify: `app/src/main/java/info/hyperreal/journal/ui/addingestion/AddIngestionViewModel.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/ui/addingestion/AddIngestionScreens.kt`

**Interfaces:**
- Consumes: `ToleranceCalculator`, `ToleranceStatus`
- Produces:
  - In `AddIngestionViewModel`:
    - `val toleranceStatus: StateFlow<ToleranceStatus?>`
  - In `AddIngestionScreens` (`EnterDoseScreen`):
    - `ToleranceAlertCard` displayed when `toleranceStatus != null && toleranceStatus.resetProgressPercent < 100` (or `riskLevel != SAFE`).

- [ ] **Step 1: Update AddIngestionViewModel**

Inject `ToleranceCalculator` into `AddIngestionViewModel`. When `selectedSubstance` changes or when view is initialized, calculate `toleranceStatus` using latest ingestions and DataStore overrides.

- [ ] **Step 2: Add ToleranceAlertCard into EnterDoseScreen**

Display warning card with reset percentage, multiplier (if psychedelic), risk badge, and harm reduction recommendations.

- [ ] **Step 3: Verify compilation & run tests**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/ui/addingestion/AddIngestionViewModel.kt app/src/main/java/info/hyperreal/journal/ui/addingestion/AddIngestionScreens.kt
git commit -m "feat: add contextual tolerance and harm reduction warning in EnterDoseScreen"
```

---

### Task 6: End-to-End Verification & Full Regression Suite

**Files:**
- Check: All modified and new files.

- [ ] **Step 1: Run complete unit test suite**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest`
Expected: All 70+ unit tests PASS (100% green).

- [ ] **Step 2: Run Android Lint or assembleDebug**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Update walkthrough and progress tracking**

Commit progress and update `docs/superpowers/specs/` and `walkthrough.md`.
