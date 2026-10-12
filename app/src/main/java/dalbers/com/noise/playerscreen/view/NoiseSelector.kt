package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dalbers.com.noise.shared.NoiseType
import dalbers.com.noise.shared.ThemePreviews
import dalbers.com.noise.shared.WhiteNoiseTheme
import dalbers.com.noise.shared.WhiteNoiseTypography
import dalbers.com.noise.shared.defaultNoiseTypes

@Composable
fun NoiseSelector(
    state: NoiseType,
    onChange: (NoiseType) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        defaultNoiseTypes.forEach {
            NoiseRadioButton(
                selected = state == it,
                text = stringResource(id = it.label),
            ) {
                onChange(it)
            }
        }
    }
}

@ThemePreviews
@Composable
private fun NoiseSelectorPreview() {
    WhiteNoiseTheme {
        Surface {
            NoiseSelector(state = NoiseType.WHITE) {}
        }
    }
}

@Composable
private fun NoiseRadioButton(
    selected: Boolean,
    text: String,
    onSelectionChanged: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = WhiteNoiseTypography.h6,
        )
        RadioButton(
            selected = selected,
            onClick = { onSelectionChanged() },
        )
    }
}
