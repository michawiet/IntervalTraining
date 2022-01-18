package eu.mikko.intervaltraining.fragments

import android.Manifest
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.model.Interval
import eu.mikko.intervaltraining.other.Constants
import eu.mikko.intervaltraining.other.TrackingUtility
import eu.mikko.intervaltraining.other.TrackingUtility.getFormattedTimeFromSeconds
import eu.mikko.intervaltraining.viewmodel.TrainingViewModel
import kotlinx.android.synthetic.main.fragment_run_start.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class RunStartFragment : Fragment(R.layout.fragment_run_start), EasyPermissions.PermissionCallbacks {

    private val viewModel: TrainingViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences

    var workoutStep: Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissions()
        runStartButton.setOnClickListener {
            findNavController().navigate(R.id.action_runStartFragment_to_runFragment)
        }

        workoutStep = sharedPref.getInt(Constants.KEY_WORKOUT_STEP, 1)
        viewModel.getIntervalByWorkoutStep(workoutStep).observe(viewLifecycleOwner, {
            tvWarmupLength.text = getFormattedTimeFromSeconds(it.warmupSeconds)
            tvTotalDistanceCovered.text = getFormattedTimeFromSeconds(it.walkSeconds)
            tvTotalTimeSpentTrackingActivity.text = getFormattedTimeFromSeconds(it.runSeconds)
            tvTotalActiveTime.text = getFormattedTimeFromSeconds(it.totalWorkoutTime)
            tvWorkoutStep.text = workoutStep.toString()

            setupPieChart(it)
        })
    }

    private fun setupPieChart(interval: Interval) {
        var totalRunTime = 0L
        var totalWalkTime = interval.warmupSeconds
        var timeLeft = interval.totalWorkoutTime - totalWalkTime

        var isRunning = true
        while(timeLeft > 0) {
            if(isRunning) {
                totalRunTime += if(interval.runSeconds > timeLeft) timeLeft else interval.runSeconds
                timeLeft -= interval.runSeconds
            } else {
                totalWalkTime += if (interval.walkSeconds > timeLeft) timeLeft else interval.walkSeconds
                timeLeft -= interval.walkSeconds
            }
            isRunning = !isRunning
        }

        val dataSet = PieDataSet(
            listOf(PieEntry(totalWalkTime.toFloat(), getString(R.string.activity_type_walk)),
                    PieEntry(totalRunTime.toFloat(), getString(R.string.activity_type_run)))
            , "")
        dataSet.apply {
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) = getFormattedTimeFromSeconds(value.toLong())
            }
            colors = listOf(Color.parseColor("#4777c0"),Color.parseColor("#a374c6"))
            setValueTextColors(listOf(Color.WHITE))
        }

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(15f)

        runTimePieChart.apply {
            data = pieData
            legend.isEnabled = false
            description.text = ""
            setDrawCenterText(true)
            setCenterTextSize(20f)
            setCenterTextColor(Color.parseColor("#222222"))
            setCenterTextTypeface(Typeface.DEFAULT_BOLD)
            centerText = "Time\nyou\nwill"
            invalidate()
        }
    }

    private fun requestPermissions() {
        if(TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "To record a run you need to accept location permissions.",
                Constants.REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "To record a run you need to accept location permissions.",
                Constants.REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
            EasyPermissions.requestPermissions(
                this,
                "To record a run with application minimized you need to accept background location permissions.",
                Constants.REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}