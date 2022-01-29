package eu.mikko.intervaltraining.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.room.Room
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.data.IntervalDao
import eu.mikko.intervaltraining.data.IntervalTrainingDatabase
import eu.mikko.intervaltraining.data.RunDao
import eu.mikko.intervaltraining.other.Constants
import eu.mikko.intervaltraining.other.Constants.PROGRESS_NOTIFICATION_ID
import eu.mikko.intervaltraining.other.Constants.SHARED_PREFERENCES_NAME
import timber.log.Timber

class ProgressReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            Timber.d("Progress intent received")
            if (Intent.ACTION_DEFAULT == intent.action) {
                val notificationHelper = NotificationHelper(context)
                val notificationBuilder = notificationHelper.progressNotification()
                val progressString = getProgress(context)
                notificationBuilder.setStyle(NotificationCompat.BigTextStyle()
                    .bigText(progressString))
                notificationHelper.getManager().notify(PROGRESS_NOTIFICATION_ID, notificationBuilder.build())
            }
        }
    }

    private fun getProgress(context: Context?): String {
        val database = Room.databaseBuilder(context!!, IntervalTrainingDatabase::class.java, Constants.DATABASE_NAME)
            .allowMainThreadQueries()
            .createFromAsset(Constants.DATABASE_ASSET_PATH)
            .build()
        val numberOfWorkoutsDone = getNumberOfWorkoutsDone(database.getRunDao(), context)
        val progressPercent = getProgressPercent(database.getIntervalDao(), context)

        return "$numberOfWorkoutsDone\n$progressPercent"
    }

    private fun getNumberOfWorkoutsDone(runDao: RunDao, context: Context?): String {
        val list = runDao.getRunsWithHigherTimestamp(System.currentTimeMillis() - (AlarmManager.INTERVAL_DAY * 7))
        if (context != null) {
            return if(list.isEmpty()) {
                context.getString(R.string.notification_did_not_train)
            } else {
                when(list.size) {
                    in 1 .. 2 -> context.getString(R.string.notification_training_recommendation_not_met, list.size)
                    3 -> context.getString(R.string.notification_training_recommendation_met)
                    else -> context.getString(R.string.notification_training_recommendation_exceeded, list.size)
                }
            }
        }
        return ""
    }

    private fun getProgressPercent(intervalDao: IntervalDao, context: Context?): String {
        val sharedPref = context!!.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val currentWorkoutStep = sharedPref.getInt(Constants.KEY_WORKOUT_LEVEL, 1) - 1

        val maxWorkoutStep = intervalDao.getMaxWorkoutStepNonLive()
        return when(val percentDone = currentWorkoutStep.div(maxWorkoutStep.toFloat() - 1f).times(100f).toInt()) {
            0 -> context.getString(R.string.notification_goal_completed_0_percent, percentDone)
            in 1 .. 29 -> context.getString(R.string.notification_goal_completed_1_to_29_percent, percentDone)
            in 30 .. 39 -> context.getString(R.string.notification_goal_completed_30_to_39_percent, percentDone)
            in 40 .. 49 -> context.getString(R.string.notification_goal_completed_40_to_49_percent, percentDone)
            in 50 .. 59 -> context.getString(R.string.notification_goal_completed_50_to_59_percent, percentDone)
            in 60 .. 79 -> context.getString(R.string.notification_goal_completed_60_to_79_percent, percentDone)
            in 80 .. 99 -> context.getString(R.string.notification_goal_completed_80_to_99_percent, percentDone)
            else -> context.getString(R.string.notification_goal_completed_100_percent, percentDone)
        }
    }
}