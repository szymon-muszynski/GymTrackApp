package com.example.gymtrackapp.data.repository

import android.content.Context
import com.example.gymtrackapp.data.dao.ExerciseDao
import com.example.gymtrackapp.data.entity.Exercise
import com.example.gymtrackapp.data.entity.ExerciseSyncStatus
import com.example.gymtrackapp.data.sync.ExerciseSyncScheduler
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val context: Context
) {
    suspend fun loadExercisesFromAssets() {
        // Otwieramy plik z assets
        val json = context.assets.open("exercises.json").bufferedReader().use { it.readText() }

        // Parsowanie JSON do listy Exercise
        val type = object : TypeToken<List<Exercise>>() {}.type
        val exercises: List<Exercise> = Gson().fromJson(json, type)

        // Zapis do Room
        if (exerciseDao.getAllExercises().isEmpty()) {
            exerciseDao.insertAll(exercises)
        }
    }

    suspend fun getAllExercises() = exerciseDao.getAllExercises()

    fun observeCustomExercises(): Flow<List<Exercise>> = exerciseDao.observeCustomExercises()

    fun observeAllExercises(): Flow<List<Exercise>> = exerciseDao.observeAllExercises()

    suspend fun getExerciseById(id: String): Exercise? = exerciseDao.getExerciseById(id)

    suspend fun createCustomExercise(
        name: String,
        level: String,
        category: String,
        equipment: String?,
        primaryMuscles: List<String>,
        secondaryMuscles: List<String>,
        instructions: List<String>,
        force: String?,
        mechanic: String?,
        createdByUserId: String?
    ): Exercise {
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "Nazwa ćwiczenia nie może być pusta" }

        val now = System.currentTimeMillis()

        val exercise = Exercise(
            id = "custom_${UUID.randomUUID()}",
            name = trimmedName,
            force = force?.takeIf { it.isNotBlank() },
            level = level.trim().ifBlank { "beginner" },
            mechanic = mechanic?.takeIf { it.isNotBlank() },
            equipment = equipment?.takeIf { it.isNotBlank() },
            primaryMuscles = primaryMuscles.map { it.trim() }.filter { it.isNotEmpty() },
            secondaryMuscles = secondaryMuscles.map { it.trim() }.filter { it.isNotEmpty() },
            instructions = instructions.map { it.trim() }.filter { it.isNotEmpty() },
            category = category.trim().ifBlank { "strength" },
            images = emptyList(),
            isCustom = true,
            createdByUserId = createdByUserId,
            createdAt = now,
            syncStatus = ExerciseSyncStatus.PENDING_UPSERT,
            updatedAtMs = now,
            deletedAtMs = null
        )

        exerciseDao.insertExercise(exercise)
        ExerciseSyncScheduler.enqueue(context.applicationContext)
        return exercise
    }

    suspend fun updateCustomExercise(exercise: Exercise): Exercise {
        require(exercise.isCustom) { "updateCustomExercise można wywołać tylko dla isCustom=true" }

        val now = System.currentTimeMillis()
        val updated = exercise.copy(
            updatedAtMs = now,
            syncStatus = ExerciseSyncStatus.PENDING_UPSERT
        )

        exerciseDao.updateExercise(updated)
        ExerciseSyncScheduler.enqueue(context.applicationContext)
        return updated
    }

    /** Soft delete + sync do chmury (dla custom). */
    suspend fun deleteExercise(exercise: Exercise) {
        if (!exercise.isCustom) return

        val now = System.currentTimeMillis()
        exerciseDao.softDeleteExerciseById(
            id = exercise.id,
            deletedAtMs = now,
            updatedAtMs = now,
            syncStatus = ExerciseSyncStatus.PENDING_DELETE
        )
        ExerciseSyncScheduler.enqueue(context.applicationContext)
    }

    /** Soft delete + sync do chmury (dla custom). */
    suspend fun deleteExerciseById(id: String) {
        val existing = exerciseDao.getExerciseById(id) ?: return
        deleteExercise(existing)
    }

    suspend fun getExercisesForMuscle(muscle: String): List<Exercise> {
        return exerciseDao.getAllExercises().filter { it.primaryMuscles.contains(muscle) }
    }

    suspend fun getExercisesForMuscleIncludingSecondary(muscle: String): List<Exercise> {
        return exerciseDao.getAllExercises().filter {
            it.primaryMuscles.contains(muscle) || it.secondaryMuscles.contains(muscle)
        }
    }

    suspend fun getAllPrimaryMuscles(): List<String> {
        return exerciseDao.getAllExercises()
            .flatMap { it.primaryMuscles }
            .distinct()
            .sorted()
    }

    suspend fun getAllSecondaryMuscles(): List<String> {
        return exerciseDao.getAllExercises()
            .flatMap { it.secondaryMuscles }
            .distinct()
            .sorted()
    }

    suspend fun getAllLevels(): List<String> = exerciseDao.getAllLevels()

    suspend fun getAllEquipments(): List<String> = exerciseDao.getAllEquipments()

    suspend fun getAllCategories(): List<String> = exerciseDao.getAllCategories()

    suspend fun getAllMechanics(): List<String> = exerciseDao.getAllMechanics()

    suspend fun getAllForces(): List<String> = exerciseDao.getAllForces()
}