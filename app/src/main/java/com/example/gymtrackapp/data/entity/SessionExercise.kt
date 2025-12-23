package com.example.gymtrackapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "session_exercises",
    foreignKeys = [
        ForeignKey(
            entity = TrainingSession::class,
            parentColumns = ["id"],
            childColumns = ["trainingSessionId"],
            onDelete = ForeignKey.CASCADE // <--- TO JEST KLUCZOWE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("trainingSessionId"),
        Index("exerciseId")
    ]
)
data class SessionExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trainingSessionId: Long,
    val exerciseId: String,
    val order: Int,
    // Firestore sync
    val remoteId: String = UUID.randomUUID().toString(),
    val updatedAtMs: Long = System.currentTimeMillis(),
    /** 0=SYNCED, 1=PENDING_UPSERT, 2=PENDING_DELETE */
    val syncStatus: Int = 1,
    val deletedAtMs: Long? = null,
)
