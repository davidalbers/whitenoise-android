package dalbers.com.noise.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import dagger.hilt.android.AndroidEntryPoint
import dalbers.com.noise.AudioPlayerButton
import dalbers.com.noise.AudioPlayerViewModel
import dalbers.com.noise.R
import dalbers.com.noise.audiocontrol.AudioController
import dalbers.com.noise.service.model.AudioPlayerScreenState
import dalbers.com.noise.shared.MainActivity
import javax.inject.Inject

/**
 * A service to play audio on a loop.
 * Features oscillating and decreasing volume.
 * Uses LoopMediaPlayer for looping audio.
 */
@AndroidEntryPoint
class AudioPlayerService : LifecycleService() {
    private val binder: IBinder = AudioPlayerBinder()

    @Inject
    lateinit var audioController: AudioController

    @Inject
    lateinit var viewModel: AudioPlayerViewModel
    private var wakeLock: WakeLock? = null

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        viewModel.stateLiveData.observe(this) {
            if (it is AudioPlayerScreenState.Shown) {
                showNotification(it)
            } else {
                dismissNotification()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioController.pause()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        super.onStartCommand(intent, flags, startId)
        val pm =
            baseContext.getSystemService(
                POWER_SERVICE,
            ) as? PowerManager
        if (pm != null) {
            wakeLock =
                pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "$WAKE_LOCK_TAG:AudioPlayerService",
                )
        }

        // if a button is pressed in the notification,
        // the service will be started with this extra
        safeValueOf<NotificationAction>(intent?.extras?.getString(DO_ACTION).orEmpty())?.let {
            viewModel.handleNotificationAction(it)
        }

        createNotificationChannel()
        return START_STICKY
    }

    /**
     * Dismiss all notifications created by our app.
     */
    fun dismissNotification() {
        // takes the service out of the foreground and removes the notification
        stopForeground(true)
        if (wakeLock!!.isHeld) {
            wakeLock!!.release()
        }
    }

    fun handoffControl() {
        viewModel.enableNotification()
    }

    /**
     * Show a notification with information about the sound being played/paused
     * and a pause button which will callback to this service.
     */
    private fun showNotification(screenState: AudioPlayerScreenState.Shown) {
        val icon =
            BitmapFactory.decodeResource(
                this.resources,
                R.mipmap.ic_launcher,
            )
        val mediaSession = MediaSessionCompat(applicationContext, MEDIA_SESSION_TAG)

        val notification: Notification
        val openAppIntent = Intent(applicationContext, MainActivity::class.java)
        openAppIntent.flags = (
            Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
        openAppIntent.putExtra("startedFromNotification", true)
        val openAppPendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                screenState.titleResource,
                openAppIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
            )
        val builder =
            NotificationCompat
                .Builder(this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_statusbar2)
                .setContentTitle(getString(screenState.titleResource))
                .setContentText(getString(screenState.subtitleResource))
                .setLargeIcon(icon)
                .setStyle(
                    androidx.media.app.NotificationCompat
                        .MediaStyle()
                        .setMediaSession(mediaSession.sessionToken),
                ).setOngoing(true)
                .addAudioPlayerButton(screenState.firstButton)
                .addAudioPlayerButton(screenState.secondButton)
                .setContentIntent(openAppPendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notification = builder.build()

        // show the notification and bring service to foreground
        startForeground(NOTIFICATION_ID, notification)
        wakeLock!!.acquire()
    }

    private fun NotificationCompat.Builder.addAudioPlayerButton(button: AudioPlayerButton): NotificationCompat.Builder {
        val pausePlayIntent = Intent(this@AudioPlayerService, AudioPlayerService::class.java)
        pausePlayIntent.putExtra(DO_ACTION, button.action.name)
        val pausePlayPendingIntent =
            PendingIntent.getService(
                this@AudioPlayerService,
                button.textResource,
                pausePlayIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
            )
        return addAction(
            button.iconResource,
            getString(button.textResource),
            pausePlayPendingIntent,
        )
    }

    /**
     * Add a notification channel to for this app.
     */
    private fun createNotificationChannel() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager?)?.apply {
            createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL,
                    "Noise Playing",
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }
    }

    /**
     * Allows binding this service through a service connection.
     */
    inner class AudioPlayerBinder : Binder() {
        val service: AudioPlayerService
            get() = this@AudioPlayerService
    }

    companion object {
        /**
         * Used when showing the notification.
         * Unique within the app.
         * If multiple notify()s are called with the same id,
         * new one will replace the old ones.
         */
        private const val NOTIFICATION_ID = 1
        private const val DO_ACTION = "do"
        private const val WAKE_LOCK_TAG = "dalbers.noise.wakelock"
        private const val NOTIFICATION_CHANNEL = "com.dalbers.whitenoise.Notifications"
        private const val MEDIA_SESSION_TAG = "dalbers.media.session"
    }
}

enum class NotificationAction {
    PAUSE_ACTION,
    CLOSE_ACTION,
    PLAY_ACTION,
}

inline fun <reified T : Enum<T>> safeValueOf(type: String): T? =
    try {
        java.lang.Enum.valueOf(T::class.java, type)
    } catch (e: IllegalArgumentException) {
        null
    }
