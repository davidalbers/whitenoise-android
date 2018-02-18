package dalbers.com.noise

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import android.media.AudioAttributes
import android.support.v4.app.NotificationCompat
import android.app.NotificationChannel
import android.support.v4.media.session.MediaSessionCompat

/**
 * A service to play audio on a loop.
 * Features oscillating and decreasing volume.
 * Uses LoopMediaPlayer for looping audio.
 */
class AudioPlayerService : Service() {
    private var mp: LoopMediaPlayer? = null
    private val binder = AudioPlayerBinder()
    /**
     * Get the amount of time left in the timer.
     * @return milliseconds left
     */
    var timeLeft: Long = 0
        private set
    private var countDownTimer: CountDownTimer? = null
    private var leftVolume = 0.5f
    private var rightVolume = 0.5f
    private var oscillateVolume = false
    private var oscillatingDown = true
    private var oscillatingLeft = true
    private var decreaseVolume = false
    private var notificationChannel = "com.dalbers.whitenoise.Notifications"
    /**
     * If using oscillate or decrease, the min volume will be the max multiplied by this value.
     */
    private val minVolumePercent = .2f
    /**
     * One complete oscillation from left to right will happen in this interval.
     */
    private var oscillatePeriod: Long = 8000
    /**
     * This is the interval at which the volume timer updates.
     * There doesn't seem to be a performance hit at this interval
     * and it's fast enough that I can't hear it.
     */
    private val tickPeriod: Long = 100
    /**
     * Value for how long the sound will be decreasing.
     * It should go from max to min in this time period.
     * Ideally, this is == timer time.
     */
    private var decreaseLength: Long = -1
    /**
     * Value for the maximum allowable volume given the current state.
     * If you're only using oscillate, this should be == initialVolume.
     * If you're using decrease, this will decrease over time.
     */
    private var maxVolume = 1.0f
    /**
     * The volume set by the user before oscillation or other things affected it.
     */
    private var initialVolume = 1.0f

    /**
     * Contains logic for determining the volume at the next tick of the clock.
     */
    internal var volumeRunnable: Runnable = Runnable {
        if (mp?.isPlaying == false) {
            return@Runnable
        }
        if (oscillateVolume) {
            if (decreaseVolume) {
                decreaseForTick()
            }
            oscillateStereoForTick()
            mp?.setVolume(leftVolume, rightVolume)
        } else if (decreaseVolume) {
            decreaseForTick()
            leftVolume = maxVolume
            rightVolume = maxVolume
            mp?.setVolume(leftVolume, rightVolume)
        }
    }

    /**
     * If the player loses focus and it was playing, then set this true.
     * When and if we regain focus, you know to start playing again if this is true.
     */
    private var startPlayingWhenFocusRegained = false

