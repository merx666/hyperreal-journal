package info.hyperreal.journal.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

@Composable
fun HyperrealTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
