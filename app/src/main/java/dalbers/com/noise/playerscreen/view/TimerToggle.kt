package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dalbers.com.noise.R
import dalbers.com.noise.shared.ThemePreviews
import dalbers.com.noise.shared.WhiteNoiseTheme
import dalbers.com.noise.shared.WhiteNoiseTypography

sealed class TimerToggleState(
    open val icon: ImageVector,
) {
    object Disabled : TimerToggleState(icon = Icons.Default.Add)

    data class Saved(
        val displayedTime: String,
    ) : TimerToggleState(icon = Icons.Default.Delete)
}

@Composable
fun TimerToggle(
    timeState: TimerToggleState,
    onToggle: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(id = R.string.timer_label),
                style = WhiteNoiseTypography.h6,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (timeState is TimerToggleState.Saved) {
                    Text(
                        text = timeState.displayedTime,
                        style = WhiteNoiseTypography.h6,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                Button(onClick = { onToggle() }) {
                    Icon(timeState.icon, "")
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun TimerSetterPreviewDisabled() {
    WhiteNoiseTheme {
        Surface {
            TimerToggle(
                TimerToggleState.Disabled,
                onToggle = {},
            )
        }
    }
}

@ThemePreviews
@Composable
private fun TimerSetterPreviewSaved() {
    WhiteNoiseTheme {
        Surface {
            TimerToggle(
                TimerToggleState.Saved("1:23:45"),
                onToggle = {},
            )
        }
    }
}
