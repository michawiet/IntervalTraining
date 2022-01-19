package eu.mikko.intervaltraining.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import eu.mikko.intervaltraining.data.IntervalTrainingDatabase
import eu.mikko.intervaltraining.other.Constants
import eu.mikko.intervaltraining.other.Utilities
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import timber.log.Timber

class RestartReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                Log.d("RestartReminderReceiver", "on boot received")
                resetAllAlarms(context)
            }
        }
    }

    private fun resetAllAlarms(context: Context?) {
        val database = Room.databaseBuilder(context!!, IntervalTrainingDatabase::class.java,Constants.DATABASE_NAME)
            .allowMainThreadQueries()
            .createFromAsset(Constants.DATABASE_ASSET_PATH)
            .build()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notificationDao = database.getTrainingNotificationDao()
        val list = notificationDao.getAllNotificationsNonLive()
        for(trainingNotification in list) {
            if(trainingNotification.isEnabled) {
                val c = Utilities.generateCalendar(trainingNotification)
                val intent = Intent(context, TrainingReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context, trainingNotification.id, intent, 0)
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent)
            }
        }
        //Progress notification
        val c = Utilities.generateCalendar(
            Constants.PROGRESS_NOTIFICATION_HOUR,
            Constants.PROGRESS_NOTIFICATION_MINUTE,
            Constants.PROGRESS_NOTIFICATION_DAY
        )
        val intent = Intent(context, ProgressReceiver::class.java)
        intent.action = Intent.ACTION_DEFAULT
        val pendingIntent = PendingIntent.getBroadcast(context,
            Constants.PROGRESS_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent)

        Timber.d("Alarms reset successfully!")
    }
}