package dalbers.com.noise;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by davidalbers on 4/10/16.
 * Plays an audio file on repeat
 */
class LoopMediaPlayer {

    private static final String TAG = LoopMediaPlayer.class.getSimpleName();

    private Context mContext = null;
    private int mResId = NO_SOUND_FILE;
    private int mCounter = 1;
    static final int NO_SOUND_FILE = -1;

    private MediaPlayer mCurrentPlayer = null;
    private MediaPlayer mNextPlayer = null;
    private float leftVolume = 1.0f;
    private float rightVolume = 1.0f;
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            //Release the completed player
            //replace it with the next player and set the volume
            //create a new, "next", media player
            // Note that on Samsung devices the next player will start playing
            //before this is called.
            mediaPlayer.release();

            mCurrentPlayer = mNextPlayer;

            mCurrentPlayer.setVolume(leftVolume, rightVolume);

            createNextMediaPlayer();

            Log.d(TAG, String.format("Loop #%d", ++mCounter));
        }
    };
    private LoopMediaPlayer(Context context) {
        mContext = context;
    }

    static LoopMediaPlayer create(Context context) {
        return new LoopMediaPlayer(context);
    }

    private void createNextMediaPlayer() {
        //default to playing white noise if none selected
        if(mResId == NO_SOUND_FILE)
            mResId = R.raw.white;
        mNextPlayer = MediaPlayer.create(mContext, mResId);
        mNextPlayer.setVolume(leftVolume, rightVolume);
        mCurrentPlayer.setNextMediaPlayer(mNextPlayer);
        mCurrentPlayer.setOnCompletionListener(onCompletionListener);
    }

    void stop() {
        if (mCurrentPlayer != null)
            mCurrentPlayer.stop();
    }

    void pause() {
        if (mCurrentPlayer != null)
            mCurrentPlayer.pause();
    }

    public void play() {
        //default to playing white noise if none selected
        if(mResId == NO_SOUND_FILE)
            mResId = R.raw.white;
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

    int getSoundFile() {
        return mResId;
    }

    void setSoundFile(int resId) {
        //reset if a different file
        if (resId != mResId) {
            boolean wasPlaying = false;
            if (mCurrentPlayer != null) {
                wasPlaying = mCurrentPlayer.isPlaying();
                stop();
            }
            mResId = resId;
            if ((mCurrentPlayer != null) && wasPlaying)
                play();
        }
    }

    boolean isPlaying() {
        return mCurrentPlayer != null && mCurrentPlayer.isPlaying();
    }

    void setVolume(float leftVolume, float rightVolume) {
        if (mCurrentPlayer != null) {
            mCurrentPlayer.setVolume(leftVolume, rightVolume);
            //set the next media players volume also so it will be in sync
            mNextPlayer.setVolume(leftVolume, rightVolume);
        }
        this.leftVolume = leftVolume;
        this.rightVolume = rightVolume;
    }

}
