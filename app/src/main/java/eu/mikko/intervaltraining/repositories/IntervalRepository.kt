package eu.mikko.intervaltraining.repositories

import eu.mikko.intervaltraining.data.IntervalDao
import javax.inject.Inject

class IntervalRepository @Inject constructor (val intervalDao: IntervalDao) {

    fun getAllIntervals() = intervalDao.getAllIntervals()

    fun getIntervalByWorkoutStep(workoutStep: Int) = intervalDao.getIntervalByWorkoutStep(workoutStep)

    fun getMaxWorkoutStep() = intervalDao.getMaxWorkoutStep()
}