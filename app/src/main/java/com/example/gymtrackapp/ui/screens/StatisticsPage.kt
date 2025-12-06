package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProgressPage(
    viewModel: com.example.gymtrackapp.ui.viewmodel.StatisticsViewModel,
    exerciseViewModel: com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }

    // Odświeżanie danych przy każdym otwarciu ekranu
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // Tab Row z przyciskami
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabButton(
                text = "Podsumowanie",
                isSelected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "Wykresy",
                isSelected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f)
            )
        }

        // Zawartość w zależności od wybranej zakładki
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> SummaryContent(viewModel = viewModel)
                1 -> ChartsContent(viewModel = viewModel, exerciseViewModel = exerciseViewModel)
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF4CAF50) else Color.White,
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SummaryContent(
    viewModel: com.example.gymtrackapp.ui.viewmodel.StatisticsViewModel
) {
    val heatmapData by viewModel.heatmapData.collectAsState()
    val heatmapDaysRange by viewModel.heatmapDaysRange.collectAsState()
    val topPRs by viewModel.topPersonalRecords.collectAsState()
    val volumeStats7Days by viewModel.volumeStats7Days.collectAsState()
    val volumeStats30Days by viewModel.volumeStats30Days.collectAsState()
    val selectedVolumeTab by viewModel.selectedVolumeTab.collectAsState()
    val muscleDistribution by viewModel.muscleGroupDistribution.collectAsState()
    val muscleDistributionDays by viewModel.muscleDistributionDays.collectAsState()
    val isLoading by viewModel.isLoadingSummary.collectAsState()

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Heatmapa aktywności
            com.example.gymtrackapp.ui.screens.statistics.TrainingHeatmapCard(
                data = heatmapData,
                selectedDaysRange = heatmapDaysRange,
                onDaysRangeChange = { viewModel.setHeatmapDaysRange(it) }
            )

            // Top 3 Personal Records
            com.example.gymtrackapp.ui.screens.statistics.TopPersonalRecordsCard(
                records = topPRs
            )

            // Total Volume
            com.example.gymtrackapp.ui.screens.statistics.TotalVolumeCard(
                stats7Days = volumeStats7Days,
                stats30Days = volumeStats30Days,
                selectedTab = selectedVolumeTab,
                onTabChange = { viewModel.setVolumeTab(it) }
            )

            // Rozkład po grupach mięśniowych
            com.example.gymtrackapp.ui.screens.statistics.MuscleGroupDistributionCard(
                distribution = muscleDistribution,
                selectedDays = muscleDistributionDays,
                onDaysChange = { viewModel.setMuscleDistributionDays(it) }
            )
        }
    }
}

@Composable
fun ChartsContent(
    viewModel: com.example.gymtrackapp.ui.viewmodel.StatisticsViewModel? = null,
    exerciseViewModel: com.example.gymtrackapp.ui.viewmodel.ExerciseViewModel,
    modifier: Modifier = Modifier
) {
    if (viewModel == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("ViewModel nie jest dostępny", color = Color.Gray)
        }
        return
    }

    val exercises by exerciseViewModel.exercises.observeAsState(emptyList())
    val selectedExerciseId by viewModel.selectedExerciseId.collectAsState()
    val estimated1RMHistory by viewModel.estimated1RMHistory.collectAsState()
    val volumeHistory by viewModel.volumeHistory.collectAsState()
    val topSetHistory by viewModel.topSetHistory.collectAsState()
    val repsAtWeightHistory by viewModel.repsAtWeightHistory.collectAsState()
    val repMaxMatrix by viewModel.repMaxMatrix.collectAsState()
    val availableWeights by viewModel.availableWeights.collectAsState()
    val selectedWeightForReps by viewModel.selectedWeightForReps.collectAsState()
    val selectedOneRMFormula by viewModel.selectedOneRMFormula.collectAsState()
    val isLoading by viewModel.isLoadingCharts.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selektor ćwiczenia
        com.example.gymtrackapp.ui.screens.statistics.ExerciseSelector(
            exercises = exercises,
            selectedExerciseId = selectedExerciseId,
            onExerciseSelected = { viewModel.selectExercise(it) }
        )

        if (selectedExerciseId != null) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Wykres 1: Estimated 1RM
                com.example.gymtrackapp.ui.screens.statistics.Estimated1RMChart(
                    data = estimated1RMHistory,
                    selectedFormula = selectedOneRMFormula,
                    onFormulaChange = { viewModel.setOneRMFormula(it) }
                )

                // Wykres 2: Volume Load
                com.example.gymtrackapp.ui.screens.statistics.VolumeLoadChart(
                    data = volumeHistory
                )

                // Wykres 3: Top Set Tracking
                com.example.gymtrackapp.ui.screens.statistics.TopSetTrackingChart(
                    data = topSetHistory
                )

                // Wykres 4: Reps at Weight
                com.example.gymtrackapp.ui.screens.statistics.RepsAtWeightChart(
                    data = repsAtWeightHistory,
                    selectedWeight = selectedWeightForReps,
                    availableWeights = availableWeights,
                    onWeightSelected = { viewModel.selectWeightForReps(it) }
                )

                // Wykres 5: Rep Max Matrix
                com.example.gymtrackapp.ui.screens.statistics.RepMaxMatrixCard(
                    matrix = repMaxMatrix
                )
            }
        } else {
            // Placeholder gdy nie wybrano ćwiczenia
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Wybierz ćwiczenie, aby zobaczyć wykresy",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// PROSTE KOMPONENTY POMOCNICZE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdown(
    items: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Wybierz") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onSelectedChange(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SimpleLabeledDropdown(
    label: String,
    items: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        SimpleDropdown(
            items = items,
            selected = selected,
            onSelectedChange = onSelectedChange
        )
    }
}

@Composable
private fun TimeFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) Color(0xFF2196F3) else Color(0xFFF0F0F0)
    val fg = if (isSelected) Color.White else Color.Black

    Surface(
        color = bg,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .padding(horizontal = 2.dp)
            .height(32.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                color = fg,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(50))
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun SimpleLineChart(
    points: List<Float>,
    lineColor: Color,
    showBaseline: Boolean,
    smoothing: Boolean
) {
    androidx.compose.foundation.Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        if (points.isEmpty()) return@Canvas

        val max = points.maxOrNull() ?: 0f
        val min = points.minOrNull() ?: 0f
        val range = (max - min).takeIf { it != 0f } ?: 1f

        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
        val baselineY = size.height * 0.8f

        // baseline
        if (showBaseline) {
            drawLine(
                color = Color.LightGray,
                start = androidx.compose.ui.geometry.Offset(0f, baselineY),
                end = androidx.compose.ui.geometry.Offset(size.width, baselineY),
                strokeWidth = 2f
            )
        }

        val path = androidx.compose.ui.graphics.Path()
        points.forEachIndexed { index, value ->
            val x = stepX * index
            val normalized = (value - min) / range
            val y = size.height - normalized * size.height * 0.8f

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                if (smoothing) {
                    // uproszczone wygładzanie: po prostu linia łamana
                    path.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 4f
            )
        )
    }
}

