package dalbers.com.noise.shared

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import dalbers.com.noise.R
import java.lang.IllegalStateException

val defaultNoiseTypes = listOf(
    NoiseType.WHITE,
    NoiseType.PINK,
    NoiseType.BROWN,
)

enum class NoiseType(
    @RawRes val soundFile: Int,
    @StringRes val notificationTitle: Int,
    val prefValue: String,
    @StringRes val label: Int,
) {
    WHITE(
        soundFile = R.raw.white,
        notificationTitle = R.string.notification_white_type,
        prefValue = "last_color_was_white",
        label = R.string.white_label,
    ),
    PINK(
        soundFile = R.raw.pink,
        notificationTitle = R.string.notification_pink_type,
        prefValue = "last_color_was_pink",
        label = R.string.pink_label,
    ),
    BROWN(
        soundFile = R.raw.brown,
        notificationTitle = R.string.notification_brown_type,
        prefValue = "last_color_was_brown",
        label = R.string.brown_label,
    ),
    NONE(0, 0, "", 0);

    companion object {
        @JvmStatic
        fun fromPrefValue(prefValue: String): NoiseType {
            for (noiseType in values()) {
                if (noiseType.prefValue == prefValue) {
                    return noiseType
                }
            }
            throw IllegalStateException("Unknown prefValue: $prefValue")
        }
    }
}