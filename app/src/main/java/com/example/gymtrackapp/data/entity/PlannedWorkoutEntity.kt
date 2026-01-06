package com.example.gymtrackapp.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Planowany trening (Weekly Planner) trzymany lokalnie w Room.
 *
 * - id: jest jednocześnie ID dokumentu w Firestore.
 * - dateEpochDay: LocalDate.toEpochDay()
 * - updatedAtMs: używane do strategii Last-Write-Wins przy sync.
 * - syncStatus: status offline-first (np. SyncStatus.PENDING_UPSERT).
 * - deletedAtMs: soft delete (tombstone) — null oznacza rekord aktywny.
 */
@Entity(
    tableName = "planned_workouts",
    indices = [Index("dateEpochDay")]
)
data class PlannedWorkoutEntity(
    @PrimaryKey val id: String,
    val dateEpochDay: Long,
    val title: String,
    val updatedAtMs: Long,
    val syncStatus: Int = SyncStatus.PENDING_UPSERT,
    val deletedAtMs: Long? = null,
)
