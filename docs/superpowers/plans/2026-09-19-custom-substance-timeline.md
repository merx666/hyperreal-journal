# Dodawanie Własnej Substancji z Danymi Wykresu Działania - Plan Implementacji

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Dodać do aplikacji możliwość dodania własnej substancji zawierającej wyłącznie dane potrzebne do wygenerowania wykresu działania substancji (`TimelineChart`), dostępnej z poziomu listy substancji oraz kreatora dodawania zażycia.

**Architecture:** Baza danych Room rozszerzona o tabelę `custom_substances` (migracja 2 -> 3). `SubstanceRepositoryImpl` reaktywnie łączy wbudowaną bazę `substances.json` z encjami własnych substancji w strumieniu Kotlin `Flow<List<Substance>>`. Nowy współdzielony komponent `AddCustomSubstanceDialog` pozwala na wprowadzanie danych czasowych w minutach lub godzinach z podglądem łącznego czasu na żywo.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Room SQLite, Hilt DI, Coroutines & Flow, JUnit 4, Turbine, MockK.

## Global Constraints

- Tylko dane niezbędne do wykresu działania: nazwa substancji, droga podania (ROA) oraz czasy faz działania (onset, comeup, peak, offset, afterglow, total).
- 100% działanie offline – lokalny zapis w bazie Room.
- Zachowanie kompatybilności wstecznej bazy danych za pomocą `MIGRATION_2_3`.
- Reaktywność: nowo dodana substancja musi być natychmiast widoczna na listach, w wyszukiwarce i wykresach bez konieczności restartu aplikacji.

---

### Task 1: Room Database Schema & DAO for Custom Substances

**Files:**
- Create: `app/src/main/java/info/hyperreal/journal/data/local/entity/CustomSubstanceEntity.kt`
- Create: `app/src/main/java/info/hyperreal/journal/data/local/dao/CustomSubstanceDao.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/data/local/database/AppDatabase.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/di/DatabaseModule.kt`
- Test: `app/src/test/java/info/hyperreal/journal/data/local/CustomSubstanceEntityTest.kt`

**Interfaces:**
- Produces: `CustomSubstanceEntity`, `CustomSubstanceDao`, `AppDatabase.MIGRATION_2_3`
- Consumes: Room `@Entity`, `@Dao`, `@Database`

- [ ] **Step 1: Write the unit test for CustomSubstanceEntity mapping**

