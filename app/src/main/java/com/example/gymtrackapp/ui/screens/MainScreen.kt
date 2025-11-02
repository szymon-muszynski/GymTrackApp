package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    exerciseViewModel: ExerciseViewModel,
    trainingViewModel: TrainingViewModel
) {
    val navController = rememberNavController()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val navItemsList = listOf(
        NavItem("Home", icon = Icons.Default.Home),
        NavItem("Calendar", icon = Icons.Default.DateRange),
        NavItem("Planner", icon = Icons.Default.Edit)
    )

    var selectedIndex by remember { mutableStateOf(0) }
    var showAddSessionDialog by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar {
                        navItemsList.forEachIndexed { index, navItem ->
                            NavigationBarItem(
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index },
                                icon = {
                                    Icon(imageVector = navItem.icon, contentDescription = navItem.label)
                                },
                                label = { Text(text = navItem.label) }
                            )
                        }
                    }
                },
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "Gym Track",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { /* TODO */ }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* TODO */ }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior,
                    )
                },
                floatingActionButton = {
                    if (selectedIndex == 1) {
                        FloatingActionButton(onClick = { showAddSessionDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Dodaj sesję")
                        }
                    }
                }
            ) { innerPadding ->
                ContentScreen(
                    modifier = Modifier.padding(innerPadding),
                    selectedIndex = selectedIndex,
                    exerciseViewModel = exerciseViewModel,
                    trainingViewModel = trainingViewModel,
                    showAddSessionDialog = showAddSessionDialog,
                    onDismissDialog = { showAddSessionDialog = false },
                    navController = navController
                )
            }
        }

        composable("add_exercise/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")?.toLongOrNull() ?: 0L
            AddExerciseScreen(
                sessionId = sessionId,
                onNavigateBack = { navController.popBackStack() },
                exerciseViewModel = exerciseViewModel,
                trainingViewModel = trainingViewModel  // ← DODAJ TO
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
                trainingViewModel = trainingViewModel
            )
        }
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    exerciseViewModel: ExerciseViewModel,
    trainingViewModel: TrainingViewModel,
    showAddSessionDialog: Boolean,
    onDismissDialog: () -> Unit,
    navController: NavHostController
) {
    when (selectedIndex) {
        0 -> HomePage(modifier = modifier)
        1 -> CalendarPage(
            modifier = modifier,
            trainingViewModel = trainingViewModel,
            exerciseViewModel = exerciseViewModel,  // ← DODAJ TO
            showAddSessionDialog = showAddSessionDialog,
            onDismissDialog = onDismissDialog,
            navController = navController
        )
        2 -> PlannerPage(modifier = modifier, viewModel = exerciseViewModel)
        else -> Text(text = "No Page Found", modifier = modifier)
    }
}
