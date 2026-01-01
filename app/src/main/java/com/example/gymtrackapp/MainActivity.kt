package com.example.gymtrackapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymtrackapp.data.ExerciseDatabase
import com.example.gymtrackapp.data.repository.ExerciseRepository
import com.example.gymtrackapp.data.repository.TemplateRepository
import com.example.gymtrackapp.data.repository.TrainingRepository
import com.example.gymtrackapp.data.social.repository.FirestoreSocialRepository
import com.example.gymtrackapp.ui.screens.AuthScreen
import com.example.gymtrackapp.ui.screens.MainScreen
import com.example.gymtrackapp.ui.theme.GymTrackAppTheme
import com.example.gymtrackapp.ui.viewmodel.AuthViewModel
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModelFactory
import com.example.gymtrackapp.ui.viewmodel.FriendsViewModel
import com.example.gymtrackapp.ui.viewmodel.FriendsViewModelFactory
import com.example.gymtrackapp.ui.viewmodel.StatisticsViewModel
import com.example.gymtrackapp.ui.viewmodel.StatisticsViewModelFactory
import com.example.gymtrackapp.ui.viewmodel.TemplateViewModel
import com.example.gymtrackapp.ui.viewmodel.TemplateViewModelFactory
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModel
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = ExerciseDatabase.getDatabase(this)
        val exerciseRepository = ExerciseRepository(database.exerciseDao(), this)
        val trainingRepository = TrainingRepository(
            database.trainingDao(),
            this,
            database.templateDao(),
            database.exerciseDao()
        )
        val templateRepository = TemplateRepository(
            database.templateDao(),
            this
        )

        val statisticsRepository = com.example.gymtrackapp.data.repository.StatisticsRepository(
            database.trainingDao(),
            database.exerciseDao()
        )

        val socialRepository = FirestoreSocialRepository(
            auth = FirebaseAuth.getInstance(),
            firestore = FirebaseFirestore.getInstance(),
            trainingDao = database.trainingDao(),
            exerciseDao = database.exerciseDao(),
            socialDao = database.socialDao(),
        )

        setContent {
            GymTrackAppTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return AuthViewModel(this@MainActivity.applicationContext) as T
                        }
                    }
                )

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
                    val friendsViewModel: FriendsViewModel = viewModel(
                        factory = FriendsViewModelFactory(socialRepository)
                    )

                    MainScreen(
                        exerciseViewModel = exerciseViewModel,
                        trainingViewModel = trainingViewModel,
                        templateViewModel = templateViewModel,
                        authViewModel = authViewModel,
                        statisticsViewModel = statisticsViewModel,
                        friendsViewModel = friendsViewModel,
                        onSignOut = { /* refresh nastąpi automatycznie przez collectAsState */ }
                    )
                }
            }
        }
    }
}
