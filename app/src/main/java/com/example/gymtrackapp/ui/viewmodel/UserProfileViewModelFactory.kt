package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymtrackapp.data.social.repository.SocialRepository

class UserProfileViewModelFactory(
    private val socialRepository: SocialRepository,
    private val userId: String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(socialRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

