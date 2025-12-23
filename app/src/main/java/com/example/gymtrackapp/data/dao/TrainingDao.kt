package com.example.gymtrackapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymtrackapp.data.entity.SessionExercise
import com.example.gymtrackapp.data.entity.SessionSetDetails
import com.example.gymtrackapp.data.entity.TrainingSession

@Dao
interface TrainingDao {
    @Query("SELECT * FROM training_sessions WHERE date = :timestamp AND deletedAtMs IS NULL")
    suspend fun getSessionsForDate(timestamp: Long): List<TrainingSession>

    @Insert
    suspend fun createEmptySession(session: TrainingSession): Long

    @Update
    suspend fun updateSession(session: TrainingSession)

    /**
     * Soft delete sesji + kaskadowo: ćwiczenia + serie.
     * Wszystko oznaczamy jako PENDING_DELETE (2), żeby Worker wysłał to do Firestore.
     */
    @Query("UPDATE training_sessions SET deletedAtMs = :deletedAtMs, updatedAtMs = :updatedAtMs, syncStatus = 2 WHERE id = :sessionId")
    suspend fun softDeleteSession(sessionId: Long, deletedAtMs: Long, updatedAtMs: Long)

    @Query("UPDATE session_exercises SET deletedAtMs = :deletedAtMs, updatedAtMs = :updatedAtMs, syncStatus = 2 WHERE trainingSessionId = :sessionId")
    suspend fun softDeleteExercisesForSession(sessionId: Long, deletedAtMs: Long, updatedAtMs: Long)

    @Query("""
        UPDATE session_set_details
        SET deletedAtMs = :deletedAtMs, updatedAtMs = :updatedAtMs, syncStatus = 2
        WHERE sessionExerciseId IN (SELECT id FROM session_exercises WHERE trainingSessionId = :sessionId)
    """)
    suspend fun softDeleteSetsForSession(sessionId: Long, deletedAtMs: Long, updatedAtMs: Long)

    @Query("SELECT * FROM session_exercises WHERE trainingSessionId = :sessionId AND deletedAtMs IS NULL ORDER BY `order`")
    suspend fun getExercisesForSession(sessionId: Long): List<SessionExercise>

    @Insert
    suspend fun insertSessionExercise(exercise: SessionExercise): Long

    @Update
    suspend fun updateSessionExercise(exercise: SessionExercise)

    @Query("UPDATE session_exercises SET deletedAtMs = :deletedAtMs, updatedAtMs = :updatedAtMs, syncStatus = 2 WHERE id = :sessionExerciseId")
    suspend fun softDeleteSessionExercise(sessionExerciseId: Long, deletedAtMs: Long, updatedAtMs: Long)

    @Query("SELECT MAX(`order`) FROM session_exercises WHERE trainingSessionId = :sessionId AND deletedAtMs IS NULL")
    suspend fun getMaxOrderForSession(sessionId: Long): Int?

    @Query("UPDATE session_exercises SET `order` = `order` - 1 WHERE trainingSessionId = :sessionId AND deletedAtMs IS NULL AND `order` > :deletedOrder")
    suspend fun reorderAfterDeletion(sessionId: Long, deletedOrder: Int)

    @Query("SELECT * FROM session_set_details WHERE sessionExerciseId = :sessionExerciseId AND deletedAtMs IS NULL ORDER BY `order`")
    suspend fun getSetsForSessionExercise(sessionExerciseId: Long): List<SessionSetDetails>

    @Insert
    suspend fun insertSet(set: SessionSetDetails): Long

    @Update
    suspend fun updateSet(set: SessionSetDetails)

    @Query("UPDATE session_set_details SET deletedAtMs = :deletedAtMs, updatedAtMs = :updatedAtMs, syncStatus = 2 WHERE id = :setId")
    suspend fun softDeleteSet(setId: Long, deletedAtMs: Long, updatedAtMs: Long)

    @Query("SELECT MAX(`order`) FROM session_set_details WHERE sessionExerciseId = :sessionExerciseId AND deletedAtMs IS NULL")
    suspend fun getMaxOrderForSessionExercise(sessionExerciseId: Long): Int?

    @Query("UPDATE session_set_details SET `order` = `order` - 1 WHERE sessionExerciseId = :sessionExerciseId AND deletedAtMs IS NULL AND `order` > :deletedOrder")
    suspend fun reorderSetsAfterDeletion(sessionExerciseId: Long, deletedOrder: Int)

    // ============= SYNC QUERIES =============

    @Query("SELECT * FROM training_sessions WHERE syncStatus != 0 ORDER BY updatedAtMs ASC LIMIT :limit")
    suspend fun getPendingSessions(limit: Int = 100): List<TrainingSession>

