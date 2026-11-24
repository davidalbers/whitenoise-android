package dalbers.com.noise.shared

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit

/**
 * The same file `androidx.preference.PreferenceManager.getDefaultSharedPreferences` uses, so settings
 * saved by earlier versions of the app carry over.
 */
fun Context.defaultSharedPreferences(): SharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)

@Composable
fun rememberIntPreference(
    key: String,
    defaultValue: Int,
): MutableState<Int> =
    rememberPreference(
        key = key,
        read = { getInt(key, defaultValue) },
        write = { putInt(key, it) },
    )

@Composable
fun rememberBooleanPreference(
    key: String,
    defaultValue: Boolean,
): MutableState<Boolean> =
    rememberPreference(
        key = key,
        read = { getBoolean(key, defaultValue) },
        write = { putBoolean(key, it) },
    )

@Composable
private fun <T> rememberPreference(
    key: String,
    read: SharedPreferences.() -> T,
    write: SharedPreferences.Editor.(T) -> Unit,
): MutableState<T> {
    val context = LocalContext.current
    val state = remember(context, key) { PreferenceState(context.defaultSharedPreferences(), key, read, write) }
    DisposableEffect(state) {
        state.startListening()
        onDispose { state.stopListening() }
    }
    return state
}

/**
 * Compose state backed by a [SharedPreferences] value. Writes go to disk and changes made elsewhere
 * (e.g. another screen holding the same key) are reflected here.
 */
private class PreferenceState<T>(
    private val preferences: SharedPreferences,
    private val key: String,
    private val read: SharedPreferences.() -> T,
    private val write: SharedPreferences.Editor.(T) -> Unit,
) : MutableState<T> {
    private val backing = mutableStateOf(preferences.read())

    // SharedPreferences only holds a weak reference to listeners, so keep a strong one here.
    private val listener =
        SharedPreferences.OnSharedPreferenceChangeListener { prefs, changedKey ->
            if (changedKey == key) backing.value = prefs.read()
        }

    override var value: T
        get() = backing.value
        set(newValue) {
            backing.value = newValue
            preferences.edit { write(newValue) }
        }

    override fun component1(): T = value

    override fun component2(): (T) -> Unit = { value = it }

    fun startListening() {
        backing.value = preferences.read()
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun stopListening() {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
