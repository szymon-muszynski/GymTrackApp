package com.example.gymtrackapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymtrackapp.data.ExerciseDatabase
import com.example.gymtrackapp.data.repository.ExerciseRepository
import com.example.gymtrackapp.data.repository.TrainingRepository
import com.example.gymtrackapp.ui.screens.MainScreen
import com.example.gymtrackapp.ui.theme.GymTrackAppTheme
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModelFactory
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModel
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = ExerciseDatabase.getDatabase(applicationContext)
        val repository = ExerciseRepository(db.exerciseDao(), applicationContext)

        setContent {
            GymTrackAppTheme {
                // ExerciseViewModel
                val exerciseFactory = ExerciseViewModelFactory(repository)
                val exerciseViewModel: ExerciseViewModel = viewModel(factory = exerciseFactory)

                // TrainingViewModel
                val trainingFactory = TrainingViewModelFactory(
                    TrainingRepository(db.trainingDao(), applicationContext)
                )
                val trainingViewModel: TrainingViewModel = viewModel(factory = trainingFactory)

                // Załaduj ćwiczenia na starcie
                LaunchedEffect(Unit) {
                    exerciseViewModel.loadAllExercises()
                }

                MainScreen(
                    exerciseViewModel = exerciseViewModel,
                    trainingViewModel = trainingViewModel
                )
            }
        }
    }
}
