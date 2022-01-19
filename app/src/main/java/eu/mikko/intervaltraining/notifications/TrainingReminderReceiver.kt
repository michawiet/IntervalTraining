package eu.mikko.intervaltraining.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.data.IntervalTrainingDatabase
import eu.mikko.intervaltraining.other.Constants.DATABASE_ASSET_PATH
import eu.mikko.intervaltraining.other.Constants.DATABASE_NAME
import eu.mikko.intervaltraining.other.Utilities.generateCalendar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.time.DayOfWeek
import javax.inject.Inject

@AndroidEntryPoint
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