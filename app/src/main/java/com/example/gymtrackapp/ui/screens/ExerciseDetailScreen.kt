package com.example.gymtrackapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.gymtrackapp.data.entity.Exercise
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel
import com.example.gymtrackapp.ui.viewmodel.TemplateViewModel
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    from: String, // "session" lub "template"
    sessionId: Long? = null,
    templateId: Long? = null,
    onNavigateBack: () -> Unit,
    exerciseViewModel: ExerciseViewModel,
    trainingViewModel: TrainingViewModel,
    templateViewModel: TemplateViewModel
) {
    val allExercises by exerciseViewModel.exercises.observeAsState(emptyList())
    val exercise = allExercises.find { it.id == exerciseId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = exercise?.name ?: "Szczegóły ćwiczenia",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            // Sticky button na dole
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = {
                        when (from) {
                            "session" -> {
                                if (sessionId != null) {
                                    trainingViewModel.addExerciseToSession(sessionId, exerciseId)
                                }
                            }
                            "template" -> {
                                if (templateId != null) {
                                    templateViewModel.addExerciseToTemplate(templateId, exerciseId)
                                }
                            }
                        }
                        // Cofnij się 2 razy - z ExerciseDetailScreen -> AddExerciseScreen -> CalendarPage/PlannerPage
                        onNavigateBack() // pierwszy popBackStack
                        onNavigateBack() // drugi popBackStack
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Dodaj ćwiczenie",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        if (exercise == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Ćwiczenie nie znalezione")
            }
        } else {
            ExerciseDetailContent(
                exercise = exercise,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseDetailContent(
    exercise: Exercise,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Galeria zdjęć
        if (exercise.images.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { exercise.images.size })

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.LightGray)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val imageUrl = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/${exercise.images[page]}"

                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "${exercise.name} - zdjęcie ${page + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        },
                        error = {
                            // Fallback dla braku internetu
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Brak internetu",
                                        modifier = Modifier.size(64.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Brak zdjęć - brak internetu",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    )
                }

                // Wskaźniki (kropki) - custom implementation
                if (exercise.images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(exercise.images.size) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == index)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            Color.White.copy(alpha = 0.5f)
                                    )
                            )
                        }
                    }
                }
            }
        } else {
            // Brak zdjęć w danych
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Brak zdjęć dla tego ćwiczenia",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        // Informacje o ćwiczeniu
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Poziom trudności
            InfoRow(label = "Poziom", value = exercise.level.capitalize())

            // Sprzęt
            exercise.equipment?.let {
                InfoRow(label = "Sprzęt", value = it)
            }

            // Kategoria
            InfoRow(label = "Kategoria", value = exercise.category)

            // Mechanika
            exercise.mechanic?.let {
                InfoRow(label = "Mechanika", value = it)
            }

            // Force
            exercise.force?.let {
                InfoRow(label = "Typ ruchu", value = it)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Główne mięśnie
            if (exercise.primaryMuscles.isNotEmpty()) {
                Text(
                    text = "Główne mięśnie:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                ExerciseFlowRow(items = exercise.primaryMuscles, color = Color(0xFF4CAF50))
            }

            // Drugorzędne mięśnie
            if (exercise.secondaryMuscles.isNotEmpty()) {
                Text(
                    text = "Drugorzędne mięśnie:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                ExerciseFlowRow(items = exercise.secondaryMuscles, color = Color(0xFF2196F3))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Instrukcje
            if (exercise.instructions.isNotEmpty()) {
                Text(
                    text = "Jak wykonać?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                exercise.instructions.forEachIndexed { index, instruction ->
                    InstructionItem(number = index + 1, text = instruction)
                    if (index < exercise.instructions.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Padding na dole dla przycisku
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseFlowRow(items: List<String>, color: Color) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = color.copy(alpha = 0.2f),
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 13.sp,
                    color = color.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun InstructionItem(number: Int, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // Numer w kółku
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

