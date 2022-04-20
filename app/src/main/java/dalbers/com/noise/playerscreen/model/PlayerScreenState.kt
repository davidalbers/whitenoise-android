package dalbers.com.noise.playerscreen.model

import dalbers.com.noise.playerscreen.view.TimerTimeState
import dalbers.com.noise.playerscreen.view.TimerToggleState
import dalbers.com.noise.shared.NoiseType

data class PlayerScreenState(
    val noiseType: NoiseType,
    val fadeEnabled: Boolean,
    val wavesEnabled: Boolean,
    val volume: Float,
    val timerState: TimerToggleState,
    val showTimerPicker: Boolean,
    val timerPickerState: TimerTimeState,
    val playing: Boolean,
) {
    companion object {
        val default = PlayerScreenState(
            noiseType = NoiseType.WHITE,
            fadeEnabled = false,
            wavesEnabled = false,
            timerState = TimerToggleState.Disabled,
            showTimerPicker = false,
            timerPickerState = TimerTimeState.zero,
            volume = 1f,
            playing = false,
        )
    }
}
