package eu.mikko.intervaltraining.other

object Constants {
    // database
    const val DATABASE_NAME = "interval_training"
    const val DATABASE_ASSET_PATH = "database/interval_training.db"

    // permissions
    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    // tracking service
    const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_RUN_FRAGMENT = "ACTION_SHOW_RUN_FRAGMENT"

    const val LOCATION_UPDATE_INTERVAL = 2000L
    const val FASTEST_LOCATION_INTERVAL = 1000L
    const val TIMER_UPDATE_INTERVAL = 50L

    // shared pref
    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val KEY_WORKOUT_STEP = "KEY_WORKOUT_STEP"

    //notifications
    const val TRACKING_NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val TRACKING_NOTIFICATION_CHANNEL_NAME = "Activity tracking"
    const val TRACKING_NOTIFICATION_ID = 8

    const val TRAINING_REMINDER_CHANNEL_ID = "trainingRemindersID"
    const val TRAINING_REMINDER_CHANNEL_NAME = "Training reminder"
    const val PROGRESS_CHANNEL_ID = "progressNotificationsID"
    const val PROGRESS_CHANNEL_NAME = "Progress"
    const val PROGRESS_NOTIFICATION_ID = 9
}