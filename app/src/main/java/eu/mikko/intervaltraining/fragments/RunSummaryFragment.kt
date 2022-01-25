package eu.mikko.intervaltraining.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.model.Run
import eu.mikko.intervaltraining.other.Constants
import eu.mikko.intervaltraining.other.Constants.INTERVAL_AVERAGE_PRECISION_LOWER_BOUND
import eu.mikko.intervaltraining.other.Constants.INTERVAL_GOOD_PRECISION_LOWER_BOUND
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
        tvRating.text = args.runData.rating.toString().plus("%")

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
            return value.toInt().toString().plus("%")
        }
    }

    private fun setupCombinedDataChart() {
        combinedChart.apply {
            isScaleYEnabled = false
            xAxis.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
                axisMinimum = 0.4f
                valueFormatter = RatingLabelFormatter()
            }
            axisLeft.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
                axisMinimum = 0.8f
                axisMaximum = 5f
            }
            axisRight.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
                axisMinimum = 0f
                axisMaximum = 110f
                addLimitLine(LimitLine(INTERVAL_GOOD_PRECISION_LOWER_BOUND.toFloat(), "Good precision limit").also {
                    it.lineWidth = 2f
                    it.enableDashedLine(20f, 20f, 0f)
                    it.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
                    it.textSize = 10f
                    it.lineColor = ContextCompat.getColor(requireContext(), R.color.black_disabled)
                    it.textColor = ContextCompat.getColor(requireContext(), R.color.black_disabled)
                })
            }
            description.text = ""
            legend.apply {
                textSize = 14f
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
            setDrawValues(false)
            color = Color.parseColor("#DC426FC0")
            lineWidth = 2f
            axisDependency = YAxis.AxisDependency.LEFT
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.4f
            setDrawCircles(false)
            setDrawCircleHole(false)
            enableDashedLine(20f, 25f, 0f)
        }
        val achievedPaceLineDataSet = LineDataSet(achievedSpeeds, "Achieved pace").apply {
            setDrawValues(true)
            color = Color.parseColor("#E87A30")
            setCircleColor(Color.parseColor("#E87A30"))
            circleHoleColor = Color.parseColor("#F6B499")
            circleRadius = 1.5f
            lineWidth = 4f
            axisDependency = YAxis.AxisDependency.LEFT
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.4f
            valueTextSize = 12f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.black_active)
        }
        val walkRatingsDataSet = BarDataSet(walkRatings, "Walk precision").apply {
            color = Color.parseColor("#CBCBCB")
            barBorderColor = Color.parseColor("#A1A1A1")
            axisDependency = YAxis.AxisDependency.RIGHT
            barBorderWidth = 2f
            valueTextSize = 12f
        }
        val runRatingsDataSet = BarDataSet(runRatings, "Run precision").apply {
            color = Color.parseColor("#FFD891")
            barBorderColor = Color.parseColor("#F9BC00")
            axisDependency = YAxis.AxisDependency.RIGHT
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
            xAxis.axisMaximum = intervalResultList.size.plus(0.5f)
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
            newRun.rating >= INTERVAL_GOOD_PRECISION_LOWER_BOUND -> writeNewWorkoutStepToSharedPref(workoutStep + 1)
            newRun.rating >= INTERVAL_AVERAGE_PRECISION_LOWER_BOUND -> writeNewWorkoutStepToSharedPref(workoutStep)
            else -> writeNewWorkoutStepToSharedPref(workoutStep - 1)
        }
        Snackbar.make(
            requireActivity().findViewById(R.id.rootView),
            "Run saved successfully!",
            Snackbar.LENGTH_LONG
        ).setAnchorView(R.id.bottom_navigation).show()
        Timber.d("Run data was saved!")

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