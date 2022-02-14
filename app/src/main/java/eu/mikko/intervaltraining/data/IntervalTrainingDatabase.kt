package eu.mikko.intervaltraining.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import eu.mikko.intervaltraining.model.Interval
import eu.mikko.intervaltraining.model.Run
import eu.mikko.intervaltraining.model.TrainingNotification

@Database(entities = [TrainingNotification::class, Interval::class, Run::class], version = 1, exportSchema = false)
abstract class IntervalTrainingDatabase: RoomDatabase() {

    abstract fun getTrainingNotificationDao(): TrainingNotificationDao
    abstract fun getRunDao(): RunDao
    abstract fun getIntervalDao(): IntervalDao
}