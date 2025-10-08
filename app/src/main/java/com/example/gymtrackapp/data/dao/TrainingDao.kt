package com.example.gymtrackapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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
}