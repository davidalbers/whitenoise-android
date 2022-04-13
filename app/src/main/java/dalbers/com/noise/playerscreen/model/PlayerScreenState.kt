package dalbers.com.noise.playerscreen.model

import dalbers.com.noise.playerscreen.view.TimerState
import dalbers.com.noise.shared.NoiseType

data class PlayerScreenState(
    val noiseType: NoiseType,
    val fadeEnabled: Boolean,
    val wavesEnabled: Boolean,
    val volume: Float,
    val timerState: TimerState,
    val playing: Boolean,
) {
    companion object {
        val default = PlayerScreenState(
            noiseType = NoiseType.WHITE,
            fadeEnabled = false,
            wavesEnabled = false,
            timerState = TimerState.Disabled,
            volume = 1f,
            playing = false,
        )
    }
}
