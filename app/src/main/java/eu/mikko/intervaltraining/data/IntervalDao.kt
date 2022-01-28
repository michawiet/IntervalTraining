package eu.mikko.intervaltraining.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import eu.mikko.intervaltraining.model.Interval

@Dao
interface IntervalDao {
    @Query("SELECT * FROM intervals_table ORDER BY workoutLevel ASC")
    fun getAllIntervals(): LiveData<List<Interval>>

    @Query("SELECT * FROM intervals_table WHERE workoutLevel=:workoutLevel")
    fun getIntervalByWorkoutStep(workoutLevel: Int): LiveData<Interval>

    @Query("SELECT MAX(workoutLevel) FROM intervals_table")
    fun getMaxWorkoutStep(): LiveData<Int>

    //Only for use in broadcast receiver
    @Query("SELECT MAX(workoutLevel) FROM intervals_table")
    fun getMaxWorkoutStepNonLive(): Int
}