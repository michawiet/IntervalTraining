package eu.mikko.intervaltraining.other

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class SecondsToMinutesAndSecondsValueFormatter : IndexAxisValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        var seconds = value.toLong()

        val minutes = TimeUnit.SECONDS.toMinutes(seconds)
        seconds -= TimeUnit.MINUTES.toSeconds(minutes)

        return String.format("%d:%02d", minutes, seconds)
    }
}