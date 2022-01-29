package eu.mikko.intervaltraining.other

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class PaceLabelFormatter : IndexAxisValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return TrackingUtility.getMinutesPerKilometerFromMetersPerSecond(value)
    }
}