package dalbers.com.noise

import android.content.Context
import android.support.annotation.RawRes
import com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC
import com.google.android.exoplayer2.C.USAGE_MEDIA
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource

class ExoPlayerWrapper(val context: Context) {
    private val player: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())

    fun setFile(@RawRes resource: Int) {
        val rawDataSource = RawResourceDataSource(context)
        // open the /raw resource file
        rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(resource)))

        // create a media source with the raw DataSource
        val mediaSource = ExtractorMediaSource.Factory(DataSource.Factory { rawDataSource })
                .createMediaSource(rawDataSource.uri)

        val audioAttributes = AudioAttributes.Builder()
                .setContentType(CONTENT_TYPE_MUSIC)
                .setUsage(USAGE_MEDIA)
                .build()

        player.audioAttributes = audioAttributes
        player.repeatMode = Player.REPEAT_MODE_ALL
        // setup the player using the source
        player.prepare(mediaSource)
    }

    fun play() {
        player.playWhenReady = true
    }

    fun pause() {
        player.playWhenReady = false
    }

    fun isPlaying(): Boolean = player.playWhenReady

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        player.volume = (leftVolume + rightVolume) / 2
    }
}
