# Design Doc: Opioids Expansion & Dose Calculators (THC & Alcohol)

**Data utworzenia:** 2026-09-18  
**Autor:** Antigravity AI & Merx  
**Status:** Zatwierdzony (Approved)  

---

## 1. Cel i Tło (Context & Goals)

Aplikacja **Hyperreal Journal** koncentruje się na rzetelnej redukcji szkód (Harm Reduction), śledzeniu czasu trwania faz działania substancji oraz wczesnym ostrzeganiu o niebezpiecznych interakcjach wielosubstancyjnych.
W obecnej wersji użytkownicy napotykają na dwa kluczowe ograniczenia:
1. **Brak szczegółowej gałęzi opioidów:** W bazie istnieje tylko ogólna kategoria `Opioidy` oraz podstawowy `Tramadol`, co uniemożliwia precyzyjne śledzenie dawek i profilu metabolicznego powszechnie stosowanych substancji (Kodeina, Morfina, Oksykodon, Fentanyl, Buprenorfina, Metadon).
2. **Konieczność ręcznego przeliczania dawek w głowie:** W przypadku marihuany (Cannabis) użytkownik operuje gramami suszu oraz zawartością % THC, a w przypadku alkoholu objętością trunku w ml oraz % objętościowym alkoholu. Wprowadzanie bezpośrednio wartości w mg/g bez pomocy asystenta jest uciążliwe i sprzyja błędom.

---

## 2. Architektura i Zakres Zmian

### A. Dynamiczny Kalkulator Dawek w UI (`DoseConverter` & `EnterDoseScreen`)

#### 1. Nowy komponent domenowy: `info.hyperreal.journal.domain.usecase.DoseConverter`
Odpowiedzialny za czystą matematykę przeliczeń jednostek oraz generowanie czytelnych znaczników tekstowych do notatek.

- **Kalkulator THC (Kannabinoidy):**
  - Wejście: `dryHerbGrams: Float`, `thcPercentage: Float`
  - Wzór: $\text{thcMg} = \text{dryHerbGrams} \times \left(\frac{\text{thcPercentage}}{100}\right) \times 1000$
  - Adnotacja: `[Kalkulator: {dryHerbGrams} g suszu ({thcPercentage}% THC)]`
  - Zaokrąglenie: 1 miejsce po przecinku (np. `45.0 mg`)

- **Kalkulator Alkoholu:**
  - Wejście: `volumeMl: Float`, `alcoholPercentage: Float`, opcjonalna nazwa presetu (`beverageName: String?`)
  - Wzór: $\text{ethanolGrams} = \text{volumeMl} \times \left(\frac{\text{alcoholPercentage}}{100}\right) \times 0.8$
  - Presety standardowe:
    - 🍺 **Piwo:** 500 ml, 5.0% vol $\to$ 20.0 g czystego etanolu
    - 🍷 **Wino:** 150 ml, 12.0% vol $\to$ 14.4 g czystego etanolu
    - 🥃 **Wódka / Shot:** 50 ml, 40.0% vol $\to$ 16.0 g czystego etanolu
    - 🍸 **Drink:** 250 ml, 8.0% vol $\to$ 16.0 g czystego etanolu
    - ✏️ **Własny:** dowolne ml i % vol
  - Adnotacja: `[Kalkulator: {beverageName ?: "Alkohol"} {volumeMl} ml ({alcoholPercentage}% vol)]`
  - Zaokrąglenie: 1 miejsce po przecinku (np. `20.0 g`)

#### 2. Interfejs Użytkownika (`EnterDoseScreen.kt`)
- Detekcja kontekstowa na podstawie `substance.id`:
  - Gdy `substance.id == "Cannabinoidy"` $\to$ wyświetlenie karty kalkulatora suszu z polami wagi (np. 0.1g - 2.0g) oraz % THC (domyślnie np. 18%, zakres 1-35%). Na dole karty na bieżąco prezentowana jest przeliczona dawka w mg THC.
  - Gdy `substance.id == "Alkohol"` $\to$ wyświetlenie segmentu presetów (Piwo, Wino, Shot, Drink, Własny) oraz pól edycji ml i % vol z dynamicznym podglądem wyliczonych gramów etanolu.
  - Dla pozostałych substancji $\to$ standardowe pole tekstowe dawki wraz z widełkami (Threshold/Light/Common/Strong).
- Akcja „Dalej”:
  - Przekazuje wyliczoną dawkę (`Float`) do ViewModelu.
  - Automatycznie dokleja wygenerowaną adnotację do pola notatek (`notes`) w `AddIngestionViewModel`.

---

### B. Rozszerzenie Bazy Danych Opioidów (`substances.json`)

Dodajemy kompletne, standaryzowane profile farmakologiczne do `app/src/main/assets/substances.json`:

1. **Kodeina (`Codeine` / `Kodeina`):**
   - ROA: Doustna (`Oral`)
   - Dawki: Threshold: 15 mg, Light: 30 mg, Common: 60 mg, Strong: 100 mg, Heavy: 150 mg.
   - Czasy: Onset: 20 min, Comeup: 30 min, Peak: 120 min, Offset: 120 min, Afterglow: 60 min, Total: 300 min.
   - Harm Reduction: Ryzyko toksyczności paracetamolu w preparatach złożonych (wymagana ekstrakcja CWE). Efekt sufitowy ~300-400 mg.

