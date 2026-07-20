package dalbers.com.noise.playerscreen.view

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun NoiseStateToggle(
    text: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
) {
    NoiseStateCard(
        modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle1,
        )
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange(it) }
        )
    }
}

@Preview
@Composable
private fun NoiseStateTogglePreview() {
    NoiseStateToggle(text = "Fade", checked = false) {}
}