package eu.mikko.intervaltraining.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import eu.mikko.intervaltraining.Utilities.Companion.generateCalendar
import eu.mikko.intervaltraining.data.IntervalTrainingDatabase
import eu.mikko.intervaltraining.model.TrainingNotification
import eu.mikko.intervaltraining.repository.TrainingNotificationRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TrainingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationHelper = NotificationHelper(context)
        val notificationBuilder = notificationHelper.trainingScheduledNotification()
        notificationHelper.getManager().notify(1, notificationBuilder.build())
    }
}