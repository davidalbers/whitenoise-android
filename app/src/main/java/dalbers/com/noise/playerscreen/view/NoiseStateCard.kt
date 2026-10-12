package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dalbers.com.noise.shared.card_background_dark
import dalbers.com.noise.shared.card_background_light

@Composable
fun NoiseStateCard(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val cardBackground = if (MaterialTheme.colors.isLight) card_background_light else card_background_dark

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardBackground)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        content = content
    )
}