package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel

@Composable
fun PlannerPage(modifier: Modifier = Modifier, viewModel: ExerciseViewModel) {
    val exercises by viewModel.exercises.observeAsState(emptyList())
    var showDialog by remember { mutableStateOf(false) }

    val levels by viewModel.levels.observeAsState(emptyList())
    val equipments by viewModel.equipments.observeAsState(emptyList())
    val categories by viewModel.categories.observeAsState(emptyList())
    val mechanics by viewModel.mechanics.observeAsState(emptyList())
    val forces by viewModel.forces.observeAsState(emptyList())
    val primaryMuscles by viewModel.primaryMuscles.observeAsState(emptyList())
    val secondaryMuscles by viewModel.secondaryMuscles.observeAsState(emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF90E39A)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ExerciseFilter(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            onClick = {
                // Przy otwarciu dialogu kopiujemy aktualne wartości
                viewModel.copySelectionsToTemp()
                showDialog = true
            },
        )

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Filter exercises") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MultiSelectDropdown("Primary muscles", primaryMuscles, viewModel.tempPrimaryMuscles) { viewModel.tempPrimaryMuscles = it }
                        MultiSelectDropdown("Secondary muscles", secondaryMuscles, viewModel.tempSecondaryMuscles) { viewModel.tempSecondaryMuscles = it }
                        MultiSelectDropdown("Poziom", levels, viewModel.tempLevels) { viewModel.tempLevels = it }
                        MultiSelectDropdown("Sprzęt", equipments, viewModel.tempEquipments) { viewModel.tempEquipments = it }
                        MultiSelectDropdown("Kategoria", categories, viewModel.tempCategories) { viewModel.tempCategories = it }
                        MultiSelectDropdown("Mechanika", mechanics, viewModel.tempMechanics) { viewModel.tempMechanics = it }
                        MultiSelectDropdown("Force", forces, viewModel.tempForces) { viewModel.tempForces = it }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.confirmSelections()
                        showDialog = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Anuluj")
                    }
                }
            )
        }

        when {
            exercises.isEmpty() -> {
                Text(
                    text = "Brak ćwiczeń dla wybranych kryteriów",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(exercises) { exercise ->
                        Card(
                            modifier = Modifier.padding(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = exercise.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = "Poziom: ${exercise.level}", fontSize = 14.sp)
                                Text(text = "Sprzęt: ${exercise.equipment ?: "Brak"}", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseFilter(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(modifier = modifier, onClick = onClick) {
        Text(text = "Filter Exercises")
    }
}

/**
 * 🔹 Dropdown z wielokrotnym wyborem (checkboxy)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectDropdown(
    label: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var localSelected by remember { mutableStateOf(selectedOptions.toMutableSet()) }

    // 🔹 Synchronizacja – żeby wartości w dropdownie były aktualne po otwarciu dialogu
    LaunchedEffect(selectedOptions) {
        localSelected = selectedOptions.toMutableSet()
    }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = "$label (${localSelected.size} wybrane)",
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    val isChecked = option in localSelected
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        localSelected = localSelected.toMutableSet().apply {
                                            if (checked) add(option) else remove(option)
                                        }
                                        onSelectionChanged(localSelected)
                                    }
                                )
                                Text(option)
                            }
                        },
                        onClick = {}
                    )
                }
            }
        }
    }
}
