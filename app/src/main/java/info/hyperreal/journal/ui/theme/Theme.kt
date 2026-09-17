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
    primary = HyperrealGreen,
    secondary = HyperrealDarkGreen,
    tertiary = HyperrealGreen,
    background = HyperrealBackground,
    surface = HyperrealSurface,
    surfaceVariant = HyperrealSurfaceVariant,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = HyperrealTextPrimary,
    onSurface = HyperrealTextPrimary,
    onSurfaceVariant = HyperrealTextSecondary,
    error = HyperrealError,
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = HyperrealDarkGreen,
    secondary = HyperrealGreen,
    tertiary = HyperrealDarkGreen,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8E8E8),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF49454F),
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
