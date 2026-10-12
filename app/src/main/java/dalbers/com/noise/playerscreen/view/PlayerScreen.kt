@file:JvmName("PlayerKt")

package dalbers.com.noise.playerscreen.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dalbers.com.noise.R
import dalbers.com.noise.playerscreen.model.PlayerScreenState
import dalbers.com.noise.playerscreen.viewmodel.PlayerScreenViewModel

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PlayerScreen(
    viewModel: PlayerScreenViewModel,
    onSettingsClicked: () -> Unit,
) {
    val state = viewModel.playerScreenState.observeAsState(initial = PlayerScreenState.default)

    ModalBottomSheetLayout(
        sheetContent = {
            TimerPicker(
                pickerState = state.value.timerPickerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                onChange = { viewModel.updateTimer(it) },
                onSet = { viewModel.setTimer() },
                onCancel = { viewModel.cancelTimer() },
            )
        },
        showSheet = state.value.showTimerPicker,
        onSheetDismissed = { viewModel.cancelTimer() }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    state.value,
                    { viewModel.togglePlay(it) },
                    { onSettingsClicked() }
                )
            },
        ) {
            Column {
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
    }
}

/**
 * Wrapper around default ModalBottomSheetLayout to avoid having to use ModalBottomSheetState directly.
 * This allows passing in a Boolean to show/hide the sheet and exposes a lambda for handling dismiss.
 */
@Composable
@ExperimentalMaterialApi
fun ModalBottomSheetLayout(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    showSheet: Boolean = false,
    onSheetDismissed: () -> Unit = {},
    sheetShape: Shape = MaterialTheme.shapes.large,
    sheetElevation: Dp = ModalBottomSheetDefaults.Elevation,
    sheetBackgroundColor: Color = MaterialTheme.colors.surface,
    sheetContentColor: Color = contentColorFor(sheetBackgroundColor),
    scrimColor: Color = ModalBottomSheetDefaults.scrimColor,
    content: @Composable () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    LaunchedEffect(modalBottomSheetState.currentValue) {
        if (!modalBottomSheetState.isVisible && showSheet) {
            onSheetDismissed()
        }
    }

    LaunchedEffect(showSheet) {
        if (showSheet) {
            modalBottomSheetState.show()
        } else {
            modalBottomSheetState.hide()
        }
    }

    ModalBottomSheetLayout(
        sheetContent = sheetContent,
        modifier = modifier,
        sheetState = modalBottomSheetState,
        sheetShape = sheetShape,
        sheetElevation = sheetElevation,
        sheetBackgroundColor = sheetBackgroundColor,
        sheetContentColor = sheetContentColor,
        scrimColor = scrimColor,
        content = content
    )
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