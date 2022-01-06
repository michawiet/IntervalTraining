package eu.mikko.intervaltraining.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import eu.mikko.intervaltraining.entities.TrainingNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingNotificationDao {

    @Insert//(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trainingNotificationEntity: TrainingNotificationEntity)

    @Update
    suspend fun update(trainingNotificationEntity: TrainingNotificationEntity)

    //@Delete
    //fun delete(trainingNotificationEntity: TrainingNotificationEntity)

    @Query("SELECT * FROM training_notifications_table ORDER BY id ASC")
    fun getAll(): Flow<List<TrainingNotificationEntity>>
}