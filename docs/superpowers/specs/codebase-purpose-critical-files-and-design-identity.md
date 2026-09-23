# Hyperreal Journal: Codebase Purpose, Critical Files & Design Identity

> **Source of Truth Specification:** Derived strictly from existing code, configuration files, bundled clinical data, and documentation. No invented placeholders.

---

## 1. Codebase Purpose & Core Goals

### Primary Mission
**Hyperreal Journal** is a privacy-focused, offline-first Android application engineered for **Harm Reduction (Redukcja Szkód)**, psychoactive substance tracking, and real-time interaction safety analysis. It is built specifically for the [hyperreal.info](https://hyperreal.info/talk) community in partnership with the empirical datasets of **SIN (Społeczna Inicjatywa Narkopolityki)** and **TripSit**.

### What the README Leads With (The Dominant Feature)
The README establishes the core hierarchy:
> *"A privacy-focused, offline-first Android application designed for harm reduction, substance tracking, and interaction analysis. Developed entirely with local storage in mind, it utilizes the reliable SIN and TripSit interaction matrices to ensure the safety of its users."*

The application's weight is dominated by three interlocking pillars, prioritized as:
1. **Interactive Pharmacokinetics (`TimelineChart`)**: Visualizing where the human body and mind currently reside along the physiological curve (*Onset, Comeup, Peak, Offset, Afterglow, Baseline*).
2. **Harm Reduction & Drug Interactions (`InteractionChecker`)**: Real-time evaluation of poly-substance combinations against 333 clinical risk rules to prevent fatal outcomes (respiratory depression, serotonin toxicity, hypertensive crisis).
3. **Zero-Cloud Privacy Guarantee**: 100% offline, local Room SQLite persistence, zero network permissions, zero telemetry/analytics, and local encrypted JSON exports.

---

## 2. Product's Native Shape: The Bio-Timeline

Hyperreal Journal is **not a dashboard, card catalog, or generic form wizard**. Its native shape is an **organic, time-series biological curve (Bio-Timeline)**.

```
Intensity (0.0 to 1.0)
   ▲                                 [ PEAK ]
   │                                ┌────────┐
   │                               /          \
   │                  [COMEUP]    /            \    [OFFSET]
   │                     ┌───────┘              └───────┐
   │                    /                                \       [AFTERGLOW]
   │  ─────────[ONSET]─┘                                  └───────────────────────► Time (minutes)
   └───────────────────────────────────▲──────────────────────────────────────────
                                  [REAL-TIME BEACON]
                              (Pulsating HyperrealGreen)
```

### Visual Anatomy of the Native Shape
- **Horizontal Axis**: Time elapsed since ingestion, spanning from `t=0` to `t=totalMinutes`.
- **Vertical Axis**: Normalized pharmacokinetic effect intensity (`0.0` to `1.0`), computed by `TimelineCalculator`.
- **Real-Time Beacon**: A pulsating live marker (`beaconPulseScale: 0.85f -> 1.35f`, `HyperrealGreen #00E676`) showing current session progress.
- **Interactive Scrubber**: Multi-touch pointer scope (`awaitPointerEventScope`) allowing the user to drag forward in time to project sobriety and offset milestones without screen-scroll conflicts.
- **In-Trip Telemetry Points**: Check-in moments logged on the Alexander Shulgin subjective qualitative scale (`-`, `±`, `+`, `++`, `+++`, `++++`).

---

## 3. Critical Files & Architecture Anchors

| Component / Layer | Critical File | Function & Invariant |
|---|---|---|
| **Visual Design Tokens** | [`Color.kt`](file:///Users/merx/hyperrealandroidapp/app/src/main/java/info/hyperreal/journal/ui/theme/Color.kt) | `HyperrealTokens` — certified Obsidian surfaces, specular border highlights, brand green, and telemetry risk colors. |
| **Theme & Windowing** | [`Theme.kt`](file:///Users/merx/hyperrealandroidapp/app/src/main/java/info/hyperreal/journal/ui/theme/Theme.kt) | `HyperrealTheme` — AMOLED-optimized `DarkColorScheme` with edge-to-edge transparent system bar controllers. |
| **Native Component** | [`TimelineChart.kt`](file:///Users/merx/hyperrealandroidapp/app/src/main/java/info/hyperreal/journal/ui/components/TimelineChart.kt) | Compose Canvas Bézier rendering (`cubicTo`), pointer interception, beacon animation, and phase badge overlays. |
| **Pharmacokinetic Engine** | `TimelineCalculator.kt` | Domain use case calculating normalized intensity `0.0`–`1.0` across 5 duration parameters. |
| **Safety Engine** | `InteractionChecker.kt` | Temporal 24-hour window evaluator matching substances and pharmacological aliases against SIN matrices. |
| **Local Persistence** | [`AppDatabase.kt`](file:///Users/merx/hyperrealandroidapp/app/src/main/java/info/hyperreal/journal/data/local/database/AppDatabase.kt) | Room SQLite database (`version = 3`) defining `ingestions`, `check_ins`, and `custom_substances` tables. |
| **Substance Knowledge** | [`substances.json`](file:///Users/merx/hyperrealandroidapp/app/src/main/assets/substances.json) | 35 curated substances with ROAs, dosage thresholds, and phase duration ranges. |
| **Interaction Matrix** | [`interactions_sin.json`](file:///Users/merx/hyperrealandroidapp/app/src/main/assets/interactions_sin.json) | 333 clinical interaction rules with status ratings and danger notes from SIN. |
| **Agent / MCP Tooling** | [`tools.yaml`](file:///Users/merx/hyperrealandroidapp/tools.yaml) | MCP Toolbox configuration exposing SQLite tools (`execute_sql`, `check_interaction`, `get_recent_ingestions`). |
| **Canonical Copy & Labels** | [`strings.xml`](file:///Users/merx/hyperrealandroidapp/app/src/main/res/values/strings.xml) | Exact Polish system terminology, notifications, phase names, dialogs, and settings strings. |

---

## 4. Visual Identity & Design System Specification

### Aesthetic Stance: **Obsidian & Bio-Telemetry**
The visual identity reflects a laboratory bio-monitor operating in a dark room: severe, distraction-free, high-contrast, and deeply respectful of eye fatigue during altered states of consciousness.

### Color Tokens (`HyperrealTokens`)

#### 1. Canvas & Obsidian Layered Surfaces (Dark / AMOLED Default)
- **`Canvas`**: `#07080B` — Absolute deepest black-slate base.
- **`SurfaceDark`**: `#0E1017` — Primary card container surface.
- **`SurfaceRaised`**: `#151824` — Modals, bottom sheets, floating dialogs.
- **`SurfaceElevated`**: `#1D2232` — Active interactive states, hovered elements.

#### 2. Specular Highlight Borders
- **`BorderSubtle`**: `0x14FFFFFF` (8% white) — Standard resting card border.
- **`BorderHighlight`**: `0x24FFFFFF` (14% white) — Focused/selected container outline.
- **`BorderActiveGreen`**: `0x4000E676` (25% green highlight) — Active session border.

#### 3. Brand Colors
- **`BrandGreen` (HyperrealGreen)**: `#00E676` — Vibrant neon signal green; indicates vitality, safe baseline, and active time tracking.
- **`BrandGreenDark`**: `#00B359` — Pressed/secondary green.
- **`BrandGreenGlow`**: `0x2600E676` (15% green glow) — Ambient radial glow behind beacon and key metrics.

#### 4. Bio-Telemetry Dosage & Risk Palette
- **`TelemetrySafe` (DoseLight / Low Risk)**: `#10B981` (Emerald) — Safe synergy, light dose, normal baseline.
- **`TelemetryNotice` (Comeup / Informational)**: `#38BDF8` (Sky) — Onset/comeup phase, low risk decrease.
- **`TelemetryWarning` (DoseCommon / Caution / Peak)**: `#F59E0B` (Amber) — Peak intensity, common dose, mix requires caution.
- **`TelemetryDanger` (DoseStrong / Unsafe / Dangerous)**: `#F43F5E` (Coral Crimson) — Strong dose, severe medical hazard.
- **`TelemetrySevere` (DoseHeavy / Fatal Risk)**: `#A855F7` (Ultraviolet) — Heavy dose, serotonin toxicity, hypertensive crisis risk.

#### 5. Typography Tones
- **`TextPrimary`**: `#F1F5F9` (Slate 100) — Primary headers and high-emphasis numbers.
- **`TextSecondary`**: `#94A3B8` (Slate 400) — Labels, secondary captions, units (`mg`, `min`, `RoA`).
- **`TextMuted`**: `#64748B` (Slate 500) — Timestamps, timestamps relative, disabled controls.

---

## 5. Authentic Terminology & Copy (No Invented Strings)

All UI copy must draw directly from [`strings.xml`](file:///Users/merx/hyperrealandroidapp/app/src/main/res/values/strings.xml) and domain definitions:

### Pharmacokinetic Phase Names
- **`Wejście (Onset)`** — Initial absorption before noticeable effects.
- **`Wzrost (Comeup)`** — Rapid escalation in subjective intensity.
- **`Szczyt (Peak)`** — Plateau of maximal pharmacological effect.
- **`Zejście (Offset)`** — Gradual clearance and metabolization.
- **`Powrót (Afterglow)`** — Residual baseline recovery and lingering mood modulation.
- **`Koniec działania (Baseline)`** — Full return to sober baseline.

### Dosage Threshold Categories
- **`Threshold`** (Próg wyczuwalności)
- **`Light`** (Dawka lekka)
- **`Common`** (Dawka typowa)
- **`Strong`** (Dawka mocna)
- **`Heavy`** (Dawka bardzo mocna)

### Interaction Risk Badges
- **`Dangerous`** (🔴 Czerwony) — Krytyczne zagrożenie życia. Bezwzględnie unikać.
- **`Unsafe`** (🟠 Pomarańczowy) — Niebezpieczne połączenie. Ryzyko powikłań somatycznych i psychicznych.
- **`Caution`** (🟡 Żółty) — Wymagana szczególna ostrożność. Nieprzewidywalne wzmocnienie efektów.
- **`Low Risk Synergy`** (🟢 Zielony) — Niskie ryzyko ze wzajemnym wzmocnieniem działania.
- **`Low Risk No Synergy`** (⚪ Szary) — Niskie ryzyko bez istotnego wzajemnego wpływu.
- **`Low Risk Decrease`** (🔵 Niebieski) — Niskie ryzyko z osłabieniem efektu jednej z substancji.

### Notification & System Messages
- **Peak Alert**: *"🌊 Szczyt – dbaj o siebie: Jesteś na szczycie działania {substance}. Wypij wodę i zrób chwilę przerwy."*
- **Hydration Alert**: *"💧 Czas na wodę: Mija {time} od przyjęcia. Pamiętaj o nawodnieniu i odpoczynku."*
- **Widget Mode**: *"Tryb dyskretny widgetu: Ukrywa nazwę substancji na ekranie głównym"*
- **Sponsorship**: *"☕ Sponsoring: Jeśli aplikacja ci pomaga, postaw kawę – wszelka pomoc jest ceniona 🙏 (https://www.buymeacoffee.com/merx666)"*

---

## 6. Design Constraints & Anti-Patterns

### ❌ Anti-Patterns (Forbidden)
- **No Light-theme generic dashboards** with default purple/blue SaaS gradients.
- **No generic rounded cards** without the certified specular border (`0x14FFFFFF` on `#0E1017`).
- **No placeholder drug names** — use only real entries from `substances.json` (e.g., *MDMA, Psylocybina, LSD, Ketamina, Kofeina, 2C-B*).
- **No cloud sync toggles or user account registration mockups** — the app is strictly **Zero-Cloud & 100% Offline**.
- **No section-template web layouts** — interfaces must be anchored around the **timeline curve** and the **interaction risk badge**.

### ✅ Mandates (Enforced)
- **Respect the Bio-Timeline**: Any screen showing an active session must display the Bézier curve with the pulsating neon beacon and phase indicator.
- **Prominent Harm Reduction**: Interaction warnings with `Dangerous` status must never be hidden behind collapsible accordions; they must display at the top with crimson borders (`#F43F5E`).
- **AMOLED Energy Efficiency**: Primary background must remain `#07080B` to minimize power draw during long multi-hour monitoring sessions.
