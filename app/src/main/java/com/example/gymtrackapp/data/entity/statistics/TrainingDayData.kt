package com.example.gymtrackapp.data.entity.statistics

/**
 * Reprezentuje dane o aktywności treningowej dla pojedynczego dnia
 * Używane do generowania heatmapy/kalendarza
 */
data class TrainingDayData(
    val date: Long,              // timestamp dnia
    val sessionCount: Int,       // liczba sesji tego dnia
    val totalVolume: Float       // suma (reps × weight) dla wszystkich serii tego dnia
)

