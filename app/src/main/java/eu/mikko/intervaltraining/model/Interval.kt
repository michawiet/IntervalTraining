package eu.mikko.intervaltraining.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intervals_table")
data class Interval (
    @PrimaryKey(autoGenerate = false)
    val workoutStep: Int,
    val warmupSeconds: Int,
    val runSeconds: Int,
    val walkSeconds: Int,
    val totalWorkoutTime: Int)