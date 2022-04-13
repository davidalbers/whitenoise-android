package dalbers.com.noise.audiocontrol

import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_LOSS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AudioFocusState {
    FOCUS_GAINED,
    FOCUS_LOST_TRANSIENT,
    FOCUS_LOST_UNKNOWN,
}

interface AudioFocusManager {
    val focusState: StateFlow<AudioFocusState>
    fun abandon()
    fun request()
}

class AudioFocusManagerImpl(
    private val audioManager: AudioManager,
) : AudioFocusManager {
    private val _focusState = MutableStateFlow(AudioFocusState.FOCUS_LOST_UNKNOWN)
    override val focusState: StateFlow<AudioFocusState> = _focusState
    override fun abandon() {
        // todo: move to undeprecated method
        audioManager.abandonAudioFocus(focusChangeListener)
    }

    override fun request() {
        audioManager.requestAudioFocus(
            focusChangeListener, AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
    }

    private var focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        val audioFocusState = when {
            focusChange > 0 -> AudioFocusState.FOCUS_GAINED
            focusChange == AUDIOFOCUS_LOSS -> AudioFocusState.FOCUS_LOST_UNKNOWN
            else -> AudioFocusState.FOCUS_LOST_TRANSIENT
        }
        _focusState.value = audioFocusState
    }
}