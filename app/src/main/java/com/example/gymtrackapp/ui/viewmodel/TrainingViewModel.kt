package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.entity.RecentSessionWithExercises
import com.example.gymtrackapp.data.entity.SessionExercise
import com.example.gymtrackapp.data.entity.SessionSetDetails
import com.example.gymtrackapp.data.entity.TrainingSession
import com.example.gymtrackapp.data.repository.TrainingRepository
import kotlinx.coroutines.Job
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

    private val _recentSessions = MutableStateFlow<List<RecentSessionWithExercises>>(emptyList())
    val recentSessions = _recentSessions.asStateFlow()

    private var sessionsForDateJob: Job? = null
    private var currentObservedDate: Long? = null

    private var recentSessionsJob: Job? = null

    fun loadSessionsForDate(date: Long) {
        // Jeśli UI woła to wiele razy dla tej samej daty (np. recomposition), nie twórz nowych collectów
        if (currentObservedDate == date && sessionsForDateJob?.isActive == true) return

        currentObservedDate = date
        sessionsForDateJob?.cancel()
        sessionsForDateJob = viewModelScope.launch {
            repository.observeSessionsForDate(date).collect { list ->
                _sessions.value = list
            }
        }
    }

    /**
     * Reaktywne ładowanie ostatnich sesji (do HomePage).
     * HomePage może to zawołać raz, a lista będzie sama się odświeżać gdy Room się zmieni.
     */
    fun loadRecentSessions(limit: Int = 3) {
        // jeśli ktoś woła kilkukrotnie, nie rób wielu kolektorów
        if (recentSessionsJob?.isActive == true) return

        recentSessionsJob = viewModelScope.launch {
            repository.observeRecentSessionsWithExercises(limit).collect { list ->
                _recentSessions.value = list
            }
        }
    }

    fun createEmptySession(date: Long, description: String) {
        viewModelScope.launch {
            repository.createEmptySession(date = date, description = description)
            // sessions Flow sam się odświeży; to jest tylko "fallback" gdyby ktoś zmienił implementację
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


    private val _sessionCreationError = MutableStateFlow<String?>(null)
    val sessionCreationError = _sessionCreationError.asStateFlow()

    fun clearSessionCreationError() {
        _sessionCreationError.value = null
    }

    fun createSessionFromTemplate(
        templateId: Long,
        date: Long,
        description: String
    ) {
        viewModelScope.launch {
            try {
                repository.createSessionFromTemplate(templateId, date, description)
                _sessionCreationError.value = null
                loadSessionsForDate(date)
            } catch (t: Throwable) {
                _sessionCreationError.value =
                    t.message ?: "Nie udało się utworzyć treningu z szablonu."
            }
        }
    }

    // Stan UI: który sessionId ma aktualnie otwarty edytor notatki
    private val _noteEditorSessionId = MutableStateFlow<Long?>(null)
    val noteEditorSessionId = _noteEditorSessionId.asStateFlow()

    fun openNoteEditor(sessionId: Long) {
        _noteEditorSessionId.value = sessionId
    }

    fun closeNoteEditor() {
        _noteEditorSessionId.value = null
    }

    /**
     * Zapis notatki do Room. Jeśli tekst jest pusty po trim(), traktujemy to jako usunięcie.
     */
    fun saveSessionNote(sessionId: Long, text: String) {
        val trimmed = text.trim().ifEmpty { null }
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId) ?: return@launch
            repository.updateSession(session.copy(note = trimmed))
            closeNoteEditor()
        }
    }

    fun deleteSessionNote(sessionId: Long) {
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId) ?: return@launch
            repository.updateSession(session.copy(note = null))
        }
    }
}