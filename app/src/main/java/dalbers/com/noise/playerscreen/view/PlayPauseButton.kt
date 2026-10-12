package dalbers.com.noise.playerscreen.view

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import dalbers.com.noise.shared.play_button_dark
import dalbers.com.noise.shared.play_button_light

@Composable
internal fun PlayPauseButton(playing: Boolean, onToggle: () -> Unit) {
    val isLight = MaterialTheme.colors.isLight
    val circleColor = if (isLight) play_button_dark else play_button_light
    val iconColor = if (isLight) play_button_light else play_button_dark
    val icon = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (playing) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-scale",
    )

    Box(
        modifier = Modifier
            .size(88.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(circleColor)
            .clickable { onToggle() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (playing) "Pause" else "Play",
            tint = iconColor,
            modifier = Modifier.size(44.dp),
        )
    }
}