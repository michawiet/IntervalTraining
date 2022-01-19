package eu.mikko.intervaltraining.data

import androidx.lifecycle.LiveData
import androidx.room.*
import eu.mikko.intervaltraining.model.TrainingNotification

@Dao
interface TrainingNotificationDao {

    @Update
    suspend fun update(trainingNotification: TrainingNotification)

    @Query("SELECT * FROM training_notifications_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<TrainingNotification>>

    //RestartReminderReceiver only
    @Query("SELECT * FROM training_notifications_table ORDER BY id ASC")
    fun getAllNotificationsNonLive(): List<TrainingNotification>
}