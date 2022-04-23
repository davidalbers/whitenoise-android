package dalbers.com.noise.settings.view

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.compose.settings.storage.base.SettingValueState
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.storage.base.rememberIntSettingState
import com.alorma.compose.settings.storage.preferences.IntPreferenceSettingValueState
import com.alorma.compose.settings.storage.preferences.rememberPreferenceBooleanSettingState
import com.alorma.compose.settings.storage.preferences.rememberPreferenceIntSettingState
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsList
import dalbers.com.noise.R
import dalbers.com.noise.shared.PREF_WAVE_INTERVAL_KEY
import dalbers.com.noise.shared.PREF_PLAY_OVER

@Composable
fun SettingsScreen(
    darkThemeState: SettingValueState<Int>,
    onBackPressed: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar { onBackPressed() } },
    ) {
        AllSettings(
            darkThemeState,
            rememberPreferenceBooleanSettingState(
                key = PREF_PLAY_OVER,
                defaultValue = false
            ),
            rememberPreferenceIntSettingState(
                key = PREF_WAVE_INTERVAL_KEY,
                defaultValue = 0
            ),
        )
    }
}

@Composable
private fun TopAppBar(
    onBackPressed: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(stringResource(id = R.string.app_name))
        },
        navigationIcon = {
            IconButton({
                onBackPressed()
            }) {
                Icon(Icons.Default.ArrowBack, "")
            }
        }
    )
}

@Preview
@Composable
private fun SettingScreenPreview() {
    AllSettings(
        darkThemeState = rememberIntSettingState(),
        playOverState = rememberBooleanSettingState(true),
        waveState = rememberIntSettingState(),
    )
}

@Composable
private fun AllSettings(
    darkThemeState: SettingValueState<Int>,
    playOverState: SettingValueState<Boolean>,
    waveState: SettingValueState<Int>,
) {
    Column {
        SettingsList(
            state = darkThemeState,
            title = { Text(text = stringResource(id = R.string.dark_theme_toggle)) },
            subtitle = { Text(text = stringResource(id = R.string.dark_theme_summary)) },
            items = stringArrayResource(id = R.array.theme_choices).toList(),
        )
        SettingsCheckbox(
            state = playOverState,
            title = { Text(text = stringResource(id = R.string.play_over_toggle)) },
            subtitle = { Text(text = stringResource(id = R.string.play_over_summary)) },
        )
        SettingsList(
            state = waveState,
            title = { Text(text = stringResource(id = R.string.wave_interval_choice_title)) },
            subtitle = { Text(text = stringResource(id = R.string.oscillate_interval_summary)) },
            items = stringArrayResource(id = R.array.wave_interval_choices).toList(),
        )
    }
}

@Composable
fun IntPreferenceSettingValueState.isDarkMode(): Boolean {
    return when (this.value) {
        0 -> isSystemInDarkTheme()
        1 -> false
        2 -> true
        else -> error("Unexpected value for Dark Mode setting")
    }
}