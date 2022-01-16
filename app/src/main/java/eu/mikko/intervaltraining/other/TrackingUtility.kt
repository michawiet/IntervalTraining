package eu.mikko.intervaltraining.other

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun hasLocationPermissions(context: Context) =
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

    fun getFormattedStopWatchTime(ms: Long): String {
        var millis = ms

        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        millis -= TimeUnit.SECONDS.toMillis(seconds)

        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getFormattedTimeFromSeconds(s: Long): String {
        var seconds = s

        val minutes = TimeUnit.SECONDS.toMinutes(seconds)
        seconds -= TimeUnit.MINUTES.toSeconds(minutes)

        return String.format("%02d:%02d", minutes, seconds)
    }

    @Parcelize
    data class ParcelableInterval(val warmupSeconds: Long,
                                  val runSeconds: Long,
                                  val walkSeconds: Long,
                                  val totalWorkoutTime: Long) : Parcelable

    data class RunData(private val intervalData: ParcelableInterval) {
        data class IntervalData(val startMillis: Long, val lengthMillis: Long, val isRunningInterval: Boolean)

        val intervals: ArrayList<IntervalData> = arrayListOf()
        val totalWorkoutTime: Long = intervalData.totalWorkoutTime * 1000

        init {
            var timeLeft = this.totalWorkoutTime
            intervals.add(IntervalData(0L, intervalData.warmupSeconds * 1000, false))
            timeLeft -= intervals.last().lengthMillis
            //val currentTimeStamp = intervals.last().lengthMillis

            var isCurrentRunningInterval = true
            while(timeLeft > 0) {
                val timestamp = this.totalWorkoutTime - timeLeft
                var toAdd : Long
                if(isCurrentRunningInterval) {
                    toAdd = intervalData.runSeconds * 1000
                    if(toAdd > timeLeft) intervals.add(IntervalData(timestamp, timeLeft, true))
                    else intervals.add(IntervalData(timestamp, toAdd, true))
                } else {
                    toAdd = intervalData.walkSeconds * 1000
                    if(toAdd > timeLeft) intervals.add(IntervalData(timestamp, timeLeft, false))
                    else intervals.add(IntervalData(timestamp, toAdd, false))
                }

                timeLeft -= intervals.last().lengthMillis
                isCurrentRunningInterval = !isCurrentRunningInterval
            }
        }
    }
}