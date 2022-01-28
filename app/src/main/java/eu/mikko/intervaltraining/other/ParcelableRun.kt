package eu.mikko.intervaltraining.other

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ParcelableRun(
    val timestamp: Long,
    val avgSpeedMetersPerSecond: Float,
    val distanceInMeters: Int,
    val timeInMillis: Long,
    val rating: Int,
    val workoutLevel: Int,
    var map: Bitmap? = null
) : Parcelable