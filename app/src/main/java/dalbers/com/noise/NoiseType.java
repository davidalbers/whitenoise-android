package dalbers.com.noise;

import android.support.annotation.IdRes;
import android.support.annotation.RawRes;

enum NoiseType {

    PINK(R.id.noiseTypePink, R.raw.pink, "last_color_was_pink"),
    BROWN(R.id.noiseTypeBrown, R.raw.brown, "last_color_was_brown"),
    WHITE(R.id.noiseTypeWhite, R.raw.white, "last_color_was_white");

    @IdRes private final int id;
    @RawRes private final int soundFile;
    private final String prefValue;

    NoiseType(@IdRes int id, @RawRes int soundFile, String prefValue) {
        this.id = id;
        this.soundFile = soundFile;
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

    public static NoiseType fromPrefValue(String prefValue) {
        for (NoiseType noiseType : NoiseType.values()) {
            if (noiseType.prefValue.equals(prefValue)) {
                return noiseType;
            }
        }
        throw new IllegalStateException("Unknown prefValue: " + prefValue);
    }
}
