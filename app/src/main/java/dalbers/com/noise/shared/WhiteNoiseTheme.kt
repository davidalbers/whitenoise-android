package dalbers.com.noise.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val WhiteNoiseTypography = Typography()

val LocalIsLightTheme = staticCompositionLocalOf { true }

@Composable
fun WhiteNoiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalIsLightTheme provides !darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = WhiteNoiseTypography,
            content = content,
        )
    }
}

private val LightColors =
    lightColorScheme(
        background = md_theme_light_background,
        primary = accent_light_primary,
        secondary = accent_light_secondary,
    )

private val DarkColors =
    darkColorScheme(
        primary = accent_dark_primary,
        secondary = accent_dark_secondary,
    )
