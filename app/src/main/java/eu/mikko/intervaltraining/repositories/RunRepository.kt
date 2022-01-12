package eu.mikko.intervaltraining.repositories

import eu.mikko.intervaltraining.data.RunDao
import eu.mikko.intervaltraining.model.Run
import javax.inject.Inject

class RunRepository @Inject constructor (val runDao: RunDao) {

    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()

    fun getTotalDistance() = runDao.getTotalDistance()
}