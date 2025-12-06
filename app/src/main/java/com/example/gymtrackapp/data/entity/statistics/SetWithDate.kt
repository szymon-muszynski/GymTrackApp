package com.example.gymtrackapp.data.entity.statistics

/**
 * Seria treningowa z datą sesji - używane do wykresów czasowych
 */
data class SetWithDate(
    val setId: Long,
    val sessionDate: Long,
    val exerciseId: String,
    val weight: Float,
    val reps: Int,
    val order: Int
) {
    // Obliczenia dla tej serii
    val volume: Float
        get() = weight * reps

}

enum class OneRMFormula {
    EPLEY,
    BRZYCKI
}

