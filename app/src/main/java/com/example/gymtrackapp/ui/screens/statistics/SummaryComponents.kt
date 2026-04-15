package com.example.gymtrackapp.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymtrackapp.data.entity.statistics.MuscleGroupVolume
import com.example.gymtrackapp.data.entity.statistics.PersonalRecord
import com.example.gymtrackapp.data.entity.statistics.TotalVolumeStats
import com.example.gymtrackapp.data.entity.statistics.TrainingDayData
import com.example.gymtrackapp.utils.DateRangeHelper
import java.util.*
import kotlin.math.abs

/**
 * Komponent wyświetlający kalendarz miesięczny aktywności treningowej
 */
@Composable
fun TrainingHeatmapCard(
    data: List<TrainingDayData>,
    selectedDaysRange: Int,
    onDaysRangeChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

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
            // Nagłówek
            Text(
                text = "Training calendar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Nawigacja miesiąc/rok
            MonthYearNavigator(
                month = currentMonth,
                year = currentYear,
                onPreviousMonth = {
                    if (currentMonth == 0) {
                        currentMonth = 11
                        currentYear--
                    } else {
                        currentMonth--
                    }
                },
                onNextMonth = {
                    if (currentMonth == 11) {
                        currentMonth = 0
                        currentYear++
                    } else {
                        currentMonth++
                    }
                }
            )

            // Kalendarz
            MonthCalendar(
                data = data,
                month = currentMonth,
                year = currentYear
            )

            // Legenda
            CalendarLegend()
        }
    }
}

/**
 * Nawigacja po miesiącach i latach
 */
@Composable
private fun MonthYearNavigator(
    month: Int,
    year: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Przycisk poprzedni miesiąc
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Previous month",
                modifier = Modifier.rotate(-90f)
            )
        }

        // Wyświetlenie miesiąca i roku
        Text(
            text = "${monthNames[month]} $year",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Przycisk następny miesiąc
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Next month",
                modifier = Modifier.rotate(90f)
            )
        }
    }
}

/**
 * Kalendarz miesięczny z dniami kolorowanymi według volume
 */
