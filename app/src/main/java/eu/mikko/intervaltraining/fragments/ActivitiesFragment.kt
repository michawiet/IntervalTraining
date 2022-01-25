package eu.mikko.intervaltraining.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.adapters.ProgressAdapter
import eu.mikko.intervaltraining.viewmodel.RunViewModel
import kotlinx.android.synthetic.main.fragment_activities.*

@AndroidEntryPoint
class ActivitiesFragment : Fragment(R.layout.fragment_activities) {

    private val viewModel: RunViewModel by viewModels()

    private lateinit var progressAdapter: ProgressAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.allRuns.observe(viewLifecycleOwner, {
            if(it != null) {
                if(it.isEmpty()) {
                    ivRunEmpty.visibility = View.VISIBLE
                    tvEmptyTitle.visibility = View.VISIBLE
                    tvEmptySubtitle.visibility = View.VISIBLE
                    fabEmpty.show()
                } else {
                    ivRunEmpty.visibility = View.INVISIBLE
                    tvEmptyTitle.visibility = View.INVISIBLE
                    tvEmptySubtitle.visibility = View.INVISIBLE
                    fabEmpty.hide()
                }
                progressAdapter.submitList(it)
            }
        })

        fabEmpty.setOnClickListener {
            findNavController().navigate(R.id.action_runListFragment_to_runStartFragment)
        }
    }

    private fun setupRecyclerView() = recycler_view.apply {
        progressAdapter = ProgressAdapter()
        adapter = progressAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }
}