package com.example.gymtrackapp.data.entity.statistics

/**
 * Dane o volume dla wykresów czasowych
 */
data class VolumeData(
    val date: Long,
    val volume: Float
)

/**
 * Agregacja volume dla różnych zakresów czasowych
 */
data class TotalVolumeStats(
    val totalVolume: Float,
    val averagePerDay: Float,
    val days: Int,
    val changePercentage: Float? = null  // % zmiana względem poprzedniego okresu
)

