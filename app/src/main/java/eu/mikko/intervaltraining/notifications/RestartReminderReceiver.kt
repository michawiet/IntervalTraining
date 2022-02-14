package eu.mikko.intervaltraining.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import ca.antonious.materialdaypicker.MaterialDayPicker
import eu.mikko.intervaltraining.data.IntervalTrainingDatabase
import eu.mikko.intervaltraining.other.CalendarUtility
import eu.mikko.intervaltraining.other.Constants
import timber.log.Timber

class RestartReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                Timber.d("on boot received")
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
                val c = CalendarUtility.generateCalendar(trainingNotification)
                val intent = Intent(context, TrainingReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context, trainingNotification.id, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent)
            }
        }

        val sharedPref = context.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

        val weekday: String = sharedPref.getString(Constants.KEY_SELECTED_DAY_PROGRESS_NOTIFICATION, "")!!
        if(weekday != "") {
            val hour = sharedPref.getInt(Constants.KEY_HOUR_PROGRESS_NOTIFICATION, 12)
            val minute = sharedPref.getInt(Constants.KEY_MINUTE_PROGRESS_NOTIFICATION, 0)

            val c = CalendarUtility.generateCalendar(hour, minute, MaterialDayPicker.Weekday.valueOf(weekday))
            val intent = Intent(context, ProgressReceiver::class.java)
            intent.action = Intent.ACTION_DEFAULT
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                Constants.PROGRESS_NOTIFICATION_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                c.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        }

        Timber.d("Alarms reset successfully!")
    }
}