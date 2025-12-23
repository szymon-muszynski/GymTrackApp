package com.example.gymtrackapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

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
    val weight: Float,
    // Firestore sync
    val remoteId: String = UUID.randomUUID().toString(),
    val updatedAtMs: Long = System.currentTimeMillis(),
    /** 0=SYNCED, 1=PENDING_UPSERT, 2=PENDING_DELETE */
    val syncStatus: Int = 1,
    val deletedAtMs: Long? = null,
)
