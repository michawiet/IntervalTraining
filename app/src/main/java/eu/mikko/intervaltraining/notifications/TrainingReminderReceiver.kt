package eu.mikko.intervaltraining.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TrainingReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null) {
            if (Intent.ACTION_DEFAULT == intent.action) {
                val notificationHelper = NotificationHelper(context)
                val notificationBuilder = notificationHelper.trainingScheduledNotification()
                notificationHelper.getManager().notify(1, notificationBuilder.build())
            }
        }
    }
}