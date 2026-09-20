# Frontier Design RAG Knowledge Base: Hyperreal Journal Mobile

**Domain:** Harm-reduction, psychoactive pharmacokinetics, substance logging, and interaction telemetry.  
**Platform:** Android (Jetpack Compose, Material 3, Edge-to-Edge).  
**Aesthetic Thesis:** *Tactile Bio-Telemetry & Cyber-Editorial Obsidian* (Rivaling Linear, Apple Health, and Raycast).

---

## 1. Competitive Benchmark Teardown & Innovations

| Benchmark App | Key UX/Visual Mechanism | How Hyperreal Journal Outpaces It |
|---------------|-------------------------|-----------------------------------|
| **Linear Mobile** | Monochromatic obsidian palette (`#090A0F`), 1px subtle borders (`rgba(255,255,255,0.08)`), high-density typography. | Applied to substance lists and journal feeds: eliminates muddy `#121212` gray cards, using layered obsidian surfaces with specular top highlights. |
| **Apple Health (Vitals)** | Fluid multi-phase physiological curves, dual-stop gradient area fills, scrub scrubber with haptic ticks. | Applied to `TimelineChart`: replaces raw segmented line graphs with anti-aliased cubic Bezier curves, glowing ambient fill, and a live pulsating "Current Time" beacon. |
| **Raycast** | Tactile micro-interactions, spring press dynamics (`0.97` scale), instant zero-latency feedback. | Applied to action buttons, FABs, and dose chips: adds instant haptic click (`HapticFeedbackType.TextHandleMove`) and spring scale transitions on tap. |
| **Cron / Notion Calendar** | High-density temporal telemetry, glanceable status pills with glowing status pips. | Applied to Mix Calculator & Ingestion cards: replace heavy alert banners with compact, high-legibility telemetry badges with ambient status glow. |
| **Whoop / Oura** | Circadian/phase staging indicators, clear visual distinction between past, active peak, and recovery. | Applied to Active Ingestions: real-time animated countdown progress ring/bar indicating exact phase (Onset -> Comeup -> Peak -> Offset -> Afterglow). |

---

## 2. Anti-AI Slop Pruning Log

* **BANNED:** Flat muddy gray `#121212` surfaces with no elevation nuance.  
  * **REPLACED WITH:** Deep Obsidian hierarchy: Canvas `#07080B`, Card `#0F1118`, Elevated Sheet `#161924`, Highlight Border `rgba(255, 255, 255, 0.08)`.
* **BANNED:** Generic bright traffic-light colors (e.g. garish neon yellow `#FFFFEB3B` and plain red `#F44336`).  
  * **REPLACED WITH:** Refined Bio-Telemetry Palette: Microdose Sky (`#38BDF8`), Light Emerald (`#10B981`), Common Warm Amber (`#F59E0B`), Strong Rose Coral (`#F43F5E`), Heavy Ultraviolet (`#A855F7`).
* **BANNED:** Static flat line charts with harsh jagged angle joints.  
  * **REPLACED WITH:** Continuous smooth Cubic Hermite Spline / Bezier curves with vertical linear gradient glow (`Brush.verticalGradient`) and ambient shadow penumbra.
* **BANNED:** Clunky full-width alert boxes with default Material 3 warning icons.  
  * **REPLACED WITH:** Precision telemetry badges: 1px border, 12sp semi-bold monospace/sans, pulsing status dot, and expandable pharmacological risk details.

---

## 3. Concrete Design Tokens & Motion Specs

### Color Palette (Hex & Compose Color)
```kotlin
object HyperrealPalette {
    // Background & Obsidian Surfaces
    val Canvas = Color(0xFF07080B)
    val SurfaceDark = Color(0xFF0E1017)
    val SurfaceRaised = Color(0xFF151822)
    val SurfaceHighlight = Color(0xFF1D2230)

    // Specular Borders
    val BorderSubtle = Color(0x14FFFFFF)    // 8% white
    val BorderHighlight = Color(0x28FFFFFF) // 16% white
    val BorderActive = Color(0x4D00E676)    // 30% brand green

    // Brand Neon Accent (Harm Reduction Safe Green)
    val NeonGreen = Color(0xFF00E676)
    val NeonGreenGlow = Color(0x2600E676)

    // Telemetry Status Accents
    val StatusSafe = Color(0xFF10B981)      // Synergistic / Low Risk
    val StatusNotice = Color(0xFF38BDF8)    // Informational / Caution
    val StatusWarning = Color(0xFFF59E0B)   // Caution / Unsafe Mix
    val StatusDanger = Color(0xFFF43F5E)    // Dangerous Interaction
    val StatusSevere = Color(0xFFA855F7)    // Serotonin Syndrome / Fatal Risk

    // Text Contrast Hierarchy
    val TextPrimary = Color(0xFFF1F5F9)    // Slate 100
    val TextSecondary = Color(0xFF94A3B8)  // Slate 400
    val TextMuted = Color(0xFF64748B)      // Slate 500
}
```

### Kinetic & Motion Parameters
* **Spring Dynamics for Tap/Press:** `spring(dampingRatio = 0.75f, stiffness = 400f)`
* **Scale on Active Press:** `0.97f` (instant physical response)
* **Timeline Chart Scrubbing:** Velocity-damped scrubbing with `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.TextHandleMove)` on phase threshold crossing.
* **Card Stagger Animation:** `tween(durationMillis = 220, easing = FastOutSlowInEasing)` with 35ms stagger index offset.

---

## 4. Component Architecture Blueprints

### A. Telemetry Timeline (`TimelineChart`)
1. **Multi-layer Path Rendering:** 
   - Base Ambient Glow: Wide blurred stroke (`strokeWidth = 6dp`, alpha `0.25`).
   - Core Line: Anti-aliased smooth bezier line (`strokeWidth = 2.5dp`, solid phase color).
   - Area Fill: Gradient brush fading from 25% phase color at peak to 0% at chart baseline.
2. **Current Time Beacon:**
   - Vertical dashed guide line (`PathEffect.dashPathEffect(floatArrayOf(10f, 10f))`).
   - Pulsing double-ring glowing beacon with smooth sine breathing animation (`0.85f -> 1.15f`).
3. **Interactive Scrub Overlay:**
   - Floating glassmorphism pill showing: exact timestamp, active phase name, and estimated remaining duration.

### B. Obsidian Bento Ingestion Cards (`JournalScreen`)
1. **Surface:** `#0E1017` with subtle 1px border `rgba(255,255,255,0.08)`.
2. **Telemetry Header:** Substance name in tight-tracking bold (`-0.02em`), accompanied by dosage badge with microdose/common/strong color coding.
3. **Live Phase Countdown:** Compact progress bar with luminous gradient corresponding to active phase, accompanied by time elapsed / time remaining countdown.
4. **Haptic Delete / Edit / Check-In:** Native touch feedback on every interactive touchpoint.

### C. Precision Mix Interaction Calculator (`MixCalculatorScreen`)
1. **Substance Selector Pills:** Segmented chips with glowing active states.
2. **Risk Severity Matrix Card:** Replaced with high-grade telemetry card containing:
   - Status Pip (pulsing dot in emerald/amber/crimson/purple).
   - Interaction Classification Title in bold letter-spaced typography.
   - Pharmacological Mechanism Summary (e.g. "Ryzyko Zespołu Serotoninowego (MAOI + SSRI)").
   - Actionable Harm Reduction Guidance (e.g. "Nie łącz pod żadnym pozorem", "Zmniejsz dawkę o 50%").
