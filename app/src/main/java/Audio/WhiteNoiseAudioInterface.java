package audio;

/**
 * Created by davidalbers on 10/22/16.
 */
public interface WhiteNoiseAudioInterface extends BaseLoopAudioInterface {


    /**
     * Set the maximum volume for this service.
     * If oscillating or decreasing volume are not being used,
     * this will be the volume. If they are being used, neither
     * will use a volume higher than this volume
     *
     * @param maxVolume a value 0.0 to 1.0 where 1.0 is max of the device
     */
    void setMaxVolume(float maxVolume);

    void setTimer(final long millis);

    void cancelTimer();

    void stopTimer();

    long getTimeLeft();

    void setSoundFile(int resId);

    int getSoundFile();

    void setOscillateVolume(boolean oscillateVolume);

    void setDecreaseVolume(boolean decreaseVolume);


    /**
     * Oscillate volume from masterVolume to (masterVolume*minVolumePercent)
     * Going from max to min to max takes 'oscillatePeriod' milliseconds
     */
    void oscillateForTick();

    /**
     * Oscillates volume in one speaker and then the other
     * Example
     * left:  1.0 -> .2  -> 1.0
     * right: stays at 1.0
     * then
     * left:  stays at 1.0
     * right: 1.0 -> 2.0 -> 1.0
     */
    void oscillateStereoForTick();

    void decreaseForTick();

    void setOscillatePeriod(long oscillatePeriod);

    /**
     * Show a notification with information about the sound being played/paused
     * and a pause button which will callback to this service
     */
    void showNotification(boolean playing);
}
