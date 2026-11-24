package dalbers.com.noise.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val WhiteNoiseTypography = Typography()

@Composable
fun WhiteNoiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = WhiteNoiseTypography,
        content = content,
    )
}

private val LightColors =
    lightColorScheme(
        primary = Color(0xFF573c27),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFa98360),
        onPrimaryContainer = Color.White,
        secondary = Color(0xFFe34989),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFffadc6),
        onSecondaryContainer = Color(0xFF3E0021),
        background = Color(0xFFF1ECE4),
        surface = Color(0xFFF1ECE4),
        surfaceVariant = Color(0xFFE5DDD3),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFEDE7DE),
        surfaceContainer = Color(0xFFE8E1D7),
        surfaceContainerHigh = Color(0xFFE3DCD1),
        surfaceContainerHighest = Color(0xFFDED6CA),
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFF856C62),
        onPrimary = Color.White,
        primaryContainer = Color(0xFF856C62),
        onPrimaryContainer = Color.White,
        secondary = Color(0xFFE8A2B5),
        onSecondary = Color(0xFF3E0021),
        secondaryContainer = Color(0xFFAB788B),
        onSecondaryContainer = Color.White,
        background = Color(0xFF121212),
        surface = Color(0xFF121212),
        surfaceVariant = Color(0xFF3A3532),
        surfaceContainerLowest = Color(0xFF0D0D0D),
        surfaceContainerLow = Color(0xFF1A1A1A),
        surfaceContainer = Color(0xFF1E1E1E),
        surfaceContainerHigh = Color(0xFF252525),
        surfaceContainerHighest = Color(0xFF2E2E2E),
    )
