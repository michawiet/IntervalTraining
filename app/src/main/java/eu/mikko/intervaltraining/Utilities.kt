package eu.mikko.intervaltraining

import eu.mikko.intervaltraining.model.TrainingNotification
import java.time.DayOfWeek
import java.util.*

class Utilities {
    companion object {
        fun generateCalendar(tn: TrainingNotification): Calendar {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, tn.hour)
            calendar.set(Calendar.MINUTE, tn.minute)
            calendar.set(Calendar.SECOND, 0)

            calendar.set(Calendar.DAY_OF_WEEK, when(DayOfWeek.valueOf(tn.dayOfWeek)) {
                DayOfWeek.MONDAY -> Calendar.MONDAY
                DayOfWeek.TUESDAY -> Calendar.TUESDAY
                DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
                DayOfWeek.THURSDAY -> Calendar.THURSDAY
                DayOfWeek.FRIDAY -> Calendar.FRIDAY
                DayOfWeek.SATURDAY -> Calendar.SATURDAY
                DayOfWeek.SUNDAY -> Calendar.SUNDAY
            })

            if(calendar.time < Date())
                calendar.add(Calendar.DAY_OF_MONTH, 1)

            return calendar
        }
    }
}