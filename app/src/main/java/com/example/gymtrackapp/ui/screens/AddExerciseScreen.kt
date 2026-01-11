package com.example.gymtrackapp.ui.screens


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.gymtrackapp.ui.theme.AppBackground
import com.example.gymtrackapp.ui.theme.AppDivider
import com.example.gymtrackapp.ui.theme.AppGreen
import com.example.gymtrackapp.ui.theme.AppMutedText
import com.example.gymtrackapp.ui.theme.AppSurface
import com.example.gymtrackapp.ui.theme.AppShapes
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun AddExerciseScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    navController: NavHostController,
    exerciseViewModel: ExerciseViewModel,
    trainingViewModel: TrainingViewModel,
    statisticsViewModel: com.example.gymtrackapp.ui.viewmodel.StatisticsViewModel
) {
    val allExercises by exerciseViewModel.exercises.observeAsState(emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }

    val levels by exerciseViewModel.levels.observeAsState(emptyList())
    val equipments by exerciseViewModel.equipments.observeAsState(emptyList())
    val categories by exerciseViewModel.categories.observeAsState(emptyList())
    val mechanics by exerciseViewModel.mechanics.observeAsState(emptyList())
    val forces by exerciseViewModel.forces.observeAsState(emptyList())
    val primaryMuscles by exerciseViewModel.primaryMuscles.observeAsState(emptyList())
    val secondaryMuscles by exerciseViewModel.secondaryMuscles.observeAsState(emptyList())

    // Filtrowanie ćwiczeń na podstawie wyszukiwarki
    val filteredExercises = remember(allExercises, searchQuery) {
        if (searchQuery.isEmpty()) {
            allExercises
        } else {
            allExercises.filter { exercise ->
                exercise.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Root wypełnia obszar contentu (między topbarem i bottombarem w MainScreen)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        // Local header (jak w innych ekranach z wewnętrznym tytułem)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
            }
            Text(
                text = "Add exercise",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Szukaj ćwiczenia") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Szukaj")
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = AppShapes.button,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppGreen,
                    focusedLabelColor = AppGreen
                )
            )

            FilledTonalIconButton(
                onClick = {
                    exerciseViewModel.copySelectionsToTemp()
                    showFilterDialog = true
                },
                shape = AppShapes.button,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = AppSurface,
                    contentColor = AppGreen
                )
            ) {
                Icon(Icons.Default.Create, contentDescription = "Filtruj")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredExercises) { exercise ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.card,
                    colors = CardDefaults.cardColors(containerColor = AppSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    onClick = {
                        navController.navigate("exercise_detail/${exercise.id}?from=session&sessionId=$sessionId")
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = exercise.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Poziom: ${exercise.level}",
                            fontSize = 14.sp,
                            color = AppMutedText
                        )
                        Text(
                            text = "Sprzęt: ${exercise.equipment ?: "Brak"}",
                            fontSize = 14.sp,
                            color = AppMutedText
                        )
                        Text(
                            text = "Główne partie: ${exercise.primaryMuscles.joinToString()}",
                            fontSize = 12.sp,
                            color = AppMutedText
                        )
                    }
                }
            }
        }
    }

    // Dialog filtrowania (skopiowany z PlannerPage)
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            shape = AppShapes.dialog,
            title = {
                Text(
                    "Filtruj ćwiczenia",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MultiSelectDropdown("Główne mięśnie", primaryMuscles, exerciseViewModel.tempPrimaryMuscles) { exerciseViewModel.tempPrimaryMuscles = it }
                    MultiSelectDropdown("Poboczne mięśnie", secondaryMuscles, exerciseViewModel.tempSecondaryMuscles) { exerciseViewModel.tempSecondaryMuscles = it }
                    MultiSelectDropdown("Poziom", levels, exerciseViewModel.tempLevels) { exerciseViewModel.tempLevels = it }
                    MultiSelectDropdown("Sprzęt", equipments, exerciseViewModel.tempEquipments) { exerciseViewModel.tempEquipments = it }
                    MultiSelectDropdown("Kategoria", categories, exerciseViewModel.tempCategories) { exerciseViewModel.tempCategories = it }
                    MultiSelectDropdown("Mechanika", mechanics, exerciseViewModel.tempMechanics) { exerciseViewModel.tempMechanics = it }
                    MultiSelectDropdown("Siła", forces, exerciseViewModel.tempForces) { exerciseViewModel.tempForces = it }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        exerciseViewModel.confirmSelections()
                        showFilterDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
                    shape = AppShapes.button
                ) {
                    Text("Zastosuj", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showFilterDialog = false },
                    shape = AppShapes.button,
                    border = BorderStroke(1.dp, AppDivider)
                ) {
                    Text("Anuluj", color = AppMutedText)
                }
            }
        )
    }
}