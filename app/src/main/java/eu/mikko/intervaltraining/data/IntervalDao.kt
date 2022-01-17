package eu.mikko.intervaltraining.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import eu.mikko.intervaltraining.model.Interval

@Dao
interface IntervalDao {
    @Query("SELECT * FROM intervals_table ORDER BY workoutStep ASC")
    fun getAllIntervals(): LiveData<List<Interval>>

    @Query("SELECT * FROM intervals_table WHERE workoutStep=:workoutStep")
    fun getIntervalByWorkoutStep(workoutStep: Int): LiveData<Interval>

    @Query("SELECT MAX(workoutStep) FROM intervals_table")
    fun getMaxWorkoutStep(): LiveData<Int>
}