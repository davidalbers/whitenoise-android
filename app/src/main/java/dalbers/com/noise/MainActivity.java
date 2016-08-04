package dalbers.com.noise;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    public static String AUDIO_RES_EXTRA_KEY = "audioRes";
    private boolean started = false;
    private AudioPlayerService audioPlayerService;
    public static String LOG_TAG = "dalbers.noise/main";
    private CountDownTimer editTextCountDownTimer;
    private SeekBar volumeBar;
    private Button playButton;
    private EditText countdownTimeTextView;
    private ImageButton timerButton;
    private boolean addedTimer = false;
    private ToggleButton pinkButton;
    private ToggleButton whiteButton;
    private ToggleButton brownButton;
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
                        long time = timeStringToMillis(countdownTimeTextView.getText().toString());
                        //timer was set before noise was playing,
                        //start the timer with the music
                        if (time != 0) {
                            startTimer(time);
                        }
                    }
                }
            }
        });

        countdownTimeTextView = (EditText) findViewById(R.id.countdownTime);
        countdownTimeTextView.setText(stringToFormattedHMS(millisToHMSZeros(0)));
        Spannable span = new SpannableString(countdownTimeTextView.getText());
        //make "h m s" smaller than numbers
        span.setSpan(new RelativeSizeSpan(0.6f), 2, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new RelativeSizeSpan(0.6f), 6, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new RelativeSizeSpan(0.6f), 10, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //apply text size changes
        countdownTimeTextView.setText(span);

        final TextWatcher countdownTimeTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                countdownTimeTextView.setSelection(11);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                countdownTimeTextView.removeTextChangedListener(this);
                //update text
                countdownTimeTextView.setText(stringToFormattedHMS(s.toString()));
                Spannable span = new SpannableString(countdownTimeTextView.getText());
                //make "h m s" smaller than numbers
                span.setSpan(new RelativeSizeSpan(0.6f), 2, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new RelativeSizeSpan(0.6f), 6, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new RelativeSizeSpan(0.6f), 10, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                //apply text size changes
                countdownTimeTextView.setText(span);
                countdownTimeTextView.addTextChangedListener(this);
            }


        };

        countdownTimeTextView.addTextChangedListener(countdownTimeTextWatcher);
        countdownTimeTextView.setImeActionLabel("Start", KeyEvent.KEYCODE_ENTER);
        countdownTimeTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //enter was pressed on timer text box,
                //if noise is playing, start the timer
                if (audioPlayerService.isPlaying()) {
                    long time = timeStringToMillis(v.getText().toString());
                    startTimer(time);
                }
                return false;
            }
        });

        //disable moving the cursor
        countdownTimeTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                countdownTimeTextView.setSelection(countdownTimeTextView.getText().length());
            }
        });

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


        whiteButton = (ToggleButton) findViewById(R.id.whiteToggleButton);
        if (whiteButton != null) {
            whiteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
               @Override
               public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                   if(isChecked) {
                       pinkButton.setChecked(false);
                       brownButton.setChecked(false);
                       if(audioPlayerService != null)
                           audioPlayerService.setSoundFile(R.raw.white);
                   }
                   else {
                       //only uncheck this button if the others are uncheck
                       //this is kind of hacky, rejecting touch events in
                       //onTouchEvent would probably be more sophisticated
                       if(!pinkButton.isChecked() && !brownButton.isChecked())
                           whiteButton.setChecked(true);
                   }
               }
            });
        }

        pinkButton = (ToggleButton) findViewById(R.id.pinkToggleButton);
        if (pinkButton != null) {
            pinkButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        //uncheck other buttons
                        whiteButton.setChecked(false);
                        brownButton.setChecked(false);
                        if(audioPlayerService != null)
                            audioPlayerService.setSoundFile(R.raw.pink);
                    }
                    else {
                        //only uncheck this button if the others are uncheck
                        //this is kind of hacky, rejecting touch events in
                        //onTouchEvent would probably be more sophisticated
                        if(!whiteButton.isChecked() && !brownButton.isChecked())
                            pinkButton.setChecked(true);
                    }
                }
            });
        }

        brownButton = (ToggleButton) findViewById(R.id.brownToggleButton);
        if (brownButton != null) {
            brownButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        pinkButton.setChecked(false);
                        whiteButton.setChecked(false);
                        if(audioPlayerService != null)
                            audioPlayerService.setSoundFile(R.raw.brown);
                    }
                    else {
                        //only uncheck this button if the others are uncheck
                        //this is kind of hacky, rejecting touch events in
                        //onTouchEvent would probably be more sophisticated
                        if(!whiteButton.isChecked() && !pinkButton.isChecked())
                            brownButton.setChecked(true);
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
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (!addedTimer) {
                        //set button image
                        Drawable stopPic = getResources().getDrawable(R.drawable.ic_clear);
                        timerButton.setImageDrawable(stopPic);

                        addedTimer = true;
                        //show the text view and make the keyboard popup on it
                        countdownTimeTextView.setVisibility(View.VISIBLE);
                        countdownTimeTextView.requestFocus();
                        imm.showSoftInput(countdownTimeTextView, InputMethodManager.SHOW_IMPLICIT);
                        //set cursor in text view to last character
                        countdownTimeTextView.setSelection(countdownTimeTextView.getText().length());
                    } else {
                        addedTimer = false;
                        setTimerUIUnsetState();
                        //hide keyboard
                        imm.hideSoftInputFromWindow(countdownTimeTextView.getWindowToken(), 0);
                        stopTimer();
                        //if playing audio, set button to play
                        setPlayButtonPlay();
                    }
                }
            });
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(audioPlayerService != null && audioPlayerService.isPlaying())
            audioPlayerService.showNotification(true);
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
            unbindService(playerConnection);
            isPlayerConnectionBound = false;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        //set menu icons color
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);
            }
        }

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

    private void setTimerUIUnsetState() {
        //set button image
        Drawable addPic = getResources().getDrawable(R.drawable.ic_add);
        timerButton.setImageDrawable(addPic);
        //in portrait mode, time only needs to be hidden
        //in landscape timer needs to be gone (hidden and takes up no space)
        //this makes it so the landscape layout moves the timer add/clear button
        //to the right of the text view when a new timer is added
        //this might cause janky behavior
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            countdownTimeTextView.setVisibility(View.INVISIBLE);
        else
            countdownTimeTextView.setVisibility(View.GONE);
    }

    private ServiceConnection playerConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            AudioPlayerService.AudioPlayerBinder audioPlayerBinder = (AudioPlayerService.AudioPlayerBinder) binder;
            audioPlayerService = audioPlayerBinder.getService();
            float[] volumes = audioPlayerService.getVolume();
            volumeBar.setProgress((int) (volumeBar.getMax() * averageLRVolume(volumes[0], volumes[1])));
            Resources res = MainActivity.this.getResources();
            if (audioPlayerService.isPlaying()) {
                Drawable playPic = res.getDrawable(R.drawable.ic_action_playback_pause_black);
                playPic.setBounds(0, 0, 120, 120);
                playButton.setCompoundDrawables(playPic, null, null, null);
            } else {
                Drawable playPic = res.getDrawable(R.drawable.ic_action_playback_play_black);
                playPic.setBounds(0, 0, 120, 120);
                playButton.setCompoundDrawables(playPic, null, null, null);
            }
            audioPlayerService.setOscillatePeriod(oscillateInterval);
        }

        public void onServiceDisconnected(ComponentName className) {
            audioPlayerService = null;
        }
    };

    /**
     * Convert a string with hours mins seconds and any number of characters to milliseconds
     *
     * @param time a string with variable number of numbers and chars
     * @return milliseconds
     */
    private long timeStringToMillis(String time) {
        int[] HMS = timeStringToHMS(time);
        long timeInMillis = HMS[0] * 60 * 60 * 1000 +
                HMS[1] * 60 * 1000 +
                HMS[2] * 1000;
        return timeInMillis;
    }

    SharedPreferences.OnSharedPreferenceChangeListener sharedPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener()
    {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                loadPreferences(prefs);
        }
    };


    private void loadPreferences(SharedPreferences prefs) {
        oscillateNeverOn = prefs.getBoolean(PREF_OSCILLATE_NEVER_ON,true);
        fadeNeverOn = prefs.getBoolean(PREF_FADE_NEVER_ON,true);
        String colorPref = prefs.getString(PREF_COLOR_KEY,PREF_COLOR_WHITE);
        if(colorPref.equals(PREF_COLOR_PINK))
            preferredColorFile = R.raw.pink;
        else if(colorPref.equals(PREF_COLOR_BROWN))
            preferredColorFile = R.raw.brown;
        else
            preferredColorFile = R.raw.white;
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
            pinkButton.setChecked(true);
        else if(preferredColorFile == R.raw.brown)
            brownButton.setChecked(true);
        else
            whiteButton.setChecked(true);
        volumeBar.setProgress((int) (volumeBar.getMax() * preferredVolume));
        oscillateButton.setChecked(preferredOscillateState);
        fadeButton.setChecked(preferredFadeState);
        countdownTimeTextView.setText(millisToHMSZeros(preferredTime));
    }
    /**
     * Parse hours mins seconds from a string with variable number of numbers and chars
     *
     * @param time a string with variable number of numbers and chars
     * @return array of ints where index 0 is hours, 1 is minutes, 2 is seconds
     */
    private int[] timeStringToHMS(String time) {
        String stripped = time.replaceAll("[^\\d]", "");
        int hrs = Integer.parseInt(stripped.substring(0, 2));
        int mins = Integer.parseInt(stripped.substring(2, 4));
        int secs = Integer.parseInt(stripped.substring(4, 6));

        mins += secs / 60;
        secs = secs % 60;

        hrs += mins / 60;
        mins = mins % 60;
        return new int[]{hrs, mins, secs};
    }

    /**
     * Converts milliseconds to hours minutes and seconds
     * Where there is a maximum of two digits for each and if there is only
     * one digit, a zero is added in front
     * if millis = 60000 this would return "000100" (1 minute)
     *
     * @param millis milliseconds
     * @return string concatenated as hours+mins+seconds with zeros added appropriately
     */
    private String millisToHMSZeros(long millis) {
        int[] HMS = millisToHMS(millis);
        String formatted = "";
        if (HMS[0] < 10)
            formatted += "0" + HMS[0];
        else formatted += HMS[0];

        if (HMS[1] < 10)
            formatted += "0" + HMS[1];
        else formatted += HMS[1];

        if (HMS[2] < 10)
            formatted += "0" + HMS[2];
        else formatted += HMS[2];

        return formatted;
    }

    /**
     * Converts milliseconds to hours minutes and seconds
     * Where time 1000 would be 1 second, time 60000 would be 1 minute etc
     *
     * @param millis milliseconds
     * @return array of ints where index 0 is hours, 1 is minutes, 2 is seconds
     */
    private int[] millisToHMS(long millis) {
        //round up
        //android's countdown timer will not tick precisely on the millisecond
        //so for example, at 2 milliseconds, you will actually have 1997 ms
        //(additionally the last tick won't happen)
        int secs = (int) (millis / 1000.0 + .5);
        int hrs = secs / (60 * 60);
        secs = secs % (60 * 60);
        int mins = secs / (60);
        secs = secs % 60;
        return new int[]{hrs, mins, secs};
    }

    /**
     * Given a string with a variable amount of numbers and chars
     * Format it to the hour, min, sec format that looks like "12h 34m 56s"
     *
     * @param input string with a variable amount of numbers and chars
     * @return formatted string that looks like "12h 34m 56s"
     */
    private String stringToFormattedHMS(String input) {
        //backspaced, removed the 's' but the user intended to remove last second number
        if (input.length() == 10)
            input = input.substring(0, 9);
        //remove all non numbers
        String stripped = input.replaceAll("[^\\d]", "");
        //remove leading zeros
        while (stripped.length() > 0) {
            if (stripped.charAt(0) == '0')
                stripped = stripped.substring(1);
            else
                break;
        }
        //any number index >5
        stripped = stripped.substring(0, Math.min(stripped.length(), 6));

        String preZeros = "";
        if (stripped.length() < 6) {
            char[] zeros = new char[6 - stripped.length()];
            Arrays.fill(zeros, '0');
            preZeros = new String(zeros);
        }
        String fullNums = preZeros + stripped;
        //format in 12h 34m 56s
        return fullNums.substring(0, 2) + "h " +
                fullNums.substring(2, 4) + "m " +
                fullNums.substring(4, 6) + "s";

    }

    /**
     * Get an average of the left and right volumes in case they're different
     *
     * @param left  0f to 1.0f left volume
     * @param right 0f to 1.0f right volume
     * @return 0f to 1.0f averaged volume
     */
    private float averageLRVolume(float left, float right) {
        return (left + right) / 2;
    }

    private void startTimer(long time) {
        if (editTextCountDownTimer != null)
            editTextCountDownTimer.cancel();
        Log.d(LOG_TAG, "setting timer for " + time);
        editTextCountDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                countdownTimeTextView.setText(millisToHMSZeros(millisUntilFinished));
                Log.d(LOG_TAG, "millis " + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                countdownTimeTextView.setText(millisToHMSZeros(0));
                setPlayButtonPlay();
                setTimerUIUnsetState();
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
        countdownTimeTextView.setText(stringToFormattedHMS(millisToHMSZeros(0)));
        setTimerUIUnsetState();
    }

    private void setPlayButtonPause() {
        Drawable playPic = getResources().getDrawable(R.drawable.ic_action_playback_pause_black);
        playPic.setBounds(0, 0, 120, 120);
        playButton.setCompoundDrawables(playPic, null, null, null);
        playButton.setText("Pause");
    }

    private void setPlayButtonPlay() {
        Drawable playPic = getResources().getDrawable(R.drawable.ic_action_playback_play_black);
        playPic.setBounds(0, 0, 120, 120);
        playButton.setCompoundDrawables(playPic, null, null, null);
        playButton.setText("Play");
    }

    @Override
    public void onStart() {
        super.onStart();
        setUIBasedOnServiceState();
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
                countdownTimeTextView.setText(stringToFormattedHMS(millisToHMSZeros(0)));
                setTimerUIUnsetState();
            }

        }
    }
    @Override
    public void onStop() {
        super.onStop();
        client.disconnect();
    }
}
