package dalbers.com.noise.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.preference.PreferenceManager

@Composable
fun rememberPreferenceIntState(
    key: String,
    defaultValue: Int,
): MutableState<Int> {
    val context = LocalContext.current
    val sharedPreferences = remember(context) { PreferenceManager.getDefaultSharedPreferences(context) }
    val state = remember { mutableStateOf(sharedPreferences.getInt(key, defaultValue)) }
    return remember {
        object : MutableState<Int> by state {
            override var value: Int
                get() = state.value
                set(newValue) {
                    state.value = newValue
                    sharedPreferences.edit().putInt(key, newValue).apply()
                }
        }
    }
}

@Composable
fun rememberPreferenceBooleanState(
    key: String,
    defaultValue: Boolean,
): MutableState<Boolean> {
    val context = LocalContext.current
    val sharedPreferences = remember(context) { PreferenceManager.getDefaultSharedPreferences(context) }
    val state = remember { mutableStateOf(sharedPreferences.getBoolean(key, defaultValue)) }
    return remember {
        object : MutableState<Boolean> by state {
            override var value: Boolean
                get() = state.value
                set(newValue) {
                    state.value = newValue
                    sharedPreferences.edit().putBoolean(key, newValue).apply()
                }
        }
    }
}
