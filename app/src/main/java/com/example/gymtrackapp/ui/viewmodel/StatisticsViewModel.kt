package com.example.gymtrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackapp.data.entity.statistics.*
import com.example.gymtrackapp.data.repository.StatisticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

/**
 * ViewModel dla ekranu statystyk
 */
class StatisticsViewModel(
    private val repository: StatisticsRepository
) : ViewModel() {

    // ============= SUMMARY TAB STATE =============

    private val _heatmapData = MutableStateFlow<List<TrainingDayData>>(emptyList())
    val heatmapData: StateFlow<List<TrainingDayData>> = _heatmapData.asStateFlow()

    private val _heatmapDaysRange = MutableStateFlow(30)
    val heatmapDaysRange: StateFlow<Int> = _heatmapDaysRange.asStateFlow()

    private val _topPersonalRecords = MutableStateFlow<List<PersonalRecord>>(emptyList())
    val topPersonalRecords: StateFlow<List<PersonalRecord>> = _topPersonalRecords.asStateFlow()

    private val _volumeStats7Days = MutableStateFlow<TotalVolumeStats?>(null)
    val volumeStats7Days: StateFlow<TotalVolumeStats?> = _volumeStats7Days.asStateFlow()

    private val _volumeStats30Days = MutableStateFlow<TotalVolumeStats?>(null)
    val volumeStats30Days: StateFlow<TotalVolumeStats?> = _volumeStats30Days.asStateFlow()

    private val _selectedVolumeTab = MutableStateFlow(0) // 0 = 7 dni, 1 = 30 dni
    val selectedVolumeTab: StateFlow<Int> = _selectedVolumeTab.asStateFlow()

    private val _muscleGroupDistribution = MutableStateFlow<List<MuscleGroupVolume>>(emptyList())
    val muscleGroupDistribution: StateFlow<List<MuscleGroupVolume>> = _muscleGroupDistribution.asStateFlow()

    private val _muscleDistributionDays = MutableStateFlow(7)
    val muscleDistributionDays: StateFlow<Int> = _muscleDistributionDays.asStateFlow()

    // Loading states
    private val _isLoadingSummary = MutableStateFlow(false)
    val isLoadingSummary: StateFlow<Boolean> = _isLoadingSummary.asStateFlow()

    // ============= CHARTS TAB STATE =============

    private val _selectedExerciseId = MutableStateFlow<String?>(null)
    val selectedExerciseId: StateFlow<String?> = _selectedExerciseId.asStateFlow()

    private val _estimated1RMHistory = MutableStateFlow<List<Pair<Long, Float>>>(emptyList())
    val estimated1RMHistory: StateFlow<List<Pair<Long, Float>>> = _estimated1RMHistory.asStateFlow()

    private val _volumeHistory = MutableStateFlow<List<VolumeData>>(emptyList())
    val volumeHistory: StateFlow<List<VolumeData>> = _volumeHistory.asStateFlow()

    private val _topSetHistory = MutableStateFlow<List<Pair<Long, Float>>>(emptyList())
    val topSetHistory: StateFlow<List<Pair<Long, Float>>> = _topSetHistory.asStateFlow()

    private val _repMaxMatrix = MutableStateFlow<RepMaxMatrix?>(null)
    val repMaxMatrix: StateFlow<RepMaxMatrix?> = _repMaxMatrix.asStateFlow()

    private val _selectedWeightForReps = MutableStateFlow<Float?>(null)
    val selectedWeightForReps: StateFlow<Float?> = _selectedWeightForReps.asStateFlow()

    private val _repsAtWeightHistory = MutableStateFlow<List<Pair<Long, Int>>>(emptyList())
    val repsAtWeightHistory: StateFlow<List<Pair<Long, Int>>> = _repsAtWeightHistory.asStateFlow()

    private val _availableWeights = MutableStateFlow<List<Float>>(emptyList())
    val availableWeights: StateFlow<List<Float>> = _availableWeights.asStateFlow()

    private val _chartsDaysRange = MutableStateFlow(90) // Domyślnie ostatnie 90 dni dla wykresów
    val chartsDaysRange: StateFlow<Int> = _chartsDaysRange.asStateFlow()

    private val _selectedOneRMFormula = MutableStateFlow(OneRMFormula.EPLEY)
    val selectedOneRMFormula: StateFlow<OneRMFormula> = _selectedOneRMFormula.asStateFlow()

    private val _isLoadingCharts = MutableStateFlow(false)
    val isLoadingCharts: StateFlow<Boolean> = _isLoadingCharts.asStateFlow()

    // ============= SUMMARY TAB FUNCTIONS =============

    /**
     * Ładuje wszystkie dane dla zakładki Summary
     */
    fun loadSummaryData() {
        viewModelScope.launch {
            _isLoadingSummary.value = true
            try {
                awaitAll(
                    async { loadHeatmapData() },
                    async { loadTopPRs() },
                    async { loadVolumeStats() },
                    async { loadMuscleDistribution() }
                )
            } finally {
                _isLoadingSummary.value = false
            }
        }
    }

    /**
     * Ładuje dane dla heatmapy
     */
    private suspend fun loadHeatmapData() {
        val data = repository.getTrainingHeatmapData(_heatmapDaysRange.value)
        _heatmapData.value = data
    }

    /**
     * Zmienia zakres dni dla heatmapy
     */
    fun setHeatmapDaysRange(days: Int) {
        _heatmapDaysRange.value = days
        viewModelScope.launch {
            loadHeatmapData()
        }
    }

    /**
     * Ładuje Top 3 najnowsze Personal Records
     */
    private suspend fun loadTopPRs() {
        val prs = repository.getTopPersonalRecords(limit = 3)
        _topPersonalRecords.value = prs
    }

    /**
     * Ładuje statystyki volume dla 7 i 30 dni
     */
    private suspend fun loadVolumeStats() {
        val stats7 = repository.getTotalVolumeStats(7)
        val stats30 = repository.getTotalVolumeStats(30)
        _volumeStats7Days.value = stats7
        _volumeStats30Days.value = stats30
    }

    /**
     * Zmienia aktywny tab w sekcji Total Volume
     */
    fun setVolumeTab(tabIndex: Int) {
        _selectedVolumeTab.value = tabIndex
    }

    /**
     * Ładuje rozkład volume po grupach mięśniowych
     */
    private suspend fun loadMuscleDistribution() {
        val distribution = repository.getMuscleGroupDistribution(_muscleDistributionDays.value)
        _muscleGroupDistribution.value = distribution
    }

    /**
     * Zmienia zakres dni dla rozkładu mięśni
     */
    fun setMuscleDistributionDays(days: Int) {
        _muscleDistributionDays.value = days
        viewModelScope.launch {
            loadMuscleDistribution()
        }
    }

    // ============= CHARTS TAB FUNCTIONS =============

    /**
     * Wybiera ćwiczenie do wyświetlenia wykresów
     */
    fun selectExercise(exerciseId: String) {
        if (_selectedExerciseId.value == exerciseId) return

        _selectedExerciseId.value = exerciseId
        loadChartsForExercise(exerciseId)
    }

    /**
     * Ładuje wszystkie wykresy dla wybranego ćwiczenia
     */
    private fun loadChartsForExercise(exerciseId: String) {
        viewModelScope.launch {
            _isLoadingCharts.value = true
            try {
                // Równoległe ładowanie danych
                launch { load1RMHistory(exerciseId) }
                launch { loadVolumeHistory(exerciseId) }
                launch { loadTopSetHistory(exerciseId) }
                launch { loadRepMaxMatrix(exerciseId) }
                launch { loadAvailableWeights(exerciseId) }
            } finally {
                _isLoadingCharts.value = false
            }
        }
    }

    /**
     * Ładuje historię estimated 1RM
     */
    private suspend fun load1RMHistory(exerciseId: String) {
        val history = repository.getEstimated1RMHistory(exerciseId, _selectedOneRMFormula.value)
        _estimated1RMHistory.value = history
    }

    /**
     * Zmienia wzór używany do obliczania 1RM
     */
    fun setOneRMFormula(formula: OneRMFormula) {
        _selectedOneRMFormula.value = formula
        _selectedExerciseId.value?.let { exerciseId ->
            viewModelScope.launch {
                load1RMHistory(exerciseId)
            }
        }
    }

    /**
     * Ładuje historię volume
     */
    private suspend fun loadVolumeHistory(exerciseId: String) {
        val history = repository.getVolumeHistory(exerciseId, _chartsDaysRange.value)
        _volumeHistory.value = history
    }

    /**
     * Ładuje historię najcięższych serii
     */
    private suspend fun loadTopSetHistory(exerciseId: String) {
        val history = repository.getTopSetHistory(exerciseId)
        _topSetHistory.value = history
    }

    /**
     * Ładuje Rep Max Matrix
     */
    private suspend fun loadRepMaxMatrix(exerciseId: String) {
        val matrix = repository.getRepMaxMatrix(exerciseId)
        _repMaxMatrix.value = matrix
    }

    /**
     * Ładuje dostępne ciężary dla ćwiczenia
     */
    private suspend fun loadAvailableWeights(exerciseId: String) {
        val weights = repository.getUniqueWeightsForExercise(exerciseId)
        _availableWeights.value = weights
    }

    /**
     * Wybiera ciężar do śledzenia w "Reps at Weight"
     */
    fun selectWeightForReps(weight: Float) {
        _selectedWeightForReps.value = weight
        _selectedExerciseId.value?.let { exerciseId ->
            viewModelScope.launch {
                val history = repository.getRepsAtWeightHistory(exerciseId, weight)
                _repsAtWeightHistory.value = history
            }
        }
    }

    /**
     * Zmienia zakres dni dla wykresów
     */
    fun setChartsDaysRange(days: Int) {
        _chartsDaysRange.value = days
        _selectedExerciseId.value?.let { exerciseId ->
            viewModelScope.launch {
                loadVolumeHistory(exerciseId)
                // Możemy też przeładować inne wykresy jeśli potrzebują tego zakresu
            }
        }
    }

    /**
     * Odświeża wszystkie dane
     */
    fun refresh() {
        loadSummaryData()
        _selectedExerciseId.value?.let { exerciseId ->
            loadChartsForExercise(exerciseId)
        }
    }
}

