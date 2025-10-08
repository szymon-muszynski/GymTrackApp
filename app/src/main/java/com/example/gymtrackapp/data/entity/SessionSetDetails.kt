package com.example.gymtrackapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_set_details")
data class SessionSetDetails(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionExerciseId: Long,
    val order: Int,
    val reps: Int,
    val weight: Float
)
