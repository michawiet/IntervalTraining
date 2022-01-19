package eu.mikko.intervaltraining.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.adapters.ProgressAdapter
import eu.mikko.intervaltraining.other.TrackingUtility.getFormattedTimeFromSeconds
import eu.mikko.intervaltraining.other.TrackingUtility.getKilometersPerMinuteFromMetersPerSecond
import eu.mikko.intervaltraining.viewmodel.ProgressViewModel
import kotlinx.android.synthetic.main.fragment_progress.*
import javax.inject.Inject

@AndroidEntryPoint
class ProgressFragment : Fragment(R.layout.fragment_progress) {

    private val viewModel: ProgressViewModel by viewModels()

    @set:Inject
    var workoutStep: Int = 1

    private lateinit var progressAdapter: ProgressAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupBarChart()

        viewModel.totalDistance.observe(viewLifecycleOwner, {
            if(it != null)
                tvTotalDistanceCovered.text = it.toFloat().div(1000f).toString()
        })
        viewModel.totalTimeInMillis.observe(viewLifecycleOwner, {
            if(it != null)
                tvTotalTimeSpentTrackingActivity.text = getFormattedTimeFromSeconds(it.div(1000))
        })
        viewModel.allRuns.observe(viewLifecycleOwner, {
            if(it != null)
                progressAdapter.submitList(it)
            val allAvgSpeeds= it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedMetersPerSecond) }
            val barDataSet = BarDataSet(allAvgSpeeds, "").apply {
                setDrawValues(false)
                color = Color.parseColor("#4777c0")
            }
            barChart.apply {
                data = BarData(barDataSet)
                invalidate()
            }
        })
        viewModel.getMaxWorkoutStep().observe(viewLifecycleOwner, {
            tvGoalProgress.text = (workoutStep - 1).div((it - 1).toFloat()).times(100f).toInt().toString().plus(" %")
        })
    }

    class LabelFormatter : IndexAxisValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return getKilometersPerMinuteFromMetersPerSecond(value)
        }
    }

    private fun setupBarChart() {
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            valueFormatter = LabelFormatter()
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            setDrawLabels(false)
            setDrawGridLines(false)
        }
        barChart.apply {
            description.text = "Pace over time"
            legend.isEnabled = false
        }
        barChart.legend.isEnabled = false
    }

    private fun setupRecyclerView() = recycler_view.apply {
        progressAdapter = ProgressAdapter()
        adapter = progressAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }
}