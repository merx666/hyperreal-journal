# Dodawanie Własnej Substancji z Danymi Wykresu Działania - Specyfikacja Projektowa

Data utworzenia: 2026-09-18  
Status: Zatwierdzony do implementacji  
Projekt: Hyperreal Journal (Android, Kotlin, Jetpack Compose, Room, Hilt, Coroutines & Flow)

---

## 1. Cel i Kontekst Biznesowy

Aplikacja *Hyperreal Journal* zawiera bogatą, wbudowaną bazę substancji psychoaktywnych (`substances.json`), z których każda posiada parametry farmakokinetyczne (czasy faz: Onset, Comeup, Peak, Offset, Afterglow, Total) służące do rysowania precyzyjnego wykresu działania w czasie (`TimelineChart`).

Wielu użytkowników eksperymentuje jednak z substancjami niszowymi, nowymi analogami (Research Chemicals), lekami na receptę, nootropami lub ziołami, których nie ma jeszcze w oficjalnym katalogu. 
Zgodnie z wymaganiem użytkownika, dodawanie własnej substancji ma być maksymalnie proste i ograniczone **wyłącznie do danych niezbędnych do generowania wykresu działania substancji**.

Funkcjonalność ta pozwala użytkownikowi:
1. Zdefiniować własną nazwę substancji oraz drogę podania (np. Doustnie, Donosowo, Waporyzacja).
2. Podać czasy faz działania w minutach lub godzinach (z wygodnym przełącznikiem jednostki i automatycznym przeliczaniem).
3. Wygenerować natychmiast precyzyjną krzywą farmakokinetyczną w `TimelineChart`.
4. Używać dodanej substancji w rejestrowaniu zażyć (`AddIngestion`), na liście substancji (`SubstancesListScreen`) oraz w kalkulatorze miksów.

---

## 2. Architektura i Przepływ Danych (Clean Architecture)

```
[UI Layer]
  - SubstancesListScreen (FAB "+ Dodaj własną")
  - ChooseSubstanceScreen ("Nie ma Twojej substancji? Dodaj własną")
  - AddCustomSubstanceDialog (Wygodny formularz z przełącznikiem Min / H)
        │
        ▼ (wywołanie w coroutine / ViewModel)
[Domain Layer]
  - SubstanceRepository.addCustomSubstance(name, roaName, duration)
  - SubstanceRepository.deleteCustomSubstance(id)
  - SubstanceRepository.getAllSubstances(): Flow<List<Substance>>
        │
        ▼
[Data Layer]
  - SubstanceRepositoryImpl
        ├── Wbudowane substancje z assets/substances.json (cache w pamięci)
        └── customSubstanceDao.getAllCustomSubstances(): Flow<List<CustomSubstanceEntity>>
              │ (Room SQLite / AppDatabase v3)
              ▼
        Tabela: `custom_substances`
```

---

## 3. Szczegóły Techniczne Implementacji

### 3.1. Baza Danych Room (Wersja 2 → 3)

#### Encja `CustomSubstanceEntity` (`info.hyperreal.journal.data.local.entity.CustomSubstanceEntity`)
```kotlin
package info.hyperreal.journal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_substances")
data class CustomSubstanceEntity(
    @PrimaryKey
    val id: String, // format: "custom_" + System.currentTimeMillis() + "_" + UUID
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

#### DAO `CustomSubstanceDao` (`info.hyperreal.journal.data.local.dao.CustomSubstanceDao`)
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

#### Migracja w `AppDatabase.kt`
Zwiększenie wersji do 3 i rejestracja migracji:
```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS custom_substances (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                roaName TEXT NOT NULL,
                onsetMinutes REAL,
                comeupMinutes REAL,
                peakMinutes REAL,
                offsetMinutes REAL,
                afterglowMinutes REAL,
                totalMinutes REAL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
```
W `DatabaseModule.kt`: udostępnienie `customSubstanceDao(database: AppDatabase): CustomSubstanceDao`.

---

### 3.2. Warstwa Domenowa i Repozytorium

#### Rozszerzenie `SubstanceRepository`
```kotlin
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

#### Implementacja w `SubstanceRepositoryImpl`
- Wstrzyknięcie `CustomSubstanceDao`.
- `getAllSubstances()` łączy w strumieniu `Flow` substancje z pliku `substances.json` oraz własne encje z `CustomSubstanceDao`.
- Konwersja `CustomSubstanceEntity` na domenowy model `Substance`:
  - `id = entity.id`
  - `name = entity.name`
  - `classes = listOf("Własne")` (zapewnia automatyczny chip filtru *"Własne"* w `SubstancesListScreen`)
  - `aliases = listOf("Własna")`
  - `summary = "Substancja dodana przez użytkownika"`
  - `roas = listOf(Roa(name = entity.roaName, duration = DurationParameters(...), dose = DoseParameters(units = "mg")))`

---

### 3.3. Interfejs Użytkownika (UI)

#### Komponent dialogu: `AddCustomSubstanceDialog`
Lokalizacja: `info.hyperreal.journal.ui.substances.AddCustomSubstanceDialog`
Pola i stan:
1. `name`: Nazwa substancji (pole tekstowe z walidacją – nie może być puste).
2. `roaName`: Droga podania – wybór z chipów: "Doustnie", "Donosowo", "Waporyzacja", "Podjęzykowo", "Inna" (lub wpisanie własnej).
3. `isHours`: Przełącznik jednostki `[Minuty]` / `[Godziny]`.
4. Pola czasu dla poszczególnych faz:
   - Onset (Wejście)
   - Comeup (Ładowanie)
   - Peak (Szczyt)
   - Offset (Zejście)
   - Afterglow (Powrót)
5. `totalSummary`: Podsumowanie w czasie rzeczywistym obliczające sumę czasów (np. *"Całkowity czas działania: ~4.5 godz."*).
6. Przyciski: "Anuluj" oraz "Zapisz substancję".

#### Punkty wejścia (Entry Points):
1. **`SubstancesListScreen`**:
   - `FloatingActionButton` ze znakiem `+` w prawym dolnym rogu.
   - Na liście substancja z klasą "Własne" posiada etykietę oraz możliwość usunięcia.
2. **`ChooseSubstanceScreen` (`AddIngestionScreens.kt`)**:
   - Gdy lista wyników wyszukiwania jest pusta lub na dole listy: przycisk/karta `"+ Dodaj własną substancję"`.
   - Dialog automatycznie pobiera wpisaną frazę z pola wyszukiwania.
   - Po zapisaniu substancja jest natychmiast wybierana jako bieżąca w kreatorze zażycia.

---

## 4. Plan Weryfikacji i Testów

1. **Testy jednostkowe repozytorium (`SubstanceRepositoryTest`)**:
   - Dodanie własnej substancji i sprawdzenie, czy pojawia się w `getAllSubstances()`.
   - Sprawdzenie poprawności mapowania faz czasu działania na `DurationParameters`.
   - Usunięcie własnej substancji i weryfikacja jej zniknięcia ze strumienia.
2. **Testy jednostkowe migracji bazy Room (`MigrationTest`)**:
   - Migracja schematu 2 → 3, sprawdzenie poprawności tworzenia tabeli `custom_substances`.
3. **Weryfikacja UI & TimelineChart**:
   - Sprawdzenie, czy dla dodanej własnej substancji wykres `TimelineChart` poprawnie rysuje krzywą intensywności (Onset -> Comeup -> Peak -> Offset -> Afterglow).
   - Test przełączania jednostek minuty/godziny.
4. **Kompilacja i uruchomienie testów**:
   - `./gradlew testDebugUnitTest` – 100% testów przechodzących.
