package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.gymtrackapp.ui.theme.AppBackground
import com.example.gymtrackapp.ui.theme.AppDivider
import com.example.gymtrackapp.ui.theme.AppGreen
import com.example.gymtrackapp.ui.theme.AppMutedText
import com.example.gymtrackapp.ui.theme.AppSurface
import com.example.gymtrackapp.ui.theme.AppShapes
import com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel
import com.example.gymtrackapp.ui.viewmodel.TemplateViewModel
import com.example.gymtrackapp.ui.viewmodel.TrainingViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
        // KLUCZOWA POPRAWKA: Wyłączamy domyślne insetsy (odstępy systemowe),
        // ponieważ ten ekran jest już wewnątrz innego Scaffolda w MainScreen.
        // To usuwa niechcianą lukę na górze.
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = AppBackground
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
                        onNavigateBack()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
                    shape = AppShapes.button
                ) {
                    Text(
                        text = "Add exercise",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
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
            // --- NAGŁÓWEK ---
            // Ustawiony idealnie tak jak w AddExerciseScreen.
            // Tam jest padding(16.dp) na rodzicu, tutaj dajemy go bezpośrednio na Row.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = exercise?.name ?: "Exercise details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            // --- ZAWARTOŚĆ ---
            if (exercise == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Exercise not found", color = AppMutedText)
                }
            } else {
                ExerciseDetailContent(
                    exercise = exercise,
                    modifier = Modifier.weight(1f)
                )
            }
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
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        // Galeria zdjęć
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = AppShapes.card,
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (exercise.images.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { exercise.images.size })

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(AppSurface)
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
                            contentDescription = "${exercise.name} - image ${page + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AppGreen)
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(AppSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "No internet",
                                            modifier = Modifier.size(48.dp),
                                            tint = AppMutedText
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No images - no internet connection",
                                            color = AppMutedText,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        )
                    }

                    if (exercise.images.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(exercise.images.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (pagerState.currentPage == index) AppGreen
                                            else Color.Black.copy(alpha = 0.15f)
                                        )
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(AppSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No images for this exercise",
                        color = AppMutedText,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Sekcja informacji
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = AppShapes.card,
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(label = "Level", value = exercise.level.capitalize())
                exercise.equipment?.let { InfoRow(label = "Equipment", value = it) }
                InfoRow(label = "Category", value = exercise.category)
                exercise.mechanic?.let { InfoRow(label = "Mechanic", value = it) }
                exercise.force?.let { InfoRow(label = "Force", value = it) }
            }
        }

        // Mięśnie
        if (exercise.primaryMuscles.isNotEmpty() || exercise.secondaryMuscles.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = AppShapes.card,
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (exercise.primaryMuscles.isNotEmpty()) {
                        Text(
                            text = "Primary muscles",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        ExerciseFlowRow(items = exercise.primaryMuscles, color = AppGreen)
                    }

                    if (exercise.secondaryMuscles.isNotEmpty()) {
                        Text(
                            text = "Secondary muscles",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        ExerciseFlowRow(items = exercise.secondaryMuscles, color = AppGreen.copy(alpha = 0.75f))
                    }
                }
            }
        }

        // Instrukcje
        if (exercise.instructions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = AppShapes.card,
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "How to perform?",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = AppGreen
                    )

                    exercise.instructions.forEachIndexed { index, instruction ->
                        InstructionItem(number = index + 1, text = instruction)
                        if (index < exercise.instructions.size - 1) {
                            HorizontalDivider(color = AppDivider)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
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
            color = AppMutedText
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
                shape = AppShapes.button,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 13.sp,
                    color = color,
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
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(AppGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AppGreen
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