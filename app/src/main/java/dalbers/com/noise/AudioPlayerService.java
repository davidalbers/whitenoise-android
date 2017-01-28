package dalbers.com.noise;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * A service to play audio on a loop.
 * Features oscillating and decreasing volume.
 * Uses LoopMediaPlayer for looping audio.
 */
public class AudioPlayerService extends Service {

    /**
     * 1 hour in milliseconds use this instead of decreaseLength
     * if no timer exists
     */
    public static final long DEFAULT_DECREASE_LENGTH = 3600000;
    private static final String LOG_TAG = AudioPlayerService.class.getSimpleName();
    /**
     * Used when showing the notification,
     * unique within the app,
     * if multiple notify()s are called with the same id,
     * new one will replace the old ones
     */
    private static final int NOTIFICATION_ID = 0;
    private LoopMediaPlayer mp;
    private IBinder mBinder = new AudioPlayerBinder();
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
     * the maximum allowable volume given the current state
     * if only using oscillate, this should be == initialVolume
     * if using decrease, this will decrease
     */
    private float maxVolume = 1.0f;
    /**
     * Volume set by the user before oscillation or other things affected it
     */
    private float initialVolume = 1.0f;
    Runnable volumeRunnable = new Runnable() {
        @Override
        public void run() {
            if (mp != null && mp.isPlaying()) {
                if (oscillateVolume) {
                    if (decreaseVolume)
                        decreaseForTick();
                    if (oscillateVolumeStereo)
                        oscillateStereoForTick();
                    else
                        oscillateForTick();
                    setVolume(leftVolume, rightVolume);
                } else if (decreaseVolume) {
                    decreaseForTick();
                    leftVolume = maxVolume;
                    rightVolume = maxVolume;
                    setVolume(leftVolume, rightVolume);
                }
            }
        }

        /**
         * Set the volume of the media player. This will not update this service's
         * volume or associated variables, use setMaxVolume() to do that.
         *
         * @param leftVolume  a value 0.0 to 1.0 where 1.0 is max of the device
         * @param rightVolume a value 0.0 to 1.0 where 1.0 is max of the device
         */
        private void setVolume(float leftVolume, float rightVolume) {

            mp.setVolume(leftVolume, rightVolume);
        }
    };

    private static final String PAUSE_ACTION = "pause";
    private static final String CLOSE_ACTION = "close";
    private static final String PLAY_ACTION = "play";
    private static final String DO_ACTION = "do";

    /**
     * If the player loses focus and it was playing, then set this true
     * When and if we regain focus, you know to start playing again if this is true
     */
    private boolean startPlayingWhenFocusRegained = false;

    @Override
    public IBinder onBind(Intent intent) {
        if (mp == null) {
            mp = LoopMediaPlayer.create(this);
        }
        return mBinder;
    }

    @Override
    public void onCreate() {
        Timer volumeChangerTimer = new Timer();
        volumeChangerTimer.schedule(new VolumeChangerTimerTask(), 0, tickPeriod);
    }

    @Override
    public void onDestroy() {
        mp.stop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mp == null)
            mp = LoopMediaPlayer.create(this);

        //if a button is pressed in the notification,
        //the service will be started with this extra
        if (intent != null && intent.hasExtra(DO_ACTION)) {
            String action = (String) intent.getExtras().get(DO_ACTION);
            if (action != null) {
                switch(action) {
                    case PAUSE_ACTION:
                        handleNotificationPause();
                        break;
                    case PLAY_ACTION:
                        handleNotificationPlay();
                        break;
                    case CLOSE_ACTION:
                        handleNotificationClose();
                        break;
                    default:
                        break;
                }
            }
        }

