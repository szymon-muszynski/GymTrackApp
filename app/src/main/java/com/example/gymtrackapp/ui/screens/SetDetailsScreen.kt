package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        // 1. Wyłączamy systemowe insetsy, bo MainScreen już je obsłużył.
        // To naprawia błąd z "luką" nad nagłówkiem.
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            // 2. Stylizacja FABa: Zielony, zaokrąglony kwadrat
            FloatingActionButton(
                onClick = { showAddSetDialog = true },
                containerColor = AppGreen,
                contentColor = Color.White,
                shape = AppShapes.button // Kształt z szablonów (zaokrąglony kwadrat)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj serię")
            }
        },
        containerColor = AppBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppBackground)
        ) {
            // --- CUSTOM HEADER (spójny z ExerciseDetailScreen i AddExerciseScreen) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Padding identyczny jak w poprzednich ekranach
                    .padding(top = 16.dp, start = 8.dp, end = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                }
                Text(
                    text = exercise?.name ?: "Szczegóły serii",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    color = Color.Black
                )
            }

            // --- ZAWARTOŚĆ ---
            if (sets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Brak dodanych serii",
                        fontSize = 16.sp,
                        color = AppMutedText
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp), // padding po bokach dla listy
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // miejsce na FAB
                ) {
                    items(sets) { set ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = AppShapes.card,
                            colors = CardDefaults.cardColors(containerColor = AppSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${set.weight} kg × ${set.reps} powtórzeń",
                                        fontSize = 15.sp,
                                        color = Color(0xFF424242)
                                    )
                                }
                                IconButton(onClick = {
                                    trainingViewModel.deleteSet(set)
                                    statisticsViewModel.refresh()
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Usuń",
                                        tint = Color.Gray // Subtelniejszy kolor ikony usuwania
                                    )
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
                statisticsViewModel.refresh()
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
        shape = AppShapes.dialog, // Zaokrąglone rogi dialogu
        containerColor = Color.White,
        title = {
            Text(
                "Dodaj serię",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Obciążenie (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.button,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppGreen,
                        focusedLabelColor = AppGreen,
                        cursorColor = AppGreen
                    )
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Liczba powtórzeń") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.button,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppGreen,
                        focusedLabelColor = AppGreen,
                        cursorColor = AppGreen
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weight.toFloatOrNull() ?: 0f
                    val r = reps.toIntOrNull() ?: 0
                    onConfirm(w, r)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
                shape = AppShapes.button
            ) {
                Text("Dodaj", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = AppShapes.button,
                border = BorderStroke(1.dp, AppDivider)
            ) {
                Text("Anuluj", color = AppMutedText)
            }
        }
    )
}