package dalbers.com.noise.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dalbers.com.noise.audiocontrol.AudioFocusManager
import dalbers.com.noise.audiocontrol.AudioFocusManagerImpl
import dalbers.com.noise.audiocontrol.AudioPlayer
import dalbers.com.noise.audiocontrol.AudioPlayerImpl
import dalbers.com.noise.shared.UserPreferences
import dalbers.com.noise.shared.UserPreferencesImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioBindingsModule {
    @Binds
    abstract fun bindAudioPlayer(impl: AudioPlayerImpl): AudioPlayer

    @Binds
    abstract fun bindAudioFocusManager(impl: AudioFocusManagerImpl): AudioFocusManager

    @Binds
    abstract fun bindUserPreferences(impl: UserPreferencesImpl): UserPreferences
}
