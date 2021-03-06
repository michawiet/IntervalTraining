package eu.mikko.intervaltraining.data

import androidx.lifecycle.LiveData
import androidx.room.*
import eu.mikko.intervaltraining.model.Run
import eu.mikko.intervaltraining.model.RunAndInterval

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Query("SELECT * FROM runs_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDateDesc(): LiveData<List<Run>>

    @Query("SELECT * FROM runs_table ORDER BY timestamp ASC")
    fun getAllRunsSortedByDateAsc(): LiveData<List<Run>>

    @Query("SELECT SUM(timeInMillis) FROM runs_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query("SELECT SUM(distanceInMeters) FROM runs_table")
    fun getTotalDistance(): LiveData<Int>

    @Query("SELECT * FROM runs_table WHERE timestamp > :lowerBoundTimestamp")
    fun getRunsWithHigherTimestamp(lowerBoundTimestamp: Long): List<Run>

    @Query("SELECT * FROM runs_table ORDER BY timestamp ASC")
    fun getAllRunsWithIntervals(): LiveData<List<RunAndInterval>>
}