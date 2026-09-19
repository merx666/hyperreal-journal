# Architektura Systemu — Hyperreal Journal

Projekt został zaprojektowany w oparciu o pryncypia **Clean Architecture** (Czystej Architektury) w połączeniu ze wzorcem **MVVM / MVI** oraz asynchronicznym, reaktywnym modelem opartym na **Kotlin Coroutines** i **StateFlow**.

---

## 📐 Diagram Architektury

Poniżej znajduje się zweryfikowany diagram komponentów wygenerowany za pomocą narzędzia **Archify** w profilu `showcase`:

![Hyperreal Journal System Architecture](../docs/architecture.svg)

> 🔗 **Wersja interaktywna:**  
> Skorzystaj z interaktywnej mapy architektury z możliwością filtrowania ścieżek zależności:  
> 👉 [docs/architecture.html](https://github.com/merx666/hyperreal-journal/blob/main/docs/architecture.html)

---

## 🏛️ Warstwy Architektoniczne

### 1. Warstwa Prezentacji (Presentation Layer)
- **Framework UI:** Jetpack Compose (Material Design 3) z pełnym wsparciem dla *Edge-to-Edge* oraz motywu *Deep Dark AMOLED* (oszczędzanie energii, brak oślepiania w nocy).
- **Główne Ekrany (`ui/`):**
  - `JournalScreen` — lista wpisów zażyć z historią i filtrowaniem po dacie.
  - `AddIngestionScreens` — wielokrokowy kreator rejestracji zażycia (wybór substancji, droga podania, dawka z automatycznymi progami bezpieczeństwa).
  - `SubstancesListScreen` & `SubstanceDetailScreen` — encyklopedia substancji z podglądem parametrów farmakokinetycznych, zasad BHP i interaktywnego wykresu działania.
  - `InteractionsMatrixScreen` — interaktywna matryca 27 substancji i grup z bezpośrednim wyszukiwaniem połączeń.
  - `InsightsScreen` — statystyki 30-dniowe, analiza częstotliwości i odstępów czasowych.
- **ViewModels:** Zarządzają stanem UI emitując niemutowalne obiekty `UiState` za pośrednictwem `StateFlow`. Przyjmują intencje użytkownika i delegują logikę do odpowiednich Use Case'ów.

---

### 2. Warstwa Domenowa (Domain Layer)
Zawiera czystą logikę biznesową wolną od zależności od frameworka Androida:
- **Modele Domenowe (`domain/model/`):**
  - `Substance` — model substancji, drogi podania (RoA), progi dawek (`Dose`), parametry czasów faz (`DurationParameters`).
  - `Ingestion` — zarejestrowane przyjęcie substancji (czas, ilość, jednostka, notatki, rating Shulgina).
  - `SubstanceInteraction` — reguła interakcji pomiędzy dwoma substancjami z przypisanym statusem ryzyka (`InteractionStatus`) i notatką medyczną.
- **Przypadki Użycia (Use Cases / Engines):**
  - `InteractionChecker` — silnik sprawdzający potencjalne interakcje nowej substancji z aktywnymi substancjami przyjętymi w ciągu ostatnich 24 godzin.
  - `TimelineCalculator` — wylicza stopień intensywności działania substancji w czasie na podstawie krzywej farmakokinetycznej.
  - `ToleranceCalculator` — szacuje czas potrzebny do zresetowania receptorów (np. 5-HT2A dla psychodelików, MOR dla opioidów, GABA dla benzodiazepin).

---

### 3. Warstwa Danych (Data Layer)
Odpowiada za pobieranie, zapis i integrację źródeł danych:
- **Lokalna Baza Danych (Room v3):**
  - `AppDatabase` — baza SQLite zarządzana przez bibliotekę Room.
  - `IngestionDao` / `IngestionEntity` — przechowywanie historii zażyć użytkownika.
  - `CustomSubstanceDao` / `CustomSubstanceEntity` — trwała pamięć własnych substancji dodanych przez użytkownika.
- **Zasoby Statyczne (Assets):**
  - `substances.json` — predefiniowany katalog ponad 80 substancji z pełnymi profilami RoA i czasów faz.
  - `interactions_sin.json` — oficjalna tabela 333 interakcji SIN w formacie maszynowym.
- **Implementacje Repozytoriów (`data/repository/`):**
  - `SubstanceRepositoryImpl` — reaktywnie scala dane z zasobów JSON ze strumieniem `Flow<List<CustomSubstanceEntity>>` z bazy Room.
  - `InteractionRepositoryImpl` — odpowiada za szybkie wyszukiwanie i sortowanie interakcji według stopnia zagrożenia.
  - `IngestionRepositoryImpl` — obsługa CRUD dla sesji i zażyć.

---

## 💉 Wstrzykiwanie Zależności (Dagger Hilt)

Wszystkie zależności w aplikacji są zarządzane i wstrzykiwane deklaratywnie przez **Dagger Hilt**:
- `AppModule` — dostarcza kontekst aplikacji oraz zasoby statyczne.
- `DatabaseModule` — tworzy instancję `AppDatabase` (z zarejestrowaną migracją `MIGRATION_2_3`), udostępniając `IngestionDao` oraz `CustomSubstanceDao`.
- `RepositoryModule` — wiąże abstrakcyjne interfejsy repozytoriów z ich implementacjami w warstwie danych.
