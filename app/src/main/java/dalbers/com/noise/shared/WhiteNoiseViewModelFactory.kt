package dalbers.com.noise.shared

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import dalbers.com.noise.playerscreen.viewmodel.PlayerScreenViewModel

// todo: Use dagger instead of this
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