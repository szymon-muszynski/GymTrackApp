package com.example.gymtrackapp.data.dao

import androidx.room.*
import com.example.gymtrackapp.data.entity.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    // --- observe all exercises (seed + custom) ---
    @Query("SELECT * FROM exercises ORDER BY name COLLATE NOCASE ASC")
    fun observeAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<Exercise>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getExerciseById(id: String): Exercise?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<Exercise>)

    // --- custom exercises ---
    @Query("SELECT * FROM exercises WHERE isCustom = 1 ORDER BY name COLLATE NOCASE ASC")
    fun observeCustomExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExerciseById(id: String)

    // 🔹 Te są proste, bo to pola typu String
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
