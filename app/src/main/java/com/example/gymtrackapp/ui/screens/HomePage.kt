package com.example.gymtrackapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Ekran główny aplikacji (Home Page)
 */
@Composable
fun HomePage(
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToPlans: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onAddWorkoutSession: () -> Unit = {},
    userName: String? = null,
    statisticsViewModel: com.example.gymtrackapp.ui.viewmodel.StatisticsViewModel? = null,
    modifier: Modifier = Modifier
) {
    // Pobierz dane o ostatnim tygodniu
    val weeklyData = statisticsViewModel?.heatmapData?.collectAsState()?.value ?: emptyList()

    // Załaduj dane przy pierwszym otwarciu
    LaunchedEffect(statisticsViewModel) {
        statisticsViewModel?.setHeatmapDaysRange(7)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        HomePageHeader(userName = userName)

        // Quick Actions
        QuickActionsSection(
            onNavigateToStatistics = onNavigateToStatistics,
            onNavigateToPlans = onNavigateToPlans,
            onNavigateToSettings = onNavigateToSettings,
            onAddWorkoutSession = onAddWorkoutSession
        )

        // Weekly Load Chart
        WeeklyLoadChartCard(weeklyData = weeklyData)

        // Today Summary
        TodaySummaryCard()

        // Recent Activity
        RecentActivityCard()

        // Bottom spacing
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HomePageHeader(userName: String? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = if (userName != null) "Cześć, $userName!" else "Cześć!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gotowy na trening?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onNavigateToStatistics: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAddWorkoutSession: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row - two main actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Nowa sesja",
                subtitle = "Dodaj trening",
                icon = Icons.Default.Add,
                modifier = Modifier.weight(1f),
                onClick = onAddWorkoutSession
            )
            QuickActionCard(
                title = "Statystyki",
                subtitle = "Przegląd postępów",
                icon = Icons.Default.Info,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToStatistics
            )
        }

        // Second row - two secondary actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Plany",
                subtitle = "Twoje rozpiski",
                icon = Icons.Default.DateRange,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPlans
            )
            QuickActionCard(
                title = "Ustawienia",
                subtitle = "Preferencje",
                icon = Icons.Default.Settings,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun WeeklyLoadChartCard(weeklyData: List<com.example.gymtrackapp.data.entity.statistics.TrainingDayData>) {
    // Oblicz sumę i porównaj z poprzednim tygodniem
    val totalVolume = weeklyData.sumOf { it.totalVolume.toDouble() }.toFloat()

    // Pobierz ostatnie 7 dni (aktualny tydzień)
    val last7Days = weeklyData.takeLast(7)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Obciążenie tygodnia",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "kg / dzień",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF757575)
                )
            }

            // Real data bar chart
            WeeklyBarChart(weeklyData = last7Days)

            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Suma: ${String.format("%.0f", totalVolume)} kg",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF424242)
                )

                // Oblicz zmianę % jeśli mamy dane z poprzedniego tygodnia
                if (weeklyData.size >= 14) {
                    val previousWeekVolume = weeklyData.take(7).sumOf { it.totalVolume.toDouble() }.toFloat()
                    val currentWeekVolume = weeklyData.takeLast(7).sumOf { it.totalVolume.toDouble() }.toFloat()

                    if (previousWeekVolume > 0) {
                        val changePercentage = ((currentWeekVolume - previousWeekVolume) / previousWeekVolume) * 100f
                        val isPositive = changePercentage >= 0

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${String.format("%.0f", kotlin.math.abs(changePercentage))}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyBarChart(weeklyData: List<com.example.gymtrackapp.data.entity.statistics.TrainingDayData>) {
    val days = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

    // Mapujemy dane do mapy dla szybkiego dostępu (tak samo jak w kalendarzu treningowym)
    val dataMap = weeklyData.associateBy { it.date }

    // Przygotuj wartości dla aktualnego tygodnia (od poniedziałku do niedzieli)
    val calendar = java.util.Calendar.getInstance()

    // Znajdź poniedziałek tego tygodnia
    calendar.firstDayOfWeek = java.util.Calendar.MONDAY
    calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)

    val values = mutableListOf<Float>()

    // Dla każdego dnia tygodnia (Pn-Nd)
    for (i in 0 until 7) {
        val currentDayCalendar = calendar.clone() as java.util.Calendar
        currentDayCalendar.add(java.util.Calendar.DAY_OF_YEAR, i)

        // Normalizuj timestamp do początku dnia (00:00:00) - tak samo jak w kalendarzu treningowym
        val dayTimestamp = com.example.gymtrackapp.utils.DateRangeHelper.getStartOfDay(currentDayCalendar.timeInMillis)

        // Pobierz dane z mapy
        val dayData = dataMap[dayTimestamp]
        values.add(dayData?.totalVolume ?: 0f)
    }

    val maxValue = values.maxOrNull()?.takeIf { it > 0 } ?: 1f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEachIndexed { index, day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // Bar
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(((values[index] / maxValue) * 110).dp.coerceAtLeast(4.dp))
                        .background(
                            color = if (values[index] > 0) Color(0xFF4CAF50) else Color(0xFFEEEEEE),
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Label
                Text(
                    text = day,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun TodaySummaryCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Dzisiaj",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "Brak zaplanowanego treningu",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF757575)
                )
            }

            Button(
                onClick = { /* Plan today */ },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Zaplanuj",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RecentActivityCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ostatnie sesje",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                TextButton(
                    onClick = { /* Show all */ },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Pokaż wszystkie",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            // Mock recent sessions
            RecentSessionItem(
                title = "Trening FBW",
                subtitle = "Wczoraj • 1 200 kg • 12 ćwiczeń",
                duration = "45 min"
            )
            HorizontalDivider(color = Color(0xFFEEEEEE))

            RecentSessionItem(
                title = "Trening górnej partii",
                subtitle = "2 dni temu • 850 kg • 8 ćwiczeń",
                duration = "38 min"
            )
            HorizontalDivider(color = Color(0xFFEEEEEE))

            RecentSessionItem(
                title = "Trening nóg",
                subtitle = "3 dni temu • 1 500 kg • 10 ćwiczeń",
                duration = "52 min"
            )
        }
    }
}

@Composable
private fun RecentSessionItem(
    title: String,
    subtitle: String,
    duration: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121)
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF757575)
            )
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF5F5F5)
        ) {
            Text(
                text = duration,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

