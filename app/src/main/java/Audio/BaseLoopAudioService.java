package audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import dalbers.com.noise.MainActivity;
import dalbers.com.noise.R;

/**
 * Provides basic functionality for playing audio on a loop.
 * Created by davidalbers on 10/17/16.
 */
abstract class BaseLoopAudioService extends Service implements BaseLoopAudioInterface {
    protected LoopMediaPlayer mp;
    private IBinder mBinder = new AudioServiceBinder();

    /**
     * Used when showing the notification,
     * unique within the app,
     * if multiple notify()s are called with the same id,
     * new one will replace the old ones
     */
    private final int NOTIFICATION_ID = 0;

    @Override
    public IBinder onBind(Intent intent) {
        if(mp == null) {
            mp = LoopMediaPlayer.create(this);
        }
        return mBinder;
    }

    @Override
    public void onDestroy()
    {
        mp.stop();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        if(mp == null)
            mp = LoopMediaPlayer.create(this);
        return START_STICKY;
    }


    @Override
    public void onCreate()
    {
        //create any class objects here
    }

    public void play() {
        mp.play();
    }

    public void stop() {
        if(mp != null)
            mp.stop();
    }

    public void pause() {
        if(mp != null)
            mp.pause();
    }

    public void setVolume(float volume) {
        mp.setVolume(volume, volume);
    }

    public void setSoundFile(int resId) {
        mp.setSoundFile(resId);
    }

    public int getSoundFile() {
        return mp.getSoundFile();
    }

    public float getVolume() {
        //average of left and right volume
        return (mp.getVolume()[0] + mp.getVolume()[1]) / 2;
    }

    public class AudioServiceBinder extends Binder {
        public BaseLoopAudioService getService() {
            return BaseLoopAudioService.this;
        }
    }

    public boolean isPlaying() {
        if(mp != null)
            return mp.isPlaying();
        else return false;
    }


    /**
     * Show a notification with information about the sound being played/paused
     * and a pause button which will callback to this service
     */
    public void showNotification(boolean playing, Bitmap icon, String title) {
        //create the notification
        Notification notification;
        if(playing) {
            Intent pausePlayIntent = new Intent(this,WhiteNoiseAudioService.class);
            pausePlayIntent.putExtra("do", "pause");
            PendingIntent pausePlayPI = PendingIntent.getService(this, 0, pausePlayIntent, PendingIntent.FLAG_ONE_SHOT);
            Intent closeIntent = new Intent(this,WhiteNoiseAudioService.class);
            closeIntent.setAction("close");
            closeIntent.putExtra("do", "close");
            PendingIntent closePI = PendingIntent.getService(getApplicationContext(), 0, closeIntent, PendingIntent.FLAG_ONE_SHOT);
            Intent openAppIntent = new Intent(getApplicationContext(),MainActivity.class);
            openAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            openAppIntent.putExtra("startedFromNotification", true);
            PendingIntent openAppPI = PendingIntent.getActivity(getApplicationContext(),0, openAppIntent, PendingIntent.FLAG_ONE_SHOT);
            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_statusbar2)
                    .setContentTitle(title)
                    .setContentText("Playing")
                    .setLargeIcon(icon)
                    .setStyle(new NotificationCompat.MediaStyle())
                    .setOngoing(true)
                    .addAction(R.drawable.ic_action_playback_pause_black, "Pause", pausePlayPI)
                    .addAction(R.drawable.ic_clear, "Close", closePI)
                    .setContentIntent(openAppPI)
                    .build();
        }
        else {
            Intent pausePlayIntent = new Intent(this,WhiteNoiseAudioService.class);
            pausePlayIntent.setAction("play");
            pausePlayIntent.putExtra("do", "play");
            PendingIntent pausePlayPI = PendingIntent.getService(this, 0, pausePlayIntent, PendingIntent.FLAG_ONE_SHOT);
            Intent closeIntent = new Intent(this,WhiteNoiseAudioService.class);
            closeIntent.setAction("close");
            closeIntent.putExtra("do", "close");
            PendingIntent closePI = PendingIntent.getService(this, 0, closeIntent, PendingIntent.FLAG_ONE_SHOT);
            Intent openAppIntent = new Intent(getApplicationContext(), MainActivity.class);
            openAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            openAppIntent.putExtra("startedFromNotification", true);
            PendingIntent openAppPI = PendingIntent.getActivity(getApplicationContext(), 0, openAppIntent, PendingIntent.FLAG_ONE_SHOT);
            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_statusbar2)
                    .setContentTitle(title)
                    .setContentText("Paused")
                    .setLargeIcon(icon)
                    .setStyle(new NotificationCompat.MediaStyle())
                    .setOngoing(true)
                    .addAction(R.drawable.ic_action_playback_play_black, "Play", pausePlayPI)
                    .addAction(R.drawable.ic_clear, "Close", closePI)
                    .setContentIntent(openAppPI)
                    .build();
        }
        //show the notification
        NotificationManager nManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, notification);
    }

    public void dismissNotification() {
        ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
    }
}
