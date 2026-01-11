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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymtrackapp.di.LocalAppContainer
import com.example.gymtrackapp.ui.viewmodel.AuthViewModel
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel
import com.example.gymtrackapp.ui.viewmodel.ExploreFeedViewModel
import com.example.gymtrackapp.ui.viewmodel.FriendsViewModel
import com.example.gymtrackapp.ui.viewmodel.MyProfileViewModel
import com.example.gymtrackapp.ui.viewmodel.MyProfileViewModelFactory
import com.example.gymtrackapp.ui.viewmodel.PlannerViewModel
import com.example.gymtrackapp.ui.viewmodel.PlannerViewModelFactory
import com.example.gymtrackapp.ui.viewmodel.SharePostViewModel
import com.example.gymtrackapp.ui.viewmodel.StatisticsViewModel
import com.example.gymtrackapp.ui.viewmodel.TemplateViewModel
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModel
import com.example.gymtrackapp.ui.viewmodel.UserProfileViewModel
import com.example.gymtrackapp.ui.viewmodel.UserProfileViewModelFactory
import com.example.gymtrackapp.utils.NetworkStatus
import com.example.gymtrackapp.ui.theme.AppGreen

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    exerciseViewModel: ExerciseViewModel,
    trainingViewModel: TrainingViewModel,
    templateViewModel: TemplateViewModel,
    authViewModel: AuthViewModel,
    statisticsViewModel: StatisticsViewModel,
    friendsViewModel: FriendsViewModel,
    exploreFeedViewModel: ExploreFeedViewModel,
    sharePostViewModel: SharePostViewModel,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    var showAddSessionDialog by remember { mutableStateOf(false) }
    var showMonthlyCalendar by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var showOfflineLogoutDialog by remember { mutableStateOf(false) }

    if (showOfflineLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showOfflineLogoutDialog = false },
            confirmButton = {
                TextButton(onClick = { showOfflineLogoutDialog = false }) {
                    Text("OK")
                }
            },
            title = { Text("Brak internetu") },
            text = {
                Text(
                    "Wylogowanie zostało wyłączone, gdy nie ma internetu, aby zapobiec utracie danych, " +
                        "które mogłyby nie zostać zsynchronizowane z chmurą.\n\n" +
                        "Połącz się z internetem i spróbuj ponownie."
                )
            }
        )
    }

    // Kick initial loads for HomePage (best effort)
    LaunchedEffect(Unit) {
        statisticsViewModel.setHeatmapDaysRange(7)
        trainingViewModel.loadRecentSessions(3)
    }

    Scaffold(
        topBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            TopAppBar(
                title = { Text("Gym Track App") },
                actions = {
                    if (currentRoute == "calendar" || currentRoute?.startsWith("calendar/") == true) {
                        IconButton(onClick = { showMonthlyCalendar = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Kalendarz miesięczny")
                        }
                    }

                    IconButton(onClick = {
                        if (NetworkStatus.isOnline(context)) {
                            authViewModel.signOut()
                            onSignOut()
                        } else {
                            showOfflineLogoutDialog = true
                        }
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
            if (currentRoute == "calendar" || currentRoute == "calendar/{date}") {
                FloatingActionButton(
                    onClick = { showAddSessionDialog = true },
                    containerColor = AppGreen
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Dodaj sesję",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        val hostModifier = Modifier.padding(paddingValues)

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = hostModifier
        ) {
            composable("home") {
                val currentUser by authViewModel.currentUser.collectAsState()
                val uid = currentUser?.uid

                val planningRepository = LocalAppContainer.current.planningRepository
                val plannerViewModel: PlannerViewModel = viewModel(
                    key = "planner_${uid}",
                    factory = PlannerViewModelFactory(planningRepository)
                )

                // Wyświetl email użytkownika
                val userName = currentUser?.email

                HomePage(
                    onNavigateToStatistics = { navController.navigate("progress") },
                    onNavigateToPlans = { navController.navigate("planner") },
                    onNavigateToSettings = { navController.navigate("profile") },
                    onAddWorkoutSession = {
                        showAddSessionDialog = true
                        navController.navigate("calendar")
                    },
                    onNavigateToSession = { sessionId, epochDay ->
                        // Konwertuj epochDay (dni od 1970) na timestamp w milisekundach
                        val timestampMillis = epochDay * 24 * 60 * 60 * 1000
                        navController.navigate("calendar/$timestampMillis")
                    },
                    userName = userName,
                    statisticsViewModel = statisticsViewModel,
                    trainingViewModel = trainingViewModel,
                    plannerViewModel = plannerViewModel,
                    uid = uid,
                    // padding już jest na poziomie NavHost
                    modifier = Modifier
                )
            }
            composable("calendar/{date}") { backStackEntry ->
                // Odczyt daty z argumentów (Long timestamp w milisekundach)
                val dateArg = backStackEntry.arguments?.getString("date")?.toLongOrNull()

                // Konwertuj timestamp na LocalDate
                val initialDate = dateArg?.let { timestampMillis ->
                    java.time.Instant.ofEpochMilli(timestampMillis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                }

                // Reset dialog state when navigating away
                DisposableEffect(Unit) {
                    onDispose {
                        showAddSessionDialog = false
                    }
                }

                CalendarPage(
                    modifier = Modifier.fillMaxSize(),
                    trainingViewModel = trainingViewModel,
                    exerciseViewModel = exerciseViewModel,
                    statisticsViewModel = statisticsViewModel,
                    templateViewModel = templateViewModel,
                    sharePostViewModel = sharePostViewModel,
                    showAddSessionDialog = showAddSessionDialog,
                    onDismissDialog = { showAddSessionDialog = false },
                    navController = navController,
                    initialDate = initialDate,
                    showMonthlyCalendar = showMonthlyCalendar,
                    onDismissMonthlyCalendar = { showMonthlyCalendar = false }
                )
            }
            composable("calendar") {
                DisposableEffect(Unit) {
                    onDispose {
                        showAddSessionDialog = false
                    }
                }

                CalendarPage(
                    modifier = Modifier.fillMaxSize(),
                    trainingViewModel = trainingViewModel,
                    exerciseViewModel = exerciseViewModel,
                    statisticsViewModel = statisticsViewModel,
                    templateViewModel = templateViewModel,
                    sharePostViewModel = sharePostViewModel,
                    showAddSessionDialog = showAddSessionDialog,
                    onDismissDialog = { showAddSessionDialog = false },
                    navController = navController,
                    showMonthlyCalendar = showMonthlyCalendar,
                    onDismissMonthlyCalendar = { showMonthlyCalendar = false }
                )
            }
            composable("planner") {
                PlannerPage(
                    templateViewModel = templateViewModel,
                    exerciseViewModel = exerciseViewModel,
                    navController = navController,
                    modifier = Modifier
                )
            }
            composable("progress") {
                ProgressPage(
                    viewModel = statisticsViewModel,
                    exerciseViewModel = exerciseViewModel,
                    modifier = Modifier
                )
            }
            composable("friends") {
                FriendsPage(
                    friendsViewModel = friendsViewModel,
                    exploreFeedViewModel = exploreFeedViewModel,
                    onUserClick = { userId -> navController.navigate("user_profile/$userId") },
                    modifier = Modifier
                )
            }

            composable(
                route = "user_profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable

                val socialRepository = LocalAppContainer.current.socialRepository

                val vm: UserProfileViewModel = viewModel(
                    key = "user_profile_${userId}",
                    factory = UserProfileViewModelFactory(socialRepository, userId)
                )

                UserProfileScreen(viewModel = vm)
            }

            composable("profile") {
                val currentUser by authViewModel.currentUser.collectAsState()
                val uid = currentUser?.uid

                if (uid == null) {
                    Text("Brak zalogowanego użytkownika")
                } else {
                    val appContainer = LocalAppContainer.current
                    val socialRepository = appContainer.socialRepository
                    val networkMonitor = appContainer.networkMonitor

                    val vm: MyProfileViewModel = viewModel(
                        key = "my_profile_${uid}",
                        factory = MyProfileViewModelFactory(socialRepository, uid, networkMonitor)
                    )

                    ProfilePage(
                        viewModel = vm,
                        modifier = Modifier
                    )
                }
            }

            composable(
                route = "exercise_detail/{exerciseId}?from={from}&sessionId={sessionId}&templateId={templateId}",
                arguments = listOf(
                    navArgument("exerciseId") {
                        type = NavType.StringType
                    },
                    navArgument("from") {
                        type = NavType.StringType
                        defaultValue = "session"
                    },
                    navArgument("sessionId") {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument("templateId") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
                val from = backStackEntry.arguments?.getString("from") ?: "session"
                val sessionId = backStackEntry.arguments?.getString("sessionId")?.toLongOrNull()
                val templateId = backStackEntry.arguments?.getString("templateId")?.toLongOrNull()

                ExerciseDetailScreen(
                    exerciseId = exerciseId,
                    from = from,
                    sessionId = sessionId,
                    templateId = templateId,
                    onNavigateBack = { navController.popBackStack() },
                    exerciseViewModel = exerciseViewModel,
                    trainingViewModel = trainingViewModel,
                    templateViewModel = templateViewModel
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

            composable("custom_exercise_details/{exerciseId}") { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
                CustomExerciseDetailsScreen(
                    exerciseId = exerciseId,
                    onNavigateBack = { navController.popBackStack() },
                    exerciseViewModel = exerciseViewModel
                )
            }

            composable(
                route = "exercise_picker?from={from}&sessionId={sessionId}&templateId={templateId}",
                arguments = listOf(
                    navArgument("from") {
                        type = NavType.StringType
                        defaultValue = "session"
                    },
                    navArgument("sessionId") {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument("templateId") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val from = backStackEntry.arguments?.getString("from") ?: "session"
                val sessionId = backStackEntry.arguments?.getString("sessionId")?.toLongOrNull()
                val templateId = backStackEntry.arguments?.getString("templateId")?.toLongOrNull()

                ExercisePickerScreen(
                    from = from,
                    sessionId = sessionId,
                    templateId = templateId,
                    onNavigateBack = { navController.popBackStack() },
                    navController = navController,
                    exerciseViewModel = exerciseViewModel
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
                            if (route == "home") {
                                popUpTo(0) {
                                    inclusive = false
                                }
                            } else {
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
