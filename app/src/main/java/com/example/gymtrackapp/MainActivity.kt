package com.example.gymtrackapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymtrackapp.data.ExerciseDatabase
import com.example.gymtrackapp.data.repository.ExerciseRepository
import com.example.gymtrackapp.data.repository.TemplateRepository
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
        val templateRepository = TemplateRepository(database.templateDao())

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

                val templateViewModel: TemplateViewModel = viewModel(
                    factory = TemplateViewModelFactory(templateRepository)
                )

                val statisticsViewModel: StatisticsViewModel = viewModel(
                    factory = StatisticsViewModelFactory(statisticsRepository)
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
                        templateViewModel = templateViewModel,
                        authViewModel = authViewModel,
                        statisticsViewModel = statisticsViewModel,
                        onSignOut = { /* refresh nastąpi automatycznie przez collectAsState */ }
                    )
                }
            }
        }
    }
}
