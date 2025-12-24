package com.example.gymtrackapp.data.dao

import androidx.room.*
import com.example.gymtrackapp.data.entity.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    // --- observe all exercises (seed + custom) ---
    @Query("SELECT * FROM exercises WHERE deletedAtMs IS NULL ORDER BY name COLLATE NOCASE ASC")
    fun observeAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<Exercise>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getExerciseById(id: String): Exercise?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<Exercise>)

    // --- custom exercises ---
    @Query("SELECT * FROM exercises WHERE isCustom = 1 AND deletedAtMs IS NULL ORDER BY name COLLATE NOCASE ASC")
    fun observeCustomExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercise(exercise: Exercise)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    /** Soft delete (custom exercises). */
    @Query("UPDATE exercises SET deletedAtMs = :deletedAtMs, updatedAtMs = :updatedAtMs, syncStatus = :syncStatus WHERE id = :id")
    suspend fun softDeleteExerciseById(
        id: String,
        deletedAtMs: Long,
        updatedAtMs: Long,
        syncStatus: Int
    )

    // ---- Sync queue helpers ----

    @Query(
        """
        SELECT * FROM exercises
        WHERE isCustom = 1 AND syncStatus != 0
        ORDER BY updatedAtMs ASC
        """
    )
    suspend fun getPendingCustomExercises(): List<Exercise>

    @Query("UPDATE exercises SET syncStatus = 0 WHERE id = :id")
    suspend fun markExerciseSynced(id: String)

    @Query("UPDATE exercises SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun setExerciseSyncStatus(id: String, syncStatus: Int)

    /** Czyści tylko customowe ćwiczenia (seedowane zostają w DB). */
    @Query("DELETE FROM exercises WHERE isCustom = 1")
    suspend fun clearCustomExercises()

    /** Na pull-u: upsert custom exercise (REPLACE, bo dokument ID jest stabilny). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercise(exercise: Exercise)

    //  Te s proste, bo to pola typu String
    @Query("SELECT DISTINCT level FROM exercises")
    suspend fun getAllLevels(): List<String>

    @Query("SELECT DISTINCT equipment FROM exercises WHERE equipment IS NOT NULL")
    suspend fun getAllEquipments(): List<String>

    @Query("SELECT DISTINCT category FROM exercises")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT DISTINCT mechanic FROM exercises WHERE mechanic IS NOT NULL")
    suspend fun getAllMechanics(): List<String>

    @Query("SELECT DISTINCT force FROM exercises WHERE force IS NOT NULL")
    suspend fun getAllForces(): List<String>
}
