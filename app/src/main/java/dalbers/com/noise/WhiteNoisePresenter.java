package dalbers.com.noise;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;


import audio.WhiteNoiseAudioInterface;

import static audio.LoopMediaPlayer.NO_SOUND_ID;

/**
 * Created by davidalbers on 10/2/16.
 */
public class WhiteNoisePresenter implements WhiteNoiseContract.Presenter {

    /**Reference to the main activity, our presenter*/
    private WhiteNoiseContract.View view;
    /**Handles playing and manipulating music*/
    private WhiteNoiseAudioInterface whiteNoiseAudioService;
    /**Handles updating time visually*/
    private CountDownTimer editTextCountDownTimer;
    /**Timer was created and has not been cleared or run out*/
    private boolean timerActive = false;
    /**Time held by timer*/
    private long currTime = 0;
    /** true if the user has never ever ever turned on oscillate option, comes from shared prefs*/
    private boolean oscillateNeverOn = false;
    /**true if the user has never ever ever turned on fade option, comes from shared prefs*/
    private boolean fadeNeverOn = false;
    //preferences keys
    public static final String PREF_OSCILLATE_NEVER_ON = "first_oscillate";
    public static final String PREF_FADE_NEVER_ON = "first_fade";
    /**Interval of volume waves, comes from shared prefs*/
    private long oscillateInterval = 8000;
    /**User specified to use dark mode in settings, comes from shared prefs*/
    private boolean useDarkMode = false;
    /**
     * The UI is actually using dark mode, this can be != useDarkMode in the case that useDark mode
     * has just changed and the UI has not gotten the update
     */
    private boolean usingDarkMode = false;

    //Keys for saved instance state
    public static final String SAVE_STATE_TIMER_CREATED = "save_state_timer_active";
    public static final String SAVE_STATE_TIMER_TIME = "save_state_timer_time";

    public enum NoiseColors {
        white,
        pink,
        brown
    }

    /**
     * Which noise file is playing or will be played currently
     */
    private int currNoiseResId = R.raw.white;
    public static final String PREF_USE_DARK_MODE_KEY = "pref_use_dark_mode";
    public static final String PREF_OSCILLATE_INTERVAL_KEY = "pref_oscillate_interval";

    public WhiteNoisePresenter(WhiteNoiseContract.View view) {
        this.view = view;
    }

    /**
     * Construct the presenter with all dependencies. This probably can't be used in production
     * because a real service is created asynchronously. The only use for this is for testing
     * with fake/mocked params
     * @param view
     * @param audioService
     */
    public WhiteNoisePresenter(WhiteNoiseContract.View view, WhiteNoiseAudioInterface audioService) {
        this.view = view;
        setWhiteNoiseAudioService(audioService);
    }

        /**Either pause or play the noise, depending on state*/
    public void playPause() {
        if (whiteNoiseAudioService != null) {
            if (whiteNoiseAudioService.isPlaying()) {
                whiteNoiseAudioService.stop();
                view.setPlayButtonPlay();
                pauseTimer();
            } else {
                whiteNoiseAudioService.play();
                view.setPlayButtonPause();
                //timer was set before noise was playing,
                //start the timer with the music
                if (currTime != 0) {
                    startTimer(currTime);
                }
            }
        }
    }

    /**
     * Set the audio volume
     * @param volume a float in range 0.0 to 1.0 where 1.0 is max and 0.0 is mute.
     */
    public void setVolume(float volume) {
        if (whiteNoiseAudioService != null)
            whiteNoiseAudioService.setMaxVolume(volume);
    }

    /**
     * Specify which color of noise to play
     * @param color
     */
    public void setNoiseColor(NoiseColors color) {
        if (whiteNoiseAudioService != null) {
            switch (color) {
                case white:
                    currNoiseResId = R.raw.white;
                    break;
                case pink:
                    currNoiseResId = R.raw.pink;
                    break;
                case brown:
                    currNoiseResId = R.raw.brown;
                    break;
            }
            whiteNoiseAudioService.setSoundFile(currNoiseResId);
        }
    }

