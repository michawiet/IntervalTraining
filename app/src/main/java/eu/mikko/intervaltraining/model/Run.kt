package eu.mikko.intervaltraining.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "runs_table")
data class Run(
    var timestamp: Long = 0L,
    var avgSpeedInKMH: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}