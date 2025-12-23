package com.example.gymtrackapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "training_sessions")
data class TrainingSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val description: String,
    // Firestore sync
    val remoteId: String = UUID.randomUUID().toString(),
    val updatedAtMs: Long = System.currentTimeMillis(),
    /** 0=SYNCED, 1=PENDING_UPSERT, 2=PENDING_DELETE */
    val syncStatus: Int = 1,
    val deletedAtMs: Long? = null,
)