    @Query("SELECT * FROM session_exercises WHERE syncStatus != 0 ORDER BY updatedAtMs ASC LIMIT :limit")
    suspend fun getPendingSessionExercises(limit: Int = 300): List<SessionExercise>

    @Query("SELECT * FROM session_set_details WHERE syncStatus != 0 ORDER BY updatedAtMs ASC LIMIT :limit")
    suspend fun getPendingSessionSets(limit: Int = 600): List<SessionSetDetails>

    @Query("UPDATE training_sessions SET syncStatus = 0 WHERE id = :sessionId")
    suspend fun markSessionSynced(sessionId: Long)

    @Query("UPDATE session_exercises SET syncStatus = 0 WHERE id = :sessionExerciseId")
    suspend fun markSessionExerciseSynced(sessionExerciseId: Long)

    @Query("UPDATE session_set_details SET syncStatus = 0 WHERE id = :setId")
    suspend fun markSessionSetSynced(setId: Long)

    // ============= STATISTICS QUERIES =============

    @Query("""
        SELECT ts.date,
               COUNT(DISTINCT ts.id) as sessionCount,
               COALESCE(SUM(ssd.reps * ssd.weight), 0) as totalVolume
        FROM training_sessions ts
        LEFT JOIN session_exercises se ON ts.id = se.trainingSessionId
        LEFT JOIN session_set_details ssd ON se.id = ssd.sessionExerciseId
        WHERE ts.date >= :startDate AND ts.date <= :endDate
          AND ts.deletedAtMs IS NULL
        GROUP BY ts.date
        ORDER BY ts.date ASC
    """)
    suspend fun getTrainingDaysData(startDate: Long, endDate: Long): List<TrainingDayDataRaw>

    @Query("""
        SELECT COALESCE(SUM(ssd.reps * ssd.weight), 0)
        FROM training_sessions ts
        JOIN session_exercises se ON ts.id = se.trainingSessionId
        JOIN session_set_details ssd ON se.id = ssd.sessionExerciseId
        WHERE ts.date >= :startDate AND ts.date <= :endDate
          AND ts.deletedAtMs IS NULL
    """)
    suspend fun getTotalVolumeInRange(startDate: Long, endDate: Long): Float

    @Query("""
        SELECT ssd.id as setId,
               ts.date as sessionDate,
               se.exerciseId,
               ssd.weight,
               ssd.reps,
               ssd.`order`
        FROM session_set_details ssd
        JOIN session_exercises se ON ssd.sessionExerciseId = se.id
        JOIN training_sessions ts ON se.trainingSessionId = ts.id
        WHERE se.exerciseId = :exerciseId
          AND ts.deletedAtMs IS NULL
        ORDER BY ts.date ASC, ssd.`order` ASC
    """)
    suspend fun getAllSetsForExerciseWithDates(exerciseId: String): List<SetWithDateRaw>

    @Query("""
        SELECT se.exerciseId,
               ssd.weight,
               ssd.reps,
               ts.date,
               MAX(ssd.weight * (1 + ssd.reps / 30.0)) as estimated1RM
        FROM session_set_details ssd
        JOIN session_exercises se ON ssd.sessionExerciseId = se.id
        JOIN training_sessions ts ON se.trainingSessionId = ts.id
        WHERE ts.deletedAtMs IS NULL
        GROUP BY se.exerciseId, ssd.weight, ssd.reps, ts.date
        ORDER BY ts.date DESC
        LIMIT :limit
    """)
    suspend fun getRecentTopSets(limit: Int): List<TopSetRaw>

    @Query("""
        SELECT e.primaryMuscles,
               COUNT(DISTINCT se.exerciseId) as exerciseCount,
               COUNT(ssd.id) as setCount,
               COALESCE(SUM(ssd.reps * ssd.weight), 0) as totalVolume
        FROM training_sessions ts
        JOIN session_exercises se ON ts.id = se.trainingSessionId
        JOIN session_set_details ssd ON se.id = ssd.sessionExerciseId
        JOIN exercises e ON se.exerciseId = e.id
        WHERE ts.date >= :startDate AND ts.date <= :endDate
          AND ts.deletedAtMs IS NULL
        GROUP BY e.primaryMuscles
        ORDER BY totalVolume DESC
    """)
    suspend fun getMuscleGroupVolumeData(startDate: Long, endDate: Long): List<MuscleGroupVolumeRaw>

