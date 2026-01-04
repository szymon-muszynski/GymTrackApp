package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymtrackapp.data.social.repository.SocialRepository

class ExploreFeedViewModelFactory(
    private val socialRepository: SocialRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExploreFeedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExploreFeedViewModel(socialRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

