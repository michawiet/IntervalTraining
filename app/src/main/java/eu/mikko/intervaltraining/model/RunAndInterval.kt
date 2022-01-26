package eu.mikko.intervaltraining.model

import androidx.room.Embedded
import androidx.room.Relation

data class RunAndInterval(
    @Embedded val run: Run,
    @Relation(
        parentColumn = "workoutStep",
        entityColumn = "workoutStep"
    )
    val interval: Interval
)