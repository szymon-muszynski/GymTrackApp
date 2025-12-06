package com.example.gymtrackapp.data.entity.statistics

/**
 * Rekord dla konkretnej liczby powtórzeń (Rep Max Matrix)
 * np. 1RM, 3RM, 5RM, 10RM
 */
data class RepMaxRecord(
    val reps: Int,
    val weight: Float,
    val date: Long,
    val exerciseId: String
) {
    fun formatDisplay(): String = "${weight}kg"
}

/**
 * Kompletna matryca rekordów dla ćwiczenia
 */
data class RepMaxMatrix(
    val exerciseId: String,
    val exerciseName: String,
    val records: Map<Int, RepMaxRecord>  // key = liczba reps (1, 3, 5, 8, 10, 12, etc.)
) {
    // Standardowe zakresy powtórzeń
    companion object {
        val STANDARD_REP_RANGES = listOf(1, 3, 5, 8, 10, 12, 15, 20)
    }
}

