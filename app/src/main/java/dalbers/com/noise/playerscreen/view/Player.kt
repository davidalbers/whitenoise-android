// kept getting a compiler error "Duplicate JVM class name"
// this is the only thing that would fix it
@file:JvmName("PlayerUnique")

package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dalbers.com.noise.R
import dalbers.com.noise.playerscreen.model.PlayerScreenState
import dalbers.com.noise.shared.NoiseType

@Composable
fun Player(
    state: PlayerScreenState,
    modifier: Modifier = Modifier,
    noiseTypeChanged: (NoiseType) -> Unit,
    fadeChanged: (Boolean) -> Unit,
    wavesChanged: (Boolean) -> Unit,
    volumeChanged: (Float) -> Unit,
    onTimerToggled: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        NoiseSelector(state = state.noiseType) {
            noiseTypeChanged(it)
        }
        NoiseStateToggle(
            text = stringResource(id = R.string.fade_label),
            checked = state.fadeEnabled,
        ) {
            fadeChanged(it)
        }
        NoiseStateToggle(
            text = stringResource(id = R.string.wave_label),
            checked = state.wavesEnabled,
        ) {
            wavesChanged(it)
        }
        VolumeControl(value = state.volume) {
            volumeChanged(it)
        }
        TimerToggle(
            timeState = state.timerToggleState,
            onToggle = { onTimerToggled() },
        )
    }
}