package dalbers.com.noise.playerscreen.model

import dalbers.com.noise.playerscreen.view.TimerPickerState
import dalbers.com.noise.playerscreen.view.TimerToggleState
import dalbers.com.noise.shared.NoiseType

data class PlayerScreenState(
    val noiseType: NoiseType,
    val fadeEnabled: Boolean,
    val wavesEnabled: Boolean,
    val volume: Float,
    val timerToggleState: TimerToggleState,
    val showTimerPicker: Boolean,
    val timerPickerState: TimerPickerState,
    val playing: Boolean,
) {
    companion object {
        val default = PlayerScreenState(
            noiseType = NoiseType.WHITE,
            fadeEnabled = false,
            wavesEnabled = false,
            timerToggleState = TimerToggleState.Disabled,
            showTimerPicker = false,
            timerPickerState = TimerPickerState.zero,
            volume = 1f,
            playing = false,
        )
    }
}
