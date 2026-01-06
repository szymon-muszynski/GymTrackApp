package com.example.gymtrackapp.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

object EpochDayFormatter {

    /**
     * Formatuje epochDay (LocalDate.toEpochDay()) do postaci np. "Poniedziałek, 12.05.2025".
     */
    fun formatEpochDay(epochDay: Long, locale: Locale = Locale.getDefault()): String {
        val date = LocalDate.ofEpochDay(epochDay)
        val dow = dayOfWeekName(date.dayOfWeek, locale)
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = date.monthValue.toString().padStart(2, '0')
        val year = date.year
        return "$dow, $day.$month.$year"
    }

    private fun dayOfWeekName(dow: DayOfWeek, locale: Locale): String =
        dow.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.titlecase(locale) }
}

