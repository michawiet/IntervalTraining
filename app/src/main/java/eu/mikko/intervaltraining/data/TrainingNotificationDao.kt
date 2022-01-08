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

    //@Delete
    //fun delete(trainingNotificationEntity: TrainingNotification)

    @Query("SELECT * FROM training_notifications_table ORDER BY id ASC")
    fun readAllData(): LiveData<List<TrainingNotification>>
}