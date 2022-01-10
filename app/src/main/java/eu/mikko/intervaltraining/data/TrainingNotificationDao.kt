package eu.mikko.intervaltraining.data

import androidx.lifecycle.LiveData
import androidx.room.*
import eu.mikko.intervaltraining.model.TrainingNotification

@Dao
interface TrainingNotificationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(trainingNotification: TrainingNotification)

    @Update
    suspend fun update(trainingNotification: TrainingNotification)

    @Query("SELECT * FROM training_notifications_table WHERE id=:id")
    suspend fun getTrainingNotificationById(id: Int): TrainingNotification

    @Query("SELECT * FROM training_notifications_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<TrainingNotification>>

    //One time use only
    @Query("SELECT * FROM training_notifications_table ORDER BY id ASC")
    suspend fun getAllNotifications(): List<TrainingNotification>

    @Query("SELECT * FROM training_notifications_table WHERE id=:id")
    suspend fun getNotification(id: Int): TrainingNotification
}