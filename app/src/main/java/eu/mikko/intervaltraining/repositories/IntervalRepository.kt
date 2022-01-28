package eu.mikko.intervaltraining.repositories

import eu.mikko.intervaltraining.data.IntervalDao
import javax.inject.Inject

class IntervalRepository @Inject constructor (val intervalDao: IntervalDao) {

    fun getAllIntervals() = intervalDao.getAllIntervals()

    fun getIntervalByWorkoutLevel(workoutStep: Int) = intervalDao.getIntervalByWorkoutStep(workoutStep)

    fun getMaxWorkoutLevel() = intervalDao.getMaxWorkoutStep()
}