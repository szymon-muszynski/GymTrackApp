package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.social.model.UserProfile
import com.example.gymtrackapp.data.social.repository.SocialRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class FriendsViewModel(
    private val socialRepository: SocialRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<UserProfile>>(emptyList())
    val results: StateFlow<List<UserProfile>> = _results.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val followingIds = socialRepository.observeFollowingIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Optimistic UI override: jeżeli user kliknie follow/unfollow,
     * to natychmiast zmieniamy stan przycisku, niezależnie od opóźnienia w Room/Firestore.
     */
    private val _optimisticFollowingOverrides = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val optimisticFollowingOverrides: StateFlow<Map<String, Boolean>> = _optimisticFollowingOverrides.asStateFlow()

    private var searchJob: Job? = null

    fun onEnterScreen() {
        viewModelScope.launch {
            runCatching { socialRepository.syncFollowing() }
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            val q = value.trim()
            if (q.length < 2) {
                _results.value = emptyList()
                _loading.value = false
                _error.value = null
                return@launch
            }

            _loading.value = true
            _error.value = null

            try {
                _results.value = socialRepository.searchUsersByDisplayNamePrefix(q, limit = 20)
            } catch (t: Throwable) {
                _error.value = t.message ?: "Błąd wyszukiwania"
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleFollow(userId: String, isFollowing: Boolean) {
        viewModelScope.launch {
            _error.value = null

            // optimistic switch
            val desired = !isFollowing
            _optimisticFollowingOverrides.value = _optimisticFollowingOverrides.value + (userId to desired)

            try {
                if (isFollowing) socialRepository.unfollow(userId) else socialRepository.follow(userId)

                // po sukcesie zdejmujemy override (źródłem prawdy pozostaje Room)
                _optimisticFollowingOverrides.value = _optimisticFollowingOverrides.value - userId
            } catch (t: Throwable) {
                _error.value = t.message ?: "Błąd"

                // rollback optimistic
                _optimisticFollowingOverrides.value = _optimisticFollowingOverrides.value - userId
            }
        }
    }
}
