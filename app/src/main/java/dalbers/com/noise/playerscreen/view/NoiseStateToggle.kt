package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dalbers.com.noise.shared.card_background_dark
import dalbers.com.noise.shared.card_background_light

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