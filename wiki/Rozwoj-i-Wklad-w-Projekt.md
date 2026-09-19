# Rozwój i Wkład w Projekt — Hyperreal Journal

Projekt **Hyperreal Journal** jest oprogramowaniem w pełni otwartoźródłowym (**Open Source**), tworzonym przez społeczność i dla społeczności. Każdy może pomóc w jego ulepszaniu!

---

## 🛠️ 1. Wymagania Środowiska Programistycznego

Aby skompilować aplikację lokalnie na swoim komputerze, potrzebujesz:
- **System operacyjny:** Linux, macOS lub Windows.
- **Java Development Kit (JDK):** JDK 17 (np. Eclipse Temurin 17 lub zintegrowany JBR z Android Studio).
- **Android SDK:** API Level 35 (Android 15), minimum API 26 (Android 8.0 Oreo).
- **Android Studio:** Jellyfish (2023.3.1) lub nowsze.
- **Build System:** Gradle 8.x z Kotlin DSL / Groovy.

---

## 💻 2. Kompilacja i Uruchomienie z Kodów Źródłowych

### Krok 1: Klonowanie repozytorium
```bash
git clone https://github.com/merx666/hyperreal-journal.git
cd hyperreal-journal
```

### Krok 2: Konfiguracja zmiennej JAVA_HOME
Upewnij się, że zmienna `JAVA_HOME` wskazuje na wersję Java 17:
```bash
# macOS (przykład ze ścieżką z Android Studio)
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Linux (przykład)
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
```

### Krok 3: Budowanie pliku instalacyjnego APK
```bash
./gradlew assembleDebug
```
Wygenerowany plik APK znajdziesz w katalogu:  
`app/build/outputs/apk/debug/app-debug.apk`.

### Krok 4: Uruchomienie testów jednostkowych
Wszystkie algorytmy farmakokinetyki, parsowania JSON i reguł interakcji są pokryte testami JUnit:
```bash
./gradlew testDebugUnitTest
```

---

## 🧪 3. Automatyzacja GitHub Actions (CI/CD)

W repozytorium skonfigurowany jest ciągły proces integracji (**GitHub Actions** w pliku `.github/workflows/android.yml`):
- Przy każdym otwarciu **Pull Requesta** lub pushu do gałęzi `main`:
  1. Sprawdzana jest poprawność kodu za pomocą lintera (`./gradlew lint`).
  2. Uruchamiane są wszystkie testy jednostkowe (`./gradlew testDebugUnitTest`).
  3. Aplikacja kompiluje się w trybie debug (`./gradlew assembleDebug`).
  4. Jeśli wszystko przejdzie na zielono ✅, gotowy pakiet APK jest automatycznie wgrywany jako artefakt wydania!

---

## 📖 4. Jak Dodać Nową Substancję do Głównej Bazy (dla każdego)

Nie musisz pisać kodu w Kotlinie, aby wzbogacić oficjalną bazę wiedzy! Wystarczy edycja pliku JSON prosto w przeglądarce:

1. Otwórz w przeglądarce plik:  
   👉 [`app/src/main/assets/substances.json`](https://github.com/merx666/hyperreal-journal/blob/main/app/src/main/assets/substances.json)
2. W prawym górnym rogu kliknij ikonkę **Ołówka (Edit this file)**. GitHub automatycznie utworzy dla Ciebie kopię roboczą (fork).
3. Na końcu pliku (przed zamykającym nawiasem `]`) dodaj nowy blok substancji ze zdefiniowanymi progami dawek i czasami faz w minutach.
4. Kliknij **Commit changes...** i wybierz opcję **Propose changes**.
5. Kliknij **Create Pull Request**.
6. Nasz bot w GitHub Actions sprawdzi poprawność składni, a po zatwierdzeniu przez opiekunów Twoja substancja pojawi się w kolejnym wydaniu aplikacji!
