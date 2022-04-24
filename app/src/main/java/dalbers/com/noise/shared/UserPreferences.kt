package dalbers.com.noise.shared

import android.content.SharedPreferences
import dalbers.com.noise.service.safeValueOf


const val PREF_USE_DARK_MODE_KEY = "pref_use_dark_mode"
const val PREF_WAVE_INTERVAL_KEY = "pref_oscillate_interval"
const val PREF_PLAY_OVER = "pref_play_over"
const val PREF_LAST_USED_COLOR = "last_used_color"
const val PREF_LAST_VOLUME = "last_volume"
const val PREF_LAST_USED_WAVY = "last_used_wavy"
const val PREF_LAST_USED_FADE = "last_used_fade"
// todo: use this one
const val PREF_LAST_TIMER_TIME = "last_timer_time"

interface UserPreferences {
    fun playOver(): Boolean
    fun waveIntervalMillis(): Int
    var lastUsedColor: NoiseType
    var lastUsedVolume: Float
    var lastUsedWavy: Boolean
    var lastUsedFade: Boolean
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
}