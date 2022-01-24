package eu.mikko.intervaltraining.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.mikko.intervaltraining.repositories.IntervalRepository
import javax.inject.Inject

@HiltViewModel
class IntervalViewModel @Inject constructor(
    private val intervalRepository: IntervalRepository,
) : ViewModel() {

    fun getIntervalByWorkoutStep(step: Int) = intervalRepository.getIntervalByWorkoutStep(step)

    fun getMaxWorkoutStep() = intervalRepository.getMaxWorkoutStep()
}