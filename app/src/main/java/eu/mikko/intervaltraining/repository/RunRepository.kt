package eu.mikko.intervaltraining.repository

import androidx.lifecycle.LiveData
import eu.mikko.intervaltraining.data.RunDao
import eu.mikko.intervaltraining.model.Run

class RunRepository(private val runDao: RunDao) {

    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    fun getAllRunsSortedByDate(): LiveData<List<Run>> = runDao.getAllRunsSortedByDate()

    fun getTotalTimeInMillis(): LiveData<Long> = runDao.getTotalTimeInMillis()

    fun getTotalDistance(): LiveData<Int> = runDao.getTotalDistance()
}