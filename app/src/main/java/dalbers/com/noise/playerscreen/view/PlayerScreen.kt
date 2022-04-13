@file:JvmName("PlayerKt")

package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dalbers.com.noise.R
import dalbers.com.noise.playerscreen.model.PlayerScreenState
import dalbers.com.noise.playerscreen.viewmodel.PlayerScreenViewModel

@Composable
fun PlayerScreen(
    viewModel: PlayerScreenViewModel,
    onSettingsClicked: () -> Unit,
) {
    val state = viewModel.playerScreenState.observeAsState(initial = PlayerScreenState.default)
    Scaffold(
        topBar = {
            TopAppBar(
                state.value,
                { viewModel.togglePlay(it) },
                { onSettingsClicked() }
            )
        }
    ) {
        Player(
            state = state.value,
            modifier = Modifier.padding(16.dp),
            noiseTypeChanged = { viewModel.changeNoiseType(it) },
            fadeChanged = { viewModel.toggleFade(it) },
            wavesChanged = { viewModel.toggleWaves(it) },
            volumeChanged = { viewModel.changeVolume(it) },
            onTimeSet = { viewModel.setTimer(it) },
            onTimerToggled = { viewModel.toggleTimer() },
        )
    }
}

@Composable
private fun TopAppBar(
    state: PlayerScreenState,
    onPlayToggled: (Boolean) -> Unit,
    onSettingsClicked: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(stringResource(id = R.string.app_name))
        },
        actions = {
            IconButton({
                onPlayToggled(!state.playing)
            }) {
                val icon = if (state.playing) Icons.Default.Pause else Icons.Default.PlayArrow
                Icon(icon, "")
            }
            IconButton({
                onSettingsClicked()
            }) {
                Icon(Icons.Default.Settings, "")
            }
        }
    )
}