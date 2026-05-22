package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymtrackapp.data.social.repository.SocialRepository

class FriendsViewModelFactory(
    private val socialRepository: SocialRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FriendsViewModel(socialRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

