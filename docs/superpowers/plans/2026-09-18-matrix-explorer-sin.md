# Matrix Explorer (Pełna Interaktywna Macierz Interakcji SIN) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a comprehensive, interactive 100% offline Explorer for the 333 interactions and 27 substance classes from the SIN (Społeczna Inicjatywa Narkopolityki) / TripSit matrix, with risk-level filtering, substance focus profiling, full-text clinical search, and an interactive Harm Reduction First Aid modal sheet.

**Architecture:** Clean Architecture + MVI in Jetpack Compose. Expanded `InteractionRepository`, dedicated `MatrixExplorerViewModel` with reactive Kotlin Coroutine StateFlow, Material 3 `PrimaryTabRow` in `MixCalculatorScreen`, and direct Drawer navigation.

**Tech Stack:** Kotlin, Jetpack Compose (Material 3), Kotlin Coroutines & StateFlow, Hilt, JUnit 4, Turbine, MockK.

## Global Constraints

- **Language & Locale:** All user-facing strings in Polish (`pl`).
- **Offline First:** 100% offline, zero network dependencies.
- **Java Home for Builds:** `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.
- **Test Integrity:** All 80 existing tests must remain 100% green.
- **TDD:** Write tests, verify failure, implement code, verify green, commit.

---

### Task 1: Repository Interface & Implementation Expansion

**Files:**
- Modify: `app/src/main/java/info/hyperreal/journal/domain/repository/InteractionRepository.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/data/repository/InteractionRepositoryImpl.kt`
- Create: `app/src/test/java/info/hyperreal/journal/data/repository/InteractionRepositoryTest.kt`

**Interfaces:**
- Produces:
  - `suspend fun getMatrixSubstances(): List<String>`
  - `suspend fun getInteractionsForSubstance(substance: String): List<SubstanceInteraction>`

- [ ] **Step 1: Write unit tests for expanded InteractionRepository**

Create `app/src/test/java/info/hyperreal/journal/data/repository/InteractionRepositoryTest.kt` testing that:
- `getMatrixSubstances()` returns distinct sorted list of substances.
- `getInteractionsForSubstance(substance)` returns all interactions where the substance is A or B, sorted by severity.

- [ ] **Step 2: Update InteractionRepository interface**

Update `InteractionRepository.kt` with the new signatures.

- [ ] **Step 3: Implement new methods in InteractionRepositoryImpl**

Update `InteractionRepositoryImpl.kt` to extract unique substances and filter by substance.

- [ ] **Step 4: Run unit tests to verify green**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/domain/repository/InteractionRepository.kt app/src/main/java/info/hyperreal/journal/data/repository/InteractionRepositoryImpl.kt app/src/test/java/info/hyperreal/journal/data/repository/InteractionRepositoryTest.kt
git commit -m "feat: add getMatrixSubstances and getInteractionsForSubstance to InteractionRepository"
```

---

### Task 2: MatrixExplorerViewModel with TDD Unit Tests

**Files:**
- Create: `app/src/test/java/info/hyperreal/journal/ui/matrix/MatrixExplorerViewModelTest.kt`
- Create: `app/src/main/java/info/hyperreal/journal/ui/matrix/MatrixExplorerViewModel.kt`

**Interfaces:**
- Produces:
  - `data class MatrixExplorerUiState(...)`
  - `class MatrixExplorerViewModel` with:
    - `fun selectSubstance(substance: String?)`
    - `fun setStatusFilter(status: InteractionStatus?)`
    - `fun setSearchQuery(query: String)`
    - `fun showInteractionDetail(interaction: SubstanceInteraction?)`
    - `fun resetFilters()`

- [ ] **Step 1: Write unit tests for MatrixExplorerViewModel**

Create `app/src/test/java/info/hyperreal/journal/ui/matrix/MatrixExplorerViewModelTest.kt` testing:
- Initial state loads all interactions, unique substances, and calculates correct status counts.
- Selecting a substance filters to only pairs containing that substance.
- Setting a status filter (e.g. `DANGEROUS`) filters to only matching interactions.
- Typing a search query filters by substance names or notes.
- Combining substance + status + search query works correctly.

- [ ] **Step 2: Implement MatrixExplorerViewModel**

Create `app/src/main/java/info/hyperreal/journal/ui/matrix/MatrixExplorerViewModel.kt`.

- [ ] **Step 3: Run unit tests to verify green**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/ui/matrix/MatrixExplorerViewModel.kt app/src/test/java/info/hyperreal/journal/ui/matrix/MatrixExplorerViewModelTest.kt
git commit -m "feat: implement MatrixExplorerViewModel with comprehensive unit tests"
```

---

### Task 3: Compose UI Components for Matrix Explorer

**Files:**
- Create: `app/src/main/java/info/hyperreal/journal/ui/matrix/MatrixExplorerScreen.kt`

**Interfaces:**
- Produces:
  - `MatrixExplorerContent` composable
  - `InteractionDetailBottomSheet` composable
  - Harm Reduction first aid protocols helper

- [ ] **Step 1: Implement MatrixExplorerScreen and sub-components**

Create `app/src/main/java/info/hyperreal/journal/ui/matrix/MatrixExplorerScreen.kt` including:
- Search text field (`OutlinedTextField`)
- Scrollable row of severity `FilterChip`s with counts
- Scrollable row of substance chips
- `LazyColumn` of interaction cards with severity color indicators
- `InteractionDetailBottomSheet` modal with pharmacological mechanism and first aid harm reduction guidance.

- [ ] **Step 2: Verify compilation & tests**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/ui/matrix/MatrixExplorerScreen.kt
git commit -m "feat: add MatrixExplorerScreen UI with detail bottom sheet and harm reduction protocols"
```

---

### Task 4: Main Navigation & MixCalculatorScreen Tab Integration

**Files:**
- Modify: `app/src/main/java/info/hyperreal/journal/ui/navigation/Screen.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/ui/mixcalculator/MixCalculatorScreen.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/ui/MainScreen.kt`

**Interfaces:**
- Produces:
  - `Screen.MatrixExplorer` route
  - `PrimaryTabRow` in `MixCalculatorScreen` switching between "Kalkulator Pary" and "Eksplorator Macierzy"
  - Drawer item "Tabela Miksów SIN" in `MainScreen`

- [ ] **Step 1: Add Screen.MatrixExplorer**

Edit `Screen.kt` to add `data object MatrixExplorer : Screen("matrix_explorer")`.

- [ ] **Step 2: Add tabs to MixCalculatorScreen**

Add `PrimaryTabRow` with tabs:
- "Kalkulator pary" (existing pairwise calculator)
- "Eksplorator Macierzy SIN" (embedding `MatrixExplorerContent`)

- [ ] **Step 3: Add Drawer item and NavHost destination in MainScreen**

Add "Tabela Miksów SIN" in `ModalDrawerSheet` and composable destination in `NavHost`.

- [ ] **Step 4: Verify compilation & tests**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest compileDebugKotlin`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/ui/navigation/Screen.kt app/src/main/java/info/hyperreal/journal/ui/mixcalculator/MixCalculatorScreen.kt app/src/main/java/info/hyperreal/journal/ui/MainScreen.kt
git commit -m "feat: integrate Matrix Explorer into tabs and drawer navigation"
```

---

### Task 5: End-to-End Verification & Full Regression

- [ ] **Step 1: Run complete test suite**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest`
Expected: All 85+ tests PASS.

- [ ] **Step 2: Run assembleDebug**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Update documentation and progress tracking**

Update `walkthrough.md` and `.superpowers/sdd/progress.md`.
