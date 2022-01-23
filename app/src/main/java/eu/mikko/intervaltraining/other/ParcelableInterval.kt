package eu.mikko.intervaltraining.other

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ParcelableInterval(val warmupSeconds: Long,
                              val runSeconds: Long,
                              val walkSeconds: Long,
                              val totalWorkoutTime: Long) : Parcelable