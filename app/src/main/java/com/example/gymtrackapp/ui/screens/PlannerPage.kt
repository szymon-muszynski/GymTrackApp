package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.navigation.NavHostController
import com.example.gymtrackapp.data.entity.WorkoutTemplate
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel
import com.example.gymtrackapp.ui.viewmodel.TemplateViewModel

@Composable
fun PlannerPage(
    modifier: Modifier = Modifier,
    templateViewModel: TemplateViewModel,
    exerciseViewModel: ExerciseViewModel,
    navController: NavHostController
) {
    val templates by templateViewModel.templates.collectAsState()
    var showAddTemplateDialog by remember { mutableStateOf(false) }

    // Ładujemy szablony przy wejściu na ekran
    LaunchedEffect(Unit) {
        templateViewModel.loadTemplates()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTemplateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj szablon")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF90E39A))
                .padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (templates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Brak szablonów. Utwórz nowy!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(templates) { template ->
                        TemplateCard(
                            template = template,
                            templateViewModel = templateViewModel,
                            exerciseViewModel = exerciseViewModel,
                            onAddExercise = {
                                navController.navigate("add_template_exercise/${template.id}")
                            },
                            onDeleteTemplate = {
                                templateViewModel.deleteTemplate(template)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddTemplateDialog) {
        AddTemplateDialog(
            onDismiss = { showAddTemplateDialog = false },
            onConfirm = { name ->
                templateViewModel.createTemplate(name, null)
                showAddTemplateDialog = false
            }
        )
    }
}

@Composable
fun TemplateCard(
    template: WorkoutTemplate,
    templateViewModel: TemplateViewModel,
    exerciseViewModel: ExerciseViewModel,
    onAddExercise: () -> Unit,
    onDeleteTemplate: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val templateExercisesMap by templateViewModel.templateExercisesMap.collectAsState()

    val templateExercises = templateExercisesMap[template.id] ?: emptyList()

    val allExercises by exerciseViewModel.exercises.observeAsState(emptyList())

    // Ładujemy ćwiczenia dla tego szablonu, gdy karta jest rozwijana
    LaunchedEffect(template.id, isExpanded) {
        if (isExpanded) {
            templateViewModel.loadExercisesForTemplate(template.id)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(onClick = onDeleteTemplate) {
                    Icon(Icons.Default.Delete, contentDescription = "Usuń szablon", tint = Color.Gray)
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                if (templateExercises.isEmpty()) {
                    Text(
                        text = "Brak ćwiczeń w szablonie",
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color.Gray
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        templateExercises.forEach { templateExercise ->
                            val exercise = allExercises.find { it.id == templateExercise.exerciseId }
                            if (exercise != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = exercise.name,
                                        fontSize = 16.sp
                                    )
                                    IconButton(
                                        onClick = {
                                            templateViewModel.deleteTemplateExercise(templateExercise)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Usuń ćwiczenie",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddExercise,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Dodaj ćwiczenie")
                }
            }
        }
    }
}

@Composable
fun AddTemplateDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowy szablon treningowy") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nazwa szablonu") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name)
                    }
                }
            ) { Text("Utwórz") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

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
