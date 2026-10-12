package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dalbers.com.noise.R
import dalbers.com.noise.shared.WhiteNoiseTypography

@Composable
fun VolumeControl(
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.volume_label),
            modifier = Modifier.padding(end = 8.dp),
            style = WhiteNoiseTypography.h6,
        )
        Slider(
            value = value,
            onValueChange = { onValueChange(it) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun VolumeControlPreview() {
    VolumeControl(value = 0.5f) {}
}