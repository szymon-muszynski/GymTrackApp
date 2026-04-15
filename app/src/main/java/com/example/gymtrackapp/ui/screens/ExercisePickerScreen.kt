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

/**
 * Wspólny ekran wyboru/wyszukiwania ćwiczeń dla:
 * - dodawania do sesji (from=session + sessionId)
 * - dodawania do templatu (from=template + templateId)
 *
 * Analogicznie do ExerciseDetailScreen używamy parametru `from` + opcjonalnych ID.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerScreen(
    from: String, // "session" | "template"
    sessionId: Long? = null,
    templateId: Long? = null,
    onNavigateBack: () -> Unit,
    navController: NavHostController,
    exerciseViewModel: ExerciseViewModel
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

    val filteredExercises = remember(allExercises, searchQuery) {
        if (searchQuery.isBlank()) allExercises
        else allExercises.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        // Header identyczny jak w AddExerciseScreen (spójność wizualna)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                label = { Text("Search exercise") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
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
                Icon(Icons.Default.Create, contentDescription = "Filter")
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
                        val route = when (from) {
                            "template" -> "exercise_detail/${exercise.id}?from=template&templateId=$templateId"
                            else -> "exercise_detail/${exercise.id}?from=session&sessionId=$sessionId"
                        }
                        navController.navigate(route)
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = exercise.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Level: ${exercise.level}",
                            fontSize = 14.sp,
                            color = AppMutedText
                        )
                        Text(
                            text = "Equipment: ${exercise.equipment ?: "None"}",
                            fontSize = 14.sp,
                            color = AppMutedText
                        )
                        Text(
                            text = "Primary muscles: ${exercise.primaryMuscles.joinToString()}",
                            fontSize = 12.sp,
                            color = AppMutedText
                        )
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            shape = AppShapes.dialog,
            title = { Text("Filter exercises", fontWeight = FontWeight.Bold) },
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
                Button(
                    onClick = {
                        exerciseViewModel.confirmSelections()
                        showFilterDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
                    shape = AppShapes.button
                ) {
                    Text("Apply", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showFilterDialog = false },
                    shape = AppShapes.button,
                    border = BorderStroke(1.dp, AppDivider)
                ) {
                    Text("Cancel", color = AppMutedText)
                }
            }
        )
    }
}
