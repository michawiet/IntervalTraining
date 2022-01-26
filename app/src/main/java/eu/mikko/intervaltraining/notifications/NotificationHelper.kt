package eu.mikko.intervaltraining.notifications

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.core.app.NotificationCompat
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
                NotificationManager.IMPORTANCE_HIGH).also {
            it.enableVibration(true)
            it.enableLights(true)
            it.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }

        val progressNotificationChannel =
            NotificationChannel(PROGRESS_CHANNEL_ID,
                PROGRESS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT).also {
                it.enableVibration(true)
                it.enableLights(true)
                it.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }

        getManager().createNotificationChannel(trainingReminderChannel)
        getManager().createNotificationChannel(progressNotificationChannel)
    }

    fun getManager() : NotificationManager {
        if (mManager == null) {
            mManager = getSystemService(NotificationManager::class.java) as NotificationManager
        }
        return mManager!!
    }

    fun trainingScheduledNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(applicationContext, TRAINING_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_round_directions_run_24)
            .setContentTitle(TRAINING_REMINDER_CHANNEL_NAME)
            .setContentText(applicationContext.getString(R.string.training_scheduled_notification))
            .setCategory("CATEGORY_REMINDER")

    fun progressNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(applicationContext, PROGRESS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_round_directions_run_24)
            .setContentTitle(getString(R.string.weekly_progress_notification_title))
            .setCategory("CATEGORY_REMINDER")

    init {
        createChannel()
    }
}