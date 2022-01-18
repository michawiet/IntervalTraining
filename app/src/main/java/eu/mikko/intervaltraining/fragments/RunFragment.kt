package eu.mikko.intervaltraining.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.model.Interval
import eu.mikko.intervaltraining.model.Run
import eu.mikko.intervaltraining.other.Constants.ACTION_INTERVAL_DATA
import eu.mikko.intervaltraining.other.Constants.ACTION_PAUSE_SERVICE
import eu.mikko.intervaltraining.other.Constants.ACTION_START_SERVICE
import eu.mikko.intervaltraining.other.Constants.ACTION_STOP_SERVICE
import eu.mikko.intervaltraining.other.Constants.EXTRAS_INTERVAL_DATA
import eu.mikko.intervaltraining.other.Constants.KEY_WORKOUT_STEP
import eu.mikko.intervaltraining.other.Constants.TIMER_UPDATE_INTERVAL
import eu.mikko.intervaltraining.other.TrackingUtility
import eu.mikko.intervaltraining.other.TrackingUtility.getKilometersPerMinuteFromMetersPerSecond
import eu.mikko.intervaltraining.other.TrackingUtility.getTotalDistance
import eu.mikko.intervaltraining.other.TrackingUtility.rateWorkout
import eu.mikko.intervaltraining.services.IntervalPathPoints
import eu.mikko.intervaltraining.services.TrackingService
import eu.mikko.intervaltraining.viewmodel.TrainingViewModel
import kotlinx.android.synthetic.main.fragment_run.*
import org.w3c.dom.Text
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    // Shared preferences injection
    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var workoutStep: Int = 1

    private var maxWorkoutStep = 36

    private val viewModel: TrainingViewModel by viewModels()
    private lateinit var interval: Interval
    private var isTracking = false
    private var pathPointsOfIntervals = mutableListOf<IntervalPathPoints>()

    private var curTimeInMillis = 0L

    private lateinit var tts: TextToSpeech

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tts = TextToSpeech(requireContext()) { status ->
            if(status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0])
                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                    Timber.d("Language not available")
                }
            } else {
                Timber.d("TTS initialization failed")
            }
        }

        activityPlayPauseFab.setOnClickListener {
            if (!isTracking) {
                //resume the service
                sendCommandToService(ACTION_START_SERVICE)
                isTracking = true
                activityPlayPauseFab.setImageResource(R.drawable.ic_round_pause_24)
            } else {
                sendCommandToService(ACTION_PAUSE_SERVICE)
                isTracking = false
                activityPlayPauseFab.setImageResource(R.drawable.ic_round_play_arrow_24)
            }
        }

        activityStopFab.setOnClickListener {
            //Dialog to confirm the action
            stopActivityCallback()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            stopActivityCallback()
        }

        viewModel.getIntervalByWorkoutStep(this.workoutStep).observe(viewLifecycleOwner, {
            sendIntervalsToService(it)
            activityPlayPauseFab.show()
            interval = it
        })

        viewModel.getMaxWorkoutStep().observe(viewLifecycleOwner, {
            maxWorkoutStep = it
        })

        subscribeToObservers()
    }

    private fun stopActivityCallback() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.stop_activity_title))
            .setMessage(getString(R.string.stop_activity_message))
            .setPositiveButton(getString(R.string.confirm_cancel)) { dialog, _ ->
                stopRun()
                dialog.cancel()
            }.setNegativeButton(getString(R.string.confirm_continue)) { dialog, _ ->
                dialog.cancel()
            }.create()
            .show()
    }

    private fun subscribeToObservers() {
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, {
            curTimeInMillis = it
            activity_timer.text = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis)
        })
        TrackingService.currentSpeedMetersPerSecond.observe(viewLifecycleOwner, {
            workout_speed.text = getKilometersPerMinuteFromMetersPerSecond(it)
        })
        TrackingService.distanceInMeters.observe(viewLifecycleOwner, {
            // convert meters to kilometers
            workout_distance.text = String.format("%.3f", it.div(1000))
        })
        TrackingService.isRunningInterval.observe(viewLifecycleOwner, { isRunningInterval ->
            if(isRunningInterval) {
                workout_interval_type.text = getString(R.string.activity_type_run)
                //trigger tts with "Start running!" message
                val string = getText(R.string.activity_type_run)
                tts.speak("Start running!", TextToSpeech.QUEUE_FLUSH, null)
            }
            else {
                workout_interval_type.text = getString(R.string.activity_type_walk)
                tts.speak("Start walking!", TextToSpeech.QUEUE_FLUSH, null)
                //trigger tts with "Start running!" message
            }
        })
        TrackingService.intervalTimer.observe(viewLifecycleOwner, {
            //interval_timer.text = TrackingUtility.getFormattedStopWatchTime(it)
            interval_timer.text = TrackingUtility.getFormattedStopWatchTimeWithMillis(it)
        })
        TrackingService.intervalProgress.observe(viewLifecycleOwner, {
            interval_progress_bar.setProgressWithAnimation(it, TIMER_UPDATE_INTERVAL)
        })
        TrackingService.activityProgress.observe(viewLifecycleOwner, {
            workout_progress_bar.setProgressWithAnimation(it, TIMER_UPDATE_INTERVAL)
        })
        TrackingService.isActivityOver.observe(viewLifecycleOwner, {
            if(it) {
                saveRun()
            }
        })
        TrackingService.pathPointsOfIntervals.observe(viewLifecycleOwner, {
            pathPointsOfIntervals = it
        })
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_runFragment_to_runStartFragment)
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    private fun sendIntervalsToService(interval: Interval) {
        val intent = Intent(requireContext(), TrackingService::class.java)
        val pInterval = TrackingUtility.ParcelableInterval(interval.warmupSeconds, interval.runSeconds, interval.walkSeconds, interval.totalWorkoutTime)
        intent.putExtra(EXTRAS_INTERVAL_DATA, pInterval)
        intent.action = ACTION_INTERVAL_DATA
        requireContext().startService(intent)
    }

    private fun saveRun() {
        val newRun = Run()
        newRun.apply {
            timeInMillis = interval.totalWorkoutTime * 1000L
            distanceInMeters = getTotalDistance(pathPointsOfIntervals).toInt()
            avgSpeedMetersPerSecond = distanceInMeters.div(interval.totalWorkoutTime.toFloat())
            timestamp = Calendar.getInstance().timeInMillis
            rating = rateWorkout(interval, pathPointsOfIntervals)
        }
        writeNewWorkoutStepToSharedPref(newRun.rating)
        viewModel.insertNewRun(newRun)
        when {
            newRun.rating > 75 -> writeNewWorkoutStepToSharedPref(workoutStep + 1)
            newRun.rating > 50 -> writeNewWorkoutStepToSharedPref(workoutStep)
            else -> writeNewWorkoutStepToSharedPref(workoutStep - 1)
        }
        Snackbar.make(
            requireActivity().findViewById(R.id.rootView),
            "Run saved successfully with a score of ${newRun.rating}",
            Snackbar.LENGTH_LONG
        ).show()
        stopRun()
        Timber.d("Run data was saved!")
    }

    private fun writeNewWorkoutStepToSharedPref(newWorkoutStep: Int) {
        //TODO("check if increased workout step is not greater than last workout step (36, but read from viewmodel")
        var correctedWorkoutStep = newWorkoutStep

        if(newWorkoutStep < 1)
            correctedWorkoutStep = 1
        else if(newWorkoutStep > maxWorkoutStep)
            correctedWorkoutStep = maxWorkoutStep

        sharedPref.edit()
            .putInt(KEY_WORKOUT_STEP, correctedWorkoutStep)
            .apply()
    }
}