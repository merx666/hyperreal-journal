# Hyperreal Journal

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9-purple.svg" alt="Kotlin">
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material3-blue.svg" alt="Compose">
  <img src="https://img.shields.io/badge/Architecture-MVVM-green.svg" alt="MVVM">
  <img src="https://img.shields.io/badge/Storage-Room_DB-red.svg" alt="Room">
  <img src="https://img.shields.io/badge/Dependency_Injection-Hilt-blueviolet.svg" alt="Hilt">
</p>

**Hyperreal Journal** to lokalna, prywatna i zaawansowana aplikacja (Android) wspierająca ideologię Harm Reduction (redukcja szkód). Służy do śledzenia przyjmowanych substancji, analizowania nawyków i sprawdzania interakcji za pomocą wbudowanych i zaufanych baz (np. tabele SIN). Została stworzona w 100% z myślą o prywatności (zero-cloud, cała baza na urządzeniu).

## Główne Funkcje

- **📝 Dziennik Ingestii:** Śledzenie daty, godziny, dawki oraz drogi podania z automatyczną kategoryzacją progu dawki (Threshold, Light, Common, Strong, Heavy).
- **📈 Interaktywny Wykres Farmakokinetyki:** Unikalny moduł `TimelineChart`, który w czasie rzeczywistym ilustruje fazy działania na osi czasu (Onset, Comeup, Peak, Offset, Afterglow).
- **⚠️ Harm Reduction & Interakcje:** Dynamiczny system ostrzegania o mixach. Baza interakcji w oparciu o pełną matrycę SIN/TripSit. Kalkulator pokazujący stopień ryzyka (np. Low Risk, Caution, Unsafe, Dangerous).
- **📊 Statystyki (Insights):** Rozbudowany widok analizujący m.in. częstotliwość używania z ostatnich 30 dni, średnie przerwy pomiędzy kolejnymi użyciami oraz histogram aktywności.
- **🛡️ Pełna Prywatność:** Wszystkie wpisy trzymane są w lokalnej bazie Room. Eksport danych odbywa się ręcznie do bezpiecznego pliku `.json`.
- **🌙 Deep Dark Theme:** Neony (HyperrealGreen) oraz zoptymalizowane kontrasty pasujące do trybów nocnych (Edge-to-Edge).

## Architektura i Technologie

Projekt wykorzystuje nowoczesne wzorce i narzędzia dla Androida:
- **Język:** Kotlin
- **UI:** Jetpack Compose, Material Design 3
- **Baza danych:** Room (SQLite)
- **Architektura:** Clean Architecture (Domain, Data, UI) połączone ze wzorcem MVVM
- **Dependency Injection:** Dagger Hilt
- **Asynchroniczność:** Coroutines & Flows
- **Wizualizacje:** Niestandardowy Compose Canvas (Wykresy słupkowe oraz złożone krzywe Béziera)

## Instrukcja Uruchomienia / Budowania

Aplikacja jest standardowym projektem Gradle (Android Studio). 

1. Sklonuj repozytorium.
2. Otwórz projekt w **Android Studio**.
3. Zsynchronizuj Gradle (Kitten / JDK 17/21).
4. Zbuduj aplikację (`Build -> Make Project`) i zainstaluj na urządzeniu lub emulatorze (wymagane min. Android API 24).

## Organizacja Danych
Wzorcowe bazy (Substancje, Interakcje) są przechowywane w `app/src/main/assets/`. Przy pierwszym uruchomieniu zostaną zmapowane jako źródło wiedzy w warstwie Domain. Wszystkie ingestie trafiają do SQLite.

## Licencja

Zgodnie z ideą Harm Reduction, oprogramowanie udostępniane jest jako Open Source. Baza interakcji oraz dane dawkowania bazują na zewnętrznych, non-profit serwisach jak SIN (Społeczna Inicjatywa Narkopolityki).

> **Ostrzeżenie:** Informacje w aplikacji służą celom edukacyjnym i poprawie bezpieczeństwa (Harm Reduction). Aplikacja nie zachęca do korzystania z substancji psychoaktywnych.

---
*Stay safe.*
