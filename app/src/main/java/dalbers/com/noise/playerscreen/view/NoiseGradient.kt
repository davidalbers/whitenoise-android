package dalbers.com.noise.playerscreen.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dalbers.com.noise.shared.NoiseType
import dalbers.com.noise.shared.toGradientColor

@Composable
fun NoiseGradient(noiseType: NoiseType, modifier: Modifier = Modifier) {
    val isDark = !MaterialTheme.colors.isLight
    val accentColor by animateColorAsState(
        targetValue = noiseType.toGradientColor(isDark),
        animationSpec = tween(durationMillis = 600),
        label = "noiseGradientColor",
    )

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Canvas(modifier = Modifier.fillMaxWidth().height(340.dp)) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(accentColor, Color.Transparent),
                    center = Offset(size.width / 2f, size.height * 1.25f),
                    radius = size.height,
                )
            )
        }
    }
}
