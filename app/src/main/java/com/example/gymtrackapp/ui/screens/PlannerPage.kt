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
            FloatingActionButton(
                onClick = { showAddTemplateDialog = true },
                containerColor = Color(0xFF4CAF50)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Dodaj szablon",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            // Nagłówek
            PlannerPageHeader()

            if (templates.isEmpty()) {
                EmptyTemplatesPlaceholder(onAddTemplate = { showAddTemplateDialog = true })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
                    // Dodatkowy spacer na końcu
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
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
private fun PlannerPageHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Szablony Treningowe",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Twórz i zarządzaj swoimi planami treningowymi",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun EmptyTemplatesPlaceholder(onAddTemplate: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFFBDBDBD)
            )
            Text(
                text = "Brak szablonów treningowych",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            Text(
                text = "Utwórz swój pierwszy szablon, aby szybko planować treningi",
                fontSize = 14.sp,
                color = Color(0xFF757575),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onAddTemplate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dodaj szablon")
            }
        }
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Nagłówek karty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${templateExercises.size} ${if (templateExercises.size == 1) "ćwiczenie" else if (templateExercises.size in 2..4) "ćwiczenia" else "ćwiczeń"}",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDeleteTemplate) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Usuń szablon",
                            tint = Color(0xFFE57373)
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(12.dp))

                if (templateExercises.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Brak ćwiczeń w szablonie",
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = Color(0xFF9E9E9E),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        templateExercises.forEach { templateExercise ->
                            val exercise = allExercises.find { it.id == templateExercise.exerciseId }
                            if (exercise != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = exercise.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF424242)
                                            )
                                            Text(
                                                text = exercise.primaryMuscles.joinToString(", "),
                                                fontSize = 12.sp,
                                                color = Color(0xFF757575)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                templateViewModel.deleteTemplateExercise(templateExercise)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Usuń ćwiczenie",
                                                tint = Color(0xFFE57373),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddExercise,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dodaj ćwiczenie", fontSize = 15.sp, fontWeight = FontWeight.Medium)
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        title = {
            Text(
                "Nowy szablon treningowy",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Nadaj nazwę swojemu szablonowi treningowemu",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa szablonu") },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        focusedLabelColor = Color(0xFF4CAF50)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Utwórz", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Text("Anuluj", color = Color(0xFF757575))
            }
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
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