    public void toggleOscillation(boolean oscillateOn) {
        if(whiteNoiseAudioService != null)
            whiteNoiseAudioService.setOscillateVolume(oscillateOn);
        if(oscillateNeverOn) {
            view.explainOscillation();
            //update oscillateNeverOn to false in locally and in settings
            oscillateNeverOn = false;
            view.saveOscillateNeverOn(oscillateNeverOn,PREF_OSCILLATE_NEVER_ON);
        }
    }

    public void toggleFade(boolean fadeOn) {
        if(whiteNoiseAudioService != null)
            whiteNoiseAudioService.setDecreaseVolume(fadeOn);
        if(fadeNeverOn) {
           view.explainFade();
            //update fadeNeverOn to false in locally and in settings
            fadeNeverOn = false;
            view.saveFadeNeverOn(fadeNeverOn,PREF_FADE_NEVER_ON);
        }
    }

    /**
     * Based on state, create or cancel the timer
     */
    public void createCancelTimer() {
        if (!timerActive) {
            view.showPickerDialog();
        } else {
            timerActive = false;
            view.setTimerUIUnsetState();
            stopTimer();
            //if playing audio, set button to play
            view.setPlayButtonPlay();
        }
    }

    /**
     * Create and start a timer with given time
     * @param time countdown timer in milliseconds
     */
    private void startTimer(long time) {
        if (editTextCountDownTimer != null)
            editTextCountDownTimer.cancel();
        editTextCountDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                currTime = millisUntilFinished;
                view.setTime(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                onTimerFinished();
            }
        };
        whiteNoiseAudioService.setTimer(time);
        editTextCountDownTimer.start();
    }

    /**
     * Update state when timer finishes
     */
    private void onTimerFinished() {
        view.setTime(0);
        view.setPlayButtonPlay();
        view.setTimerUIUnsetState();
        timerActive = false;
        currTime = 0;
    }

    /**
     * Cancel timers but don't set time to zero
     */
    private void pauseTimer() {
        if (editTextCountDownTimer != null)
            editTextCountDownTimer.cancel();
        whiteNoiseAudioService.cancelTimer();
    }

    /**
     * pause the timer and set the time to zero
     */
    private void stopTimer() {
        pauseTimer();
        if (whiteNoiseAudioService != null)
            whiteNoiseAudioService.stop();
        view.setTime(0);
        timerActive = false;
        view.setTimerUIUnsetState();
    }

    public void setTime(long time) {
        currTime = time;
        timerActive = true;
        view.setTimerUIAdded(time);
        if (whiteNoiseAudioService != null && whiteNoiseAudioService.isPlaying()) {
            startTimer(time);
        }
    }

    public void showAnyNotification() {
        if(whiteNoiseAudioService != null && whiteNoiseAudioService.isPlaying())
            whiteNoiseAudioService.showNotification(true);
    }

    public void saveInstance(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(SAVE_STATE_TIMER_CREATED, timerActive);
        savedInstanceState.putLong(SAVE_STATE_TIMER_TIME, currTime);
    }

    public void loadInstance(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            currTime = savedInstanceState.getLong(SAVE_STATE_TIMER_TIME);
            timerActive = savedInstanceState.getBoolean(SAVE_STATE_TIMER_CREATED);
            if(timerActive)
                setTime(currTime);

        }
    }

    public void dismissAnyNotification() {
        if(whiteNoiseAudioService != null)
            whiteNoiseAudioService.dismissNotification();
    }

   /**
     * Setup the UI and service based on the saved preferences
     * I'm not sure how to use this right now
     *
    public static final String PREF_STRING = "dalbers_white_noise";
    public static final String PREF_COLOR_KEY = "color";
    public static final String PREF_COLOR_WHITE = "white";
    public static final String PREF_COLOR_BROWN = "brown";
    public static final String PREF_COLOR_PINK = "pink";
    public static final String PREF_VOLUME_KEY = "volume";
    public static final String PREF_OSCILLATE_KEY = "oscillate";
    public static final String PREF_DECREASE_KEY = "decrease";
    public static final String PREF_TIME_KEY = "time";
    private void applyAudioPreferences() {
        whiteNoiseAudioService.setSoundFile(preferredColorFile);
        whiteNoiseAudioService.setMaxVolume(preferredVolume);
        whiteNoiseAudioService.setDecreaseVolume(preferredFadeState);
        whiteNoiseAudioService.setOscillateVolume(preferredOscillateState);
        whiteNoiseAudioService.setTimer(preferredTime);
        if(preferredColorFile == R.raw.pink)
            noiseTypePink.setChecked(true);
        else if(preferredColorFile == R.raw.brown)
            noiseTypeBrown.setChecked(true);
        else
            noiseTypeWhite.setChecked(true);
        volumeBar.setProgress((int) (volumeBar.getMax() * preferredVolume));
        oscillateButton.setChecked(preferredOscillateState);
        fadeButton.setChecked(preferredFadeState);
        timerTextView.setTime(preferredTime);
    }*/

    public void loadPreferences(SharedPreferences prefs) {
        oscillateNeverOn = prefs.getBoolean(PREF_OSCILLATE_NEVER_ON,true);
        fadeNeverOn = prefs.getBoolean(PREF_FADE_NEVER_ON,true);

        //String colorPref = prefs.getString(PREF_COLOR_KEY,PREF_COLOR_WHITE);
        /* This does not work, after the app restarts,
         *it will always play white noise when brown is selected

        if(colorPref.equals(PREF_COLOR_PINK))
            preferredColorFile = R.raw.pink;
        else if(colorPref.equals(PREF_COLOR_BROWN))
            preferredColorFile = R.raw.brown;
        else
            preferredColorFile = R.raw.white;
            */
        //preferredVolume = prefs.getFloat(PREF_VOLUME_KEY,0.5f);
        //preferredTime = prefs.getLong(PREF_TIME_KEY,0L);
        //preferredOscillateState = prefs.getBoolean(PREF_OSCILLATE_KEY,false);
        //preferredFadeState = prefs.getBoolean(PREF_DECREASE_KEY,false);
        useDarkMode = prefs.getBoolean(PREF_USE_DARK_MODE_KEY,false);
        oscillateInterval = Integer.parseInt(prefs.getString(PREF_OSCILLATE_INTERVAL_KEY,"4"))*1000;
    }

    public void setWhiteNoiseAudioService(WhiteNoiseAudioInterface whiteNoiseAudioService) {
        this.whiteNoiseAudioService = whiteNoiseAudioService;
        if(this.whiteNoiseAudioService != null) {
            float volume = whiteNoiseAudioService.getInitialVolume();
            view.setVolume(volume);
            whiteNoiseAudioService.setOscillatePeriod(oscillateInterval);
            //if no sound file set, set it to white by default
            if(whiteNoiseAudioService.getSoundFile() == NO_SOUND_ID)
                whiteNoiseAudioService.setSoundFile(R.raw.white);
            setUIBasedOnServiceState();
        }
    }

   private void setUIBasedOnServiceState() {
        if(whiteNoiseAudioService != null) {
            //sync up play state
            if(whiteNoiseAudioService.isPlaying())
                view.setPlayButtonPause();
            else
                view.setPlayButtonPlay();
            //sync up timer
            long timeLeft = whiteNoiseAudioService.getTimeLeft();
            //still time left, according to service
            if(timeLeft > 0) {
                //match the visual timer to the service timer
                if(whiteNoiseAudioService.isPlaying()) {
                    view.setTimerUIAdded(timeLeft);
                    startTimer(timeLeft);
                }
                else //cancel the visual timer since the service timer is also
                    pauseTimer();
            }
            else if (!timerActive) {
                //service says timer is unset and the user did not create a timer before
                //service got connected, so set timer to zero
                view.setTime(0);
                timerActive = false;
                view.setTimerUIUnsetState();
            }

        }
   }


    public boolean getUseDarkMode() {
        return useDarkMode;
    }

    public void setUsingDarkMode(boolean usingDarkMode) {
       this.usingDarkMode = usingDarkMode;
    }

    public void updateDarkMode() {
       if(usingDarkMode != useDarkMode) {
            //wait until onResume has finished,
            //then recreate the activity which will change the theme
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    view.recreateView();
                }
            }, 0);
        }
    }

    Handler handler = new Handler();
}
