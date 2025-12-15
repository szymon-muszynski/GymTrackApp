package com.example.gymtrackapp.data.repository

import android.content.Context
import com.example.gymtrackapp.data.dao.TemplateDao
import com.example.gymtrackapp.data.dao.TrainingDao
import com.example.gymtrackapp.data.entity.SessionExercise
import com.example.gymtrackapp.data.entity.SessionSetDetails
import com.example.gymtrackapp.data.entity.TrainingSession

class TrainingRepository(
    private val trainingDao: TrainingDao,
    private val context: Context,
    private val templateDao: TemplateDao
) {
    suspend fun loadSessionsForDate(timestamp: Long): List<TrainingSession> {
        return trainingDao.getSessionsForDate(timestamp)
    }

    suspend fun createEmptySession(session: TrainingSession): Long {
        return trainingDao.createEmptySession(session)
    }

    suspend fun deleteSession(session: TrainingSession) {
        trainingDao.deleteSession(session)
    }

    suspend fun updateSession(session: TrainingSession) {
        trainingDao.updateSession(session)
    }

    suspend fun getExercisesForSession(sessionId: Long): List<SessionExercise> {
        return trainingDao.getExercisesForSession(sessionId)
    }

    suspend fun addExerciseToSession(sessionId: Long, exerciseId: String) {
        val maxOrder = trainingDao.getMaxOrderForSession(sessionId) ?: -1
        val newExercise = SessionExercise(
            trainingSessionId = sessionId,
            exerciseId = exerciseId,
            order = maxOrder + 1
        )
        trainingDao.insertSessionExercise(newExercise)
    }

    suspend fun deleteSessionExercise(exercise: SessionExercise) {
        trainingDao.deleteSessionExercise(exercise)
        trainingDao.reorderAfterDeletion(exercise.trainingSessionId, exercise.order)
    }

    suspend fun getSetsForSessionExercise(sessionExerciseId: Long): List<SessionSetDetails> {
        return trainingDao.getSetsForSessionExercise(sessionExerciseId)
    }

    suspend fun addSetToSessionExercise(sessionExerciseId: Long, weight: Float, reps: Int) {
        val maxOrder = trainingDao.getMaxOrderForSessionExercise(sessionExerciseId) ?: -1
        val newSet = SessionSetDetails(
            sessionExerciseId = sessionExerciseId,
            order = maxOrder + 1,
            reps = reps,
            weight = weight
        )
        trainingDao.insertSet(newSet)
    }

    suspend fun deleteSet(set: SessionSetDetails) {
        trainingDao.deleteSet(set)
        trainingDao.reorderSetsAfterDeletion(set.sessionExerciseId, set.order)
    }

    suspend fun getRecentSessionsWithExercises(limit: Int): List<com.example.gymtrackapp.data.entity.RecentSessionWithExercises> {
        val recentSessions = trainingDao.getRecentSessions(limit)

        // Wczytaj i sparsuj exercises.json tylko raz, zbuduj mapę ID -> nazwa
        val exerciseIdToName: Map<String, String> = try {
            val exerciseJson = context.assets.open("exercises.json")
                .bufferedReader()
                .use { it.readText() }
            val jsonArray = org.json.JSONArray(exerciseJson)
            val map = mutableMapOf<String, String>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                map[id] = name
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }

        return recentSessions.map { session ->
            val exercises = trainingDao.getExercisesForSession(session.id.toLong())
            val exerciseNames = exercises.mapNotNull { sessionExercise ->
                exerciseIdToName[sessionExercise.exerciseId]
            }

            com.example.gymtrackapp.data.entity.RecentSessionWithExercises(
                sessionId = session.id.toLong(),
                sessionDate = session.date,
                sessionDescription = session.description,
                exerciseNames = exerciseNames
            )
        }
    }

    suspend fun createSessionFromTemplate(
        templateId: Long,
        date: Long,
        description: String
    ): Long {
        // 1. Utwórz sesję
        val sessionId = trainingDao.createEmptySession(
            TrainingSession(
                date = date,
                description = description
            )
        )

        // 2. Pobierz ćwiczenia z szablonu
        val templateExercises = templateDao.getExercisesForTemplate(templateId)

        // 3. Wstaw SessionExercise na podstawie TemplateExercise
        templateExercises.forEach { templateExercise ->
            trainingDao.insertSessionExercise(
                SessionExercise(
                    trainingSessionId = sessionId,
                    exerciseId = templateExercise.exerciseId,
                    order = templateExercise.order
                )
            )
        }

        return sessionId
    }
}