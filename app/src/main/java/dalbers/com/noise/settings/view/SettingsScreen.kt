package dalbers.com.noise.settings.view

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Waves
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
import com.alorma.compose.settings.ui.SettingsMenuLink
import dalbers.com.noise.R
import dalbers.com.noise.shared.DarkModeSetting
import dalbers.com.noise.shared.PREF_PLAY_OVER
import dalbers.com.noise.shared.PREF_WAVE_INTERVAL_KEY


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    version: String,
    darkThemeState: SettingValueState<Int>,
    onBackPressed: () -> Unit,
    onOpenProjectPage: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar { onBackPressed() } },
    ) {
        AllSettings(
            version = version,
            darkThemeState = darkThemeState,
            playOverState = rememberPreferenceBooleanSettingState(
                key = PREF_PLAY_OVER,
                defaultValue = false
            ),
            waveState = rememberPreferenceIntSettingState(
                key = PREF_WAVE_INTERVAL_KEY,
                defaultValue = 0
            ),
            openProjectPage = onOpenProjectPage
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
                Icon(Icons.AutoMirrored.Default.ArrowBack, "")
            }
        }
    )
}

@Preview
@Composable
private fun SettingScreenPreview() {
    AllSettings(
        version = "1.2.3",
        darkThemeState = rememberIntSettingState(),
        playOverState = rememberBooleanSettingState(true),
        waveState = rememberIntSettingState(),
        openProjectPage = {},
    )
}

@Composable
private fun AllSettings(
    version: String,
    darkThemeState: SettingValueState<Int>,
    playOverState: SettingValueState<Boolean>,
    waveState: SettingValueState<Int>,
    openProjectPage: () -> Unit,
) {
    Column {
        SettingsList(
            state = darkThemeState,
            title = { Text(text = stringResource(id = R.string.dark_theme_toggle)) },
            subtitle = { Text(text = stringResource(id = R.string.dark_theme_summary)) },
            items = stringArrayResource(id = R.array.theme_choices).toList(),
            icon = { Icon(Icons.Default.DarkMode, contentDescription = "")},
        )
        SettingsCheckbox(
            state = playOverState,
            title = { Text(text = stringResource(id = R.string.play_over_toggle)) },
            subtitle = { Text(text = stringResource(id = R.string.play_over_summary)) },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "")},
        )
        SettingsList(
            state = waveState,
            title = { Text(text = stringResource(id = R.string.wave_interval_choice_title)) },
            subtitle = { Text(text = stringResource(id = R.string.oscillate_interval_summary)) },
            items = stringArrayResource(id = R.array.wave_interval_choices).toList(),
            icon = { Icon(Icons.Default.Waves, contentDescription = "")},
        )
        SettingsMenuLink(
            title = { Text(text = "Version $version") },
            subtitle = { Text(text = "This project is open source! Tap to view it on GitHub.") },
            icon = { Icon(Icons.Default.Info, contentDescription = "")},
            onClick = { openProjectPage() },
        )
    }
}

@Composable
fun IntPreferenceSettingValueState.isDarkMode(): Boolean {
    return when (this.value) {
        DarkModeSetting.AUTO.key -> isSystemInDarkTheme()
        DarkModeSetting.LIGHT.key -> false
        DarkModeSetting.DARK.key -> true
        else -> error("Unexpected value for Dark Mode setting")
    }
}