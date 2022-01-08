package eu.mikko.intervaltraining.viewmodel

import android.app.Application
import androidx.lifecycle.*
import eu.mikko.intervaltraining.data.IntervalTrainingDatabase
import eu.mikko.intervaltraining.model.TrainingNotification
import eu.mikko.intervaltraining.repository.TrainingNotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrainingNotificationViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<TrainingNotification>>
    private val repository: TrainingNotificationRepository

    init {
        val trainingNotificationDao = IntervalTrainingDatabase.getDatabase(application).trainingNotificationDao()
        repository = TrainingNotificationRepository(trainingNotificationDao)
        readAllData = repository.readAllTrainingNotifications
    }

    fun insert(trainingNotification: TrainingNotification) = viewModelScope.launch {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(trainingNotification)
        }
    }

    fun update(trainingNotification: TrainingNotification) = viewModelScope.launch {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(trainingNotification)
        }
    }


}