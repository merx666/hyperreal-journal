# Hyperreal Journal

A privacy-focused, offline-first Android application designed for harm reduction, substance tracking, and interaction analysis. Developed entirely with local storage in mind, it utilizes the reliable SIN (Społeczna Inicjatywa Narkopolityki) and TripSit interaction matrices to ensure the safety of its users.

> 📖 **Dokumentacja i Wiki (PL):** Pełna polska wikipedia projektu, architektura oraz instrukcja użytkownika znajdują się w katalogu [**wiki/**](wiki/Home.md).  
> 🗺️ **Interaktywna Architektura (Archify):** Dostępna w [**docs/architecture.html**](docs/architecture.html) oraz jako wektorowy [**docs/architecture.svg**](docs/architecture.svg).  
> 📲 **Pobierz APK (v0.4.3):** Najnowsza wersja instalacyjna dostępna w zakładce [**Releases**](https://github.com/merx666/hyperreal-journal/releases/latest).  
> 🏬 **Sklepy i Dystrybucja:** Instrukcja instalacji przez **Obtainium, F-Droid, IzzyOnDroid, Amazon** znajduje się w [**docs/ALTERNATIVE_APP_STORES.md**](docs/ALTERNATIVE_APP_STORES.md).

## Key Features

- 🚨 **NAGŁA POMOC 24H (Emergency Assistance):** Błyskawiczny dostęp do wsparcia kryzysowego bezpośrednio z górnego paska aplikacji. Dedykowany modal z numerem ratunkowym 112 (Pogotowie ratunkowe / ZRM) oraz liniami kryzysowymi (Interwencja kryzysowa `514 202 619`, Kryzys samobójczy dorosłych `511 200 200`, Telefon Zaufania `116 123`, Dla dzieci i młodzieży `116 111`) z obowiązkowym dialogiem potwierdzenia przed połączeniem.
- 🌟 **Nowy Ekran Powitalny & Oświadczenie (18+):** Nowoczesny ekran powitalny Stitch z oficjalnym logo aplikacji, podsumowaniem filarów prywatności i harm reduction oraz pełnym oświadczeniem prawnym. Dostępny także w każdej chwili z menu bocznego.
- ⏱️ **Ekran Główny: Wyłącznie Aktywne Sesje:** Domyślny ekran prezentuje wyłącznie trwające sesje substancji z odliczaniem na żywo, fazami farmakokinetyki i wykresem Béziera.
- 🗄️ **Automatyczna i Manualna Archiwizacja:** Zakończone sesje automatycznie trafiają do archiwum. Dodatkowo każdą sesję można zakończyć i zarchiwizować manualnie w dowolnym momencie.
- 📑 **Dedykowany Pasek Zakładek:** Błyskawiczny podgląd: *Aktywne (X)* z pulsującym wskaźnikiem na żywo, *Zakończone (Y)* oraz *Wszystkie (Z)* z pełną historią wpisów.
- 💎 **Certyfikowany Motyw Stitch Obsidian:** Czysta czerń AMOLED (`#07080B`), brak domyślnych fioletowych odcieni M3, szmaragdowe wskaźniki (`#00E676`), subtelne obrysy i adaptacyjny splash screen.
- 📝 **Ingestion Journal:** Track dates, times, doses, and routes of administration (RoA) with automatic threshold categorization.
- 📈 **Interactive Pharmacokinetics Chart:** A unique `TimelineChart` module that visually maps duration phases (Onset, Comeup, Peak, Offset, Afterglow).
- ⚠️ **Harm Reduction & Interactions:** Dynamic mix warning system based on the full SIN/TripSit matrix (Low Risk, Caution, Unsafe, Dangerous).
- 📊 **Insights & Statistics:** Comprehensive analytics covering 30-day usage frequency, average intervals, and tolerance resets.
- 🛡️ **Zero-Cloud Privacy:** All data is strictly stored locally using Room (SQLite). Manual exports to encrypted `.json` files are supported.

## Tech Stack

- **Language**: Kotlin 1.9
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: Clean Architecture (Domain, Data, UI) + MVVM
- **Database**: Room (SQLite)
- **Dependency Injection**: Dagger Hilt
- **Asynchrony**: Kotlin Coroutines & Flows
- **Visualizations**: Custom Compose Canvas (Bar charts, complex Bézier curves, custom pointer input handling)

## Prerequisites

- Android Studio (Jellyfish or newer recommended)
- Java Development Kit (JDK) 17
- Android SDK 35
- Gradle 8.x+

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/user/hyperrealandroidapp.git
cd hyperrealandroidapp
```

### 2. Environment Setup

Ensure you have Java 17 configured in your environment, as it is required to build the project.

```bash
export JAVA_HOME=/path/to/your/jdk-17
```

In Android Studio, navigate to **File > Project Structure > SDK Location** and ensure the Gradle JDK is set to JDK 17.

### 3. Build and Run

1. Open the project in **Android Studio**.
2. Wait for the initial Gradle sync to complete.
3. Select an emulator (API 26+) or a physical device.
4. Click **Run > Run 'app'** (or `Shift + F10`).

To build from the command line:

```bash
./gradlew assembleDebug
```

## Architecture

The project follows an offline-first **Clean Architecture** approach combined with **MVVM / MVI** and reactive Kotlin Coroutines / `StateFlow`.

![Hyperreal Journal Architecture](docs/architecture.svg)

> 💡 **Interactive Architecture Map:** An interactive version with dependency inspection, focus trails, and semantic search is available at [docs/architecture.html](docs/architecture.html).

### Directory Structure

```text
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── assets/           # Bundled JSON databases (substances.json, interactions.json)
│   │   │   ├── java/info/hyperreal/journal/
│   │   │   │   ├── data/         # Repositories, Room DAOs, Entities, JSON Parsers
│   │   │   │   ├── domain/       # UseCases, Models (Substance, Ingestion, Interaction)
│   │   │   │   ├── di/           # Hilt Modules
│   │   │   │   └── ui/           # Jetpack Compose Screens, ViewModels, Components
│   │   │   └── res/              # Android Resources (Drawables, Values)
```

### Data Flow

1. User interacts with the Compose UI.
2. The UI communicates its intents to the `ViewModel`.
3. The `ViewModel` invokes `UseCases` from the Domain layer.
4. `UseCases` fetch or mutate data using `Repositories`.
5. `Repositories` interact with local sources (Room DAOs for ingestions, JSON parsers for read-only substance data).
6. State flows back up to the UI via Kotlin `StateFlow`.

### Key Components

**Substance Database (`assets/substances.json`)**
- Pre-bundled data defining substances, ROAs, dosage thresholds, and phase durations.
- Automatically parsed on first launch and cached in memory.

**Interactive Timeline (`TimelineChart.kt`)**
- Uses custom `awaitPointerEventScope` to handle multi-touch and drag interactions.
- Avoids scroll conflicts with parent `ScrollState` via sophisticated pointer interception.

## Available Scripts

| Command | Description |
|---|---|
| `./gradlew assembleDebug` | Build the debug APK |
| `./gradlew test` | Run unit tests |
| `./gradlew lint` | Run Android linting checks |
| `./gradlew clean` | Clean the build directory |

## Troubleshooting

### Java Runtime Environment Errors

**Error:** `Unsupported class file major version` or Gradle sync failures related to Java.
**Solution:** Ensure you are using JDK 17. Check your Android Studio settings: **Preferences > Build, Execution, Deployment > Build Tools > Gradle** and set the "Gradle JDK" to version 17.

### Unresolved References in Compose

**Error:** Code highlighting errors for Jetpack Compose components.
**Solution:** Click **File > Sync Project with Gradle Files** to rebuild the generated indices.

## License

In accordance with the Harm Reduction ideology, this software is released as Open Source. The interaction database and dosage data are based on external, non-profit organizations such as SIN (Społeczna Inicjatywa Narkopolityki) and TripSit.

> **Disclaimer:** Information provided within the application is strictly for educational purposes and harm reduction. The app does not encourage or condone the use of psychoactive substances.
