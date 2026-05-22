package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymtrackapp.data.social.repository.SocialRepository
import com.example.gymtrackapp.utils.NetworkMonitor

class MyProfileViewModelFactory(
    private val socialRepository: SocialRepository,
    private val myUserId: String,
    private val networkMonitor: NetworkMonitor,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyProfileViewModel::class.java)) {
            return MyProfileViewModel(socialRepository, myUserId, networkMonitor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
