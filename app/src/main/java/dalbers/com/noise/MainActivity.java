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

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import dalbers.com.timerpicker.TimerPickerDialogFragment;
import dalbers.com.timerpicker.TimerPickerDialogListener;
import dalbers.com.timerpicker.TimerTextView;


public class MainActivity extends AppCompatActivity {

    private AudioPlayerService audioPlayerService;
    public static String LOG_TAG = "dalbers.noise/main";
    private CountDownTimer editTextCountDownTimer;
    private SeekBar volumeBar;
    private Button playButton;
    private ImageButton timerButton;
    private boolean timerActive = false;
    private TimerTextView timerTextView;
    private RadioButton noiseTypeWhite;
    private RadioButton noiseTypePink;
    private RadioButton noiseTypeBrown;

    private GoogleApiClient client;
    private ToggleButton oscillateButton;
    private ToggleButton fadeButton;
    //preferences keys
    public static final String PREF_STRING = "dalbers_white_noise";
    public static final String PREF_COLOR_KEY = "color";
    public static final String PREF_COLOR_WHITE = "white";
    public static final String PREF_COLOR_BROWN = "brown";
    public static final String PREF_COLOR_PINK = "pink";
    public static final String PREF_VOLUME_KEY = "volume";
    public static final String PREF_OSCILLATE_KEY = "oscillate";
    public static final String PREF_DECREASE_KEY = "decrease";
    public static final String PREF_TIME_KEY = "time";
    public static final String PREF_OSCILLATE_NEVER_ON = "first_oscillate";
    public static final String PREF_FADE_NEVER_ON = "first_fade";
    public static final String PREF_USE_DARK_MODE_KEY = "pref_use_dark_mode";
    public static final String PREF_OSCILLATE_INTERVAL_KEY = "pref_oscillate_interval";
    public static final String SAVE_STATE_TIMER_ACTIVE = "save_state_timer_active";
    public static final String SAVE_STATE_TIMER_TIME = "save_state_timer_time";
    /** true if the user has never ever ever turned on oscillate option*/
    private boolean oscillateNeverOn = false;
    /**true if the user has never ever ever turned on fade option*/
    private boolean fadeNeverOn = false;
    private int preferredColorFile = R.raw.white;
    private float preferredVolume = 0.5f;
    private boolean preferredOscillateState = false;
    private boolean preferredFadeState = false;
    private long preferredTime = 0l;
    private boolean useDarkMode = false;
    private boolean usingDarkMode = false;
    private long oscillateInterval = 8000;
    private SharedPreferences sharedPref;
    private boolean isPlayerConnectionBound = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sharedPref.registerOnSharedPreferenceChangeListener(sharedPrefListener);
        loadPreferences(sharedPref);

        if(useDarkMode) {
            setTheme(R.style.Dark);
            usingDarkMode = true;
        }
        else {
            setTheme(R.style.AppTheme);
            usingDarkMode = false;
        }

        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent(MainActivity.this, AudioPlayerService.class);
        startService(serviceIntent);
        bindService(serviceIntent, playerConnection, Context.BIND_AUTO_CREATE);
        isPlayerConnectionBound = true;

