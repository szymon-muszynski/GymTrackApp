package com.example.gymtrackapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_sessions")
data class TrainingSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val description: String,
)
