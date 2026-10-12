package dalbers.com.noise.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

val WhiteNoiseTypography = Typography()

@Composable
fun WhiteNoiseTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colors = if (darkTheme) DarkColors else LightColors,
    typography = WhiteNoiseTypography,
    content = content,
  )
}

private val LightColors = lightColors(
  background = md_theme_light_background,
  primary = accent_light_primary,
  primaryVariant = accent_light_primary,
  secondary = accent_light_secondary,
  secondaryVariant = accent_light_secondary,
)

private val DarkColors = darkColors(
  primary = accent_dark_primary,
  primaryVariant = accent_dark_primary,
  secondary = accent_dark_secondary,
  secondaryVariant = accent_dark_secondary,
)