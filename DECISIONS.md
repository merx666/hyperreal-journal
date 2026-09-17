# Architecture & Product Decisions (Hyperreal App)

## 1. Offline-First & JSON Bundling
Decyzja: Zamiast odpytywać API PsychonautWiki/TripSit w czasie rzeczywistym, aplikacja w całości polega na pliku `assets/substances.json`.
Powód: Zapewnienie działania offline, większa niezawodność, ochrona prywatności (brak zapytań HTTP) i uniezależnienie się od zmian w zewnętrznych API.
Ryzyko: Dane mogą się zdezaktualizować.
Mitygacja: Skrypt w Pythonie (Epic 9) pozwoli na okresowe generowanie nowego pliku `substances.json` i aktualizację aplikacji.

## 2. Kategoryzacja siły dawek
Decyzja: Używamy sztywnych przedziałów opartych na threshold, light, common, strong, heavy z TripSit.
Powód: Zgodność z założeniami harm reduction i prostota implementacji.

## 3. Play Store i dystrybucja
Decyzja: Aplikacja jako plik APK dystrybuowany bezpośrednio (lub ew. F-Droid). Rezygnacja z Play Store w MVP.
Powód: Minimalizacja ryzyka odrzucenia ze względu na politykę Google dot. substancji, skupienie na wartości dla społeczności (harm reduction).