2. **Tramadol (`Tramadol`):**
   - ROA: Doustna (`Oral`)
   - Dawki: Threshold: 25 mg, Light: 50 mg, Common: 100 mg, Strong: 150 mg, Heavy: 250 mg.
   - Czasy: Onset: 40 min, Comeup: 45 min, Peak: 240 min, Offset: 180 min, Total: 480 min.
   - Harm Reduction: Ryzyko obniżenia progu drgawkowego i napadów padaczkowych (maks. 400 mg/dobę). Zakaz łączenia z lekami serotonergicznymi (zespół serotoninowy).

3. **Morfina (`Morphine` / `Morfina`):**
   - ROA: Doustna (`Oral`), Dożylna (`Intravenous`), Doodbytnicza (`Rectal`)
   - Dawki (Oral): Threshold: 10 mg, Light: 15 mg, Common: 30 mg, Strong: 50 mg, Heavy: 75 mg.
   - Dawki (IV): Threshold: 2 mg, Light: 5 mg, Common: 10 mg, Strong: 15 mg, Heavy: 25 mg.
   - Czasy: Onset: 20 min, Comeup: 40 min, Peak: 120 min, Offset: 120 min, Total: 300 min.
   - Harm Reduction: Niska biodostępność doustna (~30%). Silna depresja oddechowa. Nalokson w pogotowiu.

4. **Oksykodon (`Oxycodone` / `Oksykodon`):**
   - ROA: Doustna (`Oral`), Donosowa (`Insufflated`)
   - Dawki (Oral): Threshold: 2.5 mg, Light: 5 mg, Common: 10 mg, Strong: 20 mg, Heavy: 30 mg.
   - Czasy: Onset: 20 min, Comeup: 30 min, Peak: 90 min, Offset: 90 min, Total: 240 min.
   - Harm Reduction: ~1.5x silniejszy od doustnej morfiny. Ryzyko dose-dumpingu przy kruszeniu tabletek retard.

5. **Fentanyl (`Fentanyl`):**
   - ROA: Podjęzykowa/Dopoliczkowa (`Sublingual`), Dożylna (`Intravenous`), Przezskórna (`Transdermal`)
   - Dawki (Sublingual/IV): Threshold: 0.05 mg (50 mcg), Light: 0.1 mg (100 mcg), Common: 0.2 mg (200 mcg), Strong: 0.4 mg (400 mcg), Heavy: 0.8 mg (800 mcg).
   - Czasy (IV/Sublingual): Onset: 2 min, Comeup: 5 min, Peak: 20 min, Offset: 45 min, Total: 90 min.
   - Harm Reduction: Śmiertelne ryzyko (50-100x silniejszy od morfiny). Śmiertelna dawka dla osoby bez tolerancji to ~2 mg. Ryzyko sztywności klatki piersiowej uniemożliwiającej oddech.

6. **Buprenorfina (`Buprenorphine` / `Buprenorfina`):**
   - ROA: Podjęzykowa (`Sublingual`), Przezskórna (`Transdermal`)
   - Dawki (Sublingual): Threshold: 0.2 mg, Light: 0.5 mg, Common: 1.5 mg, Strong: 3.0 mg, Heavy: 6.0 mg.
   - Czasy: Onset: 30 min, Comeup: 45 min, Peak: 150 min, Offset: 360 min, Total: 720 min.
   - Harm Reduction: Częściowy agonista o bardzo silnym powinowactwie. Wypiera inne opioidy wywołując gwałtowny zespół odstawienny (*precipitated withdrawal*).

7. **Metadon (`Methadone` / `Metadon`):**
   - ROA: Doustna (`Oral`)
   - Dawki: Threshold: 5 mg, Light: 10 mg, Common: 20 mg, Strong: 40 mg, Heavy: 60 mg.
   - Czasy: Onset: 45 min, Comeup: 60 min, Peak: 180 min, Offset: 720 min, Total: 1440 min.
   - Harm Reduction: Bardzo długi okres półtrwania (24-36h). Ryzyko kumulacji dawek i opóźnionego przedawkowania.

### C. Zgodność z Mapą Interakcji SIN (`InteractionChecker`)
- Wszystkie poszczególne opioidy będą traktowane przez `InteractionChecker` jako należące do klasy `Opioidy` (lub posiadające aliasy mapujące na wpis `Opioidy` w tabeli SIN).
- Gwarantuje to natychmiastowe wyświetlanie krytycznych ostrzeżeń o depresji oddechowej przy równoczesnym zażyciu alkoholu, benzodiazepin, barbituranów, GHB/GBL czy dysocjantów.

---

## 3. Plan Weryfikacji i Testów

### Testy Jednostkowe
- `DoseConverterTest.kt`:
  - Weryfikacja obliczeń THC dla różnych wag i stężeń procentowych.
  - Weryfikacja obliczeń czystego etanolu dla piwa, wina, wódki oraz wartości niestandardowych.
  - Weryfikacja generowania adnotacji tekstowych do notatek.
- `InteractionCheckerTest.kt`:
  - Weryfikacja wykrywania interakcji dla nowo dodanych opioidów (np. Oksykodon + Alkohol $\to$ *Niebezpieczne*).
- `SubstanceRepositoryTest.kt`:
  - Weryfikacja poprawnego parsowania nowych profili z `substances.json`.

---

## 4. Grok-bot Prompt Artifact
Zapisany w pliku `brain/.../grokbot_prompt_opioids.md` na wypadek potrzeby rozbudowy o kolejne rzadkie substancje (np. O-DSMT, Tapentadol, Nitazeny).
