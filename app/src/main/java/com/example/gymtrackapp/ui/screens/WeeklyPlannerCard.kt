package com.example.gymtrackapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gymtrackapp.data.entity.PlannedWorkoutEntity
import com.example.gymtrackapp.ui.viewmodel.PlannerViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.border

private val PolishLocale = Locale("pl", "PL")

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeeklyPlannerCard(
    plannerViewModel: PlannerViewModel,
    uid: String?,
    modifier: Modifier = Modifier,
) {
    val weekStart by plannerViewModel.currentWeekStart.collectAsState()
    val plans by plannerViewModel.plans.collectAsState()

    var dialogState by remember { mutableStateOf<PlanDialogState?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header (miesiąc + rok) + strzałki
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { plannerViewModel.previousWeek() }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Poprzedni tydzień")
                }

                Text(
                    text = weekTitleFor(weekStart),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(onClick = { plannerViewModel.nextWeek() }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Następny tydzień")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pasek dni: styl jak "Kalendarz Treningowy" (mniejsza wersja)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val today = LocalDate.now()

                repeat(7) { index ->
                    val date = weekStart.plusDays(index.toLong())
                    val plan = plans[date]

                    DayItem(
                        date = date,
                        isToday = date == today,
                        hasPlan = plan != null,
                        onClick = {
                            dialogState = PlanDialogState(
                                date = date,
                                existingPlan = plan
                            )
                        }
                    )
                }
            }
        }
    }

    val state = dialogState
    if (state != null) {
        PlanDialog(
            state = state,
            onDismiss = { dialogState = null },
            onSave = { title ->
                plannerViewModel.savePlan(state.date, title, uid)
                dialogState = null
            },
            onDelete = {
                state.existingPlan?.let { plannerViewModel.deletePlan(it.id, uid) }
                dialogState = null
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayItem(
    date: LocalDate,
    isToday: Boolean,
    hasPlan: Boolean,
    onClick: () -> Unit,
) {
    val dayLabel = date.dayOfWeek
        .getDisplayName(TextStyle.SHORT, PolishLocale)
        .replaceFirstChar { it.uppercase(PolishLocale) }
        .trimEnd('.')

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Góra: nazwa dnia (szara)
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Środek: numer w kółku, z obrysem gdy "dzisiaj"
        val borderColor = if (isToday) Color(0xFF1E88E5) else Color.Transparent
        val bgColor = if (isToday) Color(0xFFEAF3FF) else Color.Transparent

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(BorderStroke(2.dp, borderColor), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dół: dyskretna zielona kropka, jeśli jest plan
        if (hasPlan) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32))
            )
        } else {
            // rezerwujemy miejsce żeby UI nie "skakało"
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun weekTitleFor(weekStart: LocalDate): String {
    val weekDates = (0..6).map { weekStart.plusDays(it.toLong()) }

    val monthCounts = weekDates.groupingBy { it.month }.eachCount()
    val targetMonth = monthCounts.maxBy { it.value }.key

    val yearCounts = weekDates.groupingBy { it.year }.eachCount()
    val targetYear = yearCounts.maxBy { it.value }.key

    // "Styczeń 2026" (PL)
    val monthName = targetMonth
        .getDisplayName(TextStyle.FULL, PolishLocale)
        .replaceFirstChar { it.uppercase(PolishLocale) }

    return "$monthName $targetYear"
}

@RequiresApi(Build.VERSION_CODES.O)
private data class PlanDialogState(
    val date: LocalDate,
    val existingPlan: PlannedWorkoutEntity?,
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun PlanDialog(
    state: PlanDialogState,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var text by remember(state.date) { mutableStateOf(state.existingPlan?.title.orEmpty()) }


    val title = "Plan: ${state.date.dayOfMonth}.${state.date.monthValue}"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Nazwa treningu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.existingPlan != null) {
                    TextButton(onClick = onDelete) {
                        Text("Usuń")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Anuluj")
                }
            }
        }
    )
}