package com.example.gymtrackapp.ui.viewmodel

import android.util.Log
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

class UserProfileViewModel(
    private val socialRepository: SocialRepository,
    val targetUserId: String,
) : ViewModel() {

    private companion object {
        const val PAGINATION_TAG = "SocialPagination"
    }

    val profile: StateFlow<UserProfile?> = socialRepository.observeUserProfile(targetUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val posts: StateFlow<List<Post>> = socialRepository.observeUserPosts(targetUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val followingIds: StateFlow<List<String>> = socialRepository.observeFollowingIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** czy trwa request follow/unfollow */
    private val _followBusy = MutableStateFlow(false)
    val followBusy: StateFlow<Boolean> = _followBusy.asStateFlow()

    /** Optimistic override dla relacji follow w profilu (true=follow, false=unfollow). */
    private val _optimisticIsFollowing = MutableStateFlow<Boolean?>(null)
    val optimisticIsFollowing: StateFlow<Boolean?> = _optimisticIsFollowing.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _loadingMore = MutableStateFlow(false)
    val loadingMore: StateFlow<Boolean> = _loadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var cursorCreatedAtMs: Long? = null

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun onEnterScreen() {
        viewModelScope.launch {
            runCatching { socialRepository.refreshUserProfile(targetUserId) }
        }
        refresh()
    }

    fun refresh(pageSize: Long = 20) {
        viewModelScope.launch {
            if (_refreshing.value) return@launch
            _refreshing.value = true
            _error.value = null
            try {
                Log.d(PAGINATION_TAG, "UserProfileVM($targetUserId): refreshFirstPage(pageSize=$pageSize)")
                cursorCreatedAtMs = socialRepository.refreshUserPostsFirstPage(targetUserId, pageSize = pageSize)
                _hasMore.value = cursorCreatedAtMs != null
                Log.d(PAGINATION_TAG, "UserProfileVM($targetUserId): first page loaded. nextCursor=$cursorCreatedAtMs hasMore=${_hasMore.value}")
            } catch (t: Throwable) {
                _error.value = t.message ?: "Błąd odświeżania"
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun loadMorePosts(pageSize: Long = 20) {
        viewModelScope.launch {
            if (_loadingMore.value || _refreshing.value) return@launch
            if (!_hasMore.value) return@launch

            val cursor = cursorCreatedAtMs
            if (cursor == null) {
                _hasMore.value = false
                Log.d(PAGINATION_TAG, "UserProfileVM($targetUserId): loadMore aborted - cursor is null (end reached)")
                return@launch
            }

            _loadingMore.value = true
            _error.value = null
            try {
                Log.d(PAGINATION_TAG, "UserProfileVM($targetUserId): loadMore(pageSize=$pageSize) startAfter=$cursor")
                cursorCreatedAtMs = socialRepository.refreshUserPostsNextPage(
                    userId = targetUserId,
                    pageSize = pageSize,
                    startAfterCreatedAtMs = cursor
                )
                _hasMore.value = cursorCreatedAtMs != null
                Log.d(PAGINATION_TAG, "UserProfileVM($targetUserId): loadMore done. nextCursor=$cursorCreatedAtMs hasMore=${_hasMore.value}")
            } catch (t: Throwable) {
                _error.value = t.message ?: "Błąd ładowania"
            } finally {
                _loadingMore.value = false
            }
        }
    }

    fun toggleFollow(isFollowing: Boolean) {
        viewModelScope.launch {
            if (_followBusy.value) return@launch
            _followBusy.value = true
            _error.value = null

            val desired = !isFollowing
            _optimisticIsFollowing.value = desired

            try {
                if (isFollowing) socialRepository.unfollow(targetUserId) else socialRepository.follow(targetUserId)
                _optimisticIsFollowing.value = null
            } catch (t: Throwable) {
                _error.value = t.message ?: "Błąd"
                _optimisticIsFollowing.value = null
            } finally {
                _followBusy.value = false
            }
        }
    }
}
