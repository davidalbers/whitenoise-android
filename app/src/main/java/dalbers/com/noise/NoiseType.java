package dalbers.com.noise;

import android.support.annotation.IdRes;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;

enum NoiseType {

    PINK(R.id.noiseTypePink, R.raw.pink, R.string.notification_pink_type, "last_color_was_pink"),
    BROWN(R.id.noiseTypeBrown, R.raw.brown, R.string.notification_brown_type, "last_color_was_brown"),
    WHITE(R.id.noiseTypeWhite, R.raw.white, R.string.notification_white_type, "last_color_was_white");

    @IdRes private final int id;
    @RawRes private final int soundFile;
    @StringRes private final int notificationTitle;
    private final String prefValue;

    NoiseType(@IdRes int id, @RawRes int soundFile, @StringRes int notificationTitle,
              String prefValue) {
        this.id = id;
        this.soundFile = soundFile;
        this.notificationTitle = notificationTitle;
        this.prefValue = prefValue;
    }

    @IdRes
    public int getId() {
        return id;
    }

    @RawRes
    public int getSoundFile() {
        return soundFile;
    }

    @StringRes
    public int getNotificationTitle() {
        return notificationTitle;
    }

    public String getPrefValue() {
        return prefValue;
    }

    public static NoiseType fromId(@IdRes int id) {
        for (NoiseType noiseType : NoiseType.values()) {
            if (noiseType.id == id) {
                return noiseType;
            }
        }
        throw new IllegalStateException("Unknown id: " + id);
    }

    public static NoiseType fromSoundFile(@RawRes int soundFile) {
        for (NoiseType noiseType : NoiseType.values()) {
            if (noiseType.soundFile == soundFile) {
                return noiseType;
            }
        }
        throw new IllegalStateException("Unknown soundFile: " + soundFile);
    }

    public static NoiseType fromPrefValue(String prefValue) {
        for (NoiseType noiseType : NoiseType.values()) {
            if (noiseType.prefValue.equals(prefValue)) {
                return noiseType;
            }
        }
        throw new IllegalStateException("Unknown prefValue: " + prefValue);
    }
}
