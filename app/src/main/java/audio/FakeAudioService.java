package audio;

import android.graphics.Bitmap;

/**
 * Created by davidalbers on 11/13/16.
 */

public class FakeAudioService implements WhiteNoiseAudioInterface {
    private boolean isPlaying = false;
    private float volume = 1.0f;
    private int soundFile = -1;
    private long time = 0l;
    private boolean oscillate = false;
    private boolean fade = false;

    @Override
    public void play() {
       isPlaying = true;
    }

    @Override
    public void stop() {
        isPlaying = false;
    }

    @Override
    public void pause() {
        isPlaying = false;
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
    }

    @Override
    public void setMaxVolume(float maxVolume) {
        this.volume = maxVolume;
    }

    @Override
    public void setTimer(long millis) {
        time = millis;
    }

    @Override
    public void cancelTimer() {
        time = 0;
    }

    @Override
    public void stopTimer() {
        time = 0;
    }

    @Override
    public long getTimeLeft() {
        return time;
    }

    @Override
    public void setSoundFile(int resId) {
        soundFile = resId;
    }

    @Override
    public int getSoundFile() {
        return soundFile;
    }

    @Override
    public void setOscillateVolume(boolean oscillateVolume) {
        this.oscillate = oscillateVolume;
    }

    @Override
    public void setDecreaseVolume(boolean decreaseVolume) {
        this.fade = decreaseVolume;
    }

    @Override
    public void oscillateForTick() {
        //todo: implement maybe?
    }

    @Override
    public void oscillateStereoForTick() {
        //todo: implement maybe?
    }

    @Override
    public void decreaseForTick() {
        //todo: implement maybe?
    }


    @Override
    public void setOscillatePeriod(long oscillatePeriod) {
        //probaby don't care about time during tests
    }

    @Override
    public void showNotification(boolean playing) {
        //fake things don't need to handle notifications
    }

    @Override
    public float getInitialVolume() {
        return volume;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void showNotification(boolean playing, Bitmap icon, String title) {
        //fake things don't need to handle notifications
    }

    @Override
    public void dismissNotification() {
        //fake things don't need to handle notifications
    }
}
