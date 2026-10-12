package dalbers.com.noise.playerscreen.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun PlayButtonWithTimer(
    millisLeft: Long,
    playing: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlayPauseButton(
            playing = playing,
            onToggle = onToggle
        )
        val timerAlpha by animateFloatAsState(
            targetValue = if (millisLeft > 0L) 1f else 0f,
            animationSpec = tween(400),
            label = "timer-alpha",
        )
        TimerCountdown(
            millisLeft = millisLeft,
            modifier = Modifier.graphicsLayer { alpha = timerAlpha },
        )
    }
}