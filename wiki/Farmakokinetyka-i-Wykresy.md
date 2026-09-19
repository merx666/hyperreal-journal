# Farmakokinetyka i Wykresy Działania — Hyperreal Journal

Jednym z najważniejszych i unikalnych elementów aplikacji **Hyperreal Journal** jest moduł dynamicznej wizualizacji farmakokinetyki — **`TimelineChart`**.

Zamiast surowych tabel z liczbami, użytkownik otrzymuje czytelny, interaktywny wykres krzywej stężenia/działania substancji w czasie.

---

## ⏳ 1. Fazy Działania Substancji

Każda droga podania (RoA) substancji w aplikacji opisana jest przez 5 kluczowych etapów czasowych (wyrażonych w minutach):

```
Poziom
działania
   ▲                  [ PEAK ]
   │                 ┌────────┐
   │                /          \
   │    [COMEUP]   /            \   [OFFSET]
   │       ┌──────┘              └──────┐
   │      /                              \      [AFTERGLOW]
   │  ───┘                                └───────────────────────
   └──────────────────────────────────────────────────────────────► Czas (min)
      [ONSET]
```

1. **Onset (Początek działania):** Czas od momentu przyjęcia do pojawienia się pierwszych zauważalnych efektów fizjologicznych lub psychicznych.
2. **Comeup (Wejście / Ładowanie):** Faza gwałtownego narastania intensywności działania.
3. **Peak (Szczyt działania):** Okres maksymalnej intensywności efektów.
4. **Offset (Schodzenie):** Stopniowe słabnięcie głównych efektów substancji.
5. **Afterglow (Stan po / Efekty resztkowe):** Czas, w którym główne działanie minęło, ale metabolity lub modulacja neuroprzekaźników wywołują subtelne odczucia (często spokój, zmęczenie lub lekka stymulacja).
6. **Total:** Szacowany łączny czas od podania do całkowitego powrotu do stanu wyjściowego.

---

## 🧮 2. Silnik Obliczeniowy (`TimelineCalculator`)

Klasa `TimelineCalculator` w warstwie domenowej przelicza aktualny czas od momentu przyjęcia dawki na znormalizowany poziom intensywności działania (wartość od `0.0` do `1.0`):

```kotlin
fun calculateIntensity(elapsedMinutes: Float, duration: DurationParameters): Float {
    val t0 = 0f
    val t1 = duration.onset
    val t2 = t1 + duration.comeup
    val t3 = t2 + duration.peak
    val t4 = t3 + duration.offset
    val t5 = t4 + duration.afterglow

    return when {
        elapsedMinutes < t1 -> (elapsedMinutes / t1.coerceAtLeast(1f)) * 0.1f // Lekkie odczucie wstępne
        elapsedMinutes < t2 -> 0.1f + 0.9f * ((elapsedMinutes - t1) / (t2 - t1).coerceAtLeast(1f)) // Wejście do 1.0
        elapsedMinutes < t3 -> 1.0f // Szczyt
        elapsedMinutes < t4 -> 1.0f - 0.7f * ((elapsedMinutes - t3) / (t4 - t3).coerceAtLeast(1f)) // Zejście do 0.3
        elapsedMinutes < t5 -> 0.3f * (1f - (elapsedMinutes - t4) / (t5 - t4).coerceAtLeast(1f)) // Afterglow
        else -> 0f // Zakończenie działania
    }
}
```

---

## 🎨 3. Rysowanie Wykresu (`TimelineChart.kt`)

Komponent UI korzysta z autorskiej implementacji na **Canvasie Jetpack Compose**:
- **Krzywe Béziera:** Punkty przejścia między fazami są wygładzane za pomocą sześciennych i kwadratowych krzywych Béziera (`cubicTo`), co daje naturalny, biologiczny kształt krzywej wchłaniania i eliminacji leku.
- **Wskaźnik Czasu Rzeczywistego:** Pionowa linia z neonowym akcentem (`HyperrealGreen`) wskazuje dokładny punkt, w którym użytkownik znajduje się w danej chwili.
- **Wsparcie dla Dotyku i Gestów:** Moduł wykorzystuje `awaitPointerEventScope`, umożliwiając przeciąganie palcem po osi czasu, aby podejrzeć przewidywany stan za godzinę, dwie lub po powrocie do domu.
- **Brak Konfliktów ze Scrollem:** Dzięki zaawansowanej interceptacji zdarzeń wskaźnika, przesuwanie po wykresie nie koliduje z przewijaniem ekranu w dół.

---

## ⏱️ 4. Wsparcie dla Własnych Substancji

W przypadku dodawania własnej substancji przez `AddCustomSubstanceDialog`, użytkownik może wprowadzać czasy faz w **minutach** lub **godzinach**. Narzędzie pomocnicze `CustomSubstanceDurationHelper` automatycznie przelicza i waliduje wartości:
- `toMinutes(value, unit)`
- `toDisplayHours(minutes)`
- Automatyczne wyliczanie sumarycznego czasu trwania: `total = onset + comeup + peak + offset + afterglow`.
