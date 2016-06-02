package dalbers.com.noise;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class AudioPlayer extends Service {
    private LoopMediaPlayer mp;
    private static String LOG_TAG = "dalbers.noise:audioPlayer";
    private IBinder mBinder = new AudioPlayerBinder();
    private long millisLeft = 0;
    private CountDownTimer countDownTimer;
    private float leftVolume = 1.0f;
    private float rightVolume = 1.0f;
    private Timer volumeChangerTimer;
    private boolean oscillateVolume = false;
    private boolean oscillateVolumeStereo = true;
    private boolean oscillatingDown = true;
    private boolean oscillatingLeft = true;
    private boolean decreaseVolume = false;
    private float minVolumePercent = .2f;
    private long oscillatePeriod = 8000;
    private long tickPeriod = 100;
    private long decreaseLength = 60000;
    public static long DEFAULT_DECREASE_LENGTH = 6000;
    private float maxVolume = 1.0f;
    private float initialVolume = 1.0f;
    @Override
    public IBinder onBind(Intent intent) {
        if(mp == null) {
            mp = LoopMediaPlayer.create(this, R.raw.white);
        }
        return mBinder;
    }

    public void onCreate()
    {
        volumeChangerTimer = new Timer();
        volumeChangerTimer.schedule(new VolumeChangerTimerTask(), 0, tickPeriod);
    }
    public void onDestroy()
    {
        mp.stop();
    }

    public int onStartCommand(Intent intent,int flags, int startId){
        Log.d(LOG_TAG,"startCommand");
        mp = LoopMediaPlayer.create(this, R.raw.white);
        return START_STICKY;
    }

    public class AudioPlayerBinder extends Binder {
        AudioPlayer getService() {
            return AudioPlayer.this;
        }
    }

    public void play() {
        mp.play();
    }

    public void stop() {
        mp.stop();
        //reset volumes to initial values
        leftVolume = initialVolume;
        rightVolume = initialVolume;
    }

    public boolean isPlaying() {
        return mp.isPlaying();
    }

    public void setMaxVolume(float maxVolume) {
        this.maxVolume = maxVolume;
        leftVolume = maxVolume;
        rightVolume = maxVolume;
        oscillatingDown = true;
        initialVolume = maxVolume;
        mp.setVolume(maxVolume, maxVolume);
    }

    public void setVolume(float leftVolume, float rightVolume) {

        mp.setVolume(leftVolume, rightVolume);
    }

    public float[] getVolume() { return new float[] {leftVolume,rightVolume}; }

    public void setTimer(final long millis) {
        millisLeft = millis;
        if(countDownTimer != null)
            countDownTimer.cancel();
        countDownTimer = new CountDownTimer(millis, 1000) {

            public void onTick(long millisUntilFinished) {
                millisLeft = millisUntilFinished;
            }

            public void onFinish() {
                mp.stop();
            }
        }.start();
    }

    public void cancelTimer() {
        countDownTimer.cancel();
    }

    public long getTimeLeft() {
        return millisLeft;
    }

    public void setSoundFile(int resId) {
        mp.setSoundFile(resId);
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

}
