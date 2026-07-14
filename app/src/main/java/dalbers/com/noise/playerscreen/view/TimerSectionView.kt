package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dalbers.com.noise.R
import dalbers.com.noise.playerscreen.model.TimerPreset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimerSectionView(
    selectedPreset: TimerPreset?,
    customTimerMillis: Long,
    onSelectPreset: (TimerPreset?) -> Unit,
    onCustomTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NoiseStateCard {
        Column(modifier = modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(id = R.string.timer_label),
                style = MaterialTheme.typography.subtitle1,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TimerChip(label = "Off", isSelected = selectedPreset == null) {
                    onSelectPreset(null)
                }
                TimerPreset.standard.forEach { preset ->
                    TimerChip(label = preset.label, isSelected = selectedPreset == preset) {
                        onSelectPreset(preset)
                    }
                }
                val customPreset = TimerPreset.from(customTimerMillis)
                if (customPreset != null && customPreset.isCustom) {
                    TimerChip(
                        label = customPreset.label,
                        isSelected = selectedPreset == customPreset
                    ) {
                        onSelectPreset(customPreset)
                    }
                }
                TimerChip(label = "Custom ›", isSelected = false) {
                    onCustomTapped()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TimerChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colors.onSurface
    } else {
        MaterialTheme.colors.onSurface.copy(alpha = 0.05f)
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colors.surface
    } else {
        MaterialTheme.colors.onSurface
    }
    val border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f))

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        contentColor = contentColor,
        border = border,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Preview
@Composable
private fun TimerSectionPreview_NoneSelected() {
    TimerSectionView(
        selectedPreset = null,
        customTimerMillis = 0L,
        onSelectPreset = {},
        onCustomTapped = {},
    )
}

@Preview
@Composable
private fun TimerSectionPreview_PresetSelected() {
    TimerSectionView(
        selectedPreset = TimerPreset(1, 0),
        customTimerMillis = 0L,
        onSelectPreset = {},
        onCustomTapped = {},
    )
}
