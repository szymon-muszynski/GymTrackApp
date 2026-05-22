package com.example.gymtrackapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.gymtrackapp.data.converters.Converters


@Entity(tableName = "exercises")
@TypeConverters(Converters::class)
data class Exercise(
    @PrimaryKey val id: String,
    val name: String,
    val force: String?,           // static / pull / push / null
    val level: String,            // beginner / intermediate / expert
    val mechanic: String?,        // isolation / compound / null
    val equipment: String?,       // dumbbell, barbell, etc. (can be null)
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val instructions: List<String>,
    val category: String,         // strength, cardio, etc.
    val images: List<String>,

    // --- Custom exercises metadata ---
    // Seedowane ćwiczenia z assets mają isCustom=false i createdByUserId=null
    val isCustom: Boolean = false,
    val createdByUserId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),

    // --- Sync metadata (offline-first) ---
    // 0=SYNCED, 1=PENDING_UPSERT, 2=PENDING_DELETE
    val syncStatus: Int = SyncStatus.SYNCED,
    val updatedAtMs: Long = createdAt,
    val deletedAtMs: Long? = null,
)

/**
 * Alias dla wstecznej kompatybilności ze starszym kodem.
 * Docelowo możemy stopniowo przejść na używanie SyncStatus bezpośrednio.
 */
object ExerciseSyncStatus {
    const val SYNCED = SyncStatus.SYNCED
    const val PENDING_UPSERT = SyncStatus.PENDING_UPSERT
    const val PENDING_DELETE = SyncStatus.PENDING_DELETE
}
