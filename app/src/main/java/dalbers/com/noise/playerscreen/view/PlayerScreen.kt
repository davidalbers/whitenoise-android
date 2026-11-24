@file:JvmName("PlayerKt")

package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dalbers.com.noise.R
import dalbers.com.noise.playerscreen.model.PlayerScreenState
import dalbers.com.noise.playerscreen.viewmodel.PlayerScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerScreenViewModel,
    onSettingsClicked: () -> Unit,
) {
    val state = viewModel.playerScreenState.observeAsState(initial = PlayerScreenState.default)

    Scaffold(
        topBar = {
            PlayerTopAppBar(
                state.value,
                { viewModel.togglePlay(it) },
                { onSettingsClicked() },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Player(
                state = state.value,
                modifier = Modifier.padding(16.dp),
                noiseTypeChanged = { viewModel.changeNoiseType(it) },
                fadeChanged = { viewModel.toggleFade(it) },
                wavesChanged = { viewModel.toggleWaves(it) },
                volumeChanged = { viewModel.changeVolume(it) },
                onTimerToggled = {
                    viewModel.toggleTimer()
                },
            )
        }
    }

    if (state.value.showTimerPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { viewModel.cancelTimer() },
            sheetState = sheetState,
        ) {
            TimerPicker(
                pickerState = state.value.timerPickerState,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                onChange = { viewModel.updateTimer(it) },
                onSet = { viewModel.setTimer() },
                onCancel = { viewModel.cancelTimer() },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerTopAppBar(
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
        },
    )
}
