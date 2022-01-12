package eu.mikko.intervaltraining.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import eu.mikko.intervaltraining.model.*

@Database(entities = [TrainingNotification::class, Interval::class, Run::class], version = 1, exportSchema = false)
abstract class IntervalTrainingDatabase: RoomDatabase() {

    abstract fun getTrainingNotificationDao(): TrainingNotificationDao
    abstract fun getRunDao(): RunDao
    abstract fun getIntervalDao(): IntervalDao

    companion object {
        @Volatile
        private var INSTANCE: IntervalTrainingDatabase? = null

        fun getDatabase(context: Context): IntervalTrainingDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IntervalTrainingDatabase::class.java,
                    "interval_training")
                    .createFromAsset("database/interval_training.db")
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}