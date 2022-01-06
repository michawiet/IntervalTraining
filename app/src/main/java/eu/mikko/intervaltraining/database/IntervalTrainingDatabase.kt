package eu.mikko.intervaltraining.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import eu.mikko.intervaltraining.dao.TrainingNotificationDao
import eu.mikko.intervaltraining.entities.TrainingNotificationEntity

@Database(entities = [TrainingNotificationEntity::class], version = 1)
abstract class IntervalTrainingDatabase: RoomDatabase() {

    abstract fun trainingNotificationDao(): TrainingNotificationDao

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
                    "interval_training"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}