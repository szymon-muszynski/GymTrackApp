package com.example.gymtrackapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymtrackapp.data.ExerciseDatabase
import com.example.gymtrackapp.data.repository.ExerciseRepository
import com.example.gymtrackapp.data.repository.TemplateRepository
import com.example.gymtrackapp.data.repository.TrainingRepository
import com.example.gymtrackapp.di.GymTrackAppContainer
import com.example.gymtrackapp.di.LocalAppContainer
import com.example.gymtrackapp.ui.screens.AuthScreen
import com.example.gymtrackapp.ui.screens.MainScreen
import com.example.gymtrackapp.ui.theme.GymTrackAppTheme
import com.example.gymtrackapp.ui.viewmodel.AuthViewModel
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModelFactory
import com.example.gymtrackapp.ui.viewmodel.ExploreFeedViewModel
import com.example.gymtrackapp.ui.viewmodel.ExploreFeedViewModelFactory
import com.example.gymtrackapp.ui.viewmodel.FriendsViewModel
import com.example.gymtrackapp.ui.viewmodel.FriendsViewModelFactory
import com.example.gymtrackapp.ui.viewmodel.SharePostViewModel
import com.example.gymtrackapp.ui.viewmodel.SharePostViewModelFactory
import com.example.gymtrackapp.ui.viewmodel.StatisticsViewModel
import com.example.gymtrackapp.ui.viewmodel.StatisticsViewModelFactory
import com.example.gymtrackapp.ui.viewmodel.TemplateViewModel
import com.example.gymtrackapp.ui.viewmodel.TemplateViewModelFactory
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModel
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Repo lokalne zostawiamy jak były (MVP) – Social przenosimy do AppContainer.
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

        setContent {
            // Jeden kontener zależności na całą kompozycję.
            val appContainer = remember { GymTrackAppContainer(applicationContext) }

            CompositionLocalProvider(LocalAppContainer provides appContainer) {
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
                    val currentUid = currentUser?.uid

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
                        val socialRepository = appContainer.socialRepository

                        // WAŻNE: kluczujemy VM po uid, żeby po zmianie konta zawsze zbudować nowy VM
                        // i nie trzymać starych StateFlow/stateIn z poprzedniego użytkownika.
                        val friendsViewModel: FriendsViewModel = viewModel(
                            key = "friends_${currentUid}",
                            factory = FriendsViewModelFactory(socialRepository)
                        )

                        val exploreFeedViewModel: ExploreFeedViewModel = viewModel(
                            key = "explore_${currentUid}",
                            factory = ExploreFeedViewModelFactory(socialRepository)
                        )

                        val sharePostViewModel: SharePostViewModel = viewModel(
                            key = "share_${currentUid}",
                            factory = SharePostViewModelFactory(socialRepository, database.trainingDao())
                        )

                        // Sync following ASAP po zalogowaniu, zanim user wejdzie w Friends.
                        LaunchedEffect(currentUid) {
                            // best-effort; błędy i tak pokażą się potem w Search/Feed
                            runCatching { socialRepository.syncFollowing() }
                        }

                        MainScreen(
                            exerciseViewModel = exerciseViewModel,
                            trainingViewModel = trainingViewModel,
                            templateViewModel = templateViewModel,
                            authViewModel = authViewModel,
                            statisticsViewModel = statisticsViewModel,
                            friendsViewModel = friendsViewModel,
                            exploreFeedViewModel = exploreFeedViewModel,
                            sharePostViewModel = sharePostViewModel,
                            onSignOut = { /* refresh nastąpi automatycznie przez collectAsState */ }
                        )
                    }
                }
            }
        }
    }
}
