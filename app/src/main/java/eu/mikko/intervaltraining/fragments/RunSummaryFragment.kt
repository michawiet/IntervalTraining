package eu.mikko.intervaltraining.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.other.RunIntervalResult
import eu.mikko.intervaltraining.other.TrackingUtility
import kotlinx.android.synthetic.main.fragment_run_summary.*
import kotlin.random.Random

class RunSummaryFragment : Fragment() {

    private val args by navArgs<RunSummaryFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_run_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvLength.text = TrackingUtility.getFormattedStopWatchTime(args.runData.timeInMillis)
        tvDistance.text = args.runData.distanceInMeters.div(1000).toString()
        tvAvgSpeed.text =
            TrackingUtility.getKilometersPerMinuteFromMetersPerSecond(args.runData.avgSpeedMetersPerSecond)
        tvRating.text = args.runData.rating.toString()

        setupCombinedDataChart()
        setDataForCombinedChart()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            discardWorkoutDialog()
        }
        
        fabDiscard.setOnClickListener { discardWorkoutDialog() }
        //fabSave.setOnClickListener { //saveRunToDb }
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

    class LabelFormatter : IndexAxisValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return TrackingUtility.getKilometersPerMinuteFromMetersPerSecond(value)
        }
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
        val intervalResultList = arrayListOf<RunIntervalResult>()
        for(i in 0 .. 11) {
            val result = if(i % 2 == 0) { RunIntervalResult(1.2f, 100, false) }
            else { RunIntervalResult(2.3f, 100, true) }
            intervalResultList.add(result)
        }

        val achievedSpeeds = arrayListOf<Entry>()
        val requiredSpeeds = arrayListOf<Entry>()
        val runRatings = arrayListOf<BarEntry>()
        val walkRatings = arrayListOf<BarEntry>()

        var i = 1f
        for(result in intervalResultList) {//args.intervalResults) {
            achievedSpeeds.add(Entry(i, result.avgSpeedMetersPerSecond))
            requiredSpeeds.add(Entry(i, if(result.isRunning) 2.8f else 1.4f))

            if(result.isRunning) runRatings.add(BarEntry(i, Random.nextInt(70, 100).toFloat()))
            else walkRatings.add(BarEntry(i, Random.nextInt(70, 100).toFloat()))

            i += 1f
        }

        val requiredSpeedsLineDataSet = LineDataSet(requiredSpeeds, "Required speed").apply {
            setDrawValues(true)
            color = Color.parseColor("#426FC0")
            lineWidth = 4f
            axisDependency = YAxis.AxisDependency.LEFT
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.4f
        }
        val achievedSpeedsLineDataSet = LineDataSet(achievedSpeeds, "Achieved speed").apply {
            setDrawValues(true)
            color = Color.parseColor("#E87A30")
            lineWidth = 4f
            axisDependency = YAxis.AxisDependency.LEFT
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.4f
        }
        val walkRatingsDataSet = BarDataSet(walkRatings, "Walk ratings").apply {
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

        val lineData = LineData(achievedSpeedsLineDataSet, requiredSpeedsLineDataSet)
        val barData = BarData(walkRatingsDataSet, runRatingsDataSet)
        barData.setValueFormatter(RatingLabelFormatter())

        val data = CombinedData()
        data.setData(lineData)
        data.setData(barData)
        data.barData.barWidth = 0.5f

        combinedChart.apply {
            setData(data)
            //data.barWidth = 0.42f
            invalidate()
            xAxis.apply {
                axisMinimum = 0f
                axisMaximum = intervalResultList.size.plus(1f)
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
}