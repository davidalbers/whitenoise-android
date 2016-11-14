package audio;

import android.graphics.Bitmap;

/**
 * Created by davidalbers on 10/23/16.
 */
interface BaseLoopAudioInterface {

    void play();

    void stop();

    void pause();

    void setVolume(float volume);

    void setSoundFile(int resId);

    int getSoundFile();

    float getVolume();

    boolean isPlaying();

    /**
     * Show a notification with information about the sound being played/paused
     * and a pause button which will callback to this service
     */
    void showNotification(boolean playing, Bitmap icon, String title);
    
    void dismissNotification();
}
