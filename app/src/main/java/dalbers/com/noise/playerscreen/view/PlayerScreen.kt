@file:JvmName("PlayerKt")

package dalbers.com.noise.playerscreen.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.storage.base.SettingValueState
import dalbers.com.noise.R
import dalbers.com.noise.playerscreen.model.PlayerScreenState
import dalbers.com.noise.playerscreen.viewmodel.PlayerScreenViewModel
import dalbers.com.noise.settings.view.SettingsSheetContent
import dalbers.com.noise.shared.card_background_dark
import dalbers.com.noise.shared.card_background_light

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlayerScreen(
    viewModel: PlayerScreenViewModel,
    version: String,
    darkThemeState: SettingValueState<Int>,
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
            Box(modifier = Modifier.fillMaxSize()) {
                NoiseGradient(noiseType = state.value.noiseType)
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 56.dp, bottom = 160.dp),
                ) {
                    Player(
                        state = state.value,
                        modifier = Modifier.padding(16.dp),
                        noiseTypeChanged = { viewModel.changeNoiseType(it) },
                        fadeChanged = { viewModel.toggleFade(it) },
                        wavesChanged = { viewModel.toggleWaves(it) },
                        volumeChanged = { viewModel.changeVolume(it) },
                        onPresetSelected = { viewModel.selectTimerPreset(it) },
                        onCustomTimerTapped = { viewModel.openCustomTimer() },
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PlayPauseButton(
                        playing = state.value.playing,
                        onToggle = { viewModel.togglePlay(!state.value.playing) },
                    )
                    val timerAlpha by animateFloatAsState(
                        targetValue = if (state.value.millisLeft > 0L) 1f else 0f,
                        animationSpec = tween(400),
                        label = "timer-alpha",
                    )
                    TimerCountdown(
                        millisLeft = state.value.millisLeft,
                        modifier = Modifier.graphicsLayer { alpha = timerAlpha },
                    )
                }
                val settingsBg = if (MaterialTheme.colors.isLight) card_background_light else card_background_dark
                Box(
                    modifier = Modifier
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
                        tint = MaterialTheme.colors.onSurface,
                    )
                }
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
