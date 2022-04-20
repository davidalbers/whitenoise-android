package dalbers.com.noise.playerscreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dalbers.com.noise.audiocontrol.AudioController
import dalbers.com.noise.audiocontrol.SoundState
import dalbers.com.noise.playerscreen.model.PlayerScreenState
import dalbers.com.noise.playerscreen.view.TimerTimeState
import dalbers.com.noise.playerscreen.view.TimerToggleState
import dalbers.com.noise.shared.NoiseType
import kotlinx.coroutines.launch

class PlayerScreenViewModel : ViewModel() {
    private var _playerScreenState = MutableLiveData<PlayerScreenState>()
    val playerScreenState: LiveData<PlayerScreenState> = _playerScreenState
    // this is attached to a service so it can play in the background not sure the best way to manage it yet
    private var audioController: AudioController? = null

    fun bindAudioController(audioController: AudioController) {
        this.audioController = audioController

        viewModelScope.launch {
            audioController.stateFlow.collect {
                _playerScreenState.value = mapSoundStateToPlayerScreenState(it)
            }
        }
    }

    private fun mapSoundStateToPlayerScreenState(soundState: SoundState): PlayerScreenState {
        val previousTimerState = _playerScreenState.value?.timerState ?: TimerToggleState.Disabled
        val timerState = if (previousTimerState is TimerToggleState.Saved) {
            if (soundState.millisLeft == 0L) {
                TimerToggleState.Disabled
            } else {
                previousTimerState.copy(displayedTime = soundState.millisLeft.millisToTimerState())
            }
        } else {
            previousTimerState
        }
        return PlayerScreenState(
            noiseType = soundState.noiseType,
            fadeEnabled = soundState.fadeEnabled,
            playing = soundState.playing,
            wavesEnabled = soundState.wavesEnabled,
            timerState = timerState,
            showTimerPicker = false,
            timerPickerState = TimerTimeState.zero,
            volume = soundState.volume,
        )
    }

    fun clearAudioController() {
        audioController = null
    }

    fun changeNoiseType(noiseType: NoiseType) {
        audioController?.setNoiseType(noiseType)
    }

    fun toggleFade(enabled: Boolean) {
        audioController?.setFade(enabled)
    }

    fun toggleWaves(enabled: Boolean) {
        audioController?.setWaves(enabled)
    }

    fun changeVolume(newVolume: Float) {
        audioController?.setVolume(newVolume)
    }

    fun updateTimer(timerChange: Int) {
        val timerTimeState = _playerScreenState.value?.timerPickerState ?: return
        val oldTimerMinutes = timerTimeState.minutes +
                timerTimeState.minutesTens * 10 +
                timerTimeState.hours * 60
        val newTimerMinutes = oldTimerMinutes + timerChange
        if (newTimerMinutes < 0) return
        _playerScreenState.value = _playerScreenState.value?.copy(
            timerPickerState = newTimerMinutes.minutesToTimerState()
        )
    }

    fun setTimer() {
        if (_playerScreenState.value?.showTimerPicker != true) return
        val timeState = _playerScreenState.value?.timerPickerState ?: return

        val newTimerToggleState = if (timeState != TimerTimeState.zero) {
            audioController?.setTimer(timeState.toMillis())
            TimerToggleState.Saved(timeState.toFormattedString())
        } else {
            TimerToggleState.Disabled
        }
        _playerScreenState.value = _playerScreenState.value?.copy(
            timerState = newTimerToggleState,
            showTimerPicker = false,
        )
    }

    fun cancelTimer() {
        _playerScreenState.value = _playerScreenState.value?.copy(
            timerState = TimerToggleState.Disabled,
            showTimerPicker = false,
        )
    }

    fun toggleTimer() {
        val currentTimerToggleState = _playerScreenState.value?.timerState
        if (currentTimerToggleState is TimerToggleState.Disabled) {
            _playerScreenState.value = _playerScreenState.value?.copy(
                showTimerPicker = true,
                timerPickerState = TimerTimeState.zero,
            )
        } else {
            audioController?.setTimer(0)
            _playerScreenState.value = _playerScreenState.value?.copy(timerState = TimerToggleState.Disabled)
        }
    }

    fun togglePlay(playing: Boolean) {
        if (playing) {
            audioController?.play()
        } else {
            audioController?.pause()
        }
    }
}

private fun Long.millisToTimerState(): String {
    val seconds = (this / 1000).toInt()
    return seconds.secondsToString()
}

private fun Int.minutesToTimerState(): TimerTimeState {
    return TimerTimeState(
        hours = this / 60,
        minutesTens = this % 60 / 10,
        minutes = this % 60 % 10,
    )
}

private fun TimerTimeState.toMillis(): Long {
    return (hours * 60 * 60L +
            minutesTens * 10 * 60L +
            minutes * 60L) * 1000
}

private fun TimerTimeState.toFormattedString(): String {
    return "$hours:${(minutesTens * 10 + minutes).withTensPadding()}:00"
}

private fun Int.secondsToString(): String {
    val hours = this / 60 / 60
    val minutes = (this / 60) % 60
    val seconds = (this % 60) % 60
    return "$hours:${minutes.withTensPadding()}:${seconds.withTensPadding()}"
}

private fun Int.withTensPadding(): String {
    return if (this < 10) "0$this" else toString()
}