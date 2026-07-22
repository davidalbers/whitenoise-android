package dalbers.com.noise.playerscreen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dalbers.com.noise.audiocontrol.AudioController
import dalbers.com.noise.audiocontrol.SoundState
import dalbers.com.noise.playerscreen.model.PlayerScreenState
import dalbers.com.noise.playerscreen.model.TimerPreset
import dalbers.com.noise.playerscreen.view.TimerPickerState
import dalbers.com.noise.shared.NoiseType
import dalbers.com.noise.shared.UserPreferences
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerScreenViewModel
    @Inject
    constructor(
        private val userPreferences: UserPreferences,
        private val audioController: AudioController,
    ) : ViewModel() {
        private var _playerScreenState = MutableLiveData<PlayerScreenState>()
        val playerScreenState: LiveData<PlayerScreenState> = _playerScreenState

        init {
            loadPastPreferences()

            viewModelScope.launch {
                audioController.stateFlow.collect {
                    _playerScreenState.value = mapSoundStateToPlayerScreenState(it)
                }
            }
        }

        private fun mapSoundStateToPlayerScreenState(soundState: SoundState): PlayerScreenState {
            val previousState = _playerScreenState.value
            return PlayerScreenState(
                noiseType = soundState.noiseType,
                fadeEnabled = soundState.fadeEnabled,
                playing = soundState.playing,
                wavesEnabled = soundState.wavesEnabled,
                selectedTimerPreset = previousState?.selectedTimerPreset,
                customTimerMillis = previousState?.customTimerMillis ?: 0L,
                showTimerPicker = previousState?.showTimerPicker == true,
                timerPickerState = previousState?.timerPickerState ?: TimerPickerState.zero,
                volume = soundState.volume,
                millisLeft = soundState.millisLeft,
            )
        }

        private fun loadPastPreferences() {
            val lastMillis = userPreferences.lastTimerTimeMillis
            val savedPreset =
                if (userPreferences.timerEnabled && lastMillis > 0) {
                    TimerPreset.from(lastMillis)
                } else {
                    null
                }

            _playerScreenState.value =
                _playerScreenState.value?.copy(
                    noiseType = userPreferences.lastUsedColor,
                    volume = userPreferences.lastUsedVolume,
                    wavesEnabled = userPreferences.lastUsedWavy,
                    fadeEnabled = userPreferences.lastUsedFade,
                    selectedTimerPreset = savedPreset,
                    customTimerMillis = lastMillis,
                )
            audioController.setNoiseType(userPreferences.lastUsedColor)
            audioController.setVolume(userPreferences.lastUsedVolume)
            audioController.setWaves(userPreferences.lastUsedWavy)
            audioController.setFade(userPreferences.lastUsedFade)
            if (savedPreset != null) {
                audioController.setTimer(lastMillis)
            }
        }

        fun changeNoiseType(noiseType: NoiseType) {
            userPreferences.lastUsedColor = noiseType
            audioController.setNoiseType(noiseType)
        }

        fun toggleFade(enabled: Boolean) {
            userPreferences.lastUsedFade = enabled
            audioController.setFade(enabled)
        }

        fun toggleWaves(enabled: Boolean) {
            userPreferences.lastUsedWavy = enabled
            audioController.setWaves(enabled)
        }

        fun changeVolume(newVolume: Float) {
            userPreferences.lastUsedVolume = newVolume
            audioController.setVolume(newVolume)
        }

        fun selectTimerPreset(preset: TimerPreset?) {
            if (preset == null) {
                audioController.setTimer(0)
                userPreferences.timerEnabled = false
                _playerScreenState.value =
                    _playerScreenState.value?.copy(
                        selectedTimerPreset = null,
                    )
            } else {
                audioController.setTimer(preset.millis)
                userPreferences.lastTimerTimeMillis = preset.millis
                userPreferences.timerEnabled = true
                _playerScreenState.value =
                    _playerScreenState.value?.copy(
                        selectedTimerPreset = preset,
                    )
            }
        }

        fun openCustomTimer() {
            _playerScreenState.value =
                _playerScreenState.value?.copy(
                    showTimerPicker = true,
                    timerPickerState = userPreferences.lastTimerTimeMillis.millisToTimerPickerState(),
                )
        }

        fun updateTimer(timerChange: Int) {
            val timerTimeState = _playerScreenState.value?.timerPickerState ?: return
            val oldTimerMinutes =
                timerTimeState.minutes +
                    timerTimeState.minutesTens * 10 +
                    timerTimeState.hours * 60
            val newTimerMinutes = oldTimerMinutes + timerChange
            if (newTimerMinutes < 0) return
            _playerScreenState.value =
                _playerScreenState.value?.copy(
                    timerPickerState = newTimerMinutes.minutesToTimerPickerState(),
                )
        }

        fun setTimer() {
            if (_playerScreenState.value?.showTimerPicker != true) return
            val timeState = _playerScreenState.value?.timerPickerState ?: return
            val millis = timeState.toMillis()

            userPreferences.lastTimerTimeMillis = millis
            val newPreset =
                if (millis > 0) {
                    audioController.setTimer(millis)
                    userPreferences.timerEnabled = true
                    TimerPreset.from(millis)
                } else {
                    userPreferences.timerEnabled = false
                    null
                }

            _playerScreenState.value =
                _playerScreenState.value?.copy(
                    selectedTimerPreset = newPreset,
                    customTimerMillis = if (millis > 0) millis else _playerScreenState.value?.customTimerMillis ?: 0L,
                    showTimerPicker = false,
                )
        }

        fun cancelTimer() {
            _playerScreenState.value =
                _playerScreenState.value?.copy(
                    showTimerPicker = false,
                )
        }

        fun togglePlay(playing: Boolean) {
            if (playing) {
                audioController.play()
            } else {
                audioController.pause()
            }
        }
    }

private fun Long.millisToTimerPickerState(): TimerPickerState = (this / 60000L).toInt().minutesToTimerPickerState()

private fun Int.minutesToTimerPickerState(): TimerPickerState =
    TimerPickerState(
        hours = this / 60,
        minutesTens = this % 60 / 10,
        minutes = this % 60 % 10,
    )

private fun TimerPickerState.toMillis(): Long =
    (
        hours * 60 * 60L +
            minutesTens * 10 * 60L +
            minutes * 60L
    ) * 1000
