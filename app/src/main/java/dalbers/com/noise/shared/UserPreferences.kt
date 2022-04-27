package dalbers.com.noise.shared

import android.content.SharedPreferences

@Deprecated("Used in an older version of the app")
const val PREF_USE_DARK_MODE_KEY_LEGACY = "pref_use_dark_mode"
const val PREF_USE_DARK_MODE_KEY = "pref_use_dark_mode_v2"
@Deprecated("Used in an older version of the app")
const val PREF_WAVE_INTERVAL_KEY_LEGACY = "pref_oscillate_interval"
const val PREF_WAVE_INTERVAL_KEY = "pref_wave_interval"
const val PREF_PLAY_OVER = "pref_play_over"
const val PREF_LAST_USED_COLOR = "last_used_color"
const val PREF_LAST_VOLUME = "last_volume"
const val PREF_LAST_USED_WAVY = "last_used_wavy"
const val PREF_LAST_USED_FADE = "last_used_fade"
// TODO: use this https://github.com/davidalbers/whitenoise-android/issues/43
const val PREF_LAST_TIMER_TIME = "last_timer_time"

enum class DarkModeSetting(val key: Int) {
    AUTO(0),
    LIGHT(1),
    DARK(2),
}

interface UserPreferences {
    fun playOver(): Boolean
    fun waveIntervalMillis(): Int
    var lastUsedColor: NoiseType
    var lastUsedVolume: Float
    var lastUsedWavy: Boolean
    var lastUsedFade: Boolean
    fun migrateLegacyPreferences()
}

class UserPreferencesImpl(
    private val sharedPreferences: SharedPreferences,
) : UserPreferences {
    override fun playOver(): Boolean = sharedPreferences.getBoolean(PREF_PLAY_OVER, false)
    override fun waveIntervalMillis(): Int {
        return when (sharedPreferences.getInt(PREF_WAVE_INTERVAL_KEY, 0)) {
            0 -> 8000
            1 -> 10000
            2 -> 12000
            3 -> 15000
            4 -> 30000
            else -> error("Unexpected preference")
        }
    }

    override var lastUsedColor: NoiseType
    get() {
        val prefString = sharedPreferences.getString(PREF_LAST_USED_COLOR, NoiseType.WHITE.prefValue)
        return NoiseType.fromPrefValue(prefString ?: NoiseType.WHITE.prefValue)
    }
    set(value) {
        sharedPreferences.edit().putString(PREF_LAST_USED_COLOR, value.prefValue).apply()
    }

    override var lastUsedVolume: Float
    get() {
        return sharedPreferences.getFloat(PREF_LAST_VOLUME, 1.0f)
    }
    set(value) {
        sharedPreferences.edit().putFloat(PREF_LAST_VOLUME, value).apply()
    }

    override var lastUsedWavy: Boolean
    get() {
        return sharedPreferences.getBoolean(PREF_LAST_USED_WAVY, false)
    }
    set(value) {
        sharedPreferences.edit().putBoolean(PREF_LAST_USED_WAVY, value).apply()
    }

    override var lastUsedFade: Boolean
    get() {
        return sharedPreferences.getBoolean(PREF_LAST_USED_FADE, false)
    }
    set(value) {
        sharedPreferences.edit().putBoolean(PREF_LAST_USED_FADE, value).apply()
    }

    override fun migrateLegacyPreferences() {
        val legacyWavePrefString = sharedPreferences.getString(PREF_WAVE_INTERVAL_KEY_LEGACY, "") ?: ""
        if (legacyWavePrefString.isNotEmpty()) {
            val newWavePref = when (Integer.parseInt(legacyWavePrefString)) {
                8 -> 0
                10 -> 1
                12 -> 2
                15 -> 3
                30 -> 4
                else -> 0
            }
            sharedPreferences.edit().putInt(PREF_WAVE_INTERVAL_KEY, newWavePref).apply()
        }
        val legacyUsedDarkMode = sharedPreferences.getBoolean(PREF_USE_DARK_MODE_KEY_LEGACY, false)
        if (legacyUsedDarkMode) {
            sharedPreferences.edit().putInt(PREF_USE_DARK_MODE_KEY, DarkModeSetting.DARK.key).apply()
        }
    }
}