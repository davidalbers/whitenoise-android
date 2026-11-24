package dalbers.com.noise.settings.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dalbers.com.noise.R
import dalbers.com.noise.shared.DarkModeSetting
import dalbers.com.noise.shared.PREF_PLAY_OVER
import dalbers.com.noise.shared.PREF_WAVE_INTERVAL_KEY
import dalbers.com.noise.shared.ThemePreviews
import dalbers.com.noise.shared.WhiteNoiseTheme
import dalbers.com.noise.shared.rememberBooleanPreference
import dalbers.com.noise.shared.rememberIntPreference
import kotlinx.coroutines.selects.select

@Composable
fun SettingsScreen(
    version: String,
    darkThemeState: MutableState<Int>,
    onBackPressed: () -> Unit,
    onOpenProjectPage: () -> Unit,
) {
    Scaffold(
        topBar = { SettingsTopAppBar { onBackPressed() } },
    ) { innerPadding ->
        AllSettings(
            version = version,
            darkThemeState = darkThemeState,
            playOverState = rememberBooleanPreference(key = PREF_PLAY_OVER, defaultValue = false),
            waveState = rememberIntPreference(key = PREF_WAVE_INTERVAL_KEY, defaultValue = 0),
            openProjectPage = onOpenProjectPage,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopAppBar(onBackPressed: () -> Unit) {
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
        },
    )
}

@ThemePreviews
@Composable
private fun SettingScreenPreview() {
    WhiteNoiseTheme {
        Surface {
            AllSettings(
                version = "1.2.3",
                darkThemeState = remember { mutableIntStateOf(DarkModeSetting.AUTO.key) },
                playOverState = remember { mutableStateOf(true) },
                waveState = remember { mutableIntStateOf(0) },
                openProjectPage = {},
            )
        }
    }
}

@Composable
private fun AllSettings(
    version: String,
    darkThemeState: MutableState<Int>,
    playOverState: MutableState<Boolean>,
    waveState: MutableState<Int>,
    openProjectPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SettingsList(
            state = darkThemeState,
            title = stringResource(id = R.string.dark_theme_toggle),
            subtitle = stringResource(id = R.string.dark_theme_summary),
            items = stringArrayResource(id = R.array.theme_choices).toList(),
            icon = Icons.Default.DarkMode,
        )
        SettingsCheckbox(
            state = playOverState,
            title = stringResource(id = R.string.play_over_toggle),
            subtitle = stringResource(id = R.string.play_over_summary),
            icon = Icons.Default.PlayArrow,
        )
        SettingsList(
            state = waveState,
            title = stringResource(id = R.string.wave_interval_choice_title),
            subtitle = stringResource(id = R.string.oscillate_interval_summary),
            items = stringArrayResource(id = R.array.wave_interval_choices).toList(),
            icon = Icons.Default.Waves,
        )
        SettingsItem(
            title = "Version $version",
            subtitle = "This project is open source! Tap to view it on GitHub.",
            icon = Icons.Default.Info,
            modifier = Modifier.clickable { openProjectPage() },
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = trailingContent,
        modifier = modifier,
    )
}

@Composable
private fun SettingsCheckbox(
    state: MutableState<Boolean>,
    title: String,
    subtitle: String,
    icon: ImageVector,
) {
    SettingsItem(
        title = title,
        subtitle = subtitle,
        icon = icon,
        modifier =
            Modifier.toggleable(
                value = state.value,
                role = Role.Checkbox,
                onValueChange = { state.value = it },
            ),
        trailingContent = { Checkbox(checked = state.value, onCheckedChange = null) },
    )
}

/**
 * A setting that opens a dialog to pick one of [items]. [state] holds the selected index.
 */
@Composable
private fun SettingsList(
    state: MutableState<Int>,
    title: String,
    subtitle: String,
    items: List<String>,
    icon: ImageVector,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    SettingsItem(
        title = title,
        subtitle = subtitle,
        icon = icon,
        modifier = Modifier.clickable { showDialog = true },
    )

    if (showDialog) {
        SettingsAlertDialog(
            title,
            items,
            selectedIndex = state.value,
            onDismiss = { showDialog = false },
            onSelect = { state.value = it }
        )
    }
}

@Composable
fun SettingsAlertDialog(
    title: String,
    items: List<String>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                items.forEachIndexed { index, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedIndex == index,
                                    role = Role.RadioButton,
                                    onClick = {
                                        onSelect(index)
                                        onDismiss()
                                    },
                                )
                                .padding(vertical = 12.dp),
                    ) {
                        RadioButton(selected = selectedIndex == index, onClick = null)
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.cancel))
            }
        },
    )
}

@Composable
fun isDarkMode(darkModeSetting: Int): Boolean =
    when (darkModeSetting) {
        DarkModeSetting.AUTO.key -> isSystemInDarkTheme()
        DarkModeSetting.LIGHT.key -> false
        DarkModeSetting.DARK.key -> true
        else -> error("Unexpected value for Dark Mode setting")
    }
