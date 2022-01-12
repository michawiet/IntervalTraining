package eu.mikko.intervaltraining.repository

import androidx.lifecycle.LiveData
import eu.mikko.intervaltraining.data.TrainingNotificationDao
import eu.mikko.intervaltraining.model.TrainingNotification

class TrainingNotificationRepository(private val trainingNotificationDao: TrainingNotificationDao) {

    fun readAllTrainingNotifications(): LiveData<List<TrainingNotification>> = trainingNotificationDao.readAllData()

    suspend fun update(trainingNotification: TrainingNotification) = trainingNotificationDao.update(trainingNotification)
}