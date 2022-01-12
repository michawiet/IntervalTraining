package eu.mikko.intervaltraining.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import eu.mikko.intervaltraining.data.IntervalTrainingDatabase
import eu.mikko.intervaltraining.model.Run
import eu.mikko.intervaltraining.repository.RunRepository

class ProgressViewModel(application: Application) : AndroidViewModel(application) {
    val allRuns: LiveData<List<Run>>
    val totalDistance: LiveData<Int>
    val totalTimeInMillis: LiveData<Long>
    private val runRepository: RunRepository

    init {
        val runDao = IntervalTrainingDatabase.getDatabase(application).getRunDao()
        runRepository = RunRepository(runDao)
        allRuns = runRepository.getAllRunsSortedByDate()
        totalDistance = runRepository.getTotalDistance()
        totalTimeInMillis = runRepository.getTotalTimeInMillis()
    }
}