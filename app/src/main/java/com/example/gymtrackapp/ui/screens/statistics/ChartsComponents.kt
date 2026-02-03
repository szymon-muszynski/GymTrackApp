package com.example.gymtrackapp.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt


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
    modifier: Modifier = Modifier,
    timeRange: ChartsTimeRange = ChartsTimeRange.ALL
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
                // Wykres liniowy używając Canvas
                LineChart(
                    data = data,
                    yAxisLabel = "1RM (kg)",
                    color = Color(0xFF2196F3),
                    xAxisType = XAxisType.EPOCH_DAY,
                    xAxisLabel = "",
                    timeRange = timeRange
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
    modifier: Modifier = Modifier,
    timeRange: ChartsTimeRange = ChartsTimeRange.ALL
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
                    color = Color(0xFF4CAF50),
                    xAxisType = XAxisType.EPOCH_DAY,
                    xAxisLabel = "",
                    timeRange = timeRange
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
    modifier: Modifier = Modifier,
    timeRange: ChartsTimeRange = ChartsTimeRange.ALL
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
                    color = Color(0xFFF44336),
                    xAxisType = XAxisType.EPOCH_DAY,
                    xAxisLabel = "",
                    timeRange = timeRange
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
    modifier: Modifier = Modifier,
    timeRange: ChartsTimeRange = ChartsTimeRange.ALL
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
                    color = Color(0xFFFF9800),
                    xAxisType = XAxisType.EPOCH_DAY,
                    xAxisLabel = "",
                    timeRange = timeRange
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

/** Zakres czasu dla wykresów (kalendarzowo, a nie sztywna liczba dni). */
enum class ChartsTimeRange {
    WEEK_1,
    MONTH_1,
    MONTHS_3,
    MONTHS_6,
    YEAR_1,
    ALL
}

private fun ChartsTimeRange.label(): String = when (this) {
    ChartsTimeRange.WEEK_1 -> "1W"
    ChartsTimeRange.MONTH_1 -> "1M"
    ChartsTimeRange.MONTHS_3 -> "3M"
    ChartsTimeRange.MONTHS_6 -> "6M"
    ChartsTimeRange.YEAR_1 -> "1Y"
    ChartsTimeRange.ALL -> "ALL"
}

// ============= KOMPONENTY POMOCNICZE =============

private enum class XAxisType {
    /** X to liczba dni od 1970-01-01 (TrainingSession.date / sessionDate). */
    EPOCH_DAY,
    /** X to timestamp w milisekundach (epoch millis). */
    EPOCH_MILLIS
}

@Composable
private fun LineChart(
    data: List<Pair<Long, Float>>,
    yAxisLabel: String,
    color: Color,
    modifier: Modifier = Modifier,
    xAxisType: XAxisType = XAxisType.EPOCH_DAY,
    xAxisLabel: String = "",
    timeRange: ChartsTimeRange = ChartsTimeRange.ALL
) {
    if (data.isEmpty()) return

    // NOTE: Docelowo chcemy spójność na epochDay. EPOCH_MILLIS zostawiamy dla kompatybilności,
    // ale wszystkie wykresy w Progress/Charts powinny przechodzić na EPOCH_DAY.
    val sortedAll = remember(data) { data.sortedBy { it.first } }

    // Wyznaczenie zakresu osi X (end = ostatni trening, a nie "dzisiaj").
    val endEpochDay: Long = remember(sortedAll, xAxisType) {
        when (xAxisType) {
            XAxisType.EPOCH_DAY -> sortedAll.last().first
            XAxisType.EPOCH_MILLIS -> Instant.ofEpochMilli(sortedAll.last().first)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toEpochDay()
        }
    }

    val startEpochDay: Long = remember(sortedAll, endEpochDay, xAxisType, timeRange) {
        val allStart = when (xAxisType) {
            XAxisType.EPOCH_DAY -> sortedAll.first().first
            XAxisType.EPOCH_MILLIS -> Instant.ofEpochMilli(sortedAll.first().first)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toEpochDay()
        }

        if (timeRange == ChartsTimeRange.ALL) return@remember allStart

        val endDate = LocalDate.ofEpochDay(endEpochDay)
        val candidate = when (timeRange) {
            ChartsTimeRange.WEEK_1 -> endDate.minusWeeks(1)
            ChartsTimeRange.MONTH_1 -> endDate.minusMonths(1)
            ChartsTimeRange.MONTHS_3 -> endDate.minusMonths(3)
            ChartsTimeRange.MONTHS_6 -> endDate.minusMonths(6)
            ChartsTimeRange.YEAR_1 -> endDate.minusYears(1)
            else -> endDate
        }.toEpochDay()

        maxOf(candidate, allStart)
    }

    // Filtr danych do zakresu. Jeśli X jest millis, mapujemy do epochDay przy filtrowaniu.
    val sorted = remember(sortedAll, xAxisType, startEpochDay, endEpochDay) {
        when (xAxisType) {
            XAxisType.EPOCH_DAY -> sortedAll.filter { it.first in startEpochDay..endEpochDay }
            XAxisType.EPOCH_MILLIS -> sortedAll.filter {
                val d = Instant.ofEpochMilli(it.first).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                d in startEpochDay..endEpochDay
            }
        }
    }

    if (sorted.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val valuesY = sorted.map { it.second }
        val maxY = valuesY.maxOrNull() ?: 0f
        val minY = valuesY.minOrNull() ?: 0f
        val rangeY = (maxY - minY).takeIf { it != 0f } ?: 1f

        // Marginesy na osie/etykiety
        val leftAxisWidth = 52f
        val bottomAxisHeight = 34f
        val topPadding = 12f
        val rightPadding = 8f

        val chartLeft = leftAxisWidth
        val chartTop = topPadding
        val chartRight = size.width - rightPadding
        val chartBottom = size.height - bottomAxisHeight

        val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)

        val pointCount = sorted.size
        val stepX = if (pointCount <= 1) 0f else chartWidth / (pointCount - 1)

        fun yToPx(y: Float): Float {
            val normalized = (y - minY) / rangeY
            return chartBottom - (normalized * chartHeight)
        }

        fun drawText(
            text: String,
            x: Float,
            y: Float,
            color: Int,
            textSizePx: Float,
            align: android.graphics.Paint.Align = android.graphics.Paint.Align.LEFT,
            isBold: Boolean = false
        ) {
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    this.color = color
                    this.textSize = textSizePx
                    this.textAlign = align
                    this.isAntiAlias = true
                    this.isFakeBoldText = isBold
                }
                canvas.nativeCanvas.drawText(text, x, y, paint)
            }
        }

        fun formatXLabel(epochDay: Long): String {
            val d = LocalDate.ofEpochDay(epochDay)
            return when (timeRange) {
                ChartsTimeRange.WEEK_1 -> "%02d.%02d".format(d.dayOfMonth, d.monthValue)
                ChartsTimeRange.MONTH_1, ChartsTimeRange.MONTHS_3, ChartsTimeRange.MONTHS_6 -> "%02d.%02d".format(d.dayOfMonth, d.monthValue)
                ChartsTimeRange.YEAR_1, ChartsTimeRange.ALL -> "%02d.%02d.%04d".format(d.dayOfMonth, d.monthValue, d.year)
            }
        }

        fun toEpochDay(x: Long): Long {
            return when (xAxisType) {
                XAxisType.EPOCH_DAY -> x
                XAxisType.EPOCH_MILLIS -> Instant.ofEpochMilli(x).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
            }
        }

        // ====== OŚ Y + ticki ======
        val axisColor = Color(0xFFBDBDBD)
        val gridColor = Color(0xFFE0E0E0)
        val labelColorInt = 0xFF616161.toInt()

        val yLabelTextSize = 26f
        val xLabelTextSize = 24f
        val axisTitleTextSize = 24f

        // Oś Y
        drawLine(
            color = axisColor,
            start = Offset(chartLeft, chartTop),
            end = Offset(chartLeft, chartBottom),
            strokeWidth = 2f
        )

        val yTicks = 4
        for (i in 0..yTicks) {
            val t = i / yTicks.toFloat()
            val yVal = maxY - t * (maxY - minY)
            val yPx = yToPx(yVal)

            // Siatka pozioma
            drawLine(
                color = gridColor,
                start = Offset(chartLeft, yPx),
                end = Offset(chartRight, yPx),
                strokeWidth = 1f
            )

            val label = if (yAxisLabel.contains("Powt", ignoreCase = true)) {
                yVal.roundToInt().toString()
            } else {
                if (kotlin.math.abs(yVal - yVal.roundToInt()) < 0.05f) yVal.roundToInt().toString() else "%.1f".format(yVal)
            }

            drawText(
                text = label,
                x = chartLeft - 10f,
                y = yPx + 4f,
                color = labelColorInt,
                textSizePx = yLabelTextSize,
                align = android.graphics.Paint.Align.RIGHT
            )
        }

        // Podpis osi Y
        drawText(
            text = yAxisLabel,
            x = chartLeft,
            y = chartTop - 2f,
            color = labelColorInt,
            textSizePx = axisTitleTextSize,
            align = android.graphics.Paint.Align.LEFT,
            isBold = true
        )

        // ====== OŚ X ======
        drawLine(
            color = axisColor,
            start = Offset(chartLeft, chartBottom),
            end = Offset(chartRight, chartBottom),
            strokeWidth = 2f
        )

        // Ticki osi X: 4–6 w zależności od zakresu.
        val desiredTicks = when (timeRange) {
            ChartsTimeRange.WEEK_1 -> 4
            ChartsTimeRange.MONTH_1 -> 4
            ChartsTimeRange.MONTHS_3 -> 5
            ChartsTimeRange.MONTHS_6 -> 6
            ChartsTimeRange.YEAR_1 -> 6
            ChartsTimeRange.ALL -> 6
        }.coerceAtLeast(2)

        // generujemy ticki po zakresie kalendarzowym, a potem mapujemy na najbliższy punkt na wykresie.
        val tickEpochDays: List<Long> = run {
            val start = startEpochDay
            val end = endEpochDay
            if (start >= end) listOf(start) else {
                val span = (end - start).toFloat()
                (0 until desiredTicks).map { idx ->
                    val t = idx / (desiredTicks - 1f)
                    (start + (t * span)).roundToInt().toLong()
                }.distinct()
            }
        }

        fun nearestIndexForEpochDay(targetEpochDay: Long): Int {
            // sorted jest po x rosnąco (ale x może być epochMillis). Dla stabilności szukamy po epochDay.
            val xs = sorted.map { toEpochDay(it.first) }
            var bestIdx = 0
            var bestDist = Long.MAX_VALUE
            for (i in xs.indices) {
                val dist = kotlin.math.abs(xs[i] - targetEpochDay)
                if (dist < bestDist) {
                    bestDist = dist
                    bestIdx = i
                }
            }
            return bestIdx
        }

        tickEpochDays.forEach { tickDay ->
            val index = nearestIndexForEpochDay(tickDay)
            val xPx = chartLeft + stepX * index

            drawLine(
                color = axisColor,
                start = Offset(xPx, chartBottom),
                end = Offset(xPx, chartBottom + 6f),
                strokeWidth = 2f
            )

            drawText(
                text = formatXLabel(tickDay),
                x = xPx,
                y = chartBottom + 26f,
                color = labelColorInt,
                textSizePx = xLabelTextSize,
                align = android.graphics.Paint.Align.CENTER
            )
        }

        // Podpis osi X – na środku pod wykresem
        drawText(
            text = xAxisLabel,
            x = (chartLeft + chartRight) / 2f,
            y = size.height - 4f,
            color = labelColorInt,
            textSizePx = axisTitleTextSize,
            align = android.graphics.Paint.Align.CENTER,
            isBold = true
        )

        // ====== Linia wykresu + punkty ======
        val path = Path()
        sorted.forEachIndexed { index, point ->
            val x = chartLeft + stepX * index
            val y = yToPx(point.second)

            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 4f)
        )

        sorted.forEachIndexed { index, point ->
            val x = chartLeft + stepX * index
            val y = yToPx(point.second)
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

/**
 * Chipsy do wybierania zakresu czasu wykresów.
 */
@Composable
fun TimeRangeChips(
    selected: ChartsTimeRange,
    onSelected: (ChartsTimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChartsTimeRange.entries.forEach { range ->
             FilterChip(
                 selected = range == selected,
                 onClick = { onSelected(range) },
                 label = {
                     Text(
                         text = range.label(),
                         fontWeight = if (range == selected) FontWeight.SemiBold else FontWeight.Normal
                     )
                 }
             )
         }
     }
 }
