package eu.mikko.intervaltraining.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.other.Constants.ACTION_PAUSE_SERVICE
import eu.mikko.intervaltraining.other.Constants.ACTION_START_SERVICE
import eu.mikko.intervaltraining.other.Constants.ACTION_STOP_SERVICE
import eu.mikko.intervaltraining.other.TrackingUtility
import eu.mikko.intervaltraining.services.Intervals
import eu.mikko.intervaltraining.services.TrackingService
import eu.mikko.intervaltraining.viewmodel.TrainingViewModel
import kotlinx.android.synthetic.main.fragment_run.*
import java.lang.IllegalArgumentException
import kotlin.math.roundToInt

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    private val viewModel: TrainingViewModel by viewModels()
    private var isTracking = false
    private var pathPointsOfIntervals = mutableListOf<Intervals>()

    private var curTimeInMillis = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityPlayPauseFab.setOnClickListener {
            if (!isTracking) {
                //resume the service
                sendCommandToService(ACTION_START_SERVICE)
                isTracking = true
                activityStopFab.show()
                activityPlayPauseFab.hide()
            } else {
              sendCommandToService(ACTION_PAUSE_SERVICE)
              isTracking = false
              activityStopFab.show()
              //activityPlayPauseFab.setImageIcon(R.drawable.ic_round_pause_24)
            }
        }
        activityStopFab.setOnClickListener {
            //Dialog to confirm the action
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.stop_activity_title))
                .setMessage(getString(R.string.stop_activity_message))
                .setPositiveButton(getString(R.string.confirm_cancel)) { dialog, _ ->
                    // TODO("cancel all the stuff")
                    stopRun()
                    dialog.cancel()
                }.setNegativeButton(getString(R.string.confirm_continue)) { dialog, _ ->
                    dialog.cancel()
                }.create()
                .show()
        }
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, {
            // updateTracking()
        })
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, {
            curTimeInMillis = it
            activity_timer.text = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis)
        })
        TrackingService.currentSpeedMetersPerSecond.observe(viewLifecycleOwner, {
            try {
                val pace = 1000 / (it * 60)
                val leftover = pace % 1
                val minutes = (pace - leftover).roundToInt()
                val seconds = (leftover * 60).roundToInt()
                workout_speed.text = String.format("%02d:%02d", minutes, seconds)
            } catch (e: IllegalArgumentException) {
                workout_speed.text = getString(R.string.workout_speed_default)
            }
        })
        TrackingService.distanceInMeters.observe(viewLifecycleOwner, {
            // convert meters to kilometers
            workout_distance.text = String.format("%.3f", it.div(1000))
        })
        TrackingService.isRunningInterval.observe(viewLifecycleOwner, { isRunning ->
            if(isRunning) {
                workout_interval_type.text = getString(R.string.activity_type_run)
                //trigger tts with "Start running!" message

            }
            else {
                workout_interval_type.text = getString(R.string.activity_type_walk)
                //trigger tts with "Start running!" message
            }
        })
    }

    private fun toggleRun() {
        if(isTracking) {
            //show stop button????? XD
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_SERVICE)
        }
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_runFragment_to_runStartFragment)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        //activityPlayPauseFab.
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
}