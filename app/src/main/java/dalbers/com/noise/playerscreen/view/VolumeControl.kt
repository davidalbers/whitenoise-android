package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dalbers.com.noise.R
import dalbers.com.noise.shared.ThemePreviews
import dalbers.com.noise.shared.WhiteNoiseTheme

@Composable
fun VolumeControl(
    value: Float,
    modifier: Modifier = Modifier,
    onValueChange: (Float) -> Unit,
) {
    NoiseStateCard(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(id = R.string.volume_label),
            modifier = Modifier.padding(end = 8.dp),
            style = MaterialTheme.typography.subtitle1,
        )
        Slider(
            value = value,
            onValueChange = { onValueChange(it) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@ThemePreviews
@Composable
private fun VolumeControlPreview() {
    WhiteNoiseTheme {
        Surface {
            VolumeControl(value = 0.5f) {}
        }
    }
}
