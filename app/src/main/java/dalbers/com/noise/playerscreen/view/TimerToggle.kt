package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dalbers.com.noise.R
import dalbers.com.noise.shared.WhiteNoiseTypography

sealed class TimerToggleState(
    open val icon: ImageVector
) {
    object Disabled: TimerToggleState(icon = Icons.Default.Add)

    data class Saved(
        val displayedTime: String,
    ): TimerToggleState(icon = Icons.Default.Delete)
}

@Composable
fun TimerToggle(
    timeState: TimerToggleState,
    onToggle: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
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
                        text =  timeState.displayedTime,
                        style = WhiteNoiseTypography.h6,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Button(onClick = { onToggle() }) {
                    Icon(timeState.icon, "")
                }
            }
        }
    }
}


@Composable
@Preview
private fun TimerSetterPreview_Disabled() {
    TimerToggle(
        TimerToggleState.Disabled,
        onToggle = {},
    )
}

@Composable
@Preview
private fun TimerSetterPreview_Saved() {
    TimerToggle(
        TimerToggleState.Saved("1:23:45"),
        onToggle = {},
    )
}

