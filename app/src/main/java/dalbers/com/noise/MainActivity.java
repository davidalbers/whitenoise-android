package dalbers.com.noise;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.RawRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import dalbers.com.timerpicker.TimerPickerDialogFragment;
import dalbers.com.timerpicker.TimerPickerDialogListener;
import dalbers.com.timerpicker.TimerTextView;

import static dalbers.com.noise.LoopMediaPlayer.NO_SOUND_FILE;


public class MainActivity extends AppCompatActivity {

    //preferences keys
    public static final String PREF_OSCILLATE_NEVER_ON = "first_oscillate";
    public static final String PREF_FADE_NEVER_ON = "first_fade";
    public static final String PREF_USE_DARK_MODE_KEY = "pref_use_dark_mode";
    public static final String PREF_OSCILLATE_INTERVAL_KEY = "pref_oscillate_interval";
    public static final String SAVE_STATE_TIMER_CREATED = "save_state_timer_active";
    public static final String SAVE_STATE_TIMER_TIME = "save_state_timer_time";
    public static final String LOG_TAG = "dalbers.noise/main";
    @BindView(R.id.volumeBar)
    SeekBar volumeBar;
    @BindView(R.id.btnPlay)
    Button playButton;
    @BindView(R.id.timerButton)
    ImageButton timerButton;
    @BindView(R.id.timerTextView)
    TimerTextView timerTextView;
    @BindView(R.id.noiseTypes)
    RadioGroup noiseTypes;
    @BindView(R.id.noiseTypeWhite)
    RadioButton noiseTypeWhite;
    @BindView(R.id.noiseTypePink)
    RadioButton noiseTypePink;
    @BindView(R.id.noiseTypeBrown)
    RadioButton noiseTypeBrown;
    @BindView(R.id.waveVolumeToggle)
    ToggleButton oscillateButton;
    @BindView(R.id.decreaseVolumeToggle)
    ToggleButton fadeButton;
    @BindDrawable(R.drawable.ic_add)
    Drawable addPic;
    @BindDrawable(R.drawable.ic_action_playback_play_black)
    Drawable playPic;
    @BindDrawable(R.drawable.ic_action_playback_pause_black)
    Drawable pausePic;
    @BindDrawable(R.drawable.ic_clear)
    Drawable stopPic;
    boolean timerActive = false;
    Handler handler = new Handler();
    private AudioPlayerService audioPlayerService;
    private CountDownTimer editTextCountDownTimer;
    /**
     * true if the user has never ever ever turned on oscillate option
     */
    private boolean oscillateNeverOn = false;
    /**
     * true if the user has never ever ever turned on fade option
     */
    private boolean fadeNeverOn = false;
    private boolean useDarkMode = false;
    private boolean usingDarkMode = false;
    private long oscillateInterval = 8000;
    SharedPreferences.OnSharedPreferenceChangeListener sharedPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            loadPreferences(prefs);
        }
    };
    private boolean isPlayerConnectionBound = false;
    private boolean timerCreatedAndNotStarted = false;
    /**
     * Do stuff when timer is set in dialog
     */
    private TimerPickerDialogListener dialogListener = new TimerPickerDialogListener() {
        @Override
        public void timeSet(long timeInMillis) {
            //ignore zero
            if (timeInMillis != 0) {
                timerActive = true;
                setTimerUIAdded(timeInMillis);
                if (audioPlayerService.isPlaying()) {
                    startTimer(timeInMillis);
                }
            }
        }

        @Override
        public void dialogCanceled() {
            //nothing important happened
        }
    };

    RadioGroup.OnCheckedChangeListener noiseChangeListener =
            new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (audioPlayerService != null) {
                        switch (checkedId) {
                            case R.id.noiseTypePink:
                                audioPlayerService.setSoundFile(R.raw.pink);
                                break;
                            case R.id.noiseTypeBrown:
                                audioPlayerService.setSoundFile(R.raw.brown);
                                break;
                            default:
                                audioPlayerService.setSoundFile(R.raw.white);
                                break;
                        }
                    }
                }
            };

    private ServiceConnection playerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            AudioPlayerService.AudioPlayerBinder audioPlayerBinder =
                    (AudioPlayerService.AudioPlayerBinder) binder;
            audioPlayerService = audioPlayerBinder.getService();
            float[] volumes = audioPlayerService.getVolume();
            volumeBar.setProgress((int) (volumeBar.getMax() * Math.max(volumes[0], volumes[1])));
            Resources res = MainActivity.this.getResources();
            //convert from 120dp to pixels
            int picSizeInPixels = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 24, res.getDisplayMetrics());

            if(audioPlayerService.getSoundFile() == NO_SOUND_FILE)
                audioPlayerService.setSoundFile(R.raw.white);
            if (audioPlayerService.isPlaying()) {
                pausePic.setBounds(0, 0, picSizeInPixels, picSizeInPixels);
                playButton.setCompoundDrawables(pausePic, null, null, null);
            } else {
                playPic.setBounds(0, 0, picSizeInPixels, picSizeInPixels);
                playButton.setCompoundDrawables(playPic, null, null, null);
            }
            //sync UI with service's chosen sound file
            setCheckedNoiseType(audioPlayerService.getSoundFile());
            audioPlayerService.setOscillatePeriod(oscillateInterval);
            setUIBasedOnServiceState();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            audioPlayerService = null;
        }

        /**
         * Set the checked sound file in the UI
         * @param soundFile file which is currently playing
         */
        private void setCheckedNoiseType(@RawRes int soundFile) {
            switch (soundFile) {
                case R.raw.brown:
                    noiseTypeBrown.setChecked(true);
                    break;
                case R.raw.pink:
                    noiseTypePink.setChecked(true);
                    break;
                default:
                    noiseTypeWhite.setChecked(true);
                    break;
            }
        }

        private void setUIBasedOnServiceState() {
            if (audioPlayerService != null) {
                //sync up play state
                if (audioPlayerService.isPlaying())
                    setPlayButtonPause();
                else
                    setPlayButtonPlay();
                //sync up timer
                long timeLeft = audioPlayerService.getTimeLeft();
                //still time left
                if (timeLeft > 0) {
                    //match the visual timer to the service timer
                    if (audioPlayerService.isPlaying()) {
                        setTimerUIAdded(timeLeft);
                        startTimer(timeLeft);
                    } else //cancel the visual timer since the service timer is also
                        pauseTimer();
                } else if (!timerCreatedAndNotStarted) {
                    //service says timer is unset and we have no saved state telling us otherwise
                    timerTextView.setTime(0);
                    timerActive = false;
                    setTimerUIUnsetState();
                }

            }
        }
    };


    private SeekBar.OnSeekBarChangeListener volumeChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float percentScrolled = (float) progress / seekBar.getMax();
            if (audioPlayerService != null)
                audioPlayerService.setMaxVolume(percentScrolled);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //Don't care about when tracking stops/starts only when progress changes
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //Don't care about when tracking stops/starts only when progress changes
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sharedPref.registerOnSharedPreferenceChangeListener(sharedPrefListener);
        loadPreferences(sharedPref);

        if (useDarkMode) {
            setTheme(R.style.Dark);
            usingDarkMode = true;
        } else {
            setTheme(R.style.AppTheme);
            usingDarkMode = false;
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Intent serviceIntent = new Intent(MainActivity.this, AudioPlayerService.class);
        startService(serviceIntent);
        bindService(serviceIntent, playerConnection, Context.BIND_AUTO_CREATE);
        isPlayerConnectionBound = true;

        volumeBar.setProgress(volumeBar.getMax());
        volumeBar.setOnSeekBarChangeListener(volumeChangeListener);
        noiseTypes.setOnCheckedChangeListener(noiseChangeListener);

        timerCreatedAndNotStarted = false;
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            long currTime = savedInstanceState.getLong(SAVE_STATE_TIMER_TIME);
            timerCreatedAndNotStarted = savedInstanceState.getBoolean(SAVE_STATE_TIMER_CREATED);
            // If the app restarted while a timer was created but not started,
            // recreate the view state
            // ignore zero times because the user did not create those.
            if (timerCreatedAndNotStarted) {
                timerActive = timerCreatedAndNotStarted;
                setTimerUIAdded(currTime);
            }
        }
    }

    @OnCheckedChanged(R.id.waveVolumeToggle)
    public void oscillateChecked(CompoundButton buttonView, boolean isChecked) {
        if (audioPlayerService != null)
            audioPlayerService.setOscillateVolume(isChecked);
        if (oscillateNeverOn) {
            Toast.makeText(MainActivity.this, getString(R.string.wave_volume_toast), Toast.LENGTH_LONG).show();
            //update oscillateNeverOn to false in locally and in settings
            oscillateNeverOn = false;
            SharedPreferences sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(PREF_OSCILLATE_NEVER_ON, false);
            editor.apply();
        }
    }

    @OnCheckedChanged(R.id.decreaseVolumeToggle)
    public void fadeChecked(CompoundButton buttonView, boolean isChecked) {
        if (audioPlayerService != null)
            audioPlayerService.setDecreaseVolume(isChecked);
        if (fadeNeverOn) {
            Toast.makeText(MainActivity.this, getString(R.string.fade_volume_toast), Toast.LENGTH_LONG).show();
            //update fadeNeverOn to false in locally and in settings
            fadeNeverOn = false;
            SharedPreferences sharedPref =
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(PREF_FADE_NEVER_ON, false);
            editor.apply();
        }
    }

    @OnClick(R.id.timerButton)
    public void onClick(View v) {
        if (!timerActive) {
            showPickerDialog();
        } else {
            timerActive = false;
            setTimerUIUnsetState();
            stopTimer();
            //if playing audio, set button to play
            setPlayButtonPlay();
        }
    }



    @OnClick(R.id.btnPlay)
    public void playButtonClick() {
        if (audioPlayerService != null) {
            if (audioPlayerService.isPlaying()) {
                audioPlayerService.stop();
                setPlayButtonPlay();
                pauseTimer();
            } else {
                audioPlayerService.play();
                setPlayButtonPause();
                long time = timerTextView.getTime();
                //timer was set before noise was playing,
                //start the timer with the music
                if (time != 0) {
                    startTimer(time);
                }
            }
        }
    }


    /**
     * Create and show a timer picker
     */
    private void showPickerDialog() {
        TimerPickerDialogFragment timerDialog = new TimerPickerDialogFragment();
        timerDialog.show(getSupportFragmentManager(), "TimerPickerDialog");
        timerDialog.setDialogListener(dialogListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (audioPlayerService != null && audioPlayerService.isPlaying())
            audioPlayerService.showNotification(true);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        boolean timerSetAndNotStarted = false;
        //we only need to save timer state if it was created but not started
        //if it wasn't created - who cares
        //if it was started - the service will tell us its state
        if (audioPlayerService != null &&
                !audioPlayerService.isPlaying() && timerTextView.getTime() != 0)
            timerSetAndNotStarted = true;
        savedInstanceState.putBoolean(SAVE_STATE_TIMER_CREATED, timerSetAndNotStarted);
        savedInstanceState.putLong(SAVE_STATE_TIMER_TIME, timerTextView.getTime());
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (usingDarkMode != useDarkMode) {
            //wait until onResume has finished,
            //then recreate the activity which will change the theme
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recreate();
                }
            }, 0);
        }
        if (audioPlayerService != null)
            audioPlayerService.dismissNotification();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPlayerConnectionBound) {
            //dismiss any notification
            if (audioPlayerService != null)
                audioPlayerService.dismissNotification();
            //unbind the service, it will still be running
            unbindService(playerConnection);
            isPlayerConnectionBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if(item.getItemId() == R.id.settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            settingsIntent.putExtra(PREF_USE_DARK_MODE_KEY, usingDarkMode);
            startActivity(settingsIntent);
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    /**
     * Visually show that there is no timer by hiding the timertextview
     * and changing the  timer button back to having a "+"
     */
    private void setTimerUIUnsetState() {
        //set button image
        timerButton.setImageDrawable(addPic);
        timerTextView.setVisibility(View.GONE);
    }

    /**
     * Visually show there is a new timer by showing timer text view with time and changing
     * the timer button to having an "x"
     */
    private void setTimerUIAdded(long currTime) {
        //change button to "clear"
        timerButton.setImageDrawable(stopPic);
        timerTextView.setTime(currTime);
        timerTextView.setVisibility(View.VISIBLE);
    }

    private void loadPreferences(SharedPreferences prefs) {
        oscillateNeverOn = prefs.getBoolean(PREF_OSCILLATE_NEVER_ON, true);
        fadeNeverOn = prefs.getBoolean(PREF_FADE_NEVER_ON, true);
        useDarkMode = prefs.getBoolean(PREF_USE_DARK_MODE_KEY, false);
        oscillateInterval =
                Integer.parseInt(prefs.getString(PREF_OSCILLATE_INTERVAL_KEY, "4")) * 1000;
        if (audioPlayerService != null)
            audioPlayerService.setOscillatePeriod(oscillateInterval);
    }


    private void startTimer(long time) {
        if (editTextCountDownTimer != null)
            editTextCountDownTimer.cancel();
        editTextCountDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setTime(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerTextView.setTime(0);
                setPlayButtonPlay();
                setTimerUIUnsetState();
                timerActive = false;
            }
        };
        audioPlayerService.setTimer(time);
        editTextCountDownTimer.start();
    }

    private void pauseTimer() {
        if (editTextCountDownTimer != null)
            editTextCountDownTimer.cancel();
        audioPlayerService.cancelTimer();
    }

    /**
     * pause the timer and set the time to zero
     */
    private void stopTimer() {
        pauseTimer();
        if (audioPlayerService != null)
            audioPlayerService.stop();
        timerTextView.setTime(0);
        timerActive = false;
        setTimerUIUnsetState();
    }

    private void setPlayButtonPause() {
        //convert from 120dp to pixels
        int picSizeInPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        pausePic.setBounds(0, 0, picSizeInPixels, picSizeInPixels);
        playButton.setCompoundDrawables(pausePic, null, null, null);
        playButton.setText(getString(R.string.audio_pause));
    }

    private void setPlayButtonPlay() {
        //convert from 120dp to pixels
        int picSizeInPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        playPic.setBounds(0, 0, picSizeInPixels, picSizeInPixels);
        playButton.setCompoundDrawables(playPic, null, null, null);
        playButton.setText(getString(R.string.audio_play));
    }

}
