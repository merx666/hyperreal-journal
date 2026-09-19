# Redukcja Szkód i Matryca Interakcji — Hyperreal Journal

Bezpieczeństwo użytkowników oraz minimalizacja ryzyka zdrowotnego (**Harm Reduction**) stanowią nadrzędny cel projektu Hyperreal Journal.

---

## 🛑 1. Baza Danych Interakcji (SIN & TripSit)

Aplikacja integruje oficjalną tabelę miksów opracowaną przez **Społeczną Inicjatywę Narkopolityki (SIN)** we współpracy z międzynarodowym projektem **TripSit**.

Baza obejmuje **333 szczegółowe reguły interakcji**, skategoryzowane pod kątem stopnia zagrożenia:

| Poziom Zagrożenia | Kolor Statusu | Znaczenie Medyczne / Kliniczne |
|---|---|---|
| **Dangerous** | 🔴 Czerwony | **Krytyczne zagrożenie życia.** Ryzyko przełomu nadciśnieniowego, zapaści krążeniowej, drgawek, zespołu serotoninowego lub śmiertelnej depresji oddechowej. Bezwzględnie unikać. |
| **Unsafe** | 🟠 Pomarańczowy | **Niebezpieczne połączenie.** Istotne ryzyko powikłań somatycznych i psychicznych, znaczne obciążenie narządów (wątroba, nerki, serce). |
| **Caution** | 🟡 Żółty | **Wymagana szczególna ostrożność.** Działania mogą potęgować się w sposób nieprzewidywalny; ryzyko nasilonych skutków ubocznych lub utraty kontroli. |
| **Low Risk Synergy** | 🟢 Zielony | **Niskie ryzyko z synergią.** Efekty wzajemnie się wzmacniają bez skokowego wzrostu toksyczności (np. psychodelik + kannabinoidy). |
| **Low Risk No Synergy** | ⚪ Szary | **Niskie ryzyko bez synergii.** Substancje działają niezależnie bez istotnego wpływu na toksyczność. |
| **Low Risk Decrease** | 🔵 Niebieski | **Niskie ryzyko z osłabieniem efektu.** Jedna substancja tłumi działanie drugiej (np. trip-sitter stosujący benzodiazepiny). |

---

## 🔍 2. Silnik Rozpoznawania i Aliasowania (`InteractionChecker`)

Wielu użytkowników posługuje się nazwami handlowymi (np. *Medikinet*, *Xanax*, *Relanium*, *OxyContin*), nazwami chemicznymi lub skrótami. Silnik aplikacji automatycznie rozwiązuje te nazwy do właściwych grup farmakologicznych w matrycy SIN:

### Mapowanie Opioidów:
Substancje takie jak: *Kodeina, Morfina, Oksykodon, Fentanyl, Buprenorfina, Metadon, Tramadol, Heroina* oraz substancje z klasą `Opioid` są automatycznie mapowane pod kategorię **Opioidy**.

### Mapowanie Benzodiazepin:
Substancje takie jak: *Alprazolam, Klonazepam, Diazepam, Lorazepam* oraz klasa `Benzodiazepiny` mapują się do kategorii **Benzodiazepiny**.

### Mapowanie Stymulantów i Fenidatów (Metylofenidat):
*Metylofenidat IR, Medikinet, Ritalin, Concerta, MPH* są precyzyjnie przypisywane do profilu interakcji stymulantów i **Amfetaminy** (ostrzegając przed śmiertelnym połączeniem z MAOI oraz podwyższoną kardiotoksycznością z alkoholem).

---

## ⚡ 3. Algorytm Sprawdzania w Czasie Rzeczywistym

Gdy użytkownik planuje zażycie nowej substancji:
1. `InteractionChecker` pobiera z bazy wpisy z ostatnich 24 godzin (okno aktywnego metabolizmu).
2. Sprawdza relacje dwukierunkowe:
   - Czy Nowa Substancja ma zdefiniowaną interakcję z którąkolwiek z Wcześniej Zażytych?
   - Czy którakolwiek z Wcześniej Zażytych substancji definiuje interakcję z Nową?
3. Wyniki są natychmiast sortowane według ważności (najgroźniejsze interakcje `Dangerous` wyświetlają się zawsze na samej górze z jaskrawym alertem).
4. Jeśli wykryto zagrożenie, aplikacja prezentuje użytkownikowi dokładną notatkę medyczną wyjaśniającą mechanizm ryzyka (np. *„Wysokie ryzyko blackoutu, depresji ośrodka oddechowego i zachłyśnięcia wymiocinami”*).
