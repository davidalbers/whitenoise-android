package dalbers.com.noise

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import dalbers.com.noise.audiocontrol.AudioController
import dalbers.com.noise.audiocontrol.SoundState
import dalbers.com.noise.service.NotificationAction
import dalbers.com.noise.service.model.AudioPlayerScreenState
import dalbers.com.noise.shared.NoiseType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AudioPlayerViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val audioController: AudioController = mockk(relaxed = true)
    private lateinit var stateFlow: MutableStateFlow<SoundState>

    private fun createViewModel(initialState: SoundState = SoundState.default): AudioPlayerViewModel {
        stateFlow = MutableStateFlow(initialState)
        every { audioController.stateFlow } returns stateFlow
        return AudioPlayerViewModel(audioController)
    }

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is hidden`() {
        val viewModel = createViewModel()

        assertEquals(AudioPlayerScreenState.Hidden, viewModel.stateLiveData.value)
    }

    @Test
    fun `enableNotification while not playing keeps state hidden`() {
        val viewModel = createViewModel(SoundState.default.copy(playing = false))

        viewModel.enableNotification()

        assertEquals(AudioPlayerScreenState.Hidden, viewModel.stateLiveData.value)
    }

    @Test
    fun `enableNotification while playing shows player with pause and close buttons`() {
        val viewModel = createViewModel(SoundState.default.copy(playing = true, noiseType = NoiseType.RAIN))

        viewModel.enableNotification()

        val state = viewModel.stateLiveData.value
        assertTrue(state is AudioPlayerScreenState.Shown)
        state as AudioPlayerScreenState.Shown
        assertEquals(NoiseType.RAIN.notificationTitle, state.titleResource)
        assertEquals(AudioPlayerButton.pause.action, state.firstButton.action)
        assertEquals(AudioPlayerButton.close.action, state.secondButton.action)
    }

    @Test
    fun `state updates are ignored until notification is enabled`() {
        val viewModel = createViewModel(SoundState.default.copy(playing = false))

        stateFlow.value = stateFlow.value.copy(playing = true)

        assertEquals(AudioPlayerScreenState.Hidden, viewModel.stateLiveData.value)
    }

    @Test
    fun `state updates after enableNotification reflect playing transitions`() {
        val viewModel = createViewModel(SoundState.default.copy(playing = true))
        viewModel.enableNotification()

        stateFlow.value = stateFlow.value.copy(playing = false)

        val state = viewModel.stateLiveData.value
        assertTrue(state is AudioPlayerScreenState.Shown)
        state as AudioPlayerScreenState.Shown
        assertEquals(AudioPlayerButton.play.action, state.firstButton.action)
    }

    @Test
    fun `handleNotificationAction PAUSE_ACTION pauses and cancels the timer`() {
        val viewModel = createViewModel()

        viewModel.handleNotificationAction(NotificationAction.PAUSE_ACTION)

        verify { audioController.pause() }
        verify { audioController.cancelTimer() }
    }

    @Test
    fun `handleNotificationAction PLAY_ACTION plays and resumes the remaining timer`() {
        val viewModel = createViewModel(SoundState.default.copy(millisLeft = 12_345L))

        viewModel.handleNotificationAction(NotificationAction.PLAY_ACTION)

        verify { audioController.play() }
        verify { audioController.setTimer(12_345L) }
    }

    @Test
    fun `handleNotificationAction CLOSE_ACTION pauses, stops the timer and hides the player`() {
        val viewModel = createViewModel(SoundState.default.copy(playing = true))
        viewModel.enableNotification()

        viewModel.handleNotificationAction(NotificationAction.CLOSE_ACTION)

        verify { audioController.pause() }
        verify { audioController.stopTimer() }
        assertEquals(AudioPlayerScreenState.Hidden, viewModel.stateLiveData.value)
    }

    @Test
    fun `state updates are ignored again after CLOSE_ACTION disables notification`() {
        val viewModel = createViewModel(SoundState.default.copy(playing = true))
        viewModel.enableNotification()
        viewModel.handleNotificationAction(NotificationAction.CLOSE_ACTION)

        stateFlow.value = stateFlow.value.copy(playing = false)

        assertEquals(AudioPlayerScreenState.Hidden, viewModel.stateLiveData.value)
    }
}
