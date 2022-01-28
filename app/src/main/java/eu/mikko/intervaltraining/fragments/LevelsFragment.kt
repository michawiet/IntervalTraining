package eu.mikko.intervaltraining.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.adapters.LevelsAdapter
import eu.mikko.intervaltraining.model.Interval
import eu.mikko.intervaltraining.other.Constants
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

    private lateinit var levelsList: List<Interval>

    private var workoutLevel: Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get current workout level
        workoutLevel = sharedPref.getInt(Constants.KEY_WORKOUT_LEVEL, 1)
        setupRecyclerView()
        setupLineChart()

        viewModel.allIntervals.observe(viewLifecycleOwner) {
            //TODO("pass the list to an adapter")
            levelsAdapter.submitList(it)
            val currentInterval = it[workoutLevel - 1]
            tvTrainingPlanNumber.text = currentInterval.workoutLevel.toString()
            tvTotalTime.text = getFormattedTimeFromSeconds(currentInterval.totalWorkoutTime)
            tvRunTime.text = getFormattedTimeFromSeconds(currentInterval.runSeconds)
            tvWalkTime.text = getFormattedTimeFromSeconds(currentInterval.walkSeconds)

            setupDataForLineChart(currentInterval)
            //probably also save it locally?
        }
    }

    //TODO: update the data for the chart
    private fun setupLineChart() {

    }

    //TODO: update the data for the chart
    private fun setupDataForLineChart(interval: Interval) {

    }

    //TODO: use in a callback from adapter
    private fun updateCurrentSelectedInterval(interval: Interval) {
        Timber.d("updateCurrentSelectedInterval got -> ${interval.workoutLevel}")

        tvTrainingPlanNumber.text = interval.workoutLevel.toString()
        tvTotalTime.text = getFormattedTimeFromSeconds(interval.totalWorkoutTime)
        tvRunTime.text = getFormattedTimeFromSeconds(interval.runSeconds)
        tvWalkTime.text = getFormattedTimeFromSeconds(interval.walkSeconds)

        setupDataForLineChart(interval)
    }

    private fun setupRecyclerView() = recycler_view.apply {
        levelsAdapter = LevelsAdapter { updateCurrentSelectedInterval(it) }
        adapter = levelsAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }
}