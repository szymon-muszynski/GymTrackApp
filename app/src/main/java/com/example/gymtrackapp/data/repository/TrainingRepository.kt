package com.example.gymtrackapp.data.repository

import android.content.Context
import com.example.gymtrackapp.data.dao.TrainingDao
import com.example.gymtrackapp.data.entity.SessionExercise
import com.example.gymtrackapp.data.entity.TrainingSession

class TrainingRepository(
    private val trainingDao: TrainingDao,
    private val context: Context
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
}