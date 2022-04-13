package dalbers.com.noise

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dalbers.com.noise.audiocontrol.AudioController
import dalbers.com.noise.audiocontrol.SoundState
import dalbers.com.noise.service.NotificationAction
import dalbers.com.noise.service.model.AudioPlayerScreenState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class AudioPlayerButton(
    val action: NotificationAction,
    @StringRes val textResource: Int,
    @DrawableRes val iconResource: Int,
) {
    companion object {
        val play = AudioPlayerButton(
            action = NotificationAction.PLAY_ACTION,
            textResource = R.string.audio_play,
            iconResource = R.drawable.ic_action_playback_play_black,
        )
        val pause = AudioPlayerButton(
            action = NotificationAction.PAUSE_ACTION,
            textResource = R.string.audio_pause,
            iconResource = R.drawable.ic_action_playback_pause_black,
        )
        val close = AudioPlayerButton(
            action = NotificationAction.CLOSE_ACTION,
            textResource = R.string.notification_close,
            iconResource = R.drawable.ic_clear,
        )
    }
}

class AudioPlayerViewModel(
    private val audioController: AudioController,
): ViewModel() {
    private var _stateLiveData = MutableLiveData<AudioPlayerScreenState>(AudioPlayerScreenState.Hidden)
    var stateLiveData: LiveData<AudioPlayerScreenState> = _stateLiveData
    private var playing = false
    private var notificationEnabled = false

    init {
        viewModelScope.launch {
            audioController.stateFlow.collect { soundState ->
                if (!notificationEnabled) return@collect
                if (playing != soundState.playing) {
                    _stateLiveData.value = soundState.toAudioPlayerScreenState()
                    playing = !playing
                }
            }
        }
    }

    fun enableNotification() {
        notificationEnabled = true
        if (audioController.stateFlow.value.playing) {
            _stateLiveData.value = audioController.stateFlow.value.toAudioPlayerScreenState()
            playing = true
        }
    }

    private fun SoundState.toAudioPlayerScreenState(): AudioPlayerScreenState {
        return AudioPlayerScreenState.Shown(
            titleResource = noiseType.notificationTitle,
            subtitleResource = if (playing) R.string.notification_playing else R.string.notification_paused,
            firstButton = if (playing) AudioPlayerButton.pause else AudioPlayerButton.play,
            secondButton = AudioPlayerButton.close,
        )
    }

    fun handleNotificationAction(notificationAction: NotificationAction) {
        when (notificationAction) {
            NotificationAction.PAUSE_ACTION -> {
                audioController.pause()
                audioController.cancelTimer()
            }
            NotificationAction.PLAY_ACTION -> {
                audioController.play()
                audioController.setTimer(audioController.stateFlow.value.millisLeft)
            }
            NotificationAction.CLOSE_ACTION -> {
                notificationEnabled = false
                audioController.pause()
                audioController.stopTimer()
                _stateLiveData.value = AudioPlayerScreenState.Hidden
            }
        }
    }
}