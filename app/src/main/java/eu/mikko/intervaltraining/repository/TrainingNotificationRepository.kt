package eu.mikko.intervaltraining.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import eu.mikko.intervaltraining.dao.TrainingNotificationDao
import eu.mikko.intervaltraining.entities.TrainingNotificationEntity
import kotlinx.coroutines.flow.Flow

class TrainingNotificationRepository(private val trainingNotificationDao: TrainingNotificationDao) {
    val allTrainingNotification: Flow<List<TrainingNotificationEntity>> = trainingNotificationDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(trainingNotificationEntity: TrainingNotificationEntity) {
        trainingNotificationDao.insert(trainingNotificationEntity)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(trainingNotificationEntity: TrainingNotificationEntity) {
        trainingNotificationDao.update(trainingNotificationEntity)
    }
}