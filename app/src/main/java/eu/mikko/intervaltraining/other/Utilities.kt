package eu.mikko.intervaltraining.other

import eu.mikko.intervaltraining.model.TrainingNotification
import java.time.DayOfWeek
import java.util.*

object Utilities {
    fun generateCalendar(tn: TrainingNotification) = generateCalendar(tn.hour, tn.minute, DayOfWeek.valueOf(tn.dayOfWeek))

    fun generateCalendar(hour: Int, minute: Int, dayOfWeek: DayOfWeek): Calendar {
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.DAY_OF_WEEK, when(dayOfWeek) {
                DayOfWeek.MONDAY -> Calendar.MONDAY
                DayOfWeek.TUESDAY -> Calendar.TUESDAY
                DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
                DayOfWeek.THURSDAY -> Calendar.THURSDAY
                DayOfWeek.FRIDAY -> Calendar.FRIDAY
                DayOfWeek.SATURDAY -> Calendar.SATURDAY
                DayOfWeek.SUNDAY -> Calendar.SUNDAY
            })
        }

        if(calendar.time < Date())
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        return calendar
    }
}