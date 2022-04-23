package dalbers.com.noise.audiocontrol

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import dalbers.com.noise.shared.NoiseType
import dalbers.com.noise.shared.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * This is the interval at which the volume timer updates.
 * There doesn't seem to be a performance hit at this interval
 * and it's fast enough that I can't hear it.
 */
const val tickPeriod: Long = 100
/**
 * Use this instead of decreaseLength if no timer exists.
 */
val DEFAULT_DECREASE_LENGTH = TimeUnit.HOURS.toMillis(1)

/**
 * If using oscillate or decrease, the min volume will be the max multiplied by this value.
 */
const val minVolumePercent = .2f

data class SoundState(
    val fadeEnabled: Boolean,
    val wavesEnabled: Boolean,
    val millisLeft: Long,
    val noiseType: NoiseType,
    val playing: Boolean,
    val volume: Float,
) {
    companion object {
        val default = SoundState(
            fadeEnabled = false,
            wavesEnabled = false,
            millisLeft = 0L,
            noiseType = NoiseType.WHITE,
            playing = false,
            volume = 1f,
        )
    }
}

class AudioController(
    private val player: AudioPlayer,
    private val audioFocusManager: AudioFocusManager,
    private val looper: Looper,
    private val userPreferences: UserPreferences,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) {
    private var _stateFlow = MutableStateFlow(SoundState.default)
    var stateFlow: StateFlow<SoundState> = _stateFlow
    private var volume = 1f
    private var oscillatingDown = true
    private var countDownTimer: CountDownTimer? = null
    /**
     * If the player loses focus and it was playing, then set this true.
     * When and if we regain focus, you know to start playing again if this is true.
     */
    private var startPlayingWhenFocusRegained = false
    private val handler = Handler(looper)

    fun setNoiseType(noiseType: NoiseType) {
        _stateFlow.value = _stateFlow.value.copy(noiseType = noiseType)
        setFile(noiseType)
    }

    fun setFade(fadeEnabled: Boolean) {
        _stateFlow.value = _stateFlow.value.copy(fadeEnabled = fadeEnabled)
    }

    fun setWaves(wavesEnabled: Boolean) {
        _stateFlow.value = _stateFlow.value.copy(wavesEnabled = wavesEnabled)
    }

    /**
     * One complete oscillation from left to right will happen in this interval.
     */
    private var oscillatePeriod: Int = 8000

    /**
     * Value for how long the sound will be decreasing.
     * It should go from max to min in this time period.
     * Ideally, this is == timer time.
     */
    private var decreaseLength: Long = -1

    /**
     * Value for the maximum allowable volume given the current state.
     * If you're only using oscillate, this should be == initialVolume.
     * If you're using decrease, this will decrease over time.
     */
    private var maxVolume = 1.0f

    fun setVolume(volume: Float) {
        maxVolume = volume
        minVolume = volume * minVolumePercent
        oscillatingDown = true
        _stateFlow.value = _stateFlow.value.copy(volume = volume)
        player.setVolume(maxVolume)
    }

    private var minVolume = minVolumePercent

    init {
        val volumeChangerTimer = Timer()
        volumeChangerTimer.schedule(object: TimerTask() {
            override fun run() {
                handler.post{ tick() }
            }
        }, 0, tickPeriod)
        coroutineScope.launch {
            audioFocusManager.focusState.collect {
                onAudioFocusChange(it)
            }
        }
        setNoiseType(_stateFlow.value.noiseType)
    }

    fun tick() {
        oscillatePeriod = userPreferences.waveIntervalMillis()
        val state = _stateFlow.value
        if (player.isPlaying()) {
            when {
                state.wavesEnabled && state.fadeEnabled -> waveAndFadeForTick()
                state.wavesEnabled -> waveForTick()
                state.fadeEnabled -> fadeForTick()
            }
            player.setVolume(volume)
        }
    }

    private fun setFile(noiseType: NoiseType) {
        player.setFile(noiseType.soundFile)
        player.setVolume(maxVolume)
    }

    fun pause() {
        player.pause()
        _stateFlow.value = _stateFlow.value.copy(playing = false)
        volume = _stateFlow.value.volume
        maxVolume = volume
        player.setVolume(maxVolume)
        audioFocusManager.abandon()
    }

    fun play() {
        player.play()
        countDownTimer?.start()
        _stateFlow.value = _stateFlow.value.copy(playing = true)
        if (!userPreferences.playOver()) {
            //every time we start playing, we have to request audio focus and listen for other
            //apps also listening for focus with the focusChangeListener
            audioFocusManager.request()
        }
    }

    /**
     * Set a CountDownTimer for playing audio.
     * @param millis time to play audio, in ms
     */
    fun setTimer(millis: Long) {
        _stateFlow.value = _stateFlow.value.copy(millisLeft = millis)
        decreaseLength = millis
        countDownTimer?.cancel()
        if (millis == 0L) return
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _stateFlow.value = _stateFlow.value.copy(millisLeft = millisUntilFinished)
            }

            override fun onFinish() {
                _stateFlow.value = _stateFlow.value.copy(millisLeft = 0)
                pause()
            }
        }.apply {
            if (_stateFlow.value.playing) {
                start()
            }
        }
    }

    /**
     * Cancel timer, but don't clear any data about it (e.g. time left).
     */
    fun cancelTimer() {
        countDownTimer?.cancel()
    }

    /**
     * Cancel timer and clear any data about it (e.g. time left).
     */
    fun stopTimer() {
        cancelTimer()
        decreaseLength = -1
        _stateFlow.value = _stateFlow.value.copy(millisLeft = 0)
    }

    private fun fadeForTick() {
        fadeMaxVolumeForTick()
        volume = maxVolume
    }

    /**
     * Based on the minimum volume and clock interval, decrease the volume the appropriate amount
     * for this clock tick.
     */
    private fun fadeMaxVolumeForTick() {
        if (maxVolume > minVolume) {
            val actualDecreaseLength = if (decreaseLength == -1L) DEFAULT_DECREASE_LENGTH else decreaseLength
            val delta = -1 * (_stateFlow.value.volume - minVolume) / (actualDecreaseLength / tickPeriod)
            maxVolume += delta
        }
    }

    private fun waveAndFadeForTick() {
        fadeMaxVolumeForTick()
        waveForTick()
    }

    /**
     * Oscillate volume from masterVolume to (masterVolume*minVolumePercent).
     * Going from max to min to max takes 'oscillatePeriod' milliseconds.
     */
    private fun waveForTick() {
        var delta = (maxVolume - minVolume) / (oscillatePeriod / 2 / tickPeriod)
        if (oscillatingDown) {
            delta *= -1
        }
        volume += delta
        if (volume <= minVolume) {
            volume = minVolume
            oscillatingDown = false
        }
        if (volume >= maxVolume) {
            volume = maxVolume
            oscillatingDown = true
        }
    }

    /**
     * Handles audio focus changes. By default, the app will pause when it looses focus and start
     * again when it regains focus (when lost for a short period). The user has the ability to
     * disable this functionality.
     */
    private fun onAudioFocusChange(focusState: AudioFocusState) {
        if (focusState == AudioFocusState.FOCUS_GAINED && startPlayingWhenFocusRegained) {
            play()
            startPlayingWhenFocusRegained = false
        } else if (!userPreferences.playOver() && focusState != AudioFocusState.FOCUS_GAINED && _stateFlow.value.playing) {
            //we lost audio focus, stop playing
            pause()
            if (focusState == AudioFocusState.FOCUS_LOST_TRANSIENT) {
                //in these cases, we can expect to get audio back soon
                startPlayingWhenFocusRegained = true
            }
        }
    }
}