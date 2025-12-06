package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetDetailsScreen(
    sessionExerciseId: Long,
    exerciseId: String,
    onNavigateBack: () -> Unit,
    exerciseViewModel: ExerciseViewModel,
    trainingViewModel: TrainingViewModel,
    statisticsViewModel: com.example.gymtrackapp.ui.viewmodel.StatisticsViewModel
) {
    val allExercises by exerciseViewModel.exercises.observeAsState(emptyList())
    val exercise = allExercises.find { it.id == exerciseId }

    val sets by trainingViewModel.getSetsForSessionExercise(sessionExerciseId).collectAsState(initial = emptyList())

    var showAddSetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sessionExerciseId) {
        trainingViewModel.loadSetsForSessionExercise(sessionExerciseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = exercise?.name ?: "Ćwiczenie",
                        fontWeight = FontWeight.Bold // ← Pogrubienie tytułu
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSetDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj serię")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (sets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Brak serii",
                        fontSize = 20.sp, // ← Zwiększona czcionka
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sets) { set ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Seria ${set.order + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp // ← Zwiększona czcionka
                                    )
                                    Text(
                                        text = "${set.weight} kg × ${set.reps} powtórzeń",
                                        fontSize = 16.sp // ← Zwiększona czcionka
                                    )
                                }
                                IconButton(onClick = {
                                    trainingViewModel.deleteSet(set)
                                    statisticsViewModel.refresh() // Odświeżamy statystyki
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Usuń")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSetDialog) {
        AddSetInputDialog(
            onDismiss = { showAddSetDialog = false },
            onConfirm = { weight, reps ->
                trainingViewModel.addSetToSessionExercise(sessionExerciseId, weight, reps)
                statisticsViewModel.refresh() // Odświeżamy statystyki
                showAddSetDialog = false
            }
        )
    }
}


@Composable
fun AddSetInputDialog(
    onDismiss: () -> Unit,
    onConfirm: (Float, Int) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj serię") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Obciążenie (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Liczba powtórzeń") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val w = weight.toFloatOrNull() ?: 0f
                    val r = reps.toIntOrNull() ?: 0
                    onConfirm(w, r)
                }
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
