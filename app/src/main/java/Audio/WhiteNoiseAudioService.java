package audio;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import dalbers.com.noise.R;

/**
 * A modified BaseLoopAudioService that adds
 *  timer and oscillating and decreasing volume.
 */
public class WhiteNoiseAudioService extends BaseLoopAudioService
        implements WhiteNoiseAudioInterface {
    private static String LOG_TAG = "dalbers.noise:audioPlayer";
    private long millisLeft = 0;
    private CountDownTimer countDownTimer;
    private float leftVolume = 0.5f;
    private float rightVolume = 0.5f;
    private boolean oscillateVolume = false;
    private boolean oscillateVolumeStereo = true;
    private boolean oscillatingDown = true;
    private boolean oscillatingLeft = true;
    private boolean decreaseVolume = false;
    /**
     * If using oscillate or decrease, the min volume will be the max multiplied by this value
     */
    private float minVolumePercent = .2f;
    /**
     * One complete oscillation from left to right will happen in this interval
     */
    private long oscillatePeriod = 8000;
    /**
     * Interval the volume timer updates
     * There doesn't seem to be a performance hit at this interval
     * and it's fast enough that I can't hear it
     */
    private long tickPeriod = 100;
    /**
     * how long the sound will be decreasing
     * should go from max to min in this time period
     * ideally, this is == timer time
     */
    private long decreaseLength = -1;
    /**
     * 1 hour in milliseconds use this instead of decreaseLength
     * if no timer exists
     * */
    public static long DEFAULT_DECREASE_LENGTH = 3600000;
    /**
     * the maximum allowable volume given the current state
     * if only using oscillate, this should be == initialVolume
     * if using decrease, this will decrease
     */
    private float maxVolume = 1.0f;
    /**
     * Volume set by the user before oscillation or other things affected it
     */
    private float initialVolume = 1.0f;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Timer volumeChangerTimer = new Timer();
        volumeChangerTimer.schedule(new VolumeChangerTimerTask(), 0, tickPeriod);
    }


    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        //if a button is pressed in the notification,
        //the service will be started with this extra
        if(intent != null) {
            if (intent.hasExtra("do")) {
                String action = (String) intent.getExtras().get("do");
                if (action.equals("pause")) {
                    Log.d(LOG_TAG, "paused");
                    pause();
                    //there's no way to pause the timer
                    //just cancel it and start a new one if play is pressed
                    cancelTimer();
                    showNotification(false);
                } else if (action.equals("play")) {
                    Log.d(LOG_TAG, "playing");
                    play();
                    //there was a timer before pause was pressed
                    //start it again with the leftover time
                    if (millisLeft > 0)
                        setTimer(millisLeft);
                    showNotification(true);
                } else if (action.equals("close")) {
                    stop();
                    stopTimer();
                }
            }
        }
        return super.onStartCommand(intent,flags,startId);
    }

    /**
     * Set the maximum volume for this service.
     * If oscillating or decreasing volume are not being used,
     * this will be the volume. If they are being used, neither
     * will use a volume higher than this volume
     * @param maxVolume a value 0.0 to 1.0 where 1.0 is max of the device
     */
    public void setMaxVolume(float maxVolume) {
        this.maxVolume = maxVolume;
        leftVolume = maxVolume;
        rightVolume = maxVolume;
        oscillatingDown = true;
        initialVolume = maxVolume;
        mp.setVolume(maxVolume, maxVolume);
//        Log.d(LOG_TAG, maxVolume + "");
    }

    /**
     * Set the volume of the media player. This will not update this service's
     * volume or associated variables, use setMaxVolume() to do that.
     * @param leftVolume a value 0.0 to 1.0 where 1.0 is max of the device
     * @param rightVolume a value 0.0 to 1.0 where 1.0 is max of the device
     */
    private void setVolume(float leftVolume, float rightVolume) {
        mp.setVolume(leftVolume, rightVolume);
    }


    public void setTimer(final long millis) {
        millisLeft = millis;
        decreaseLength = millis;
        if(countDownTimer != null)
            countDownTimer.cancel();
        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisLeft = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                dismissNotification();
                millisLeft = 0;
                mp.stop();
            }
        }.start();
    }

    public void cancelTimer() {
        if(countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void stopTimer() {
        cancelTimer();
        decreaseLength = -1;
        millisLeft = 0;
    }

    public long getTimeLeft() {
        return millisLeft;
    }

    @Override
    public void setSoundFile(int resId) {
        super.setSoundFile(resId);
        mp.setVolume(maxVolume,maxVolume);
    }

    public void setOscillateVolume(boolean oscillateVolume) {
        this.oscillateVolume = oscillateVolume;
    }

    public void setDecreaseVolume(boolean decreaseVolume) {
        this.decreaseVolume = decreaseVolume;
    }

    Runnable volumeRunnable = new Runnable() {
        @Override
        public void run() {
            if(mp != null && mp.isPlaying()) {
                if(oscillateVolume) {
                    if(decreaseVolume)
                        decreaseForTick();
                    if(oscillateVolumeStereo)
                        oscillateStereoForTick();
                    else
                        oscillateForTick();
                    setVolume(leftVolume, rightVolume);
                }
                else if(decreaseVolume) {
                    decreaseForTick();
                    leftVolume = maxVolume;
                    rightVolume = maxVolume;
                    setVolume(leftVolume,rightVolume);
                }
            }
        }
    };

    /**
     * Oscillate volume from masterVolume to (masterVolume*minVolumePercent)
     * Going from max to min to max takes 'oscillatePeriod' milliseconds
     */
    public void oscillateForTick() {
        float minVolume = (initialVolume * minVolumePercent);
        float delta = (maxVolume-minVolume) / (oscillatePeriod / 2 / tickPeriod);
        if(oscillatingDown)
            delta = -1 * delta;
        leftVolume += delta;
        rightVolume += delta;
        if(leftVolume <= minVolume || rightVolume <= minVolume) {
            leftVolume = minVolume;
            rightVolume = minVolume;
            oscillatingDown = false;
        }
        if(leftVolume >= maxVolume || rightVolume >= maxVolume) {
            leftVolume = maxVolume;
            rightVolume = maxVolume;
            oscillatingDown = true;
        }
        Log.d(LOG_TAG, leftVolume + "," + rightVolume);
    }

    /**
     * Oscillates volume in one speaker and then the other
     * Example
     * left:  1.0 -> .2  -> 1.0
     * right: stays at 1.0
     * then
     * left:  stays at 1.0
     * right: 1.0 -> 2.0 -> 1.0
     */
    public void oscillateStereoForTick() {
        float minVolume = (initialVolume * minVolumePercent);
        float delta = (maxVolume-minVolume) / (oscillatePeriod / 2 / tickPeriod);
        if(oscillatingDown)
            delta = -1 * delta;
        if(oscillatingLeft)
            leftVolume += delta;
        else
            rightVolume += delta;
        if(leftVolume <= minVolume || rightVolume <= minVolume) {
            if(oscillatingLeft)
                leftVolume = minVolume;
            else
                rightVolume = minVolume;
            oscillatingDown = false;
        }
        if(leftVolume >= maxVolume && rightVolume >= maxVolume && !oscillatingDown) {
            if(oscillatingLeft)
                leftVolume = maxVolume;
            else
                rightVolume = maxVolume;
            oscillatingDown = true;
            oscillatingLeft = !oscillatingLeft;
        }
        Log.d(LOG_TAG,leftVolume+","+rightVolume);
    }

    public void decreaseForTick() {

        float minVolume = (initialVolume * minVolumePercent);
        if(maxVolume > minVolume) {
            float delta = 0.0f;
            if (decreaseLength == -1)
                delta = -1 * (maxVolume - minVolume) / (DEFAULT_DECREASE_LENGTH / tickPeriod);
            else
                delta = -1 * (maxVolume - minVolume) / (decreaseLength / tickPeriod);
            maxVolume += delta;
        }

        Log.d(LOG_TAG,maxVolume+"");
    }


    class VolumeChangerTimerTask extends TimerTask {
        @Override
        public void run() {
            Handler volumeHandler = new Handler(Looper.getMainLooper());
            volumeHandler.post(volumeRunnable);
        }
    }

    public void setOscillatePeriod(long oscillatePeriod) {
        this.oscillatePeriod = oscillatePeriod;
    }

    /**
     * Show a notification with information about the sound being played/paused
     * and a pause button which will callback to this service
     */
    public void showNotification(boolean playing) {
        String title;
        //which noise is playing?
        switch (mp.getSoundFile())
        {
            case R.raw.white:
                title = "White Noise";
                break;
            case R.raw.brown:
                title = "Brown Noise";
                break;
            case R.raw.pink:
                title = "Pink Noise";
                break;
            default:
                title = "Noise";
        }
        Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.ic_launcher);
        super.showNotification(playing,icon,title);
    }
}

