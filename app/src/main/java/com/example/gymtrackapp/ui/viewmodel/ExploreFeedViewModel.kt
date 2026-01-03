package com.example.gymtrackapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.social.model.Post
import com.example.gymtrackapp.data.social.repository.SocialRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreFeedViewModel(
    private val socialRepository: SocialRepository,
) : ViewModel() {

    private companion object {
        const val PAGINATION_TAG = "SocialPagination"
    }

    val feed: StateFlow<List<Post>> = socialRepository.observeExploreFeed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _loadingMore = MutableStateFlow(false)
    val loadingMore: StateFlow<Boolean> = _loadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var cursorCreatedAtMs: Long? = null

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun refresh() = refreshFirstPage()

    fun refreshFirstPage(pageSize: Long = 20) {
        viewModelScope.launch {
            if (_refreshing.value) return@launch
            _refreshing.value = true
            _error.value = null
            try {
                Log.d(PAGINATION_TAG, "ExploreVM: refreshFirstPage(pageSize=$pageSize)")
                cursorCreatedAtMs = socialRepository.refreshExploreFeedFirstPage(pageSize = pageSize)
                _hasMore.value = cursorCreatedAtMs != null
                Log.d(PAGINATION_TAG, "ExploreVM: first page loaded. nextCursor=$cursorCreatedAtMs hasMore=${_hasMore.value}")
            } catch (t: Throwable) {
                _error.value = t.message ?: "Błąd odświeżania"
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun loadMore(pageSize: Long = 20) {
        viewModelScope.launch {
            if (_loadingMore.value || _refreshing.value) return@launch
            if (!_hasMore.value) return@launch

            val cursor = cursorCreatedAtMs
            if (cursor == null) {
                _hasMore.value = false
                Log.d(PAGINATION_TAG, "ExploreVM: loadMore aborted - cursor is null (end reached)")
                return@launch
            }

            _loadingMore.value = true
            _error.value = null
            try {
                Log.d(PAGINATION_TAG, "ExploreVM: loadMore(pageSize=$pageSize) startAfter=$cursor")
                cursorCreatedAtMs = socialRepository.refreshExploreFeedNextPage(
                    pageSize = pageSize,
                    startAfterCreatedAtMs = cursor
                )
                _hasMore.value = cursorCreatedAtMs != null
                Log.d(PAGINATION_TAG, "ExploreVM: loadMore done. nextCursor=$cursorCreatedAtMs hasMore=${_hasMore.value}")
            } catch (t: Throwable) {
                _error.value = t.message ?: "Błąd ładowania"
            } finally {
                _loadingMore.value = false
            }
        }
    }
}
