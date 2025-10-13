package com.example.gymtrackapp.ui.screens


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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtrackapp.data.entity.Exercise
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    exerciseViewModel: ExerciseViewModel,
    trainingViewModel: TrainingViewModel
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add exercise") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Wiersz z wyszukiwarką i przyciskiem filtra
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search for an exercise") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Szukaj")
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        exerciseViewModel.copySelectionsToTemp()
                        showFilterDialog = true
                    }
                ) {
                    Icon(Icons.Default.Create, contentDescription = "Filtruj")
                }
            }

            // Lista ćwiczeń
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredExercises) { exercise ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            trainingViewModel.addExerciseToSession(sessionId, exercise.id)
                            onNavigateBack()
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = exercise.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Advance level: ${exercise.level}",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Equipment: ${exercise.equipment ?: "None"}",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Main muscle part: ${exercise.primaryMuscles.joinToString()}",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog filtrowania (skopiowany z PlannerPage)
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter exercises") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MultiSelectDropdown("Primary muscles", primaryMuscles, exerciseViewModel.tempPrimaryMuscles) { exerciseViewModel.tempPrimaryMuscles = it }
                    MultiSelectDropdown("Secondary muscles", secondaryMuscles, exerciseViewModel.tempSecondaryMuscles) { exerciseViewModel.tempSecondaryMuscles = it }
                    MultiSelectDropdown("Level", levels, exerciseViewModel.tempLevels) { exerciseViewModel.tempLevels = it }
                    MultiSelectDropdown("Equipment", equipments, exerciseViewModel.tempEquipments) { exerciseViewModel.tempEquipments = it }
                    MultiSelectDropdown("Category", categories, exerciseViewModel.tempCategories) { exerciseViewModel.tempCategories = it }
                    MultiSelectDropdown("Mechanics", mechanics, exerciseViewModel.tempMechanics) { exerciseViewModel.tempMechanics = it }
                    MultiSelectDropdown("Force", forces, exerciseViewModel.tempForces) { exerciseViewModel.tempForces = it }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    exerciseViewModel.confirmSelections()
                    showFilterDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}