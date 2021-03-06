package eu.mikko.intervaltraining.other

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlin.math.roundToInt

class DistanceValueFormatter : IndexAxisValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return String.format("${value.roundToInt()} km")
    }
}