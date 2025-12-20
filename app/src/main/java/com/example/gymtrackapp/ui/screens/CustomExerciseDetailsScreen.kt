package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomExerciseDetailsScreen(
    exerciseId: String,
    onNavigateBack: () -> Unit,
    exerciseViewModel: ExerciseViewModel
) {
    val exercise = exerciseViewModel.customExerciseDetails.observeAsState().value

    LaunchedEffect(exerciseId) {
        exerciseViewModel.loadExerciseDetails(exerciseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = exercise?.name ?: "Ćwiczenie",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (exercise == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Nie znaleziono ćwiczenia",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Podstawowe informacje",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            HorizontalDivider(color = Color(0xFFE0E0E0))
                            Spacer(modifier = Modifier.height(4.dp))

                            Text("Poziom: ${exercise.level}")
                            Text("Kategoria: ${exercise.category}")
                            Text("Sprzęt: ${exercise.equipment ?: "Brak"}")
                            Text("Mechanika: ${exercise.mechanic ?: "Brak"}")
                            Text("Force: ${exercise.force ?: "Brak"}")
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Mięśnie",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            HorizontalDivider(color = Color(0xFFE0E0E0))
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Główne: ${exercise.primaryMuscles.joinToString(", ").ifBlank { "Brak" }}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Pomocnicze: ${exercise.secondaryMuscles.joinToString(", ").ifBlank { "Brak" }}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Instrukcje",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            HorizontalDivider(color = Color(0xFFE0E0E0))
                            Spacer(modifier = Modifier.height(4.dp))

                            if (exercise.instructions.isEmpty()) {
                                Text(text = "Brak instrukcji", color = Color.Gray)
                            } else {
                                exercise.instructions.forEachIndexed { idx, line ->
                                    Text(text = "${idx + 1}. $line")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
