package dalbers.com.noise.shared

import android.content.SharedPreferences


const val PREF_USE_DARK_MODE_KEY = "pref_use_dark_mode"
const val PREF_OSCILLATE_INTERVAL_KEY = "pref_oscillate_interval"
const val PREF_PLAY_OVER = "pref_play_over"

interface UserPreferences {
    fun playOver(): Boolean
    fun waveIntervalMillis(): Int
}

class UserPreferencesImpl(
    private val sharedPreferences: SharedPreferences,
) : UserPreferences {
    override fun playOver(): Boolean = sharedPreferences.getBoolean(PREF_PLAY_OVER, false)
    override fun waveIntervalMillis(): Int {
        return when (sharedPreferences.getInt(PREF_OSCILLATE_INTERVAL_KEY, 0)) {
            0 -> 8000
            1 -> 10000
            2 -> 12000
            3 -> 15000
            4 -> 30000
            else -> error("Unexpected preference")
        }
    }
}