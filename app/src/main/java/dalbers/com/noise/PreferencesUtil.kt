package dalbers.com.noise

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

enum class Preferences(val key: String) {
    OSCILLATE_NEVER_ON("first_oscillate"),
    FADE_NEVER_ON("first_fade"),
    DARK_MODE("pref_use_dark_mode"),
    WAVE_INTERVAL("pref_oscillate_interval"),
    PLAY_OVER("pref_play_over"),
    STATE_TIMER_CREATED("save_state_timer_active"),
    STATE_TIMER_TIME("save_state_timer_time"),
    COLOR("last_used_color"),
    VOLUME("last_volume"),
    WAVY("last_used_wavy"),
    FADE("last_used_fade"),
    TIME("last_timer_time")
}
class PreferencesUtil {

    fun use(baseContext: Context, function: (SharedPreferences.Editor) -> Unit) {
        val editor = PreferenceManager.getDefaultSharedPreferences(baseContext).edit()
        function(editor)
        editor.apply()
    }

    fun getBoolean(baseContext: Context, pref: Preferences, default: Boolean) =
            get(baseContext).getBoolean(pref.key, default)

    fun getString(baseContext: Context, pref: Preferences, default: String) =
            get(baseContext).getString(pref.key, default)

    fun getLong(baseContext: Context, pref: Preferences, default: Long) =
            get(baseContext).getLong(pref.key, default)

    fun getFloat(baseContext: Context, pref: Preferences, default: Float) =
            get(baseContext).getFloat(pref.key, default)

    fun setChangeListener(baseContext: Context,
                                  listener: SharedPreferences.OnSharedPreferenceChangeListener) =
            get(baseContext).registerOnSharedPreferenceChangeListener(listener)

    private fun get(baseContext: Context): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(baseContext)

}
