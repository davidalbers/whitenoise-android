package dalbers.com.noise.playerscreen.model

import dalbers.com.noise.playerscreen.view.TimerPickerState
import dalbers.com.noise.shared.NoiseType

data class PlayerScreenState(
    val noiseType: NoiseType,
    val fadeEnabled: Boolean,
    val wavesEnabled: Boolean,
    val volume: Float,
    val selectedTimerPreset: TimerPreset?,
    val customTimerMillis: Long,
    val showTimerPicker: Boolean,
    val timerPickerState: TimerPickerState,
    val playing: Boolean,
    val millisLeft: Long,
) {
    companion object {
        val default = PlayerScreenState(
            noiseType = NoiseType.WHITE,
            fadeEnabled = false,
            wavesEnabled = false,
            selectedTimerPreset = null,
            customTimerMillis = 0L,
            showTimerPicker = false,
            timerPickerState = TimerPickerState.zero,
            volume = 1f,
            playing = false,
            millisLeft = 0L,
        )
    }
}
