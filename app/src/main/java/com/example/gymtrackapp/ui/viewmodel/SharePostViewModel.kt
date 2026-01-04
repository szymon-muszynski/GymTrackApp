package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.social.repository.SocialRepository
import com.example.gymtrackapp.data.dao.TrainingDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharePostViewModel(
    private val socialRepository: SocialRepository,
    private val trainingDao: TrainingDao,
) : ViewModel() {

    private val _publishing = MutableStateFlow(false)
    val publishing: StateFlow<Boolean> = _publishing.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun publish(sessionId: Long) {
        viewModelScope.launch {
            if (_publishing.value) return@launch
            _publishing.value = true
            _message.value = null
            try {
                val wasPosted = trainingDao.isSessionPosted(sessionId) == true
                socialRepository.publishPost(sessionId)
                _message.value = if (wasPosted) "Zaktualizowano post" else "Udostępniono"
            } catch (t: Throwable) {
                _message.value = t.message ?: "Błąd udostępniania"
            } finally {
                _publishing.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
