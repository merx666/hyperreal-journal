package info.hyperreal.journal.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Certified Design Tokens for Hyperreal Journal.
 * Follows Obsidian & Bio-Telemetry aesthetic guidelines.
 */
object HyperrealTokens {
    // Canvas & Layered Obsidian Surfaces
    val Canvas = Color(0xFF07080B)         // Deepest background
    val SurfaceDark = Color(0xFF0E1017)    // Card surface
    val SurfaceRaised = Color(0xFF151824)  // Modals / Floating sheets
    val SurfaceElevated = Color(0xFF1D2232)// Active state / Hover

    // Specular Highlight Borders (1px light-source simulation)
    val BorderSubtle = Color(0x14FFFFFF)     // 8% white
    val BorderHighlight = Color(0x24FFFFFF)  // 14% white
    val BorderActiveGreen = Color(0x4000E676)// 25% green highlight

    // Brand Colors
    val BrandGreen = Color(0xFF00E676)
    val BrandGreenDark = Color(0xFF00B359)
    val BrandGreenGlow = Color(0x2600E676)

    // Bio-Telemetry Dose & Risk Palette
    val TelemetrySafe = Color(0xFF10B981)    // Emerald (Synergy / Safe)
    val TelemetryNotice = Color(0xFF38BDF8)  // Sky (Informational / Comeup)
    val TelemetryWarning = Color(0xFFF59E0B) // Amber (Caution / Peak)
    val TelemetryDanger = Color(0xFFF43F5E)  // Coral Crimson (Unsafe / Dangerous)
    val TelemetrySevere = Color(0xFFA855F7)  // Ultraviolet (Fatal risk / Serotonin)

    // Typography Tones
    val TextPrimary = Color(0xFFF1F5F9)     // Slate 100
    val TextSecondary = Color(0xFF94A3B8)   // Slate 400
    val TextMuted = Color(0xFF64748B)       // Slate 500
}

// Backward-compatible aliases mapped to certified tokens
val HyperrealGreen = HyperrealTokens.BrandGreen
val HyperrealDarkGreen = HyperrealTokens.BrandGreenDark
val HyperrealBackground = HyperrealTokens.Canvas
val HyperrealSurface = HyperrealTokens.SurfaceDark
val HyperrealSurfaceVariant = HyperrealTokens.SurfaceRaised
val HyperrealTextPrimary = HyperrealTokens.TextPrimary
val HyperrealTextSecondary = HyperrealTokens.TextSecondary
val HyperrealError = HyperrealTokens.TelemetryDanger
val HyperrealWarning = HyperrealTokens.TelemetryWarning

// Dose levels mapped to certified telemetry palette
val DoseLight = HyperrealTokens.TelemetrySafe
val DoseCommon = HyperrealTokens.TelemetryWarning
val DoseStrong = HyperrealTokens.TelemetryDanger
val DoseHeavy = HyperrealTokens.TelemetrySevere

