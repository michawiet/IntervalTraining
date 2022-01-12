package eu.mikko.intervaltraining.repositories

import eu.mikko.intervaltraining.data.TrainingNotificationDao
import eu.mikko.intervaltraining.model.TrainingNotification
import javax.inject.Inject

class TrainingNotificationRepository @Inject constructor(private val trainingNotificationDao: TrainingNotificationDao) {

    fun readAllTrainingNotifications() = trainingNotificationDao.readAllData()

    suspend fun update(trainingNotification: TrainingNotification) = trainingNotificationDao.update(trainingNotification)
}