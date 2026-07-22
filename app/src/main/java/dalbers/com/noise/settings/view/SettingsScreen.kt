package dalbers.com.noise.settings.view

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Waves
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import dalbers.com.noise.shared.ThemePreviews
import dalbers.com.noise.shared.WhiteNoiseTheme

@ThemePreviews
@Composable
private fun SettingScreenPreview() {
    WhiteNoiseTheme {
        Surface {
            AllSettings(
                version = "1.2.3",
                darkThemeState = rememberIntSettingState(),
                playOverState = rememberBooleanSettingState(true),
                waveState = rememberIntSettingState(),
                openProjectPage = {},
            )
        }
    }
}

@Composable
internal fun SettingsSheetContent(
    version: String,
    darkThemeState: SettingValueState<Int>,
    onOpenProjectPage: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
        Text(
            text = stringResource(id = R.string.settings_menu_title),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        AllSettings(
            version = version,
            darkThemeState = darkThemeState,
            playOverState = rememberPreferenceBooleanSettingState(key = PREF_PLAY_OVER, defaultValue = false),
            waveState = rememberPreferenceIntSettingState(key = PREF_WAVE_INTERVAL_KEY, defaultValue = 0),
            openProjectPage = onOpenProjectPage,
        )
    }
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
            icon = { Icon(Icons.Default.DarkMode, contentDescription = "") },
        )
        SettingsCheckbox(
            state = playOverState,
            title = { Text(text = stringResource(id = R.string.play_over_toggle)) },
            subtitle = { Text(text = stringResource(id = R.string.play_over_summary)) },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "") },
        )
        SettingsList(
            state = waveState,
            title = { Text(text = stringResource(id = R.string.wave_interval_choice_title)) },
            subtitle = { Text(text = stringResource(id = R.string.oscillate_interval_summary)) },
            items = stringArrayResource(id = R.array.wave_interval_choices).toList(),
            icon = { Icon(Icons.Default.Waves, contentDescription = "") },
        )
        SettingsMenuLink(
            title = { Text(text = "Version $version") },
            subtitle = { Text(text = "This project is open source! Tap to view it on GitHub.") },
            icon = { Icon(Icons.Default.Info, contentDescription = "") },
            onClick = { openProjectPage() },
        )
    }
}

@Composable
fun IntPreferenceSettingValueState.isDarkMode(): Boolean =
    when (this.value) {
        DarkModeSetting.AUTO.key -> isSystemInDarkTheme()
        DarkModeSetting.LIGHT.key -> false
        DarkModeSetting.DARK.key -> true
        else -> error("Unexpected value for Dark Mode setting")
    }
