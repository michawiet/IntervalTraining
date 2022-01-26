package eu.mikko.intervaltraining.notifications

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.room.Room
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
        val numberOfWorkoutsDone = getNumberOfWorkoutsDone(database.getRunDao())
        val progressPercent = getProgressPercent(database.getIntervalDao(), context)

        return "$numberOfWorkoutsDone\n$progressPercent"
    }

    private fun getNumberOfWorkoutsDone(runDao: RunDao): String {
        val list = runDao.getRunsWithHigherTimestamp(System.currentTimeMillis() - (AlarmManager.INTERVAL_DAY * 7))
        return if(list.isEmpty()) {
            "You did not train this week! Train 3 times per week!"
        } else {
            when(list.size) {
                in 1 .. 2 -> "It is recommended that you train 3 times per week! (${list.size}/3)"
                3 -> "You met the recommendations for the number of training days this week."
                else -> "It is recommended to train 3 times per week - more than that increases the chance of injury! (${list.size}/3)"
            }
        }
    }

    private fun getProgressPercent(intervalDao: IntervalDao, context: Context?): String {
        var progressInfo = ""
        val sharedPref = context!!.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val currentWorkoutStep = sharedPref.getInt(Constants.KEY_WORKOUT_LEVEL, 1) - 1

        val maxWorkoutStep = intervalDao.getMaxWorkoutStepNonLive()
        when(val percentDone = currentWorkoutStep.div(maxWorkoutStep.toFloat() - 1f).times(100f).toInt()) {
            0 -> progressInfo = "Hey, don't discourage yourself - YOU can do this! $percentDone% of workout levels completed"
            in 1 .. 29 -> progressInfo = "Keep training! $percentDone% of the goal achieved"
            in 30 .. 39 -> progressInfo = "Dream on, dream on, dream until the dream come true! $percentDone% of workout levels completed"
            in 40 .. 49 -> progressInfo = "Work it, make it, do it, makes us harder, better, faster, stronger! $percentDone% of workout levels completed"
            in 50 .. 59 -> progressInfo = "Whoa, you're half way there, whoa-oh, livin' on a prayer! $percentDone% of workout levels completed"
            in 60 .. 79 -> progressInfo = "You're still standing, after all this time! $percentDone% of workout levels completed"
            in 80 .. 99 -> progressInfo = "You're gonna go, go, go, there's no stopping you! $percentDone% of workout levels completed"
            100 -> progressInfo = "You are the champion, my friend! Congratulations, you've completed $percentDone% of workout levels!"
        }
        return progressInfo
    }
}