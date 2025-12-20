package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.gymtrackapp.data.entity.Exercise
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
    var selectedTab by remember { mutableStateOf(0) } // 0=templates, 1=custom exercises

    // templates state
    val templates by templateViewModel.templates.collectAsState()
    var showAddTemplateDialog by remember { mutableStateOf(false) }

    // custom exercises state
    val customExercises by exerciseViewModel.customExercises.observeAsState(emptyList())
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }

    // 2adujemy szablony przy wej2bciu na ekran
    LaunchedEffect(Unit) {
        templateViewModel.loadTemplates()
    }

    Scaffold(
        floatingActionButton = {
            when (selectedTab) {
                0 -> {
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

                1 -> {
                    FloatingActionButton(
                        onClick = { showAddExerciseDialog = true },
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Dodaj ćwiczenie",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            // Tab Row z przyciskami (jak w Progress)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    text = "Szablony",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Ćwiczenia",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }

            when (selectedTab) {
                0 -> {
                    // Nag13wek
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
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }

                1 -> {
                    CustomExercisesHeader()

                    if (customExercises.isEmpty()) {
                        EmptyCustomExercisesPlaceholder(onAddExercise = { showAddExerciseDialog = true })
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(customExercises, key = { it.id }) { exercise ->
                                CustomExerciseCard(
                                    exercise = exercise,
                                    onClick = {
                                        navController.navigate("custom_exercise_details/${exercise.id}")
                                    },
                                    onDelete = {
                                        exerciseToDelete = exercise
                                        showDeleteConfirm = true
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
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

    if (showAddExerciseDialog) {
        AddCustomExerciseDialog(
            exerciseViewModel = exerciseViewModel,
            onDismiss = {
                exerciseViewModel.clearCustomExerciseError()
                showAddExerciseDialog = false
            },
            onSaved = {
                exerciseViewModel.clearCustomExerciseError()
                showAddExerciseDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        val ex = exerciseToDelete
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                exerciseToDelete = null
            },
            title = { Text("Usuń ćwiczenie?") },
            text = {
                Text("Czy na pewno chcesz usunąć ćwiczenie \"${ex?.name ?: ""}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (ex != null) {
                            exerciseViewModel.deleteCustomExercise(ex)
                        }
                        showDeleteConfirm = false
                        exerciseToDelete = null
                    }
                ) {
                    Text("Usuń", color = Color(0xFFE57373), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        exerciseToDelete = null
                    }
                ) {
                    Text("Anuluj")
                }
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
private fun CustomExercisesHeader() {
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
                text = "Twoje ćwiczenia",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Twórz i zarządzaj własnymi ćwiczeniami",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun EmptyCustomExercisesPlaceholder(onAddExercise: () -> Unit) {
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
                text = "Brak własnych ćwiczeń",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            Text(
                text = "Utwórz swoje pierwsze ćwiczenie, aby używać go w treningach",
                fontSize = 14.sp,
                color = Color(0xFF757575),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onAddExercise,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dodaj ćwiczenie")
            }
        }
    }
}

@Composable
private fun CustomExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exercise.primaryMuscles.joinToString(", ").ifBlank { "Brak mięśni" },
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Usuń ćwiczenie",
                    tint = Color(0xFFE57373)
                )
            }
        }
    }
}

@Composable
private fun AddCustomExerciseDialog(
    exerciseViewModel: ExerciseViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    val levels by exerciseViewModel.levels.observeAsState(emptyList())
    val categories by exerciseViewModel.categories.observeAsState(emptyList())
    val equipments by exerciseViewModel.equipments.observeAsState(emptyList())
    val mechanics by exerciseViewModel.mechanics.observeAsState(emptyList())
    val forces by exerciseViewModel.forces.observeAsState(emptyList())
    val primaryMuscles by exerciseViewModel.primaryMuscles.observeAsState(emptyList())

    var selectedLevel by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedEquipment by remember { mutableStateOf<String?>(null) }
    var selectedMechanic by remember { mutableStateOf<String?>(null) }
    var selectedForce by remember { mutableStateOf<String?>(null) }
    var selectedPrimaryMuscle by remember { mutableStateOf<String?>(null) }

    var instructionsText by remember { mutableStateOf("") }

    // Zamknij dialog dopiero gdy operacja zakończy się sukcesem (bez errora)
    val error by exerciseViewModel.customExerciseError.observeAsState(null)
    var saveRequested by remember { mutableStateOf(false) }

    LaunchedEffect(saveRequested, error) {
        if (saveRequested && error.isNullOrBlank()) {
            onSaved()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        title = {
            Text(
                "Nowe ćwiczenie",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            // Scroll, bo pól jest sporo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!error.isNullOrBlank()) {
                    Text(text = error!!, color = Color(0xFFE57373), fontWeight = FontWeight.Medium)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        focusedLabelColor = Color(0xFF4CAF50)
                    )
                )

                SingleSelectDropdown(
                    label = "Poziom",
                    options = levels,
                    selected = selectedLevel,
                    onSelectedChange = { selectedLevel = it },
                    allowNone = false
                )

                SingleSelectDropdown(
                    label = "Kategoria",
                    options = categories,
                    selected = selectedCategory,
                    onSelectedChange = { selectedCategory = it },
                    allowNone = false
                )

                SingleSelectDropdown(
                    label = "Sprzęt",
                    options = equipments,
                    selected = selectedEquipment,
                    onSelectedChange = { selectedEquipment = it },
                    allowNone = true
                )

                SingleSelectDropdown(
                    label = "Mechanika",
                    options = mechanics,
                    selected = selectedMechanic,
                    onSelectedChange = { selectedMechanic = it },
                    allowNone = true
                )

                SingleSelectDropdown(
                    label = "Force",
                    options = forces,
                    selected = selectedForce,
                    onSelectedChange = { selectedForce = it },
                    allowNone = true
                )

                SingleSelectDropdown(
                    label = "Primary muscle",
                    options = primaryMuscles,
                    selected = selectedPrimaryMuscle,
                    onSelectedChange = { selectedPrimaryMuscle = it },
                    allowNone = false
                )

                OutlinedTextField(
                    value = instructionsText,
                    onValueChange = { instructionsText = it },
                    label = { Text("Instrukcje (każda linia = krok)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    saveRequested = true
                    val instructions = instructionsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    val primary = selectedPrimaryMuscle?.let { listOf(it) } ?: emptyList()

                    exerciseViewModel.createCustomExercise(
                        name = name,
                        level = selectedLevel ?: "beginner",
                        category = selectedCategory ?: "strength",
                        equipment = selectedEquipment,
                        primaryMuscles = primary,
                        secondaryMuscles = emptyList(),
                        instructions = instructions,
                        force = selectedForce,
                        mechanic = selectedMechanic,
                        createdByUserId = null
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text("Zapisz", fontWeight = FontWeight.Medium)
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
private fun SingleSelectDropdown(
    label: String,
    options: List<String>,
    selected: String?,
    onSelectedChange: (String?) -> Unit,
    allowNone: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        val display = selected ?: if (allowNone) "Brak" else "Wybierz"

        TextField(
            value = display,
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
            if (allowNone) {
                DropdownMenuItem(
                    text = { Text("Brak") },
                    onClick = {
                        onSelectedChange(null)
                        expanded = false
                    }
                )
            }

            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectedChange(option)
                        expanded = false
                    }
                )
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
