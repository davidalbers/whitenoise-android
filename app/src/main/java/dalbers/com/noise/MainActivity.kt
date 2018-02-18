package dalbers.com.noise

import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Toast
import dalbers.com.timerpicker.TimerPickerDialogFragment
import dalbers.com.timerpicker.TimerPickerDialogListener
import kotlinx.android.synthetic.main.general_layout.*

class MainActivity : AppCompatActivity(), TimerPickerDialogListener, ServiceConnection,
        SeekBar.OnSeekBarChangeListener {

    private var timerActive = false
    private var handler = Handler()
    private var audioPlayerService: AudioPlayerService? = null
    private var editTextCountDownTimer: CountDownTimer? = null
    /**
     * If the user has never turned on oscillate option, this is true.
     */
    private var oscillateNeverOn = false
    /**
     * If the user has never turned on fade option, this is true.
     */
    private var fadeNeverOn = false
    private var useDarkMode = false
    private var usingDarkMode = false
    private var oscillateInterval = 8000L

    private val sharedPrefListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, _ ->
        loadPreferences()
    }

    private var isPlayerConnectionBound = false
    private var timerCreatedAndNotStarted = false

    private val noiseChangeListener = RadioGroup.OnCheckedChangeListener { _, checkedId ->
        val noiseType = NoiseType.fromId(checkedId)
        audioPlayerService?.let {
            it.noiseType = noiseType
        }
        prefsUtil.use(baseContext) {
            it.putString(Preferences.COLOR.key, noiseType.prefValue)
        }
    }

    private val playPic by lazy {
        ContextCompat.getDrawable(this, R.drawable.ic_action_playback_play_black)
    }
    private val pausePic by lazy {
        ContextCompat.getDrawable(this, R.drawable.ic_action_playback_pause_black)
    }
    private val stopPic by lazy {
        ContextCompat.getDrawable(this, R.drawable.ic_clear)
    }
    private val addPic by lazy {
        ContextCompat.getDrawable(this, R.drawable.ic_add)
    }

    private lateinit var prefsUtil: PreferencesUtil


    override fun timeSet(timeInMillis: Long) {
        //ignore zero
        if (timeInMillis != 0L) {
            timerActive = true
            setTimerUiAdded(timeInMillis)

            audioPlayerService?.let {
                if (it.isPlaying) {
                    startTimer(timeInMillis)
                }
            }
        }
        prefsUtil.use(baseContext) { it.putLong(Preferences.TIME.key, timeInMillis) }
    }

    override fun dialogCanceled() {
        // nothing to do
    }

    override fun onServiceConnected(className: ComponentName, binder: IBinder) {
        val audioPlayerBinder = binder as AudioPlayerService.AudioPlayerBinder
        audioPlayerService = audioPlayerBinder.service

        if (audioPlayerService?.noiseType == NoiseType.NONE) {
            audioPlayerService?.noiseType = NoiseType.fromId(noiseTypes.checkedRadioButtonId)
        }
        if (audioPlayerService?.isPlaying == true) {
            setPlayButtonPause()
        } else {
            setPlayButtonPlay()
        }

        audioPlayerService?.setOscillatePeriod(oscillateInterval)
        audioPlayerService?.setOscillateVolume(waveVolumeToggle.isChecked)
        audioPlayerService?.setDecreaseVolume(decreaseVolumeToggle.isChecked)
        audioPlayerService?.setMaxVolume(
                calculateVolumePercent(volumeBar.progress, volumeBar))

        //sync UI with service's chosen sound file
        setUiBasedOnServiceState()
    }

    override fun onServiceDisconnected(className: ComponentName) {
        audioPlayerService = null
    }

    /**
     * When the service is bound, get relevant information from the service and set the UI based
     * on it. This is useful if the activity has been killed but the service was running in the
     * background.
     */
    private fun setUiBasedOnServiceState() {
        audioPlayerService?.let {
            //sync up play state
            if (it.isPlaying) {
                setPlayButtonPause()
            } else {
                setPlayButtonPlay()
            }
            //sync up timer
            val timeLeft = it.timeLeft
            //still time left
            if (timeLeft > 0) {
                //match the visual timer to the service timer
                if (it.isPlaying) {
                    setTimerUiAdded(timeLeft)
                    startTimer(timeLeft)
                } else {
                    //cancel the visual timer since the service timer is also
                    pauseTimer()
                }
            } else if (!timerCreatedAndNotStarted) {
                //service says timer is unset, check shared pref for any previously used times
                val lastTime = prefsUtil.getLong(baseContext, Preferences.TIME, 0L)
                if (lastTime > 0) {
                    setTimerUiAdded(lastTime)
                    timerActive = true
                } else {
                    timerTextView.time = 0
                    timerActive = false
                    setTimerUiUnsetState()
                }
            }
        }
    }

    /**
     * Map the seekbar's progress to a [0.0, 1.0] scale for volume.
     * @param progress Seekbar's progress
     * @param seekBar Seekbar to check
     * @return a float in range [0.0, 1.0] where 0.0 is min volume and 1.0 is max
     */
    private fun calculateVolumePercent(progress: Int, seekBar: SeekBar): Float {
        return (progress.toFloat() / seekBar.max)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val percentScrolled = calculateVolumePercent(progress, seekBar)
        audioPlayerService?.setMaxVolume(percentScrolled)

        prefsUtil.use(baseContext) { it.putFloat(Preferences.VOLUME.key, percentScrolled) }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        //Don't care about when tracking stops/starts only when progress changes
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        //Don't care about when tracking stops/starts only when progress changes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefsUtil = PreferencesUtil()

        prefsUtil.setChangeListener(baseContext, sharedPrefListener)
        loadPreferences()

        setTheme(if (useDarkMode) R.style.Dark else R.style.AppTheme)
        usingDarkMode = useDarkMode
        setContentView(R.layout.activity_main)

        waveVolumeToggle.setOnCheckedChangeListener { _, b -> oscillateChecked(b) }
        decreaseVolumeToggle.setOnCheckedChangeListener { _, b -> fadeChecked(b) }
        timerButton.setOnClickListener { onTimerClick() }
        btnPlay.setOnClickListener { playButtonClick() }

        setUiBasedOnPrefs()

        val serviceIntent = Intent(this, AudioPlayerService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)
        isPlayerConnectionBound = true

        volumeBar.setOnSeekBarChangeListener(this)
        noiseTypes.setOnCheckedChangeListener(noiseChangeListener)

        timerCreatedAndNotStarted = false
        savedInstanceState?.let {
            // Restore value of members from saved state
            val currTime = it.getLong(Preferences.STATE_TIMER_TIME.key)
            timerCreatedAndNotStarted = it.getBoolean(Preferences.STATE_TIMER_TIME.key)
            // If the app restarted while a timer was created but not started,
            // recreate the view state
            // ignore zero times because the user did not create those.
            if (timerCreatedAndNotStarted) {
                timerActive = timerCreatedAndNotStarted
                setTimerUiAdded(currTime)
            }
        }
    }

    /**
     * Look at shared prefs and update UI to the last saved state.
     */
    private fun setUiBasedOnPrefs() {
        val defaultColor = NoiseType.WHITE.prefValue
        val lastColor = prefsUtil.getString(baseContext, Preferences.COLOR, defaultColor)
        val noiseType = NoiseType.fromPrefValue(lastColor)
        noiseTypes.check(noiseType.id)

        val lastVolume = prefsUtil.getFloat(baseContext, Preferences.VOLUME, .5f)
        volumeBar.progress = (volumeBar.max * lastVolume).toInt()

        decreaseVolumeToggle.isChecked = prefsUtil.getBoolean(baseContext, Preferences.FADE, false)

        waveVolumeToggle.isChecked = prefsUtil.getBoolean(baseContext, Preferences.WAVY, false)

        val lastTime = prefsUtil.getLong(baseContext, Preferences.TIME, 0L)
        if (lastTime > 0) {
            setTimerUiAdded(lastTime)
            timerActive = true
        }
    }

    /**
     * Handle the oscillate button being checked by setting the oscillate option and possibly
     * explaining what it does if the user has never used it.
     * @param isChecked is checked button checked
     */
    private fun oscillateChecked(isChecked: Boolean) {
        audioPlayerService?.setOscillateVolume(isChecked)
        if (oscillateNeverOn) {
            Toast.makeText(
                    this,
                    getString(R.string.wave_volume_toast),
                    Toast.LENGTH_LONG)
                    .show()
            //update oscillateNeverOn to false in locally and in settings
            oscillateNeverOn = false
            prefsUtil.use(baseContext) {
                it.putBoolean(Preferences.OSCILLATE_NEVER_ON.key, false)
            }
        }
        prefsUtil.use(baseContext) { it.putBoolean(Preferences.WAVY.key, isChecked) }
    }

    /**
     * Handle the fade button being checked by setting the fade option and possibly
     * explaining what it does if the user has never used it.
     * @param isChecked is checked button checked
     */
    private fun fadeChecked(isChecked: Boolean) {
        audioPlayerService?.setDecreaseVolume(isChecked)
        if (fadeNeverOn) {
            Toast.makeText(
                    this,
                    getString(R.string.fade_volume_toast),
                    Toast.LENGTH_LONG)
                    .show()
            //update fadeNeverOn to false in locally and in settings
            fadeNeverOn = false
            prefsUtil.use(baseContext) {
                it.putBoolean(Preferences.FADE_NEVER_ON.key, false)
            }
        }
        prefsUtil.use(baseContext) { it.putBoolean(Preferences.FADE.key, isChecked) }
    }

    /**
     * Handle the timer button being clicked by either showing the timer picker dialog or clearing
     * the timer.
     */
    private fun onTimerClick() {
        if (!timerActive) {
            showPickerDialog()
        } else {
            timerActive = false
            setTimerUiUnsetState()
            stopTimer()
            //if playing audio, set button to play
            setPlayButtonPlay()
            prefsUtil.use(baseContext) { it.putLong(Preferences.TIME.key, 0L) }
        }
    }


    /**
     * Handle the play button being clicked by either playing or pausing audio.
     */
    private fun playButtonClick() {
        audioPlayerService?.let {
            if (it.isPlaying) {
                it.stop()
                setPlayButtonPlay()
                pauseTimer()
            } else {
                it.play()
                setPlayButtonPause()
                val time = timerTextView.time
                //timer was set before noise was playing,
                //start the timer with the music
                if (time != 0L) {
                    startTimer(time)
                }
            }
        }
    }

    /**
     * Create and show a timer picker.
     */
    private fun showPickerDialog() {
        TimerPickerDialogFragment().let {
            it.show(supportFragmentManager, "TimerPickerDialog")
            it.setDialogListener(this)
        }
    }

    override fun onPause() {
        super.onPause()
        if (audioPlayerService?.isPlaying == true) {
            audioPlayerService?.showNotification(true)
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        var timerSetAndNotStarted = false
        //we only need to save timer state if it was created but not started
        //if it wasn't created - who cares
        //if it was started - the service will tell us its state
        if (audioPlayerService?.isPlaying == false && timerTextView.time != 0L) {
            timerSetAndNotStarted = true
        }
        savedInstanceState.putBoolean(Preferences.STATE_TIMER_CREATED.key, timerSetAndNotStarted)
        savedInstanceState.putLong(Preferences.STATE_TIMER_TIME.key, timerTextView.time)
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState)
    }


    override fun onResume() {
        super.onResume()
        if (usingDarkMode != useDarkMode) {
            //wait until onResume has finished,
            //then recreate the activity which will change the theme
            handler.postDelayed({ recreate() }, 0)
        }
        audioPlayerService?.dismissNotification()
        if (audioPlayerService?.isPlaying == true) {
            setPlayButtonPause()
        } else {
            setPlayButtonPlay()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPlayerConnectionBound) {
            //dismiss any notification
            audioPlayerService?.dismissNotification()
            //unbind the service, it will still be running
            unbindService(this)
            isPlayerConnectionBound = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        if (item.itemId == R.id.settings) {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            settingsIntent.putExtra(Preferences.DARK_MODE.key, usingDarkMode)
            startActivity(settingsIntent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Visually show that there is no timer by hiding the timertextview
     * and changing the  timer button back to having a "+".
     */
    private fun setTimerUiUnsetState() {
        //set button image
        timerButton.setImageDrawable(addPic)
        timerTextView.visibility = View.GONE
    }

    /**
     * Visually show there is a new timer by showing timer text view with time and changing
     * the timer button to having an "x".
     */
    private fun setTimerUiAdded(currTime: Long) {
        //change button to "clear"
        timerButton.setImageDrawable(stopPic)
        timerTextView.time = currTime
        timerTextView.visibility = View.VISIBLE
    }

    /**
     * Load and set relevant saved values about the UI.
     */
    private fun loadPreferences() {
        fadeNeverOn = prefsUtil.getBoolean(baseContext, Preferences.FADE_NEVER_ON, true)
        useDarkMode = prefsUtil.getBoolean(baseContext, Preferences.DARK_MODE, false)
        oscillateInterval = Integer.parseInt(
                prefsUtil.getString(baseContext, Preferences.WAVE_INTERVAL, "4")) * 1000L
        audioPlayerService?.setOscillatePeriod(oscillateInterval)
    }

    /**
     * Start a countdown timer.
     * @param time time to countdown from in milliseconds
     */
    private fun startTimer(time: Long) {
        editTextCountDownTimer?.cancel()
        editTextCountDownTimer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.time = millisUntilFinished
            }

            override fun onFinish() {
                val lastTime = prefsUtil.getLong(baseContext, Preferences.TIME, 0L)
                timerTextView.time = lastTime
                setPlayButtonPlay()
            }
        }
        audioPlayerService?.setTimer(time)
        editTextCountDownTimer?.start()
    }

    /**
     * Pause the timer but retain time left.
     */
    private fun pauseTimer() {
        editTextCountDownTimer?.cancel()
        audioPlayerService?.cancelTimer()
    }

    /**
     * Pause the timer and set the time to zero.
     */
    private fun stopTimer() {
        pauseTimer()
        audioPlayerService?.stop()
        timerTextView.time = 0
        timerActive = false
        setTimerUiUnsetState()
    }

    /**
     * Show the play button as paused.
     */
    private fun setPlayButtonPause() {
        //convert from 120dp to pixels
        val picSizeInPixels = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics).toInt()
        pausePic.setBounds(0, 0, picSizeInPixels, picSizeInPixels)
        btnPlay.setCompoundDrawables(pausePic, null, null, null)
        btnPlay.text = getString(R.string.audio_pause)
    }

    /**
     * Show the play button as playing.
     */
    private fun setPlayButtonPlay() {
        //convert from 120dp to pixels
        val picSizeInPixels = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics).toInt()
        playPic.setBounds(0, 0, picSizeInPixels, picSizeInPixels)
        btnPlay.setCompoundDrawables(playPic, null, null, null)
        btnPlay.text = getString(R.string.audio_play)
    }

}
