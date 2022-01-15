package eu.mikko.intervaltraining.other

import android.Manifest
import android.content.Context
import android.os.Build
import eu.mikko.intervaltraining.model.Interval
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

    data class IntervalData(val intervalLengthMillis: Long, val isRunningInterval: Boolean)

    data class RunData(private val intervalData: Interval) {
        val intervals: ArrayList<IntervalData> = arrayListOf()
        val totalWorkoutTime: Long = intervalData.totalWorkoutTime * 1000

        init {
            var timeLeft = this.totalWorkoutTime
            intervals.add(IntervalData(intervalData.warmupSeconds, false))
            timeLeft -= intervalData.warmupSeconds

            var isCurrentRunningInterval = true
            while(timeLeft > 0) {
                if(isCurrentRunningInterval) {
                    intervals.add(IntervalData(intervalData.runSeconds * 1000, true))
                } else {
                    intervals.add(IntervalData(intervalData.walkSeconds * 1000, false))
                }

                timeLeft -= intervals.last().intervalLengthMillis
                isCurrentRunningInterval = !isCurrentRunningInterval
            }
        }
    }
}