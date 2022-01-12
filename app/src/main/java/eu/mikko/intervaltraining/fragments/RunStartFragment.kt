package eu.mikko.intervaltraining.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.viewmodel.TrainingViewModel
import kotlinx.android.synthetic.main.fragment_run_start.*

@AndroidEntryPoint
class RunStartFragment : Fragment(R.layout.fragment_run_start) {

    private val viewModel: TrainingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        runStartButton.setOnClickListener {
            //
            findNavController().navigate(R.id.action_runStartFragment_to_runFragment)
        }
    }
}