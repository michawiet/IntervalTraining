package eu.mikko.intervaltraining.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.adapters.ProgressAdapter
import eu.mikko.intervaltraining.other.TrackingUtility.getFormattedTimeFromSeconds
import eu.mikko.intervaltraining.viewmodel.ProgressViewModel
import kotlinx.android.synthetic.main.fragment_progress.*

@AndroidEntryPoint
class ProgressFragment : Fragment(R.layout.fragment_progress) {

    private val viewModel: ProgressViewModel by viewModels()

    private lateinit var progressAdapter: ProgressAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

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
        })
    }

    private fun setupRecyclerView() = recycler_view.apply {
        progressAdapter = ProgressAdapter()
        adapter = progressAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }
}