package com.example.gymtrackapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymtrackapp.data.entity.SessionExercise
import com.example.gymtrackapp.data.entity.SessionSetDetails
import com.example.gymtrackapp.data.entity.TrainingSession

@Dao
interface TrainingDao {
    @Query("SELECT * FROM training_sessions WHERE date = :timestamp")
    suspend fun getSessionsForDate(timestamp: Long): List<TrainingSession>

    @Insert
    suspend fun createEmptySession(session: TrainingSession): Long

    @Update
    suspend fun updateSession(session: TrainingSession)

    @Delete
    suspend fun deleteSession(session: TrainingSession)

    @Query("SELECT * FROM session_exercises WHERE trainingSessionId = :sessionId ORDER BY `order`")
    suspend fun getExercisesForSession(sessionId: Long): List<SessionExercise>

    @Insert
    suspend fun insertSessionExercise(exercise: SessionExercise): Long

    @Delete
    suspend fun deleteSessionExercise(exercise: SessionExercise)

    @Query("SELECT MAX(`order`) FROM session_exercises WHERE trainingSessionId = :sessionId")
    suspend fun getMaxOrderForSession(sessionId: Long): Int?

    @Query("UPDATE session_exercises SET `order` = `order` - 1 WHERE trainingSessionId = :sessionId AND `order` > :deletedOrder")
    suspend fun reorderAfterDeletion(sessionId: Long, deletedOrder: Int)

    @Query("SELECT * FROM session_set_details WHERE sessionExerciseId = :sessionExerciseId ORDER BY `order`")
    suspend fun getSetsForSessionExercise(sessionExerciseId: Long): List<SessionSetDetails>

    @Insert
    suspend fun insertSet(set: SessionSetDetails): Long

    @Delete
    suspend fun deleteSet(set: SessionSetDetails)

    @Query("SELECT MAX(`order`) FROM session_set_details WHERE sessionExerciseId = :sessionExerciseId")
    suspend fun getMaxOrderForSessionExercise(sessionExerciseId: Long): Int?

    @Query("UPDATE session_set_details SET `order` = `order` - 1 WHERE sessionExerciseId = :sessionExerciseId AND `order` > :deletedOrder")
    suspend fun reorderSetsAfterDeletion(sessionExerciseId: Long, deletedOrder: Int)

    // ============= STATISTICS QUERIES =============

    /**
     * Pobiera dane o aktywności treningowej w zakresie dat dla heatmapy/kalendarza
     * Zwraca timestamp, liczbę sesji i total volume dla każdego dnia
     */
    @Query("""
        SELECT ts.date, 
               COUNT(DISTINCT ts.id) as sessionCount,
               COALESCE(SUM(ssd.reps * ssd.weight), 0) as totalVolume
        FROM training_sessions ts
        LEFT JOIN session_exercises se ON ts.id = se.trainingSessionId
        LEFT JOIN session_set_details ssd ON se.id = ssd.sessionExerciseId
        WHERE ts.date >= :startDate AND ts.date <= :endDate
        GROUP BY ts.date
        ORDER BY ts.date ASC
    """)
    suspend fun getTrainingDaysData(startDate: Long, endDate: Long): List<TrainingDayDataRaw>

    /**
     * Oblicza total volume w zakresie dat
     */
    @Query("""
        SELECT COALESCE(SUM(ssd.reps * ssd.weight), 0)
        FROM training_sessions ts
        JOIN session_exercises se ON ts.id = se.trainingSessionId
        JOIN session_set_details ssd ON se.id = ssd.sessionExerciseId
        WHERE ts.date >= :startDate AND ts.date <= :endDate
    """)
    suspend fun getTotalVolumeInRange(startDate: Long, endDate: Long): Float

    /**
     * Pobiera wszystkie serie dla konkretnego ćwiczenia z datami sesji
     * Używane do wykresów postępu
     */
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
        ORDER BY ts.date ASC, ssd.`order` ASC
    """)
    suspend fun getAllSetsForExerciseWithDates(exerciseId: String): List<SetWithDateRaw>

    /**
     * Pobiera najlepsze serie (top PRs) dla każdego ćwiczenia
     * Sortowane po dacie (najnowsze pierwsze)
     */
    @Query("""
        SELECT se.exerciseId,
               ssd.weight,
               ssd.reps,
               ts.date,
               MAX(ssd.weight * (1 + ssd.reps / 30.0)) as estimated1RM
        FROM session_set_details ssd
        JOIN session_exercises se ON ssd.sessionExerciseId = se.id
        JOIN training_sessions ts ON se.trainingSessionId = ts.id
        GROUP BY se.exerciseId, ssd.weight, ssd.reps, ts.date
        ORDER BY ts.date DESC
        LIMIT :limit
    """)
    suspend fun getRecentTopSets(limit: Int): List<TopSetRaw>

    /**
     * Pobiera volume per partia mięśniowa w zakresie dat
     */
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
        GROUP BY e.primaryMuscles
        ORDER BY totalVolume DESC
    """)
    suspend fun getMuscleGroupVolumeData(startDate: Long, endDate: Long): List<MuscleGroupVolumeRaw>

    /**
     * Pobiera najlepszą serię dla każdej liczby powtórzeń (Rep Max Matrix)
     */
    @Query("""
        SELECT ssd.reps,
               MAX(ssd.weight) as weight,
               ts.date,
               se.exerciseId
        FROM session_set_details ssd
        JOIN session_exercises se ON ssd.sessionExerciseId = se.id
        JOIN training_sessions ts ON se.trainingSessionId = ts.id
        WHERE se.exerciseId = :exerciseId
        GROUP BY ssd.reps
        ORDER BY ssd.reps ASC
    """)
    suspend fun getRepMaxRecords(exerciseId: String): List<RepMaxRecordRaw>

    /**
     * Pobiera volume per dzień dla wykresów
     */
    @Query("""
        SELECT ts.date,
               COALESCE(SUM(ssd.reps * ssd.weight), 0) as volume
        FROM training_sessions ts
        LEFT JOIN session_exercises se ON ts.id = se.trainingSessionId
        LEFT JOIN session_set_details ssd ON se.id = ssd.sessionExerciseId
        WHERE se.exerciseId = :exerciseId AND ts.date >= :startDate AND ts.date <= :endDate
        GROUP BY ts.date
        ORDER BY ts.date ASC
    """)
    suspend fun getVolumeDataForExercise(exerciseId: String, startDate: Long, endDate: Long): List<VolumeDataRaw>

    /**
     * Pobiera najcięższą serię dla każdego dnia treningowego (Top Set Tracking)
     */
    @Query("""
        SELECT ts.date,
               MAX(ssd.weight) as weight,
               ssd.reps
        FROM session_set_details ssd
        JOIN session_exercises se ON ssd.sessionExerciseId = se.id
        JOIN training_sessions ts ON se.trainingSessionId = ts.id
        WHERE se.exerciseId = :exerciseId
        GROUP BY ts.date
        HAVING MAX(ssd.weight)
        ORDER BY ts.date ASC
    """)
    suspend fun getTopSetsByDate(exerciseId: String): List<TopSetByDateRaw>

    /**
     * Pobiera ostatnie N sesji treningowych
     */
    @Query("SELECT * FROM training_sessions ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int): List<TrainingSession>

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
