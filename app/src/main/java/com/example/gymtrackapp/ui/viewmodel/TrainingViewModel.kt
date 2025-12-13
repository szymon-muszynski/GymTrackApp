package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.entity.SessionExercise
import com.example.gymtrackapp.data.entity.SessionSetDetails
import com.example.gymtrackapp.data.entity.TrainingSession
import com.example.gymtrackapp.data.repository.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TrainingViewModel(private val repository: TrainingRepository) : ViewModel() {

    private val _sessions = MutableStateFlow<List<TrainingSession>>(emptyList())
    val sessions = _sessions.asStateFlow()

    private val _sessionExercisesMap = MutableStateFlow<Map<Long, List<SessionExercise>>>(emptyMap())
    val sessionExercisesMap = _sessionExercisesMap.asStateFlow()

    private val _sessionExerciseSetsMap = MutableStateFlow<Map<Long, List<SessionSetDetails>>>(emptyMap())

    private val _recentSessions = MutableStateFlow<List<com.example.gymtrackapp.data.entity.RecentSessionWithExercises>>(emptyList())
    val recentSessions = _recentSessions.asStateFlow()

    fun loadSessionsForDate(date: Long) {
        viewModelScope.launch {
            _sessions.value = repository.loadSessionsForDate(date)
        }
    }

    fun createEmptySession(date: Long, description: String) {
        viewModelScope.launch {
            repository.createEmptySession(
                TrainingSession(
                    date = date,
                    description = description
                )
            )
            loadSessionsForDate(date)
        }
    }

    fun deleteSession(session: TrainingSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
            loadSessionsForDate(session.date)
        }
    }

    fun updateSession(session: TrainingSession) {
        viewModelScope.launch {
            repository.updateSession(session)
            loadSessionsForDate(session.date)
        }
    }

    fun loadExercisesForSession(sessionId: Long) {
        viewModelScope.launch {
            val exercises = repository.getExercisesForSession(sessionId)
            _sessionExercisesMap.value = _sessionExercisesMap.value.toMutableMap().apply {
                this[sessionId] = exercises
            }
        }
    }

    fun addExerciseToSession(sessionId: Long, exerciseId: String) {
        viewModelScope.launch {
            repository.addExerciseToSession(sessionId, exerciseId)
            loadExercisesForSession(sessionId)
        }
    }

    fun deleteSessionExercise(exercise: SessionExercise) {
        viewModelScope.launch {
            repository.deleteSessionExercise(exercise)
            loadExercisesForSession(exercise.trainingSessionId)
        }
    }

    fun getExercisesForSession(sessionId: Long): List<SessionExercise> {
        return _sessionExercisesMap.value[sessionId] ?: emptyList()
    }

    fun getSetsForSessionExercise(sessionExerciseId: Long) =
        _sessionExerciseSetsMap.map { it[sessionExerciseId] ?: emptyList() }

    fun loadSetsForSessionExercise(sessionExerciseId: Long) {
        viewModelScope.launch {
            val sets = repository.getSetsForSessionExercise(sessionExerciseId)
            _sessionExerciseSetsMap.value = _sessionExerciseSetsMap.value.toMutableMap().apply {
                this[sessionExerciseId] = sets
            }
        }
    }

    fun addSetToSessionExercise(sessionExerciseId: Long, weight: Float, reps: Int) {
        viewModelScope.launch {
            repository.addSetToSessionExercise(sessionExerciseId, weight, reps)
            loadSetsForSessionExercise(sessionExerciseId)
        }
    }

    fun deleteSet(set: SessionSetDetails) {
        viewModelScope.launch {
            repository.deleteSet(set)
            loadSetsForSessionExercise(set.sessionExerciseId)
        }
    }

    fun loadRecentSessions(limit: Int = 3) {
        viewModelScope.launch {
            _recentSessions.value = repository.getRecentSessionsWithExercises(limit)
        }
    }
}