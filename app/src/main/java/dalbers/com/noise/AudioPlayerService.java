package dalbers.com.noise;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A service to play audio on a loop.
 * Features oscillating and decreasing volume.
 * Uses LoopMediaPlayer for looping audio.
 */
public class AudioPlayerService extends Service {
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
    private Notification notification;
    private MediaSessionCompat mediaSession;
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
        mediaSession = new MediaSessionCompat(this,"white noise");
    }
    public void onDestroy()
    {
        mp.stop();
    }

    public int onStartCommand(Intent intent,int flags, int startId){
        mp = LoopMediaPlayer.create(this, R.raw.white);
        if(intent.hasExtra("DO")) {
            String action = (String) intent.getExtras().get("DO");
            if (action.equals("pause")) {
               Log.d(LOG_TAG,"paused");
            }
        }

        return START_STICKY;
    }

    public class AudioPlayerBinder extends Binder {
        AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    public void play() {
        mp.play();
    }

    public void stop() {
        if(mp != null)
            mp.stop();
        //reset volumes to initial values
        leftVolume = initialVolume;
        rightVolume = initialVolume;
    }

    public boolean isPlaying() {
        if(mp != null)
            return mp.isPlaying();
        else return false;
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
        Log.d(LOG_TAG,maxVolume+"");
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

        if(countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public long getTimeLeft() {
        return millisLeft;
    }

    public void setSoundFile(int resId) {
        mp.setSoundFile(resId);
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

    public void showNotificationWidget() {
        String title;
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
                R.drawable.ic_action_add);
        Intent volume = new Intent(this,AudioPlayerService.class);
        volume.putExtra("DO", "pause");
        PendingIntent btn1 = PendingIntent.getService(this, 0, volume, 0);
        notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_add)
                .setContentTitle(title)
                .setContentText("Playing in Noise App")
                .setLargeIcon(icon)
                .setStyle(new NotificationCompat.MediaStyle())
                .setOngoing(true)
                .addAction(R.drawable.ic_add,"add",btn1)
                .build();
        NotificationManager nManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(2, notification);
    }

}

