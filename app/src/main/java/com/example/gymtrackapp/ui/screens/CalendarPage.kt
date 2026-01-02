package com.example.gymtrackapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch
import com.example.gymtrackapp.data.entity.TrainingSession
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel
import kotlin.collections.forEach
import androidx.compose.material3.CardDefaults
import com.example.gymtrackapp.ui.viewmodel.TemplateViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.example.gymtrackapp.data.entity.WorkoutTemplate
import com.example.gymtrackapp.ui.viewmodel.SharePostViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarPage(
    modifier: Modifier = Modifier,
    trainingViewModel: TrainingViewModel,
    exerciseViewModel: ExerciseViewModel,
    statisticsViewModel: com.example.gymtrackapp.ui.viewmodel.StatisticsViewModel,
    templateViewModel: TemplateViewModel,
    sharePostViewModel: SharePostViewModel,
    showAddSessionDialog: Boolean,
    onDismissDialog: () -> Unit,
    navController: NavHostController,
    initialDate: LocalDate? = null
) {
    var selectedDate by remember { mutableStateOf(initialDate ?: LocalDate.now()) }
    val sessions by trainingViewModel.sessions.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var sessionToEdit by remember { mutableStateOf<TrainingSession?>(null) }

    val templates by templateViewModel.templates.collectAsState()

    LaunchedEffect(Unit) {
        templateViewModel.loadTemplates()
    }

    LaunchedEffect(selectedDate) {
        trainingViewModel.loadSessionsForDate(selectedDate.toEpochDay())
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val shareMessage by sharePostViewModel.message.collectAsState()
    LaunchedEffect(shareMessage) {
        if (shareMessage != null) {
            snackbarHostState.showSnackbar(shareMessage!!)
            sharePostViewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFF9FBAE8)
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF9FBAE8)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalInfiniteCalendar(
                selected = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            SessionsList(
                trainingSessions = sessions,
                trainingViewModel = trainingViewModel,
                exerciseViewModel = exerciseViewModel,
                sharePostViewModel = sharePostViewModel,
                onEdit = { session ->
                    sessionToEdit = session
                    showEditDialog = true
                },
                onDelete = { session ->
                    trainingViewModel.deleteSession(session)
                },
                onAddExercise = { session ->
                    navController.navigate("add_exercise/${session.id}")
                },
                onExerciseClick = { sessionExerciseId, exerciseId ->
                    navController.navigate("set_details/$sessionExerciseId/$exerciseId")
                }
            )
        }
    }

    if (showAddSessionDialog) {
        AddSessionDialog(
            date = selectedDate,
            templates = templates,
            onDismiss = onDismissDialog,
            onCreateEmptySession = { description ->
                trainingViewModel.createEmptySession(
                    date = selectedDate.toEpochDay(),
                    description = description
                )
            },
            onCreateSessionFromTemplate = { templateId, description ->
                trainingViewModel.createSessionFromTemplate(
                    templateId = templateId,
                    date = selectedDate.toEpochDay(),
                    description = description
                )
            }
        )
    }

    if (showEditDialog && sessionToEdit != null) {
        EditSessionDialog(
            session = sessionToEdit!!,
            onDismiss = { showEditDialog = false },
            onConfirm = { newDescription ->
                trainingViewModel.updateSession(
                    sessionToEdit!!.copy(description = newDescription)
                )
                statisticsViewModel.refresh() // Odświeżamy statystyki
                showEditDialog = false
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HorizontalInfiniteCalendar(
    selected: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val centerIndex = 50_000
    val totalCount = 100_000
    val today = LocalDate.now()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = centerIndex)
    val scope = rememberCoroutineScope()

    LaunchedEffect(selected) {
        val offset = selected.toEpochDay() - today.toEpochDay()
        val targetIndex = (centerIndex + offset.toInt()).coerceIn(0, totalCount - 1)
        scope.launch {
            listState.animateScrollToItem(targetIndex)
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(count = totalCount) { index ->
            val dayOffset = index - centerIndex
            val date = today.plusDays(dayOffset.toLong())
            val isSelected = date == selected

            DayCard(
                date = date,
                selected = isSelected,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayCard(
    date: LocalDate,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFF2B6CB0) else Color.White
    val textColor = if (selected) Color.White else Color.Black
    Card(
        modifier = Modifier
            .height(88.dp)
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .background(bg)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                fontSize = 12.sp,
                color = textColor
            )
            Text(
                text = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                fontSize = 12.sp,
                color = textColor
            )
        }
    }
}

@Composable
fun SessionsList(
    trainingSessions: List<TrainingSession>,
    trainingViewModel: TrainingViewModel,
    exerciseViewModel: ExerciseViewModel,
    sharePostViewModel: SharePostViewModel,
    onEdit: (TrainingSession) -> Unit,
    onDelete: (TrainingSession) -> Unit,
    onAddExercise: (TrainingSession) -> Unit,
    onExerciseClick: (Long, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(trainingSessions) { session ->
            TrainingSessionItem(
                modifier = Modifier.padding(vertical = 4.dp),
                session = session,
                trainingViewModel = trainingViewModel,
                exerciseViewModel = exerciseViewModel,
                sharePostViewModel = sharePostViewModel,
                onEdit = onEdit,
                onDelete = onDelete,
                onAddExercise = onAddExercise,
                onExerciseClick = onExerciseClick
            )
        }
    }
}

@Composable
fun TrainingSessionItem(
    modifier: Modifier = Modifier,
    session: TrainingSession,
    trainingViewModel: TrainingViewModel,
    exerciseViewModel: ExerciseViewModel,
    sharePostViewModel: SharePostViewModel,
    onEdit: (TrainingSession) -> Unit = {},
    onDelete: (TrainingSession) -> Unit = {},
    onAddExercise: (TrainingSession) -> Unit = {},
    onExerciseClick: (Long, String) -> Unit = { _, _ -> }
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val sessionExercisesMap by trainingViewModel.sessionExercisesMap.collectAsState()
    val sessionExercises = sessionExercisesMap[session.id.toLong()] ?: emptyList()

    val allExercises by exerciseViewModel.exercises.observeAsState(emptyList())

    LaunchedEffect(session.id, isExpanded) {
        if (isExpanded) {
            trainingViewModel.loadExercisesForSession(session.id.toLong())
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6B9BD1) // ← Zmieniony kolor na ciemniejszy niebieski
        )
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = session.description,
                        fontWeight = FontWeight.Bold, // ← Pogrubienie
                        fontSize = 18.sp // ← Można opcjonalnie zwiększyć
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Button(onClick = { onAddExercise(session) }) {
                            Text("Add exercise")
                        }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Więcej")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edytuj") },
                                    onClick = {
                                        menuExpanded = false
                                        onEdit(session)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Usuń") },
                                    onClick = {
                                        menuExpanded = false
                                        onDelete(session)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Udostępnij") },
                                    onClick = {
                                        menuExpanded = false
                                        sharePostViewModel.publish(session.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (isExpanded) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    if (sessionExercises.isEmpty()) {
                        Text(
                            text = "Sesja jest pusta",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 15.sp // ← Zwiększona czcionka
                        )
                    } else {
                        Column {
                            sessionExercises.forEach { sessionExercise ->
                                val exercise = allExercises.find { it.id == sessionExercise.exerciseId }
                                if (exercise != null) {
                                    val sets by trainingViewModel.getSetsForSessionExercise(sessionExercise.id).collectAsState(initial = emptyList())

                                    LaunchedEffect(sessionExercise.id) {
                                        trainingViewModel.loadSetsForSessionExercise(sessionExercise.id)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onExerciseClick(sessionExercise.id, exercise.id)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = exercise.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp // ← Zwiększona czcionka
                                            )
                                            if (sets.isNotEmpty()) {
                                                Text(
                                                    text = sets.joinToString(", ") { "${it.weight}kg×${it.reps}" },
                                                    fontSize = 14.sp, // ← Zwiększona z 12sp
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                        IconButton(onClick = {
                                            trainingViewModel.deleteSessionExercise(sessionExercise)
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddSessionDialog(
    date: LocalDate,
    templates: List<WorkoutTemplate>,
    onDismiss: () -> Unit,
    onCreateEmptySession: (String) -> Unit,
    onCreateSessionFromTemplate: (Long, String) -> Unit
) {
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj sesję treningową") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis sesji (opcjonalnie)") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Divider()

                Text(
                    text = "Utwórz pustą sesję",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = {
                        onCreateEmptySession(description)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pusta sesja")
                }

                if (templates.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lub wybierz szablon",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        templates.forEach { template ->
                            OutlinedButton(
                                onClick = {
                                    onCreateSessionFromTemplate(template.id, description)
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(template.name)
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Brak szablonów. Dodaj je w zakładce Planner.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            // zostaw puste, korzystamy z przycisków w `text`
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
fun EditSessionDialog(
    session: TrainingSession,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var description by remember { mutableStateOf(session.description) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj nazwę sesji") },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Nowa nazwa") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(description) }
            ) { Text("Potwierdź") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

@Composable
fun AddSetDialog(
    exerciseName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(exerciseName) },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Enter data") }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
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