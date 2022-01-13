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
            } else if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                //Log.d("TrainingReminderReceiver", "on boot received")
                //resetAllAlarms(context)
            }
        }
    }
    /*
    private fun resetAllAlarms(context: Context?) {
        val database = IntervalTrainingDatabase.getDatabase(context!!).getTrainingNotificationDao()
        val getDataJob = GlobalScope.async { database.getAllNotifications() }

        getDataJob.invokeOnCompletion { cause ->
            if (cause != null) {
                Log.d("AlarmManagerHelper", "Could not fetch data")
                Unit
            } else {
                val list = getDataJob.getCompleted()
                for(trainingNotification in list) {
                    if(trainingNotification.isEnabled) {
                        val c = generateCalendar(trainingNotification)
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val intent = Intent(context, TrainingReminderReceiver::class.java)
                        val pendingIntent = PendingIntent.getBroadcast(context, trainingNotification.id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent)
                    }
                }
            }
        }
    }

    private fun cancelAllAlarms(context: Context?) {
        for(dayOfWeek in DayOfWeek.values()) {
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TrainingReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, dayOfWeek.value, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            alarmManager.cancel(pendingIntent)
        }
    }
     */
}