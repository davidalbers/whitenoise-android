@file:OptIn(ExperimentalAnimationApi::class)

package dalbers.com.noise.shared

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.preference.PreferenceManager
import com.alorma.compose.settings.storage.preferences.rememberPreferenceIntSettingState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import dalbers.com.noise.playerscreen.view.PlayerScreen
import dalbers.com.noise.playerscreen.viewmodel.PlayerScreenViewModel
import dalbers.com.noise.service.AudioPlayerService
import dalbers.com.noise.settings.view.SettingsScreen
import dalbers.com.noise.settings.view.isDarkMode

class MainActivity : AppCompatActivity() {
    private val playerViewModel by viewModels<PlayerScreenViewModel> {
        WhiteNoiseViewModelFactory(
            this,
            UserPreferencesImpl(PreferenceManager.getDefaultSharedPreferences(applicationContext)),
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

        setContent {
            val darkState = rememberPreferenceIntSettingState(
                key = PREF_USE_DARK_MODE_KEY,
                defaultValue = 0
            )

            val navController = rememberAnimatedNavController()
            WhiteNoiseTheme(darkTheme = darkState.isDarkMode()) {
                AnimatedNavHost(
                    navController = navController,
                    startDestination = NavigationDestination.PLAYER.key,
                    enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Left) },
                    exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Left) },
                    popEnterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Right) },
                    popExitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Right) },
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








