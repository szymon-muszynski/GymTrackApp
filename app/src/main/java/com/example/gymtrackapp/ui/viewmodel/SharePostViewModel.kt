package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.social.repository.SocialRepository
import com.example.gymtrackapp.data.dao.TrainingDao
import com.example.gymtrackapp.utils.NetworkMonitor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SharePostViewModel(
    private val socialRepository: SocialRepository,
    private val trainingDao: TrainingDao,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _publishing = MutableStateFlow(false)
    val publishing: StateFlow<Boolean> = _publishing.asStateFlow()

    // legacy (wciąż może być używane w UI), ale do snackbara lepsze są eventy
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    private val _events = Channel<UiEvent>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun publish(sessionId: Long) {
        viewModelScope.launch {
            if (_publishing.value) return@launch

            if (networkMonitor.getCurrent() != NetworkMonitor.NetworkState.OnlineValidated) {
                _events.trySend(UiEvent.ShowSnackbar("Brak połączenia z internetem"))
                return@launch
            }

            _publishing.value = true
            _message.value = null
            try {
                val wasPosted = trainingDao.isSessionPosted(sessionId) == true
                socialRepository.publishPost(sessionId)
                val msg = if (wasPosted) "Zaktualizowano post" else "Udostępniono"
                _message.value = msg
                _events.trySend(UiEvent.ShowSnackbar(msg))
            } catch (t: Throwable) {
                val msg = t.message ?: "Błąd udostępniania"
                _message.value = msg
                _events.trySend(UiEvent.ShowSnackbar(msg))
            } finally {
                _publishing.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
