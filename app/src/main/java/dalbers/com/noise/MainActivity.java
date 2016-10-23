package dalbers.com.noise;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import audio.WhiteNoiseAudioService;
import dalbers.com.timerpicker.TimerPickerDialogFragment;
import dalbers.com.timerpicker.TimerPickerDialogListener;
import dalbers.com.timerpicker.TimerTextView;


public class MainActivity extends AppCompatActivity {


    public static String LOG_TAG = "dalbers.noise/main";
    private SeekBar volumeBar;
    private Button playButton;
    private ImageButton timerButton;
    private TimerTextView timerTextView;
    private GoogleApiClient client;
    private boolean isPlayerConnectionBound = false;

    private WhiteNoisePresenter whiteNoisePresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        whiteNoisePresenter = new WhiteNoisePresenter(this,sharedPref);
        //let presenter decide if we want to use dark mode
        boolean useDarkMode = whiteNoisePresenter.getUseDarkMode();
        if(useDarkMode) {
            Log.d(LOG_TAG,"setting theme to dark mode");
            setTheme(R.style.Dark);
        }
        else {
            Log.d(LOG_TAG,"setting theme to regular");
            setTheme(R.style.AppTheme);
        }
        //let the presenter know we've set dark mode on/off
        whiteNoisePresenter.setUsingDarkMode(useDarkMode);

        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent(MainActivity.this, WhiteNoiseAudioService.class);
        startService(serviceIntent);
        bindService(serviceIntent, playerConnection, Context.BIND_AUTO_CREATE);
        isPlayerConnectionBound = true;

        playButton = (Button) (findViewById(R.id.btnPlay));
        playButton.setOnClickListener(PlayPauseClickListener);

        timerTextView = (TimerTextView)findViewById(R.id.timerTextView);


        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        volumeBar.setProgress(volumeBar.getMax());
        volumeBar.setOnSeekBarChangeListener(volumeChangeListener);

        final RadioGroup noiseTypes = (RadioGroup) findViewById(R.id.noiseTypes);

        if(noiseTypes != null) {
            noiseTypes.setOnCheckedChangeListener(noiseColorChangeListener);
        }

        ToggleButton oscillateButton = (ToggleButton) findViewById(R.id.waveVolumeToggle);
        if (oscillateButton != null) {
            oscillateButton.setOnCheckedChangeListener(toggleOscillation);
        }

        ToggleButton fadeButton = (ToggleButton) findViewById(R.id.decreaseVolumeToggle);
        if (fadeButton != null) {
            fadeButton.setOnCheckedChangeListener(toggleFade);
        }

        timerButton = (ImageButton) findViewById(R.id.timerButton);
        if (timerButton != null) {
            timerButton.setOnClickListener(timerButtonClicked);
        }

