package dalbers.com.noise.settings.view

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsMenuLink
import dalbers.com.noise.R
import dalbers.com.noise.shared.DarkModeSetting
import dalbers.com.noise.shared.PREF_PLAY_OVER
import dalbers.com.noise.shared.PREF_WAVE_INTERVAL_KEY
import dalbers.com.noise.shared.ThemePreviews
import dalbers.com.noise.shared.WhiteNoiseTheme
import dalbers.com.noise.shared.rememberPreferenceBooleanState
import dalbers.com.noise.shared.rememberPreferenceIntState

@ThemePreviews
@Composable
private fun SettingScreenPreview() {
    WhiteNoiseTheme {
        Surface {
            AllSettings(
                version = "1.2.3",
                darkThemeState = remember { mutableStateOf(DarkModeSetting.AUTO.key) },
                playOverState = remember { mutableStateOf(true) },
                waveState = remember { mutableStateOf(0) },
                openProjectPage = {},
            )
        }
    }
}

@Composable
internal fun SettingsSheetContent(
    version: String,
    darkThemeState: MutableState<Int>,
    onOpenProjectPage: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
        Text(
            text = stringResource(id = R.string.settings_menu_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        AllSettings(
            version = version,
            darkThemeState = darkThemeState,
            playOverState = rememberPreferenceBooleanState(key = PREF_PLAY_OVER, defaultValue = false),
            waveState = rememberPreferenceIntState(key = PREF_WAVE_INTERVAL_KEY, defaultValue = 0),
            openProjectPage = onOpenProjectPage,
        )
    }
}

@Composable
private fun AllSettings(
    version: String,
    darkThemeState: MutableState<Int>,
    playOverState: MutableState<Boolean>,
    waveState: MutableState<Int>,
    openProjectPage: () -> Unit,
) {
    Column {
        SingleChoiceSettingsMenuLink(
            state = darkThemeState,
            title = { Text(text = stringResource(id = R.string.dark_theme_toggle)) },
            subtitle = { Text(text = stringResource(id = R.string.dark_theme_summary)) },
            items = stringArrayResource(id = R.array.theme_choices).toList(),
            icon = { Icon(Icons.Default.DarkMode, contentDescription = "") },
        )
        SettingsCheckbox(
            state = playOverState.value,
            onCheckedChange = { playOverState.value = it },
            title = { Text(text = stringResource(id = R.string.play_over_toggle)) },
            subtitle = { Text(text = stringResource(id = R.string.play_over_summary)) },
            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "") },
        )
        SingleChoiceSettingsMenuLink(
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
private fun SingleChoiceSettingsMenuLink(
    state: MutableState<Int>,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    items: List<String>,
    icon: @Composable () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    SettingsMenuLink(
        title = title,
        subtitle = { Text(text = items.getOrElse(state.value) { "" }) },
        icon = icon,
        onClick = { showDialog = true },
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = title,
            text = {
                Column {
                    items.forEachIndexed { index, label ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = index == state.value,
                                        onClick = {
                                            state.value = index
                                            showDialog = false
                                        },
                                    ),
                        ) {
                            RadioButton(selected = index == state.value, onClick = null)
                            Text(text = label, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(id = R.string.time_cancel))
                }
            },
        )
    }
}

@Composable
fun Int.isDarkMode(): Boolean =
    when (this) {
        DarkModeSetting.AUTO.key -> isSystemInDarkTheme()
        DarkModeSetting.LIGHT.key -> false
        DarkModeSetting.DARK.key -> true
        else -> error("Unexpected value for Dark Mode setting")
    }
