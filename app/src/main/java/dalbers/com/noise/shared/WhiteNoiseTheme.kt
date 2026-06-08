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
  primary = md_theme_light_primary,
  primaryVariant = md_theme_light_primaryVariant,
  secondary = md_theme_light_secondary,
  secondaryVariant = md_theme_light_secondaryVariant,
  background = md_theme_light_background,
)

private val DarkColors = darkColors(
  primary = md_theme_dark_primary,
  primaryVariant = md_theme_dark_primaryVariant,
  secondary = md_theme_dark_secondary,
  secondaryVariant = md_theme_dark_secondaryVariant,
)