package com.example.gymtrackapp.data.entity.statistics

/**
 * Reprezentuje rekord osobisty (PR) dla ćwiczenia
 */
data class PersonalRecord(
    val exerciseId: String,
    val exerciseName: String,
    val weight: Float,
    val reps: Int,
    val date: Long,              // kiedy pobity rekord
    val estimated1RM: Float,     // szacowany 1RM dla tego PR
    val volume: Float            // weight × reps
) {
    // Formatowanie do wyświetlenia: "100kg × 5 reps"
    fun formatDisplay(): String = "${weight}kg × ${reps} rep${if (reps > 1) "s" else ""}"
}

