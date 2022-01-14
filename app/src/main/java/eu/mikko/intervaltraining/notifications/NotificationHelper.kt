package eu.mikko.intervaltraining.notifications

import android.content.ContextWrapper
import android.app.NotificationManager
import android.annotation.TargetApi
import android.app.Notification
import android.os.Build
import android.app.NotificationChannel
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.other.Constants.PROGRESS_CHANNEL_ID
import eu.mikko.intervaltraining.other.Constants.PROGRESS_CHANNEL_NAME
import eu.mikko.intervaltraining.other.Constants.TRAINING_REMINDER_CHANNEL_ID
import eu.mikko.intervaltraining.other.Constants.TRAINING_REMINDER_CHANNEL_NAME

class NotificationHelper(base: Context?) : ContextWrapper(base) {

    private var mManager: NotificationManager? = null

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val trainingReminderChannel = NotificationChannel(TRAINING_REMINDER_CHANNEL_ID,
            TRAINING_REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH)
        trainingReminderChannel.enableVibration(true)
        trainingReminderChannel.enableLights(true)
        trainingReminderChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val progressNotificationChannel =
            NotificationChannel(PROGRESS_CHANNEL_ID, PROGRESS_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)

        getManager().createNotificationChannel(trainingReminderChannel)
    }

    fun getManager() : NotificationManager {
        if (mManager == null) {
            mManager = getSystemService(NotificationManager::class.java) as NotificationManager
        }
        return mManager!!
    }

    fun trainingScheduledNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(applicationContext, TRAINING_REMINDER_CHANNEL_ID)
            .setContentTitle(TRAINING_REMINDER_CHANNEL_NAME)
            .setContentText(applicationContext.getString(R.string.training_scheduled_notification))
            .setSmallIcon(R.drawable.ic_round_directions_run_24)
            .setCategory("CATEGORY_REMINDER")

    fun trainingProgressNotification(progressStats: String): NotificationCompat.Builder =
        NotificationCompat.Builder(applicationContext, PROGRESS_CHANNEL_ID)
            .setContentTitle(R.string.app_name.toString())
            .setContentText(progressStats)
            .setSmallIcon(R.drawable.ic_round_directions_run_24)
            .setCategory("CATEGORY_REMINDER")

    init {
        createChannel()
    }
}