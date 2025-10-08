package com.example.gymtrackapp.data.repository

import android.content.Context
import com.example.gymtrackapp.data.dao.TrainingDao
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
}