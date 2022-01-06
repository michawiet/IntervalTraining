package eu.mikko.intervaltraining.data

import java.time.DayOfWeek

data class DayOfWeekRecyclerViewItem(val dayOfWeekLocalName: String,
                                     val notificationTime: String,
                                     val isNotificationEnabled: Boolean,
                                     val dayOfWeek: DayOfWeek)
