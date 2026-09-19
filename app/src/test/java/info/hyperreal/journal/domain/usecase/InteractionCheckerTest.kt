package info.hyperreal.journal.domain.usecase

import info.hyperreal.journal.domain.model.Ingestion
import info.hyperreal.journal.domain.model.Interaction
import info.hyperreal.journal.domain.model.Substance
import org.junit.Assert.*
import org.junit.Test

class InteractionCheckerTest {

    private val checker = InteractionChecker()

    private fun makeSubstance(
        id: String,
        name: String,
        interactions: List<Interaction> = emptyList()
    ) = Substance(
        id = id,
        name = name,
        interactions = interactions
    )

    private fun makeIngestion(
        substanceId: String,
        timestamp: Long = System.currentTimeMillis() - 3600_000 // 1 hour ago
    ) = Ingestion(
        id = 1,
        substanceId = substanceId,
        roa = "Oral",
        doseAmount = 50f,
        doseUnit = "mg",
        timestamp = timestamp,
        notes = ""
    )

    // --- Basic functionality ---

    @Test
    fun `returns empty list when no recent ingestions`() {
        val newSub = makeSubstance("mdma", "MDMA")
        val result = checker.checkInteractions(newSub, emptyList(), listOf(newSub))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty list when no interactions defined`() {
        val mdma = makeSubstance("mdma", "MDMA")
        val alcohol = makeSubstance("alcohol", "Alkohol")
        val recentIngestion = makeIngestion("alcohol")

        val result = checker.checkInteractions(mdma, listOf(recentIngestion), listOf(mdma, alcohol))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `detects forward interaction (new substance defines interaction with past)`() {
        val alcohol = makeSubstance("alcohol", "Alkohol")
        val mdma = makeSubstance(
            "mdma", "MDMA",
            interactions = listOf(
                Interaction(substanceId = "alcohol", status = "Dangerous", notes = "Dehydration risk")
            )
        )
        val recentIngestion = makeIngestion("alcohol")

        val result = checker.checkInteractions(mdma, listOf(recentIngestion), listOf(mdma, alcohol))
        assertEquals(1, result.size)
        assertEquals("Dangerous", result[0].status)
        assertEquals("Alkohol", result[0].pastSubstanceName)
        assertEquals("Dehydration risk", result[0].notes)
    }

    @Test
    fun `detects backward interaction (past substance defines interaction with new)`() {
        val mdma = makeSubstance("mdma", "MDMA")
        val alcohol = makeSubstance(
            "alcohol", "Alkohol",
            interactions = listOf(
                Interaction(substanceId = "mdma", status = "Unsafe", notes = "Liver stress")
            )
        )
        val recentIngestion = makeIngestion("alcohol")

        val result = checker.checkInteractions(mdma, listOf(recentIngestion), listOf(mdma, alcohol))
        assertEquals(1, result.size)
        assertEquals("Unsafe", result[0].status)
    }

    // --- Severity sorting ---

    @Test
    fun `results sorted by severity - Dangerous first, Caution last`() {
        val sub1 = makeSubstance(
            "sub1", "Sub1",
            interactions = listOf(
                Interaction(substanceId = "new", status = "Caution", notes = null)
            )
        )
        val sub2 = makeSubstance(
            "sub2", "Sub2",
            interactions = listOf(
                Interaction(substanceId = "new", status = "Dangerous", notes = null)
            )
        )
        val sub3 = makeSubstance(
            "sub3", "Sub3",
            interactions = listOf(
                Interaction(substanceId = "new", status = "Unsafe", notes = null)
            )
        )

        val newSub = makeSubstance("new", "New Substance")
        val ingestions = listOf(
            makeIngestion("sub1"),
            makeIngestion("sub2"),
            makeIngestion("sub3")
        )

        val result = checker.checkInteractions(
            newSub, ingestions, listOf(newSub, sub1, sub2, sub3)
        )

        assertEquals(3, result.size)
        assertEquals("Dangerous", result[0].status)  // severity 3
        assertEquals("Unsafe", result[1].status)      // severity 2
        assertEquals("Caution", result[2].status)      // severity 1
    }

    // --- Filtering ---

    @Test
    fun `does not warn for low-risk synergy`() {
        val cannabis = makeSubstance(
            "cannabis", "Kannabinoidy",
            interactions = listOf(
                Interaction(substanceId = "new", status = "Low Risk & Synergy", notes = null)
            )
        )
        val newSub = makeSubstance("new", "New")
        val ingestions = listOf(makeIngestion("cannabis"))

        val result = checker.checkInteractions(newSub, ingestions, listOf(newSub, cannabis))
        assertTrue("Low Risk & Synergy should not produce warnings", result.isEmpty())
    }

    @Test
    fun `does not warn for low-risk no synergy`() {
        val sub = makeSubstance(
            "sub", "Sub",
            interactions = listOf(
                Interaction(substanceId = "new", status = "Low Risk & No Synergy", notes = null)
            )
        )
        val newSub = makeSubstance("new", "New")
        val result = checker.checkInteractions(newSub, listOf(makeIngestion("sub")), listOf(newSub, sub))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `warns for serotonin syndrome`() {
        val ssri = makeSubstance(
            "ssri", "SSRI",
            interactions = listOf(
                Interaction(substanceId = "new", status = "Serotonin Syndrome", notes = "Critical")
            )
        )
        val newSub = makeSubstance("new", "MDMA")
        val result = checker.checkInteractions(newSub, listOf(makeIngestion("ssri")), listOf(newSub, ssri))
        assertEquals(1, result.size)
    }

    // --- Edge cases ---

    @Test
    fun `handles unknown past substance gracefully`() {
        val newSub = makeSubstance("new", "New")
        // Ingestion references substance that's not in knownSubstances
        val ingestion = makeIngestion("unknown-sub")

        val result = checker.checkInteractions(newSub, listOf(ingestion), listOf(newSub))
        assertTrue("Should not crash on unknown substance", result.isEmpty())
    }

    @Test
    fun `forward interaction takes priority - no duplicate when both directions defined`() {
        val pastSub = makeSubstance(
            "past", "Past",
            interactions = listOf(
                Interaction(substanceId = "new", status = "Caution", notes = "backward")
            )
        )
        val newSub = makeSubstance(
            "new", "New",
            interactions = listOf(
                Interaction(substanceId = "past", status = "Dangerous", notes = "forward")
            )
        )
        val ingestions = listOf(makeIngestion("past"))

        val result = checker.checkInteractions(newSub, ingestions, listOf(newSub, pastSub))
        // Forward interaction found first → continue skips backward check
        assertEquals(1, result.size)
        assertEquals("Dangerous", result[0].status)
        assertEquals("forward", result[0].notes)
    }

    // --- SIN database & Aliases ---

    @Test
    fun `detects interaction from SIN database list`() {
        val alcohol = makeSubstance("alcohol", "Alkohol")
        val ghb = makeSubstance("ghb", "GHB/GBL")
        val ingestion = makeIngestion("alcohol")

        val sinInteractions = listOf(
            info.hyperreal.journal.domain.model.SubstanceInteraction(
                substanceA = "Alkohol",
                substanceB = "GHB/GBL",
                status = info.hyperreal.journal.domain.model.InteractionStatus.DANGEROUS,
                note = "Zagrożenie depresją oddechową"
            )
        )

        val result = checker.checkInteractions(
            newSubstance = ghb,
            recentIngestions = listOf(ingestion),
            knownSubstances = listOf(alcohol, ghb),
            sinInteractions = sinInteractions
        )

        assertEquals(1, result.size)
        assertEquals("Dangerous", result[0].status)
        assertEquals("Alkohol", result[0].pastSubstanceName)
        assertEquals("Zagrożenie depresją oddechową", result[0].notes)
    }

    @Test
    fun `detects SIN interaction via alias`() {
        val mdma = Substance(
            id = "mdma",
            name = "MDMA",
            aliases = listOf("Ekstaza", "Molly")
        )
        val ssri = Substance(
            id = "ssri",
            name = "SSRI",
            aliases = listOf("Sertralina", "Fluoksetyna")
        )
        val ingestion = makeIngestion("ssri")

        val sinInteractions = listOf(
            info.hyperreal.journal.domain.model.SubstanceInteraction(
                substanceA = "MDMA",
                substanceB = "SSRI",
                status = info.hyperreal.journal.domain.model.InteractionStatus.UNSAFE,
                note = "Ryzyko zespołu serotoninowego i stłumienie efektów"
            )
        )

        val result = checker.checkInteractions(
            newSubstance = mdma,
            recentIngestions = listOf(ingestion),
            knownSubstances = listOf(ssri, mdma),
            sinInteractions = sinInteractions
        )

        assertEquals(1, result.size)
        assertEquals("Unsafe", result[0].status)
    }

    @Test
    fun `detects SIN interaction for specific opioid like Oksykodon against Alkohol`() {
        val oxy = Substance(
            id = "Oksykodon",
            name = "Oksykodon",
            classes = listOf("Opioid", "Depresant"),
            aliases = listOf("Oxycodone", "OxyContin")
        )
        val alcohol = Substance(
            id = "Alkohol",
            name = "Alkohol",
            classes = listOf("Depresant"),
            aliases = listOf("Etanol")
        )
        val ingestion = makeIngestion("Alkohol")

        val sinInteractions = listOf(
            info.hyperreal.journal.domain.model.SubstanceInteraction(
                substanceA = "Alkohol",
                substanceB = "Opioidy",
                status = info.hyperreal.journal.domain.model.InteractionStatus.DANGEROUS,
                note = "Ciężka depresja oddechowa"
            )
        )

        val result = checker.checkInteractions(
            newSubstance = oxy,
            recentIngestions = listOf(ingestion),
            knownSubstances = listOf(alcohol, oxy),
            sinInteractions = sinInteractions
        )

        assertEquals(1, result.size)
        assertEquals("Dangerous", result[0].status)
        assertEquals("Alkohol", result[0].pastSubstanceName)
        assertEquals("Ciężka depresja oddechowa", result[0].notes)
    }


    @Test
    fun `ignores past ingestions outside max window`() {
        val alcohol = makeSubstance("alcohol", "Alkohol")
        val ghb = makeSubstance("ghb", "GHB/GBL")
        // 48 hours ago
        val oldIngestion = makeIngestion("alcohol", timestamp = System.currentTimeMillis() - 48 * 3600_000L)

        val sinInteractions = listOf(
            info.hyperreal.journal.domain.model.SubstanceInteraction(
                substanceA = "Alkohol",
                substanceB = "GHB/GBL",
                status = info.hyperreal.journal.domain.model.InteractionStatus.DANGEROUS,
                note = "Zagrożenie"
            )
        )

        val result = checker.checkInteractions(
            newSubstance = ghb,
            recentIngestions = listOf(oldIngestion),
            knownSubstances = listOf(alcohol, ghb),
            sinInteractions = sinInteractions,
            maxWindowHours = 24
        )

        assertTrue("Old ingestion (>24h) should not trigger active warning", result.isEmpty())
    }

    @Test
    fun `detects SIN interaction for Metylofenidat against MAOI as Dangerous`() {
        val mph = Substance(
            id = "Metylofenidat IR",
            name = "Metylofenidat IR",
            classes = listOf("Stymulant", "Fenidat", "NDRI"),
            aliases = listOf("Medikinet", "Ritalin")
        )
        val maoi = Substance(
            id = "MAOI",
            name = "MAOI",
            classes = listOf("Lek"),
            aliases = listOf("Inhibitory MAO")
        )
        val pastMaoi = makeIngestion("MAOI")

        val sinInteractions = listOf(
            info.hyperreal.journal.domain.model.SubstanceInteraction(
                substanceA = "Amfetamina",
                substanceB = "MAOI",
                status = info.hyperreal.journal.domain.model.InteractionStatus.DANGEROUS,
                note = "Krytyczne ryzyko przełomu nadciśnieniowego i zawału"
            )
        )

        val result = checker.checkInteractions(
            newSubstance = mph,
            recentIngestions = listOf(pastMaoi),
            knownSubstances = listOf(maoi, mph),
            sinInteractions = sinInteractions
        )

        assertEquals(1, result.size)
        assertEquals("Dangerous", result[0].status)
        assertEquals("MAOI", result[0].pastSubstanceName)
        assertEquals("Krytyczne ryzyko przełomu nadciśnieniowego i zawału", result[0].notes)
    }

    @Test
    fun `does not map non-methylphenidate NDRI such as bupropion to Amphetamine`() {
        val bupropion = Substance(
            id = "bupropion",
            name = "Bupropion",
            classes = listOf("Antydepresant", "NDRI"),
            aliases = listOf("Wellbutrin", "Zyban")
        )
        val maoi = Substance(
            id = "MAOI",
            name = "MAOI",
            classes = listOf("Lek"),
            aliases = listOf("Inhibitory MAO")
        )
        val pastMaoi = makeIngestion("MAOI")

        val sinInteractions = listOf(
            info.hyperreal.journal.domain.model.SubstanceInteraction(
                substanceA = "Amfetamina",
                substanceB = "MAOI",
                status = info.hyperreal.journal.domain.model.InteractionStatus.DANGEROUS,
                note = "Krytyczne ryzyko przełomu nadciśnieniowego i zawału"
            )
        )

        val result = checker.checkInteractions(
            newSubstance = bupropion,
            recentIngestions = listOf(pastMaoi),
            knownSubstances = listOf(maoi, bupropion),
            sinInteractions = sinInteractions
        )

        assertTrue("Bupropion is an NDRI but NOT methylphenidate, so it must not inherit Amphetamine interactions", result.isEmpty())
    }
}
