package dalbers.com.noise.playerscreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dalbers.com.noise.audiocontrol.AudioController
import dalbers.com.noise.audiocontrol.SoundState
import dalbers.com.noise.playerscreen.model.PlayerScreenState
import dalbers.com.noise.playerscreen.view.TimerPickerState
import dalbers.com.noise.playerscreen.view.TimerToggleState
import dalbers.com.noise.shared.NoiseType
import dalbers.com.noise.shared.UserPreferences
import kotlinx.coroutines.launch

class PlayerScreenViewModel(
    private val userPreferences: UserPreferences,
) : ViewModel() {
    private var _playerScreenState = MutableLiveData<PlayerScreenState>()
    val playerScreenState: LiveData<PlayerScreenState> = _playerScreenState
    // this is attached to a service so it can play in the background not sure the best way to manage it yet
    private var audioController: AudioController? = null

    fun bindAudioController(audioController: AudioController) {
        this.audioController = audioController
        loadPastPreferences()

        viewModelScope.launch {
            audioController.stateFlow.collect {
                _playerScreenState.value = mapSoundStateToPlayerScreenState(it)
            }
        }
    }

    private fun mapSoundStateToPlayerScreenState(soundState: SoundState): PlayerScreenState {
        val previousTimerState = _playerScreenState.value?.timerToggleState ?: TimerToggleState.Disabled
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
            timerToggleState = timerState,
            showTimerPicker = _playerScreenState.value?.showTimerPicker ?: false,
            timerPickerState = _playerScreenState.value?.timerPickerState ?: TimerPickerState.zero,
            volume = soundState.volume,
        )
    }

    private fun loadPastPreferences() {
        _playerScreenState.value = _playerScreenState.value?.copy(
            noiseType = userPreferences.lastUsedColor,
            volume = userPreferences.lastUsedVolume,
            wavesEnabled = userPreferences.lastUsedWavy,
            fadeEnabled = userPreferences.lastUsedFade,
        )
        audioController?.setNoiseType(userPreferences.lastUsedColor)
        audioController?.setVolume(userPreferences.lastUsedVolume)
        audioController?.setWaves(userPreferences.lastUsedWavy)
        audioController?.setFade(userPreferences.lastUsedFade)
        val audioControllerState = audioController?.stateFlow?.value
        if (audioControllerState?.playing == false
            && audioControllerState.millisLeft == 0L
            && userPreferences.lastTimerTimeMillis != 0L
            && userPreferences.timerEnabled) {
            _playerScreenState.value = _playerScreenState.value?.copy(
                timerToggleState = TimerToggleState.Saved(userPreferences.lastTimerTimeMillis.millisToTimerState())
            )
            audioController?.setTimer(userPreferences.lastTimerTimeMillis)
        }
    }

    fun clearAudioController() {
        audioController = null
    }

    fun changeNoiseType(noiseType: NoiseType) {
        userPreferences.lastUsedColor = noiseType
        audioController?.setNoiseType(noiseType)
    }

    fun toggleFade(enabled: Boolean) {
        userPreferences.lastUsedFade = enabled
        audioController?.setFade(enabled)
    }

    fun toggleWaves(enabled: Boolean) {
        userPreferences.lastUsedWavy = enabled
        audioController?.setWaves(enabled)
    }

    fun changeVolume(newVolume: Float) {
        userPreferences.lastUsedVolume = newVolume
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
            timerPickerState = newTimerMinutes.minutesToTimerPickerState()
        )
    }

    fun setTimer() {
        if (_playerScreenState.value?.showTimerPicker != true) return
        val timeState = _playerScreenState.value?.timerPickerState ?: return

        userPreferences.lastTimerTimeMillis = timeState.toMillis()
        val newTimerToggleState = if (timeState != TimerPickerState.zero) {
            audioController?.setTimer(timeState.toMillis())
            userPreferences.timerEnabled = true
            TimerToggleState.Saved(timeState.toFormattedString())
        } else {
            userPreferences.timerEnabled = false
            TimerToggleState.Disabled
        }
        _playerScreenState.value = _playerScreenState.value?.copy(
            timerToggleState = newTimerToggleState,
            showTimerPicker = false,
        )
    }

    fun cancelTimer() {
        userPreferences.lastTimerTimeMillis = 0L
        userPreferences.timerEnabled = false
        _playerScreenState.value = _playerScreenState.value?.copy(
            timerToggleState = TimerToggleState.Disabled,
            showTimerPicker = false,
        )
    }

    fun toggleTimer() {
        val currentTimerToggleState = _playerScreenState.value?.timerToggleState
        if (currentTimerToggleState is TimerToggleState.Disabled) {
            _playerScreenState.value = _playerScreenState.value?.copy(
                showTimerPicker = true,
                timerPickerState = userPreferences.lastTimerTimeMillis.millisToTimerPickerState(),
            )
        } else {
            audioController?.setTimer(0)
            userPreferences.timerEnabled = false
            _playerScreenState.value = _playerScreenState.value?.copy(timerToggleState = TimerToggleState.Disabled)
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

private fun Long.millisToTimerPickerState(): TimerPickerState {
    return (this / 60000L).toInt().minutesToTimerPickerState()
}

private fun Int.minutesToTimerPickerState(): TimerPickerState {
    return TimerPickerState(
        hours = this / 60,
        minutesTens = this % 60 / 10,
        minutes = this % 60 % 10,
    )
}

private fun TimerPickerState.toMillis(): Long {
    return (hours * 60 * 60L +
            minutesTens * 10 * 60L +
            minutes * 60L) * 1000
}

private fun TimerPickerState.toFormattedString(): String {
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