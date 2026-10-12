package dalbers.com.noise.playerscreen.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import dalbers.com.noise.audiocontrol.AudioController
import dalbers.com.noise.audiocontrol.SoundState
import dalbers.com.noise.playerscreen.view.TimerPickerState
import dalbers.com.noise.playerscreen.view.TimerToggleState
import dalbers.com.noise.shared.NoiseType
import dalbers.com.noise.shared.UserPreferences
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerScreenViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val audioController: AudioController = mockk(relaxed = true)
    private lateinit var audioControllerStateFlow: MutableStateFlow<SoundState>

    private fun createViewModel(initialSoundState: SoundState = SoundState.default): PlayerScreenViewModel {
        audioControllerStateFlow = MutableStateFlow(initialSoundState)
        every { audioController.stateFlow } returns audioControllerStateFlow
        return PlayerScreenViewModel(userPreferences, audioController)
    }

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { userPreferences.lastUsedColor } returns NoiseType.WHITE
        every { userPreferences.lastUsedVolume } returns 1f
        every { userPreferences.lastUsedWavy } returns false
        every { userPreferences.lastUsedFade } returns false
        every { userPreferences.lastTimerTimeMillis } returns 0L
        every { userPreferences.timerEnabled } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init applies saved preferences to the audio controller`() {
        every { userPreferences.lastUsedColor } returns NoiseType.PINK
        every { userPreferences.lastUsedVolume } returns 0.5f
        every { userPreferences.lastUsedWavy } returns true
        every { userPreferences.lastUsedFade } returns true

        createViewModel()

        verify { audioController.setNoiseType(NoiseType.PINK) }
        verify { audioController.setVolume(0.5f) }
        verify { audioController.setWaves(true) }
        verify { audioController.setFade(true) }
    }

    @Test
    fun `init resumes a saved timer when timer was enabled`() {
        every { userPreferences.lastTimerTimeMillis } returns 60_000L
        every { userPreferences.timerEnabled } returns true

        createViewModel()

        verify { audioController.setTimer(60_000L) }
    }

    @Test
    fun `init does not set a timer when no timer was saved`() {
        every { userPreferences.lastTimerTimeMillis } returns 0L
        every { userPreferences.timerEnabled } returns true

        createViewModel()

        verify(exactly = 0) { audioController.setTimer(any()) }
    }

    @Test
    fun `init does not set a timer when timer was disabled`() {
        every { userPreferences.lastTimerTimeMillis } returns 60_000L
        every { userPreferences.timerEnabled } returns false

        createViewModel()

        verify(exactly = 0) { audioController.setTimer(any()) }
    }

    @Test
    fun `init shows a restored timer as saved`() {
        every { userPreferences.lastTimerTimeMillis } returns 60_000L
        every { userPreferences.timerEnabled } returns true
        every { audioController.setTimer(60_000L) } answers {
            audioControllerStateFlow.value = audioControllerStateFlow.value.copy(millisLeft = 60_000L)
        }

        val viewModel = createViewModel()

        assertEquals(TimerToggleState.Saved("0:01:00"), viewModel.playerScreenState.value?.timerToggleState)
    }

    @Test
    fun `init keeps a timer that is already running instead of restarting it`() {
        every { userPreferences.lastTimerTimeMillis } returns 60_000L
        every { userPreferences.timerEnabled } returns true

        val viewModel = createViewModel(SoundState.default.copy(playing = true, millisLeft = 30_000L))

        verify(exactly = 0) { audioController.setTimer(any()) }
        assertEquals(TimerToggleState.Saved("0:00:30"), viewModel.playerScreenState.value?.timerToggleState)
    }

    @Test
    fun `playerScreenState reflects audio controller state changes`() {
        val viewModel = createViewModel()

        audioControllerStateFlow.value =
            audioControllerStateFlow.value.copy(
                noiseType = NoiseType.BROWN,
                playing = true,
                volume = 0.3f,
                wavesEnabled = true,
                fadeEnabled = true,
                millisLeft = 5_000L,
            )

        val state = viewModel.playerScreenState.value
        assertEquals(NoiseType.BROWN, state?.noiseType)
        assertTrue(state?.playing == true)
        assertEquals(0.3f, state?.volume)
        assertTrue(state?.wavesEnabled == true)
        assertTrue(state?.fadeEnabled == true)
    }

    @Test
    fun `changeNoiseType persists the preference and updates the audio controller`() {
        val viewModel = createViewModel()

        viewModel.changeNoiseType(NoiseType.BROWN)

        verify { userPreferences.lastUsedColor = NoiseType.BROWN }
        verify { audioController.setNoiseType(NoiseType.BROWN) }
    }

    @Test
    fun `toggleFade persists the preference and updates the audio controller`() {
        val viewModel = createViewModel()

        viewModel.toggleFade(true)

        verify { userPreferences.lastUsedFade = true }
        verify { audioController.setFade(true) }
    }

    @Test
    fun `toggleWaves persists the preference and updates the audio controller`() {
        val viewModel = createViewModel()

        viewModel.toggleWaves(true)

        verify { userPreferences.lastUsedWavy = true }
        verify { audioController.setWaves(true) }
    }

    @Test
    fun `changeVolume persists the preference and updates the audio controller`() {
        val viewModel = createViewModel()

        viewModel.changeVolume(0.7f)

        verify { userPreferences.lastUsedVolume = 0.7f }
        verify { audioController.setVolume(0.7f) }
    }

    @Test
    fun `toggleTimer when disabled shows the picker seeded from the saved timer`() {
        every { userPreferences.lastTimerTimeMillis } returns (90 * 60_000L)
        val viewModel = createViewModel()

        viewModel.toggleTimer()

        val state = viewModel.playerScreenState.value
        assertTrue(state?.showTimerPicker == true)
        assertEquals(TimerPickerState(hours = 1, minutesTens = 3, minutes = 0), state?.timerPickerState)
    }

    @Test
    fun `toggleTimer when saved clears the timer`() {
        val viewModel = createViewModel()
        viewModel.toggleTimer()
        viewModel.updateTimer(30)
        viewModel.setTimer()

        viewModel.toggleTimer()

        verify { audioController.setTimer(0) }
        verify { userPreferences.timerEnabled = false }
        assertEquals(TimerToggleState.Disabled, viewModel.playerScreenState.value?.timerToggleState)
    }

    @Test
    fun `updateTimer increases the picker minutes`() {
        val viewModel = createViewModel()
        viewModel.toggleTimer()

        viewModel.updateTimer(5)

        assertEquals(TimerPickerState(hours = 0, minutesTens = 0, minutes = 5), viewModel.playerScreenState.value?.timerPickerState)
    }

    @Test
    fun `updateTimer ignores changes that would go negative`() {
        val viewModel = createViewModel()
        viewModel.toggleTimer()
        val before = viewModel.playerScreenState.value?.timerPickerState

        viewModel.updateTimer(-5)

        assertEquals(before, viewModel.playerScreenState.value?.timerPickerState)
    }

    @Test
    fun `setTimer commits the picker value and hides the picker`() {
        val viewModel = createViewModel()
        viewModel.toggleTimer()
        viewModel.updateTimer(45)

        viewModel.setTimer()

        val state = viewModel.playerScreenState.value
        assertFalse(state?.showTimerPicker == true)
        assertEquals(TimerToggleState.Saved("0:45:00"), state?.timerToggleState)
        verify { audioController.setTimer(45 * 60_000L) }
        verify { userPreferences.lastTimerTimeMillis = 45 * 60_000L }
        verify { userPreferences.timerEnabled = true }
    }

    @Test
    fun `setTimer with zero minutes disables the timer`() {
        val viewModel = createViewModel()
        viewModel.toggleTimer()

        viewModel.setTimer()

        verify { userPreferences.timerEnabled = false }
        assertEquals(TimerToggleState.Disabled, viewModel.playerScreenState.value?.timerToggleState)
    }

    @Test
    fun `setTimer does nothing when the picker is not open`() {
        val viewModel = createViewModel()

        viewModel.setTimer()

        verify(exactly = 0) { audioController.setTimer(any()) }
    }

    @Test
    fun `cancelTimer hides the picker and clears the saved timer`() {
        val viewModel = createViewModel()
        viewModel.toggleTimer()

        viewModel.cancelTimer()

        assertFalse(viewModel.playerScreenState.value?.showTimerPicker == true)
        assertEquals(TimerToggleState.Disabled, viewModel.playerScreenState.value?.timerToggleState)
        verify { userPreferences.lastTimerTimeMillis = 0L }
        verify { userPreferences.timerEnabled = false }
    }

    @Test
    fun `timer display counts down with the audio controller and resets when it finishes`() {
        val viewModel = createViewModel()
        viewModel.toggleTimer()
        viewModel.updateTimer(1)
        viewModel.setTimer()

        audioControllerStateFlow.value = audioControllerStateFlow.value.copy(millisLeft = 59_000L)
        assertEquals(TimerToggleState.Saved("0:00:59"), viewModel.playerScreenState.value?.timerToggleState)

        audioControllerStateFlow.value = audioControllerStateFlow.value.copy(millisLeft = 0L)
        assertEquals(TimerToggleState.Disabled, viewModel.playerScreenState.value?.timerToggleState)
    }

    @Test
    fun `togglePlay true plays the audio`() {
        val viewModel = createViewModel()

        viewModel.togglePlay(true)

        verify { audioController.play() }
        verify(exactly = 0) { audioController.pause() }
    }

    @Test
    fun `togglePlay false pauses the audio`() {
        val viewModel = createViewModel()

        viewModel.togglePlay(false)

        verify { audioController.pause() }
        verify(exactly = 0) { audioController.play() }
    }
}
