package com.example.gymtrackapp.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymtrackapp.data.repository.PlanningRepository

class PlannerViewModelFactory(
    private val planningRepository: PlanningRepository,
) : ViewModelProvider.Factory {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlannerViewModel(planningRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