Utwórz plik `app/src/test/java/info/hyperreal/journal/data/local/CustomSubstanceEntityTest.kt`:
```kotlin
package info.hyperreal.journal.data.local

import info.hyperreal.journal.data.local.entity.CustomSubstanceEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CustomSubstanceEntityTest {

    @Test
    fun testCustomSubstanceEntityCreation() {
        val entity = CustomSubstanceEntity(
            id = "custom_123",
            name = "Test Substance",
            roaName = "Doustnie",
            onsetMinutes = 30f,
            comeupMinutes = 45f,
            peakMinutes = 120f,
            offsetMinutes = 90f,
            afterglowMinutes = 60f,
            totalMinutes = 345f,
            createdAt = 1700000000000L
        )

        assertEquals("custom_123", entity.id)
        assertEquals("Test Substance", entity.name)
        assertEquals("Doustnie", entity.roaName)
        assertEquals(30f, entity.onsetMinutes)
        assertEquals(345f, entity.totalMinutes)
        assertNotNull(entity.createdAt)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Uruchom: `./gradlew testDebugUnitTest --tests "info.hyperreal.journal.data.local.CustomSubstanceEntityTest"`
Oczekiwany wynik: BŁĄD kompilacji (brak klasy `CustomSubstanceEntity`).

- [ ] **Step 3: Implement CustomSubstanceEntity, CustomSubstanceDao, AppDatabase & DatabaseModule**

1. Utwórz `app/src/main/java/info/hyperreal/journal/data/local/entity/CustomSubstanceEntity.kt`:
```kotlin
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
```

2. Utwórz `app/src/main/java/info/hyperreal/journal/data/local/dao/CustomSubstanceDao.kt`:
```kotlin
package info.hyperreal.journal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import info.hyperreal.journal.data.local.entity.CustomSubstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomSubstanceDao {
    @Query("SELECT * FROM custom_substances ORDER BY name ASC")
    fun getAllCustomSubstances(): Flow<List<CustomSubstanceEntity>>

    @Query("SELECT * FROM custom_substances WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CustomSubstanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomSubstance(entity: CustomSubstanceEntity)

    @Query("DELETE FROM custom_substances WHERE id = :id")
    suspend fun deleteCustomSubstance(id: String)
}
```

3. Zaktualizuj `app/src/main/java/info/hyperreal/journal/data/local/database/AppDatabase.kt`:
Dodaj `CustomSubstanceEntity::class` do entities, zwiększ wersję do `3`, dodaj pole `abstract val customSubstanceDao: CustomSubstanceDao` oraz obiekt `MIGRATION_2_3`.

4. Zaktualizuj `app/src/main/java/info/hyperreal/journal/di/DatabaseModule.kt`:
Dodaj `.addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)` oraz metodę `@Provides @Singleton fun provideCustomSubstanceDao(db: AppDatabase): CustomSubstanceDao = db.customSubstanceDao`.

- [ ] **Step 4: Run test to verify it passes**

Uruchom: `./gradlew testDebugUnitTest --tests "info.hyperreal.journal.data.local.CustomSubstanceEntityTest"`
Oczekiwany wynik: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/data/local/ app/src/main/java/info/hyperreal/journal/di/ app/src/test/java/info/hyperreal/journal/data/local/
git commit -m "feat(db): add custom_substances Room entity, DAO and migration 2 to 3"
```

---

### Task 2: Repository Extension & Domain Integration

**Files:**
- Modify: `app/src/main/java/info/hyperreal/journal/domain/repository/SubstanceRepository.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/data/repository/SubstanceRepositoryImpl.kt`
- Test: `app/src/test/java/info/hyperreal/journal/data/repository/SubstanceRepositoryImplTest.kt`

**Interfaces:**
- Produces: `SubstanceRepository.addCustomSubstance(...)`, `SubstanceRepository.deleteCustomSubstance(id)`
- Consumes: `CustomSubstanceDao`, `DurationParameters`, `Substance`

- [ ] **Step 1: Write the failing unit test for SubstanceRepositoryImpl custom substances handling**

