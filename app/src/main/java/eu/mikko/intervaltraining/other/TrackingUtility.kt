package eu.mikko.intervaltraining.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Parcelable
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.model.Interval
import eu.mikko.intervaltraining.services.IntervalPathPoints
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_run.*
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.roundToInt

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

    fun getTotalDistance(intervals: MutableList<IntervalPathPoints>): Float {
        var totalDistance = 0f

        for (interval in intervals) {
            totalDistance += getIntervalDistance(interval)
        }

        return totalDistance
    }

    fun getIntervalDistance(interval: IntervalPathPoints): Float {
        var distance = 0f
        for(chunk in interval) {
            //for loop with check of size
            for(i in 0..chunk.size - 2) {
                val pos1 = chunk[i]
                val pos2 = chunk[i + 1]
                val result = FloatArray(1)

                Location.distanceBetween(
                    pos1.latitude,
                    pos1.longitude,
                    pos2.latitude,
                    pos2.longitude,
                    result
                )

                distance += result[0]
            }
        }
        return distance
    }

    fun rateInterval(avgSpeed: Float, isRunningInterval: Boolean): Int {
        var rating: Int = if(isRunningInterval) {
            ((-1 * ((avgSpeed - 3) * 1.2).pow(4) + 1) * 100).toInt()
        } else {
            ((-1 * ((avgSpeed - 1.3) * 1.7).pow(4) + 1) * 100).toInt()
        }

        return if(rating > 0) rating else 0
    }

    fun rateWorkout(intervalData: Interval, intervals: MutableList<IntervalPathPoints>): Int {
        var sum = 0
        var count = 0
        var isRunningInterval = true

        //warmup
        sum += rateInterval(getIntervalDistance(intervals[0]) / intervalData.warmupSeconds, false)
        count++


        for(i in 1 until intervals.size) {
            val speed = getIntervalDistance(intervals[i]) / if(isRunningInterval) intervalData.runSeconds else intervalData.walkSeconds
            sum += rateInterval(speed, isRunningInterval)
            count++
            isRunningInterval = !isRunningInterval
        }

        return sum / count
    }

    fun getKilometersPerMinuteFromMetersPerSecond(speed: Float): String {
        return try {
            val pace = 1000 / (speed * 60)
            val leftover = pace % 1
            val minutes = (pace - leftover).roundToInt()
            val seconds = (leftover * 60).roundToInt()
            String.format("%02d:%02d", minutes, seconds)
        } catch (e: IllegalArgumentException) {
            "--:--"
        }
    }
}