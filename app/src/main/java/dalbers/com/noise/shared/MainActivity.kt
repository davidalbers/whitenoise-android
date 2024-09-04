package dalbers.com.noise.shared

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.alorma.compose.settings.storage.preferences.rememberPreferenceIntSettingState
import dalbers.com.noise.playerscreen.view.PlayerScreen
import dalbers.com.noise.playerscreen.viewmodel.PlayerScreenViewModel
import dalbers.com.noise.service.AudioPlayerService
import dalbers.com.noise.settings.view.SettingsScreen
import dalbers.com.noise.settings.view.isDarkMode

class MainActivity : AppCompatActivity() {
    private lateinit var userPreferences: UserPreferences

    private val playerViewModel by viewModels<PlayerScreenViewModel> {
        WhiteNoiseViewModelFactory(
            this,
            userPreferences,
            intent.extras,
        )
    }

    private var service: AudioPlayerService? = null
    private val playerConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            binder: IBinder
        ) {
            val audioPlayerBinder = binder as AudioPlayerService.AudioPlayerBinder
            service = audioPlayerBinder.service
            playerViewModel.bindAudioController(audioPlayerBinder.service.audioController)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            playerViewModel.clearAudioController()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, AudioPlayerService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, playerConnection, BIND_AUTO_CREATE)
        userPreferences = UserPreferencesImpl(PreferenceManager.getDefaultSharedPreferences(this))
        userPreferences.migrateLegacyPreferences()

        setContent {
            val darkState = rememberPreferenceIntSettingState(
                key = PREF_USE_DARK_MODE_KEY,
                defaultValue = DarkModeSetting.AUTO.key,
            )

            val navController = rememberNavController()
            WhiteNoiseTheme(darkTheme = darkState.isDarkMode()) {
                NavHost(
                    navController = navController,
                    startDestination = NavigationDestination.PLAYER.key,
                    enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                    exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
                    popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) },
                    popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) },
                ) {
                    composable(
                        NavigationDestination.PLAYER.key,
                    ) {
                        PlayerScreen(playerViewModel) {
                            navController.navigate(NavigationDestination.SETTINGS.key)
                        }
                    }
                    composable(
                        NavigationDestination.SETTINGS.key,
                    ) {
                        SettingsScreen(darkThemeState = darkState) {
                            navController.popBackStack()
                        }
                    }
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
        service?.run {
            dismissNotification()
            playerViewModel.bindAudioController(audioController)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        service = null
        //unbind the service, it will still be running
        unbindService(playerConnection)
    }
}








