package com.example.gymtrackapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gymtrackapp.data.entity.SessionExercise
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
}