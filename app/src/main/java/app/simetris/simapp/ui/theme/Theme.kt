package app.simetris.simapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightPalette = lightColorScheme(
    primary = DeepTeal, onPrimary = Color.White,
    primaryContainer = LightTurquoise, onPrimaryContainer = DarkTeal,
    secondary = Turquoise, onSecondary = TextColor,
    secondaryContainer = LightTurquoise, onSecondaryContainer = DarkTeal,
    tertiary = DeepTeal, onTertiary = Color.White,
    tertiaryContainer = LightTurquoise, onTertiaryContainer = DarkTeal,
    background = BackgroundColor, onBackground = TextColor,
    surface = Color.White, onSurface = TextColor,
    surfaceVariant = Color(0xFFEDF3F5), onSurfaceVariant = MutedTextColor,
    outline = BorderColor, outlineVariant = BorderColor, error = ErrorColor, errorContainer = LightErrorColor,
)

private val DarkPalette = darkColorScheme(
    primary = Turquoise, onPrimary = Color(0xFF003B3E),
    primaryContainer = DarkTeal, onPrimaryContainer = Color.White,
    secondary = Turquoise, secondaryContainer = DarkTeal,
    onSecondaryContainer = Color.White,
    background = Color(0xFF121B21), onBackground = Color(0xFFF0F5F6),
    surface = Color(0xFF1B2830), onSurface = Color(0xFFF0F5F6),
    surfaceVariant = Color(0xFF26363E), onSurfaceVariant = Color(0xFFC2D1D5),
    error = Color(0xFFFFB4AB),
)

@Composable
fun SimAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkPalette else LightPalette, typography = Typography,
        shapes = Shapes(small = RoundedCornerShape(8.dp), medium = RoundedCornerShape(10.dp),
            large = RoundedCornerShape(12.dp), extraLarge = RoundedCornerShape(16.dp)),
        content = content)
}
