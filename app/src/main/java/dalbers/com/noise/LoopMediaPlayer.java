package dalbers.com.noise;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Created by davidalbers on 4/10/16.
 * Plays an audio file on repeat
 */
class LoopMediaPlayer {

    private static final String TAG = LoopMediaPlayer.class.getSimpleName();

    private Context context = null;
    private NoiseType noiseType = NoiseType.NONE;
    private int counter = 1;

    private MediaPlayer currentPlayer = null;
    private MediaPlayer nextPlayer = null;
    private float leftVolume = 1.0f;
    private float rightVolume = 1.0f;
    private MediaPlayer.OnCompletionListener onCompletionListener =
            new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            //Release the completed player
            //replace it with the next player and set the volume
            //create a new, "next", media player
            // Note that on Samsung devices the next player will start playing
            //before this is called.
            mediaPlayer.release();

            currentPlayer = nextPlayer;

            currentPlayer.setVolume(leftVolume, rightVolume);

            createNextMediaPlayer();

            Log.d(TAG, String.format("Loop #%d", ++counter));
        }
    };

    private LoopMediaPlayer(Context context) {
        this.context = context;
    }

    static LoopMediaPlayer create(Context context) {
        return new LoopMediaPlayer(context);
    }

    private void createNextMediaPlayer() {
        //default to playing white noise if none selected
        if (noiseType == NoiseType.NONE) {
            noiseType = NoiseType.WHITE;
        }
        nextPlayer = MediaPlayer.create(context, noiseType.getSoundFile());
        nextPlayer.setVolume(leftVolume, rightVolume);
        currentPlayer.setNextMediaPlayer(nextPlayer);
        currentPlayer.setOnCompletionListener(onCompletionListener);
        currentPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        nextPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    void stop() {
        if (currentPlayer != null) {
            currentPlayer.stop();
        }
    }

    void pause() {
        if (currentPlayer != null) {
            currentPlayer.pause();
        }
    }

    public void play() {
        //default to playing white noise if none selected
        if (noiseType == NoiseType.NONE) {
            noiseType = NoiseType.WHITE;
        }
        currentPlayer = MediaPlayer.create(context, noiseType.getSoundFile());
        currentPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                currentPlayer.setVolume(leftVolume, rightVolume);
                currentPlayer.start();
            }
        });
        currentPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        createNextMediaPlayer();
    }

    NoiseType getNoiseType() {
        return noiseType;
    }

    void setNoiseType(NoiseType noiseType) {
        //reset if a different file
        if (this.noiseType != noiseType) {
            boolean wasPlaying = false;
            if (currentPlayer != null) {
                wasPlaying = currentPlayer.isPlaying();
                stop();
            }
            this.noiseType = noiseType;
            if ((currentPlayer != null) && wasPlaying) {
                play();
            }
        }
    }

    boolean isPlaying() {
        return currentPlayer != null && currentPlayer.isPlaying();
    }

    void setVolume(float leftVolume, float rightVolume) {
        if (currentPlayer != null) {
            currentPlayer.setVolume(leftVolume, rightVolume);
            //set the next media players volume also so it will be in sync
            nextPlayer.setVolume(leftVolume, rightVolume);
        }
        this.leftVolume = leftVolume;
        this.rightVolume = rightVolume;
    }


}
