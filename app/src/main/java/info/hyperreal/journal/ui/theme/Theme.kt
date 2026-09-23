package info.hyperreal.journal.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = HyperrealTokens.BrandGreen,
    onPrimary = Color(0xFF07080B),
    primaryContainer = HyperrealTokens.SurfaceElevated,
    onPrimaryContainer = HyperrealTokens.BrandGreen,
    inversePrimary = HyperrealTokens.BrandGreenDark,

    secondary = HyperrealTokens.BrandGreenDark,
    onSecondary = Color.White,
    secondaryContainer = HyperrealTokens.SurfaceRaised,
    onSecondaryContainer = HyperrealTokens.TextPrimary,

    tertiary = HyperrealTokens.TelemetryNotice,
    onTertiary = Color(0xFF07080B),
    tertiaryContainer = HyperrealTokens.SurfaceRaised,
    onTertiaryContainer = HyperrealTokens.TelemetryNotice,

    background = HyperrealTokens.Canvas,
    onBackground = HyperrealTokens.TextPrimary,

    surface = HyperrealTokens.SurfaceDark,
    onSurface = HyperrealTokens.TextPrimary,
    surfaceVariant = HyperrealTokens.SurfaceRaised,
    onSurfaceVariant = HyperrealTokens.TextSecondary,
    surfaceTint = HyperrealTokens.BrandGreen,
    surfaceDim = HyperrealTokens.Canvas,
    surfaceBright = HyperrealTokens.SurfaceElevated,

    surfaceContainerLowest = HyperrealTokens.Canvas,
    surfaceContainerLow = HyperrealTokens.SurfaceDark,
    surfaceContainer = HyperrealTokens.SurfaceDark,
    surfaceContainerHigh = HyperrealTokens.SurfaceRaised,
    surfaceContainerHighest = HyperrealTokens.SurfaceElevated,

    outline = HyperrealTokens.BorderSubtle,
    outlineVariant = HyperrealTokens.BorderHighlight,

    error = HyperrealTokens.TelemetryDanger,
    onError = Color.White,
    errorContainer = Color(0xFF3B1219),
    onErrorContainer = HyperrealTokens.TelemetryDanger
)

private val LightColorScheme = lightColorScheme(
    primary = HyperrealDarkGreen,
    onPrimary = Color.White,
    primaryContainer = HyperrealTokens.SurfaceElevatedLight,
    onPrimaryContainer = HyperrealDarkGreen,
    inversePrimary = HyperrealGreen,

    secondary = HyperrealGreen,
    onSecondary = Color.Black,
    secondaryContainer = HyperrealTokens.SurfaceRaisedLight,
    onSecondaryContainer = Color(0xFF0F172A),

    tertiary = HyperrealDarkGreen,
    onTertiary = Color.White,
    tertiaryContainer = HyperrealTokens.SurfaceRaisedLight,
    onTertiaryContainer = HyperrealDarkGreen,

    background = HyperrealTokens.CanvasLight,
    onBackground = Color(0xFF0F172A),

    surface = HyperrealTokens.SurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = HyperrealTokens.SurfaceRaisedLight,
    onSurfaceVariant = Color(0xFF475569),
    surfaceTint = HyperrealDarkGreen,
    surfaceDim = HyperrealTokens.SurfaceRaisedLight,
    surfaceBright = HyperrealTokens.SurfaceLight,

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = HyperrealTokens.SurfaceLight,
    surfaceContainer = HyperrealTokens.SurfaceRaisedLight,
    surfaceContainerHigh = HyperrealTokens.SurfaceElevatedLight,
    surfaceContainerHighest = Color(0xFFCBD5E1),

    outline = HyperrealTokens.BorderSubtleLight,
    outlineVariant = HyperrealTokens.BorderHighlightLight,

    error = HyperrealError,
    onError = Color.White,
    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = HyperrealError
)

@Composable
fun HyperrealTheme(
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkMode) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDarkMode
            insetsController.isAppearanceLightNavigationBars = !isDarkMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
