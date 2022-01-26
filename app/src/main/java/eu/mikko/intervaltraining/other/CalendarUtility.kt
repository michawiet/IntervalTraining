package eu.mikko.intervaltraining.other

import ca.antonious.materialdaypicker.MaterialDayPicker
import eu.mikko.intervaltraining.model.TrainingNotification
import java.time.DayOfWeek
import java.util.*

object CalendarUtility {

    private fun materialDayPickerWeekdayToCalendarDayOfWeek(weekday: MaterialDayPicker.Weekday) = when(weekday) {
        MaterialDayPicker.Weekday.MONDAY -> Calendar.MONDAY
        MaterialDayPicker.Weekday.TUESDAY -> Calendar.TUESDAY
        MaterialDayPicker.Weekday.WEDNESDAY -> Calendar.WEDNESDAY
        MaterialDayPicker.Weekday.THURSDAY -> Calendar.THURSDAY
        MaterialDayPicker.Weekday.FRIDAY -> Calendar.FRIDAY
        MaterialDayPicker.Weekday.SATURDAY -> Calendar.SATURDAY
        MaterialDayPicker.Weekday.SUNDAY -> Calendar.SUNDAY
    }

    private fun dayOfWeekToCalendarDayOfWeek(dayOfWeek: DayOfWeek) = when(dayOfWeek) {
        DayOfWeek.MONDAY -> Calendar.MONDAY
        DayOfWeek.TUESDAY -> Calendar.TUESDAY
        DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
        DayOfWeek.THURSDAY -> Calendar.THURSDAY
        DayOfWeek.FRIDAY -> Calendar.FRIDAY
        DayOfWeek.SATURDAY -> Calendar.SATURDAY
        DayOfWeek.SUNDAY -> Calendar.SUNDAY
    }

    fun calendarDayOfWeekToDayOfWeek(dayOfWeek: Int) = when(dayOfWeek) {
        Calendar.MONDAY -> DayOfWeek.MONDAY
        Calendar.TUESDAY -> DayOfWeek.TUESDAY
        Calendar.WEDNESDAY -> DayOfWeek.WEDNESDAY
        Calendar.THURSDAY -> DayOfWeek.THURSDAY
        Calendar.FRIDAY -> DayOfWeek.FRIDAY
        Calendar.SATURDAY -> DayOfWeek.SATURDAY
        Calendar.SUNDAY -> DayOfWeek.SUNDAY
        else -> DayOfWeek.MONDAY
    }

    fun generateCalendar(hour: Int, minute: Int, weekday: MaterialDayPicker.Weekday) =
        generateCalendar(hour, minute, materialDayPickerWeekdayToCalendarDayOfWeek(weekday))

    fun generateCalendar(tn: TrainingNotification) = generateCalendar(tn.hour, tn.minute, DayOfWeek.valueOf(tn.dayOfWeek))

    fun generateCalendar(hour: Int, minute: Int, dayOfWeek: DayOfWeek) =
        generateCalendar(hour, minute, dayOfWeekToCalendarDayOfWeek(dayOfWeek))

    private fun generateCalendar(hour: Int, minute: Int, dayOfWeek: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
        }

        if(calendar.time < Date())
            calendar.add(Calendar.DAY_OF_MONTH, 7)

        return calendar
    }
}