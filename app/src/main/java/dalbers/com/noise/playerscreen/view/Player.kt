// kept getting a compiler error "Duplicate JVM class name"
// this is the only thing that would fix it
@file:JvmName("PlayerUnique")

package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dalbers.com.noise.R
import dalbers.com.noise.playerscreen.model.PlayerScreenState
import dalbers.com.noise.playerscreen.model.TimerPreset
import dalbers.com.noise.shared.NoiseType
import dalbers.com.noise.shared.ThemePreviews
import dalbers.com.noise.shared.WhiteNoiseTheme

@Composable
fun Player(
    state: PlayerScreenState,
    modifier: Modifier = Modifier,
    noiseTypeChanged: (NoiseType) -> Unit,
    fadeChanged: (Boolean) -> Unit,
    wavesChanged: (Boolean) -> Unit,
    volumeChanged: (Float) -> Unit,
    onPresetSelected: (TimerPreset?) -> Unit,
    onCustomTimerTapped: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        NoiseSelector(state = state.noiseType) {
            noiseTypeChanged(it)
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoiseStateToggle(
            text = stringResource(id = R.string.fade_label),
            checked = state.fadeEnabled,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            fadeChanged(it)
        }
        Spacer(modifier = Modifier.height(8.dp))
        NoiseStateToggle(
            text = stringResource(id = R.string.wave_label),
            checked = state.wavesEnabled,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            wavesChanged(it)
        }
        Spacer(modifier = Modifier.height(8.dp))
        VolumeControl(
            value = state.volume,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            volumeChanged(it)
        }
        Spacer(modifier = Modifier.height(8.dp))
        TimerSectionView(
            selectedPreset = state.selectedTimerPreset,
            customTimerMillis = state.customTimerMillis,
            onSelectPreset = { onPresetSelected(it) },
            onCustomTapped = { onCustomTimerTapped() },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        )
    }
}

@ThemePreviews
@Composable
private fun PlayerPreview() {
    WhiteNoiseTheme {
        Surface {
            Player(
                state = PlayerScreenState.default,
                noiseTypeChanged = {},
                fadeChanged = {},
                wavesChanged = {},
                volumeChanged = {},
                onPresetSelected = {},
                onCustomTimerTapped = {},
            )
        }
    }
}
