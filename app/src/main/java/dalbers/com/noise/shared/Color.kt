package dalbers.com.noise.shared

import androidx.compose.ui.graphics.Color

fun NoiseType.toGradientColor(isDark: Boolean): Color = when (this) {
    NoiseType.WHITE -> orb_white_ring
    NoiseType.PINK -> if (isDark) orb_pink_base_dark else orb_pink_base_light
    NoiseType.BROWN -> if (isDark) orb_brown_base_dark else orb_brown_base_light
    NoiseType.NATURE -> if (isDark) orb_nature_base_dark else orb_nature_base_light
    NoiseType.FIRE -> if (isDark) orb_fire_base_dark else orb_fire_base_light
    NoiseType.RAIN -> if (isDark) orb_rain_base_dark else orb_rain_base_light
    NoiseType.NONE -> Color.Transparent
}

// ── Light theme ──────────────────────────────────────────────────────────────
val md_theme_light_primary = Color(0xFF573C27)
val md_theme_light_primaryVariant = Color(0xFFA98360)
val md_theme_light_secondary = Color(0xFFFFADC6)
val md_theme_light_secondaryVariant = Color(0xFFE34989)
val md_theme_light_background = Color(0xFFF1ECE4)

// ── Dark theme ────────────────────────────────────────────────────────────────
val md_theme_dark_primary = Color(0xFF856C62)
val md_theme_dark_primaryVariant = Color(0xFF856C62)
val md_theme_dark_secondary = Color(0xFFAB788B)
val md_theme_dark_secondaryVariant = Color(0xFFE8A2B5)

// ── Control cards ────────────────────────────────────────────────────────────
val card_background_light = Color(0x33000000) // 20% black
val card_background_dark = Color(0x33FFFFFF)  // 20% white

// ── White noise orb ───────────────────────────────────────────────────────────
val orb_white_base = Color(0xFFE7E7E7)
val orb_white_highlight = Color(0xFFF8F8F8)
val orb_white_ring = Color(0xFFA5A7A7)

// ── Pink noise orb ────────────────────────────────────────────────────────────
val orb_pink_base_light = Color(0xFFFF98BC)
val orb_pink_highlight_light = Color(0xFFFFB8D1)
val orb_pink_base_dark = Color(0xFFFFCFCB)
val orb_pink_highlight_dark = Color(0xFFFFEDEB)

// ── Brown noise orb ───────────────────────────────────────────────────────────
val orb_brown_base_light = Color(0xFF967861)
val orb_brown_highlight_light = Color(0xFFC3A790)
val orb_brown_base_dark = Color(0xFFA1887F)
val orb_brown_highlight_dark = Color(0xFFCFBCB6)

// ── Nature noise orb ──────────────────────────────────────────────────────────
val orb_nature_base_light = Color(0xFF238533)
val orb_nature_highlight_light = Color(0xFF48BD5A)
val orb_nature_base_dark = Color(0xFF2E9E3D)
val orb_nature_highlight_dark = Color(0xFF56D667)

// ── Fire noise orb ────────────────────────────────────────────────────────────
val orb_fire_base_light = Color(0xFFD9590D)
val orb_fire_highlight_light = Color(0xFFFF7726)
val orb_fire_base_dark = Color(0xFFF2731A)
val orb_fire_highlight_dark = Color(0xFFFF8732)

// ── Rain noise orb ────────────────────────────────────────────────────────────
val orb_rain_base_light = Color(0xFF2E73D9)
val orb_rain_highlight_light = Color(0xFF5096FF)
val orb_rain_base_dark = Color(0xFF408CF2)
val orb_rain_highlight_dark = Color(0xFF5DA2FF)
