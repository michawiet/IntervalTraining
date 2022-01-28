package eu.mikko.intervaltraining.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.adapters.AlarmsAdapter
import eu.mikko.intervaltraining.model.TrainingNotification
import eu.mikko.intervaltraining.notifications.TrainingReminderReceiver
import eu.mikko.intervaltraining.other.CalendarUtility.generateCalendar
import eu.mikko.intervaltraining.viewmodel.AlarmsViewModel
import kotlinx.android.synthetic.main.fragment_alarms.view.*
import java.time.DayOfWeek
import java.time.format.TextStyle

@AndroidEntryPoint
class AlarmsFragment : Fragment(R.layout.fragment_alarms) {

    private val viewModel: AlarmsViewModel by viewModels()

    private fun getPendingIntent(id: Int): PendingIntent {
        val intent = Intent(context, TrainingReminderReceiver::class.java)
        intent.action = Intent.ACTION_DEFAULT
        return PendingIntent.getBroadcast(context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun startAlarm(tn: TrainingNotification) {
        val c = generateCalendar(tn)
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.timeInMillis, AlarmManager.INTERVAL_DAY * 7, getPendingIntent(tn.id))
    }

    private fun cancelAlarm(id: Int) {
        //id is a request code
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getPendingIntent(id))
    }

    private fun showSnackBarAlarmEnabled(hour: Int, minute: Int, dayOfWeek: String) {
        val dayOfWeekDisplayName = DayOfWeek
            .valueOf(dayOfWeek)
            .getDisplayName(TextStyle.FULL, ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0])
        Snackbar.make(
            requireActivity().findViewById(R.id.rootView),
            String.format("Training reminder enabled - %s, %02d:%02d", dayOfWeekDisplayName, hour, minute),
            Snackbar.LENGTH_LONG
        ).setAnchorView(R.id.bottom_navigation).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alarms, container, false)
        val adapter = AlarmsAdapter( {
            TimePickerDialog(requireContext(),
                //timeSetListener
                { _, hourOfDay, minute ->
                    val newTrainingNotification = TrainingNotification(it.id, it.dayOfWeek, hourOfDay, minute, true)
                    //add new alarm
                    startAlarm(newTrainingNotification)
                    showSnackBarAlarmEnabled(hourOfDay, minute, it.dayOfWeek)
                    this.viewModel.update(newTrainingNotification)
                },
                it.hour,
                it.minute,
                true
            ).show()
        }, {
            val newTrainingNotification = TrainingNotification(it.id, it.dayOfWeek, it.hour, it.minute, !it.isEnabled)
            if(it.isEnabled)
                cancelAlarm(it.id)
            else {
                startAlarm(newTrainingNotification)
                showSnackBarAlarmEnabled(it.hour, it.minute, it.dayOfWeek)
            }
            this.viewModel.update(newTrainingNotification)
        })

        val recyclerView = view.recycler_view

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        this.viewModel.readAllData.observe(viewLifecycleOwner) { trainingNotification ->
            adapter.setData(trainingNotification)
        }

        return view
    }
}