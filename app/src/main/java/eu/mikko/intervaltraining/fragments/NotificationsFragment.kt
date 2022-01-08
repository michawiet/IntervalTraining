package eu.mikko.intervaltraining.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.adapters.TrainingNotificationListAdapter
import eu.mikko.intervaltraining.viewmodel.TrainingNotificationViewModel
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.fragment_notifications.view.*

/**
 * A simple [Fragment] subclass.
 * Use the [NotificationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NotificationsFragment : Fragment() {

    private lateinit var mTrainingNotificationViewModel: TrainingNotificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        val adapter = TrainingNotificationListAdapter()
        val recyclerView = view.recycler_view

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //recycler_view.setHasFixedSize(true)

        mTrainingNotificationViewModel = ViewModelProvider(this).get(TrainingNotificationViewModel::class.java)
        mTrainingNotificationViewModel.readAllData.observe(viewLifecycleOwner, Observer { trainingNotification ->
            adapter.setData(trainingNotification)
        })

        return view
    //return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    //override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    //    super.onViewCreated(view, savedInstanceState)
//
    //    //recycler_view.adapter = TrainingNotificationListAdapter()
    //    //recycler_view.layoutManager = LinearLayoutManager(context)
    //    //recycler_view.setHasFixedSize(true)
    //}
}