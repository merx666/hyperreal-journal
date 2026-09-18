# Opioids Expansion & Dose Calculators Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rozbudowa bazy danych substancji o kluczowe opioidy (Kodeina, Morfina, Oksykodon, Fentanyl, Buprenorfina, Metadon) wraz z ich profilami ROA/Harm Reduction oraz wdrożenie dynamicznych kalkulatorów dawek (THC dla suszu konopnego oraz czystego etanolu dla alkoholu) bezpośrednio w interfejsie dodawania wpisu.

**Architecture:** Nowy Use Case `DoseConverter` odpowiedzialny za precyzyjną matematykę przeliczeń i formatowanie adnotacji do notatek. Ekran `EnterDoseScreen` wzbogacony o kontekstowe komponenty UI (kalkulator suszu THC oraz presety napojów alkoholowych), przekazujące dawkę i adnotacje do `AddIngestionViewModel`. Baza `substances.json` zasilana nowymi encjami mapowanymi na kategorię interakcji SIN `Opioidy`.

**Tech Stack:** Kotlin 2.0, Jetpack Compose, Material 3, Hilt, JUnit 4, Turbine, MockK.

## Global Constraints
- Język interfejsu i komunikatów: wyłącznie polski (`pl`).
- Architektura offline-first (wszystkie dane lokalne w `assets/substances.json` i Room).
- Wzorce Clean Architecture + TDD (najpierw testy jednostkowe, potem implementacja).
- Przelicznik gęstości etanolu: 0.8 g/ml.
- Przelicznik THC: $\text{waga (g)} \times (\%\text{ THC} / 100) \times 1000\text{ mg}$.

---

### Task 1: Use Case `DoseConverter` i testy jednostkowe (TDD)

**Files:**
- Create: `app/src/main/java/info/hyperreal/journal/domain/usecase/DoseConverter.kt`
- Create: `app/src/test/java/info/hyperreal/journal/domain/usecase/DoseConverterTest.kt`

**Interfaces:**
- Produces:
  - `DoseConverter.calculateThcMg(dryHerbGrams: Float, thcPercent: Float): Float`
  - `DoseConverter.formatThcNote(dryHerbGrams: Float, thcPercent: Float): String`
  - `DoseConverter.calculateEthanolGrams(volumeMl: Float, alcoholPercent: Float): Float`
  - `DoseConverter.formatAlcoholNote(beverageName: String, volumeMl: Float, alcoholPercent: Float): String`

- [ ] **Step 1: Write the failing unit tests for `DoseConverter`**
- [ ] **Step 2: Run test to verify it fails**
- [ ] **Step 3: Implement `DoseConverter`**
- [ ] **Step 4: Run test to verify it passes**
- [ ] **Step 5: Commit**

---

### Task 2: Rozszerzenie bazy `substances.json` o gałąź opioidów

**Files:**
- Modify: `app/src/main/assets/substances.json`
- Test: `app/src/test/java/info/hyperreal/journal/data/repository/SubstanceRepositoryImplTest.kt`

- [ ] **Step 1: Write verification test for newly added opioid substances**
- [ ] **Step 2: Append complete JSON definitions to `app/src/main/assets/substances.json`**
- [ ] **Step 3: Run tests to verify parsing**
- [ ] **Step 4: Commit**

---

### Task 3: Rozszerzenie `InteractionChecker` o bezpośrednie mapowanie nowych opioidów

**Files:**
- Modify: `app/src/main/java/info/hyperreal/journal/domain/usecase/InteractionChecker.kt`
- Modify: `app/src/test/java/info/hyperreal/journal/domain/usecase/InteractionCheckerTest.kt`

- [ ] **Step 1: Write unit test in `InteractionCheckerTest.kt`**
- [ ] **Step 2: Update `InteractionChecker` aliases map**
- [ ] **Step 3: Run tests to verify**
- [ ] **Step 4: Commit**

---

### Task 4: UI Kalkulatora Dawek w `EnterDoseScreen` i `AddIngestionViewModel`

**Files:**
- Modify: `app/src/main/java/info/hyperreal/journal/ui/addingestion/AddIngestionViewModel.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/ui/addingestion/AddIngestionScreens.kt`

- [ ] **Step 1: Update `AddIngestionViewModel.kt`**
- [ ] **Step 2: Update `EnterDoseScreen.kt` with interactive cards for THC and Alcohol**
- [ ] **Step 3: Verify build and manual UI flow**
- [ ] **Step 4: Commit**

---

### Task 5: Zapisanie artefaktu z promptem Grok-bota i weryfikacja całości

**Files:**
- Create: `brain/.../grokbot_prompt_opioids.md`
- Run full test suite: `./gradlew testDebugUnitTest`

- [ ] **Step 1: Utworzenie artefaktu `grokbot_prompt_opioids.md`**
- [ ] **Step 2: Uruchomienie pełnego zestawu testów jednostkowych**
- [ ] **Step 3: Commit i podsumowanie**
