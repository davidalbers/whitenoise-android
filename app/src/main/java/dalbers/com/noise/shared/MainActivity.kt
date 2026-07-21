package dalbers.com.noise.shared

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.alorma.compose.settings.storage.preferences.rememberPreferenceIntSettingState
import dagger.hilt.android.AndroidEntryPoint
import dalbers.com.noise.playerscreen.view.PlayerScreen
import dalbers.com.noise.playerscreen.viewmodel.PlayerScreenViewModel
import dalbers.com.noise.service.AudioPlayerService
import dalbers.com.noise.settings.view.isDarkMode
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var userPreferences: UserPreferences
    private val versionProvider = VersionProvider(this)

    private val playerViewModel by viewModels<PlayerScreenViewModel>()

    private var service: AudioPlayerService? = null
    private val playerConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            binder: IBinder
        ) {
            val audioPlayerBinder = binder as AudioPlayerService.AudioPlayerBinder
            service = audioPlayerBinder.service
        }

        override fun onServiceDisconnected(className: ComponentName) {
            service = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val serviceIntent = Intent(this, AudioPlayerService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, playerConnection, BIND_AUTO_CREATE)
        userPreferences.migrateLegacyPreferences()

        setContent {
            val darkState = rememberPreferenceIntSettingState(
                key = PREF_USE_DARK_MODE_KEY,
                defaultValue = DarkModeSetting.AUTO.key,
            )

            WhiteNoiseTheme(darkTheme = darkState.isDarkMode()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PlayerScreen(
                        viewModel = playerViewModel,
                        version = versionProvider.getVersion(),
                        darkThemeState = darkState,
                        onOpenProjectPage = {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/davidalbers/whitenoise-android")
                                )
                            )
                        },
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        service?.handoffControl()
    }

    override fun onStart() {
        super.onStart()
        service?.dismissNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        service = null
        unbindService(playerConnection)
    }
}
