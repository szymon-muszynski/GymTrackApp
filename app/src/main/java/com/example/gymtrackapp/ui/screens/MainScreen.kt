package com.example.gymtrackapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gymtrackapp.ui.viewmodel.AuthViewModel
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModel
import com.example.gymtrackapp.ui.viewmodel.StatisticsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    exerciseViewModel: ExerciseViewModel,
    trainingViewModel: TrainingViewModel,
    authViewModel: AuthViewModel,
    statisticsViewModel: StatisticsViewModel,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    var showAddSessionDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gym Track App") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.signOut()
                        onSignOut()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Wyloguj")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute == "calendar") {
                FloatingActionButton(onClick = { showAddSessionDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj sesję")
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                val currentUser by authViewModel.currentUser.collectAsState()
                val userName = currentUser?.displayName ?: currentUser?.email?.substringBefore("@")

                HomePage(
                    onNavigateToStatistics = { navController.navigate("progress") },
                    onNavigateToPlans = { navController.navigate("planner") },
                    onNavigateToSettings = { navController.navigate("profile") },
                    onAddWorkoutSession = {
                        showAddSessionDialog = true
                        navController.navigate("calendar")
                    },
                    userName = userName,
                    statisticsViewModel = statisticsViewModel
                )
            }
            composable("calendar") {
                // Reset dialog state when navigating away
                DisposableEffect(Unit) {
                    onDispose {
                        showAddSessionDialog = false
                    }
                }

                CalendarPage(
                    trainingViewModel = trainingViewModel,
                    exerciseViewModel = exerciseViewModel,
                    statisticsViewModel = statisticsViewModel,
                    showAddSessionDialog = showAddSessionDialog,
                    onDismissDialog = { showAddSessionDialog = false },
                    navController = navController
                )
            }
            composable("planner") {
                PlannerPage(viewModel = exerciseViewModel)
            }
            composable("progress") {
                ProgressPage(
                    viewModel = statisticsViewModel,
                    exerciseViewModel = exerciseViewModel
                )
            }
            composable("friends") {
                FriendsPage()
            }
            composable("profile") {
                ProfilePage()
            }
            composable("add_exercise/{sessionId}") { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId")?.toLongOrNull() ?: 0L
                AddExerciseScreen(
                    sessionId = sessionId,
                    onNavigateBack = { navController.popBackStack() },
                    exerciseViewModel = exerciseViewModel,
                    trainingViewModel = trainingViewModel,
                    statisticsViewModel = statisticsViewModel
                )
            }
            composable("set_details/{sessionExerciseId}/{exerciseId}") { backStackEntry ->
                val sessionExerciseId = backStackEntry.arguments?.getString("sessionExerciseId")?.toLongOrNull() ?: 0L
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
                SetDetailsScreen(
                    sessionExerciseId = sessionExerciseId,
                    exerciseId = exerciseId,
                    onNavigateBack = { navController.popBackStack() },
                    exerciseViewModel = exerciseViewModel,
                    trainingViewModel = trainingViewModel,
                    statisticsViewModel = statisticsViewModel
                )
            }
        }
    }

}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Calendar", Icons.Default.DateRange),
        NavItem("Planner", Icons.Default.Build),
        NavItem("Progress", Icons.Default.Star),
        NavItem("Friends", Icons.Default.Face),
        NavItem("Profile", Icons.Default.Person)
    )

    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        items.forEach { item ->
            val route = item.label.lowercase()
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            // Dla Home - wyczyść cały backstack
                            if (route == "home") {
                                popUpTo(0) {
                                    inclusive = false
                                }
                            } else {
                                // Dla innych - wróć do home ale nie usuwaj go
                                popUpTo("home") {
                                    inclusive = false
                                }
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                }
            )
        }
    }
}

