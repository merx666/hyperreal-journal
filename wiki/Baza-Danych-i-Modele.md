# Baza Danych i Modele Danych — Hyperreal Journal

Zgodnie z fundamentalną zasadą prywatności projektu (**Zero-Cloud & 100% Offline**), wszystkie dane aplikacji są przechowywane lokalnie na urządzeniu użytkownika.

---

## 🗄️ 1. Architektura Magazynu Danych

Aplikacja korzysta z dwóch komplementarnych mechanizmów utrwalania danych:
1. **Lokalna relacyjna baza SQLite (Room):** przechowuje zmienne dane użytkownika (wpisy zażyć, własne substancje).
2. **Niemutowalne zasoby statyczne (JSON Assets):** wbudowane w plik APK katalogi wiedzy farmakologicznej i matrycy miksów.

---

## 📦 2. Schemat Bazy Room (Wersja v3)

Główną bazą danych aplikacji jest `AppDatabase`. Schemat przeszedł ewolucję od wersji 1 do wersji 3.

### Tabela `ingestions` (`IngestionEntity`)
Przechowuje wpisy w dzienniku zażyć:
```sql
CREATE TABLE IF NOT EXISTS `ingestions` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `substanceId` TEXT NOT NULL,
    `roa` TEXT NOT NULL,
    `doseAmount` REAL NOT NULL,
    `doseUnit` TEXT NOT NULL,
    `timestamp` INTEGER NOT NULL,
    `notes` TEXT NOT NULL DEFAULT '',
    `shulginRating` INTEGER DEFAULT NULL
);
```

### Tabela `custom_substances` (`CustomSubstanceEntity` — dodana w v3)
Pozwala użytkownikom definiować własne substancje wraz z parametrami osi czasu:
```sql
CREATE TABLE IF NOT EXISTS `custom_substances` (
    `id` TEXT PRIMARY KEY NOT NULL,
    `name` TEXT NOT NULL,
    `roaName` TEXT NOT NULL,
    `onsetMinutes` REAL NOT NULL,
    `comeupMinutes` REAL NOT NULL,
    `peakMinutes` REAL NOT NULL,
    `offsetMinutes` REAL NOT NULL,
    `afterglowMinutes` REAL NOT NULL,
    `totalMinutes` REAL NOT NULL,
    `createdAt` INTEGER NOT NULL
);
```

### Migracja `MIGRATION_2_3`
Wdrożona w kodzie aplikacji bezpieczna migracja schematu bazodanowego:
```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `custom_substances` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `roaName` TEXT NOT NULL,
                `onsetMinutes` REAL NOT NULL,
                `comeupMinutes` REAL NOT NULL,
                `peakMinutes` REAL NOT NULL,
                `offsetMinutes` REAL NOT NULL,
                `afterglowMinutes` REAL NOT NULL,
                `totalMinutes` REAL NOT NULL,
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
    }
}
```

---

## 📄 3. Predefiniowane Zasoby JSON

### A. Katalog Substancji (`app/src/main/assets/substances.json`)
Zawiera pełną specyfikację substancji z podziałem na drogi podania (RoA), progi dawkowania oraz czasy trwania poszczególnych faz działania:

```json
{
  "id": "Metylofenidat IR",
  "name": "Metylofenidat IR",
  "aliases": ["Medikinet", "Medikinet IR", "Ritalin", "MPH"],
  "summary": "Ośrodkowy stymulant (NDRI)...",
  "harmReduction": "Unikaj łączenia z inhibitorami MAO...",
  "classes": ["Stymulant", "Fenidat", "NDRI"],
  "roas": [
    {
      "name": "Oral",
      "dose": {
        "threshold": 5.0,
        "light": 10.0,
        "common": 20.0,
        "strong": 40.0,
        "heavy": 60.0
      },
      "duration": {
        "onset": 20.0,
        "comeup": 30.0,
        "peak": 90.0,
        "offset": 90.0,
        "afterglow": 120.0,
        "total": 350.0
      }
    }
  ]
}
```
*Wszystkie czasy trwania (`duration`) są wyrażane w minutach, a dawki (`dose`) w miligramach.*

### B. Matryca Interakcji SIN (`app/src/main/assets/interactions_sin.json`)
Zawiera 333 zweryfikowane reguły łączenia substancji:
```json
{
  "substance_a": "Alkohol",
  "substance_b": "Benzodiazepiny",
  "status": "Dangerous",
  "note": "Ryzyko utraty pamięci, silnej depresji ośrodka oddechowego i zgonu."
}
```

---

## 🔄 4. Reaktywne Łączenie Danych w Repozytorium

Metoda `SubstanceRepositoryImpl.getAllSubstances()` łączy w jeden strumień obiekty z `substances.json` oraz Flow z bazy Room:
```kotlin
override fun getAllSubstances(): Flow<List<Substance>> {
    return customSubstanceDao.getAllCustomSubstances().map { customEntities ->
        val mappedCustom = customEntities.map { entity ->
            Substance(
                id = entity.id,
                name = entity.name,
                classes = listOf("Własne"),
                roas = listOf(
                    Roa(
                        name = entity.roaName,
                        duration = DurationParameters(
                            onset = entity.onsetMinutes,
                            comeup = entity.comeupMinutes,
                            peak = entity.peakMinutes,
                            offset = entity.offsetMinutes,
                            afterglow = entity.afterglowMinutes,
                            total = entity.totalMinutes
                        )
                    )
                )
            )
        }
        bundledSubstances + mappedCustom
    }
}
```
Dzięki temu nowo dodana własna substancja natychmiast pojawia się na wszystkich ekranach aplikacji bez konieczności ponownego uruchamiania.
