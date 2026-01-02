package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymtrackapp.data.dao.TrainingDao
import com.example.gymtrackapp.data.social.repository.SocialRepository

class SharePostViewModelFactory(
    private val socialRepository: SocialRepository,
    private val trainingDao: TrainingDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharePostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharePostViewModel(socialRepository, trainingDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
