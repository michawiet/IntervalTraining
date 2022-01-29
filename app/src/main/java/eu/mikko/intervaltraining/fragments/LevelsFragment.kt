package eu.mikko.intervaltraining.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.adapters.LevelsAdapter
import eu.mikko.intervaltraining.model.Interval
import eu.mikko.intervaltraining.other.Constants
import eu.mikko.intervaltraining.other.Constants.RUN_PACE
import eu.mikko.intervaltraining.other.Constants.WALK_PACE
import eu.mikko.intervaltraining.other.TrackingUtility.getFormattedTimeFromSeconds
import eu.mikko.intervaltraining.viewmodel.IntervalViewModel
import kotlinx.android.synthetic.main.fragment_journal.recycler_view
import kotlinx.android.synthetic.main.fragment_levels.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LevelsFragment : Fragment(R.layout.fragment_levels) {

    private val viewModel: IntervalViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences
    private lateinit var levelsAdapter: LevelsAdapter
    private var workoutLevel: Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get current workout level
        workoutLevel = sharedPref.getInt(Constants.KEY_WORKOUT_LEVEL, 1)
        setupRecyclerView()
        setupLineChart()

        viewModel.allIntervals.observe(viewLifecycleOwner) {
            //pass the list to an adapter
            levelsAdapter.submitList(it)
            levelsAdapter.setCurrentlyChecked(workoutLevel - 1)
            //set data for the selected item
            val currentInterval = it[workoutLevel - 1]
            tvTrainingPlanNumber.text = currentInterval.workoutLevel.toString()
            tvTotalTime.text = getFormattedTimeFromSeconds(currentInterval.totalWorkoutTime)
            tvRunTime.text = getFormattedTimeFromSeconds(currentInterval.runSeconds)
            tvWalkTime.text = getFormattedTimeFromSeconds(currentInterval.walkSeconds)
            //setup
            setDataForLineChart(currentInterval)
        }
    }

    private fun setupLineChart() {
        levelLineChart.apply {
            isScaleYEnabled = false
            legend.isEnabled = false
            description.text = ""
            axisRight.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
                limitLines.add(LimitLine(WALK_PACE, "Walk").apply {
                    lineWidth = 2f
                    enableDashedLine(20f, 20f, 0f)
                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
                    textSize = 10f
                    lineColor = ContextCompat.getColor(requireContext(), R.color.black_disabled)
                    textColor = ContextCompat.getColor(requireContext(), R.color.black_disabled)
                })
                limitLines.add(LimitLine(RUN_PACE, "Run").apply {
                    lineWidth = 2f
                    enableDashedLine(20f, 20f, 0f)
                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                    textSize = 10f
                    lineColor = ContextCompat.getColor(requireContext(), R.color.black_disabled)
                    textColor = ContextCompat.getColor(requireContext(), R.color.black_disabled)
                })
                setDrawLimitLinesBehindData(false)
            }
            axisLeft.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
            }
            xAxis.apply {
                //label formatter for minutes
                valueFormatter = TimeValueFormatter()
            }
            val gradient = LinearGradient(0f, 600f, 0f, 0f,
                ContextCompat.getColor(requireContext(), R.color.walk_dark_transparent),
                ContextCompat.getColor(requireContext(), R.color.run_dark_transparent),
                Shader.TileMode.CLAMP)
            renderer.paintRender.shader = gradient
        }
    }

    class TimeValueFormatter : IndexAxisValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return getFormattedTimeFromSeconds(value.toLong())
        }
    }

    private fun setDataForLineChart(interval: Interval) {
        //Create entries
        val entries = arrayListOf<Entry>()

        val walkPaceMultiplier = 1.09f
        val runPaceMultiplier = 0.955f
        val xOffset = 0.1f
        var usedTime = 0f
        //add warmup phase
        entries.add(Entry(usedTime, WALK_PACE))
        usedTime += interval.warmupSeconds
        entries.add(Entry(usedTime.times(0.9f), WALK_PACE.times(walkPaceMultiplier)))

        var isRunningInterval = true
        while(usedTime < interval.totalWorkoutTime) {
             if(isRunningInterval) {
                 entries.add(Entry(usedTime.plus(interval.runSeconds.times(xOffset)), RUN_PACE.times(runPaceMultiplier)))
                 entries.add(Entry(usedTime.plus(interval.runSeconds.times(0.5f)), RUN_PACE))
                 usedTime += interval.runSeconds.toFloat()
                 if(usedTime < interval.totalWorkoutTime)
                     entries.add(Entry(usedTime.minus(interval.runSeconds.times(xOffset)), RUN_PACE.times(runPaceMultiplier)))
                 else
                     entries.add(Entry(usedTime, RUN_PACE))
            } else {
                 entries.add(Entry(usedTime.plus(interval.walkSeconds.times(xOffset)), WALK_PACE.times(walkPaceMultiplier)))
                 entries.add(Entry(usedTime.plus(interval.walkSeconds.times(0.5f)), WALK_PACE))
                 usedTime += interval.walkSeconds.toFloat()
                 if(usedTime < interval.totalWorkoutTime)
                     entries.add(Entry(usedTime.minus(interval.walkSeconds.times(xOffset)), WALK_PACE.times(walkPaceMultiplier)))
                 else
                     entries.add(Entry(usedTime, WALK_PACE))
            }
            isRunningInterval = !isRunningInterval
        }

        val lineDataSet = LineDataSet(entries, "Pace").apply {
            lineWidth = 5f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.1f
            axisDependency = YAxis.AxisDependency.RIGHT
            setDrawCircles(false)
            setDrawCircleHole(false)

            fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.fade_blue)
            setDrawFilled(true)
        }
        //create data
        val lineData = LineData(lineDataSet)
        lineData.isHighlightEnabled = false

        levelLineChart.apply {
            //style lineChart
            xAxis.apply {
                axisMaximum = interval.totalWorkoutTime.toFloat()
            }
            setVisibleXRangeMaximum(350f)
            setVisibleXRangeMinimum(120f)
            data = lineData
            invalidate()
        }
    }

    private fun updateCurrentSelectedInterval(interval: Interval) {
        tvTrainingPlanNumber.text = interval.workoutLevel.toString()
        tvTotalTime.text = getFormattedTimeFromSeconds(interval.totalWorkoutTime)
        tvRunTime.text = getFormattedTimeFromSeconds(interval.runSeconds)
        tvWalkTime.text = getFormattedTimeFromSeconds(interval.walkSeconds)

        setDataForLineChart(interval)
    }

    private fun setupRecyclerView() = recycler_view.apply {
        levelsAdapter = LevelsAdapter { updateCurrentSelectedInterval(it) }
        adapter = levelsAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }
}