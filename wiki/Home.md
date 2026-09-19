# Witaj w oficjalnej Wiki Hyperreal Journal 🌿

**Hyperreal Journal** to bezkompromisowo bezpieczna, działająca w 100% offline aplikacja na system Android, stworzona dla społeczności [hyperreal.info](https://hyperreal.info/talk) w duchu **redukcji szkód (Harm Reduction)**.

Aplikacja łączy w sobie precyzyjny dziennik zażyć, silnik obliczania krzywych farmakokinetycznych w czasie rzeczywistym oraz zintegrowaną matrycę interakcji opartą na danych **Społecznej Inicjatywy Narkopolityki (SIN)** i **TripSit**.

---

## 🗺️ Mapa Architektury Systemu

Poniższy diagram przedstawia przepływ danych i architekturę warstwową (Clean Architecture) zastosowaną w aplikacji:

![Hyperreal Journal Architecture](../docs/architecture.svg)

> 💡 **Interaktywny diagram Archify:**  
> Dostępna jest pełna, interaktywna wersja diagramu z inspekcją zależności, ścieżkami skupienia i wyszukiwaniem semantycznym:  
> 👉 [Otwórz docs/architecture.html](https://github.com/merx666/hyperreal-journal/blob/main/docs/architecture.html)

---

## 📚 Spis Treści Dokumentacji

1. **[Architektura Systemu](Architektura-Systemu)**  
   Szczegółowe omówienie Clean Architecture, warstw UI (Compose), Domain i Data, wzorca MVVM/MVI, Dagger Hilt oraz reaktywnego strumienia danych Kotlin Flow.

2. **[Baza Danych i Modele Danych](Baza-Danych-i-Modele)**  
   Lokalna baza Room SQLite (wersja v3), migracje schematu, przechowywanie wpisów zażyć, baza własnych substancji oraz struktury predefiniowanych plików JSON (`substances.json`, `interactions_sin.json`).

3. **[Farmakokinetyka i Wykresy Działania](Farmakokinetyka-i-Wykresy)**  
   Algorytmy `TimelineCalculator`, wyliczanie etapów farmakokinetyki (*Onset, Comeup, Peak, Offset, Afterglow*) oraz autorski komponent `TimelineChart` renderowany na Canvasie Jetpack Compose.

4. **[Redukcja Szkód i Matryca Interakcji](Redukcja-Szkod-i-Interakcje)**  
   Zasady działania modułu `InteractionChecker`, pełna integracja z 333 regułami tabeli miksów SIN, poziomy ryzyka (*Low Risk, Caution, Unsafe, Dangerous*) oraz bezpieczne mapowanie aliasów.

5. **[Instrukcja Użytkownika](Instrukcja-Uzytkownika)**  
   Krok po kroku: jak zainstalować plik APK, jak prowadzić dziennik zażyć, jak zdefiniować własną substancję oraz jak wykonywać bezpieczne kopie zapasowe.

6. **[Rozwój i Wkład w Projekt](Rozwoj-i-Wklad-w-Projekt)**  
   Wymagania środowiska (JDK 17, Android SDK 35), budowanie aplikacji, testy jednostkowe, automatyzacja GitHub Actions CI oraz poradnik dodawania substancji przez Pull Request.

---

## 🛡️ Kluczowe Zasady Projektu

* **Zero-Cloud / 100% Offline:** Aplikacja nie posiada uprawnień sieciowych, nie wysyła żadnych zapytań HTTP, nie posiada Google Analytics, Firebase ani żadnych trackerów.
* **Lokalna Baza Danych:** Wszystkie wrażliwe dane użytkownika pozostają wyłącznie w zaszyfrowanym magazynie urządzenia.
* **Otwarty Kod (Open Source):** Pełna transparentność algorytmów dawkowania i interakcji w celu ratowania zdrowia i życia.
