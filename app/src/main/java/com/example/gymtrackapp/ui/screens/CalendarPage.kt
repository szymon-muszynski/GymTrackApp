package com.example.gymtrackapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarPage(modifier: Modifier = Modifier) {

    val mockSessionsList = listOf(
        TrainingSession("Chest Day", Date(2024 - 1900, 6, 10)),
        TrainingSession("Leg Day", Date(2024 - 1900, 6, 10)),
        TrainingSession("Back and Biceps", Date(2024 - 1900, 6, 10)),
        TrainingSession("Shoulders", Date(2024 - 1900, 6, 11)),
        TrainingSession("Cardio", Date(2024 - 1900, 6, 11))
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF9FBAE8)),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Horyzontalny, zapętlony kalendarz
        HorizontalInfiniteCalendar(
            selected = selectedDate,
            onDateSelected = { selectedDate = it; showDatePicker = false },
            onDateLongClick = { showDatePicker = true }
        )

        // Możesz filtrować sesje według selectedDate - tutaj pokazuję wszystkie
        SessionsList(mockSessionsList)
    }

    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { millis ->
                millis?.let {
                    // konwersja millis -> LocalDate
                    selectedDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HorizontalInfiniteCalendar(
    selected: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDateLongClick: () -> Unit
) {
    val centerIndex = 50_000
    val totalCount = 100_000
    val today = LocalDate.now()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = centerIndex)
    val scope = rememberCoroutineScope()

    // Ensure the visible item is centered on startup at the selected date offset
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
                onClick = { onDateSelected(date) },
                onLongClick = onDateLongClick
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayCard(
    date: LocalDate,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun SessionsList(trainingSessions: List<TrainingSession>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(trainingSessions) { session ->
            TrainingSessionItem(
                modifier = Modifier.padding(vertical = 4.dp),
                session = session
            )
        }
    }
}

@Composable
fun TrainingSessionItem(modifier: Modifier = Modifier, session: TrainingSession) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = session.name + ", " + session.date.toString())
        }
    }
}
