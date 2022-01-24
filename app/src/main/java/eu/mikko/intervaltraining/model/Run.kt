package eu.mikko.intervaltraining.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import eu.mikko.intervaltraining.data.Converters

@Entity(tableName = "runs_table")
data class Run(
    var timestamp: Long = 0L,
    var avgSpeedMetersPerSecond: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var rating: Int = 0,
    var workoutStep: Int = 0,
    var map: Bitmap? = null)
{
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}