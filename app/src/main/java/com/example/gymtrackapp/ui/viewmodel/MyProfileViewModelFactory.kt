package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymtrackapp.data.social.repository.SocialRepository

class MyProfileViewModelFactory(
    private val socialRepository: SocialRepository,
    private val myUserId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyProfileViewModel::class.java)) {
            return MyProfileViewModel(socialRepository, myUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

