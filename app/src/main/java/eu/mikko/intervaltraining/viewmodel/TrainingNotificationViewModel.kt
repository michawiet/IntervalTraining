package eu.mikko.intervaltraining.viewmodel

import androidx.lifecycle.*
import eu.mikko.intervaltraining.entities.TrainingNotificationEntity
import eu.mikko.intervaltraining.repository.TrainingNotificationRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class TrainingNotificationViewModel(private val repository: TrainingNotificationRepository) : ViewModel() {
    val allTrainingNotifications: LiveData<List<TrainingNotificationEntity>> = repository.allTrainingNotification.asLiveData()

    fun insert(trainingNotificationEntity: TrainingNotificationEntity) = viewModelScope.launch {
        repository.insert(trainingNotificationEntity)
    }

    fun update(trainingNotificationEntity: TrainingNotificationEntity) = viewModelScope.launch {
        repository.update(trainingNotificationEntity)
    }
}

class TrainingNotificationViewModelFactory(private val repository: TrainingNotificationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(TrainingNotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrainingNotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}