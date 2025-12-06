package com.example.gymtrackapp.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtrackapp.data.entity.Exercise
import com.example.gymtrackapp.data.entity.statistics.OneRMFormula
import com.example.gymtrackapp.data.entity.statistics.RepMaxMatrix
import com.example.gymtrackapp.data.entity.statistics.VolumeData
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelector(
    exercises: List<Exercise>,
    selectedExerciseId: String?,
    onExerciseSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val selectedExercise = exercises.find { it.id == selectedExerciseId }

    // Filtrowanie ćwiczeń na podstawie zapytania
    val filteredExercises = remember(searchQuery, exercises) {
        if (searchQuery.isEmpty()) {
            exercises
        } else {
            exercises.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Wybierz ćwiczenie",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Box {
                OutlinedTextField(
                    value = if (isSearchActive) searchQuery else (selectedExercise?.name ?: ""),
                    onValueChange = {
                        searchQuery = it
                        isSearchActive = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Szukaj ćwiczenia...") },
                    trailingIcon = {
                        if (isSearchActive && searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                isSearchActive = false
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Wyczyść"
                                )
                            }
                        } else {
                            Icon(Icons.Default.Search, "Szukaj")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(),
                    singleLine = true
                )

                // Podpowiedzi (dropdown)
                if (isSearchActive && filteredExercises.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp)
                            .heightIn(max = 300.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            filteredExercises.forEach { exercise ->
                                SuggestionItem(
                                    exercise = exercise,
                                    searchQuery = searchQuery,
                                    onClick = {
                                        onExerciseSelected(exercise.id)
                                        searchQuery = ""
                                        isSearchActive = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Info gdy brak wyników
                if (isSearchActive && searchQuery.isNotEmpty() && filteredExercises.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            text = "Brak wyników dla \"$searchQuery\"",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Wyświetlenie wybranego ćwiczenia
            if (!isSearchActive && selectedExercise != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE3F2FD)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓ ${selectedExercise.name}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1976D2)
                        )
                        TextButton(onClick = { isSearchActive = true }) {
                            Text("Zmień", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    exercise: Exercise,
    searchQuery: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Podświetlenie szukanej frazy
            HighlightedText(
                fullText = exercise.name,
                highlight = searchQuery
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun HighlightedText(
    fullText: String,
    highlight: String
) {
    if (highlight.isEmpty()) {
        Text(text = fullText, fontSize = 14.sp)
        return
    }

    val startIndex = fullText.indexOf(highlight, ignoreCase = true)
    if (startIndex == -1) {
        Text(text = fullText, fontSize = 14.sp)
        return
    }

    val endIndex = startIndex + highlight.length

    val annotatedText = buildAnnotatedString {
        append(fullText.substring(0, startIndex))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))) {
            append(fullText.substring(startIndex, endIndex))
        }
        append(fullText.substring(endIndex))
    }

    Text(text = annotatedText, fontSize = 14.sp)
}


/**
 * Wykres 1: Estimated 1RM Chart
 */
@Composable
fun Estimated1RMChart(
    data: List<Pair<Long, Float>>,
    selectedFormula: OneRMFormula,
    onFormulaChange: (OneRMFormula) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Szacowany 1RM",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Selektor wzoru
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FormulaChip(
                        label = "Epley",
                        isSelected = selectedFormula == OneRMFormula.EPLEY,
                        onClick = { onFormulaChange(OneRMFormula.EPLEY) }
                    )
                    FormulaChip(
                        label = "Brzycki",
                        isSelected = selectedFormula == OneRMFormula.BRZYCKI,
                        onClick = { onFormulaChange(OneRMFormula.BRZYCKI) }
                    )
                }
            }

            if (data.isEmpty()) {
                EmptyChartPlaceholder("Brak danych do wyświetlenia")
            } else {
                // Wykres liniowy używając Vico
                LineChart(
                    data = data,
                    yAxisLabel = "1RM (kg)",
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

/**
 * Wykres 2: Volume Load Chart
 */
@Composable
fun VolumeLoadChart(
    data: List<VolumeData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Objętość Treningowa (Volume Load)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (data.isEmpty()) {
                EmptyChartPlaceholder("Brak danych do wyświetlenia")
            } else {
                LineChart(
                    data = data.map { it.date to it.volume },
                    yAxisLabel = "Volume (kg)",
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

/**
 * Wykres 3: Top Set Tracking Chart
 */
@Composable
fun TopSetTrackingChart(
    data: List<Pair<Long, Float>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Najcięższa Seria",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (data.isEmpty()) {
                EmptyChartPlaceholder("Brak danych do wyświetlenia")
            } else {
                LineChart(
                    data = data,
                    yAxisLabel = "Ciężar (kg)",
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

/**
 * Wykres 4: Reps at Weight Chart
 */
@Composable
fun RepsAtWeightChart(
    data: List<Pair<Long, Int>>,
    selectedWeight: Float?,
    availableWeights: List<Float>,
    onWeightSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Powtórzenia dla Ciężaru",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Selektor ciężaru
            if (availableWeights.isNotEmpty()) {
                WeightSelector(
                    weights = availableWeights,
                    selectedWeight = selectedWeight,
                    onWeightSelected = onWeightSelected
                )
            }
            
            if (data.isEmpty()) {
                EmptyChartPlaceholder(
                    if (selectedWeight == null) "Wybierz ciężar" else "Brak danych dla tego ciężaru"
                )
            } else {
                LineChart(
                    data = data.map { it.first to it.second.toFloat() },
                    yAxisLabel = "Powtórzenia",
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

/**
 * Wykres 5: Rep Max Matrix (tabela)
 */
@Composable
fun RepMaxMatrixCard(
    matrix: RepMaxMatrix?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Drabina Rekordów",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (matrix == null || matrix.records.isEmpty()) {
                EmptyChartPlaceholder("Brak rekordów do wyświetlenia")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RepMaxMatrix.STANDARD_REP_RANGES.forEach { reps ->
                        val record = matrix.records[reps]
                        RepMaxRow(
                            reps = reps,
                            record = record
                        )
                    }
                }
            }
        }
    }
}

// ============= KOMPONENTY POMOCNICZE =============

@Composable
private fun LineChart(
    data: List<Pair<Long, Float>>,
    yAxisLabel: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        val values = data.map { it.second }
        val max = values.maxOrNull() ?: 0f
        val min = values.minOrNull() ?: 0f
        val range = (max - min).takeIf { it != 0f } ?: 1f
        
        val stepX = size.width / (values.size - 1).coerceAtLeast(1)
        val paddingTop = 20f
        val chartHeight = size.height - paddingTop
        
        // Rysowanie linii wykresu
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = stepX * index
            val normalized = (value - min) / range
            val y = paddingTop + chartHeight - (normalized * chartHeight)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 4f)
        )
        
        // Rysowanie punktów
        values.forEachIndexed { index, value ->
            val x = stepX * index
            val normalized = (value - min) / range
            val y = paddingTop + chartHeight - (normalized * chartHeight)
            
            drawCircle(
                color = color,
                radius = 6f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun FormulaChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF2196F3) else Color(0xFFEEEEEE)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.Gray,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun WeightSelector(
    weights: List<Float>,
    selectedWeight: Float?,
    onWeightSelected: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        weights.take(10).forEach { weight ->
            Surface(
                onClick = { onWeightSelected(weight) },
                shape = RoundedCornerShape(16.dp),
                color = if (selectedWeight == weight) Color(0xFFFF9800) else Color(0xFFEEEEEE)
            ) {
                Text(
                    text = "${weight.toInt()}kg",
                    fontSize = 12.sp,
                    fontWeight = if (selectedWeight == weight) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedWeight == weight) Color.White else Color.Gray,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun RepMaxRow(
    reps: Int,
    record: com.example.gymtrackapp.data.entity.statistics.RepMaxRecord?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (record != null) Color(0xFFF5F5F5) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${reps}RM",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        if (record != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = record.formatDisplay(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(record.date)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        } else {
            Text(
                text = "—",
                fontSize = 18.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun EmptyChartPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

