package eu.mikko.intervaltraining.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intervals_table")
data class Interval (
    @PrimaryKey(autoGenerate = false)
    val workoutLevel: Int,
    val warmupSeconds: Long,
    val runSeconds: Long,
    val walkSeconds: Long,
    val totalWorkoutTime: Long)