        whiteNoisePresenter.loadInstance(savedInstanceState);

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    View.OnClickListener PlayPauseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            whiteNoisePresenter.playPause();
        }
    };

    SeekBar.OnSeekBarChangeListener volumeChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float percentScrolled = (float) progress / seekBar.getMax();
            whiteNoisePresenter.setVolume(percentScrolled);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    RadioGroup.OnCheckedChangeListener noiseColorChangeListener =
            new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.noiseTypeWhite:
                    whiteNoisePresenter.setNoiseColor(WhiteNoisePresenter.NoiseColors.white);
                    break;
                case R.id.noiseTypePink:
                    whiteNoisePresenter.setNoiseColor(WhiteNoisePresenter.NoiseColors.pink);
                    break;
                case R.id.noiseTypeBrown:
                    whiteNoisePresenter.setNoiseColor(WhiteNoisePresenter.NoiseColors.brown);
                    break;
            }
        }
    };

    CompoundButton.OnCheckedChangeListener toggleOscillation =
            new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            whiteNoisePresenter.toggleOscillation(isChecked);
        }
    };

    CompoundButton.OnCheckedChangeListener toggleFade =
            new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            whiteNoisePresenter.toggleFade(isChecked);
        }
    };

    View.OnClickListener timerButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            whiteNoisePresenter.createCancelTimer();
        }
    };

    /** Create and show a timer picker */
    public void showPickerDialog() {
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
                whiteNoisePresenter.setTime(timeInMillis);
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
        whiteNoisePresenter.showAnyNotification();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //Pass on the bundle to the presenter so it can save anything relevant
        //this might be a messy implementation,
        //but the presenter does have important things to save
        whiteNoisePresenter.saveInstance(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        whiteNoisePresenter.updateDarkMode();
        whiteNoisePresenter.dismissAnyNotification();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isPlayerConnectionBound) {
            whiteNoisePresenter.dismissAnyNotification();
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
                startSettingsActivity(whiteNoisePresenter.getUseDarkMode());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startSettingsActivity(boolean useDarkMode) {
        Intent settingsIntent = new Intent(this,SettingsActivity.class);
        settingsIntent.putExtra(WhiteNoisePresenter.PREF_USE_DARK_MODE_KEY,useDarkMode);
        startActivity(settingsIntent);
    }
    /**
     * Visually show that there is no timer by hiding the timertextview and changing the  timer button
     * back to having a "+"
     */
    public void setTimerUIUnsetState() {
        //set button image
        Drawable addPic = getResources().getDrawable(R.drawable.ic_add);
        timerButton.setImageDrawable(addPic);
        timerTextView.setVisibility(View.GONE);
    }

    /**
     * Visually show there is a new timer by showing timer text view with time and changing
     * the timer button to having an "x"
     */
    public void setTimerUIAdded(long currTime) {
        Drawable stopPic = getResources().getDrawable(R.drawable.ic_clear);
        //change button to "clear"
        timerButton.setImageDrawable(stopPic);
        timerTextView.setTime(currTime);
        timerTextView.setVisibility(View.VISIBLE);
    }

    /** Set the time shown visually in the timerTextView*/
    public void setTime(long time) {
        timerTextView.setTime(time);
    }

    public void setPlayButtonPause() {
        Drawable playPic = getResources().getDrawable(R.drawable.ic_action_playback_pause_black);
        //convert from 120dp to pixels
        int picSizeInPixels = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        if (playPic != null) {
            playPic.setBounds(0, 0, picSizeInPixels, picSizeInPixels);
        }
        playButton.setCompoundDrawables(playPic, null, null, null);
        playButton.setText("Pause");
    }

    public void setPlayButtonPlay() {
        Drawable playPic = getResources().getDrawable(R.drawable.ic_action_playback_play_black);
        //convert from 120dp to pixels
        int picSizeInPixels = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        if (playPic != null) {
            playPic.setBounds(0, 0, picSizeInPixels, picSizeInPixels);
        }
        playButton.setCompoundDrawables(playPic, null, null, null);
        playButton.setText("Play");
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Explain to the user what oscillation is
     */
    public void explainOscillation() {
        Toast.makeText(MainActivity.this, "Wavy Volume", Toast.LENGTH_LONG).show();
    }

    /**
     * Explain to the user what fade is
     */
    public void explainFade() {
        Toast.makeText(MainActivity.this, "Fade Volume", Toast.LENGTH_LONG).show();
    }

    /**
     * Set the ProgressBar that displays volume
     */
    public void setVolume(float volume) {
       volumeBar.setProgress((int) (volumeBar.getMax() * volume));
    }

    /**
     * Handle connection to service, as soon as you get a connection
     * pass the connection off to the presenter
     */
    private ServiceConnection playerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            WhiteNoiseAudioService.AudioServiceBinder audioServiceBinder = (WhiteNoiseAudioService.AudioServiceBinder) binder;
            WhiteNoiseAudioService whiteNoiseAudioService = (WhiteNoiseAudioService)audioServiceBinder.getService();
            whiteNoisePresenter.setWhiteNoiseAudioService(whiteNoiseAudioService);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            whiteNoisePresenter.setWhiteNoiseAudioService(null);
        }
    };
}
