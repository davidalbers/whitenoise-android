package dalbers.com.noise

import android.support.annotation.IdRes
import android.support.annotation.RawRes
import android.support.annotation.StringRes

enum class NoiseType constructor(@param:IdRes @field:IdRes @get:IdRes
                                         val id: Int, @param:RawRes @field:RawRes @get:RawRes
                                         val soundFile: Int, @param:StringRes @field:StringRes @get:StringRes
                                         val notificationTitle: Int,
                                         val prefValue: String) {

    PINK(R.id.noiseTypePink, R.raw.pink, R.string.notification_pink_type, "last_color_was_pink"),
    BROWN(R.id.noiseTypeBrown, R.raw.brown, R.string.notification_brown_type,
            "last_color_was_brown"),
    WHITE(R.id.noiseTypeWhite, R.raw.white, R.string.notification_white_type,
            "last_color_was_white"),
    NONE(0, 0, 0, "");


    companion object {

        fun fromId(@IdRes id: Int): NoiseType {
            for (noiseType in NoiseType.values()) {
                if (noiseType.id == id) {
                    return noiseType
                }
            }
            throw IllegalStateException("Unknown id: " + id)
        }

        fun fromPrefValue(prefValue: String): NoiseType {
            NoiseType.values()
                    .filter { it.prefValue == prefValue }
                    .forEach { return it }
            throw IllegalStateException("Unknown prefValue: " + prefValue)
        }
    }
}
