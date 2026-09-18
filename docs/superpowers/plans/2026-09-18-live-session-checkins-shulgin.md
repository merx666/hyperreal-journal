# Live Session Check-ins & Shulgin Rating Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement real-time session check-ins and the Shulgin Rating Scale (`±`, `+`, `++`, `+++`, `++++`) for Hyperreal Journal. This includes a new Room `check_ins` table with safe migration 1 -> 2, repository, `JournalViewModel` integration, an interactive `CheckInBottomSheet`, and a timeline visualizer in `JournalScreen`.

**Architecture:** Clean Architecture with MVI in Jetpack Compose. Room Database entity with ForeignKey to `ingestions(id)` (CASCADE delete), exposed via reactive Kotlin Coroutine Flow in `CheckInRepository`, integrated into `JournalViewModel`, and rendered in Material 3 Compose components.

**Tech Stack:** Kotlin, Jetpack Compose (Material 3 ModalBottomSheet), Room DB 2.6+, SQLite Migration, Kotlin Coroutines & StateFlow, Hilt, JUnit 4.

## Global Constraints

- **Language & Locale:** All user-facing strings must be in Polish (`pl`).
- **Offline First:** 100% offline, zero network requests.
- **Java Home for Builds:** `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.
- **Database Safety:** Safe migration 1 -> 2 with zero loss of existing user journal entries.
- **Test Integrity:** All 75 existing tests must remain 100% green.
- **TDD:** Write tests, verify failure, implement code, verify green, commit.

---

### Task 1: Domain Models and Repository Interface

**Files:**
- Create: `app/src/main/java/info/hyperreal/journal/domain/model/CheckIn.kt`
- Create: `app/src/main/java/info/hyperreal/journal/domain/repository/CheckInRepository.kt`

**Interfaces:**
- Produces:
  - `enum class ShulginRating(val symbol: String, val label: String, val description: String)`
  - `data class CheckIn(val id: Long, val ingestionId: Long, val timestamp: Long, val phase: TimelinePhase, val shulginRating: ShulginRating, val notes: String?)`
  - `interface CheckInRepository`

- [x] **Step 1: Create domain models**

Create `app/src/main/java/info/hyperreal/journal/domain/model/CheckIn.kt`:
```kotlin
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
```

- [x] **Step 2: Create repository interface**

Create `app/src/main/java/info/hyperreal/journal/domain/repository/CheckInRepository.kt`:
```kotlin
package info.hyperreal.journal.domain.repository

import info.hyperreal.journal.domain.model.CheckIn
import kotlinx.coroutines.flow.Flow

interface CheckInRepository {
    suspend fun insertCheckIn(checkIn: CheckIn): Long
    suspend fun deleteCheckIn(id: Long)
    fun getCheckInsForIngestion(ingestionId: Long): Flow<List<CheckIn>>
    fun getAllCheckIns(): Flow<List<CheckIn>>
}
```

- [x] **Step 3: Verify compilation**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [x] **Step 4: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/domain/model/CheckIn.kt app/src/main/java/info/hyperreal/journal/domain/repository/CheckInRepository.kt
git commit -m "feat: add domain models and repository interface for check-ins"
```

---

### Task 2: Room Entity, DAO, Migration 1 -> 2, and Mapper

**Files:**
- Create: `app/src/main/java/info/hyperreal/journal/data/local/entity/CheckInEntity.kt`
- Create: `app/src/main/java/info/hyperreal/journal/data/local/dao/CheckInDao.kt`
- Create: `app/src/main/java/info/hyperreal/journal/data/mapper/CheckInMapper.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/data/local/database/AppDatabase.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/di/DatabaseModule.kt`

**Interfaces:**
- Consumes: `CheckIn`, `ShulginRating`, `IngestionEntity`
- Produces: `CheckInDao`, `AppDatabase.MIGRATION_1_2`

- [x] **Step 1: Create CheckInEntity**

Create `app/src/main/java/info/hyperreal/journal/data/local/entity/CheckInEntity.kt`:
```kotlin
package info.hyperreal.journal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "check_ins",
    foreignKeys = [
        ForeignKey(
            entity = IngestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingestionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ingestionId"])]
)
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ingestionId: Long,
    val timestamp: Long,
    val phase: String,
    val shulginRating: String,
    val notes: String?
)
```

- [x] **Step 2: Create CheckInDao**

