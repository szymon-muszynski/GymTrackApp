package com.example.gymtrackapp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

/**
 * Dialog z kalendarzem miesięcznym.
 * Pokazuje siatę dni, oznacza dni z treningami zielonym kółkiem.
 * Pozwala na wybór dnia i przełączanie między miesiącami.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthlyCalendarDialog(
    selectedDate: LocalDate,
    trainingDates: Set<Long>, // zbiór epoch days z treningami
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    onMonthChanged: (Int, Int) -> Unit = { _, _ -> } // year, month
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    // Załaduj dni z treningami dla aktualnie wyświetlanego miesiąca
    LaunchedEffect(currentMonth) {
        onMonthChanged(currentMonth.year, currentMonth.monthValue)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        var offsetX = 0f
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX > 100) {
                                    // Swipe prawo -> poprzedni miesiąc
                                    currentMonth = currentMonth.minusMonths(1)
                                } else if (offsetX < -100) {
                                    // Swipe lewo -> następny miesiąc
                                    currentMonth = currentMonth.plusMonths(1)
                                }
                                offsetX = 0f
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                offsetX += dragAmount
                            }
                        )
                    }
            ) {
                // Tytuł z nawigacją
                MonthHeader(
                    month = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Nagłówki dni tygodnia
                WeekDaysHeader()

                Spacer(modifier = Modifier.height(8.dp))

                // Siatka dni
                MonthGrid(
                    month = currentMonth,
                    selectedDate = selectedDate,
                    trainingDates = trainingDates,
                    onDateClick = { date ->
                        onDateSelected(date)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthHeader(
    month: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Poprzedni miesiąc"
            )
        }

        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("pl"))} ${month.year}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Następny miesiąc"
            )
        }
    }
}

@Composable
fun WeekDaysHeader() {
    val daysOfWeek = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    trainingDates: Set<Long>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()

    // Oblicz od którego dnia tygodnia zaczyna się miesiąc (1=Pn, 7=Nd)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value

    // Oblicz ile pustych komórek przed pierwszym dniem
    val emptyDaysBefore = firstDayOfWeek - 1

    // Całkowita liczba komórek
    val totalCells = emptyDaysBefore + daysInMonth
    val rows = (totalCells + 6) / 7 // zaokrąglenie w górę

    Column {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    val dayOfMonth = index - emptyDaysBefore + 1

                    if (dayOfMonth in 1..daysInMonth) {
                        val date = month.atDay(dayOfMonth)
                        val hasTraining = trainingDates.contains(date.toEpochDay())
                        val isSelected = date == selectedDate

                        DayCell(
                            day = dayOfMonth,
                            hasTraining = hasTraining,
                            isSelected = isSelected,
                            onClick = { onDateClick(date) }
                        )
                    } else {
                        // Pusta komórka
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.DayCell(
    day: Int,
    hasTraining: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Tło i obwódka
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    when {
                        hasTraining -> Color(0xFF4CAF50) // Zielone kółko
                        else -> Color.Transparent
                    }
                )
                .then(
                    if (isSelected) {
                        Modifier.background(
                            color = Color(0xFF2196F3).copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                fontSize = 14.sp,
                fontWeight = if (hasTraining) FontWeight.Bold else FontWeight.Normal,
                color = if (hasTraining) Color.White else Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

