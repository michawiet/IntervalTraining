package eu.mikko.intervaltraining.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.adapters.JournalAdapter
import eu.mikko.intervaltraining.viewmodel.JournalViewModel
import kotlinx.android.synthetic.main.fragment_journal.*

@AndroidEntryPoint
class JournalFragment : Fragment(R.layout.fragment_journal) {

    private val viewModel: JournalViewModel by viewModels()

    private lateinit var journalAdapter: JournalAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.allRuns.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isEmpty()) {
                    ivRunEmpty.visibility = View.VISIBLE
                    tvEmptyTitle.visibility = View.VISIBLE
                    tvEmptySubtitle.visibility = View.VISIBLE
                } else {
                    ivRunEmpty.visibility = View.INVISIBLE
                    tvEmptyTitle.visibility = View.INVISIBLE
                    tvEmptySubtitle.visibility = View.INVISIBLE
                }
                journalAdapter.submitList(it)
            }
        }
    }

    private fun setupRecyclerView() = recycler_view.apply {
        journalAdapter = JournalAdapter()
        adapter = journalAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }
}