package dalbers.com.noise.playerscreen.view

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dalbers.com.noise.shared.ThemePreviews
import dalbers.com.noise.shared.WhiteNoiseTheme

@Composable
fun NoiseStateToggle(
    text: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
) {
    NoiseStateCard(
        modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle1,
        )
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) },
        )
    }
}

@ThemePreviews
@Composable
private fun NoiseStateTogglePreview() {
    WhiteNoiseTheme {
        Surface {
            NoiseStateToggle(text = "Fade", checked = false) {}
        }
    }
}
