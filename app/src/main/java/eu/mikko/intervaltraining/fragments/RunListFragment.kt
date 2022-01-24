package eu.mikko.intervaltraining.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.adapters.ProgressAdapter
import eu.mikko.intervaltraining.viewmodel.ProgressViewModel
import eu.mikko.intervaltraining.viewmodel.RunViewModel
import kotlinx.android.synthetic.main.fragment_run_list.*

@AndroidEntryPoint
class RunListFragment : Fragment(R.layout.fragment_run_list) {

    private val viewModel: RunViewModel by viewModels()

    private lateinit var progressAdapter: ProgressAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.allRuns.observe(viewLifecycleOwner, {
            if(it != null) progressAdapter.submitList(it)
        })
    }

    private fun setupRecyclerView() = recycler_view.apply {
        progressAdapter = ProgressAdapter()
        adapter = progressAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }
}