    @Query("""
        SELECT ssd.reps,
               MAX(ssd.weight) as weight,
               ts.date,
               se.exerciseId
        FROM session_set_details ssd
        JOIN session_exercises se ON ssd.sessionExerciseId = se.id
        JOIN training_sessions ts ON se.trainingSessionId = ts.id
        WHERE se.exerciseId = :exerciseId
          AND ts.deletedAtMs IS NULL
        GROUP BY ssd.reps
        ORDER BY ssd.reps ASC
    """)
    suspend fun getRepMaxRecords(exerciseId: String): List<RepMaxRecordRaw>

    @Query("""
        SELECT ts.date,
               COALESCE(SUM(ssd.reps * ssd.weight), 0) as volume
        FROM training_sessions ts
        LEFT JOIN session_exercises se ON ts.id = se.trainingSessionId
        LEFT JOIN session_set_details ssd ON se.id = ssd.sessionExerciseId
        WHERE se.exerciseId = :exerciseId AND ts.date >= :startDate AND ts.date <= :endDate
          AND ts.deletedAtMs IS NULL
        GROUP BY ts.date
        ORDER BY ts.date ASC
    """)
    suspend fun getVolumeDataForExercise(exerciseId: String, startDate: Long, endDate: Long): List<VolumeDataRaw>

    @Query("""
        SELECT ts.date,
               MAX(ssd.weight) as weight,
               ssd.reps
        FROM session_set_details ssd
        JOIN session_exercises se ON ssd.sessionExerciseId = se.id
        JOIN training_sessions ts ON se.trainingSessionId = ts.id
        WHERE se.exerciseId = :exerciseId
          AND ts.deletedAtMs IS NULL
        GROUP BY ts.date
        HAVING MAX(ssd.weight)
        ORDER BY ts.date ASC
    """)
    suspend fun getTopSetsByDate(exerciseId: String): List<TopSetByDateRaw>

    @Query("SELECT * FROM training_sessions WHERE deletedAtMs IS NULL ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int): List<TrainingSession>

    @Query("SELECT * FROM training_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): TrainingSession?

    @Query("SELECT * FROM session_exercises WHERE id = :sessionExerciseId")
    suspend fun getSessionExerciseById(sessionExerciseId: Long): SessionExercise?

    @Query("SELECT remoteId FROM training_sessions WHERE id = :sessionId")
    suspend fun getSessionRemoteId(sessionId: Long): String?

    @Query("SELECT remoteId FROM session_exercises WHERE id = :sessionExerciseId")
    suspend fun getSessionExerciseRemoteId(sessionExerciseId: Long): String?

    @Query("SELECT trainingSessionId FROM session_exercises WHERE id = :sessionExerciseId")
    suspend fun getTrainingSessionIdForSessionExercise(sessionExerciseId: Long): Long?

    // ============= PULL / LOGIN SYNC HELPERS =============

    /** Czyści dane treningowe (sesje + ćwiczenia + sety). */
    @Query("DELETE FROM session_set_details")
    suspend fun clearSessionSets()

    @Query("DELETE FROM session_exercises")
    suspend fun clearSessionExercises()

    @Query("DELETE FROM training_sessions")
    suspend fun clearTrainingSessions()

    @Query("SELECT id FROM training_sessions WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findTrainingSessionIdByRemoteId(remoteId: String): Long?

    @Query("SELECT id FROM session_exercises WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findSessionExerciseIdByRemoteId(remoteId: String): Long?

    @Query("SELECT id FROM session_set_details WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findSessionSetIdByRemoteId(remoteId: String): Long?

    @Insert
    suspend fun insertTrainingSession(session: TrainingSession): Long

    @Insert
    suspend fun insertSessionSetDetails(set: SessionSetDetails): Long

}

// ============= RAW DATA CLASSES FOR ROOM QUERIES =============

/**
 * Raw data classes używane przez Room do mapowania wyników SQL
 * Będą konwertowane do właściwych data classes w Repository
 */

data class TrainingDayDataRaw(
    val date: Long,
    val sessionCount: Int,
    val totalVolume: Float
)

data class SetWithDateRaw(
    val setId: Long,
    val sessionDate: Long,
    val exerciseId: String,
    val weight: Float,
    val reps: Int,
    val order: Int
)

data class TopSetRaw(
    val exerciseId: String,
    val weight: Float,
    val reps: Int,
    val date: Long,
    val estimated1RM: Float
)

data class MuscleGroupVolumeRaw(
    val primaryMuscles: String,  // JSON string - będzie sparsowany
    val exerciseCount: Int,
    val setCount: Int,
    val totalVolume: Float
)

data class RepMaxRecordRaw(
    val reps: Int,
    val weight: Float,
    val date: Long,
    val exerciseId: String
)

data class VolumeDataRaw(
    val date: Long,
    val volume: Float
)

data class TopSetByDateRaw(
    val date: Long,
    val weight: Float,
    val reps: Int
)
