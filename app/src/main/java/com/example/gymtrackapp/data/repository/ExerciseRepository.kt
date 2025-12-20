package com.example.gymtrackapp.data.repository

import android.content.Context
import com.example.gymtrackapp.data.dao.ExerciseDao
import com.example.gymtrackapp.data.entity.Exercise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(val exerciseDao: ExerciseDao, private val context: Context) {
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
            createdAt = System.currentTimeMillis()
        )

        exerciseDao.insertExercise(exercise)
        return exercise
    }

    suspend fun deleteExercise(exercise: Exercise) {
        exerciseDao.deleteExercise(exercise)
    }

    suspend fun deleteExerciseById(id: String) {
        exerciseDao.deleteExerciseById(id)
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


}