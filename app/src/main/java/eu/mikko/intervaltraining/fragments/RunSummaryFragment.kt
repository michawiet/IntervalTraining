package eu.mikko.intervaltraining.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.model.Run
import eu.mikko.intervaltraining.other.Constants
import eu.mikko.intervaltraining.other.Constants.RUN_PACE
import eu.mikko.intervaltraining.other.Constants.WALK_PACE
import eu.mikko.intervaltraining.other.PaceLabelFormatter
import eu.mikko.intervaltraining.other.ParcelableRunIntervalResult
import eu.mikko.intervaltraining.other.TrackingUtility
import eu.mikko.intervaltraining.viewmodel.TrainingViewModel
import kotlinx.android.synthetic.main.fragment_run_summary.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class RunSummaryFragment : Fragment(R.layout.fragment_run_summary) {

    private val viewModel: TrainingViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var workoutStep: Int = 1

    private var maxWorkoutStep = 36

    private val args by navArgs<RunSummaryFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvLength.text = TrackingUtility.getFormattedStopWatchTime(args.runData.timeInMillis)
        tvDistance.text = args.runData.distanceInMeters.div(1000).toString()
        tvAvgSpeed.text =
            TrackingUtility.getKilometersPerMinuteFromMetersPerSecond(args.runData.avgSpeedMetersPerSecond)
        tvRating.text = args.runData.rating.toString()

        Glide.with(this).load(args.runData.map).into(summaryMapImageView)

        setupCombinedDataChart()
        setDataForCombinedChart()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            discardWorkoutDialog()
        }

        viewModel.getMaxWorkoutStep().observe(viewLifecycleOwner, {
            this.maxWorkoutStep = it
        })

        fabDiscard.setOnClickListener { discardWorkoutDialog() }
        fabSave.setOnClickListener { saveRun() }
    }

    private fun discardWorkoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.discard_workout_title))
            .setMessage(getString(R.string.discard_workout_message))
            .setPositiveButton(getString(R.string.confirm_cancel)) { dialog, _ ->
                findNavController().navigate(R.id.action_runSummaryFragment_to_runStartFragment)
                dialog.cancel()
            }.setNegativeButton(getString(R.string.confirm_continue)) { dialog, _ ->
                dialog.cancel()
            }.create()
            .show()
    }

    class RatingLabelFormatter : IndexAxisValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }
    }

    private fun setupCombinedDataChart() {
        combinedChart.apply {
            xAxis.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
            }
            axisLeft.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
            }
            axisRight.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
            }
            description.text = ""
            legend.apply {
                textSize = 20f
                form = Legend.LegendForm.CIRCLE
                isWordWrapEnabled = true
            }
        }
    }

    private fun setDataForCombinedChart() {
        val intervalResultList = arrayListOf<ParcelableRunIntervalResult>()
        for(i in 0 .. 11) {
            val result = if(i % 2 == 0) { ParcelableRunIntervalResult(1.2f, 100, false) }
            else { ParcelableRunIntervalResult(2.3f, 100, true) }
            intervalResultList.add(result)
        }

        val achievedSpeeds = arrayListOf<Entry>()
        val requiredSpeeds = arrayListOf<Entry>()
        val runRatings = arrayListOf<BarEntry>()
        val walkRatings = arrayListOf<BarEntry>()

        var i = 1f
        for(result in intervalResultList) {//args.intervalResults) {
            achievedSpeeds.add(Entry(i, result.avgSpeedMetersPerSecond))
            requiredSpeeds.add(Entry(i, if(result.isRunning) RUN_PACE else WALK_PACE))

            if(result.isRunning) runRatings.add(BarEntry(i, Random.nextInt(70, 100).toFloat()))
            else walkRatings.add(BarEntry(i, Random.nextInt(70, 100).toFloat()))

            i += 1f
        }

        val requiredPaceLineDataSet = LineDataSet(requiredSpeeds, "Required pace").apply {
            setDrawValues(true)
            color = Color.parseColor("#426FC0")
            lineWidth = 4f
            axisDependency = YAxis.AxisDependency.LEFT
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.4f
            circleRadius = 1.5f
            setCircleColor(Color.parseColor("#426FC0"))
            circleHoleColor = Color.parseColor("#9DADDA")
        }
        val achievedPaceLineDataSet = LineDataSet(achievedSpeeds, "Achieved pace").apply {
            setDrawValues(true)
            color = Color.parseColor("#E87A30")
            lineWidth = 4f
            axisDependency = YAxis.AxisDependency.LEFT
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.4f
            circleRadius = 1.5f
            setCircleColor(Color.parseColor("#E87A30"))
            circleHoleColor = Color.parseColor("#F6B499")
        }
        val walkRatingsDataSet = BarDataSet(walkRatings, "Walk rating").apply {
            color = Color.parseColor("#CBCBCB")
            axisDependency = YAxis.AxisDependency.RIGHT
            barBorderColor = Color.parseColor("#A1A1A1")
            barBorderWidth = 2f
            valueTextSize = 12f
        }
        val runRatingsDataSet = BarDataSet(runRatings, "Run rating").apply {
            color = Color.parseColor("#FFD891")
            axisDependency = YAxis.AxisDependency.RIGHT
            barBorderColor = Color.parseColor("#F9BC00")
            barBorderWidth = 2f
            valueTextSize = 12f
        }

        val lineData = LineData(achievedPaceLineDataSet, requiredPaceLineDataSet)
        lineData.setValueFormatter(PaceLabelFormatter())
        val barData = BarData(walkRatingsDataSet, runRatingsDataSet)
        barData.setValueFormatter(RatingLabelFormatter())

        val data = CombinedData().apply {
            setData(lineData)
            setData(barData)
            barData.barWidth = 0.5f
        }

        combinedChart.apply {
            setData(data)
            invalidate()
            xAxis.apply {
                axisMinimum = 0.4f
                axisMaximum = intervalResultList.size.plus(0.5f)
                valueFormatter = RatingLabelFormatter()
            }
            axisRight.apply {
                axisMinimum = 0f
                axisMaximum = 110f
            }
            axisLeft.apply {
                axisMinimum = 0.8f
                axisMaximum = 5f
            }
            isScaleYEnabled = false
        }
    }

    private fun saveRun() {

        val newRun = Run(
            args.runData.timestamp,
            args.runData.avgSpeedMetersPerSecond,
            args.runData.distanceInMeters,
            args.runData.timeInMillis,
            args.runData.rating,
            args.runData.workoutStep,
            args.runData.map
        )

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
        Timber.d("Run data was saved!")

        //TODO "navigate to the progress? or somewhere"
        findNavController().navigate(R.id.action_runSummaryFragment_to_runStartFragment)
    }

    private fun writeNewWorkoutStepToSharedPref(newWorkoutStep: Int) {
        var correctedWorkoutStep = newWorkoutStep

        if(newWorkoutStep < 1)
            correctedWorkoutStep = 1
        else if(newWorkoutStep > maxWorkoutStep)
            correctedWorkoutStep = maxWorkoutStep

        sharedPref.edit()
            .putInt(Constants.KEY_WORKOUT_STEP, correctedWorkoutStep)
            .apply()
    }
}