        return START_STICKY;
    }

    AudioManager.OnAudioFocusChangeListener focusChangeListener =
        new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            boolean playOverOtherSound = getDefaultSharedPreferences(getBaseContext())
                    .getBoolean(MainActivity.PREF_PLAY_OVER, false);
            if (focusChange > 0 && startPlayingWhenFocusRegained) {
                play();
                startPlayingWhenFocusRegained = false;
            }
            else if (!playOverOtherSound && focusChange < 0 && isPlaying()) {
                //we lost audio focus, stop playing
                pause();
                if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                        || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    //in these cases, we can expect to get audio back soon
                    startPlayingWhenFocusRegained = true;
                }
            }
        }
    };

    private void handleNotificationPause() {
        pause();
        //there's no way to pause the timer
        //just cancel it and start a new one (with the old time) if play is pressed
        cancelTimer();
        showNotification(false);
    }

    public void handleNotificationPlay() {
        play();
        //there was a timer before pause was pressed
        //start it again with the leftover time
        if (millisLeft > 0)
            setTimer(millisLeft);
        showNotification(true);
    }

    public void handleNotificationClose() {
        stop();
        stopTimer();
        dismissNotification();
    }

    public void play() {
        mp.play();
        if (!getDefaultSharedPreferences(getBaseContext())
                .getBoolean(MainActivity.PREF_PLAY_OVER, false)) {
            //every time we start playing, we have to request audio focus and listen for other
            //apps also listening for focus with the focusChangeListener
            Log.d(LOG_TAG, "Getting audio focus");
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    public void stop() {
        if (mp != null)
            mp.stop();
        abandonAudioFocus();
        //reset volumes to initial values
        leftVolume = initialVolume;
        rightVolume = initialVolume;
    }


    public void pause() {
        if (mp != null)
            mp.pause();
        abandonAudioFocus();
    }

    private void abandonAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(focusChangeListener);
    }

    public boolean isPlaying() {
        return mp != null && mp.isPlaying();
    }

    public void dismissNotification() {
        ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
    }

    /**
     * Set the maximum volume for this service.
     * If oscillating or decreasing volume are not being used,
     * this will be the volume. If they are being used, neither
     * will use a volume higher than this volume
     *
     * @param maxVolume a value 0.0 to 1.0 where 1.0 is max of the device
     */
    public void setMaxVolume(float maxVolume) {
        this.maxVolume = maxVolume;
        leftVolume = maxVolume;
        rightVolume = maxVolume;
        oscillatingDown = true;
        initialVolume = maxVolume;
        mp.setVolume(maxVolume, maxVolume);
        Log.d(LOG_TAG, Float.toString(maxVolume));
    }


    public float[] getVolume() {
        return new float[]{leftVolume, rightVolume};
    }

    public void setTimer(final long millis) {
        millisLeft = millis;
        decreaseLength = millis;
        if (countDownTimer != null)
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
        if (countDownTimer != null) {
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


    public void setSoundFile(@RawRes int resId) {
        mp.setSoundFile(resId);
        mp.setVolume(maxVolume, maxVolume);
    }

    public void setOscillateVolume(boolean oscillateVolume) {
        this.oscillateVolume = oscillateVolume;
    }

    public void setDecreaseVolume(boolean decreaseVolume) {
        this.decreaseVolume = decreaseVolume;
    }

    /**
     * Oscillate volume from masterVolume to (masterVolume*minVolumePercent)
     * Going from max to min to max takes 'oscillatePeriod' milliseconds
     */
    public void oscillateForTick() {
        float minVolume = initialVolume * minVolumePercent;
        float delta = (maxVolume - minVolume) / (oscillatePeriod / 2 / tickPeriod);
        if (oscillatingDown)
            delta = -1 * delta;
        leftVolume += delta;
        rightVolume += delta;
        if (leftVolume <= minVolume || rightVolume <= minVolume) {
            leftVolume = minVolume;
            rightVolume = minVolume;
            oscillatingDown = false;
        }
        if (leftVolume >= maxVolume || rightVolume >= maxVolume) {
            leftVolume = maxVolume;
            rightVolume = maxVolume;
            oscillatingDown = true;
        }
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
        float minVolume = initialVolume * minVolumePercent;
        float delta = (maxVolume - minVolume) / (oscillatePeriod / 2 / tickPeriod);
        if (oscillatingDown)
            delta = -1 * delta;
        if (oscillatingLeft)
            leftVolume += delta;
        else
            rightVolume += delta;
        if (leftVolume <= minVolume || rightVolume <= minVolume) {
            if (oscillatingLeft)
                leftVolume = minVolume;
            else
                rightVolume = minVolume;
            oscillatingDown = false;
        }
        if (leftVolume >= maxVolume && rightVolume >= maxVolume && !oscillatingDown) {
            if (oscillatingLeft)
                leftVolume = maxVolume;
            else
                rightVolume = maxVolume;
            oscillatingDown = true;
            oscillatingLeft = !oscillatingLeft;
        }
    }

    @RawRes public int getSoundFile() {
        return mp.getSoundFile();
    }

    public void decreaseForTick() {
        float minVolume = initialVolume * minVolumePercent;
        if (maxVolume > minVolume) {
            float delta;
            if (decreaseLength == -1)
                delta = -1 * (maxVolume - minVolume) / (DEFAULT_DECREASE_LENGTH / tickPeriod);
            else
                delta = -1 * (maxVolume - minVolume) / (decreaseLength / tickPeriod);
            maxVolume += delta;
        }

        Log.d(LOG_TAG, Float.toString(maxVolume));
    }

    public void setOscillatePeriod(long oscillatePeriod) {
        this.oscillatePeriod = oscillatePeriod;
    }

    /**
     * Show a notification with information about the sound being played/paused
     * and a pause button which will callback to this service
     */
    public void showNotification(boolean playing) {
        @StringRes int titleRes;
        //which noise is playing?
        switch (mp.getSoundFile()) {
            case R.raw.brown:
                titleRes = R.string.notification_brown_type;
                break;
            case R.raw.pink:
                titleRes = R.string.notification_pink_type;
                break;
            default:
                titleRes = R.string.notification_white_type;
                break;
        }
        String title = getString(titleRes);
        Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.ic_launcher);
        //make an intent to callback this service when the pause button is pressed

        //create the notification
        Notification notification;
        if (playing) {
            Intent pausePlayIntent = new Intent(this, AudioPlayerService.class);
            pausePlayIntent.putExtra(DO_ACTION, PAUSE_ACTION);
            PendingIntent pausePlayPI =
                    PendingIntent.getService(this, 0, pausePlayIntent, PendingIntent.FLAG_ONE_SHOT);
            Intent closeIntent = new Intent(this, AudioPlayerService.class);
            closeIntent.setAction(CLOSE_ACTION);
            closeIntent.putExtra(DO_ACTION, CLOSE_ACTION);
            PendingIntent closePI = PendingIntent.getService(getApplicationContext(),
                    0,
                    closeIntent,
                    PendingIntent.FLAG_ONE_SHOT);
            Intent openAppIntent = new Intent(getApplicationContext(), MainActivity.class);
            openAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            openAppIntent.putExtra("startedFromNotification", true);
            PendingIntent openAppPI = PendingIntent.getActivity(getApplicationContext(),
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder builder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_statusbar2)
                    .setContentTitle(title)
                    .setContentText(getString(R.string.notification_playing))
                    .setLargeIcon(icon)
                    .setStyle(new NotificationCompat.MediaStyle())
                    .setOngoing(true)
                    .addAction(R.drawable.ic_action_playback_pause_black,
                            getString(R.string.audio_pause), pausePlayPI)
                    .addAction(R.drawable.ic_clear, getString(R.string.notification_close), closePI)
                    .setContentIntent(openAppPI)
                    .setPriority(Notification.PRIORITY_MAX);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }
            notification = builder.build();
        } else {
            Intent pausePlayIntent = new Intent(this, AudioPlayerService.class);
            pausePlayIntent.setAction(PLAY_ACTION);
            pausePlayIntent.putExtra(DO_ACTION, PLAY_ACTION);
            PendingIntent pausePlayPI = PendingIntent.getService(this,
                    0,
                    pausePlayIntent,
                    PendingIntent.FLAG_ONE_SHOT);
            Intent closeIntent = new Intent(this, AudioPlayerService.class);
            closeIntent.setAction(CLOSE_ACTION);
            closeIntent.putExtra(DO_ACTION, CLOSE_ACTION);
            PendingIntent closePI = PendingIntent.getService(this,
                    0,
                    closeIntent,
                    PendingIntent.FLAG_ONE_SHOT);
            Intent openAppIntent = new Intent(getApplicationContext(), MainActivity.class);
            openAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            openAppIntent.putExtra("startedFromNotification", true);
            PendingIntent openAppPI = PendingIntent.getActivity(getApplicationContext(),
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_ONE_SHOT);
            NotificationCompat.Builder builder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_statusbar2)
                    .setContentTitle(title)
                    .setContentText(getString(R.string.notification_paused))
                    .setLargeIcon(icon)
                    .setStyle(new NotificationCompat.MediaStyle())
                    .setOngoing(true)
                    .addAction(R.drawable.ic_action_playback_play_black,
                            getString(R.string.audio_play), pausePlayPI)
                    .addAction(R.drawable.ic_clear, getString(R.string.notification_close), closePI)
                    .setContentIntent(openAppPI)
                    .setPriority(Notification.PRIORITY_MAX);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }
            notification = builder.build();

        }
        //show the notification
        NotificationManager nManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, notification);
    }

    public class AudioPlayerBinder extends Binder {
        AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    class VolumeChangerTimerTask extends TimerTask {
        @Override
        public void run() {
            Handler volumeHandler = new Handler(Looper.getMainLooper());
            volumeHandler.post(volumeRunnable);
        }
    }
}
