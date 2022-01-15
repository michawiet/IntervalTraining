package eu.mikko.intervaltraining.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.mikko.intervaltraining.model.Run
import eu.mikko.intervaltraining.repositories.IntervalRepository
import eu.mikko.intervaltraining.repositories.RunRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val intervalRepository: IntervalRepository,
    private val runRepository: RunRepository) : ViewModel() {

    val allIntervals = intervalRepository.getAllIntervals()

    fun getIntervalByWorkoutStep(step: Int) = intervalRepository.getIntervalByWorkoutStep(step)

    fun insertNewRun(run: Run) = viewModelScope.launch {
        runRepository.insertRun(run)
    }
}