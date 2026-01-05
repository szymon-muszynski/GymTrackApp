package com.example.gymtrackapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeeklyPlannerCard(
    plannerViewModel: PlannerViewModel,
    uid: String?,
    modifier: Modifier = Modifier,
) {
    val weekStart by plannerViewModel.currentWeekStart.collectAsStateCompat()
    val plans by plannerViewModel.plans.collectAsStateCompat()

    var dialogState by remember { mutableStateOf<PlanDialogState?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { plannerViewModel.previousWeek() }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Poprzedni tydzień")
                }

                Text(
                    text = plannerViewModel.formatWeekRange(weekStart),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(onClick = { plannerViewModel.nextWeek() }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Następny tydzień")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val today = LocalDate.now()

                repeat(7) { index ->
                    val date = weekStart.plusDays(index.toLong())
                    val plan = plans[date]

                    DayPill(
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
private fun DayPill(
    date: LocalDate,
    isToday: Boolean,
    hasPlan: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val bg = if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color(0xFFF6F6F6)
    val border = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pl", "PL"))
            .replaceFirstChar { it.uppercase(Locale("pl", "PL")) }

        Text(text = dayLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(text = date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        Spacer(modifier = Modifier.height(6.dp))

        if (hasPlan) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        } else {
            Box(modifier = Modifier.size(6.dp))
        }

        // pseudo-border (prosto i bez dodatkowych zależności)
        if (border != Color.Transparent) {
            Spacer(modifier = Modifier.height(0.dp))
        }
    }
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

    // UX: jeśli wejdziemy w dzień bez planu, wstawiamy placeholder i focusujemy input (na przyszłość).
    LaunchedEffect(state.date) {
        // no-op for now
    }

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

/**
 * Minimalny helper, żeby nie mieszać zależności Compose w ViewModelach.
 * Projekt już używa collectAsState(), ale w kilku plikach importy są różne.
 */
@Composable
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsStateCompat(): State<T> {
    return this.collectAsState()
}
