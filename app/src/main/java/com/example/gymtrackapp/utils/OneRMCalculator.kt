package com.example.gymtrackapp.utils

import com.example.gymtrackapp.data.entity.statistics.OneRMFormula
import kotlin.math.min

/**
 * Kalkulator szacowanego One Rep Max (1RM)
 * Zawiera najpopularniejsze wzory używane w trening siłowym
 */
object OneRMCalculator {

    /**
     * Oblicza szacowany 1RM używając wybranego wzoru
     *
     * @param weight Ciężar użyty w serii
     * @param reps Liczba wykonanych powtórzeń
     * @param formula Wzór do użycia (domyślnie Epley)
     * @return Szacowany maksymalny ciężar na 1 powtórzenie
     */
    fun calculate(weight: Float, reps: Int, formula: OneRMFormula): Float {
        if (weight <= 0f || reps <= 0) return 0f

        return when (formula) {
            OneRMFormula.EPLEY ->
                weight * (1f + reps / 30f)
            OneRMFormula.BRZYCKI ->
                if (reps >= 37) 0f else weight * (36f / (37f - reps))
        }
    }


    /**
     * Wzór Epley'a (najbardziej popularny)
     * 1RM = weight × (1 + reps/30)
     *
     * Przykład: 100kg × 5 reps = 100 × (1 + 5/30) = 116.67kg
     */
    private fun calculateEpley(weight: Float, reps: Int): Float {
        return weight * (1 + reps / 30f)
    }

    /**
     * Wzór Brzycki'ego (bardziej konserwatywny)
     * 1RM = weight × (36 / (37 - reps))
     *
     * Przykład: 100kg × 5 reps = 100 × (36 / 32) = 112.5kg
     */
    private fun calculateBrzycki(weight: Float, reps: Int): Float {
        if (reps >= 37) return weight * 2 // Zabezpieczenie przed dzieleniem przez 0 lub ujemną wartość
        return weight * (36f / (37f - reps))
    }

    /**
     * Oblicza średni 1RM z wielu serii (używając różnych wzorów i uśredniając)
     * Przydatne do bardziej dokładnej oceny siły
     */
    fun calculateAverage(weight: Float, reps: Int): Float {
        if (reps == 1) return weight

        val epley = calculateEpley(weight, min(reps, 15))
        val brzycki = calculateBrzycki(weight, min(reps, 15))

        return (epley + brzycki) / 2f
    }

    /**
     * Oblicza % maksymalnego ciężaru (intensity)
     * Np. 80kg przy 1RM = 100kg → 80%
     */
    fun calculateIntensity(weight: Float, oneRM: Float): Float {
        if (oneRM == 0f) return 0f
        return (weight / oneRM) * 100f
    }

    /**
     * Sugerowana liczba powtórzeń dla danego % 1RM
     * Używane w planowaniu treningów
     */
    fun suggestedRepsForPercentage(percentage: Float): IntRange {
        return when {
            percentage >= 95f -> 1..2    // 95-100% = 1-2 reps
            percentage >= 90f -> 2..3    // 90-95% = 2-3 reps
            percentage >= 85f -> 3..5    // 85-90% = 3-5 reps
            percentage >= 80f -> 5..6    // 80-85% = 5-6 reps
            percentage >= 75f -> 6..8    // 75-80% = 6-8 reps
            percentage >= 70f -> 8..10   // 70-75% = 8-10 reps
            percentage >= 65f -> 10..12  // 65-70% = 10-12 reps
            else -> 12..15               // <65% = 12-15+ reps
        }
    }
}

