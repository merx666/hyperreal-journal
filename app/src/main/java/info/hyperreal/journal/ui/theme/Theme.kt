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
    secondary = HyperrealTokens.BrandGreenDark,
    tertiary = HyperrealTokens.TelemetryNotice,
    background = HyperrealTokens.Canvas,
    surface = HyperrealTokens.SurfaceDark,
    surfaceVariant = HyperrealTokens.SurfaceRaised,
    onPrimary = Color(0xFF07080B),
    onSecondary = Color.White,
    onTertiary = Color(0xFF07080B),
    onBackground = HyperrealTokens.TextPrimary,
    onSurface = HyperrealTokens.TextPrimary,
    onSurfaceVariant = HyperrealTokens.TextSecondary,
    outline = HyperrealTokens.BorderSubtle,
    outlineVariant = HyperrealTokens.BorderHighlight,
    error = HyperrealTokens.TelemetryDanger,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = HyperrealDarkGreen,
    secondary = HyperrealGreen,
    tertiary = HyperrealDarkGreen,
    background = HyperrealTokens.CanvasLight,
    surface = HyperrealTokens.SurfaceLight,
    surfaceVariant = HyperrealTokens.SurfaceRaisedLight,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
    outline = HyperrealTokens.BorderSubtleLight,
    outlineVariant = HyperrealTokens.BorderHighlightLight,
    error = HyperrealError,
    onError = Color.White
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