    private lateinit var prefsUtil : PreferencesUtil
    /**
     * Handles audio focus changes. By default, the app will pause when it looses focus and start
     * again when it regains focus (when lost for a short period). The user has the ability to
     * disable this functionality.
     */
    private var focusChangeListener: AudioManager.OnAudioFocusChangeListener
            = AudioManager.OnAudioFocusChangeListener { focusChange ->
        val playOverOtherSound = prefsUtil.getBoolean(baseContext, Preferences.PLAY_OVER, false)
        if (focusChange > 0 && startPlayingWhenFocusRegained) {
            play()
            startPlayingWhenFocusRegained = false
        } else if (!playOverOtherSound && focusChange < 0 && isPlaying) {
            //we lost audio focus, stop playing
            pause()
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                    || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                //in these cases, we can expect to get audio back soon
                startPlayingWhenFocusRegained = true
            }
        }
    }

    /**
     * Determine whether player is playing audio.
     * @return true if player is playing. False if not.
     */
    val isPlaying: Boolean
        get() = mp != null && mp!!.isPlaying

    /**
     * Get the noise type being used by the media player.
     */
    /**
     * Set the noise to play.
     */
    var noiseType: NoiseType
        get() = mp?.noiseType ?: NoiseType.NONE
        set(noiseType) {
            mp?.noiseType = noiseType
            mp?.setVolume(maxVolume, maxVolume)
        }

    private var focusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent): IBinder? {
        if (mp == null) {
            mp = LoopMediaPlayer.create(this)
        }
        return binder
    }

    override fun onCreate() {
        val volumeChangerTimer = Timer()
        volumeChangerTimer.schedule(VolumeChangerTimerTask(), 0, tickPeriod)
        prefsUtil = PreferencesUtil()
    }

    override fun onDestroy() {
        mp?.stop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (mp == null) {
            mp = LoopMediaPlayer.create(this)
        }

        //if a button is pressed in the notification,
        //the service will be started with this extra
        if (intent?.hasExtra(Action.DO.key) == true) {
            val action = intent.extras?.get(Action.DO.key) as String
            when (action) {
                Action.PAUSE.key -> handleNotificationPause()
                Action.PLAY.key -> handleNotificationPlay()
                Action.CLOSE.key -> handleNotificationClose()
            }
        }

        return Service.START_STICKY
    }

    /**
     * Provides logic for when the notification is paused.
     * Pauses playback and timer. Updates notification.
     */
    private fun handleNotificationPause() {
        pause()
        //there's no way to pause the timer
        //just cancel it and start a new one (with the old time) if play is pressed
        cancelTimer()
        showNotification(false)
    }

    /**
     * Provides logic for when the play button is pressed on the notification.
     * Starts playing audio and starts timer if applicable. Updates notification.
     */
    private fun handleNotificationPlay() {
        play()
        //there was a timer before pause was pressed
        //start it again with the leftover time
        if (timeLeft > 0) {
            setTimer(timeLeft)
        }
        showNotification(true)
    }

    /**
     * Provides logic for when the close button is pressed on the notification.
     * Stops playback and timer. Dismisses notification.
     */
    private fun handleNotificationClose() {
        stop()
        stopTimer()
        dismissNotification()
    }

    /**
     * Start audio playback.
     * If user has correct settings, will ask for audio focus so that other audio streams will be
     * paused.
     */
    fun play() {
        mp?.play()
        setAudioFocus(true)
    }

    /**
     * Stop audio playback, if any.
     * Abandons audio focus so other services can play.
     */
    fun stop() {
        mp?.stop()
        setAudioFocus(false)
        //reset volumes to initial values
        leftVolume = initialVolume
        rightVolume = initialVolume
    }


    /**
     * Pause audio playback, if any.
     * Abandons audio focus so other services can play.
     */
    private fun pause() {
        mp?.pause()
        setAudioFocus(false)
    }

    /**
     * Every time we start playing, we have to request audio focus and listen for other
     * apps also listening for focus with the focusChangeListener.
     * When sound is paused, tell Android that our player no longer has focus. This will allow any
     * other player to gain focus and begin playing.
     */
    private fun setAudioFocus(focused: Boolean) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (focused) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let {
                    audioManager.abandonAudioFocusRequest(it)
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(focusChangeListener)
            }
        } else if (!prefsUtil.getBoolean(baseContext, Preferences.PLAY_OVER, false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setOnAudioFocusChangeListener(focusChangeListener)
                        .setAudioAttributes(playbackAttributes).build()
                audioManager.requestAudioFocus(focusRequest)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN)
            }
        }
    }

    /**
     * Dismiss all notifications created by our app.
     */
    fun dismissNotification() {
        (this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
    }

    /**
     * Set the maximum volume for this service.
     * If oscillating or decreasing volume are not being used,
     * this will be the volume. If they are being used, neither
     * will use a volume higher than this volume
     *
     * @param maxVolume a value 0.0 to 1.0 where 1.0 is max of the device
     */
    fun setMaxVolume(maxVolume: Float) {
        this.maxVolume = maxVolume
        leftVolume = maxVolume
        rightVolume = maxVolume
        oscillatingDown = true
        initialVolume = maxVolume
        mp?.setVolume(maxVolume, maxVolume)
        Log.d(LOG_TAG, java.lang.Float.toString(maxVolume))
    }


    /**
     * Set a CountDownTimer for playing audio.
     * @param millis time to play audio, in ms
     */
    fun setTimer(millis: Long) {
        timeLeft = millis
        decreaseLength = millis
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
            }

            override fun onFinish() {
                dismissNotification()
                timeLeft = 0
                mp?.stop()
            }
        }.start()
    }

    /**
     * Cancel timer, but don't clear any data about it (e.g. time left).
     */
    fun cancelTimer() {
        countDownTimer?.cancel()
    }

    /**
     * Cancel timer and clear any data about it (e.g. time left).
     */
    private fun stopTimer() {
        cancelTimer()
        decreaseLength = -1
        timeLeft = 0
    }

    /**
     * Tell player to oscillate (wave) the volume.
     * @param oscillateVolume true if volume should be oscillated, false if not
     */
    fun setOscillateVolume(oscillateVolume: Boolean) {
        this.oscillateVolume = oscillateVolume
    }

    /**
     * Tell player to decrease (fade) the volume.
     * @param decreaseVolume true if volume should be decreased.
     */
    fun setDecreaseVolume(decreaseVolume: Boolean) {
        this.decreaseVolume = decreaseVolume
    }

    /**
     * Oscillates volume in one speaker and then the other.
     * Example
     * left:  1.0 -> .2  -> 1.0
     * right: stays at 1.0
     * then
     * left:  stays at 1.0
     * right: 1.0 -> 2.0 -> 1.0
     */
    private fun oscillateStereoForTick() {
        val minVolume = initialVolume * minVolumePercent
        var delta = (maxVolume - minVolume) / (oscillatePeriod / 2 / tickPeriod)
        if (oscillatingDown) {
            delta *= -1
        }
        if (oscillatingLeft) {
            leftVolume += delta
        } else {
            rightVolume += delta
        }
        if (leftVolume <= minVolume || rightVolume <= minVolume) {
            if (oscillatingLeft) {
                leftVolume = minVolume
            } else {
                rightVolume = minVolume
            }
            oscillatingDown = false
        }
        if (leftVolume >= maxVolume && rightVolume >= maxVolume && !oscillatingDown) {
            if (oscillatingLeft) {
                leftVolume = maxVolume
            } else {
                rightVolume = maxVolume
            }
            oscillatingDown = true
            oscillatingLeft = !oscillatingLeft
        }
    }

    /**
     * Based on the minimum volume and clock interval, decrease the volume the appropriate amount
     * for this clock tick.
     */
    private fun decreaseForTick() {
        val minVolume = initialVolume * minVolumePercent
        if (maxVolume > minVolume) {
            val delta = if (decreaseLength == -1L) {
                -1 * (maxVolume - minVolume) / (DEFAULT_DECREASE_LENGTH / tickPeriod)
            } else {
                -1 * (maxVolume - minVolume) / (decreaseLength / tickPeriod)
            }
            maxVolume += delta
        }

        Log.d(LOG_TAG, java.lang.Float.toString(maxVolume))
    }

    /**
     * Set the period between oscillations.
     * @param oscillatePeriod in milliseconds
     */
    fun setOscillatePeriod(oscillatePeriod: Long) {
        this.oscillatePeriod = oscillatePeriod
    }

    /**
     * Add a notification channel to for this app.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(NotificationChannel(notificationChannel,
                "Noise Playing", NotificationManager.IMPORTANCE_LOW))
    }

    /**
     * Depending on player state, add actions (buttons) to a notification.
     */
    private fun addNotificationActions(builder: NotificationCompat.Builder, playing: Boolean) {
        val action = if (playing) Action.PAUSE  else Action.PLAY

        val pausePlayPendingIntent = PendingIntent.getService(this, 0,
            Intent(this, AudioPlayerService::class.java).putExtra(Action.DO.key, action.key),
            PendingIntent.FLAG_ONE_SHOT)

        val actionResID = if (playing) R.drawable.ic_action_playback_pause_black
            else R.drawable.ic_action_playback_play_black

        builder.addAction(actionResID, getString(R.string.audio_play), pausePlayPendingIntent)
    }

    /**
     * Show a notification with information about the sound being played/paused
     * and a pause button which will callback to this service.
     */
    fun showNotification(playing: Boolean) {
        createNotificationChannel()

        val openAppIntent = Intent(applicationContext, MainActivity::class.java)
        openAppIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        openAppIntent.putExtra("startedFromNotification", true)
        val openAppPendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                openAppIntent,
                PendingIntent.FLAG_ONE_SHOT)

        val title = getString(mp?.noiseType?.notificationTitle ?: R.string.blank)
        val darkMode = prefsUtil.getBoolean(baseContext, Preferences.DARK_MODE, false)
        var iconRes = R.mipmap.ic_launcher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            iconRes = if (darkMode) {
                R.drawable.gray
            } else {
                R.drawable.multi
            }
        }
        val icon = BitmapFactory.decodeResource(this.resources, iconRes)
        val mediaSession = MediaSessionCompat(applicationContext, "session tag")
        val token = mediaSession.sessionToken

        val closeIntent = Intent(this, AudioPlayerService::class.java)
        closeIntent.action = Action.CLOSE.key
        closeIntent.putExtra(Action.DO.key, Action.CLOSE.key)
        val closePendingIntent = PendingIntent.getService(
                applicationContext,
                0,
                closeIntent,
                PendingIntent.FLAG_ONE_SHOT)

        val builder = NotificationCompat.Builder(this, notificationChannel)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_statusbar2)
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(token))
                .setContentTitle(title)
                .setLargeIcon(icon)
                .setContentIntent(openAppPendingIntent)
                .setDeleteIntent(closePendingIntent)

        if (!darkMode) {
            // Colorization changes the background and text color based on the largeIcon.
            // It looks weird with the multi-colored icon we're using.
            builder.setColorized(false)
        }

        addNotificationActions(builder, playing)

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
        return
    }

    /**
     * Allows binding this service through a service connection.
     */
    inner class AudioPlayerBinder : Binder() {
        internal val service: AudioPlayerService
            get() = this@AudioPlayerService
    }

    /**
     * Runs a periodic task on the main thread to adjust the volume based on settings such as
     * oscillate and fade.
     */
    internal inner class VolumeChangerTimerTask : TimerTask() {
        override fun run() {
            val volumeHandler = Handler(Looper.getMainLooper())
            volumeHandler.post(volumeRunnable)
        }
    }

    companion object {

        /**
         * Use this instead of decreaseLength if no timer exists.
         */
        val DEFAULT_DECREASE_LENGTH = TimeUnit.HOURS.toMillis(1)
        private val LOG_TAG = AudioPlayerService::class.java.simpleName
        /**
         * Used when showing the notification.
         * Unique within the app.
         * If multiple notify()s are called with the same id,
         * new one will replace the old ones.
         */
        private val NOTIFICATION_ID = 0

        enum class Action (val key: String) {
            PAUSE("pause"),
            CLOSE("close"),
            PLAY("play"),
            DO("do")
        }
    }
}
