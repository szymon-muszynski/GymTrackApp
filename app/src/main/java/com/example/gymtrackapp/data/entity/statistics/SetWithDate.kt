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

    fun estimated1RM(formula: OneRMFormula = OneRMFormula.EPLEY): Float {
        return when (formula) {
            OneRMFormula.EPLEY -> weight * (1 + reps / 30f)
            OneRMFormula.BRZYCKI -> if (reps == 1) weight else weight * (36f / (37f - reps))
        }
    }
}

enum class OneRMFormula {
    EPLEY,
    BRZYCKI
}

