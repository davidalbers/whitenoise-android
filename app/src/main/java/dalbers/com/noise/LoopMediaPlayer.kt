package dalbers.com.noise

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log

/**
 * Created by davidalbers on 4/10/16.
 * Plays an audio file on repeat
 */
internal class LoopMediaPlayer private constructor(private val context: Context) {

    var noiseType = NoiseType.NONE
        set(value) {
            //reset if a different file
            if (field == value) {
                return
            }
            field = value
            if (currentPlayer?.isPlaying == true) {
                stop()
                play()
            }
        }
    private var counter = 1

    private var currentPlayer: MediaPlayer? = null
    private var nextPlayer: MediaPlayer? = null
    private var leftVolume = 1.0f
    private var rightVolume = 1.0f
    private val tag = LoopMediaPlayer::class.java.simpleName
    private val onCompletionListener = MediaPlayer.OnCompletionListener { mediaPlayer ->
        //Release the completed player
        //replace it with the next player and set the volume
        //create a new, "next", media player
        // Note that on Samsung devices the next player will start playing
        //before this is called.
        mediaPlayer.release()

        currentPlayer = nextPlayer

        currentPlayer?.setVolume(leftVolume, rightVolume)

        createNextMediaPlayer()

        Log.d(tag, String.format("Loop #%d", ++counter))
    }

    val isPlaying: Boolean
        get() = currentPlayer?.isPlaying == true

    private fun createNextMediaPlayer() {
        //default to playing white noise if none selected
        if (noiseType == NoiseType.NONE) {
            noiseType = NoiseType.WHITE
        }
        nextPlayer = MediaPlayer.create(context, noiseType.soundFile)
        nextPlayer?.setVolume(leftVolume, rightVolume)
        currentPlayer?.apply {
            setNextMediaPlayer(nextPlayer)
            setOnCompletionListener(onCompletionListener)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
    }

    fun stop() {
        currentPlayer?.stop()
    }

    fun pause() {
        currentPlayer?.pause()
    }

    fun play() {
        //default to playing white noise if none selected
        if (noiseType == NoiseType.NONE) {
            noiseType = NoiseType.WHITE
        }
        currentPlayer = MediaPlayer.create(context, noiseType.soundFile)
        currentPlayer?.setOnPreparedListener {
            currentPlayer?.setVolume(leftVolume, rightVolume)
            currentPlayer?.start()
        }
        currentPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

        createNextMediaPlayer()
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        currentPlayer?.setVolume(leftVolume, rightVolume)
        //set the next media players volume also so it will be in sync
        nextPlayer?.setVolume(leftVolume, rightVolume)
        this.leftVolume = leftVolume
        this.rightVolume = rightVolume
    }

    companion object {
        fun create(context: Context): LoopMediaPlayer {
            return LoopMediaPlayer(context)
        }
    }


}
