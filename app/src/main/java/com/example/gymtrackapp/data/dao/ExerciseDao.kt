package com.example.gymtrackapp.data.dao

import androidx.room.*
import com.example.gymtrackapp.data.entity.Exercise

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises")
    suspend fun getAllExercises(): List<Exercise>

    @Query("SELECT * FROM exercises WHERE id = :exerciseId LIMIT 1")
    suspend fun getExerciseById(exerciseId: String): Exercise?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<Exercise>)

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
