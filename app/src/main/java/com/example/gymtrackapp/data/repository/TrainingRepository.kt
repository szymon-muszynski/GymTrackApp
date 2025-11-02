package com.example.gymtrackapp.data.repository

import android.content.Context
import com.example.gymtrackapp.data.dao.TrainingDao
import com.example.gymtrackapp.data.entity.SessionExercise
import com.example.gymtrackapp.data.entity.SessionSetDetails
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

}