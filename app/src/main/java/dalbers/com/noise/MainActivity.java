package dalbers.com.noise;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Arrays;

/**
 * TODO:
 * -Need some way of showing which color noise is being used
 */

public class MainActivity extends AppCompatActivity {

    public static String AUDIO_RES_EXTRA_KEY = "audioRes";
    private boolean started = false;
    private AudioPlayer audioPlayerService;
    public static String LOG_TAG = "dalbers.noise/main";
    private CountDownTimer editTextCountDownTimer;
    private SeekBar volumeBar;
    private Button playButton;
    private EditText countdownTimeTextView;
    private ImageButton timerButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent serviceIntent = new Intent(MainActivity.this, AudioPlayer.class);
        bindService(serviceIntent, playerConnection, Context.BIND_AUTO_CREATE);
        playButton = (Button)(findViewById(R.id.btnPlay));

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(audioPlayerService != null) {
                    Resources res = MainActivity.this.getResources();
                    if(audioPlayerService.isPlaying()) {
                        audioPlayerService.stop();
                        Drawable playPic = res.getDrawable(R.drawable.ic_action_playback_play_black);
                        playPic.setBounds( 0, 0, 120, 120 );
                        ((Button)v).setCompoundDrawables(playPic, null,null,null);
                        ((Button)v).setText("Play");
                        pauseTimer();
                    }
                    else {
                        audioPlayerService.play();
                        Drawable playPic = res.getDrawable(R.drawable.ic_action_playback_pause_black);
                        playPic.setBounds( 0, 0, 120, 120 );
                        ((Button)v).setCompoundDrawables(playPic, null, null, null);
                        long time = timeStringToMillis(countdownTimeTextView.getText().toString());
                        ((Button)v).setText("Pause");
                        //timer was set before noise was playing,
                        //start the timer with the music
                        if(time != 0) {
                            startTimer(time);
                        }
                    }
                }
        }});

        countdownTimeTextView = (EditText)findViewById(R.id.countdownTime);
        countdownTimeTextView.setText(stringToFormattedHMS(millisToHMSZeros(0)));
        Spannable span = new SpannableString(countdownTimeTextView.getText());
        //make "h m s" smaller than numbers
        span.setSpan(new RelativeSizeSpan(0.6f),  2,  4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new RelativeSizeSpan(0.6f),  6,  8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new RelativeSizeSpan(0.6f), 10, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //apply text size changes
        countdownTimeTextView.setText(span);

        TextWatcher countdownTimeTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s){
                countdownTimeTextView.setSelection(11);
            }
            public void beforeTextChanged(CharSequence s,int start,int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                countdownTimeTextView.removeTextChangedListener(this);
                //update text
                countdownTimeTextView.setText(stringToFormattedHMS(s.toString()));
                Spannable span = new SpannableString(countdownTimeTextView.getText());
                //make "h m s" smaller than numbers
                span.setSpan(new RelativeSizeSpan(0.6f),  2,  4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                span.setSpan(new RelativeSizeSpan(0.6f),  6,  8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                if(audioPlayerService.isPlaying()) {
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

        volumeBar = (SeekBar)findViewById(R.id.volumeBar);

        volumeBar.setProgress(volumeBar.getMax());
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float percentScrolled = (float) progress / seekBar.getMax();
                if(audioPlayerService != null)
                    audioPlayerService.setMaxVolume(percentScrolled);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Button whiteButton = (Button)findViewById(R.id.whiteToggleButton);
        if(whiteButton != null) {
            whiteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    audioPlayerService.setSoundFile(R.raw.white);
                }
            });
        }

        Button pinkButton = (Button)findViewById(R.id.pinkToggleButton);
        if(pinkButton != null) {
            pinkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    audioPlayerService.setSoundFile(R.raw.pink);
                }
            });
        }

        Button brownButton = (Button)findViewById(R.id.brownToggleButton);
        if(brownButton != null) {
            brownButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    audioPlayerService.setSoundFile(R.raw.brown);
                }
            });
        }

        ToggleButton oscillateButton = (ToggleButton)findViewById(R.id.waveVolumeToggle);
        if(oscillateButton != null) {
            oscillateButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    audioPlayerService.setOscillateVolume(isChecked);
                }
            });
        }

        ToggleButton decreaseButton = (ToggleButton)findViewById(R.id.decreaseVolumeToggle);
        if(decreaseButton != null) {
            decreaseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    audioPlayerService.setDecreaseVolume(isChecked);
                }
            });
        }

        timerButton = (ImageButton)findViewById(R.id.timerButton);
        if(timerButton != null) {
            timerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopTimer();
                }
            });
        }
    }

    private ServiceConnection playerConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            AudioPlayer.AudioPlayerBinder audioPlayerBinder = (AudioPlayer.AudioPlayerBinder) binder;
            audioPlayerService = audioPlayerBinder.getService();
            float[] volumes = audioPlayerService.getVolume();
            volumeBar.setProgress((int)(volumeBar.getMax()*averageLRVolume(volumes[0],volumes[1])));
            Resources res = MainActivity.this.getResources();
            if(audioPlayerService.isPlaying()) {
                Drawable playPic = res.getDrawable(R.drawable.ic_action_playback_pause_black);
                playPic.setBounds( 0, 0, 120, 120 );
                playButton.setCompoundDrawables(playPic, null, null, null);
            }
            else {
                Drawable playPic = res.getDrawable(R.drawable.ic_action_playback_play_black);
                playPic.setBounds( 0, 0, 120, 120 );
                playButton.setCompoundDrawables(playPic, null, null, null);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            audioPlayerService = null;
        }
    };

    /**
     * Convert a string with hours mins seconds and any number of characters to milliseconds
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

    /**
     * Parse hours mins seconds from a string with variable number of numbers and chars
     * @param time a string with variable number of numbers and chars
     * @return array of ints where index 0 is hours, 1 is minutes, 2 is seconds
     */
    private int[] timeStringToHMS(String time) {
        String stripped = time.replaceAll("[^\\d]", "");
        int hrs = Integer.parseInt(stripped.substring( 0,2));
        int mins = Integer.parseInt(stripped.substring(2,4));
        int secs = Integer.parseInt(stripped.substring(4,6));

        mins += secs / 60;
        secs = secs % 60;

        hrs += mins / 60;
        mins = mins % 60;
        return new int[]{hrs,mins,secs};
    }

    /**
     * Converts milliseconds to hours minutes and seconds
     * Where there is a maximum of two digits for each and if there is only
     * one digit, a zero is added in front
     * if millis = 60000 this would return "000100" (1 minute)
     * @param millis milliseconds
     * @return string concatenated as hours+mins+seconds with zeros added appropriately
     */
    private String millisToHMSZeros(long millis) {
        int[] HMS = millisToHMS(millis);
        String formatted = "";
        if(HMS[0] < 10)
            formatted += "0" + HMS[0];
        else formatted += HMS[0];

        if(HMS[1] < 10)
            formatted += "0" + HMS[1];
        else formatted += HMS[1];

        if(HMS[2] < 10)
            formatted += "0" + HMS[2];
        else formatted += HMS[2];

        return formatted;
    }

    /**
     * Converts milliseconds to hours minutes and seconds
     * Where time 1000 would be 1 second, time 60000 would be 1 minute etc
     * @param millis milliseconds
     * @return array of ints where index 0 is hours, 1 is minutes, 2 is seconds
     */
    private int[] millisToHMS(long millis) {
        int secs =  (int)(millis/1000);
        int hrs = secs / (60 * 60);
        secs = secs % (60 * 60);
        int mins = secs / (60);
        secs = secs % 60;
        return new int[]{hrs,mins,secs};
    }

    /**
     * Given a string with a variable amount of numbers and chars
     * Format it to the hour, min, sec format that looks like "12h 34m 56s"
     * @param input  string with a variable amount of numbers and chars
     * @return formatted string that looks like "12h 34m 56s"
     */
    private String stringToFormattedHMS(String input) {
        //backspaced, removed the 's' but the user intended to remove last second number
        if(input.length() == 10)
            input = input.substring(0,9);
        //remove all non numbers
        String stripped = input.replaceAll("[^\\d]", "");
        //remove leading zeros
        while(stripped.length() > 0) {
            if(stripped.charAt(0) == '0')
                stripped = stripped.substring(1);
            else
                break;
        }
        //any number index >5
        stripped = stripped.substring(0,Math.min(stripped.length(),6));

        String preZeros = "";
        if(stripped.length() < 6) {
            char[] zeros = new char[6 - stripped.length()];
            Arrays.fill(zeros, '0');
            preZeros = new String(zeros);
        }
        String fullNums = preZeros + stripped;
        //format in 12h 34m 56s
        return  fullNums.substring(0,2) + "h " +
                fullNums.substring(2,4) + "m " +
                fullNums.substring(4,6) + "s";

    }

    /**
     * Get an average of the left and right volumes in case they're different
     * @param left 0f to 1.0f left volume
     * @param right 0f to 1.0f right volume
     * @return 0f to 1.0f averaged volume
     */
    private float averageLRVolume(float left, float right) {
        return (left + right) /2;
    }

    private void startTimer(long time) {
        if (editTextCountDownTimer != null)
            editTextCountDownTimer.cancel();
        editTextCountDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                countdownTimeTextView.setText(millisToHMSZeros(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                countdownTimeTextView.setText(millisToHMSZeros(0));
            }
        };
        audioPlayerService.setTimer(time);
        editTextCountDownTimer.start();
    }
    private void pauseTimer() {
        if(editTextCountDownTimer != null)
            editTextCountDownTimer.cancel();
        audioPlayerService.cancelTimer();
    }

    private void stopTimer() {
        //pause the timer and set the time to zero
        if(editTextCountDownTimer != null)
            editTextCountDownTimer.cancel();
        countdownTimeTextView.setText(stringToFormattedHMS(millisToHMSZeros(0)));
        Log.d(LOG_TAG,"stopped timer");
    }
}
