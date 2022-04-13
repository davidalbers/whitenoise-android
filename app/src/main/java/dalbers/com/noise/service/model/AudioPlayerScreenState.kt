package dalbers.com.noise.service.model

import androidx.annotation.StringRes
import dalbers.com.noise.AudioPlayerButton

sealed class AudioPlayerScreenState {
    data class Shown(
        @StringRes val titleResource: Int,
        @StringRes val subtitleResource: Int,
        val firstButton: AudioPlayerButton,
        val secondButton: AudioPlayerButton,
    ) : AudioPlayerScreenState()

    object Hidden : AudioPlayerScreenState()
}