@Composable
private fun MonthCalendar(
    data: List<TrainingDayData>,
    month: Int,
    year: Int
) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Niedziela, 2=Poniedziałek...
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Przesunięcie dla polskiego kalendarza (Poniedziałek = pierwszy dzień)
    val startOffset = if (firstDayOfWeek == 1) 6 else firstDayOfWeek - 2

    // Mapujemy dane do mapy dla szybkiego dostępu
    val dataMap = data.associateBy { DateRangeHelper.getStartOfDay(it.date) }

    // Obliczamy max volume dla normalizacji kolorów
    val maxVolume = data.maxOfOrNull { it.totalVolume } ?: 1f

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Nagłówki dni tygodnia
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd").forEach { dayName ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
        }

        // Siatka dni miesiąca
        val totalCells = startOffset + daysInMonth
        val rows = (totalCells + 6) / 7 // Zaokrąglenie w górę

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val dayOfMonth = cellIndex - startOffset + 1

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (cellIndex >= startOffset && dayOfMonth <= daysInMonth) {
                                // Pobierz timestamp dla tego dnia
                                val dayCalendar = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                val dayTimestamp = DateRangeHelper.getStartOfDay(dayCalendar.timeInMillis)
                                val dayData = dataMap[dayTimestamp]

                                CalendarDayCell(
                                    day = dayOfMonth,
                                    dayData = dayData,
                                    maxVolume = maxVolume,
                                    isToday = isToday(year, month, dayOfMonth)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Pojedyncza komórka dnia w kalendarzu
 */
@Composable
private fun CalendarDayCell(
    day: Int,
    dayData: TrainingDayData?,
    maxVolume: Float,
    isToday: Boolean
) {
    // Kolor tła zależny od volume
    val backgroundColor = when {
        dayData == null || dayData.totalVolume == 0f -> Color(0xFFF5F5F5) // Brak treningu
        dayData.totalVolume < maxVolume * 0.25f -> Color(0xFFC8E6C9) // 0-25%
        dayData.totalVolume < maxVolume * 0.5f -> Color(0xFF81C784)  // 25-50%
        dayData.totalVolume < maxVolume * 0.75f -> Color(0xFF4CAF50) // 50-75%
        else -> Color(0xFF2E7D32) // 75-100%
    }

    val textColor = if (dayData != null && dayData.totalVolume > maxVolume * 0.5f) {
        Color.White
    } else if (isToday) {
        Color(0xFF2196F3)
    } else {
        Color(0xFF424242)
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isToday) {
                    Modifier.border(2.dp, Color(0xFF2196F3), CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 14.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

/**
 * Sprawdza czy dany dzień to dzisiaj
 */
private fun isToday(year: Int, month: Int, day: Int): Boolean {
    val today = Calendar.getInstance()
    return today.get(Calendar.YEAR) == year &&
           today.get(Calendar.MONTH) == month &&
           today.get(Calendar.DAY_OF_MONTH) == day
}

/**
 * Legenda dla kalendarza
 */
@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Less", fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))

        listOf(
            Color(0xFFF5F5F5),
            Color(0xFFC8E6C9),
            Color(0xFF81C784),
            Color(0xFF4CAF50),
            Color(0xFF2E7D32)
        ).forEach { color ->
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .padding(horizontal = 2.dp)
                    .background(color, RoundedCornerShape(3.dp))
                    .border(0.5.dp, Color(0xFFDDDDDD), RoundedCornerShape(3.dp))
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Text("More", fontSize = 11.sp, color = Color.Gray)
    }
}

// Usunięte stare funkcje HeatmapGrid, HeatmapCell, HeatmapLegend

/**
 * Komponent wyświetlający Top 3 najnowsze Personal Records
 */
@Composable
fun TopPersonalRecordsCard(
    records: List<PersonalRecord>,
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
                text = "🏆 Latest PRs",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            if (records.isEmpty()) {
                Text(
                    text = "No PRs yet. Start training!",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                records.take(3).forEach { pr ->
                    PersonalRecordItem(pr)
                    if (pr != records.last()) {
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalRecordItem(pr: PersonalRecord) {
    val daysSincePR = DateRangeHelper.daysBetween(pr.date, System.currentTimeMillis())
    val isNew = daysSincePR <= 7

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pr.exerciseName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pr.formatDisplay(),
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = DateRangeHelper.formatDate(pr.date),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        if (isNew) {
            Surface(
                color = Color(0xFFFFEB3B),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "NEW",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF57C00),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Komponent wyświetlający Total Volume
 */
@Composable
fun TotalVolumeCard(
    stats7Days: TotalVolumeStats?,
    stats30Days: TotalVolumeStats?,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
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
                text = "Total volume",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VolumeTabButton(
                    label = "7 days",
                    isSelected = selectedTab == 0,
                    onClick = { onTabChange(0) },
                    modifier = Modifier.weight(1f)
                )
                VolumeTabButton(
                    label = "30 days",
                    isSelected = selectedTab == 1,
                    onClick = { onTabChange(1) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Wyświetlanie statystyk
            val currentStats = if (selectedTab == 0) stats7Days else stats30Days

            if (currentStats != null) {
                VolumeStatsDisplay(stats = currentStats)
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
private fun VolumeTabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF4CAF50) else Color(0xFFF5F5F5),
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Text(label, fontSize = 14.sp)
    }
}

@Composable
private fun VolumeStatsDisplay(stats: TotalVolumeStats) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Główna liczba
        Text(
            text = "${stats.totalVolume.toInt()} kg",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Średnia per dzień
        Text(
            text = "Avg ${stats.averagePerDay.toInt()} kg/day",
            fontSize = 14.sp,
            color = Color.Gray
        )

        // Porównanie z poprzednim okresem
        stats.changePercentage?.let { change ->
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val isPositive = change > 0
                Icon(
                    imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${abs(change).toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }
}

/**
 * Komponent wyświetlający rozkład volume po grupach mięśniowych (wykres kołowy)
 */
@Composable
fun MuscleGroupDistributionCard(
    distribution: List<MuscleGroupVolume>,
    selectedDays: Int,
    onDaysChange: (Int) -> Unit,
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
                    text = "Workout split",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    RangeChip(
                        label = "7",
                        isSelected = selectedDays == 7,
                        onClick = { onDaysChange(7) }
                    )
                    RangeChip(
                        label = "30",
                        isSelected = selectedDays == 30,
                        onClick = { onDaysChange(30) }
                    )
                }
            }

            if (distribution.isEmpty()) {
                Text(
                    text = "No training data",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // Prosty wykres kołowy (aproksymacja z prostokątami)
                SimplePieChart(distribution = distribution)

                Spacer(modifier = Modifier.height(8.dp))

                // Legenda
                distribution.forEachIndexed { index, group ->
                    MuscleGroupLegendItem(group, index)
                }
            }
        }
    }
}

@Composable
private fun SimplePieChart(distribution: List<MuscleGroupVolume>) {
    val colors = listOf(
        Color(0xFF4CAF50), // Zielony
        Color(0xFF2196F3), // Niebieski
        Color(0xFFF44336), // Czerwony
        Color(0xFFFF9800), // Pomarańczowy
        Color(0xFF9C27B0), // Fioletowy
        Color(0xFF00BCD4), // Cyan
        Color(0xFFFFEB3B)  // Żółty
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Canvas dla wykresu kołowego
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(100.dp)
        ) {
            val total = distribution.sumOf { it.totalVolume.toDouble() }.toFloat()
            if (total == 0f) return@Canvas

            var startAngle = -90f // Start od góry

            distribution.forEachIndexed { index, group ->
                val sweepAngle = (group.totalVolume / total) * 360f
                val color = colors[index % colors.size]

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = size
                )

                startAngle += sweepAngle
            }
        }
    }
}

@Composable
private fun MuscleGroupLegendItem(group: MuscleGroupVolume, index: Int) {
    val colors = listOf(
        Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFF44336),
        Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFFFFEB3B)
    )
    val color = colors[index % colors.size]

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = group.muscleGroup.replaceFirstChar { it.uppercase() },
                fontSize = 14.sp
            )
        }

        Text(
            text = "${group.percentage.toInt()}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
    }
}

@Composable
private fun RangeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF4CAF50) else Color(0xFFEEEEEE)
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
