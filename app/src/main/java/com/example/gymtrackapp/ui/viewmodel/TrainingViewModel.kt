package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.entity.TrainingSession
import com.example.gymtrackapp.data.repository.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrainingViewModel(private val repository: TrainingRepository) : ViewModel() {

    private val _sessions = MutableStateFlow<List<TrainingSession>>(emptyList())
    val sessions = _sessions.asStateFlow()

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
            loadSessionsForDate(session.date) // odśwież listę
        }
    }

    fun updateSession(session: TrainingSession) {
        viewModelScope.launch {
            repository.updateSession(session)
            loadSessionsForDate(session.date)
        }
    }
}