package eu.mikko.intervaltraining.other

import android.graphics.Color
import java.time.DayOfWeek

object Constants {
    // database
    const val DATABASE_NAME = "interval_training"
    const val DATABASE_ASSET_PATH = "database/interval_training_mock.db"
    //const val DATABASE_ASSET_PATH = "database/interval_training.db"

    // permissions
    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    // tracking service
    const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_RUN_FRAGMENT = "ACTION_SHOW_RUN_FRAGMENT"
    const val ACTION_INTERVAL_DATA = "ACTION_INTERVAL_DATA"

    const val EXTRAS_INTERVAL_DATA = "EXTRAS_INTERVAL_DATA"

    const val LOCATION_UPDATE_INTERVAL = 2000L
    const val FASTEST_LOCATION_INTERVAL = 1000L
    const val TIMER_UPDATE_INTERVAL = 50L

    // shared pref
    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val KEY_WORKOUT_LEVEL = "KEY_WORKOUT_LEVEL"
    const val KEY_SELECTED_DAY_PROGRESS_NOTIFICATION = "KEY_SELECTED_DAY_PROGRESS_NOTIFICATION"
    const val KEY_HOUR_PROGRESS_NOTIFICATION = "KEY_HOUR_PROGRESS_NOTIFICATION"
    const val KEY_MINUTE_PROGRESS_NOTIFICATION = "KEY_MINUTE_PROGRESS_NOTIFICATION"

    //notifications
    const val TRACKING_NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val TRACKING_NOTIFICATION_CHANNEL_NAME = "Activity tracking"
    const val TRACKING_NOTIFICATION_ID = 8

    const val TRAINING_REMINDER_CHANNEL_ID = "trainingRemindersID"
    const val TRAINING_REMINDER_CHANNEL_NAME = "Training reminder"
    const val PROGRESS_CHANNEL_ID = "progressNotificationsID"
    const val PROGRESS_CHANNEL_NAME = "Progress"
    const val PROGRESS_NOTIFICATION_ID = 9

    const val PROGRESS_NOTIFICATION_HOUR = 20
    const val PROGRESS_NOTIFICATION_MINUTE = 0
    val PROGRESS_NOTIFICATION_DAY = DayOfWeek.SUNDAY

    const val RUN_PACE = 2.78f
    const val WALK_PACE = 1.389f

    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 17f

    const val INTERVAL_GOOD_PRECISION_LOWER_BOUND = 75
}