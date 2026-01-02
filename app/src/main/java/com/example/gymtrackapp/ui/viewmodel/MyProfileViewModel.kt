package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.social.model.Post
import com.example.gymtrackapp.data.social.model.UserProfile
import com.example.gymtrackapp.data.social.repository.SocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyProfileViewModel(
    private val socialRepository: SocialRepository,
    private val myUserId: String,
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = socialRepository.observeUserProfile(myUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val posts: StateFlow<List<Post>> = socialRepository.observeUserPosts(myUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _deleteBusy = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val deleteBusy: StateFlow<Map<String, Boolean>> = _deleteBusy.asStateFlow()

    fun onEnterScreen() {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (_refreshing.value) return@launch
            _refreshing.value = true
            _error.value = null
            try {
                socialRepository.refreshUserProfile(myUserId)
                socialRepository.refreshUserPosts(myUserId, limit = 30)
            } catch (t: Throwable) {
                _error.value = t.message ?: "Błąd odświeżania"
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            if (_deleteBusy.value[post.postId] == true) return@launch
            _deleteBusy.value = _deleteBusy.value + (post.postId to true)
            _error.value = null
            try {
                socialRepository.deletePost(post)
            } catch (t: Throwable) {
                _error.value = t.message ?: "Błąd usuwania posta"
            } finally {
                _deleteBusy.value = _deleteBusy.value - post.postId
            }
        }
    }
}

