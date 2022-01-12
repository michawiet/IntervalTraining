package eu.mikko.intervaltraining.viewmodel

import android.app.Application
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.mikko.intervaltraining.data.IntervalTrainingDatabase
import eu.mikko.intervaltraining.model.TrainingNotification
import eu.mikko.intervaltraining.repositories.TrainingNotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderNotificationViewModel @Inject constructor(private val repository: TrainingNotificationRepository) : ViewModel() {

    //val readAllData = repository.readAllTrainingNotifications()

    //fun update(trainingNotification: TrainingNotification) = viewModelScope.launch(Dispatchers.IO) {
    //        repository.update(trainingNotification)
    //}


}