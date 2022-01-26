package eu.mikko.intervaltraining.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import eu.mikko.intervaltraining.R
import eu.mikko.intervaltraining.model.Run
import eu.mikko.intervaltraining.notifications.ProgressReceiver
import eu.mikko.intervaltraining.other.CalendarUtility
import eu.mikko.intervaltraining.other.CalendarUtility.calendarDayOfWeekToDayOfWeek
import eu.mikko.intervaltraining.other.Constants
import eu.mikko.intervaltraining.other.Constants.KEY_HOUR_PROGRESS_NOTIFICATION
import eu.mikko.intervaltraining.other.Constants.KEY_MINUTE_PROGRESS_NOTIFICATION
import eu.mikko.intervaltraining.other.Constants.KEY_SELECTED_DAY_PROGRESS_NOTIFICATION
import eu.mikko.intervaltraining.other.DistanceValueFormatter
import eu.mikko.intervaltraining.other.MinutesValueFormatter
import eu.mikko.intervaltraining.other.TrackingUtility.getFormattedStopWatchTime
import eu.mikko.intervaltraining.viewmodel.ProgressViewModel
import kotlinx.android.synthetic.main.fragment_stats.*
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private val viewModel: ProgressViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var workoutStep: Int = 1

    //var weekdayProgressNotification = ""
    private var hourProgressNotification: Int = 12
    private var minuteProgressNotification: Int = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCombinedChart()
        setupProgressNotification()

        viewModel.totalDistance.observe(viewLifecycleOwner, {
            if(it != null) {
                tvTotalDistanceCovered.text = String.format("%.1f km", it.toFloat().div(1000f))
            }
        })
        viewModel.totalTimeInMillis.observe(viewLifecycleOwner, {
            if(it != null) {
                tvTotalTimeSpentTrackingActivity.text = getFormattedStopWatchTime(it)
            }
        })
        viewModel.allRuns.observe(viewLifecycleOwner, {
            setDataForCombinedChart(it)
            combinedProgressChart.invalidate()
        })
        viewModel.getMaxWorkoutStep().observe(viewLifecycleOwner, {
            tvGoalProgress.text = "$workoutStep/$it"
        })
    }

    private fun setupProgressNotification() {
        this.hourProgressNotification = sharedPref.getInt(KEY_HOUR_PROGRESS_NOTIFICATION, 12)
        this.minuteProgressNotification = sharedPref.getInt(KEY_MINUTE_PROGRESS_NOTIFICATION, 0)
        val weekdayProgressNotification: String = sharedPref.getString(KEY_SELECTED_DAY_PROGRESS_NOTIFICATION, "")!!
        tvProgressNotificationTime.text = String.format("%02d:%02d", hourProgressNotification, minuteProgressNotification)

        if(weekdayProgressNotification != "") {
            materialDayPickerProgressNotification.selectDay(
                MaterialDayPicker.Weekday.valueOf(
                    weekdayProgressNotification
                )
            )
            tvProgressNotificationTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_500))
        }

        tvProgressNotificationTime.setOnClickListener {
            TimePickerDialog(requireContext(),
                //timeSetListener
                { _, hourOfDay, minute ->
                    //update text view
                    tvProgressNotificationTime.text = String.format("%02d:%02d", hourOfDay, minute)
                    hourProgressNotification = hourOfDay
                    minuteProgressNotification = minute
                    sharedPref.edit()
                        .putInt(KEY_HOUR_PROGRESS_NOTIFICATION, hourOfDay)
                        .putInt(KEY_MINUTE_PROGRESS_NOTIFICATION, minute)
                        .apply()
                    //if any day is selected set alarm
                    if(materialDayPickerProgressNotification.selectedDays.isNotEmpty()) {
                        setProgressNotificationAlarm(CalendarUtility.generateCalendar(
                            hourOfDay,
                            minute,
                            materialDayPickerProgressNotification.selectedDays.first()
                        ), getProgressNotificationPendingIntent())
                    }
                },
                hourProgressNotification,
                minuteProgressNotification,
                true
            ).show()
        }
        materialDayPickerProgressNotification.setDayPressedListener { weekday, isSelected ->
            val pendingIntent = getProgressNotificationPendingIntent()
            if(isSelected) {
                tvProgressNotificationTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_500))
                //update the changes in shared pref
                sharedPref.edit()
                    .putString(KEY_SELECTED_DAY_PROGRESS_NOTIFICATION, weekday.toString())
                    .apply()
                //update pending intent
                val c = CalendarUtility.generateCalendar(hourProgressNotification, minuteProgressNotification, weekday)
                setProgressNotificationAlarm(c, pendingIntent)
            } else {
                tvProgressNotificationTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.black_inactive))
                sharedPref.edit()
                    .putString(KEY_SELECTED_DAY_PROGRESS_NOTIFICATION, "")
                    .apply()
                //cancel this day
                val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
            }
            weekday.toString()
        }
    }

    private fun setProgressNotificationAlarm(c: Calendar, pendingIntent: PendingIntent) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pendingIntent)
        //display snackbar
        val dayOfWeek = calendarDayOfWeekToDayOfWeek(c[Calendar.DAY_OF_WEEK]).getDisplayName(TextStyle.FULL, ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0])
        Snackbar.make(requireActivity().findViewById(R.id.rootView),
            String.format("Progress reminder set - %s, %02d:%02d", dayOfWeek, c[Calendar.HOUR_OF_DAY], c[Calendar.MINUTE]),
            Snackbar.LENGTH_LONG
        ).setAnchorView(R.id.bottom_navigation).show()
    }

    private fun getProgressNotificationPendingIntent(): PendingIntent {
        val intent = Intent(requireContext(), ProgressReceiver::class.java)
        intent.action = Intent.ACTION_DEFAULT

        return PendingIntent.getBroadcast(
            requireContext(),
            Constants.PROGRESS_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    //TODO "use data provided with the list"
    private fun setDataForCombinedChart(list: List<Run>) {
        val mockList = arrayListOf<Run>()
        for(i in 0 .. 10) {
            mockList.add(Run(0,
                Random.nextInt(1, 27).div(9f),
                Random.nextInt(30, 50).times(100),
                Random.nextLong(25L, 30L).times(60L).times(1000L),
                Random.nextInt(70, 100)))
        }

        val distances = arrayListOf<Entry>()
        val activityTypeTimes = arrayListOf<BarEntry>()

        var i = 1f
        for(run in mockList) {
            distances.add(Entry(i, run.distanceInMeters.div(1000f)))
            activityTypeTimes.add(BarEntry(i, floatArrayOf(5f, 25f)))

            i += 1f
        }

        val distanceDataSet = LineDataSet(distances, "Distance").apply {
            setDrawValues(true)
            color = Color.parseColor("#426FC0")
            lineWidth = 4f
            axisDependency = YAxis.AxisDependency.LEFT
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.2f
            circleRadius = 2.0f
            valueTextSize = 10f
            setDrawValues(false)
            setCircleColor(Color.parseColor("#426FC0"))
            circleHoleColor = Color.parseColor("#9DADDA")
        }

        val activityTypeTimesDataSet = BarDataSet(activityTypeTimes, "").apply {
            stackLabels = arrayOf("Walk length", "Run length")
            colors = listOf(Color.parseColor("#CBCBCB"), Color.parseColor("#FFD891"))
            axisDependency = YAxis.AxisDependency.RIGHT
            barBorderColor = Color.parseColor("#A1A1A1")
            barBorderWidth = 2f
            valueTextSize = 12f
            valueFormatter = MinutesValueFormatter()
        }

        val lineData = LineData(distanceDataSet)
        val barData = BarData(activityTypeTimesDataSet)
        val data = CombinedData().apply {
            setData(lineData)
            setData(barData)
            barData.barWidth = 0.5f
        }

        combinedProgressChart.apply {
            setData(data)
            xAxis.axisMaximum = mockList.size.plus(0.5f)
        }
    }

    private fun setupCombinedChart() {
        combinedProgressChart.apply {
            isScaleYEnabled = false
            xAxis.apply {
                axisMinimum = 0.4f
                setDrawLabels(false)
                setDrawGridLines(false)
            }
            axisLeft.apply {
                setDrawGridLines(true)
                setDrawLabels(true)
                valueFormatter = DistanceValueFormatter()
                axisMinimum = 0f
                axisMaximum = 8f
            }
            axisRight.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
                axisMinimum = 0f
            }
            description.text = ""
            legend.apply {
                textSize = 15f
                form = Legend.LegendForm.CIRCLE
                isWordWrapEnabled = true
            }
        }
    }
}