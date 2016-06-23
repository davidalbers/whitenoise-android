package dalbers.com.noise;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by davidalbers on 4/10/16.
 */
public class LoopMediaPlayer {

    public static final String TAG = LoopMediaPlayer.class.getSimpleName();

    private Context mContext = null;
    private int mResId = 0;
    private int mCounter = 1;

    private MediaPlayer mCurrentPlayer = null;
    private MediaPlayer mNextPlayer = null;

    public static LoopMediaPlayer create(Context context, int resId) {
        LoopMediaPlayer looper = new LoopMediaPlayer(context);
        looper.mResId = resId;
        return looper;
    }

    private LoopMediaPlayer(Context context) {
        mContext = context;
    }

    private float leftVolume = 1.0f;
    private float rightVolume = 1.0f;

    private void createNextMediaPlayer() {
        mNextPlayer = MediaPlayer.create(mContext, mResId);

        mCurrentPlayer.setNextMediaPlayer(mNextPlayer);
        mCurrentPlayer.setOnCompletionListener(onCompletionListener);
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.release();
            mCurrentPlayer = mNextPlayer;

            mCurrentPlayer.setVolume(leftVolume, rightVolume);

            createNextMediaPlayer();

            Log.d(TAG, String.format("Loop #%d", ++mCounter));
        }
    };

    public void stop() {
        if(mCurrentPlayer != null)
            mCurrentPlayer.stop();
    }

    public void pause() {
        if(mCurrentPlayer != null)
            mCurrentPlayer.pause();
    }

    public void play() {
        mCurrentPlayer = MediaPlayer.create(mContext, mResId);
        mCurrentPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mCurrentPlayer.setVolume(leftVolume, rightVolume);
                mCurrentPlayer.start();
            }
        });

        createNextMediaPlayer();
    }

    public void setSoundFile(int resId) {
        //reset if a different file
        if(resId != mResId) {
            boolean wasPlaying = false;
            if(mCurrentPlayer != null) {
                wasPlaying = mCurrentPlayer.isPlaying();
                stop();
            }
            mResId = resId;
            if((mCurrentPlayer != null) && wasPlaying)
                play();
        }
    }

    public int getSoundFile() {
        return mResId;
    }

    public boolean isPlaying() {
        if(mCurrentPlayer == null) return false;
        return mCurrentPlayer.isPlaying();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        if(mCurrentPlayer != null) {
            mCurrentPlayer.setVolume(leftVolume, rightVolume);
        }
        this.leftVolume = leftVolume;
        this.rightVolume = rightVolume;

    }

    public float[] getVolume() { return new float[]{leftVolume, rightVolume}; }


}
