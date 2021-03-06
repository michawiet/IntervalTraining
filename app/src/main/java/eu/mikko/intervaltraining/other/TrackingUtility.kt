package eu.mikko.intervaltraining.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import eu.mikko.intervaltraining.model.Interval
import eu.mikko.intervaltraining.other.Constants.RUN_PACE
import eu.mikko.intervaltraining.other.Constants.WALK_PACE
import eu.mikko.intervaltraining.services.IntervalPathPoints
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

    fun getFormattedStopWatchTimeWithMillis(ms: Long): String {
        var millis = ms

        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        millis -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
        millis -= TimeUnit.SECONDS.toMillis(seconds)

        return String.format("%02d:%02d:%01d", minutes, seconds, millis.div(100))
    }

    fun getFormattedTimeFromSeconds(s: Long): String {
        var seconds = s

        val minutes = TimeUnit.SECONDS.toMinutes(seconds)
        seconds -= TimeUnit.MINUTES.toSeconds(minutes)

        return String.format("%02d:%02d", minutes, seconds)
    }

    data class RunData(private val intervalData: ParcelableInterval) {
        data class IntervalData(val startMillis: Long, val lengthMillis: Long, val isRunningInterval: Boolean)

        val intervals: ArrayList<IntervalData> = arrayListOf()
        val totalWorkoutTime: Long = intervalData.totalWorkoutTime * 1000

        init {
            var timeLeft = this.totalWorkoutTime
            intervals.add(IntervalData(0L, intervalData.warmupSeconds * 1000, false))
            timeLeft -= intervals.last().lengthMillis

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

    private fun getIntervalDistance(interval: IntervalPathPoints): Float {
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

    private fun rateInterval(avgSpeed: Float, isRunningInterval: Boolean): Int {
        val rating: Int = if(isRunningInterval) {
            ((-1 * (avgSpeed - RUN_PACE).pow(4) + 1) * 100).toInt()
        } else {
            ((-1 * ((avgSpeed - WALK_PACE) * 2).pow(4) + 1) * 100).toInt()
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

    fun rateIntervals(intervalData: Interval, intervals: MutableList<IntervalPathPoints>): Array<ParcelableRunIntervalResult> {
        val ratingArray = arrayListOf<ParcelableRunIntervalResult>()
        var isRunningInterval = true

        //warmup
        val tmpSpeed = getIntervalDistance(intervals[0]) / intervalData.warmupSeconds
        val tmpResult = rateInterval(tmpSpeed, false)
        ratingArray.add(ParcelableRunIntervalResult(tmpSpeed, tmpResult, false))

        for(i in 1 until intervals.size) {
            val speed = getIntervalDistance(intervals[i]) / if(isRunningInterval) intervalData.runSeconds else intervalData.walkSeconds
            ratingArray.add(ParcelableRunIntervalResult(speed, rateInterval(speed, isRunningInterval), isRunningInterval))
            isRunningInterval = !isRunningInterval
        }

        return ratingArray.toTypedArray()
    }

    fun getMinutesPerKilometerFromMetersPerSecond(speed: Float): String {
        return try {
            val pace = 1000 / (speed * 60)
            val leftover = pace % 1
            var minutes = (pace - leftover).roundToInt()
            var seconds = (leftover * 60).roundToInt()
            if(seconds == 60) {
                minutes += 1
                seconds = 0
            }
            String.format("%02d:%02d", minutes, seconds)
        } catch (e: IllegalArgumentException) {
            "--:--"
        }
    }

    //walkTime[0]
    fun getTotalActivityTimeFromInterval(interval: Interval): FloatArray {
        var totalRunTime = 0L
        var totalWalkTime = interval.warmupSeconds
        var timeLeft = interval.totalWorkoutTime - totalWalkTime

        var isRunning = true
        while(timeLeft > 0) {
            if(isRunning) {
                totalRunTime += if(interval.runSeconds > timeLeft) timeLeft else interval.runSeconds
                timeLeft -= interval.runSeconds
            } else {
                totalWalkTime += if (interval.walkSeconds > timeLeft) timeLeft else interval.walkSeconds
                timeLeft -= interval.walkSeconds
            }
            isRunning = !isRunning
        }

        return floatArrayOf(totalWalkTime.toFloat(), totalRunTime.toFloat())
    }
}