package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dalbers.com.noise.R
import dalbers.com.noise.shared.WhiteNoiseTypography

data class TimerPickerState(
    val hours: Int,
    val minutesTens: Int,
    val minutes: Int,
) {
    companion object {
        val zero = TimerPickerState(0, 0, 0)
    }
}

@Preview
@Composable
fun TimerPickerPreview() {
    TimerPicker(
        pickerState = TimerPickerState.zero,
        onChange = {},
        onSet = {},
        onCancel = {},
    )
}

@Composable
fun TimerPicker(
    pickerState: TimerPickerState,
    modifier: Modifier = Modifier,
    onChange: (Int) -> Unit,
    onSet: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Max),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeUnitPicker(
                value = pickerState.hours,
                onIncrement = { onChange(60) },
                onDecrement = { onChange(-60) },
            )
            Text(
                text = stringResource(id = R.string.time_divider),
                style = WhiteNoiseTypography.h1,
            )
            TimeUnitPicker(
                value = pickerState.minutesTens,
                onIncrement = { onChange(10) },
                onDecrement = { onChange(-10) },
                modifier = Modifier.padding(end = 4.dp),
            )
            TimeUnitPicker(
                value = pickerState.minutes,
                onIncrement = { onChange(1) },
                onDecrement = { onChange(-1) },
            )
        }

        Button(
            onClick = { onSet() },
            modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
        ) {
            Text(text = stringResource(id = R.string.time_set))
        }
        Button(
            onClick = { onCancel() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Text(text = stringResource(id = R.string.time_cancel))
        }
    }
}

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
            style = WhiteNoiseTypography.h1,
        )
        Button(
            onClick = { onDecrement() },
            modifier = Modifier.width(48.dp),
        ) {
            Icon(Icons.Default.Remove, contentDescription = "")
        }
    }
}

@Preview
@Composable
private fun TimeUnitPickerPreview() {
    TimeUnitPicker(0, {}, {})
}