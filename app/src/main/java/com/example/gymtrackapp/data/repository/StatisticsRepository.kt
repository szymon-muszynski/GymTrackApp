package com.example.gymtrackapp.data.repository

import com.example.gymtrackapp.data.dao.ExerciseDao
import com.example.gymtrackapp.data.dao.TrainingDao
import com.example.gymtrackapp.data.entity.statistics.*
import com.example.gymtrackapp.utils.DateRangeHelper
import com.example.gymtrackapp.utils.OneRMCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * Repository odpowiedzialne za pobieranie i przetwarzanie danych statystycznych
 */
class StatisticsRepository(
    private val trainingDao: TrainingDao,
    private val exerciseDao: ExerciseDao
) {

    /**
     * Pobiera dane o aktywności treningowej dla heatmapy/kalendarza
     */
    suspend fun getTrainingHeatmapData(days: Int): List<TrainingDayData> = withContext(Dispatchers.IO) {
        val (startDate, endDate) = DateRangeHelper.getLastNDays(days)

        // Konwersja milisekund na epochDay (TrainingSession.date przechowuje dni, nie milisekundy)
        val startEpochDay = startDate / (24 * 60 * 60 * 1000)
        val endEpochDay = endDate / (24 * 60 * 60 * 1000)

        val rawData = trainingDao.getTrainingDaysData(startEpochDay, endEpochDay)

        // Konwertujemy raw data do TrainingDayData
        rawData.map { raw ->
            // Konwersja epochDay na milisekundy (początek dnia, 00:00:00)
            // Używamy tego samego podejścia co w kalendarzu treningowym
            val epochDayMillis = raw.date * (24 * 60 * 60 * 1000)

            TrainingDayData(
                date = DateRangeHelper.getStartOfDay(epochDayMillis),
                sessionCount = raw.sessionCount,
                totalVolume = raw.totalVolume
            )
        }
    }

    /**
     * Pobiera top N najnowszych personal records
     * Sortowane po dacie (najnowsze pierwsze)
     */
    suspend fun getTopPersonalRecords(limit: Int = 10): List<PersonalRecord> = withContext(Dispatchers.IO) {
        val topSets = trainingDao.getRecentTopSets(limit * 3) // Pobieramy więcej, bo będziemy filtrować
        val uniqueExercises = mutableMapOf<String, PersonalRecord>()

        // Grupujemy po ćwiczeniu i bierzemy najlepszy wynik dla każdego
        for (set in topSets) {
            val exercise = exerciseDao.getExerciseById(set.exerciseId) ?: continue

            val pr = PersonalRecord(
                exerciseId = set.exerciseId,
                exerciseName = exercise.name,
                weight = set.weight,
                reps = set.reps,
                date = set.date * (24 * 60 * 60 * 1000), // Konwersja epochDay na milisekundy
                estimated1RM = set.estimated1RM,
                volume = set.weight * set.reps
            )

            // Jeśli to ćwiczenie nie jest jeszcze w mapie, lub ten PR jest nowszy
            val existing = uniqueExercises[set.exerciseId]
            if (existing == null || pr.date > existing.date) {
                uniqueExercises[set.exerciseId] = pr
            }
        }

        // Zwracamy sortowane po dacie, limit
        uniqueExercises.values
            .sortedByDescending { it.date }
            .take(limit)
    }

    /**
     * Oblicza total volume dla zakresu dat
     */
    suspend fun getTotalVolume(days: Int): Float = withContext(Dispatchers.IO) {
        val (startDate, endDate) = DateRangeHelper.getLastNDays(days)
        val startEpochDay = startDate / (24 * 60 * 60 * 1000)
        val endEpochDay = endDate / (24 * 60 * 60 * 1000)
        trainingDao.getTotalVolumeInRange(startEpochDay, endEpochDay)
    }

    /**
     * Pobiera statystyki volume z porównaniem do poprzedniego okresu
     */
    suspend fun getTotalVolumeStats(days: Int): TotalVolumeStats = withContext(Dispatchers.IO) {
        val (startDate, endDate) = DateRangeHelper.getLastNDays(days)
        val startEpochDay = startDate / (24 * 60 * 60 * 1000)
        val endEpochDay = endDate / (24 * 60 * 60 * 1000)
        val currentVolume = trainingDao.getTotalVolumeInRange(startEpochDay, endEpochDay)

        // Obliczamy volume dla poprzedniego okresu (dla porównania)
        val previousPeriodEndEpochDay = startEpochDay - 1
        val previousPeriodStartEpochDay = previousPeriodEndEpochDay - (endEpochDay - startEpochDay)
        val previousVolume = trainingDao.getTotalVolumeInRange(previousPeriodStartEpochDay, previousPeriodEndEpochDay)

        // Obliczamy % zmianę
        val changePercentage = if (previousVolume > 0) {
            ((currentVolume - previousVolume) / previousVolume) * 100f
        } else {
            null
        }

        TotalVolumeStats(
            totalVolume = currentVolume,
            averagePerDay = currentVolume / days,
            days = days,
            changePercentage = changePercentage
        )
    }

    /**
     * Pobiera rozkład volume po grupach mięśniowych
     * Parsuje primaryMuscles[0] z każdego ćwiczenia
     */
    suspend fun getMuscleGroupDistribution(days: Int): List<MuscleGroupVolume> = withContext(Dispatchers.IO) {
        val (startDate, endDate) = DateRangeHelper.getLastNDays(days)
        val startEpochDay = startDate / (24 * 60 * 60 * 1000)
        val endEpochDay = endDate / (24 * 60 * 60 * 1000)
        val rawData = trainingDao.getMuscleGroupVolumeData(startEpochDay, endEpochDay)

        // Grupujemy po pierwszym primary muscle
        val groupedData = mutableMapOf<String, MuscleGroupVolume>()

        for (raw in rawData) {
            try {
                // Parsujemy JSON array primaryMuscles
                val musclesArray = JSONArray(raw.primaryMuscles)
                if (musclesArray.length() > 0) {
                    val primaryMuscle = musclesArray.getString(0).lowercase()

                    val existing = groupedData[primaryMuscle]
                    if (existing != null) {
                        // Agregujemy dane
                        groupedData[primaryMuscle] = existing.copy(
                            totalVolume = existing.totalVolume + raw.totalVolume,
                            exerciseCount = existing.exerciseCount + raw.exerciseCount,
                            setCount = existing.setCount + raw.setCount
                        )
                    } else {
                        // Nowa grupa mięśniowa
                        groupedData[primaryMuscle] = MuscleGroupVolume(
                            muscleGroup = primaryMuscle.replaceFirstChar { it.uppercase() },
                            totalVolume = raw.totalVolume,
                            exerciseCount = raw.exerciseCount,
                            setCount = raw.setCount
                        )
                    }
                }
            } catch (e: Exception) {
                // Ignorujemy błędy parsowania
                continue
            }
        }

        // Obliczamy procenty
        val totalVolume = groupedData.values.sumOf { it.totalVolume.toDouble() }.toFloat()
        groupedData.values.forEach { group ->
            group.percentage = if (totalVolume > 0) {
                (group.totalVolume / totalVolume) * 100f
            } else {
                0f
            }
        }

        // Sortujemy po volume (największe pierwsze)
        groupedData.values.sortedByDescending { it.totalVolume }
    }

    /**
     * Pobiera wszystkie serie dla ćwiczenia z datami (do wykresów)
     */
    suspend fun getAllSetsForExercise(exerciseId: String): List<SetWithDate> = withContext(Dispatchers.IO) {
        val rawData = trainingDao.getAllSetsForExerciseWithDates(exerciseId)
        rawData.map { raw ->
            SetWithDate(
                setId = raw.setId,
                sessionDate = raw.sessionDate,
                exerciseId = raw.exerciseId,
                weight = raw.weight,
                reps = raw.reps,
                order = raw.order
            )
        }
    }

    /**
     * Oblicza historię estimated 1RM dla ćwiczenia
     * Zwraca najlepszy 1RM z każdego dnia treningowego
     */
    suspend fun getEstimated1RMHistory(
        exerciseId: String,
        formula: OneRMFormula = OneRMFormula.EPLEY
    ): List<Pair<Long, Float>> = withContext(Dispatchers.IO) {
        val sets = getAllSetsForExercise(exerciseId)

        val MILLIS_IN_DAY = 24L * 60 * 60 * 1000

        sets.groupBy { it.sessionDate }  // sessionDate to epochDay
            .map { (epochDay, setsInDay) ->
                val maxOneRM = setsInDay.maxOfOrNull { set ->
                    OneRMCalculator.calculate(set.weight, set.reps, formula)
                } ?: 0f
                val millis = epochDay * MILLIS_IN_DAY
                millis to maxOneRM
            }
            .sortedBy { it.first }
    }

    /**
     * Pobiera historię volume dla ćwiczenia
     */
    suspend fun getVolumeHistory(exerciseId: String, days: Int): List<VolumeData> = withContext(Dispatchers.IO) {
        val (startDate, endDate) = DateRangeHelper.getLastNDays(days)
        val startEpochDay = startDate / (24 * 60 * 60 * 1000)
        val endEpochDay = endDate / (24 * 60 * 60 * 1000)
        val rawData = trainingDao.getVolumeDataForExercise(exerciseId, startEpochDay, endEpochDay)

        rawData.map { raw ->
            VolumeData(
                date = raw.date,
                volume = raw.volume
            )
        }
    }

    /**
     * Pobiera historię najcięższych serii (Top Set Tracking)
     */
    suspend fun getTopSetHistory(exerciseId: String): List<Pair<Long, Float>> = withContext(Dispatchers.IO) {
        val rawData = trainingDao.getTopSetsByDate(exerciseId)
        rawData.map { raw ->
            raw.date to raw.weight
        }
    }

    /**
     * Pobiera Rep Max Matrix dla ćwiczenia
     */
    suspend fun getRepMaxMatrix(exerciseId: String): RepMaxMatrix = withContext(Dispatchers.IO) {
        val exercise = exerciseDao.getExerciseById(exerciseId)
        val rawRecords = trainingDao.getRepMaxRecords(exerciseId)

        val recordsMap = rawRecords.associate { raw ->
            raw.reps to RepMaxRecord(
                reps = raw.reps,
                weight = raw.weight,
                date = raw.date,
                exerciseId = raw.exerciseId
            )
        }

        RepMaxMatrix(
            exerciseId = exerciseId,
            exerciseName = exercise?.name ?: "Unknown",
            records = recordsMap
        )
    }

    /**
     * Pobiera historię powtórzeń dla konkretnego ciężaru
     */
    suspend fun getRepsAtWeightHistory(exerciseId: String, targetWeight: Float): List<Pair<Long, Int>> = withContext(Dispatchers.IO) {
        val sets = getAllSetsForExercise(exerciseId)

        // Filtrujemy serie z danym ciężarem (z tolerancją ±0.5kg)
        sets.filter { kotlin.math.abs(it.weight - targetWeight) <= 0.5f }
            .map { it.sessionDate to it.reps }
            .sortedBy { it.first }
    }

    /**
     * Pobiera listę unikalnych ciężarów używanych w ćwiczeniu
     * Przydatne do selekcji ciężaru w "Reps at Weight Chart"
     */
    suspend fun getUniqueWeightsForExercise(exerciseId: String): List<Float> = withContext(Dispatchers.IO) {
        val sets = getAllSetsForExercise(exerciseId)
        sets.map { it.weight }.distinct().sorted()
    }
}