Utwórz `app/src/test/java/info/hyperreal/journal/data/repository/SubstanceRepositoryImplTest.kt`:
```kotlin
package info.hyperreal.journal.data.repository

import android.content.Context
import android.content.res.AssetManager
import com.google.gson.Gson
import info.hyperreal.journal.data.local.dao.CustomSubstanceDao
import info.hyperreal.journal.data.local.entity.CustomSubstanceEntity
import info.hyperreal.journal.domain.model.DurationParameters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream

class SubstanceRepositoryImplTest {

    private val context = mockk<Context>()
    private val assets = mockk<AssetManager>()
    private val dao = mockk<CustomSubstanceDao>()
    private val gson = Gson()
    private lateinit var repository: SubstanceRepositoryImpl

    @Before
    fun setup() {
        val sampleJson = """
            [
                {
                    "id": "caffeine",
                    "name": "Kofeina",
                    "classes": ["Stymulant"],
                    "roas": []
                }
            ]
        """.trimIndent()

        every { context.assets } returns assets
        every { assets.open("substances.json") } returns ByteArrayInputStream(sampleJson.toByteArray())
        repository = SubstanceRepositoryImpl(context, gson, dao)
    }

    @Test
    fun `getAllSubstances combines bundled and custom substances`() = runTest {
        val customEntity = CustomSubstanceEntity(
            id = "custom_1",
            name = "Własny Nootropik",
            roaName = "Doustnie",
            onsetMinutes = 20f,
            comeupMinutes = 30f,
            peakMinutes = 90f,
            offsetMinutes = 60f,
            afterglowMinutes = 30f,
            totalMinutes = 230f
        )
        every { dao.getAllCustomSubstances() } returns flowOf(listOf(customEntity))

        val result = repository.getAllSubstances().first()

        assertEquals(2, result.size)
        val custom = result.find { it.id == "custom_1" }
        assertTrue(custom != null)
        assertEquals("Własny Nootropik", custom?.name)
        assertTrue(custom?.classes?.contains("Własne") == true)
        val roa = custom?.roas?.firstOrNull()
        assertEquals("Doustnie", roa?.name)
        assertEquals(20f, roa?.duration?.onset)
        assertEquals(230f, roa?.duration?.total)
    }

    @Test
    fun `addCustomSubstance inserts into DAO and returns Substance`() = runTest {
        coEvery { dao.insertCustomSubstance(any()) } returns Unit

        val duration = DurationParameters(onset = 15f, peak = 60f, total = 120f)
        val created = repository.addCustomSubstance("Moja Substancja", "Waporyzacja", duration)

        coVerify { dao.insertCustomSubstance(match { it.name == "Moja Substancja" && it.roaName == "Waporyzacja" }) }
        assertEquals("Moja Substancja", created.name)
        assertEquals("Waporyzacja", created.roas.first().name)
        assertEquals(15f, created.roas.first().duration?.onset)
    }

    @Test
    fun `deleteCustomSubstance calls DAO delete`() = runTest {
        coEvery { dao.deleteCustomSubstance("custom_1") } returns Unit

        repository.deleteCustomSubstance("custom_1")

        coVerify { dao.deleteCustomSubstance("custom_1") }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Uruchom: `./gradlew testDebugUnitTest --tests "info.hyperreal.journal.data.repository.SubstanceRepositoryImplTest"`
Oczekiwany wynik: BŁĄD (brak metod `addCustomSubstance`, `deleteCustomSubstance`, brak `CustomSubstanceDao` w konstruktorze `SubstanceRepositoryImpl`).

- [ ] **Step 3: Update SubstanceRepository and SubstanceRepositoryImpl**

1. W `app/src/main/java/info/hyperreal/journal/domain/repository/SubstanceRepository.kt`:
```kotlin
package info.hyperreal.journal.domain.repository

import info.hyperreal.journal.domain.model.DurationParameters
import info.hyperreal.journal.domain.model.Substance
import kotlinx.coroutines.flow.Flow

