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

class NotificationHelper(base: Context?) : ContextWrapper(base) {

    private var mManager: NotificationManager? = null

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val trainingReminderChannel = NotificationChannel(channelTrainingRemindersID,
                channelTrainingRemindersName,
                NotificationManager.IMPORTANCE_HIGH)
        trainingReminderChannel.enableVibration(true)
        trainingReminderChannel.enableLights(true)
        trainingReminderChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val progressNotificationChannel =
            NotificationChannel(channelProgressID, channelTrainingRemindersName, NotificationManager.IMPORTANCE_DEFAULT)

        getManager().createNotificationChannel(trainingReminderChannel)
    }

    fun getManager() : NotificationManager {
        if (mManager == null) {
            mManager = getSystemService(NotificationManager::class.java) as NotificationManager
        }
        return mManager!!
    }

    fun trainingScheduledNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(applicationContext, channelTrainingRemindersID)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(applicationContext.getString(R.string.training_scheduled_notification))
            .setSmallIcon(R.drawable.ic_round_directions_run_24)
            .setCategory("CATEGORY_REMINDER")

    fun trainingProgressNotification(progressStats: String): NotificationCompat.Builder =
        NotificationCompat.Builder(applicationContext, channelProgressID)
            .setContentTitle(R.string.app_name.toString())
            .setContentText(progressStats)
            .setSmallIcon(R.drawable.ic_round_directions_run_24)
            .setCategory("CATEGORY_REMINDER")

    companion object {
        const val channelTrainingRemindersID = "trainingRemindersID"
        const val channelTrainingRemindersName = "Training reminder"
        const val channelProgressID = "progressNotificationsID"
        const val channelProgressName = R.string.progress_notification_channel_name.toString()
    }

    init {
        createChannel()
    }
}