package com.example.gymtrackapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_exercises")
data class SessionExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trainingSessionId: Long,
    val exerciseId: String,
    val order: Int
)
