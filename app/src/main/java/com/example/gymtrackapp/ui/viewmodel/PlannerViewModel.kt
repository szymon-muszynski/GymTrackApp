package com.example.gymtrackapp.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.entity.PlannedWorkoutEntity
import com.example.gymtrackapp.data.repository.PlanningRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@RequiresApi(Build.VERSION_CODES.O)
class PlannerViewModel(
    private val planningRepository: PlanningRepository,
) : ViewModel() {

    private val _currentWeekOffset = MutableStateFlow(0)
    val currentWeekOffset: StateFlow<Int> = _currentWeekOffset.asStateFlow()

    val currentWeekStart: StateFlow<LocalDate> = _currentWeekOffset
        .map { offset ->
            val today = LocalDate.now()
            val thisMonday = today.with(DayOfWeek.MONDAY)
            thisMonday.plusWeeks(offset.toLong())
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LocalDate.now().with(DayOfWeek.MONDAY)
        )

    /**
     * Plany na aktualnie wyświetlany tydzień.
     *
     * Klucz: LocalDate (dzień), wartość: encja planu.
     */
    val plans: StateFlow<Map<LocalDate, PlannedWorkoutEntity>> = currentWeekStart
        .flatMapLatest { weekStart ->
            val startEpochDay = weekStart.toEpochDay()
            val endEpochDay = weekStart.plusDays(6).toEpochDay()

            planningRepository
                .plansBetween(startEpochDay, endEpochDay)
                .map { list ->
                    // Zakładamy max 1 plan na dzień; jeśli będzie kilka, bierzemy ostatni (deterministycznie)
                    list
                        .groupBy { LocalDate.ofEpochDay(it.dateEpochDay) }
                        .mapValues { (_, v) -> v.maxBy { it.updatedAtMs } }
                }
        }
        .stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = emptyMap())

    fun loadPlans() {
        // UI korzysta z Flow (plans), więc nie musimy tu nic robić.
        // Zostawiamy metodę jako "hak" pod przyszłą logikę.
    }

    fun nextWeek() {
        _currentWeekOffset.value = _currentWeekOffset.value + 1
    }

    fun previousWeek() {
        _currentWeekOffset.value = _currentWeekOffset.value - 1
    }

    fun savePlan(date: LocalDate, title: String, uid: String?) {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return

        // Explicit ID: jeśli edytujemy istniejący kafel, używamy jego id
        val existingId = plans.value[date]?.id

        viewModelScope.launch {
            planningRepository.addPlan(
                dateEpochDay = date.toEpochDay(),
                title = trimmed,
                id = existingId,
                uid = uid,
            )
        }
    }

    fun deletePlan(planId: String, uid: String?) {
        viewModelScope.launch {
            planningRepository.deletePlan(planId = planId, uid = uid)
        }
    }

    fun formatWeekRange(weekStart: LocalDate, locale: Locale = Locale("pl", "PL")): String {
        val weekEnd = weekStart.plusDays(6)
        // np. "6 - 12 sty"
        val monthFormatter = DateTimeFormatter.ofPattern("LLL", locale)
        val endMonth = weekEnd.format(monthFormatter)
        return "${weekStart.dayOfMonth} - ${weekEnd.dayOfMonth} $endMonth"
    }
}
