package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
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

sealed class TimerState(
    open val icon: ImageVector,
) {
    object Disabled: TimerState(icon = Icons.Default.Add)

    data class Saved(
        val displayedTime: String,
    ): TimerState(icon = Icons.Default.Delete)

    data class Setting(
        val timerTimeState: TimerTimeState,
    ): TimerState(icon = Icons.Default.Done)
}

data class TimerTimeState(
    val hours: Int,
    val minutesTens: Int,
    val minutes: Int,
) {
    companion object {
        val zero = TimerTimeState(0, 0, 0)
    }
}

@Composable
fun TimerSetter(
    timeState: TimerState,
    onChange: (Int) -> Unit,
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
                if (timeState is TimerState.Saved) {
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
        if (timeState is TimerState.Setting) {
            TimerPicker(
                timeState = timeState.timerTimeState,
                modifier = Modifier.padding(top = 24.dp),
            ) { onChange(it) }
        }
    }
}

private val timerTextStyle = WhiteNoiseTypography.h1
@Composable
private fun TimeUnitPicker(
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Button(
            onClick = { onIncrement() },
            modifier = Modifier.width(48.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "")
        }
        Text(
            text = value.toString(),
            style = timerTextStyle,
        )
        Button(
            onClick = { onDecrement() },
            modifier = Modifier.width(48.dp),
        ) {
            Icon(Icons.Default.Remove, contentDescription = "")
        }
    }
}

@Composable
@Preview
private fun TimerSetterPreview() {
    TimerSetter(
        TimerState.Setting(TimerTimeState(
            hours = 1,
            minutesTens = 2,
            minutes = 3,
        )),
        onChange = {},
        onToggle = {},
    )
}

@Composable
@Preview
private fun TimerSetterPreview_Disabled() {
    TimerSetter(
        TimerState.Disabled,
        onChange = {},
        onToggle = {},
    )
}

@Composable
@Preview
private fun TimerSetterPreview_Saved() {
    TimerSetter(
        TimerState.Saved("1:23:45"),
        onChange = {},
        onToggle = {},
    )
}



@Composable
private fun TimerPicker(
    timeState: TimerTimeState,
    modifier: Modifier = Modifier,
    onChange: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        TimeUnitPicker(
            value = timeState.hours,
            onIncrement = { onChange(60) },
            onDecrement = { onChange(-60) },
        )
        Text(
            text = stringResource(id = R.string.time_divider),
            style = timerTextStyle,
        )
        TimeUnitPicker(
            value = timeState.minutesTens,
            onIncrement = { onChange(10) },
            onDecrement = { onChange(-10) },
            modifier = Modifier.padding(end = 4.dp),
        )
        TimeUnitPicker(
            value = timeState.minutes,
            onIncrement = { onChange(1) },
            onDecrement = { onChange(-1) },
        )
    }
}

@Preview
@Composable
private fun TimeUnitPickerPreview() {
    TimeUnitPicker(0, {}, {})
}
