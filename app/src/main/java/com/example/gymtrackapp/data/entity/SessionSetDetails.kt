package com.example.gymtrackapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_set_details",
    foreignKeys = [
        ForeignKey(
            entity = SessionExercise::class,
            parentColumns = ["id"],
            childColumns = ["sessionExerciseId"],
            onDelete = ForeignKey.CASCADE // <--- TO JEST KLUCZOWE
        )
    ],
    indices = [Index("sessionExerciseId")]
)
data class SessionSetDetails(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionExerciseId: Long,
    val order: Int,
    val reps: Int,
    val weight: Float
)
