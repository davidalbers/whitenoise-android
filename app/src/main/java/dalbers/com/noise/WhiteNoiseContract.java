package dalbers.com.noise;

import android.content.SharedPreferences;
import android.os.Bundle;

import audio.WhiteNoiseAudioInterface;
import audio.WhiteNoiseAudioService;

/**
 * Created by davidalbers on 10/22/16.
 */
public interface WhiteNoiseContract {
    interface Presenter {
        void playPause();
        void setVolume(float volume);
        void setNoiseColor(WhiteNoisePresenter.NoiseColors color);
        void toggleOscillation(boolean oscillateOn);
        void toggleFade(boolean fadeOn);
        void createCancelTimer();
        void setTime(long time);
        void showAnyNotification();
        void saveInstance(Bundle savedInstanceState);
        void loadInstance(Bundle savedInstanceState);
        void dismissAnyNotification();
        void setWhiteNoiseAudioService(WhiteNoiseAudioInterface whiteNoiseAudioService);
        boolean getUseDarkMode();
        void setUsingDarkMode(boolean usingDarkMode);
        void updateDarkMode();
    }
    interface View {

        void explainOscillation();

        void explainFade();

        void setVolume(float volume);

        void setTimerUIUnsetState();

        void setTimerUIAdded(long currTime);

        void setTime(long time);

        void setPlayButtonPause();
        
        void setPlayButtonPlay();

        void showPickerDialog();

        void saveOscillateNeverOn(boolean oscillateOn, String saveKey);

        void saveFadeNeverOn(boolean fadeNeverOn, String prefFadeNeverOn);

        void recreateView();
    }
}
