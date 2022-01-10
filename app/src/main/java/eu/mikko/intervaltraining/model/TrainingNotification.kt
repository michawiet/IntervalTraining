package eu.mikko.intervaltraining.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "training_notifications_table", indices = [Index(value = ["dayOfWeek"], unique = true)])
data class TrainingNotification(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val dayOfWeek: String,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean)