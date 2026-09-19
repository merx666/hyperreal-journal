# Instrukcja Użytkownika — Hyperreal Journal

Poradnik dla użytkowników aplikacji opisujący najważniejsze funkcjonalności i codzienne korzystanie z Hyperreal Journal.

---

## 📥 1. Instalacja Aplikacji na Androidzie

Ponieważ aplikacja na etapie beta-testów nie jest dostępna w sklepie Google Play, pobieramy ją bezpośrednio jako pakiet instalacyjny `.apk`:

1. Wejdź na stronę wydań: [GitHub Releases — Hyperreal Journal](https://github.com/merx666/hyperreal-journal/releases).
2. Pobierz najnowszy plik o nazwie `hyperreal_journal.apk` (rozmiar: ok. 12 MB).
3. Otwórz pobrany plik w telefonie:
   - Jeśli pojawi się komunikat o instalowaniu aplikacji z nieznanych źródeł, wejdź w **Ustawienia** i zaznacz **„Zezwalaj z tego źródła”** dla Twojej przeglądarki lub menedżera plików.
   - Kliknij **Zainstaluj**.
4. Aplikacja jest gotowa do użycia. **Nie wymaga połączenia z internetem** — możesz włączyć tryb samolotowy.

---

## 📝 2. Rejestrowanie Nowego Zażycia (Kreator Krok po Kroku)

Aby dodać nowe zażycie:
1. Na ekranie głównym kliknij przycisk **`+ Wprowadź zażycie`**.
2. **Wybierz substancję:** Skorzystaj z wyszukiwarki lub przefiltruj listę po kategoriach (Stymulanty, Psychodeliki, Dysocjanty, Opioidy, Benzodiazepiny, Kannabinoidy, Własne).
3. **Wybierz drogę podania (RoA):** np. *Doustnie (Oral), Donosowo (Insufflated), Waporyzacja (Vapourised)*.
4. **Podaj dawkę:**
   - Wpisz ilość (np. `20`) i wybierz jednostkę (`mg`, `µg`, `ml`, `g`).
   - Aplikacja podświetli odpowiedni próg dawkowania:
     - **Threshold (Progowa):** najmniejsza odczuwalna ilość.
     - **Light (Lekka):** subtelne, łagodne efekty.
     - **Common (Średnia):** standardowa dawka rekreacyjna lub terapeutyczna.
     - **Strong (Mocna):** bardzo wyraźne, silne działanie.
     - **Heavy (Bardzo mocna):** wysokie ryzyko nieprzyjemnych efektów ubocznych i przedawkowania.
5. **Weryfikacja interakcji:** Jeśli w ciągu ostatnich 24 godzin przyjąłeś inne substancje, u góry ekranu pojawi się alert ostrzegający przed potencjalnymi powikłaniami.
6. Kliknij **Zapisz zażycie**.

---

## ➕ 3. Dodawanie Własnej Substancji

Jeśli stosujesz niszowy analog, nootrop, lek na receptę lub rzadkie zioło:
1. W zakładce **Substancje** kliknij pływający przycisk akcji **`+`** (FAB) w prawym dolnym rogu (lub w wyszukiwarce zażyć kliknij *„Nie ma Twojej substancji? Dodaj własną”*).
2. Wpisz nazwę substancji (np. `2-FMA`, `Fenibut`, `Noopept`).
3. Wybierz drogę podania (Doustnie, Donosowo, Waporyzacja, Podjęzykowo, Inna).
4. Wybierz preferowaną jednostkę: **[Minuty]** lub **[Godziny]**.
5. Wpisz czasy trwania poszczególnych faz:
   - Onset, Comeup, Peak, Offset, Afterglow.
   - Aplikacja w czasie rzeczywistym podlicza łączny czas działania (*Łącznie: Xh Ymin*).
6. Kliknij **Zapisz**.
7. Od tego momentu własna substancja posiada swój własny wykres `TimelineChart`, jest widoczna na liście z etykietą **Własna** i może być wybierana w dzienniku zażyć. W razie potrzeby możesz ją usunąć jednym kliknięciem ikony kosza.

---

## 📊 4. Śledzenie Wykresu i Stanu w Czasie Rzeczywistym

Po zapisaniu zażycia na ekranie głównym pojawia się aktywny panel osi czasu:
- Widzisz, w której fazie działania aktualnie się znajdujesz.
- Kolorowa linia wskazuje przewidywany moment zejścia piku i zakończenia działania.
- Możesz dotknąć i przeciągnąć palcem po wykresie, aby sprawdzić prognozowane stężenie za kilka godzin.

---

## 💾 5. Bezpieczeństwo i Kopia Zapasowa Danych

- Wszystkie wpisy są przechowywane w prywatnej bazie SQLite w pamięci Twojego telefonu.
- W ustawieniach aplikacji możesz wykonać **Ręczny eksport do pliku JSON**.
- Plik eksportu możesz zapisać na karcie pamięci lub w bezpiecznym zaszyfrowanym sejfie (np. KeePass / Cryptomator).
