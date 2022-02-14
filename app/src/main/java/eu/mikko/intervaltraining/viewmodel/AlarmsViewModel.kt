package eu.mikko.intervaltraining.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.mikko.intervaltraining.model.TrainingNotification
import eu.mikko.intervaltraining.repositories.TrainingNotificationRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmsViewModel @Inject constructor(private val repository: TrainingNotificationRepository) : ViewModel() {

    val readAllData = repository.readAllTrainingNotifications()

    fun update(trainingNotification: TrainingNotification) = viewModelScope.launch {
            repository.update(trainingNotification)
    }
}