        playButton = (Button) (findViewById(R.id.btnPlay));
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioPlayerService != null) {
                    Resources res = MainActivity.this.getResources();
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
        });

        timerTextView = (TimerTextView)findViewById(R.id.timerTextView);


        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        volumeBar.setProgress(volumeBar.getMax());
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float percentScrolled = (float) progress / seekBar.getMax();
                if (audioPlayerService != null)
                    audioPlayerService.setMaxVolume(percentScrolled);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final RadioGroup noiseTypes = (RadioGroup) findViewById(R.id.noiseTypes);
        noiseTypeWhite = (RadioButton) findViewById(R.id.noiseTypeWhite);
        noiseTypePink = (RadioButton) findViewById(R.id.noiseTypePink);
        noiseTypeBrown = (RadioButton) findViewById(R.id.noiseTypeBrown);
        if(noiseTypes != null) {
            noiseTypes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (audioPlayerService != null) {
                        switch (checkedId) {
                            case R.id.noiseTypeWhite:
                                audioPlayerService.setSoundFile(R.raw.white);
                                break;
                            case R.id.noiseTypePink:
                                audioPlayerService.setSoundFile(R.raw.pink);
                                break;
                            case R.id.noiseTypeBrown:
                                audioPlayerService.setSoundFile(R.raw.brown);
                                break;
                        }
                    }
                }
            });
        }

        oscillateButton = (ToggleButton) findViewById(R.id.waveVolumeToggle);
        if (oscillateButton != null) {
            oscillateButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(audioPlayerService != null)
                        audioPlayerService.setOscillateVolume(isChecked);
                    if(oscillateNeverOn) {
                        Toast.makeText(MainActivity.this,"Wavy Volume",Toast.LENGTH_LONG).show();
                        //update oscillateNeverOn to false in locally and in settings
                        oscillateNeverOn = false;
                        SharedPreferences sharedPref =
                                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(PREF_OSCILLATE_NEVER_ON,false);
                        editor.apply();
                    }
                }
            });
        }

        fadeButton = (ToggleButton) findViewById(R.id.decreaseVolumeToggle);
        if (fadeButton != null) {
            fadeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(audioPlayerService != null)
                        audioPlayerService.setDecreaseVolume(isChecked);
                    if(fadeNeverOn) {
                        Toast.makeText(MainActivity.this,"Fade Volume",Toast.LENGTH_LONG).show();
                        //update fadeNeverOn to false in locally and in settings
                        fadeNeverOn = false;
                        SharedPreferences sharedPref =
                                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(PREF_FADE_NEVER_ON,false);
                        editor.apply();
                    }
                }
            });
        }

        timerButton = (ImageButton) findViewById(R.id.timerButton);
        if (timerButton != null) {
            timerButton.setOnClickListener(new View.OnClickListener() {
                @Override
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
            });
        }


        if (savedInstanceState != null) {
            // Restore value of members from saved state
            timerActive = savedInstanceState.getBoolean(SAVE_STATE_TIMER_ACTIVE);
            long currTime = savedInstanceState.getLong(SAVE_STATE_TIMER_TIME);
            setTimerUIAdded(currTime);
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


    }

    /** Create and show a timer picker */
    private void showPickerDialog() {
        TimerPickerDialogFragment timerDialog = new TimerPickerDialogFragment();
        timerDialog.show(getSupportFragmentManager(), "TimerPickerDialog");
        timerDialog.setDialogListener(dialogListener);
    }

    /** Do stuff when timer is set in dialog  */
    private TimerPickerDialogListener dialogListener = new TimerPickerDialogListener() {
        @Override
        public void timeSet(long timeInMillis) {
            //ignore zero
            if(timeInMillis != 0) {
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


    @Override
    protected void onPause() {
        super.onPause();
        if(audioPlayerService != null && audioPlayerService.isPlaying())
            audioPlayerService.showNotification(true);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean(SAVE_STATE_TIMER_ACTIVE, timerActive);
        savedInstanceState.putLong(SAVE_STATE_TIMER_TIME, timerTextView.getTime());
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(usingDarkMode != useDarkMode) {
            //wait until onResume has finished,
            //then recreate the activity which will change the theme
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    recreate();
                }
            }, 0);
        }
        if(audioPlayerService != null)
            audioPlayerService.dismissNotification();
    }

    Handler handler = new Handler();


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isPlayerConnectionBound) {
            //dismiss any notification
            if(audioPlayerService != null)
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
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent(this,SettingsActivity.class);
                settingsIntent.putExtra(PREF_USE_DARK_MODE_KEY,usingDarkMode);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Visually show that there is no timer by hiding the timertextview and changing the  timer button
     * back to having a "+"
     */
    private void setTimerUIUnsetState() {
        //set button image
        Drawable addPic = getResources().getDrawable(R.drawable.ic_add);
        timerButton.setImageDrawable(addPic);
        timerTextView.setVisibility(View.GONE);
    }

    /**
     * Visually show there is a new timer by showing timer text view with time and changing
     * the timer button to having an "x"
     */
    private void setTimerUIAdded(long currTime) {
        Drawable stopPic = getResources().getDrawable(R.drawable.ic_clear);
        //change button to "clear"
        timerButton.setImageDrawable(stopPic);
        timerTextView.setTime(currTime);
        timerTextView.setVisibility(View.VISIBLE);
    }

    private ServiceConnection playerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            AudioPlayerService.AudioPlayerBinder audioPlayerBinder = (AudioPlayerService.AudioPlayerBinder) binder;
            audioPlayerService = audioPlayerBinder.getService();
            float[] volumes = audioPlayerService.getVolume();
            volumeBar.setProgress((int) (volumeBar.getMax() * Math.max(volumes[0], volumes[1])));
            Resources res = MainActivity.this.getResources();
            //convert from 120dp to pixels
            int picSizeInPixels = (int)TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 24, res.getDisplayMetrics());

            if (audioPlayerService.isPlaying()) {
                Drawable playPic = res.getDrawable(R.drawable.ic_action_playback_pause_black);
                playPic.setBounds(0, 0, picSizeInPixels, picSizeInPixels);
                playButton.setCompoundDrawables(playPic, null, null, null);
            } else {
                Drawable playPic = res.getDrawable(R.drawable.ic_action_playback_play_black);
                playPic.setBounds(0, 0, picSizeInPixels, picSizeInPixels);
                playButton.setCompoundDrawables(playPic, null, null, null);
            }
            audioPlayerService.setOscillatePeriod(oscillateInterval);
            setUIBasedOnServiceState();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            audioPlayerService = null;
        }
    };

    SharedPreferences.OnSharedPreferenceChangeListener sharedPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                loadPreferences(prefs);
        }
    };


    private void loadPreferences(SharedPreferences prefs) {
        oscillateNeverOn = prefs.getBoolean(PREF_OSCILLATE_NEVER_ON,true);
        fadeNeverOn = prefs.getBoolean(PREF_FADE_NEVER_ON,true);
        String colorPref = prefs.getString(PREF_COLOR_KEY,PREF_COLOR_WHITE);
        /* This does not work, after the app restarts,
         *it will always play white noise when brown is selected

        if(colorPref.equals(PREF_COLOR_PINK))
            preferredColorFile = R.raw.pink;
        else if(colorPref.equals(PREF_COLOR_BROWN))
            preferredColorFile = R.raw.brown;
        else
            preferredColorFile = R.raw.white;
            */
        preferredVolume = prefs.getFloat(PREF_VOLUME_KEY,0.5f);
        preferredTime = prefs.getLong(PREF_TIME_KEY,0L);
        preferredOscillateState = prefs.getBoolean(PREF_OSCILLATE_KEY,false);
        preferredFadeState = prefs.getBoolean(PREF_DECREASE_KEY,false);
        useDarkMode = prefs.getBoolean(PREF_USE_DARK_MODE_KEY,false);
        oscillateInterval = Integer.parseInt(prefs.getString(PREF_OSCILLATE_INTERVAL_KEY,"4"))*1000;
        if(audioPlayerService != null)
            audioPlayerService.setOscillatePeriod(oscillateInterval);
    }

    /**
     * Setup the UI and service based on the saved preferences
     * I'm not sure how to use this right now
     */
    private void applyAudioPreferences() {
        audioPlayerService.setSoundFile(preferredColorFile);
        audioPlayerService.setMaxVolume(preferredVolume);
        audioPlayerService.setDecreaseVolume(preferredFadeState);
        audioPlayerService.setOscillateVolume(preferredOscillateState);
        audioPlayerService.setTimer(preferredTime);
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
    }


    private void startTimer(long time) {
        if (editTextCountDownTimer != null)
            editTextCountDownTimer.cancel();
        Log.d(LOG_TAG, "setting timer for " + time);
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
        Drawable playPic = getResources().getDrawable(R.drawable.ic_action_playback_pause_black);
        //convert from 120dp to pixels
        int picSizeInPixels = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        playPic.setBounds(0, 0, picSizeInPixels, picSizeInPixels);
        playButton.setCompoundDrawables(playPic, null, null, null);
        playButton.setText("Pause");
    }

    private void setPlayButtonPlay() {
        Drawable playPic = getResources().getDrawable(R.drawable.ic_action_playback_play_black);
        //convert from 120dp to pixels
        int picSizeInPixels = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        playPic.setBounds(0, 0, picSizeInPixels, picSizeInPixels);
        playButton.setCompoundDrawables(playPic, null, null, null);
        playButton.setText("Play");
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setUIBasedOnServiceState() {
        if(audioPlayerService != null) {
            //sync up play state
            if(audioPlayerService.isPlaying())
                setPlayButtonPause();
            else
                setPlayButtonPlay();
            //sync up timer
            long timeLeft = audioPlayerService.getTimeLeft();
            //still time left
            if(timeLeft > 0) {
                //match the visual timer to the service timer
                if(audioPlayerService.isPlaying())
                    startTimer(timeLeft);
                else //cancel the visual timer since the service timer is also
                    pauseTimer();
            }
            else {
                //kill the timer just to make sure
                pauseTimer();
                timerTextView.setTime(0);
                if(audioPlayerService.getNotifyUITimerStop()) {
                    timerActive = false;
                    setTimerUIUnsetState();
                    audioPlayerService.markNotifiedUITimerStop();
                }
            }

        }
    }
    @Override
    public void onStop() {
        super.onStop();
        client.disconnect();
    }
}
