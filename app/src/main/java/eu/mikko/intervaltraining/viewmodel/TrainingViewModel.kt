package eu.mikko.intervaltraining.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import eu.mikko.intervaltraining.data.IntervalTrainingDatabase
import eu.mikko.intervaltraining.model.Interval
import eu.mikko.intervaltraining.model.Run
import eu.mikko.intervaltraining.repository.IntervalRepository
import eu.mikko.intervaltraining.repository.RunRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrainingViewModel(application: Application) : AndroidViewModel(application) {
    private val intervalRepository: IntervalRepository
    private val runRepository: RunRepository
    val allIntervals: List<Interval>

    init {
        val database = IntervalTrainingDatabase.getDatabase(application)

        val intervalDao = database.getIntervalDao()
        intervalRepository = IntervalRepository(intervalDao)
        allIntervals = intervalRepository.getAllIntervals()

        val runDao = database.getRunDao()
        runRepository = RunRepository(runDao)
    }

    fun addNewRun(run: Run) = viewModelScope.launch(Dispatchers.IO) {
        runRepository.insertRun(run)
    }
}