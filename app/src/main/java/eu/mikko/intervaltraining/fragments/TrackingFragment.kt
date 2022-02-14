package eu.mikko.intervaltraining.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.model.Interval
import eu.mikko.intervaltraining.other.Constants
import eu.mikko.intervaltraining.other.Constants.ACTION_INTERVAL_DATA
import eu.mikko.intervaltraining.other.Constants.ACTION_PAUSE_SERVICE
import eu.mikko.intervaltraining.other.Constants.ACTION_START_SERVICE
import eu.mikko.intervaltraining.other.Constants.ACTION_STOP_SERVICE
import eu.mikko.intervaltraining.other.Constants.EXTRAS_INTERVAL_DATA
import eu.mikko.intervaltraining.other.Constants.MAP_ZOOM
import eu.mikko.intervaltraining.other.Constants.POLYLINE_WIDTH
import eu.mikko.intervaltraining.other.Constants.TIMER_UPDATE_INTERVAL
import eu.mikko.intervaltraining.other.ParcelableInterval
import eu.mikko.intervaltraining.other.ParcelableRun
import eu.mikko.intervaltraining.other.TrackingUtility
import eu.mikko.intervaltraining.other.TrackingUtility.getMinutesPerKilometerFromMetersPerSecond
import eu.mikko.intervaltraining.other.TrackingUtility.getTotalDistance
import eu.mikko.intervaltraining.other.TrackingUtility.rateIntervals
import eu.mikko.intervaltraining.other.TrackingUtility.rateWorkout
import eu.mikko.intervaltraining.services.IntervalPathPoints
import eu.mikko.intervaltraining.services.TrackingService
import eu.mikko.intervaltraining.viewmodel.IntervalViewModel
import kotlinx.android.synthetic.main.fragment_tracking.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    private var workoutLevel: Int = 1

    private var maxWorkoutLevel = 36

    private val viewModel: IntervalViewModel by viewModels()
    private lateinit var interval: Interval
    private var isTracking = false
    private var pathPointsOfIntervals = mutableListOf<IntervalPathPoints>()
    private var isRunningInterval = false

    private var curTimeInMillis = 0L

    private var map: GoogleMap? = null

    //Permissions are checked with TrackingUtility.hasLocationPermissions(requireContext())
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView.onCreate(savedInstanceState)
        workoutLevel = sharedPref.getInt(Constants.KEY_WORKOUT_LEVEL, 1)

        mapView.getMapAsync {
            it.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.empty_map_style))
            if(TrackingUtility.hasLocationPermissions(requireContext())) {
                it.isMyLocationEnabled = true
            }
            it.uiSettings.setAllGesturesEnabled(false)
            it.uiSettings.isMyLocationButtonEnabled = false

            map = it
            addAllPolylines()
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

        viewModel.getIntervalByWorkoutLevel(this.workoutLevel).observe(viewLifecycleOwner) {
            sendIntervalsToService(it)
            activityPlayPauseFab.show()
            interval = it
        }

        viewModel.getMaxWorkoutLevel().observe(viewLifecycleOwner) {
            maxWorkoutLevel = it
        }

        subscribeToObservers()
    }

    private fun stopActivityCallback() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.stop_activity_title))
            .setMessage(getString(R.string.stop_activity_message))
            .setPositiveButton(getString(R.string.confirm_cancel)) { dialog, _ ->
                stopRun()
                findNavController().navigate(R.id.action_runFragment_to_runStartFragment)
                dialog.cancel()
            }.setNegativeButton(getString(R.string.confirm_continue)) { dialog, _ ->
                dialog.cancel()
            }.create()
            .show()
    }

    private fun subscribeToObservers() {
        TrackingService.currentLocation.observe(viewLifecycleOwner) {
            if (it.latitude != 0.0 && it.longitude != 0.0)
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(it, MAP_ZOOM))
        }
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            curTimeInMillis = it
            activity_timer.text = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis)
        }
        TrackingService.currentSpeedMetersPerSecond.observe(viewLifecycleOwner) {
            workout_speed.text = getMinutesPerKilometerFromMetersPerSecond(it)
        }
        TrackingService.distanceInMeters.observe(viewLifecycleOwner) {
            // convert meters to kilometers
            workout_distance.text = String.format("%.3f", it.div(1000))
        }
        TrackingService.isRunningInterval.observe(viewLifecycleOwner) {
            this.isRunningInterval = it
            if (it) workout_interval_type.text = getString(R.string.activity_type_run)
            else workout_interval_type.text = getString(R.string.activity_type_walk)
        }
        TrackingService.intervalTimer.observe(viewLifecycleOwner) {
            interval_timer.text = TrackingUtility.getFormattedStopWatchTimeWithMillis(it)
        }
        TrackingService.intervalProgress.observe(viewLifecycleOwner) {
            interval_progress_bar.setProgressWithAnimation(it, TIMER_UPDATE_INTERVAL)
        }
        TrackingService.activityProgress.observe(viewLifecycleOwner) {
            workout_progress_bar.setProgressWithAnimation(it, TIMER_UPDATE_INTERVAL)
        }
        TrackingService.isActivityOver.observe(viewLifecycleOwner) {
            if (it) {
                passRunResultToSummaryFragment()
            }
        }
        TrackingService.pathPointsOfIntervals.observe(viewLifecycleOwner) {
            pathPointsOfIntervals = it
            addLatestPolyline()
            moveCameraToUser()
        }
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    private fun sendIntervalsToService(interval: Interval) {
        val intent = Intent(requireContext(), TrackingService::class.java)
        val pInterval = ParcelableInterval(interval.warmupSeconds, interval.runSeconds, interval.walkSeconds, interval.totalWorkoutTime)
        intent.putExtra(EXTRAS_INTERVAL_DATA, pInterval)
        intent.action = ACTION_INTERVAL_DATA
        requireContext().startService(intent)
    }

    //Permission check for disabling my location?
    @SuppressLint("MissingPermission")
    private fun passRunResultToSummaryFragment() {
        loadingConstraintLayout.visibility = View.VISIBLE
        activityPlayPauseFab.visibility = View.INVISIBLE

        if(TrackingUtility.hasLocationPermissions(requireContext())) {
            map?.isMyLocationEnabled = false
        }
        zoomToSeeWholeTrack()

        map?.setOnMapLoadedCallback {
            map?.snapshot { bmp ->
                val timestamp = Calendar.getInstance().timeInMillis
                val distanceInMeters = getTotalDistance(pathPointsOfIntervals).toInt()
                val avgSpeedMetersPerSecond =
                    distanceInMeters.div(interval.totalWorkoutTime.toFloat())
                val timeInMillis = interval.totalWorkoutTime * 1000L
                val rating: Int = rateWorkout(interval, pathPointsOfIntervals)

                val run = ParcelableRun(
                    timestamp,
                    avgSpeedMetersPerSecond,
                    distanceInMeters,
                    timeInMillis,
                    rating,
                    workoutLevel,
                    bmp
                )

                val ratedIntervals = rateIntervals(interval, pathPointsOfIntervals)

                stopRun()

                val action = TrackingFragmentDirections.actionRunFragmentToRunSummaryFragment(
                    run,
                    ratedIntervals
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun moveCameraToUser() {
        if(pathPointsOfIntervals.isNotEmpty() && pathPointsOfIntervals.last().isNotEmpty() && pathPointsOfIntervals.last().last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPointsOfIntervals.last().last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        var includedPoints = 0
        for(interval in pathPointsOfIntervals) {
            for(chunk in interval) {
                for(pos in chunk) {
                    bounds.include(pos)
                    ++includedPoints
                }
            }
        }

        Timber.d("zoomToSeeWholeTrack -> includedPoints=$includedPoints")

        if(includedPoints > 1) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds.build(),
                    mapView.width,
                    mapView.height,
                    (mapView.height * 0.2f).toInt()
                )
            )
        }
    }

    private fun addAllPolylines() {
        val walkColor = ContextCompat.getColor(requireContext(), R.color.walk_dark)
        val runColor = ContextCompat.getColor(requireContext(), R.color.run_dark)
        var isRunning = false
        for (interval in pathPointsOfIntervals) {
            for (polyline in interval) {
                val polylineOptions = PolylineOptions()
                    .color(if (isRunning) runColor else walkColor)
                    .width(POLYLINE_WIDTH)
                    .addAll(polyline)
                map?.addPolyline(polylineOptions)
            }
            isRunning = !isRunning
        }
    }

    private fun addLatestPolyline() {
        if (pathPointsOfIntervals.isNotEmpty() && pathPointsOfIntervals.last()
                .isNotEmpty() && pathPointsOfIntervals.last().last().size > 1
        ) {
            val preLastLatLng = pathPointsOfIntervals.last().last()[pathPointsOfIntervals.last()
                .last().size - 2]
            val lastLatLng = pathPointsOfIntervals.last().last().last()
            val polylineOptions = PolylineOptions()
                .color(
                    if (isRunningInterval) ContextCompat.getColor(requireContext(),
                        R.color.run_dark)
                    else ContextCompat.getColor(requireContext(), R.color.walk_dark)
                )
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    //Google map functionality
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}