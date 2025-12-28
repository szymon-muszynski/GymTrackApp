package com.example.gymtrackapp.data.repository

import android.content.Context
import com.example.gymtrackapp.data.dao.ExerciseDao
import com.example.gymtrackapp.data.dao.TemplateDao
import com.example.gymtrackapp.data.dao.TrainingDao
import com.example.gymtrackapp.data.entity.RecentSessionWithExercises
import com.example.gymtrackapp.data.entity.SessionExercise
import com.example.gymtrackapp.data.entity.SessionSetDetails
import com.example.gymtrackapp.data.entity.TrainingSession
import com.example.gymtrackapp.data.sync.TrainingSyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TrainingRepository(
    private val trainingDao: TrainingDao,
    private val context: Context,
    private val templateDao: TemplateDao,
    private val exerciseDao: ExerciseDao
) {
    suspend fun loadSessionsForDate(timestamp: Long): List<TrainingSession> =
        trainingDao.getSessionsForDate(timestamp)

    fun observeSessionsForDate(timestamp: Long): Flow<List<TrainingSession>> =
        trainingDao.observeSessionsForDate(timestamp)

    suspend fun createEmptySession(date: Long, description: String): Long {
        val sessionId = trainingDao.createEmptySession(
            TrainingSession(
                date = date,
                description = description
            )
        )
        TrainingSyncScheduler.enqueue(context)
        return sessionId
    }

    suspend fun updateSession(session: TrainingSession) {
        trainingDao.updateSession(
            session.copy(
                updatedAtMs = System.currentTimeMillis(),
                syncStatus = 1
            )
        )
        TrainingSyncScheduler.enqueue(context)
    }

    /** Soft delete kaskadowy: sesja + ćwiczenia + serie. */
    suspend fun deleteSession(session: TrainingSession) {
        val now = System.currentTimeMillis()
        // DAO robi soft delete i ustawia syncStatus=2
        trainingDao.softDeleteSetsForSession(session.id, deletedAtMs = now, updatedAtMs = now)
        trainingDao.softDeleteExercisesForSession(session.id, deletedAtMs = now, updatedAtMs = now)
        trainingDao.softDeleteSession(session.id, deletedAtMs = now, updatedAtMs = now)
        TrainingSyncScheduler.enqueue(context)
    }

    suspend fun getExercisesForSession(sessionId: Long): List<SessionExercise> =
        trainingDao.getExercisesForSession(sessionId)

    suspend fun addExerciseToSession(sessionId: Long, exerciseId: String) {
        val maxOrder = trainingDao.getMaxOrderForSession(sessionId) ?: -1
        val newExercise = SessionExercise(
            trainingSessionId = sessionId,
            exerciseId = exerciseId,
            order = maxOrder + 1
        )
        trainingDao.insertSessionExercise(newExercise)
        TrainingSyncScheduler.enqueue(context)
    }

    suspend fun updateSessionExercise(exercise: SessionExercise) {
        trainingDao.updateSessionExercise(
            exercise.copy(
                updatedAtMs = System.currentTimeMillis(),
                syncStatus = 1
            )
        )
        TrainingSyncScheduler.enqueue(context)
    }

    suspend fun deleteSessionExercise(exercise: SessionExercise) {
        val now = System.currentTimeMillis()
        // soft delete ćwiczenia
        trainingDao.softDeleteSessionExercise(exercise.id, deletedAtMs = now, updatedAtMs = now)
        // reordering tylko na aktywnych rekordach (DAO już filtruje deletedAtMs)
        trainingDao.reorderAfterDeletion(exercise.trainingSessionId, exercise.order)

        // soft delete serii pod tym ćwiczeniem (optymalnie jednym query)
        val sets = trainingDao.getSetsForSessionExercise(exercise.id)
        for (set in sets) {
            trainingDao.softDeleteSet(set.id, deletedAtMs = now, updatedAtMs = now)
        }

        TrainingSyncScheduler.enqueue(context)
    }

    suspend fun getSetsForSessionExercise(sessionExerciseId: Long): List<SessionSetDetails> =
        trainingDao.getSetsForSessionExercise(sessionExerciseId)

    suspend fun addSetToSessionExercise(sessionExerciseId: Long, weight: Float, reps: Int) {
        val maxOrder = trainingDao.getMaxOrderForSessionExercise(sessionExerciseId) ?: -1
        val newSet = SessionSetDetails(
            sessionExerciseId = sessionExerciseId,
            order = maxOrder + 1,
            reps = reps,
            weight = weight
        )
        trainingDao.insertSet(newSet)
        TrainingSyncScheduler.enqueue(context)
    }

    suspend fun updateSet(set: SessionSetDetails) {
        trainingDao.updateSet(
            set.copy(
                updatedAtMs = System.currentTimeMillis(),
                syncStatus = 1
            )
        )
        TrainingSyncScheduler.enqueue(context)
    }

    suspend fun deleteSet(set: SessionSetDetails) {
        val now = System.currentTimeMillis()
        trainingDao.softDeleteSet(set.id, deletedAtMs = now, updatedAtMs = now)
        trainingDao.reorderSetsAfterDeletion(set.sessionExerciseId, set.order)
        TrainingSyncScheduler.enqueue(context)
    }

    suspend fun getRecentSessionsWithExercises(limit: Int): List<RecentSessionWithExercises> {
        val recentSessions = trainingDao.getRecentSessions(limit)

        return recentSessions.map { session ->
            val exercises = trainingDao.getExercisesForSession(session.id)
            val exerciseNames = exercises.mapNotNull { se ->
                exerciseDao.getExerciseById(se.exerciseId)?.name
            }

            RecentSessionWithExercises(
                sessionId = session.id,
                sessionDate = session.date,
                sessionDescription = session.description,
                exerciseNames = exerciseNames
            )
        }
    }

    fun observeRecentSessionsWithExercises(limit: Int): Flow<List<RecentSessionWithExercises>> =
        trainingDao.observeRecentSessions(limit).map { sessions ->
            sessions.map { session ->
                val exercises = trainingDao.getExercisesForSession(session.id)
                val exerciseNames = exercises.mapNotNull { se ->
                    exerciseDao.getExerciseById(se.exerciseId)?.name
                }

                RecentSessionWithExercises(
                    sessionId = session.id,
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
        val sessionId = createEmptySession(date, description)

        val templateExercises = templateDao.getExercisesForTemplate(templateId)
        templateExercises.forEach { templateExercise ->
            trainingDao.insertSessionExercise(
                SessionExercise(
                    trainingSessionId = sessionId,
                    exerciseId = templateExercise.exerciseId,
                    order = templateExercise.order
                )
            )
        }

        TrainingSyncScheduler.enqueue(context)
        return sessionId
    }
}