Create `app/src/main/java/info/hyperreal/journal/data/local/dao/CheckInDao.kt`:
```kotlin
package info.hyperreal.journal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import info.hyperreal.journal.data.local.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckInEntity): Long

    @Query("DELETE FROM check_ins WHERE id = :id")
    suspend fun deleteCheckIn(id: Long)

    @Query("SELECT * FROM check_ins WHERE ingestionId = :ingestionId ORDER BY timestamp ASC")
    fun getCheckInsForIngestion(ingestionId: Long): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins ORDER BY timestamp ASC")
    fun getAllCheckIns(): Flow<List<CheckInEntity>>
}
```

- [x] **Step 3: Create CheckInMapper**

Create `app/src/main/java/info/hyperreal/journal/data/mapper/CheckInMapper.kt`:
```kotlin
package info.hyperreal.journal.data.mapper

import info.hyperreal.journal.data.local.entity.CheckInEntity
import info.hyperreal.journal.domain.model.CheckIn
import info.hyperreal.journal.domain.model.ShulginRating
import info.hyperreal.journal.domain.usecase.TimelinePhase

fun CheckInEntity.toDomain(): CheckIn {
    val domainPhase = runCatching { TimelinePhase.valueOf(phase) }.getOrDefault(TimelinePhase.PEAK)
    val domainRating = runCatching { ShulginRating.valueOf(shulginRating) }.getOrDefault(ShulginRating.PLUS_TWO)
    return CheckIn(
        id = id,
        ingestionId = ingestionId,
        timestamp = timestamp,
        phase = domainPhase,
        shulginRating = domainRating,
        notes = notes
    )
}

fun CheckIn.toEntity(): CheckInEntity {
    return CheckInEntity(
        id = id,
        ingestionId = ingestionId,
        timestamp = timestamp,
        phase = phase.name,
        shulginRating = shulginRating.name,
        notes = notes
    )
}
```

- [x] **Step 4: Update AppDatabase with version 2 and MIGRATION_1_2**

Edit `app/src/main/java/info/hyperreal/journal/data/local/database/AppDatabase.kt`:
Add `CheckInEntity::class`, `abstract val checkInDao: CheckInDao`, `version = 2`, and `MIGRATION_1_2`.

- [x] **Step 5: Update DatabaseModule to add migration and provide CheckInDao**

Edit `app/src/main/java/info/hyperreal/journal/di/DatabaseModule.kt`:
Add `.addMigrations(AppDatabase.MIGRATION_1_2)` and `@Provides fun provideCheckInDao(db: AppDatabase): CheckInDao`.

- [x] **Step 6: Verify compilation and tests**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest`
Expected: PASS

- [x] **Step 7: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/data/local/entity/CheckInEntity.kt app/src/main/java/info/hyperreal/journal/data/local/dao/CheckInDao.kt app/src/main/java/info/hyperreal/journal/data/mapper/CheckInMapper.kt app/src/main/java/info/hyperreal/journal/data/local/database/AppDatabase.kt app/src/main/java/info/hyperreal/journal/di/DatabaseModule.kt
git commit -m "feat: add CheckInEntity, DAO, and database migration 1 to 2"
```

---

### Task 3: CheckInRepository Implementation & DI Wiring

**Files:**
- Create: `app/src/main/java/info/hyperreal/journal/data/repository/CheckInRepositoryImpl.kt`
- Modify: `app/src/main/java/info/hyperreal/journal/di/RepositoryModule.kt`

**Interfaces:**
- Consumes: `CheckInDao`, `CheckInRepository`
- Produces: `CheckInRepositoryImpl` bound to `CheckInRepository`

- [x] **Step 1: Implement CheckInRepositoryImpl**

Create `app/src/main/java/info/hyperreal/journal/data/repository/CheckInRepositoryImpl.kt`:
```kotlin
package info.hyperreal.journal.data.repository

import info.hyperreal.journal.data.local.dao.CheckInDao
import info.hyperreal.journal.data.mapper.toDomain
import info.hyperreal.journal.data.mapper.toEntity
import info.hyperreal.journal.domain.model.CheckIn
import info.hyperreal.journal.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInRepositoryImpl @Inject constructor(
    private val checkInDao: CheckInDao
) : CheckInRepository {

    override suspend fun insertCheckIn(checkIn: CheckIn): Long {
        return checkInDao.insertCheckIn(checkIn.toEntity())
    }

    override suspend fun deleteCheckIn(id: Long) {
        checkInDao.deleteCheckIn(id)
    }

    override fun getCheckInsForIngestion(ingestionId: Long): Flow<List<CheckIn>> {
        return checkInDao.getCheckInsForIngestion(ingestionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllCheckIns(): Flow<List<CheckIn>> {
        return checkInDao.getAllCheckIns().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
```

