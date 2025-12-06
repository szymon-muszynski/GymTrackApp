package com.example.gymtrackapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymtrackapp.data.ExerciseDatabase
import com.example.gymtrackapp.data.repository.ExerciseRepository
import com.example.gymtrackapp.data.repository.TrainingRepository
import com.example.gymtrackapp.ui.screens.AuthScreen
import com.example.gymtrackapp.ui.screens.MainScreen
import com.example.gymtrackapp.ui.theme.GymTrackAppTheme
import com.example.gymtrackapp.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = ExerciseDatabase.getDatabase(this)
        val exerciseRepository = ExerciseRepository(database.exerciseDao(), this)
        val trainingRepository = TrainingRepository(database.trainingDao(), this)
        val statisticsRepository = com.example.gymtrackapp.data.repository.StatisticsRepository(
            database.trainingDao(),
            database.exerciseDao()
        )

        setContent {
            GymTrackAppTheme {
                val authViewModel: AuthViewModel = viewModel()
                val currentUser by authViewModel.currentUser.collectAsState()

                val exerciseViewModel: ExerciseViewModel = viewModel(
                    factory = ExerciseViewModelFactory(exerciseRepository)
                )
                val trainingViewModel: TrainingViewModel = viewModel(
                    factory = TrainingViewModelFactory(trainingRepository)
                )
                val statisticsViewModel: com.example.gymtrackapp.ui.viewmodel.StatisticsViewModel = viewModel(
                    factory = com.example.gymtrackapp.ui.viewmodel.StatisticsViewModelFactory(statisticsRepository)
                )

                LaunchedEffect(Unit) {
                    exerciseViewModel.loadAllExercises()
                }

                if (currentUser == null) {
                    AuthScreen(
                        authViewModel = authViewModel,
                        onAuthSuccess = {}
                    )
                } else {
                    MainScreen(
                        exerciseViewModel = exerciseViewModel,
                        trainingViewModel = trainingViewModel,
                        authViewModel = authViewModel,
                        statisticsViewModel = statisticsViewModel,
                        onSignOut = { /* refresh nastąpi automatycznie przez collectAsState */ }
                    )
                }
            }
        }
    }
}
