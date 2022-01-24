package eu.mikko.intervaltraining.other

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ParcelableRunIntervalResult(
    val avgSpeedMetersPerSecond: Float,
    val rating: Int,
    val isRunning: Boolean
) : Parcelable