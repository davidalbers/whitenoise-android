@file:Suppress("ktlint:standard:no-wildcard-imports")

package dalbers.com.noise.playerscreen.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dalbers.com.noise.shared.*

@Composable
fun NoiseSelector(
    state: NoiseType,
    onChange: (NoiseType) -> Unit,
) {
    val scrollState = rememberScrollState()
    FadingEdgeBox(scrollState) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            defaultNoiseTypes.forEachIndexed { index, noiseType ->
                NoiseOrb(
                    noiseType = noiseType,
                    isSelected = state == noiseType,
                    onClick = { onChange(noiseType) },
                )
                if (index < defaultNoiseTypes.lastIndex) {
                    Spacer(Modifier.size(8.dp))
                }
            }
        }
    }
}

@Composable
private fun FadingEdgeBox(
    scrollState: ScrollState,
    content: @Composable BoxScope.() -> Unit,
) {
    val scrollThreshold = (scrollState.maxValue * 0.1).toInt()
    val atStart = scrollState.value <= scrollThreshold
    val atEnd = scrollState.value >= scrollState.maxValue - scrollThreshold
    val leftAlpha by animateFloatAsState(
        targetValue = if (atStart) 0f else 1f,
        animationSpec = tween(200),
    )
    val rightAlpha by animateFloatAsState(
        targetValue = if (atEnd) 0f else 1f,
        animationSpec = tween(200),
    )
    val background = MaterialTheme.colors.surface

    Box(
        modifier =
            Modifier.drawWithContent {
                drawContent()
                val fadeWidth = 64.dp.toPx()
                drawRect(
                    brush =
                        Brush.horizontalGradient(
                            colors = listOf(background, Color.Transparent),
                            endX = fadeWidth,
                        ),
                    size = Size(fadeWidth, size.height),
                    alpha = leftAlpha,
                )
                drawRect(
                    brush =
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, background),
                            startX = size.width - fadeWidth,
                        ),
                    topLeft = Offset(size.width - fadeWidth, 0f),
                    size = Size(fadeWidth, size.height),
                    alpha = rightAlpha,
                )
            },
        content = content,
    )
}

@Composable
private fun NoiseOrb(
    noiseType: NoiseType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val orbDp = 80.dp
    val containerDp = 92.dp
    val colors = noiseType.orbColors()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Canvas(modifier = Modifier.size(containerDp)) {
            val orbRadius = orbDp.toPx() / 2f

            val gradient =
                Brush.radialGradient(
                    colors = listOf(colors.light, colors.base),
                    center =
                        Offset(
                            size.width / 3f,
                            size.height / 3f,
                        ),
                    radius = orbRadius,
                )
            drawCircle(brush = gradient, radius = orbRadius)
            if (isSelected) {
                drawCircle(
                    color = colors.base,
                    radius = orbRadius + 4.dp.toPx(),
                    style = Stroke(width = 2.5.dp.toPx()),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(id = noiseType.label),
            style =
                MaterialTheme.typography.caption.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                ),
        )
    }
}

private data class OrbColors(
    val base: Color,
    val light: Color,
)

@Composable
private fun NoiseType.orbColors(): OrbColors {
    val dark = isSystemInDarkTheme()
    return when (this) {
        NoiseType.WHITE -> OrbColors(orb_white_base, orb_white_highlight)
        NoiseType.PINK ->
            if (dark) {
                OrbColors(orb_pink_base_dark, orb_pink_highlight_dark)
            } else {
                OrbColors(orb_pink_base_light, orb_pink_highlight_light)
            }
        NoiseType.BROWN ->
            if (dark) {
                OrbColors(orb_brown_base_dark, orb_brown_highlight_dark)
            } else {
                OrbColors(orb_brown_base_light, orb_brown_highlight_light)
            }
        NoiseType.NATURE ->
            if (dark) {
                OrbColors(orb_nature_base_dark, orb_nature_highlight_dark)
            } else {
                OrbColors(orb_nature_base_light, orb_nature_highlight_light)
            }
        NoiseType.FIRE ->
            if (dark) {
                OrbColors(orb_fire_base_dark, orb_fire_highlight_dark)
            } else {
                OrbColors(orb_fire_base_light, orb_fire_highlight_light)
            }
        NoiseType.RAIN ->
            if (dark) {
                OrbColors(orb_rain_base_dark, orb_rain_highlight_dark)
            } else {
                OrbColors(orb_rain_base_light, orb_rain_highlight_light)
            }
        NoiseType.NONE -> OrbColors(Color.Transparent, Color.Transparent)
    }
}

@ThemePreviews
@Composable
private fun NoiseSelectorPreview() {
    WhiteNoiseTheme {
        Surface {
            NoiseSelector(state = NoiseType.PINK) {}
        }
    }
}
