package dalbers.com.noise.playerscreen.view

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dalbers.com.noise.shared.play_button_dark
import dalbers.com.noise.shared.play_button_light
import java.util.Locale

@Composable
internal fun TimerCountdown(millisLeft: Long, modifier: Modifier = Modifier) {
    val hours = millisLeft / 3_600_000L
    val minutes = (millisLeft % 3_600_000L) / 60_000L
    val seconds = (millisLeft % 60_000L) / 1_000L
    val text = if (hours > 0) String.format(Locale.ENGLISH, "%d:%02d:%02d", hours, minutes, seconds)
               else String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds)
    Text(
        text = text,
        style = MaterialTheme.typography.h6,
        color = if (MaterialTheme.colors.isLight) play_button_dark else play_button_light,
        modifier = modifier,
    )
}