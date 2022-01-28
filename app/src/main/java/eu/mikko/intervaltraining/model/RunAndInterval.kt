package eu.mikko.intervaltraining.model

import androidx.room.Embedded
import androidx.room.Relation

data class RunAndInterval(
    @Embedded val run: Run,
    @Relation(
        parentColumn = "workoutLevel",
        entityColumn = "workoutLevel"
    )
    val interval: Interval
)