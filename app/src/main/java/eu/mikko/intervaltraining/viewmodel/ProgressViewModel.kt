package eu.mikko.intervaltraining.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.mikko.intervaltraining.repositories.IntervalRepository
import eu.mikko.intervaltraining.repositories.RunRepository
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    runRepository: RunRepository,
    private val intervalRepository: IntervalRepository) : ViewModel() {

    val allRuns = runRepository.getAllRunsSortedByDate()
    val totalDistance = runRepository.getTotalDistance()
    val totalTimeInMillis = runRepository.getTotalTimeInMillis()

    fun getMaxWorkoutStep() = intervalRepository.getMaxWorkoutStep()
}