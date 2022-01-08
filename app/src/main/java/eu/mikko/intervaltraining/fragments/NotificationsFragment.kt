package eu.mikko.intervaltraining.fragments

import android.app.TimePickerDialog
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.adapters.TrainingNotificationListAdapter
import eu.mikko.intervaltraining.viewmodel.TrainingNotificationViewModel
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.fragment_notifications.view.*
import android.widget.RelativeLayout.LayoutParams
import android.widget.TextView
import androidx.core.os.ConfigurationCompat
import eu.mikko.intervaltraining.model.TrainingNotification
import java.time.DayOfWeek
import java.time.format.TextStyle
import kotlin.math.min

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

        val adapter = TrainingNotificationListAdapter( {
            TimePickerDialog(context,
                { view, hourOfDay, minute ->
                    val newTime = String.format("%02d:%02d", hourOfDay, minute)
                    Toast.makeText(context, "Notification on ${it.dayOfWeek} is ${it.isEnabled} at $hourOfDay:$minute", Toast.LENGTH_SHORT).show()
                    // Update the notification in database
                    mTrainingNotificationViewModel.update(TrainingNotification(it.id, it.dayOfWeek, newTime, true))
                    // If the pending intent exists, update it

                    // Else create new pending intent
                },
                Integer.parseInt(it.time.subSequence(0, 2).toString()),
                Integer.parseInt(it.time.subSequence(3, 5).toString()),
                true
            ).show()
        }, {
            Toast.makeText(context, "Notification toggled! ${it.dayOfWeek}; ${it.isEnabled}; ${it.time}", Toast.LENGTH_SHORT).show()
            mTrainingNotificationViewModel.update(TrainingNotification(it.id, it.dayOfWeek, it.time, !it.isEnabled))
        })

        val recyclerView = view.recycler_view

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        mTrainingNotificationViewModel = ViewModelProvider(this)[TrainingNotificationViewModel::class.java]
        mTrainingNotificationViewModel.readAllData.observe(viewLifecycleOwner, { trainingNotification ->
                adapter.setData(trainingNotification)
            })

        return view
    }
}