package com.example.gymtrackapp.data.repository

import android.content.Context
import com.example.gymtrackapp.data.dao.ExerciseDao
import com.example.gymtrackapp.data.entity.Exercise
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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