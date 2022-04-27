package dalbers.com.noise.shared

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import dalbers.com.noise.playerscreen.viewmodel.PlayerScreenViewModel

// TODO: Use DI instead of this https://github.com/davidalbers/whitenoise-android/issues/41
class WhiteNoiseViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val userPreferences: UserPreferences,
    defaultArgs: Bundle? = null
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(
        key: String, modelClass: Class<T>, handle: SavedStateHandle
    ): T {
        return PlayerScreenViewModel(userPreferences) as T
    }
}