package eu.mikko.intervaltraining.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.viewmodel.TrainingViewModel
import kotlinx.android.synthetic.main.fragment_run.*

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    private val viewModel: TrainingViewModel by viewModels()
    private var isPaused = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityStopFab.hide()
        activityPlayPauseFab.setOnClickListener {
            if (isPaused) {
                isPaused = false
                activityStopFab.hide()
                activityPlayPauseFab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_pause_24))
            } else {
                isPaused = true
                activityStopFab.show()
                activityPlayPauseFab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_play_arrow_24))

            }
        }
        activityStopFab.setOnClickListener {
            //Dialog to confirm the action
            context?.let { c ->
                AlertDialog.Builder(c)
                    .setTitle(getString(R.string.stop_activity_title))
                    .setMessage(getString(R.string.stop_activity_message))
                    .setPositiveButton(getString(R.string.confirm_cancel)) { dialog, _ ->
                        // TODO("cancel all the stuff")
                        dialog.cancel()
                        findNavController().navigate(R.id.action_runFragment_to_runStartFragment)
                    }.setNegativeButton(getString(R.string.confirm_continue)) { dialog, _ ->
                        dialog.cancel()
                    }.create()
                    .show()
            }

        }
    }
}