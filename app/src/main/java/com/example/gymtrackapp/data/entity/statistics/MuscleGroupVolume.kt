package com.example.gymtrackapp.data.entity.statistics

/**
 * Reprezentuje volume treningowy dla grupy mięśniowej
 */
data class MuscleGroupVolume(
    val muscleGroup: String,     // np. "chest", "back", "legs"
    val totalVolume: Float,      // suma (reps × weight)
    val exerciseCount: Int,      // liczba unikalnych ćwiczeń
    val setCount: Int            // liczba serii
) {
    // Procent względem całkowitego volume (obliczany później)
    var percentage: Float = 0f
}

