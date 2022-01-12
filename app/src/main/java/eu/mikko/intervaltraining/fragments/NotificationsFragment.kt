package eu.mikko.intervaltraining.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.Utilities.Companion.generateCalendar
import eu.mikko.intervaltraining.adapters.TrainingNotificationListAdapter
import eu.mikko.intervaltraining.model.TrainingNotification
import eu.mikko.intervaltraining.notifications.TrainingReminderReceiver
import eu.mikko.intervaltraining.viewmodel.ReminderNotificationViewModel
import kotlinx.android.synthetic.main.fragment_notifications.view.*

@AndroidEntryPoint
class NotificationsFragment : Fragment(R.layout.fragment_notifications) {

    private val viewModel: ReminderNotificationViewModel by viewModels()

    private fun startAlarm(tn: TrainingNotification) {
        val c = generateCalendar(tn)

        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TrainingReminderReceiver::class.java)
        intent.action = Intent.ACTION_DEFAULT
        val pendingIntent = PendingIntent.getBroadcast(context, tn.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent)
    }

    private fun cancelAlarm(id: Int) {
        //id is a request code
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TrainingReminderReceiver::class.java)
        intent.action = Intent.ACTION_DEFAULT
        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.cancel(pendingIntent)
    }
/*
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)
        val adapter = TrainingNotificationListAdapter( {
            TimePickerDialog(context,
                //timeSetListener
                { _, hourOfDay, minute ->
                    val newTrainingNotification = TrainingNotification(it.id, it.dayOfWeek, hourOfDay, minute, true)
                    // if enabled cancel old, add new
                    if(it.isEnabled)
                        cancelAlarm(it.id)
                    startAlarm(newTrainingNotification)

                    // Update the notification in database
                    this.viewModel.update(newTrainingNotification)
                },
                it.hour,
                it.minute,
                true
            ).show()
        }, {
            val newTrainingNotification = TrainingNotification(it.id, it.dayOfWeek, it.hour, it.minute, !it.isEnabled)
            Toast.makeText(context, "Notification toggled! ${it.dayOfWeek}; now is ${!it.isEnabled}; ${it.hour}:${it.minute}", Toast.LENGTH_SHORT).show()

            // If enabled, cancel the alarm
            if(it.isEnabled)
                cancelAlarm(it.id)
            else
                startAlarm(newTrainingNotification)
            // Else create new pending intent

            this.viewModel.update(newTrainingNotification)
        })

        val recyclerView = view.recycler_view

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        this.viewModel = ViewModelProvider(this)[ReminderNotificationViewModel::class.java]
        this.viewModel.readAllData.observe(viewLifecycleOwner, { trainingNotification ->
                adapter.setData(trainingNotification)
            })

        return view
    }*/
}