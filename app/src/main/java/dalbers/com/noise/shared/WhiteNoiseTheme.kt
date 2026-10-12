package dalbers.com.noise.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
  primary =  Color(0xFF573c27),
  primaryVariant =  Color(0xFFa98360),
  secondary = Color(0xFFffadc6),
  secondaryVariant = Color(0xFFe34989),
  background = Color(0xFFF1ECE4),
)

private val DarkColors = darkColors(
  primary =  Color(0xFF856C62),
  primaryVariant =  Color(0xFF856C62),
  secondary = Color(0xFFAB788B),
  secondaryVariant = Color(0xFFE8A2B5),
)