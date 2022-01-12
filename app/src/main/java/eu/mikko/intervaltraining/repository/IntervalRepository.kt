package eu.mikko.intervaltraining.repository

import eu.mikko.intervaltraining.data.IntervalDao
import eu.mikko.intervaltraining.model.Interval

class IntervalRepository(private val intervalDao: IntervalDao) {

    fun getAllIntervals(): List<Interval> = intervalDao.getAllIntervals()

    fun getIntervalByWorkoutStep(workoutStep: Int): Interval = intervalDao.getIntervalByWorkoutStep(workoutStep)
}