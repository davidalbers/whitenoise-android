package dalbers.com.noise.audiocontrol

import android.content.Context
import androidx.annotation.RawRes
import com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC
import com.google.android.exoplayer2.C.USAGE_MEDIA
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util


interface AudioPlayer {
    fun setFile(@RawRes resource: Int)
    fun play()
    fun pause()
    fun isPlaying(): Boolean
    fun setVolume(volume: Float)
}

class AudioPlayerImpl(private val context: Context) : AudioPlayer {
    private val player: SimpleExoPlayer = SimpleExoPlayer.Builder(context).build()
    @RawRes private var lastFile: Int = 0

    override fun setFile(@RawRes resource: Int) {
        if (lastFile == resource) return
        lastFile = resource
        val rawDataSource = RawResourceDataSource(context)
        // open the /raw resource file
        rawDataSource.open(DataSpec(RawResourceDataSource.buildRawResourceUri(resource)))

        // create a media source with the raw DataSource
        val mediaSource = Util.getUserAgent(context, context.packageName).let { userAgent ->
            ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
                .createMediaSource(rawDataSource.uri)
        }

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(CONTENT_TYPE_MUSIC)
            .setUsage(USAGE_MEDIA)
            .build()

        player.audioAttributes = audioAttributes
        player.repeatMode = Player.REPEAT_MODE_ALL
        // setup the player using the source
        player.prepare(mediaSource)
    }

    override fun play() {
        player.playWhenReady = true
    }

    override fun pause() {
        player.playWhenReady = false
    }

    override fun isPlaying(): Boolean = player.playWhenReady

    override fun setVolume(volume: Float) {
        player.volume = volume
    }
}