- [x] **Step 2: Bind CheckInRepository in RepositoryModule**

Edit `app/src/main/java/info/hyperreal/journal/di/RepositoryModule.kt` to bind `CheckInRepositoryImpl` as `CheckInRepository`.

- [x] **Step 3: Verify compilation & tests**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest`
Expected: PASS

- [x] **Step 4: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/data/repository/CheckInRepositoryImpl.kt app/src/main/java/info/hyperreal/journal/di/RepositoryModule.kt
git commit -m "feat: implement CheckInRepository and bind in Hilt"
```

---

### Task 4: JournalViewModel Integration & Unit Tests

**Files:**
- Modify: `app/src/main/java/info/hyperreal/journal/ui/journal/JournalViewModel.kt`
- Modify: `app/src/test/java/info/hyperreal/journal/ui/journal/JournalViewModelTest.kt`

**Interfaces:**
- Consumes: `CheckInRepository`, `JournalEntry`
- Produces:
  - In `JournalEntry`: `val checkIns: List<CheckIn> = emptyList()`
  - In `JournalViewModel`:
    - `fun addCheckIn(ingestionId: Long, phase: TimelinePhase, rating: ShulginRating, notes: String?)`
    - `fun deleteCheckIn(id: Long)`

- [x] **Step 1: Write test for JournalViewModel check-ins**

Update `app/src/test/java/info/hyperreal/journal/ui/journal/JournalViewModelTest.kt` with tests for check-in adding and grouping into `JournalEntry`.

- [x] **Step 2: Update JournalViewModel with check-in support**

Inject `CheckInRepository` into `JournalViewModel`. Combine `getAllCheckIns()` into `rawEntries` so each `JournalEntry` contains its own `checkIns`. Add `addCheckIn` and `deleteCheckIn`.

- [x] **Step 3: Run unit tests to verify green**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest`
Expected: PASS

- [x] **Step 4: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/ui/journal/JournalViewModel.kt app/src/test/java/info/hyperreal/journal/ui/journal/JournalViewModelTest.kt
git commit -m "feat: integrate check-ins into JournalViewModel with unit tests"
```

---

### Task 5: UI: CheckInBottomSheet & Timeline in JournalScreen

**Files:**
- Modify: `app/src/main/java/info/hyperreal/journal/ui/journal/JournalScreen.kt`

**Interfaces:**
- Produces:
  - `CheckInBottomSheet` composable
  - Button "Zrób Check-in (Skala Shulgina)" on active session card
  - Visual timeline displaying check-ins under journal entry card

- [x] **Step 1: Create CheckInBottomSheet composable in JournalScreen.kt**

Implement modal bottom sheet with:
- Subtitle with substance name and time elapsed
- Fazy sesji (FilterChips for ONSET, COMEUP, PEAK, OFFSET, AFTERGLOW)
- Shulgin Rating cards/chips (`±`, `+`, `++`, `+++`, `++++`) with description text
- Note OutlinedTextField
- Save button calling `viewModel.addCheckIn`

- [x] **Step 2: Add check-in list & button to active card and entry cards**

Display check-ins inside each card with timestamp, phase badge, rating symbol, and notes.

- [x] **Step 3: Verify compilation & tests**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest compileDebugKotlin`
Expected: PASS

- [x] **Step 4: Commit**

```bash
git add app/src/main/java/info/hyperreal/journal/ui/journal/JournalScreen.kt
git commit -m "feat: add CheckInBottomSheet and timeline check-in rendering in JournalScreen"
```

---

### Task 6: End-to-End Verification & Full Regression

- [x] **Step 1: Run complete test suite**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew testDebugUnitTest`
Expected: All 75+ tests PASS.

- [x] **Step 2: Run assembleDebug**

Run: `JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [x] **Step 3: Update documentation and progress tracking**

Update `walkthrough.md` and `.superpowers/sdd/progress.md`.