interface SubstanceRepository {
    fun getAllSubstances(): Flow<List<Substance>>
    suspend fun getSubstanceById(id: String): Substance?
    suspend fun addCustomSubstance(
        name: String,
        roaName: String = "Doustnie",
        duration: DurationParameters
    ): Substance
    suspend fun deleteCustomSubstance(id: String)
}
```

2. W `app/src/main/java/info/hyperreal/journal/data/repository/SubstanceRepositoryImpl.kt`:
Wstrzyknij `private val customSubstanceDao: CustomSubstanceDao`.
Połącz Flow:
```kotlin
    override fun getAllSubstances(): Flow<List<Substance>> {
        val bundled = getBundledSubstances()
        return customSubstanceDao.getAllCustomSubstances().map { entities ->
            val custom = entities.map { it.toSubstance() }
            bundled + custom
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getSubstanceById(id: String): Substance? {
        val bundled = getBundledSubstances().find { it.id == id }
        if (bundled != null) return bundled
        return customSubstanceDao.getById(id)?.toSubstance()
    }

    override suspend fun addCustomSubstance(
        name: String,
        roaName: String,
        duration: DurationParameters
    ): Substance {
        val id = "custom_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val entity = CustomSubstanceEntity(
            id = id,
            name = name.trim(),
            roaName = roaName.ifBlank { "Doustnie" }.trim(),
            onsetMinutes = duration.onset,
            comeupMinutes = duration.comeup,
            peakMinutes = duration.peak,
            offsetMinutes = duration.offset,
            afterglowMinutes = duration.afterglow,
            totalMinutes = duration.total ?: listOfNotNull(duration.onset, duration.comeup, duration.peak, duration.offset, duration.afterglow).sum()
        )
        customSubstanceDao.insertCustomSubstance(entity)
        return entity.toSubstance()
    }

    override suspend fun deleteCustomSubstance(id: String) {
        customSubstanceDao.deleteCustomSubstance(id)
    }
```
Dodaj funkcję rozszerzającą `CustomSubstanceEntity.toSubstance()`:
```kotlin
fun CustomSubstanceEntity.toSubstance(): Substance {
    val durationParams = DurationParameters(
        onset = onsetMinutes,
        comeup = comeupMinutes,
        peak = peakMinutes,
        offset = offsetMinutes,
        afterglow = afterglowMinutes,
        total = totalMinutes
    )
    return Substance(
        id = id,
        name = name,
        aliases = listOf("Własna"),
        summary = "Własna substancja dodana przez użytkownika.",
        classes = listOf("Własne"),
        roas = listOf(
            Roa(
                name = roaName,
                dose = DoseParameters(units = "mg"),
                duration = durationParams
            )
        )
    )
}
```

- [ ] **Step 4: Run test to verify it passes**

Uruchom: `./gradlew testDebugUnitTest --tests "info.hyperreal.journal.data.repository.SubstanceRepositoryImplTest"`
Oczekiwany wynik: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/domain/repository/ app/src/main/java/info/hyperreal/journal/data/repository/ app/src/test/java/info/hyperreal/journal/data/repository/
git commit -m "feat(substances): integrate custom substances into SubstanceRepository"
```

---

### Task 3: UI Component `AddCustomSubstanceDialog`

**Files:**
- Create: `app/src/main/java/info/hyperreal/journal/ui/substances/AddCustomSubstanceDialog.kt`
- Test: `app/src/test/java/info/hyperreal/journal/ui/substances/CustomSubstanceDurationHelperTest.kt`

**Interfaces:**
- Produces: `AddCustomSubstanceDialog(initialName: String = "", onDismiss: () -> Unit, onConfirm: (name: String, roaName: String, duration: DurationParameters) -> Unit)`
- Produces: `CustomSubstanceDurationHelper.calculateDuration(isHours: Boolean, onset: String, comeup: String, peak: String, offset: String, afterglow: String, customTotal: String?): DurationParameters`

- [ ] **Step 1: Write helper test for unit conversion (minutes vs hours) and duration calculation**

Utwórz `app/src/test/java/info/hyperreal/journal/ui/substances/CustomSubstanceDurationHelperTest.kt`:
```kotlin
package info.hyperreal.journal.ui.substances

import org.junit.Assert.assertEquals
import org.junit.Test

class CustomSubstanceDurationHelperTest {

    @Test
    fun `parses minutes correctly`() {
        val duration = CustomSubstanceDurationHelper.calculateDuration(
            isHours = false,
            onset = "30",
            comeup = "45",
            peak = "120",
            offset = "90",
            afterglow = "60"
        )

        assertEquals(30f, duration.onset)
        assertEquals(45f, duration.comeup)
        assertEquals(120f, duration.peak)
        assertEquals(90f, duration.offset)
        assertEquals(60f, duration.afterglow)
        assertEquals(345f, duration.total)
    }

    @Test
    fun `parses hours and converts to minutes correctly`() {
        val duration = CustomSubstanceDurationHelper.calculateDuration(
            isHours = true,
            onset = "0.5",
            comeup = "1.0",
            peak = "2.0",
            offset = "1.5",
            afterglow = "1.0"
        )

        assertEquals(30f, duration.onset)
        assertEquals(60f, duration.comeup)
        assertEquals(120f, duration.peak)
        assertEquals(90f, duration.offset)
        assertEquals(60f, duration.afterglow)
        assertEquals(360f, duration.total)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Uruchom: `./gradlew testDebugUnitTest --tests "info.hyperreal.journal.ui.substances.CustomSubstanceDurationHelperTest"`
Oczekiwany wynik: BŁĄD kompilacji (brak `CustomSubstanceDurationHelper`).

- [ ] **Step 3: Implement CustomSubstanceDurationHelper and AddCustomSubstanceDialog composable**

Utwórz `app/src/main/java/info/hyperreal/journal/ui/substances/AddCustomSubstanceDialog.kt` z:
1. `object CustomSubstanceDurationHelper` implementującym przeliczanie i wyliczanie `DurationParameters`.
2. `@Composable fun AddCustomSubstanceDialog(...)`:
   - Pole nazwy (wymagane).
   - Wybór drogi podania (Doustnie, Donosowo, Waporyzacja, Podjęzykowo, Inna).
   - Przełącznik jednostki `[Minuty]` | `[Godziny]`.
   - Pola tekstowe dla 5 faz (Onset, Comeup, Peak, Offset, Afterglow).
   - Podgląd łącznego czasu działania na żywo.
   - Walidacja: nazwa nie może być pusta, a przynajmniej jedna faza musi być > 0.
   - Przyciski "Anuluj" i "Zapisz".

- [ ] **Step 4: Run test to verify it passes**

Uruchom: `./gradlew testDebugUnitTest --tests "info.hyperreal.journal.ui.substances.CustomSubstanceDurationHelperTest"`
Oczekiwany wynik: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/ui/substances/AddCustomSubstanceDialog.kt app/src/test/java/info/hyperreal/journal/ui/substances/CustomSubstanceDurationHelperTest.kt
git commit -m "feat(ui): create AddCustomSubstanceDialog with minutes/hours unit toggle"
```

---

### Task 4: UI Integration in `SubstancesListScreen` & `SubstancesViewModel`

**Files:**
- Modify: `app/src/main/java/info/hyperreal/journal/ui/substances/SubstancesViewModel.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/ui/substances/SubstancesListScreen.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/ui/substances/SubstanceDetailScreen.kt`

**Interfaces:**
- Produces: `SubstancesViewModel.addCustomSubstance(...)`, `SubstancesViewModel.deleteCustomSubstance(id)`
- Consumes: `AddCustomSubstanceDialog`, `SubstanceRepository`

- [ ] **Step 1: Add addCustomSubstance and deleteCustomSubstance to SubstancesViewModel**

W `app/src/main/java/info/hyperreal/journal/ui/substances/SubstancesViewModel.kt`:
```kotlin
    fun addCustomSubstance(
        name: String,
        roaName: String,
        duration: DurationParameters,
        onCreated: (Substance) -> Unit = {}
    ) {
        viewModelScope.launch {
            val created = repository.addCustomSubstance(name, roaName, duration)
            onCreated(created)
        }
    }

    fun deleteCustomSubstance(id: String) {
        viewModelScope.launch {
            repository.deleteCustomSubstance(id)
        }
    }
```

- [ ] **Step 2: Add FAB and delete action to SubstancesListScreen**

W `app/src/main/java/info/hyperreal/journal/ui/substances/SubstancesListScreen.kt`:
- Dodaj stan dialogu: `var showAddDialog by remember { mutableStateOf(false) }`.
- Owiń ekran w `Scaffold(floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, "Dodaj własną substancję") } })`.
- Gdy `showAddDialog == true`, wyświetl `AddCustomSubstanceDialog(...)`.
- Na karcie/liście substancji: jeśli `substance.classes.contains("Własne")` lub `substance.id.startsWith("custom_")`:
  - Wyświetl etykietę `Badge` / chip `"Własna"`.
  - Dodaj ikonę kosza `IconButton(onClick = { viewModel.deleteCustomSubstance(substance.id) })` z dialogiem potwierdzenia.

- [ ] **Step 3: Add delete button to SubstanceDetailScreen for custom substances**

W `app/src/main/java/info/hyperreal/journal/ui/substances/SubstanceDetailScreen.kt`:
- Jeśli `s.classes.contains("Własne")` lub `s.id.startsWith("custom_")`, wyświetl przycisk *"Usuń tę substancję"* z dialogiem potwierdzenia, który wywołuje usunięcie i cofa nawigację (`onBackClick`).

- [ ] **Step 4: Run build to ensure UI compiles**

Uruchom: `./gradlew assembleDebug`
Oczekiwany wynik: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/ui/substances/
git commit -m "feat(ui): add FAB and delete custom substance support to SubstancesListScreen and Detail"
```

---

### Task 5: UI Integration in `ChooseSubstanceScreen` (`AddIngestionScreens.kt`)

**Files:**
- Modify: `app/src/main/java/info/hyperreal/journal/ui/addingestion/AddIngestionViewModel.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/ui/addingestion/AddIngestionScreens.kt`

**Interfaces:**
- Produces: `AddIngestionViewModel.addCustomSubstance(...)`
- Consumes: `AddCustomSubstanceDialog`, `ChooseSubstanceScreen`

- [ ] **Step 1: Add custom substance creation to AddIngestionViewModel**

W `app/src/main/java/info/hyperreal/journal/ui/addingestion/AddIngestionViewModel.kt`:
```kotlin
    fun addCustomSubstance(
        name: String,
        roaName: String,
        duration: DurationParameters,
        onCreated: (Substance) -> Unit = {}
    ) {
        viewModelScope.launch {
            val created = substanceRepository.addCustomSubstance(name, roaName, duration)
            selectSubstance(created)
            if (created.roas.isNotEmpty()) {
                selectRoa(created.roas.first())
            }
            onCreated(created)
        }
    }
```

- [ ] **Step 2: Add "Dodaj własną substancję" action to ChooseSubstanceScreen**

W `app/src/main/java/info/hyperreal/journal/ui/addingestion/AddIngestionScreens.kt`:
W `ChooseSubstanceScreen`:
- Dodaj parametr: `onAddCustomSubstance: ((String, String, DurationParameters) -> Unit)? = null`.
- Dodaj stan dialogu `var showAddDialog by remember { mutableStateOf(false) }`.
- Gdy lista wyników `filtered.isEmpty()` lub na samym dole listy dodaj estetyczny przycisk / kartę:
  ```kotlin
  OutlinedButton(
      onClick = { showAddDialog = true },
      modifier = Modifier.fillMaxWidth().padding(16.dp)
  ) {
      Icon(Icons.Default.Add, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text(if (query.isNotBlank()) "Dodaj własną: \"$query\"" else "Dodaj własną substancję")
  }
  ```
- Gdy `showAddDialog == true`, otwórz `AddCustomSubstanceDialog(initialName = query, ...)`.

- [ ] **Step 3: Run build to ensure UI compiles**

Uruchom: `./gradlew assembleDebug`
Oczekiwany wynik: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/ui/addingestion/
git commit -m "feat(ui): add quick custom substance addition to ChooseSubstanceScreen in AddIngestion"
```

---

### Task 6: Full Verification and Testing

**Files:**
- Test: All unit test suites

- [ ] **Step 1: Run complete test suite**

Uruchom: `./gradlew testDebugUnitTest`
Oczekiwany wynik: Wszystkie testy (w tym nowe testy DAO, repozytorium i UI helpera) przechodzą pomyślnie.

- [ ] **Step 2: Verify TimelineChart compatibility test**

Uruchom: `./gradlew testDebugUnitTest --tests "info.hyperreal.journal.domain.usecase.TimelineCalculatorTest"`
Oczekiwany wynik: PASS.

- [ ] **Step 3: Build Debug APK**

Uruchom: `./gradlew assembleDebug`
Oczekiwany wynik: BUILD SUCCESSFUL.

- [ ] **Step 4: Final commit and summary**

```bash
git status
```
