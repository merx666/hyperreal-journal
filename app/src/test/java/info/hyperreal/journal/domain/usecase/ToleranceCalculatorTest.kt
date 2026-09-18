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
