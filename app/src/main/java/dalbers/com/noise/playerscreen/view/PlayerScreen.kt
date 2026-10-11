@file:JvmName("PlayerKt")

package dalbers.com.noise.playerscreen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dalbers.com.noise.R
import dalbers.com.noise.playerscreen.model.PlayerScreenState
import dalbers.com.noise.playerscreen.viewmodel.PlayerScreenViewModel
import dalbers.com.noise.settings.view.SettingsSheetContent
import dalbers.com.noise.shared.LocalIsLightTheme
import dalbers.com.noise.shared.card_background_dark
import dalbers.com.noise.shared.card_background_light

@Composable
fun PlayerScreen(
    viewModel: PlayerScreenViewModel,
    version: String,
    darkThemeState: MutableState<Int>,
    onOpenProjectPage: () -> Unit,
) {
    val state = viewModel.playerScreenState.observeAsState(initial = PlayerScreenState.default)
    var showSettings by remember { mutableStateOf(false) }

    ModalBottomSheetLayout(
        sheetContent = {
            SettingsSheetContent(
                version = version,
                darkThemeState = darkThemeState,
                onOpenProjectPage = onOpenProjectPage,
            )
        },
        showSheet = showSettings,
        onSheetDismissed = { showSettings = false },
    ) {
        ModalBottomSheetLayout(
            sheetContent = {
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
            },
            showSheet = state.value.showTimerPicker,
            onSheetDismissed = { viewModel.cancelTimer() },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NoiseGradient(noiseType = state.value.noiseType)
                Column(
                    modifier =
                        Modifier
                            .statusBarsPadding()
                            .padding(top = 56.dp, bottom = 160.dp),
                ) {
                    Player(
                        state = state.value,
                        noiseTypeChanged = { viewModel.changeNoiseType(it) },
                        fadeChanged = { viewModel.toggleFade(it) },
                        wavesChanged = { viewModel.toggleWaves(it) },
                        volumeChanged = { viewModel.changeVolume(it) },
                        onPresetSelected = { viewModel.selectTimerPreset(it) },
                        onCustomTimerTapped = { viewModel.openCustomTimer() },
                    )
                }
                PlayButtonWithTimer(
                    millisLeft = state.value.millisLeft,
                    playing = state.value.playing,
                    onToggle = { viewModel.togglePlay(!state.value.playing) },
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 32.dp),
                )
                val settingsBg = if (LocalIsLightTheme.current) card_background_light else card_background_dark
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(8.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(settingsBg)
                            .clickable { showSettings = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(id = R.string.settings_menu_title),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

/**
 * Wrapper around default ModalBottomSheet to avoid having to use SheetState directly.
 * This allows passing in a Boolean to show/hide the sheet and exposes a lambda for handling dismiss.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheetLayout(
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    showSheet: Boolean = false,
    onSheetDismissed: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    content()

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onSheetDismissed,
            modifier = modifier,
            content = sheetContent,
        )
    }
}
