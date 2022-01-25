package eu.mikko.intervaltraining.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.model.Run
import eu.mikko.intervaltraining.other.DistanceValueFormatter
import eu.mikko.intervaltraining.other.MinutesValueFormatter
import eu.mikko.intervaltraining.other.PaceLabelFormatter
import eu.mikko.intervaltraining.other.TrackingUtility.getFormattedTimeFromSeconds
import eu.mikko.intervaltraining.other.TrackingUtility.getKilometersPerMinuteFromMetersPerSecond
import eu.mikko.intervaltraining.viewmodel.ProgressViewModel
import kotlinx.android.synthetic.main.fragment_progress.*
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class ProgressFragment : Fragment(R.layout.fragment_progress) {

    private val viewModel: ProgressViewModel by viewModels()

    @set:Inject
    var workoutStep: Int = 1

    @Inject


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCombinedChart()

        viewModel.totalDistance.observe(viewLifecycleOwner, {
            if(it != null)
                tvTotalDistanceCovered.text = it.toFloat().div(1000f).toString()
        })
        viewModel.totalTimeInMillis.observe(viewLifecycleOwner, {
            if(it != null)
                tvTotalTimeSpentTrackingActivity.text = getFormattedTimeFromSeconds(it.div(1000))
        })
        viewModel.allRuns.observe(viewLifecycleOwner, {
            setDataForCombinedChart(it)
            combinedProgressChart.invalidate()
        })
        viewModel.getMaxWorkoutStep().observe(viewLifecycleOwner, {
            tvGoalProgress.text = (workoutStep - 1).div((it - 1).toFloat()).times(100f).toInt().toString().plus(" %")
        })
    }

    //TODO "use data provided with the list"
    private fun setDataForCombinedChart(list: List<Run>) {
        val mockList = arrayListOf<Run>()
        for(i in 0 .. 10) {
            mockList.add(Run(0,
                Random.nextInt(1, 27).div(9f),
                Random.nextInt(30, 50).times(100),
                Random.nextLong(25L, 30L).times(60L).times(1000L),
                Random.nextInt(70, 100)))
        }

        val distances = arrayListOf<Entry>()
        val activityTypeTimes = arrayListOf<BarEntry>()

        var i = 1f
        for(run in mockList) {
            distances.add(Entry(i, run.distanceInMeters.div(1000f)))
            activityTypeTimes.add(BarEntry(i, floatArrayOf(5f, 25f)))

            i += 1f
        }

        val distanceDataSet = LineDataSet(distances, "Distance").apply {
            setDrawValues(true)
            color = Color.parseColor("#426FC0")
            lineWidth = 4f
            axisDependency = YAxis.AxisDependency.LEFT
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
            circleRadius = 2.0f
            valueTextSize = 10f
            setDrawValues(false)
            setCircleColor(Color.parseColor("#426FC0"))
            circleHoleColor = Color.parseColor("#9DADDA")
        }

        val activityTypeTimesDataSet = BarDataSet(activityTypeTimes, "").apply {
            stackLabels = arrayOf("Walk length", "Run length")
            colors = listOf(Color.parseColor("#CBCBCB"), Color.parseColor("#FFD891"))
            axisDependency = YAxis.AxisDependency.RIGHT
            barBorderColor = Color.parseColor("#A1A1A1")
            barBorderWidth = 2f
            valueTextSize = 12f
            valueFormatter = MinutesValueFormatter()
        }

        val lineData = LineData(distanceDataSet)
        val barData = BarData(activityTypeTimesDataSet)
        val data = CombinedData().apply {
            setData(lineData)
            setData(barData)
            barData.barWidth = 0.5f
        }

        combinedProgressChart.apply {
            setData(data)
            xAxis.axisMaximum = mockList.size.plus(0.5f)
        }
    }

    private fun setupCombinedChart() {
        combinedProgressChart.apply {
            isScaleYEnabled = false
            xAxis.apply {
                axisMinimum = 0.4f
                setDrawLabels(false)
                setDrawGridLines(false)
            }
            axisLeft.apply {
                setDrawGridLines(true)
                setDrawLabels(true)
                valueFormatter = DistanceValueFormatter()
                axisMinimum = 0f
                axisMaximum = 8f
            }
            axisRight.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
                axisMinimum = 0f
            }
            description.text = ""
            legend.apply {
                textSize = 15f
                form = Legend.LegendForm.CIRCLE
                isWordWrapEnabled = true
            }
        }
    }
}