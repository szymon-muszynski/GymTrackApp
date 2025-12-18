package com.example.gymtrackapp.utils

import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Pomocnicze funkcje do pracy z zakresami dat
 */
object DateRangeHelper {

    /**
     * Pobiera timestamp początku dnia (00:00:00)
     */
    fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    /**
     * Pobiera timestamp końca dnia (23:59:59)
     */
    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }

    /**
     * Pobiera zakres dat dla ostatnich N dni
     * @return Pair(startTimestamp, endTimestamp)
     */
    fun getLastNDays(days: Int): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val endOfToday = getEndOfDay(now)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -(days - 1))  // -1 bo dzisiejszy dzień też liczymy
        }
        val startOfFirstDay = getStartOfDay(calendar.timeInMillis)

        return Pair(startOfFirstDay, endOfToday)
    }

    /**
     * Pobiera zakres dat dla ostatnich 7 dni
     */
    fun getLast7Days(): Pair<Long, Long> = getLastNDays(7)

    /**
     * Pobiera zakres dat dla ostatnich 30 dni
     */
    fun getLast30Days(): Pair<Long, Long> = getLastNDays(30)

    /**
     * Pobiera zakres dat dla ostatnich 60 dni
     */
    fun getLast60Days(): Pair<Long, Long> = getLastNDays(60)

    /**
     * Pobiera zakres dat dla ostatnich 90 dni
     */
    fun getLast90Days(): Pair<Long, Long> = getLastNDays(90)

    /**
     * Pobiera zakres dat obejmujący przeszłość i przyszłość
     * @param daysBack liczba dni wstecz od dzisiaj
     * @param daysForward liczba dni w przód od dzisiaj
     * @return Pair(startTimestamp, endTimestamp)
     */
    fun getDateRangeWithFuture(daysBack: Int, daysForward: Int): Pair<Long, Long> {
        val now = System.currentTimeMillis()

        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -daysBack)
        }
        val startOfRange = getStartOfDay(startCalendar.timeInMillis)

        val endCalendar = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, daysForward)
        }
        val endOfRange = getEndOfDay(endCalendar.timeInMillis)

        return Pair(startOfRange, endOfRange)
    }

    /**
     * Sprawdza czy dwie daty są tego samego dnia
     */
    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        return getStartOfDay(timestamp1) == getStartOfDay(timestamp2)
    }

    /**
     * Oblicza różnicę w dniach między dwoma timestampami
     */
    fun daysBetween(start: Long, end: Long): Int {
        val diff = end - start
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    /**
     * Generuje listę wszystkich dni w zakresie (jako timestampy początku dnia)
     */
    fun getAllDaysInRange(startDate: Long, endDate: Long): List<Long> {
        val days = mutableListOf<Long>()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = getStartOfDay(startDate)
        }
        val endOfRange = getStartOfDay(endDate)

        while (calendar.timeInMillis <= endOfRange) {
            days.add(calendar.timeInMillis)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return days
    }

    /**
     * Formatuje timestamp do czytelnej daty
     */
    fun formatDate(timestamp: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return "$day.$month.$year"
    }
}

