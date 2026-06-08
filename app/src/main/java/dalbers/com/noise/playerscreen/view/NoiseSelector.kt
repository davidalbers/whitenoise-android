package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dalbers.com.noise.shared.*

@Composable
fun NoiseSelector(
    state: NoiseType,
    onChange: (NoiseType) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    ) {
        defaultNoiseTypes.forEach { noiseType ->
            NoiseOrb(
                noiseType = noiseType,
                isSelected = state == noiseType,
                onClick = { onChange(noiseType) },
            )
        }
    }
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
    val ring = noiseType.ringColor()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Canvas(modifier = Modifier.size(containerDp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val orbRadius = orbDp.toPx() / 2f

            val gradient = Brush.radialGradient(
                colors = listOf(colors.light, colors.base),
                center = Offset(
                    cx + (0.38f - 0.5f) * orbDp.toPx(),
                    cy + (0.32f - 0.5f) * orbDp.toPx(),
                ),
                radius = orbRadius,
            )
            drawCircle(brush = gradient, radius = orbRadius)
            if (isSelected) {
                drawCircle(
                    color = ring,
                    radius = orbRadius + 4.dp.toPx(),
                    style = Stroke(width = 2.5.dp.toPx()),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(id = noiseType.label),
            style = MaterialTheme.typography.body1.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            ),
        )
    }
}

private data class OrbColors(val base: Color, val light: Color)

@Composable
private fun NoiseType.orbColors(): OrbColors {
    val dark = isSystemInDarkTheme()
    return when (this) {
        NoiseType.WHITE -> OrbColors(orb_white_base, orb_white_highlight)
        NoiseType.PINK -> if (dark)
            OrbColors(orb_pink_base_dark, orb_pink_highlight_dark)
        else
            OrbColors(orb_pink_base_light, orb_pink_highlight_light)
        NoiseType.BROWN -> if (dark)
            OrbColors(orb_brown_base_dark, orb_brown_highlight_dark)
        else
            OrbColors(orb_brown_base_light, orb_brown_highlight_light)
        NoiseType.NONE -> OrbColors(Color.Transparent, Color.Transparent)
    }
}

@Composable
private fun NoiseType.ringColor(): Color = when (this) {
    NoiseType.WHITE -> orb_white_ring
    NoiseType.PINK -> if (isSystemInDarkTheme()) orb_pink_base_dark else orb_pink_base_light
    NoiseType.BROWN -> if (isSystemInDarkTheme()) orb_brown_base_dark else orb_brown_base_light
    NoiseType.NONE -> Color.Transparent
}

@Preview(showBackground = true)
@Composable
private fun NoiseSelectorPreview() {
    WhiteNoiseTheme {
        NoiseSelector(state = NoiseType.PINK) {}
    }
}
